package org.webswing.server.services.security.extension.onetimeurl;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.List;

import org.webswing.server.common.model.Config;
import org.webswing.server.common.model.meta.ConfigField;
import org.webswing.server.common.model.meta.ConfigFieldDefaultValueGenerator;
import org.webswing.server.common.model.meta.ConfigFieldDefaultValueNumber;
import org.webswing.server.common.model.meta.ConfigFieldDefaultValueString;
import org.webswing.server.common.model.meta.ConfigFieldOrder;
import org.webswing.server.common.model.meta.ConfigFieldPresets;

@ConfigFieldOrder({ "requestorId", "secret", "validForSec", "hMacAlgo", "allowedUsers", "allowedRoles", "allowedPermissions" })
public interface OtpAccessConfig extends Config {
	@ConfigField(label = "Trusted Party ID", description = "Identificator of a trusted third party, we share the secret with.")
	String getRequestorId();

	@ConfigField(label = "Validity Interval (in sec.)", description = "Time interval length in seconds in which the generated URL token is valid.")
	@ConfigFieldDefaultValueNumber(30)
	Integer getValidForSec();

	@ConfigField(label = "Security Hash Algorithm", description = "Algorithm used to sign the token.")
	@ConfigFieldDefaultValueString("HmacSHA512")
	@ConfigFieldPresets({ "HmacSHA1", "HmacSHA256", "HmacSHA512" })
	String getHMacAlgo();

	@ConfigField(label = "Secret", description = "Secret token, used as the key for signing and verification of URL tokens. This token needs to be kept private.")
	@ConfigFieldDefaultValueGenerator("generateSecret")
	String getSecret();

	@ConfigField(label = "Allowed Users", description = "List of users that can authenticate using this One-time-URL extension. If empty, all users are considered valid.")
	List<String> getAllowedUsers();

	@ConfigField(label = "Allowed Roles", description = "List of roles allowed for users authenticated with One-time-URL extension. If empty, all roles are allowed.")
	List<String> getAllowedRoles();

	@ConfigField(label = "Allowed Permissions", description = "List of permissions allowed for users authenticated with One-time-URL extension. If empty, all permissions are allowed.")
	List<String> getAllowedPermissions();

	/**
	 * Used to generate default value for {{@link #getSecret()}
	 * See {@link ConfigFieldDefaultValueGenerator}
	 * @return
	 */
	public static String generateSecret(OtpAccessConfig config) {
		SecureRandom random = new SecureRandom();
		return new BigInteger(130, random).toString(32);
	}
}
