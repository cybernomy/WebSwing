define(['atmosphere', 'ProtoBuf', 'jquery', 'text!webswing.proto'], function amdFactory(atmosphere, ProtoBuf, $, wsProto) {
    "use strict";
    var proto = ProtoBuf.loadProto(wsProto, "webswing.proto");
    var InputEventsFrameMsgInProto = proto.build("org.webswing.server.model.proto.InputEventsFrameMsgInProto");
    var AppFrameMsgOutProto = proto.build("org.webswing.server.model.proto.AppFrameMsgOutProto");

    return function SocketModule() {
        var module = this;
        var api;
        module.injects = api = {
            cfg: 'webswing.config',
            processMessage: 'base.processMessage',
            showDialog: 'dialog.show',
            showBar: 'dialog.showBar',
            hideDialog: 'dialog.hide',
            currentDialog: 'dialog.current',
            stoppedDialog: 'dialog.content.stoppedDialog',
            disconnectedDialog: 'dialog.content.disconnectedDialog',
            timedoutDialog: 'dialog.content.timedoutDialog',
            connectionErrorDialog: 'dialog.content.connectionErrorDialog',
            initializingDialog: 'dialog.content.initializingDialog',
            longPollingWarningDialog: 'dialog.content.longPollingWarningDialog'
        };
        module.provides = {
            connect: connect,
            send: send,
            uuid: getuuid,
            awaitResponse: awaitResponse,
            dispose: dispose
        };
        module.ready = function () {
            binary = api.cfg.typedArraysSupported && api.cfg.binarySocket;
        };

        var socket, uuid, binary;
        var responseHandlers = {};

        function connect() {
            var request = {
                url: api.cfg.connectionUrl + 'async/swing',
                contentType: "application/json",
                // logLevel : 'debug',
                transport: 'websocket',
                trackMessageLength: true,
                reconnectInterval: 5000,
                fallbackTransport: 'long-polling',
                enableXDR: true,
                headers: {}
            };

            if (binary) {
                request.url = request.url + '-bin';
                request.headers['X-Atmosphere-Binary'] = true;
                request.enableProtocol = false;
                request.trackMessageLength = false;
                request.contentType = 'application/octet-stream';
                request.webSocketBinaryType = 'arraybuffer';
            }

            if (api.cfg.recordingPlayback) {
                request.url = api.cfg.connectionUrl + 'playback/async/swing-play';
                request.headers['file'] = api.cfg.recordingPlayback;
            }

            if (api.cfg.args != null) {
                request.headers['X-webswing-args'] = api.cfg.args;
            }
            if (api.cfg.recording != null) {
                request.headers['X-webswing-recording'] = api.cfg.recording;
            }
            if (api.cfg.debugPort != null) {
                request.headers['X-webswing-debugPort'] = api.cfg.debugPort;
            }

            request.onOpen = function (response) {
                if (response.transport !== 'websocket') {
                    if (binary) {
                        console.error('Webswing: Binary encoding not supported for ' + response.transport + ' transport. Falling back to json encoding.');
                        api.cfg.binarySocket = false;
                        binary = false;
                        dispose();
                        connect();
                    } else {
                        api.showBar(api.longPollingWarningDialog);
                    }
                }
            };

            request.onReopen = function (response) {
                api.hideDialog();
            };

            request.onMessage = function (response) {
                var receivedTimestamp = new Date().getTime();
                try {
                    var data = decodeResponse(response);
                    if (data.sessionId != null) {
                        uuid = data.sessionId;
                    }
                    // javascript2java response handling
                    if (data.javaResponse != null && data.javaResponse.correlationId != null) {
                        var correlationId = data.javaResponse.correlationId;
                        if (responseHandlers[correlationId] != null) {
                            var callback = responseHandlers[correlationId];
                            delete responseHandlers[correlationId];
                            callback(data.javaResponse);
                        }
                    }
                    data.receivedTimestamp = receivedTimestamp;
                    api.processMessage(data);
                } catch (e) {
                    console.error(e.stack);
                    return;
                }
            };

            request.onClose = function (response) {
                if (api.currentDialog() !== api.stoppedDialog && api.currentDialog() !== api.timedoutDialog) {
                    api.showDialog(api.disconnectedDialog);
                }
            };

            request.onError = function (response) {
                api.showDialog(api.connectionErrorDialog);
            };
            $.ajax({
                xhrFields: {
                    withCredentials: true
                },
                type: 'GET',
                url: api.cfg.connectionUrl + 'rest/CSRFToken',
                success: function (data) {
                    request.headers['X-webswing-CSRFToken'] = data;
                    socket = atmosphere.subscribe(request);
                },
                error: function (xhr) {
                    api.showDialog(api.connectionErrorDialog);
                    console.error("CSRF Token validation failed.");
                }
            });
        }

        function decodeResponse(response) {
            var message = response.responseBody;
            var data;
            if (binary) {
                if (message.byteLength === 1) {
                    return {};// ignore atmosphere heartbeat
                }
                data = AppFrameMsgOutProto.decode(message);
                explodeEnumNames(data);
            } else {
                data = atmosphere.util.parseJSON(message);
            }
            return data;
        }

        function dispose() {
            atmosphere.unsubscribe(socket);
            socket = null;
            uuid = null;
        }

        function send(message) {
            if (socket != null) {
                if (socket.request.isOpen && !socket.request.closed) {
                    if (typeof message === "object") {
                        if (binary) {
                            var msg = new InputEventsFrameMsgInProto(message);
                            socket.push(msg.encode().toArrayBuffer());
                        } else {
                            socket.push(atmosphere.util.stringifyJSON(message));
                        }
                    } else {
                        console.log("message is not an object " + message);
                    }
                } else {
                    api.showDialog(api.disconnectedDialog);
                }
            }
        }

        function awaitResponse(callback, request, correlationId, timeout) {
            send(request);
            responseHandlers[correlationId] = callback;
            setTimeout(function () {
                if (responseHandlers[correlationId] != null) {
                    delete responseHandlers[correlationId];
                    callback(new Error("Java call timed out after " + timeout + " ms."));
                }
            }, timeout);
        }

        function getuuid() {
            return uuid;
        }

        function explodeEnumNames(data) {
            if (data != null) {
                if (Array.isArray(data)) {
                    data.forEach(function (d) {
                        explodeEnumNames(d);
                    });
                } else {
                    data.$type._fields.forEach(function (field) {
                        if (field.resolvedType != null) {
                            if (field.resolvedType.className === "Enum") {
                                var enm = field.resolvedType.object;
                                for (var key in enm) {
                                    if (enm[key] === data[field.name]) {
                                        data[field.name] = key;
                                    }
                                }
                            } else if (field.resolvedType.className === "Message") {
                                explodeEnumNames(data[field.name]);
                            }
                        }
                    });
                }
            }
        }
    };

});