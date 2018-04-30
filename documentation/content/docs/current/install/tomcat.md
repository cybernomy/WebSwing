---
title: "Tomcat Deployment"
weight: 40
---


Even though Webswing comes with embedded Jetty server, it is still possible to deploy it to external servlet container like Tomcat. Other J2EE servers should work as well, as far as they support Servlet 3.0 spec (but only Tomcat 8 is tested). Previous experience with Tomcat configuration is expected.

To deploy Webswing to Tomcat, follow the following steps:

1. Create a new folder named `webswing` in Tomcat's home folder and unzip the Webswing distribution to this folder.
2. Move `webswing-server.war` from distribution package to Tomcat's `webapps` folder.
3. In `conf/catalina.properties` file add the following properties. ( Or use `-D` JVM options to specify these properties) 

```properties
webswing.warLocation=webapps/webswing-server.war
webswing.configFile=webswing/webswing.config
webswing.tempDirBase=webswing/tmp
```

>Please note that the locations in demo `webswing.config` are pointing to relative paths, so in order to make the demo applications running in tomcat you will need to change the paths accordingly

In case of issues analyze the logfiles.
