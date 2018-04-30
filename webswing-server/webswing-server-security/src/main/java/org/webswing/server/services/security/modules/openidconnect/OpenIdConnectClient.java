package org.webswing.server.services.security.modules.openidconnect;

import com.google.api.client.auth.oauth2.*;
import com.google.api.client.auth.openidconnect.IdToken;
import com.google.api.client.auth.openidconnect.IdTokenResponse;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webswing.server.services.security.api.AbstractWebswingUser;
import org.webswing.toolkit.util.DeamonThreadFactory;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by vikto on 06-Feb-17.
 */
public class OpenIdConnectClient {
	private static final Logger log = LoggerFactory.getLogger(OpenIdConnectClient.class);

	private static final ScheduledExecutorService aliveChecker = Executors.newSingleThreadScheduledExecutor(DeamonThreadFactory.getInstance("Webswing OpenID watchdog"));

	public static final String CODE = "code";
	public static final String ISSUER = "issuer";
	public static final String AUTHORIZATION_ENDPOINT = "authorization_endpoint";
	public static final String TOKEN_ENDPOINT = "token_endpoint";
	public static final String OPENID_SCOPE = "openid profile";
	public static final String CLIENT_ID = "client_id";

	private final String roleAttrName;
	private final String usernameAttrName;
	private URL discovery;
	private URL callback;
	private String clientId;
	private String clientSecret;
	private String logoutUrl;
	private NetHttpTransport.Builder transportBuilder;
	private Credential.AccessMethod method;
	private ClientParametersAuthentication auth;
	private JacksonFactory jsonFactory = new JacksonFactory();
	private AuthorizationCodeFlow flow;

	public OpenIdConnectClient(URL discovery, URL callback, String clientId, String clientSecret, boolean disableCertValidation, File trustedCert, String roleAttrName, String usernameAttrName) throws Exception {
		this.callback = callback;
		this.roleAttrName = roleAttrName;
		this.usernameAttrName = usernameAttrName;
		this.discovery = discovery;
		this.clientId = clientId;
		this.clientSecret = clientSecret;
		transportBuilder = new NetHttpTransport.Builder();
		if (disableCertValidation) {
			transportBuilder.doNotValidateCertificate();
		}
		if (trustedCert != null) {
			transportBuilder.trustCertificatesFromStream(new FileInputStream(trustedCert));
		}

		if (StringUtils.isNotBlank(clientSecret)) {
			auth = new ClientParametersAuthentication(clientId, clientSecret);
		}
		method = BearerToken.authorizationHeaderAccessMethod();

		initializeFlow();

		Integer period = Integer.getInteger("org.webswing.openid.ping.interval", 60);
		aliveChecker.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				initializeFlow();
			}
		}, period, period, TimeUnit.SECONDS);
	}

	private synchronized void initializeFlow() {
		URL authURI;
		URL tokenURI;
		String issuer;
		if (discovery != null) {
			try {
				log.info("Loading OpenID Connect definition from: " + discovery);
				HttpResponse response = transportBuilder.build().createRequestFactory().buildGetRequest(new GenericUrl(discovery)).execute();
				Map<String, Object> json = jsonFactory.createJsonParser(response.getContent()).parse(Map.class);
				if (json.get(ISSUER) != null) {
					issuer = (String) json.get(ISSUER);
				} else {
					throw new RuntimeException("Discovery json does not define issuer field");
				}
				if (json.get(AUTHORIZATION_ENDPOINT) != null) {
					authURI = new URL((String) json.get(AUTHORIZATION_ENDPOINT));
				} else {
					throw new RuntimeException("Discovery json does not define authorization_endpoint field");
				}
				if (json.get(TOKEN_ENDPOINT) != null) {
					tokenURI = new URL((String) json.get(TOKEN_ENDPOINT));
				} else {
					throw new RuntimeException("Discovery json does not define token_endpoint field");
				}
			} catch (IOException e) {
				log.error("Failed resolve OpenID Connect details :" + e.getMessage());
				log.debug("Failed resolve OpenID Connect details", e);
				flow = null;
				return;
			}
		} else {
			throw new RuntimeException("OpenID Connect Discovery URL is not defined.");
		}
		if (flow == null && authURI != null && tokenURI != null && issuer != null) {
			AuthorizationCodeFlow.Builder builder = new AuthorizationCodeFlow.Builder(method, transportBuilder.build(), jsonFactory, new GenericUrl(tokenURI), auth, clientId, authURI.toString());
			builder.setScopes(Collections.singletonList(OPENID_SCOPE));
			flow = builder.build();
		}
	}

	public String getOpenIDRedirectUrl() throws IOException {
		if (flow != null) {
			return flow.newAuthorizationUrl().setRedirectUri(callback.toString()).build();
		}
		return null;
	}

	public static String getCode(HttpServletRequest request) {
		String query = request.getQueryString();
		if (query == null)
			return null;
		String[] params = query.split("&");
		for (String param : params) {
			int eq = param.indexOf('=');
			if (eq == -1)
				continue;
			String name = param.substring(0, eq);
			if (!name.equals(CODE))
				continue;
			return param.substring(eq + 1);
		}
		return null;
	}

	private static IdToken executeIdToken(TokenRequest tokenRequest) throws IOException {
		IdTokenResponse idTokenResponse = IdTokenResponse.execute(tokenRequest);
		return idTokenResponse.parseIdToken();
	}

	public AbstractWebswingUser getUser(String openIdCode, Map<String, Serializable> extraAttribs) throws IOException {
		if (flow != null) {
			AuthorizationCodeTokenRequest tokenRequest = flow.newTokenRequest(openIdCode).setRedirectUri(callback.toString());
			tokenRequest.set(CLIENT_ID, flow.getClientId());
			IdToken result = executeIdToken(tokenRequest);
			OpenIdWebswingUser user = new OpenIdWebswingUser(result, this.usernameAttrName, this.roleAttrName, extraAttribs);
			return user;
		}
		return null;
	}

	public boolean isInitialized() {
		return flow != null;
	}

	public String getLogoutUrl() {
		return logoutUrl;
	}

	public void setLogoutUrl(String logoutUrl) {
		this.logoutUrl = logoutUrl;
	}
}
