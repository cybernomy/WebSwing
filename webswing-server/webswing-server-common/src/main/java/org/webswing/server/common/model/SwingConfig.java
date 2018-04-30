package org.webswing.server.common.model;

import org.webswing.server.common.model.meta.*;
import org.webswing.server.common.model.meta.ConfigFieldEditorType.EditorType;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

@ConfigType(metadataGenerator = SwingConfig.SwingConfigurationMetadataGenerator.class)
@ConfigFieldOrder({ "name", "theme", "fontConfig", "directdraw", "javaFx", "debug", "userDir", "jreExecutable", "javaVersion", "classPathEntries", "vmArgs", "launcherType", "launcherConfig", "maxClients", "sessionMode", "swingSessionTimeout", "timeoutIfInactive", "monitorEdtEnabled", "allowStealSession", "autoLogout", "goodbyeUrl", "isolatedFs",
		"allowUpload", "allowDelete", "allowDownload", "allowAutoDownload", "transparentFileOpen", "transparentFileSave", "transferDir", "clearTransferDir", "uploadMaxSize", "allowedCorsOrigins", "allowJsLink", "allowLocalClipboard", "allowServerPrinting" })
public interface SwingConfig extends Config {

	public enum SessionMode {
		ALWAYS_NEW_SESSION,
		CONTINUE_FOR_BROWSER,
		CONTINUE_FOR_USER;
	}

	public enum LauncherType {
		Applet,
		Desktop;
	}

	@ConfigField(tab = ConfigGroup.General, label = "Name", description = "Application name.")
	@ConfigFieldVariables(VariableSetName.SwingInstance)
	@ConfigFieldDefaultValueString("My Application")
	public String getName();

	@ConfigField(tab = ConfigGroup.General, label = "Theme", description = "Select one of the default window decoration themes or a enter path to a XFWM4 theme folder.")
	@ConfigFieldVariables(VariableSetName.SwingInstance)
	@ConfigFieldDefaultValueString("Murrine")
	@ConfigFieldPresets({ "Murrine", "Agualemon", "Sassandra", "Therapy", "Totem", "Vertex", "Vertex-Light" })
	public String getTheme();

	@ConfigField(tab = ConfigGroup.General, label = "Fonts", description = "Customize logical font mappings and define physical fonts available to application. These fonts (TTF only) will be used for DirectDraw as native fonts. Key: name of font (ie. dialog|dialoginput|sansserif|serif|monospaced), Value: path to font file.")
	@ConfigFieldVariables(VariableSetName.SwingInstance)
	@ConfigFieldPresets({ "dialog", "dialoginput", "sansserif", "serif", "monospaced" })
	public Map<String, String> getFontConfig();

	@ConfigField(tab = ConfigGroup.General, label = "DirectDraw Rendering", description = "DirectDraw rendering mode uses canvas instructions to render the application instead of server-rendered png images. DirectDraw improves performance but is not recomended for applications with lot of graphics content.")
	@ConfigFieldDefaultValueBoolean(true)
	public boolean isDirectdraw();

	@ConfigField(tab = ConfigGroup.General, label = "JavaFx Support (experimental)", description = "!Only for Java8! Enables native or embeded JavaFx framework support.")
	@ConfigFieldDefaultValueBoolean(false)
	public boolean isJavaFx();

	@ConfigField(tab = ConfigGroup.General, label = "Enable Debug Mode", description = "Enables remote debug for this application. To start the application in debug mode use '?debugPort=8000' url param.")
	@ConfigFieldDefaultValueBoolean(false)
	public boolean isDebug();

	@ConfigField(tab = ConfigGroup.Java, label = "Working Directory", description = "The User working directory. Path from which the application process will be started. (See the Java System Property: 'user.dir')")
	@ConfigFieldVariables(VariableSetName.SwingInstance)
	@ConfigFieldDefaultValueString("")
	public String getUserDir();

	@ConfigField(tab = ConfigGroup.Java, label = "JRE Executable", description = "Path to java executable that will be used to spawn application process. Java 6,7 and 8 is supported.")
	@ConfigFieldVariables(VariableSetName.SwingInstance)
	@ConfigFieldDefaultValueString("${java.home}/bin/java")
	public String getJreExecutable();

	@ConfigField(tab = ConfigGroup.Java, label = "Java Version", description = "Java version of the JRE executable defined above. Expected values are starting with '1.6', '1.7' or '1.8'.")
	@ConfigFieldVariables(VariableSetName.SwingInstance)
	@ConfigFieldDefaultValueString("${java.version}")
	public String getJavaVersion();

	@ConfigField(tab = ConfigGroup.Java, label = "Class Path", description = "Application's classpath. Absolute or relative path to jar file or classes directory. At least one classPath entry should be specified containing the main class. Supports ? and * wildcards.")
	@ConfigFieldVariables(VariableSetName.SwingInstance)
	public List<String> getClassPathEntries();

	@ConfigField(tab = ConfigGroup.Java, label = "JVM Arguments", description = "Commandline arguments processed by Oracle's Java Virtual Machine. (ie. '-Xmx128m')")
	@ConfigFieldVariables(VariableSetName.SwingInstance)
	public String getVmArgs();

	@ConfigField(tab = ConfigGroup.Java, label = "Launcher Type", description = "Select the application type. Applet or regular Desktop Application.")
	@ConfigFieldDefaultValueString("Desktop")
	@ConfigFieldDiscriminator
	public LauncherType getLauncherType();

	@ConfigField(tab = ConfigGroup.Java, label = "Launcher Configuration", description = "Launcher type specific configuration options")
	@ConfigFieldDefaultValueObject(HashMap.class)
	@ConfigFieldEditorType(editor = EditorType.Object)
	public Map<String, Object> getLauncherConfig();

	@ConfigField(tab = ConfigGroup.Session, label = "Max. Connections", description = "Maximum number of allowed simultaneous connections for this application.")
	@ConfigFieldDefaultValueNumber(1)
	public int getMaxClients();

	@ConfigField(tab = ConfigGroup.Session, label = "Session Mode", description = "Select session behavior when user reconnects to application. 1.ALWAYS_NEW_SESSION: New application is started for every Webswing session. (Session timeout will be set to 0) 2.CONTINUE_FOR_BROWSER: Webswing session can be resumed in the same browser after connection is terminated (Session timeout applies). 3.CONTINUE_FOR_USER: Application session can be resumed by the same user from any computer after the connection is terminated(Session timeout applies).")
	@ConfigFieldDefaultValueString("CONTINUE_FOR_BROWSER")
	public SessionMode getSessionMode();

	@ConfigField(tab = ConfigGroup.Session, label = "Session Timeout", description = "Specifies how long (seconds) will be the application left running after the user closes the browser. User can reconnect in this interval and continue in last session.")
	@ConfigFieldDefaultValueNumber(300)
	public int getSwingSessionTimeout();

	@ConfigField(tab = ConfigGroup.Session, label = "Timeout if Inactive", description = "If True, the Session Timeout will apply for user inactivity (Session Timeout has to be > 0). Otherwise only disconnected sessions will time out.")
	@ConfigFieldDefaultValueBoolean(false)
	public boolean isTimeoutIfInactive();

	@ConfigField(tab = ConfigGroup.Session, label = "Monitor App Responsiveness", description = "If True, Webswing will display a progress animation if Swing's Event Dispatch thread is not responding.")
	@ConfigFieldDefaultValueBoolean(true)
	public boolean isMonitorEdtEnabled();

	@ConfigField(tab = ConfigGroup.Session, label = "Session Stealing", description = "If enabled, and session mode 'CONTINUE_FOR_USER' is selected, user can resume Webswing session even if the connection is open in other browser. Former browser window will be disconnected.")
	@ConfigFieldDefaultValueBoolean(true)
	public boolean isAllowStealSession();

	@ConfigField(tab = ConfigGroup.Session, label = "Auto Logout", description = "If enabled, user is automatically logged out after the application finished.")
	@ConfigFieldDefaultValueBoolean(true)
	@ConfigFieldDiscriminator
	public boolean isAutoLogout();

	@ConfigField(tab = ConfigGroup.Session, label = "Goodbye URL", description = "Absolute or relative URL to redirect to, when application exits. Use '/' to navigate back to Application selector.")
	@ConfigFieldVariables(VariableSetName.SwingInstance)
	@ConfigFieldDefaultValueString("")
	String getGoodbyeUrl();

	@ConfigField(tab = ConfigGroup.Features, label = "Isolated Filesystem", description = "If true, every file chooser dialog will be restricted to access only the home directory of current application.")
	@ConfigFieldDefaultValueBoolean(false)
	@ConfigFieldDiscriminator
	public boolean isIsolatedFs();

	@ConfigField(tab = ConfigGroup.Features, label = "Uploading Files", description = "If selected, the JFileChooser integration will allow users to upload files to folder opened in the file chooser dialog")
	@ConfigFieldDefaultValueBoolean(true)
	@ConfigFieldDiscriminator
	public boolean isAllowUpload();

	@ConfigField(tab = ConfigGroup.Features, label = "Deleting Files", description = "If selected, the JFileChooser integration will allow users to delete files displayed in the file chooser dialog")
	@ConfigFieldDefaultValueBoolean(true)
	public boolean isAllowDelete();

	@ConfigField(tab = ConfigGroup.Features, label = "Downloading Files", description = "If selected, the JFileChooser integration will allow users to download files displayed in the file chooser dialog")
	@ConfigFieldDefaultValueBoolean(true)
	@ConfigFieldDiscriminator
	public boolean isAllowDownload();

	@ConfigField(tab = ConfigGroup.Features, label = "Auto-Download from Save Dialog", description = "If selected, the JFileChooser dialog's save mode will trigger file download as soon as the selected file is available on filesystem.")
	@ConfigFieldDefaultValueBoolean(true)
	public boolean isAllowAutoDownload();

	@ConfigField(tab = ConfigGroup.Features, label = "Transparent Open File Dialog", description = "If selected, the JFileChooser dialog's open mode will open a client side file browser and transparently upload selected files and triggers selection.")
	@ConfigFieldDefaultValueBoolean(true)
	public boolean isTransparentFileOpen();

	@ConfigField(tab = ConfigGroup.Features, label = "Transparent Save File Dialog", description = "If selected, the JFileChooser dialog's save mode will open a client side dialog to enter the file name to be saved.")
	@ConfigFieldDefaultValueBoolean(true)
	public boolean isTransparentFileSave();

	@ConfigField(tab = ConfigGroup.Features, label = "Upload Folder", description = "If Isolated Filesystem is enabled. This will be the folder on the server where the user can upload and download files from. Multiple folders can be defined using path separator (${path.separator})")
	@ConfigFieldVariables(VariableSetName.SwingInstance)
	@ConfigFieldDefaultValueString("${user}/upload")
	public String getTransferDir();

	@ConfigField(tab = ConfigGroup.Features, label = "Clear Upload Folder", description = "If enabled, all files in the transfer folder will be deleted when the application process is terminated.")
	@ConfigFieldDefaultValueBoolean(true)
	public boolean isClearTransferDir();

	@ConfigField(tab = ConfigGroup.Features, label = "Upload Size Limit", description = "Maximum size of upload for single file (in MB). Set 0 for unlimited size.")
	@ConfigFieldDefaultValueNumber(5)
	public double getUploadMaxSize();

	@ConfigField(tab = ConfigGroup.Features, label = "Domains Allowed to Embed", description = "If you are embedding webswing to page on different domain, you have to enable Cross-origin resource sharing (CORS) by adding the domain in this list. Use * to allow all domains.")
	public List<String> getAllowedCorsOrigins();

	@ConfigField(tab = ConfigGroup.Features, label = "Allow JsLink", description = "If enabled, the JSLink feature will be enabled, allowing application to invoke javascript and vice versa. (See netscape.javascript.JSObject)")
	@ConfigFieldDefaultValueBoolean(true)
	public boolean isAllowJsLink();

	@ConfigField(tab = ConfigGroup.Features, label = "Allow Local Clipboard", description = "Enables built-in integration of client's local clipboard. Due to browser security limitations clipboard toolbar is displayed.")
	@ConfigFieldDefaultValueBoolean(true)
	boolean isAllowLocalClipboard();

	@ConfigField(tab = ConfigGroup.Features, label = "Allow Server Printing", description = "Enables native printing on devices configured on server's OS. If disabled a pdf is generated and sent to client browser.")
	@ConfigFieldDefaultValueBoolean(false)
	boolean isAllowServerPrinting();

	public static class SwingConfigurationMetadataGenerator extends MetadataGenerator<SwingConfig> {
		@Override
		public Class<?> getExplicitType(SwingConfig config, ClassLoader cl, String propertyName, Method readMethod, Object value) throws ClassNotFoundException {
			if (propertyName.equals("launcherConfig")) {
				if (config.getLauncherType() != null) {
					switch (config.getLauncherType()) {
					case Applet:
						return AppletLauncherConfig.class;
					case Desktop:
						return DesktopLauncherConfig.class;
					default:
						return null;
					}
				} else {
					return null;
				}
			} else {
				return super.getExplicitType(config, cl, propertyName, readMethod, value);
			}
		}

		@Override
		protected LinkedHashSet<String> getPropertyNames(SwingConfig config, ClassLoader cl) throws Exception {
			LinkedHashSet<String> names = super.getPropertyNames(config, cl);
			if (!config.isAllowUpload()) {
				names.remove("uploadMaxSize");
				names.remove("allowAutoUpload");
			}
			if (!config.isAllowDownload()) {
				names.remove("allowAutoDownload");
			}
			if (!config.isIsolatedFs()) {
				names.remove("transferDir");
				names.remove("transparentFileSave");
				names.remove("transparentFileOpen");
				names.remove("clearTransferDir");
			}
			if (config.isAutoLogout()) {
				names.remove("goodbyeUrl");
			}
			return names;
		}

	}
}
