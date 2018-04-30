package org.webswing.server.services.security.modules.embeded;

import org.webswing.server.common.model.meta.ConfigField;
import org.webswing.server.common.model.meta.ConfigFieldDefaultValueObject;
import org.webswing.server.common.model.meta.ConfigFieldEditorType;
import org.webswing.server.common.model.meta.ConfigFieldOrder;
import org.webswing.server.services.security.extension.api.WebswingExtendableSecurityModuleConfig;

import java.util.ArrayList;
import java.util.List;

@ConfigFieldOrder({ "users" })
public interface EmbededSecurityModuleConfig extends WebswingExtendableSecurityModuleConfig {

	@ConfigField(label = "Users", description = "User definitions")
	@ConfigFieldEditorType(editor = ConfigFieldEditorType.EditorType.ObjectListAsTable)
	@ConfigFieldDefaultValueObject(ArrayList.class)
	List<EmbededUserEntry> getUsers();
}
