---
title: "Embedding"
weight: 30
---

## Embedding to web page

With Webswing it is possible to embed your swing application to your own web page. Good example is the default Webswing page or Admin console mirror view feature, where Webswing JavaScript is embedded in same like in below snippet. You can copy this snippet to your web page: 

```html
<head>
  <link rel="stylesheet" href="http://<webswing-host-and-port>/<application_path>/css/style.css"/>
</head>
```

```html
<div class="webswing-element" data-webswing-instance="webswingInstance0">
    <div id="loading" class="ws-modal-container">
        <div class="ws-login">
			<div  class="ws-login-content">
				<div class="ws-spinner"><div class="ws-spinner-dot-1"></div> <div class="ws-spinner-dot-2"></div></div>
			</div>
		</div>
	</div>
</div>
```

```html
<script>
    var webswingInstance0 = {
        options: {
            autoStart: true,
            args: 'foo',
            recording: getParam('recording'),
            binarySocket: getParam('binarySocket'),
            debugPort: getParam('debugPort'),
            recordingPlayback: getParam('recordingPlayback'),
            connectionUrl:'http://<webswing-host-and-port>/<application_path>' 
        }
    }

    function getParam(name) {
        name = name.replace(/[\[]/, "\\\[").replace(/[\]]/, "\\\]");
        var results = new RegExp("[\\?&]" + name + "=([^&#]*)").exec(location.href);
        return results == null ? null : decodeURIComponent(results[1]);
    }
</script>
```

```html
<script data-webswing-global-var="webswing">
    (function (window, document) {
        var loader = function () {
            var baseUrl = 'http://<webswing-host-and-port>/<application_path>';
            baseUrl = baseUrl.indexOf("/", baseUrl.length - 1) !== -1 ? baseUrl : (baseUrl + "/");
            var xmlhttp = new XMLHttpRequest();
            xmlhttp.onreadystatechange = function () {
                if (xmlhttp.readyState == XMLHttpRequest.DONE) {
                    var version = xmlhttp.status == 200 ? xmlhttp.responseText : "undefined";
                    var script = document.createElement("script"),
                        tag = document.getElementsByTagName("script")[0];
                    script.src = baseUrl + "javascript/webswing-embed.js?version=" + version;
                    tag.parentNode.insertBefore(script, tag);
                }
            };
            xmlhttp.open("GET", baseUrl + "rest/version", true);
            xmlhttp.send();
        };
        window.addEventListener ? window.addEventListener("load", loader, false) : window.attachEvent("onload", loader);
    })(window, document);
</script>
```

There are few things you need to configure in the snippet. Most important is to replace the 3  `<webswing-host-and-port>/<application_path>` placeholders with the location of your Webswing deployment. For example `localhost:8080/swingset3` if using the default settings. Note that `<application_path>` must be lowercase.


The `data-webswing-instance="webswingInstance0"` attribute tells the JavaScript to use this div element as Webswing instance. Name of the instance `"webswingInstance0"` will be used as name for global object variable with functions controlling this instance. Following functions are available in this global object: 

* **`configure(options)`** - configures this webswing instance. This function read the options from `<data-webswing-instance_value>.config` (ie. webswingInstance0.options) global variable if exists. Argument `options` is object with following properties:
	* `autoStart`  - tells Webswing to execute configure() and start() right after the instance is initialized. If it is false, start() function has to be triggered manually. This option only valid if set up in `data-webswing-options` attribute
    * `args` - additional Java application arguments. Appended to those defined in configuration.
    * `binarySocket` - use binary transfer in websocket - saves bandwidth (default:true)
    * `recording` - record this application session (default:false)
    * `clientId` - set the clientId, used with mirror session or to continue running session. 
    * `mirror` - for starting a mirror session. Only admin role is allowed to use this (default:false)
    * `connectionUrl` - base URL for connecting to websocket service. (default: current location url) 
    * `debugPort` - integer that specifies on which port should the debugger listen. See development docs for more information.
    * `javaCallTimeout` - JsLink Java method invocation timeout. If Java method is not returned with result, error is logged to browser console.  
    * `recordingPlayback` - File for session recording playback. Only admin role allowed to use this.
    * `appletParams` - Applet parameters. Ignored if swing application is not applet.
* **`disconnect()`** - disconnects the current Webswing session, but leaving the swing application running. 
* **`kill()`** - disconnects the current Webswing session with stopping the swing application. 
* **`setControl(boolean)`** - enables/disables the control of application. If set to false, no user events are sent to swing application.
* **`start()`** - This will initiate the connection to Webswing server and start the swing application. If the `autoStart` is set to false or not defined in config object, start function has to be called manually, otherwise the Webswing will call start function automatically. 

You can control the size of the div element where Webswing is displayed with regular CSS . You can even change the size of the div dynamically and Webswing canvas will automatically scale. 

If you are embedding Webswing to page on different domain, you will have to enable Cross-origin resource sharing (`CORS`) in applications configuration's  `allowedCorsOrigins` option. Set `*` to allow all domains, or use list of allowed domains. 


## Bootstrapping Webswing JavaScript 

When the Webswing JavaScript is loaded it will scan the DOM for div elements with attribute `data-webswing-instance` and will initialize them automatically. For use cases when Webswing instance has to be started later it is possible to export global Webswing variable exposing the bootstrapping API. 
By defining the attribute `data-webswing-global-var="webswingControl"` on the `<script>` element of embedded snippet, you tell Webswing to create a global variable called `webswingControl` which has following functions: 

* **`scan()`** - re-runs the full DOM search for `data-webswing-instance` attribute.
* **`bootstrap(divElement)`** - instantiate Webswing in div element specified in argument. 