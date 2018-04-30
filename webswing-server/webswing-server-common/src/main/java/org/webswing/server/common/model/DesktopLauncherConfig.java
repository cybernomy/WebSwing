package org.webswing.server.common.model;

import org.webswing.server.common.model.meta.ConfigField;
import org.webswing.server.common.model.meta.ConfigFieldOrder;
import org.webswing.server.common.model.meta.ConfigFieldVariables;
import org.webswing.server.common.model.meta.ConfigType;
import org.webswing.server.common.model.meta.VariableSetName;

@ConfigType
@ConfigFieldOrder({"mainClass","args"})
public interface DesktopLauncherConfig {

	@ConfigField(label = "Main Class", description = "Swing application fully qualiffied class name. (ie. 'com.mypackage.Main')")
	@ConfigFieldVariables(VariableSetName.SwingInstance)
	public String getMainClass();

	@ConfigField(label = "Main Arguments", description = "Swing application main method arguments. This string will be passed to the main method's (String[] args) ")
	@ConfigFieldVariables(VariableSetName.SwingInstance)
	public String getArgs();
}
