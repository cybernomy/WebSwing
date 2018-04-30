package org.webswing.server.services.security.modules.keycloak;

import org.webswing.server.common.model.meta.*;
import org.webswing.server.services.security.extension.api.WebswingExtendableSecurityModuleConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vikto on 03-Feb-17.
 */
@ConfigFieldOrder({ "keycloakUrl", "keyCloakRealms", "callbackUrl", "clientId", "clientSecret", "trustedPemFile", "usernameAttributeName", "rolesAttributeName" })
public interface KeycloakSecurityModuleConfig extends WebswingExtendableSecurityModuleConfig {
	@ConfigField(label = "Keycloak URL")
	@ConfigFieldVariables
	String getKeycloakUrl();

	@ConfigField(label = "Realms", description = "At least one realm is required. First realm is the default one.")
	@ConfigFieldDefaultValueObject(ArrayList.class)
	@ConfigFieldEditorType(editor = ConfigFieldEditorType.EditorType.ObjectListAsTable)
	List<RealmEntry> getRealms();

	@ConfigField(label = "Callback URL", description = "URL of the webswing server where auth token will be received. Must end with /login")
	@ConfigFieldDefaultValueString("https://<webswing_host>:<webswing_port>/<swing_path>/login")
	@ConfigFieldVariables
	String getCallbackUrl();

	@ConfigField(label = "Client ID", description = "Client ID / API key")
	@ConfigFieldVariables
	String getClientId();

	@ConfigField(label = "Client Secret", description = "Leave empty if not required")
	@ConfigFieldVariables
	String getClientSecret();

	@ConfigField(label = "Trusted Certs File (PEM)", description = "Trusted cert chains to establish TLS connection with Open Id server. To disable cert validation use 'DISABLED' (only for testing)")
	@ConfigFieldVariables
	String getTrustedPemFile();

	@ConfigField(label = "Username Attribute Name", description = "ID Token claim name to be used as username")
	@ConfigFieldVariables
	@ConfigFieldDefaultValueString("preferred_username")
	String getUsernameAttributeName();

	@ConfigField(label = "Fallback Users File", description = "In case Keycloak Url is not reachable, fallback to local authentication. To enable fallback, set path to properties file with list of users. User entry format: user.<username>=<password>[,role1][,role2]")
	@ConfigFieldVariables
	String getFallbackFile();

	@ConfigField(label = "Roles Attribute Name", description = "Leave empty if not required")
	@ConfigFieldVariables
	String getRolesAttributeName();

}
