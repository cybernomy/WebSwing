---
title: "URL Params"
weight: 20
---

## Available URL parameters
When you start Webswing it will be available on `http://localhost:8080`. The page served on this URL (`index.html`) allows you to change some of the default behavior using URL parameters. 

Following arguments are available by default:

### Additional application arguments
[http://localhost:8080?args=arg1 arg2 "arg3 and 4"](http://localhost:8080?args=arg1 arg2 "arg3 and 4") : for specifying additional arguments for the swing application. Value of this parameter will be available in `${customArgs}` variable that can be used in `webswing.config` file ( i.e. `"args": "${customArgs}"`). 
You can specify the arguments as you would in command line. The example URL would resolve to 3 arguments in your main method: `arg1` ,`arg2` and `arg3 and 4`.

### Disable binary socket
[http://localhost:8080?binarySocket=false](http://localhost:8080?binarySocket=false) : Binary socket is enabled by default. If you wish to use JSON for transferring data through websocket, set this option to false.

### Session recording
[http://localhost:8080?recording=true](http://localhost:8080?recording=true) : if recording is enabled, Webswing will create a recording file in temp folder, which can be later used for replay of the application frame by frame just like a movie.

### Session playback 
[http://localhost:8080?recordingPlayback=path/to/recordingFile.wss](http://localhost:8080?recordingPlayback=path) : This option can be used to play the recording file. This option is internally used in Admin console for playback of finished sessions. Only users with admin role are allowed to playback recordings.

### Debugging 
[http://localhost:8080?debugPort=8000](http://localhost:8080?debugPort=8000) : See [debuging guide](../../contrib)


 

URL parameters can be combined together.

If your deployment has other specific requirements which are not achievable by this set of parameters, you can embed Webswing's JavaScript snippet to your own web page, where you have full control and ability to further customize the behavior.

