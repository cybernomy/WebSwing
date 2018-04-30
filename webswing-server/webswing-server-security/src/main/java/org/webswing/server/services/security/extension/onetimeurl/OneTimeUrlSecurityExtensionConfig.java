package org.webswing.server.services.security.extension.onetimeurl;

import java.util.List;

import org.webswing.server.common.model.meta.ConfigField;
import org.webswing.server.common.model.meta.ConfigFieldEditorType;
import org.webswing.server.common.model.meta.ConfigFieldEditorType.EditorType;
import org.webswing.server.services.security.extension.api.SecurityModuleExtensionConfig;

public interface OneTimeUrlSecurityExtensionConfig extends SecurityModuleExtensionConfig {

	@ConfigField(label = "API key", description = "Secret keys for generating One-Time-URLs per trusted third party")
	@ConfigFieldEditorType(editor = EditorType.ObjectList)
	List<OtpAccessConfig> getApiKeys();

}
