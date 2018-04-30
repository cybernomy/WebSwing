package org.webswing.server.services.security.api;

import org.webswing.server.services.security.modules.anonym.AnonymSecurityModule;
import org.webswing.server.services.security.modules.database.DatabaseSecurityModule;
import org.webswing.server.services.security.modules.embeded.EmbededSecurityModule;
import org.webswing.server.services.security.modules.keycloak.KeycloakSecurityModule;
import org.webswing.server.services.security.modules.openidconnect.OpenIDConnectSecurityModule;
import org.webswing.server.services.security.modules.property.PropertySecurityModule;
import org.webswing.server.services.security.modules.saml2.Saml2SecurityModule;

public enum BuiltInModules {
	INHERITED(null),
	NONE(AnonymSecurityModule.class.getName()),
	EMBEDED(EmbededSecurityModule.class.getName()),
	PROPERTY_FILE(PropertySecurityModule.class.getName()),
	DATABASE(DatabaseSecurityModule.class.getName()),
	OPENID_CONNECT(OpenIDConnectSecurityModule.class.getName()),
	KEYCLOAK(KeycloakSecurityModule.class.getName()),
	SAML2(Saml2SecurityModule.class.getName());

	private String type;

	private BuiltInModules(String type) {
		this.type = type;

	}

	public String getType() {
		return type;
	}

	public static String getSecurityModuleClassName(String module) {
		try {
			BuiltInModules builtInModule = BuiltInModules.valueOf(module);
			return builtInModule.getType();
		} catch (Exception e) {
			return module;
		}
	}

}