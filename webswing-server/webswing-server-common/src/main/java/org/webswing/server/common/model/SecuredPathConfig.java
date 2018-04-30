package org.webswing.server.common.model;

import java.util.HashMap;
import java.util.Map;

import org.webswing.server.common.model.meta.*;
import org.webswing.server.common.model.meta.ConfigFieldEditorType.EditorType;

@ConfigFieldOrder({ "enabled", "path", "homeDir", "webFolder", "langFolder", "icon", "security", "swingConfig" })
public interface SecuredPathConfig extends Config {

	@ConfigField(label = "Enabled", description = "If true, application will be started automatically, when server starts.")
	@ConfigFieldDefaultValueBoolean(true)
	public boolean isEnabled();

	@ConfigField(label = "Context Path", description = "Url context path where the application will be deployed.")
	public String getPath();

	@ConfigField(label = "Home Folder", description = "Application's home directory. Application instances will be executed from this directory. This will also be the base directory of any relative classpath entries specified.")
	@ConfigFieldVariables
	@ConfigFieldDiscriminator
	@ConfigFieldDefaultValueString("${user.dir}")
	public String getHomeDir();

	@ConfigField(label = "Web Folder", description = "Folder to be used to store customized static web files like HTML, CSS or Javascript.")
	@ConfigFieldVariables(VariableSetName.SwingApp)
	@ConfigFieldDefaultValueString("")
	public String getWebFolder();

	@ConfigField(label = "Localization Folder", description = "Folder to be used to store customized messages and translations in supported languages. English is available by default.")
	@ConfigFieldVariables(VariableSetName.SwingApp)
	@ConfigFieldDefaultValueString("")
	public String getLangFolder();

	@ConfigField(label = "Icon", description = "Path to icon displayed in application selection dialog.")
	@ConfigFieldVariables(VariableSetName.SwingApp)
	public String getIcon();

	@ConfigField(label = "Security")
	@ConfigFieldEditorType(editor = EditorType.Object, className = "org.webswing.server.services.security.api.WebswingSecurityConfig")
	@ConfigFieldDefaultValueObject(HashMap.class)
	public Map<String, Object> getSecurity();

	@ConfigField(label = "Application")
	SwingConfig getSwingConfig();
}
