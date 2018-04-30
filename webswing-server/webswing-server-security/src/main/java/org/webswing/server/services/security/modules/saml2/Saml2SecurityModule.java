package org.webswing.server.services.security.modules.saml2;

import main.Main;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webswing.server.common.util.CommonUtil;
import org.webswing.server.services.security.api.AbstractWebswingUser;
import org.webswing.server.services.security.api.WebswingAuthenticationException;
import org.webswing.server.services.security.modules.AbstractExtendableSecurityModule;
import org.webswing.server.services.security.modules.saml2.com.lastpass.saml.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.CertificateException;

public class Saml2SecurityModule extends AbstractExtendableSecurityModule<Saml2SecurityModuleConfig> {
	private static final Logger log = LoggerFactory.getLogger(Saml2SecurityModule.class);
	private static boolean staticInit = false;
	private static final String SAML_PARAMETER = "SAMLResponse";

	static {
		try {
			SAMLInit.initialize();
			staticInit = true;
		} catch (SAMLException e) {
			log.error("Initializing SAML2 client failed.", e);
		}
	}

	private SAMLClient client;

	public Saml2SecurityModule(Saml2SecurityModuleConfig config) {
		super(config);
	}

	public void init() {
		super.init();
		if (!staticInit) {
			throw new RuntimeException("SAML2 module was not initialized correctly. Not possible to configure security module. ");
		}
		try {
			String idpFile = getConfig().getIdentityProviderMetadataFile();
			File file = getMetadataFile(idpFile);
			if (file == null || !file.isFile()) {
				throw new SAMLException("The SAML2 Identity provider metadata file " + idpFile + " does not exist.");
			}
			String consumerUrl = getConfig().getServiceProviderConsumerUrl();
			if (StringUtils.isEmpty(consumerUrl)) {
				throw new SAMLException("The SAML2 serviceProviderConsumerUrl property must not be empty.");
			}
			String entityId = getConfig().getServiceProviderEntityId();
			if (StringUtils.isEmpty(entityId)) {
				throw new RuntimeException("The SAML2 Service provider entityId property must not be empty.");
			}
			String keyStore = getConfig().getContext().replaceVariables(getConfig().getDecryptionKeyStore());
			PrivateKey privateKey = null;
			if (StringUtils.isNotEmpty(keyStore)) {
				File keyStoreFile = getConfig().getContext().resolveFile(keyStore);
				if (file == null || !file.exists()) {
					log.error("Failed to load keystore.", new Exception(keyStore + " does not exits."));
				} else {
					String ext = FilenameUtils.getExtension(keyStoreFile.getName());
					String fileType = "p12".equals(ext) ? "PKCS12" : "JKS";
					String keyStoreAlias = getConfig().getContext().replaceVariables(getConfig().getDecryptionKeyAlias());
					String keyStorePwd = getConfig().getContext().replaceVariables(getConfig().getDecryptionKeyStorePwd());
					String keyPwd = getConfig().getContext().replaceVariables(getConfig().getDecryptionKeyPwd());
					try {
						KeyStore store = loadKeyStore(keyStoreFile, keyStorePwd, fileType);
						privateKey = (PrivateKey) store.getKey(keyStoreAlias, null == keyPwd ? null : keyPwd.toCharArray());
					} catch (Exception e) {
						log.error("Failed to load private key from keystore", e);
					}
				}
			}
			IdPConfig idpConfig = new IdPConfig(file);
			try {
				String spTemplate = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("saml2/saml2-sp-template.xml"));
				spTemplate = spTemplate.replace("${entityID}", entityId);
				spTemplate = spTemplate.replace("${consumerUrl}", consumerUrl);
				SPConfig spConfig = new SPConfig(new ByteArrayInputStream(spTemplate.getBytes("UTF-8")));
				if (privateKey != null) {
					spConfig.setPrivateKey(privateKey);
				}
				client = new SAMLClient(spConfig, idpConfig);
			} catch (IOException e) {
				throw new SAMLException("The SAML2 template file could not be loaded.", e);
			}
		} catch (SAMLException e) {
			throw new RuntimeException("Failed to initialize SAML2 webswing security module. ", e);
		}
	}

	private File getMetadataFile(String idpFile) throws SAMLException {
		File metadata = getConfig().getContext().resolveFile(idpFile);
		if (metadata != null) {
			return metadata;
		} else {
			try {
				File tempFile = new File(Main.getTempDir(), Base64.encodeBase64URLSafeString(idpFile.getBytes()));
				FileUtils.copyURLToFile(new URL(idpFile), tempFile);
				return tempFile;
			} catch (MalformedURLException e) {
				return null;
			} catch (IOException e) {
				throw new SAMLException("Failed to load SAML2 Identity provider metadata.", e);
			}
		}
	}

	@Override
	protected void serveLoginPartial(HttpServletRequest request, HttpServletResponse response, WebswingAuthenticationException exception) throws IOException {
		if (exception != null) {
			sendPartialHtml(request, response, "errorPartial.html", exception);
		} else {
			String url = getSaml2RedirectUrl(request);
			sendRedirect(request, response, url);
		}
	}

	private String getSaml2RedirectUrl(HttpServletRequest request) throws IOException {
		String requestId = SAMLUtils.generateRequestId();
		String authrequest;
		try {
			authrequest = client.generateAuthnRequest(requestId);
		} catch (SAMLException e1) {
			throw new IOException("Failed to build SAML request.", e1);
		}
		String url = client.getIdPConfig().getLoginUrl();
		String searchQuery = request.getQueryString();
		if (StringUtils.isNotBlank(searchQuery)) {
			url = CommonUtil.addParam(url, searchQuery);
		}
		String param = "SAMLRequest=" + URLEncoder.encode(authrequest, "UTF-8");
		url = CommonUtil.addParam(url, param);
		return url;
	}

	@Override
	protected AbstractWebswingUser authenticate(HttpServletRequest request) throws WebswingAuthenticationException {
		String samlResponse = request.getParameter(SAML_PARAMETER);
		if (!StringUtils.isEmpty(samlResponse)) {
			AttributeSet aset;
			try {
				aset = client.validateResponse(samlResponse);
				String user = aset.getNameId();
				logSuccess(request, user);
				return new Saml2User(samlResponse, user, aset.getAttributes());
			} catch (SAMLException e1) {
				logFailure(request, null, "Failed to authenticate." + e1.getMessage());
				log.error("Failed to authenticate", e1);
				throw new WebswingAuthenticationException("Failed to auhenticate. " + e1.getMessage(), WebswingAuthenticationException.FAILED_TO_AUTHENTICATE, e1);
			}
		}
		return null;
	}

	@Override
	public void doLogout(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String logoutUrl = replaceVar(getConfig().getLogoutUrl());
		logoutRedirect(request, response, logoutUrl);
	}

	public static KeyStore loadKeyStore(final File keystoreFile, final String password, final String keyStoreType) throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
		final URL keystoreUrl = keystoreFile.toURI().toURL();
		final KeyStore keystore = KeyStore.getInstance(keyStoreType);
		InputStream is = null;
		try {
			is = keystoreUrl.openStream();
			keystore.load(is, null == password ? null : password.toCharArray());
		} finally {
			if (null != is) {
				is.close();
			}
		}
		return keystore;
	}
}
