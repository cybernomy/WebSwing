---
title: "Admin Console"
weight: 30
---

For configuration, management and monitoring purposes Webswing provides a convenient web interface.
![admin console screen](../img/admin-console.png)

## Statistics
From the main screen of Admin Console you can see some basics statistics of each application. This may help you to quickly analyze the current state of the applications to support the maintenance and monitoring.

![sessions](../img/sessions.png)

Overall application statistics - session aggregated.

![stats](../img/stats.png)

*Show sessions* button will navigate you to sessions detail page of the specific application. Here you can see details about each session, shutdown the session, record the session.

![sessions](../img/sessions-detail.png)

## Clone view
In the session detail page you can click the *View* button to see the copy of the user's session. 
Following information and features are available:

* Username of the session owner
* Statistics of current session
* Clone view of the session
* Full control of the application
* Possibility to record the session
* Shutting down the session

![session mirror](../img/session-mirror.png)

## Logs
In the top navigation bar you can access *Logs*.  
Within the logs page 2 logs are accessible:

* Audit - application access logs, pointed to System Property *webswing.log.file.audit* (by default audit.log)
* Server - application event logs, pointed to System Property *webswing.log.file.server* (by default webswing.log)

![logs](../img/logs.png)

## Access control 
Webswing supports broad options of access control. Following types and features are available:

* admin page per application - specific admin rights per Webswing application within one Webswing server
* security configuration defined globally at Webswing server level
* security defined per application
* various options of built-in security modules: SAML2, KEYCLOACK, OPENID_CONNECT, DATABASE 
* it is possible to implement custom security extension and easily plug it in Webswing