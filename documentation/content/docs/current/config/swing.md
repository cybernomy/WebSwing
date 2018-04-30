---
title: "Application Setup"
weight: 10

---

## Application setup

Swing applications are configured in json format and by default saved in file `webswing.config`, which is by default placed in the same folder as the `webswing-server.war` file. This location may be changed using the `-c` command-line option. It is recommended to use Administration console form based configuration screen to modify this file to avoid JSON formatting problems.

---

## Using Admin console

Easiest way of configuring your application is using Admin console. For accessing Admin console click **_Manage_** from Webswing homepage or add `/admin` path to your Webswing URL (ie. [http://localhost:8080/admin](http://localhost:8080/admin)).
This will navigate you to Admin Dashboard where you can see the list of configured applications. You can add new or edit existing applications here. 

![Swing application list ](../img/app-list.png) 

You can select specific application from the left-hand navigation bar or scroll down within the list of all. To configure the application click **_Show Config_** button which will navigate you to the configuration page of selected application. 

You can edit or view the settings. These are split in logical sections.
Each setting has a description, which is displayed when hovered on the question mark.

![Swing general settings ](../img/config1.png) 

---

## JSON format description

Here is a sample `webswing.config` file content with demo swing application configured:
```json
{
  "/swingset3" : {
    "path" : "/swingset3",
    "webFolder" : "",
    "security" : {
      "module" : "INHERITED",
      "config" : null,
      "classPath" : [ ],
      "authorizationConfig" : {
        "users" : [ ],
        "roles" : [ ]
      }
    },
    "icon" : "${webswing.rootDir}/demo/SwingSet3/icon.png",
    "homeDir" : "demo/SwingSet3",
    "langFolder" : "${webswing.rootDir}/lang",
    "swingConfig" : {
      "name" : "SwingSet3",
      "jreExecutable" : "${java.home}/bin/java",
      "javaVersion" : "${java.version}",
      "vmArgs" : "-Xmx128m -DauthorizedUser=${user}",
      "classPathEntries" : [ "${webswing.rootDir}/demo/SwingSet3/SwingSet3.jar", "${webswing.rootDir}/demo/SwingSet3/lib/*.jar" ],
      "theme" : "Murrine",
      "fontConfig" : {
        "dialog" : "${webswing.rootDir}/fonts/Roboto-Regular.ttf",
        "dialoginput" : "${webswing.rootDir}/fonts/RobotoMono-Regular.ttf",
        "serif" : "${webswing.rootDir}/fonts/RobotoSlab-Regular.ttf"
      },
      "maxClients" : 10,
      "sessionMode" : "CONTINUE_FOR_BROWSER",
      "swingSessionTimeout" : 300,
      "allowStealSession" : true,
      "isolatedFs" : true,
      "debug" : true,
      "javaFx" : true,
      "directdraw" : true,
      "allowDelete" : true,
      "allowDownload" : true,
      "allowAutoDownload" : true,
      "allowUpload" : true,
      "uploadMaxSize" : 5,
      "allowJsLink" : true,
      "launcherType" : "Desktop",
      "launcherConfig" : {
        "mainClass" : "com.sun.swingset3.SwingSet3",
        "args" : null
      },
      "allowedCorsOrigins" : [ "*" ],
      "userDir" : "${user}",
      "transparentFileSave" : true,
      "clearTransferDir" : true,
      "transparentFileOpen" : true,
      "autoLogout" : true,
      "transferDir" : "${user}/upload",
      "timeoutIfInactive" : false,
      "allowLocalClipboard" : true,
      "goodbyeUrl" : ""
    },
    "enabled" : true
  }
}
```

### Configuration File Organization

The JSON configuration file is organized by the unique URL endpoints. Each URL endpoint has therefore its own configuration. This can be seen in the Webswing binary distribution webswing.config file:
```json
{
  "/" : {
    "path" : "/",
    "security" : ...
    },
    ...
  },
  "/swingset3" : {
    "path" : "/swingset3",
    "security" : {...},
    ...
    "swingConfig" : {
      "name" : "SwingSet3",
      ...
      "launcherType" : "Desktop",
      "launcherConfig" : {
        "mainClass" : "com.sun.swingset3.SwingSet3",
        "args" : null
      },
    },
  },
  "/netbeans" : {
    "path" : "/netbeans",
    ...
  },
  "/applet" : {
    "path" : "/applet",
    ...
  },
  "/javafx" : {
    "path" : "/javafx",
    ...
}
```



---

## Variable resolution

Most of the text options support variable replacement. Available variables are java system properties, OS environment variables and set of special Webswing variables. Variables are specified with dollar followed by variable name in curly brackets. For example `${variable_name}`.

In additions to variables, for `classPathEntries` properties it is possible to use wild-card characters. Supported wild-cards are `*` (everything) and `?` (any singe character). 

**Available variables:**

Variable Name 				| Description
----------------------------|------------
**`${user}`**					| Webswing specific logged in user name.
**`${clientId}`** 				| Webswing specific unique browser identifier. 
**`${clientIp}`**				| IP address of browser which started this application.
**`${clientLocale}`**			| Locale of browser which started this application.
**`${customArgs}`**				| Custom Arguments specified in URL parameters. [See details](../../integrate/browser/#additional-application-arguments)
**Java system Properties** 		| All properties accessible to server's JVM using System.getProperty method
**System environment variables**| All OS level environment variables accessible to script that started Webswing server JVM. 

In Admin console options with variable replacement support appears with a flash icon. When focused a panel with resolved value of is displayed:

![Variables resolution](../img/resolve-var.png)

---

## Settings overview
### General 

| Setting | Description |
| ------------- | ------------- |
| Name | Application name |
| Theme | Select one of the default window decoration themes or a enter path to a XFWM4 theme folder |
| Fonts | Customize logical font mappings and define physical fonts available to application. These fonts (TTF only) will be used for DirectDraw as native fonts. Key: name of font (ie. dialog|dialoginput|sansserif|serif|monospaced), Value: path to font file |
| DirectDraw Rendering | DirectDraw rendering mode uses canvas instructions to render the application instead of server-rendered png images. DirectDraw improves performance but is not recomended for applications with lot of graphics content |
| JavaFx Support | !Only for Java8! Enables native or embeded JavaFx framework support |
| Enable Debug Mode | Enables remote debug for this application. To start the application in debug mode use '?debugPort=8000' url param |

### Java 

| Setting | Description |
| ------------- | ------------- |
| Working Directory | The User working directory. Path from which the application process will be started. (See the Java System Property: 'user.dir') |
| JRE Executable | Path to java executable that will be used to spawn application process. Java 7 and 8 is supported |
| Java Version | Java version of the JRE executable defined above. Expected values are starting with '1.7' or '1.8' |
| Class Path | Application's classpath. Absolute or relative path to jar file or classes directory. At least one classPath entry should be specified containing the main class. Supports ? and * wildcards |
| JVM Arguments | Commandline arguments processed by Oracle's Java Virtual Machine. (ie. '-Xmx128m') |
| Launcher Type | Select the application type. Applet or regular Desktop Application |
| Launcher Configuration | Launcher type specific configuration options |

### Session

| Setting | Description |
| ------------- | ------------- |
| Max. Connections | Maximum number of allowed simultaneous connections for this applicatio
| Session Mode | Select session behavior when user reconnects to application.<ol><li>ALWAYS_NEW_SESSION: New application is started for every Webswing session. (Session timeout will be set to 0)</li><li>CONTINUE_FOR_BROWSER: Webswing session can be resumed in the same browser after connection is terminated (Session timeout applies).</li><li>CONTINUE_FOR_USER: Application session can be resumed by the same user from any computer after the connection is terminated(Session timeout applies)</li></ol> |
| Session Timeout | Specifies how long (seconds) will be the application left running after the user closes the browser. User can reconnect in this interval and continue in last session |
| Timeout if Inactive | If True, the Session Timeout will apply for user inactivity (Session Timeout has to be > 0). Otherwise only disconnected sessions will time out |
| Session Stealing | If enabled, and session mode 'CONTINUE_FOR_USER' is selected, user can resume Webswing session even if the connection is open in other browser. Former browser window will be disconnected |
| Auto Logout | If enabled, user is automatically logged out after the application finished |
| Goodbye URL | Absolute or relative URL to redirect to, when application exits. Use '/' to navigate back to Application selector |

### Features

| Setting | Description |
| ------------- | ------------- |
| Isolated Filesystem | If true, every file chooser dialog will be restricted to access only the home directory of current application |
| Uploading Files | If selected, the JFileChooser integration will allow users to upload files to folder opened in the file chooser dialog |
| Deleting Files | If selected, the JFileChooser integration will allow users to delete files displayed in the file chooser dialog |
| Downloading Files | If selected, the JFileChooser integration will allow users to download files displayed in the file chooser dialog |
| Auto-Download from Save Dialog | If selected, the JFileChooser dialog's save mode will trigger file download as soon as the selected file is available on filesystem |
| Transparent Open File Dialog | If selected, the JFileChooser dialog's open mode will open a client side file browser and transparently upload selected files and triggers selection |
| Transparent Save File Dialog | If selected, the JFileChooser dialog's save mode will open a client side dialog to enter the file name to be saved |
| Upload Folder | If Isolated Filesystem is enabled. This will be the folder on the server where the user can upload and download files from. Multiple folders can be defined using path separator (${path.separator}) |
| Clear Upload Folder | If enabled, all files in the transfer folder will be deleted when the application process is terminated |
| Upload Size Limit | Maximum size of upload for single file (in MB). Set 0 for unlimited size |
| Domains Allowed to Embed | If you are embedding webswing to page on different domain, you have to enable Cross-origin resource sharing (CORS) by adding the domain in this list. Use * to allow all domains |
| Allow JsLink | If enabled, the JSLink feature will be enabled, allowing application to invoke javascript and vice versa. (See netscape.javascript.JSObject) |
| Allow Local Clipboard | Enables built-in integration of client's local clipboard. Due to browser security limitations clipboard toolbar is displayed |

---

## Fonts configuration

Selection of fonts available to any Swing application is dependent on the platform it is running on. Different set of fonts is available on Windows system and different on Linux system. To ensure the user experience is consistent, Webswing provides an option to configure which fonts will be available for each swing application.

Font are configured in `webswing.config` file as described above. This is how the configuration looks like:  

```
  "fontConfig" : {
      "dialog" : "${user.dir}/fonts/Roboto-Regular.ttf",
      "dialoginput" : "${user.dir}/fonts/RobotoMono-Regular.ttf",
      "serif" : "${user.dir}/fonts/RobotoSlab-Regular.ttf"
    },
```

If you omit this setting or no fonts are defined, Webswing will use the default platform specific settings.

If this setting is present, only fonts defined in this property file will be available to your swing application. 
  
Font settings are important when DirectDraw rendering is enabled. DirectDraw will transfer configured fonts to browser (when used for the first time) and use them as native fonts. 

>Using too many or too large fonts may result in rendering delays. 

If fonts are not configured, Webswing will use default browser fonts for rendering logical font families ( `dialog, dialoginput, sansserif, serif, monospaced`) and less efficient Glyph rendering for other fonts.

---

## Custom Swing startup script

Sometimes it is necessary to prepare the environment before the Swing process is started. This may include steps like 
changing current working directory or using sudo to run Swing as different user. This can be achieved by pointing the `jreExecutable` 
option to custom startup script. 

Custom Swing startup script must follow few rules in order to work with Webswing: 

1. Last step of the script should execute Java with the arguments as passed in by Webswing. (ie. `$JAVA_HOME/bin/java $@` )
2. If the script has arguments of its own, they should be shifted before calling Java (`shift 3` if your script uses 3 arguments)
3. Be aware that variable resolution in `webswing.config` is done in servers context. (the environment changes will not be reflected to variables defined in webswing.config)

Here is an example of custom script that will use `sudo` to run the swing process as logged in user. We assume that users defined in `users.properties` have OS level counterparts
and the user used to start the server is properly configured in `sudoers` (needs NOPASSWD flag in sudoers - see man page). 

Here is our application configuration:
```json
{
    "name" : "SwingSet3",
    "jreExecutable": "startSwingSet3.sh ${user}",
    "javaVersion": "1.8",
    "mainClass" : "com.sun.swingset3.SwingSet3",
    "classPathEntries" : [ "SwingSet3.jar", "lib/*.jar" ],
    "vmArgs" : "-Xmx128m",
    "args" : "",
    "homeDir" : "demo/SwingSet3"
}
```

When Webswing will start a Swing application with above configuration, command line will look like this:

```
startSwingSet3.sh johnDoe -Xmx128m <webswing specific configuration> -cp webswing-server.war main.Main
```

Now the custom script `demo/SwingSet3/startSwingSet3.sh` that runs the Java process as logged in user will look like following:

```sh
#!/bin/sh
#save user to temporary variable
USER=$1
#shift the arguments by one - the user
shift
#handle system events
trap : SIGTERM SIGINT
#run java with sudo 
sudo -u $USER /home/work/jdk/jdk/bin/java $@ &
#wait for java to exit or kill if signaled 
JAVA_PID=$!
wait $JAVA_PID
if [[ $? -gt 128 ]]
then
    kill $JAVA_PID
fi
```




