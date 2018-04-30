---
title: "Access Control"
weight: 5
---

## Security concept

Since version 2.5 Security can be configured for the server and per application separately.
Using different user base per application and for admin console access enables
easy multi-tenant deployments. 
 
To setup security for the server use the "Configuration" page in admin console. 
Application security is part of application's config screen. 

Both server and application security use the same configuration options.

First, select security module from list of built-in modules. 

## Built-in modules 

| Module | Description |
| ------------- | ------------- |
| INHERITED | Application does not define its own security, but shares security context with server instead. This module is not available the Server.  |
| NONE | Enables anonymous access to application. No login is required. |
| EMBEDED | Users, passwords and roles are defined directly in webswing.config configuration file.|
| PROPERTY_FILE | Users, passwords and roles are defined in property file.|
| DATABASE | Users are stored in database. Library with JDBC driver has to be specified in security module's class path. Database connection details has to be defined.|
| OPENID_CONNECT | Single-sign-on using OpenID Connect protocol. |
| SAML2 | Single-sign-on using SAML2 protocol. |
| KEYCLOAK | Keycloak authentication server integration module. |
| Custom module | Custom security module can be created and fully-qualified class name defined.|


>in case you need help with security setup contact support@webswing.org