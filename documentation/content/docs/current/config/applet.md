---
title: "Applets Setup"
weight: 20
---

## Java Applet configuration

Webswing now offers a solution for running applets in web browser after the support of Java plugins is sbeing discontinued in modern browsers. Java applets are not supported any more in current versions of Chrome browser: [NPAPI support removed from Chrome](https://java.com/en/download/faq/chrome.xml).

Webswing offers an easy way to continue using your applets on your page the same way you did before, but more securely and reliably. No more struggling with getting your applet to run for clients. Here is how it works.

**Applets support:** 

* **[Embedding to your web page](../../integrate/browser)**
* **[Java <-> JavaScript interface](../../integrate/jslink)**

---

## Setup your applet in Webswing server

In Admin console you can set up your Java applet the same way as a swing application and most of the settings in the panel are same as with standard [Swing applications](../swing). The only difference is in `Application - Java` section, where you set up your **Launcher Type** as _Applet_ and configure specific settings in _Launcher configuration_ `appletClass` and additional `applet parameters` for applets.

![Applet configuration screen](../img/applet-config.png)

The same can be achieved through JSON configuration file (webswing.config)/
These are the main differences in configuration in comparision to a Swing application:

```JSON
...
"launcherType" : "Applet",
"launcherConfig" : {
  "appletClass" : "org.webswing.demo.applet.SwingSet3Applet",
  "parameters" : {
    "param1" : "paramvalue1",
    "param2" : "paramvalue2"
  }
},
...
```

**`appletClass`** This is the "main" class of your Java Applet, which extends `java.applet.Applet` class.

**`parameters`** For setting custom applet parameters. 

>Note: rest of the properties are documented in [swing application setup page](../swing)