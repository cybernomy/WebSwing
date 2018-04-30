package org.webswing.server.services.security.modules.keycloak;

import org.webswing.server.common.model.Config;
import org.webswing.server.common.model.meta.ConfigField;
import org.webswing.server.common.model.meta.ConfigFieldOrder;
import org.webswing.server.common.model.meta.ConfigFieldVariables;

/**
 * Created by vikto on 09-Feb-17.
 */
@ConfigFieldOrder({ "realm", "logoutUrl" })
public interface RealmEntry extends Config {

	@ConfigField(label = "Realm Name")
	@ConfigFieldVariables
	String getRealm();

	@ConfigField(label = "Logout URL", description = "Webswing will redirect to this URL after logout for user logged in against this realm.")
	@ConfigFieldVariables
	String getLogoutUrl();
}
