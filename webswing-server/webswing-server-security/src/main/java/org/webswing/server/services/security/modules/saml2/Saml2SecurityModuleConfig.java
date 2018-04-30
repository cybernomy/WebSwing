package org.webswing.server.services.security.modules.saml2;

import org.webswing.server.common.model.meta.ConfigField;
import org.webswing.server.common.model.meta.ConfigFieldDefaultValueString;
import org.webswing.server.common.model.meta.ConfigFieldOrder;
import org.webswing.server.common.model.meta.ConfigFieldVariables;
import org.webswing.server.common.model.meta.VariableSetName;
import org.webswing.server.services.security.extension.api.WebswingExtendableSecurityModuleConfig;

@ConfigFieldOrder({ "identityProviderMetadataFile", "serviceProviderConsumerUrl", "serviceProviderEntityId", "decryptionKeyStore", "decryptionKeyAlias", "decryptionKeyStorePwd", "decryptionKeyPwd", "logoutUrl" })
public interface Saml2SecurityModuleConfig extends WebswingExtendableSecurityModuleConfig {

	@ConfigField(label = "Identity Provider Metadata File", description = "Metadata file downloaded from Identity provider.")
	@ConfigFieldVariables(VariableSetName.SwingApp)
	String getIdentityProviderMetadataFile();

	@ConfigField(label = "Service Provider Consumer URL", description = "Url that verifies the SAML2 token. Should be 'https://<webswing_host>:<webswing_port>/<current_app_path>/login'")
	@ConfigFieldDefaultValueString("https://<webswing_host>:<webswing_port>/<swing_path>/login")
	String getServiceProviderConsumerUrl();

	@ConfigField(label = "Service Provider Entity ID", description = "Identitficator used when registering Webswing with Idp.")
	String getServiceProviderEntityId();

	@ConfigField(label = "Logout URL", description = "Webswing will redirect to this URL after logout.")
	String getLogoutUrl();

	@ConfigField(label = "Decryption Key Store", description = "PKCS#12 or JKS Key Store file containing the private key used to decrypt the assertions returned by server. Leave empty if server does not encrypt assertions.")
	@ConfigFieldVariables(VariableSetName.Basic)
	String getDecryptionKeyStore();

	@ConfigField(label = "Decryption Key Alias", description = "Key alias the private key is stored under.")
	@ConfigFieldVariables(VariableSetName.Basic)
	String getDecryptionKeyAlias();

	@ConfigField(label = "Decryption Store Password", description = "Password to access the private key.")
	@ConfigFieldVariables(VariableSetName.Basic)
	String getDecryptionKeyStorePwd();

	@ConfigField(label = "Decryption Key Password", description = "Password to access the private key.")
	@ConfigFieldVariables(VariableSetName.Basic)
	String getDecryptionKeyPwd();

}
