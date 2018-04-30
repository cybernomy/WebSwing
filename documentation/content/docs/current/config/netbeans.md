---
title: "NetBeans setup"
weight: 20
---

## NetBeans Platform Application setup

In general NetBeans platform applications are configured the same way as [regular swing applications](../swing). We can use Admin console or edit the `webswing.config` file manually. There are however few specific settings that are necessary for running NetBeans in Webswing.

In Webswing distribution there are two examples for setting up NetBeans platform. 

---

## Simple NetBeans setup

First is a minimalistic NetBeans platform demonstration bootstrapped with simple main class, not using the standard NetBeans packaging, which use shell script or exe file for bootstrapping the application. Here is how its configured in Webswing: 

```json
{
    "name" : "NetBeans Platform",
    "mainClass" : "org.webswing.demo.NbMain",
    "classPathEntries" : [ "nbplatform.jar", "lib/*.jar" ],
    "vmArgs" : "-Dorg.netbeans.log.startup=print -DTopSecurityManager.disable=true",
	//rest of the JSON omitted
}  
```

Most important part here is the `-DTopSecurityManager.disable=true` property in `vmArgs`. This tells NetBeans to disable its default Java Security Manager which would otherwise prevent Webswing to do its magic. 

We are using our own main class to bootstrap the NetBeans application. This is the only class located in `nbplatform.jar`. The `lib` folder contains the smallest subset of NetBeans libraries for starting the NB platform window. This is the content of main class:

```java
package org.webswing.demo;

public class NbMain {
	public static void main(String[] args) throws Exception {
		org.netbeans.core.startup.Main.main(args);
	}
} 
```

![NetBeans platform window](../img/nbplatform.png)

---

## Full NetBeans IDE setup

Second example included in distribution is the full blown NetBeans 8 IDE. The NetBeans installation itself is not included in the Webswing distribution for  for obvious reasons. So before running it, you will need to set up the `homeDir` property in `webswing.config` file to point to your NetBeans installation.

Here is the snippet of relevant part of Webswing configuration for NetBeans IDE:

```json
 {
    "name" : "NetBeans IDE (set homeDir)",
    "mainClass" : "org.netbeans.Main",
    "classPathEntries" : [ "platform/lib/*.jar","platform/lib/*/*.jar" ],
    "vmArgs" : "-Dnetbeans.user=${user.dir}/tmp/netbeans/${user} -Dnetbeans.home=platform  -Dorg.netbeans.log.startup=print -DTopSecurityManager.disable=true -Dnetbeans.dirs=\"platform;nb;ergonomics;ide;extide;java;apisupport;webcommon;websvccommon;enterprise;mobility;profiler;python;php;identity;harness;cnd;dlight;groovy;extra;javacard;javafx\"",
    "args" : "",
    "homeDir" : "c:/Program Files/NetBeans 8.0.2",
    //rest of configuration omitted...
}
```
>Note: this setup is for unmodified folder structure of NetBeans IDE. 

This configuration mimics the shell script that initializes NetBeans IDE. Similar setup can be used for NetBeans platform applications that use the standard NetBeans packaging. 

Here are the most important parts of the setup explained.
 
**`mainClass : "org.netbeans.Main"`**

Main class for NetBeans will always be the above in case you are using standard way of packaging. 

**`"classPathEntries" : [ "platform/lib/*.jar","platform/lib/*/*.jar" ]`**

These folders contains the NetBeans libraries that are necessary for bootstrapping the NB application. Always use only these folders. NetBeans will load the rest of libraries by its plugin system.

**`"vmArgs" : "-Dnetbeans.home=platform"`**

This setting tells NetBeans where to look for platform libraries and resources.

**`"vmArgs" : "-Dnetbeans.user=${user.dir}/tmp/netbeans/${user}"`**

NetBeans create its user session files in folder specified by this setting. It is good to have it different for each user, therefore we include the Webswing specific variable `${user}` to the path. 

**`"vmArgs" : "-DTopSecurityManager.disable=true"`**

As noted above this will allow Webswing to do the magic.

**`"vmArgs" : "-Dnetbeans.dirs = \"platform;nb;ergonomics;ide;extide;java;apisupport...`**

This property will tell NetBeans which folders to look into for initializing the plugins. You can use the subset of necessary folders for your use case.
>Note: Be careful with the escaped chars like `\"` in the json configuration. It is recommended to use Admin console to avoid problems with escaping.

![Netbeans IDE](../img/netbeans.png) 