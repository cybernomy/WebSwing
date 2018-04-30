package org.webswing.server.services.security.extension.onetimeurl;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.UndeclaredThrowableException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webswing.server.common.util.ConfigUtil;
import org.webswing.server.services.security.api.AbstractWebswingUser;
import org.webswing.server.services.security.api.WebswingAction;
import org.webswing.server.services.security.api.WebswingAuthenticationException;
import org.webswing.server.services.security.extension.api.SecurityModuleExtension;
import org.webswing.server.services.security.modules.AbstractExtendableSecurityModule;
import org.webswing.server.services.security.modules.AbstractSecurityModule;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;

public class OneTimeUrlSecurityExtension extends SecurityModuleExtension<OneTimeUrlSecurityExtensionConfig> {
	private static final String SECURITY_TOKEN = "securityToken";
	private static final Logger log = LoggerFactory.getLogger(OneTimeUrlSecurityExtension.class);

	public OneTimeUrlSecurityExtension(OneTimeUrlSecurityExtensionConfig config) {
		super(config);
	}

	@Override
	public AbstractWebswingUser doSufficientPreValidation(AbstractExtendableSecurityModule<?> m, HttpServletRequest request, HttpServletResponse response) throws WebswingAuthenticationException {
		Map<String, Object> requestData = m.getLoginRequest(request);
		if (requestData != null && requestData.containsKey(SECURITY_TOKEN)) {
			AbstractWebswingUser result = verifyOneTimePassword(request, (String) requestData.get(SECURITY_TOKEN));
			return result;
		}
		return null;
	}

	@Override
	public boolean serveAuthenticated(AbstractWebswingUser user, String path, HttpServletRequest req, HttpServletResponse res) {
		if (path.equals("/oneTimeUrl") && user.isPermitted(WebswingAction.rest_getOneTimePassword.name())) {
			String requestor = req.getParameter("requestorId");
			String userName = req.getParameter("user");
			String roles = req.getParameter("roles");
			String permissions = req.getParameter("permissions");
			String[] rolesArray = roles != null ? roles.split(",") : null;
			String[] permissionsArray = permissions != null ? permissions.split(",") : null;
			List<List<String>> attributes = new ArrayList<>();
			for (String paramName : req.getParameterMap().keySet()) {
				attributes.add(Arrays.asList(paramName, req.getParameter(paramName)));
			}
			try {
				String otp = generateOneTimePassword(requestor, userName, rolesArray, permissionsArray, attributes);
				res.getWriter().print(otp);
				res.flushBuffer();
				res.getWriter().close();
			} catch (Exception e) {
				log.error("Failed to generate OneTimeUrl", e);
				try {
					res.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
				} catch (IOException e1) {
				}
			}
			return true;
		} else {
			return false;
		}
	}

	public AbstractWebswingUser verifyOneTimePassword(HttpServletRequest request, String otp) throws WebswingAuthenticationException {
		try {
			OtpTokenData token = parseOtpTokenString(otp);
			verifyTokenValid(token);
			String secret = getOtpSecret(token.getRequestorId());
			String message = getOtpMessage(token);
			String crypto = getOtpCrypto(token.getRequestorId());
			int validityInterval = getOtpValidForSec(token.getRequestorId());
			for (int i = -1; i <= 1; i++) {
				String expectedPassword = calculateTotpString(crypto, secret, message, i, validityInterval);
				if (expectedPassword.equals(token.getOneTimePassword())) {
					return createUser(request, token);
				}
			}
			logFailure(request, null, "Invalid or expired OTP. ");
			throw new WebswingAuthenticationException("Invalid or expired OTP. ");
		} catch (Exception e) {
			logFailure(request, null, "Failed to verify OTP. " + e.getMessage());
			throw new WebswingAuthenticationException("Failed to verify OTP. " + e.getMessage(), e);
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public String generateOneTimePassword(String requestorId, String user, String[] roles, String[] permissions, List attributes) throws WebswingAuthenticationException {
		String encoded;
		try {
			OtpTokenDataImpl token = new OtpTokenDataImpl();
			token.setUser(user);
			token.setRequestorId(requestorId);
			token.setAttributes(attributes);
			token.setRoles(Arrays.asList(roles));
			token.setPermissions(Arrays.asList(permissions));
			verifyTokenValid(token);
			String secret = getOtpSecret(requestorId);
			String message = getOtpMessage(token);
			String crypto = getOtpCrypto(requestorId);
			String otp = calculateTotpString(crypto, secret, message, 0, getOtpValidForSec(requestorId));
			token.setOneTimePassword(otp);
			encoded = encodeOtpToken(token);
		} catch (Exception e) {
			throw new WebswingAuthenticationException("Failed to generate OTP. " + e.getMessage(), e);
		}
		return encoded;
	}

	public void verifyTokenValid(OtpTokenData token) throws WebswingAuthenticationException {
		if (StringUtils.isEmpty(token.getUser())) {
			throw new WebswingAuthenticationException("User must not be empty.");
		}
		if (StringUtils.isEmpty(token.getRequestorId())) {
			throw new WebswingAuthenticationException("Requestor Id must not be empty.");
		}
		if (getConfig().getApiKeys() == null || getConfigForRequestor(getConfig(), token.getRequestorId()) == null) {
			throw new WebswingAuthenticationException("RequestorId not configured.");
		}
	}

	public static OtpAccessConfig getConfigForRequestor(OneTimeUrlSecurityExtensionConfig config, String requestorId) {
		if (config != null) {
			for (OtpAccessConfig oac : config.getApiKeys()) {
				if (StringUtils.equals(requestorId, oac.getRequestorId())) {
					return oac;
				}
			}
		}
		return null;
	}

	public int getOtpValidForSec(String requestingClient) {
		Integer result = null;
		OtpAccessConfig c = getConfigForRequestor(getConfig(), requestingClient);
		result = c.getValidForSec();
		return result == null ? 30 : result;
	}

	public String getOtpCrypto(String requestingClient) {
		String result = null;
		OtpAccessConfig c = getConfigForRequestor(getConfig(), requestingClient);
		result = c.getHMacAlgo();
		return result == null ? "HmacSHA512" : result;
	}

	public String getOtpSecret(String requestingClient) throws WebswingAuthenticationException {
		String result = null;
		OtpAccessConfig c = getConfigForRequestor(getConfig(), requestingClient);
		result = c.getSecret();
		if (result == null) {
			log.error("Secret not found for requestor '" + requestingClient + "'");
			throw new WebswingAuthenticationException("OTP Access not configured.");
		}
		return result;
	}

	public static String getOtpMessage(OtpTokenData token) {
		String attributes = listToString(token.getAttributes());
		String permissions = listToString(token.getPermissions());
		String roles = listToString(token.getRoles());
		return "" + token.getUser() + attributes + roles + permissions;
	}

	@SuppressWarnings("rawtypes")
	private static String listToString(List array) {
		String result = "";
		if (array != null) {
			for (Object item : array) {
				if (item instanceof List) {
					result += listToString((List) item);
				} else {
					result += item;
				}
			}
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	public OtpTokenData parseOtpTokenString(String otp) throws WebswingAuthenticationException {
		try {
			Map<String, Object> tokenRaw = AbstractSecurityModule.getMapper().readValue(Base64.decodeBase64(otp), Map.class);
			OtpTokenData token = ConfigUtil.instantiateConfig(tokenRaw, OtpTokenData.class);
			return token;
		} catch (Exception e) {
			throw new WebswingAuthenticationException("Failed to parse OTP token", e);
		}
	}

	public static String encodeOtpToken(OtpTokenData token) throws WebswingAuthenticationException {
		try {
			byte[] value = AbstractSecurityModule.getMapper().writeValueAsBytes(token);
			return Base64.encodeBase64String(value);
		} catch (Exception e) {
			throw new WebswingAuthenticationException("Failed to encode OTP token", e);
		}
	}

	public static String calculateTotpString(String crypto, String secret, String message, int intervalshift, int intervalLengthSec) {
		long time = System.currentTimeMillis() / 1000 / intervalLengthSec;
		time = time + intervalshift;
		message = message + time;
		byte[] msg = message.getBytes();
		byte[] key = secret.getBytes();
		byte[] hash = hmacSha(crypto, key, msg);
		return Base64.encodeBase64String(hash);
	}

	private static byte[] hmacSha(String crypto, byte[] keyBytes, byte[] text) {
		try {
			Mac hmac;
			hmac = Mac.getInstance(crypto);
			SecretKeySpec macKey = new SecretKeySpec(keyBytes, "RAW");
			hmac.init(macKey);
			return hmac.doFinal(text);
		} catch (GeneralSecurityException gse) {
			throw new UndeclaredThrowableException(gse);
		}
	}

	@SuppressWarnings("rawtypes")
	public AbstractWebswingUser createUser(HttpServletRequest req, OtpTokenData token) throws WebswingAuthenticationException {
		OtpAccessConfig c = getConfigForRequestor(getConfig(), token.getRequestorId());
		Map<String, Serializable> attributes = new HashMap<String, Serializable>();
		Set<String> roles = new HashSet<>();
		Set<String> permissions = new HashSet<>();

		if (token.getAttributes() != null) {
			for (Object attribute : token.getAttributes()) {
				if (attribute instanceof List && ((List) attribute).size() == 2) {
					List at = (List) attribute;
					attributes.put((String) at.get(0), (Serializable) at.get(1));
				}
			}
		}
		if (token.getRoles() != null) {
			roles.addAll(token.getRoles());
		}
		if (token.getPermissions() != null) {
			permissions.addAll(token.getPermissions());
		}

		if (c.getAllowedUsers() != null && c.getAllowedUsers().size() > 0 && !c.getAllowedUsers().contains(token.getUser())) {
			logFailure(req, token.getUser(), "User is not allowed to use OTP.");
			throw new WebswingAuthenticationException("User '" + token.getUser() + "' is not allowed to use OTP.");
		}
		if (c.getAllowedRoles() != null) {
			roles.retainAll(c.getAllowedRoles());
		}
		if (c.getAllowedPermissions() != null) {
			permissions.retainAll(c.getAllowedPermissions());
		}

		AbstractWebswingUser user = new OtpWebswingUser(token.getUser(), attributes, roles, permissions);
		logSuccess(req, token.getUser());
		return user;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void main(String[] args) throws WebswingAuthenticationException, JsonGenerationException, JsonMappingException, IOException {
		OtpTokenDataImpl token = new OtpTokenDataImpl();
		token.setUser("john");
		token.setRequestorId("myWebPage");
		String secret = "5gV4gchlFZEwmXHNLgGj";
		String crypto = "HmacSHA512";
		List attrs = new ArrayList<>();
		attrs.add(Arrays.asList("attr1", "value1"));
		attrs.add(Arrays.asList("attr2", "value2"));

		String encoded;
		token.setAttributes(attrs);
		token.setRoles(new ArrayList<>());
		token.setPermissions(new ArrayList<>());
		String message = getOtpMessage(token);
		String otp = calculateTotpString(crypto, secret, message, 0, 30);
		token.setOneTimePassword(otp);
		encoded = encodeOtpToken(token);
		long time = System.currentTimeMillis() / 1000 / 30;
		time = time + 0;
		message = message + time;
		System.out.println("message: " + message);
		System.out.println("response: " + AbstractSecurityModule.getMapper().writeValueAsString(token));
		System.out.println("http://localhost:8080/swingset3/?securityToken=" + encoded);
	}

	/* PHP snippet to generate One time URL
	<?php
	$secret =  "skm72joe465mta4ki3a7fmkapp";
	$requestorId = 'thirdPartyID';
	$userName = "user1";
	$userAttributes = array(
	array("attr1","asdf")
	);
	$roles = array();
	$permissions = array();
	
	$intervalSec = 30;
	
	
	//message = username + attributes + roles + permissions + time
	$message = $userName;
	foreach ($userAttributes as $value) {
	$message = $message .$value[0];
	if(is_bool($value[1])){
		$message = $message.var_export($value[1], true);
	}else{
		$message = $message.$value[1];
	}
	}
	foreach ($roles as $value) {
	$message = $message . $value;
	}
	foreach ($permissions as $value) {
	$message = $message . $value;
	}
	$message = $message.round(microtime(true)/$intervalSec);
	
	$otp = base64_encode(hash_hmac("sha512",$message,$secret,true));
	$response = (object)array(
	    'requestorId' => $requestorId,
		'user' => $userName,
		'attributes' => $userAttributes,
		'roles' => $roles,
		'permissions' => $permissions,
		'oneTimePassword' => $otp
	);
	$token=json_encode($response,JSON_UNESCAPED_SLASHES);
	$b64token=base64_encode($token);
	$url='http://localhost:8080/swingset3/?securityToken='.$b64token;
	echo 'message: '.$message;
	echo '<br>';
	echo 'response: '.$token;
	echo '<br>';
	echo '<a href = "'.$url.'" target="_blank">'.$url.'</a>';
	?>
	*/

}
