---
title: "API"
weight: 50
---

## Maven Dependency

Webswing provides a set of services for cases when your application needs to interact with Webswing.
In your project you need to setup maven dependency on *webswing-api* like:
```xml
<dependency>
	<groupId>org.webswing</groupId>
	<artifactId>webswing-api</artifactId>
	<version>2.5</version>
</dependency>
```

To use Webswing API just call static method *WebswingUtil.getWebswingApi()*
Some examples are given in the project *webswing-demo-swingset3* in the class *ApiDemo*

### User Related Services

**getPrimaryUser()**

Returns the user of connected web session. Note:if user disconnects/closes browser, this method will return null.

**getMirrorViewUser()**

Returns the user of connected mirror view web session. (Admin console -> view session) Note:if admin disconnects/closes browser, this method will return null.

**Boolean primaryUserHasRole(String role) throws WebswingApiException**

Check if connected web session user has role defined.

`Parameters:`
_role_ - name of role

`Returns:`
_True_ if user has the role, _false_ if not. _Null_ if no user is connected

`Throws:`
_WebswingApiException_ - if communication with server fails.

**Boolean primaryUserIsPermitted(String permission) throws WebswingApiException**

Checks if connected web session user has permission defined.

`Parameters`
_permission_ - name of permission

`Returns`
_True_ if user has the permission, _false_ if not. _Null_ if no user is connected

`Throws`
_WebswingApiException_ - if communication with server fails.

---
### Event Listeners Services

**void addUserConnectionListener(WebswingUserListener listener)**

Adds listener to receive notifications when user (primary/mirror view) connects or disconnects from session

**void removeUserConnectionListener(WebswingUserListener listener)**

Removes user listener

**void notifyShutdown(int forceKillTimeout)**

When swing application exit and shutdown process takes longer time to process, invoking this method will notify user (web session) the application has finished, and disconnect the user from the session. Swing Application process is removed from running sessions list even though the process might still be running.

`Parameters` _forceKillTimeout_ - how long (in Ms) to wait for process to finish. After this time the process is forcefully terminated.

**void addShutdownListener(WebswingShutdownListener listener)**

Adds listener that is triggered when Webswing requests swing application to exit. If there is no explicit shutdown listener Webswing use default shutdown procedure (send window closing event to all windows). Otherwise listeners are fired. It is expected the result of listener execution will exit the swing process. Otherwise the swing process will be forcefully terminated after defined timeout (system property "webswing.waitForExit", default: 30000)

**void removeShutdownListener(WebswingShutdownListener listener)**

Removes shutdown listener

**void setUrlState(WebswingUrlState state)**

See WebswingApi#setUrlState(WebswingUrlState, boolean). This method will not trigger url change event in WebswingUrlStateChangeListener

`Parameters` _state_ - state object url is generated from

**void setUrlState(WebswingUrlState state, boolean fireChangeEvent)**

Sets the hash Fragment of users browser URL to represent the current state of the swing application. Intended for use in combination with WebswingUrlStateChangeListener and/or WebswingApi#getUrlState()

`Parameters`
_state_ - state object url is generated from, 
_fireChangeEvent_ - if true, invoking this method will trigger url change event


**WebswingUrlState getUrlState()**

Returns current user's URL state (parsed hash fragment of URL) or null if no user is connected.

**void removeUrlStateChangeListener(WebswingUrlStateChangeListener listener)**

Registers a URL state change listener

**void addUrlStateChangeListener(WebswingUrlStateChangeListener listener)**

Removes URL state change listener

### Clipboard services

**BrowserTransferable getBrowserClipboard()**

Clipboard data received from browser after CTRL+V key events (browser security allows access to clipboard only in these events).
Typically used for customized clipboard integration, while the built-in integration is disabled in configuration ("allowLocalClipboard" is false).

Example use case:

1. Create new context menu item "Paste from browser", that will open a modal dialog asking user to press CTRL+V.
2. Listen for CTRL+V keystroke. When received, call this method to get the clipboard content.

Returns latest clipboard content received from browser

**BrowserTransferable getBrowserClipboard(PasteRequestContext ctx)**

Requests user to paste from browser clipboard by showing built-in html modal dialog. This method will block EDT thread (by showing a invisible modal JDialog) until response received from user.
Typically used for customized clipboard integration, while the built-in integration is disabled in configuration ("allowLocalClipboard" is false).

Returns user submitted clipboard content received from browser (null if canceled)

**void sendClipboard(WebswingClipboardData content)**

Sends the specified data to browser.
A toolbar will appear in browser displaying the data. User can click or press CTRL+C to store the content to local clipboard.
Typically used for customized clipboard integration, while the built-in integration is disabled in configuration ("allowLocalClipboard" is false).

`Parameters`
_content_ - clipboard data to be sent to browser

**void sendClipboard()**

Sends the current Swing clipboard content to browser.
A toolbar will appear in browser displaying the data. User can click or press CTRL+C to store the content to local clipboard.
Typically used for customized clipboard integration, while the built-in integration is disabled in configuration ("allowLocalClipboard" is false).

### Additional services

**void resetInactivityTimeout()**

Resets session timeout to prevent automatic termination. Useful if a long running operation has to finish even if user disconnects or is inactive for longer timeframe. Note: Reset needs to be called in periods shorter than configured session timeout. ("webswing.sessionTimeoutSec" system property) Note2: This method has no effect if session timeout is set to 0

**String getWebswingVersion**

Returns the Webswing version in 'git describe' format