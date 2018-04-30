define(['webswing-dd', 'webswing-util'], function amdFactory(WebswingDirectDraw, util) {
    "use strict";
    return function BaseModule() {
        var module = this;
        var api;
        module.injects = api = {
            cfg: 'webswing.config',
            disconnect: 'webswing.disconnect',
            getSocketId: 'socket.uuid',
            send: 'socket.send',
            getCanvas: 'canvas.get',
            registerInput: 'input.register',
            sendInput: 'input.sendInput',
            registerTouch: 'touch.register',
            getUser: 'login.user',
            login: 'login.login',
            logout: 'login.logout',
            touchSession: 'login.touchSession',
            getIdentity: 'identity.get',
            disposeIdentity: 'identity.dispose',
            getLocale: 'translate.getLocale',
            showDialog: 'dialog.show',
            showDialogBar: 'dialog.showBar',
            hideDialog: 'dialog.hide',
            hideDialogBar: 'dialog.hideBar',
            dialogBarContent: 'dialog.currentBar',
            currentDialog: 'dialog.current',
            startingDialog: 'dialog.content.startingDialog',
            stoppedDialog: 'dialog.content.stoppedDialog',
            timedoutDialog: 'dialog.content.timedoutDialog',
            unauthorizedAccess: 'dialog.content.unauthorizedAccess',
            applicationAlreadyRunning: 'dialog.content.applicationAlreadyRunning',
            sessionStolenNotification: 'dialog.content.sessionStolenNotification',
            tooManyClientsNotification: 'dialog.content.tooManyClientsNotification',
            continueOldSessionDialog: 'dialog.content.continueOldSessionDialog',
            inactivityTimeoutWarningDialog: 'dialog.content.inactivityTimeoutWarningDialog',
            applicationBusyDialog: 'dialog.content.applicationBusyDialog',
            processFileDialogEvent: 'files.process',
            closeFileDialog: 'files.close',
            openLink: 'files.link',
            print: 'files.print',
            download: 'files.download',
            displayCopyBar: 'clipboard.displayCopyBar',
            displayPasteDialog: 'clipboard.displayPasteDialog',
            processJsLink: 'jslink.process',
            playbackInfo: 'playback.playbackInfo'
        };
        module.provides = {
            startApplication: startApplication,
            startMirrorView: startMirrorView,
            continueSession: continueSession,
            kill: kill,
            handshake: handshake,
            processMessage: processMessage,
            dispose: dispose,
            repaint: repaint
        };

        var timer1, timer3;
        var drawingLock;
        var drawingQ = [];
        var warningTimeout = null;
        var windowImageHolders = {};
        var directDraw = new WebswingDirectDraw({logDebug:api.cfg.debugLog});

        function startApplication(name) {
            initialize(api.getUser() + api.getIdentity() + name, name, false);
        }

        function startMirrorView(clientId, appName) {
            initialize(clientId, appName, true)
        }

        function initialize(clientId, name, isMirror) {
            api.registerInput();
            api.registerTouch();
            window.addEventListener('beforeunload', beforeUnloadEventHandler);
            if (!isMirror) {
                window.addEventListener('hashchange', handshake);
            }
            resetState();
            api.cfg.clientId = clientId;
            api.cfg.appName = name;
            api.cfg.canPaint = true;
            api.cfg.hasControl = !isMirror;
            api.cfg.mirrorMode = isMirror;
            handshake();
            if (isMirror) {
                repaint();
            }
            api.showDialog(api.startingDialog);
        }

        function beforeUnloadEventHandler(evt) {
            dispose();
        }

        function continueSession() {
            api.hideDialog();
            api.cfg.canPaint = true;
            handshake();
            repaint();
            ack();
        }

        function resetState() {
            api.cfg.clientId = '';
            api.cfg.viewId = util.GUID();
            api.cfg.appName = null;
            api.cfg.hasControl = false;
            api.cfg.mirrorMode = false;
            api.cfg.canPaint = false;
            clearInterval(timer1);
            clearInterval(timer3);
            timer1 = setInterval(api.sendInput, 100);
            timer3 = setInterval(servletHeartbeat, 100000);
            windowImageHolders = {};
            directDraw.dispose();
            directDraw = new WebswingDirectDraw({logDebug:api.cfg.debugLog});
        }

        function sendMessageEvent(message) {
            api.sendInput({
                event: {
                    type: message
                }
            });
        }

        function servletHeartbeat() {
            // touch servlet session to avoid timeout
            api.touchSession();
        }

        function repaint() {
            sendMessageEvent('repaint');
        }

        function ack(data) {
            sendMessageEvent('paintAck');
            if (data != null) {
                api.sendInput({
                    timestamps: {
                        startTimestamp: data.startTimestamp,
                        sendTimestamp: data.sendTimestamp,
                        renderingTime: "" + (new Date().getTime() - data.receivedTimestamp)
                    }
                });
            }
        }

        function kill() {
            if (api.cfg.hasControl) {
                sendMessageEvent('killSwing');
            }
        }

        function unload() {
            if (!api.cfg.mirrorMode) {
                sendMessageEvent('unload');
            }
        }

        function handshake() {
            api.sendInput(getHandShake(api.getCanvas()));
        }

        function dispose() {
            clearInterval(timer1);
            unload();
            api.sendInput();
            resetState();
            api.closeFileDialog();
            api.hideDialogBar();
            window.removeEventListener('beforeunload', beforeUnloadEventHandler);
            directDraw.dispose();
        }

        function processMessage(data) {
            if (data.playback != null) {
                api.playbackInfo(data);
            }
            if (data.applications != null && data.applications.length != 0) {
                if (api.cfg.mirror) {
                    startMirrorView(api.cfg.clientId, api.cfg.applicationName);
                } else if (data.applications.length === 1) {
                    startApplication(data.applications[0].name);
                }
                return;
            }
            if (data.event != null && !api.cfg.recordingPlayback) {
                if (data.event == "shutDownAutoLogoutNotification") {
                    api.disconnect();
                    if (api.cfg.mirror == false) {
                        api.logout();
                    }
                } else if (data.event == "shutDownNotification") {
                    if (api.currentDialog() !== api.timedoutDialog) {
                        api.showDialog(api.stoppedDialog);
                    }
                    api.disconnect();
                } else if (data.event == "applicationAlreadyRunning") {
                    api.showDialog(api.applicationAlreadyRunning);
                } else if (data.event == "tooManyClientsNotification") {
                    api.showDialog(api.tooManyClientsNotification);
                } else if (data.event == "continueOldSession") {
                    continueSession();
                    var oldBarContent = api.dialogBarContent();
                    api.showDialogBar(api.continueOldSessionDialog);
                    api.dialogBarContent().focused = false;
                    window.setTimeout(function () {
                        if (api.dialogBarContent()!=null && !api.dialogBarContent().focused) {
                            api.hideDialogBar();
                            api.showDialogBar(oldBarContent);
                        }
                    }, 5000);
                } else if (data.event == "sessionStolenNotification") {
                    api.cfg.canPaint = false;
                    api.showDialog(api.sessionStolenNotification);
                } else if (data.event == "unauthorizedAccess") {
                    api.cfg.canPaint = false;
                    api.showDialog(api.unauthorizedAccess);
                } else if (data.event == "sessionTimeoutWarning") {
                    window.clearTimeout(warningTimeout);
                    api.showDialogBar(api.inactivityTimeoutWarningDialog);
                    warningTimeout = window.setTimeout(function () {
                        api.hideDialogBar();
                    }, 2000);
                } else if (data.event == "sessionTimedOutNotification") {
                    api.showDialog(api.timedoutDialog);
                    api.disconnect();
                } else if (data.event == "applicationBusy") {
                    if(api.currentDialog()==null){
                        api.showDialog(api.applicationBusyDialog);
                    }
                }
                return;
            }
            if (data.jsRequest != null && api.cfg.mirrorMode == false && !api.cfg.recordingPlayback) {
                api.processJsLink(data.jsRequest);
            }
            if (data.pixelsRequest != null && api.cfg.mirrorMode == false && !api.cfg.recordingPlayback) {
                var pixelsResponse = getPixels(data.pixelsRequest);
                api.send(pixelsResponse);
            }
            if (api.cfg.canPaint) {
                queuePaintingRequest(data);
            }
        }

        function queuePaintingRequest(data) {
            drawingQ.push(data);
            if (!drawingLock) {
                processNextQueuedFrame();
            }
        }

        function processNextQueuedFrame() {
            drawingLock = null;
            if (drawingQ.length > 0) {
                drawingLock = drawingQ.shift();
                try {
                    processRequest(api.getCanvas(), drawingLock);
                } catch (error) {
                    drawingLock = null;
                    throw error;
                }
            }
        }

        function processRequest(canvas, data) {
            if (data.windows != null && data.windows.length > 0) {
                api.hideDialog();
            }
            var context = canvas.getContext("2d");
            if (data.linkAction != null && !api.cfg.recordingPlayback) {
                if (data.linkAction.action == 'url') {
                    api.openLink(data.linkAction.src);
                } else if (data.linkAction.action == 'print') {
                    api.print('file?id=' + data.linkAction.src);
                } else if (data.linkAction.action == 'file') {
                    api.download('file?id=' + data.linkAction.src);
                } else if (data.linkAction.action == 'redirect' && !api.cfg.mirrorMode) {
                    window.location.href = data.linkAction.src;
                }
            }
            if (data.moveAction != null) {
                copy(data.moveAction.sx, data.moveAction.sy, data.moveAction.dx, data.moveAction.dy, data.moveAction.width, data.moveAction.height, context);
            }
            if (data.cursorChange != null && api.cfg.hasControl && !api.cfg.recordingPlayback) {
                canvas.style.cursor = getCursorStyle(data.cursorChange);
            }
            if (data.copyEvent != null && api.cfg.hasControl && !api.cfg.recordingPlayback) {
                api.displayCopyBar(data.copyEvent);
            }
            if(data.pasteRequest != null && api.cfg.hasControl && !api.cfg.recordingPlayback){
                api.displayPasteDialog(data.pasteRequest);
            }
            if (data.fileDialogEvent != null && api.cfg.hasControl && !api.cfg.recordingPlayback) {
                api.processFileDialogEvent(data.fileDialogEvent);
            }
            if (data.closedWindow != null) {
                delete windowImageHolders[data.closedWindow.id];
            }
            // first is always the background
            for (var i in data.windows) {
                var win = data.windows[i];
                if (win.id == 'BG') {
                    if (api.cfg.mirrorMode || api.cfg.recordingPlayback) {
                        adjustCanvasSize(canvas, win.width, win.height);
                    }
                    for (var x in win.content) {
                        var winContent = win.content[x];
                        if (winContent != null) {
                            clear(win.posX + winContent.positionX, win.posY + winContent.positionY, winContent.width, winContent.height, context);
                        }
                    }
                    data.windows.splice(i, 1);
                    break;
                }
            }
            // regular windows (background removed)
            if (data.windows != null) {
                data.windows.reduce(function (sequence, window) {
                    return sequence.then(function () {
                        return renderWindow(window, context);
                    }, errorHandler);
                }, Promise.resolve()).then(function () {
                    ack(data);
                    processNextQueuedFrame();
                }, errorHandler);
            } else {
                processNextQueuedFrame();
            }
        }

        function renderWindow(win, context) {
            if (win.directDraw != null) {
                return renderDirectDrawWindow(win, context);
            } else {
                return renderPngDrawWindow(win, context);
            }
        }

        function renderPngDrawWindow(win, context) {
            return win.content.reduce(function (sequence, winContent) {
                return sequence.then(function () {
                    return new Promise(function (resolved, rejected) {
                        if (winContent != null) {
                            var imageObj = new Image();
                            var onloadFunction = function () {
                                context.drawImage(imageObj, win.posX + winContent.positionX, win.posY + winContent.positionY);
                                resolved();
                                imageObj.onload = null;
                                imageObj.src = '';
                                if (imageObj.clearAttributes != null) {
                                    imageObj.clearAttributes();
                                }
                                imageObj = null;
                            };
                            imageObj.onload = function () {
                                // fix for ie - onload is fired before the image
                                // is ready for rendering to canvas.
                                // This is an ugly quickfix
                                if (api.cfg.ieVersion && api.cfg.ieVersion <= 10) {
                                    window.setTimeout(onloadFunction, 20);
                                } else {
                                    onloadFunction();
                                }
                            };
                            imageObj.src = util.getImageString(winContent.base64Content);
                        }
                    });
                }, errorHandler);
            }, Promise.resolve());
        }

        function renderDirectDrawWindow(win, context) {
            return new Promise(function (resolved, rejected) {
                var ddPromise;
                if (typeof win.directDraw === 'string') {
                    ddPromise = directDraw.draw64(win.directDraw, windowImageHolders[win.id]);
                } else {
                    ddPromise = directDraw.drawBin(win.directDraw, windowImageHolders[win.id]);
                }
                ddPromise.then(function (resultImage) {
                    windowImageHolders[win.id] = resultImage;
                    for (var x in win.content) {
                        var winContent = win.content[x];
                        if (winContent != null) {
                            var dx = win.posX + winContent.positionX;
                            var dy = win.posY + winContent.positionY;
                            var dw = winContent.width;
                            var dh = winContent.height;
                            if (dx <= context.canvas.width && dy <= context.canvas.height && dx + dw > 0 && dy + dh > 0) {
                                context.drawImage(resultImage, winContent.positionX, winContent.positionY, winContent.width, winContent.height, dx, dy, dw, dh);
                            }
                        }
                    }
                    resolved();
                }, function (error) {
                    rejected(error);
                });
            });
        }

        function adjustCanvasSize(canvas, width, height) {
            if (canvas.width != width || canvas.height != height) {
                var snapshot = canvas.getContext("2d").getImageData(0, 0, canvas.width, canvas.height);
                canvas.width = width;
                canvas.height = height;
                canvas.getContext("2d").putImageData(snapshot, 0, 0);
            }
        }

        function clear(x, y, w, h, context) {
            context.clearRect(x, y, w, h);
        }

        function copy(sx, sy, dx, dy, w, h, context) {
            var copy = context.getImageData(sx, sy, w, h);
            context.putImageData(copy, dx, dy);
        }

        function getCursorStyle(cursorMsg) {
            if (cursorMsg.b64img == null) {
                return cursorMsg.cursor;
            } else {
                if (api.cfg.ieVersion) {
                    return 'url(\'' + api.cfg.connectionUrl + 'file?id=' + cursorMsg.curFile + '\'), auto';
                } else {
                    var data = util.getImageString(cursorMsg.b64img);
                    return 'url(' + data + ') ' + cursorMsg.x + ' ' + cursorMsg.y + ' , auto';
                }
            }
        }

        function getHandShake(canvas) {
            var handshake = {
                applicationName: api.cfg.appName,
                clientId: api.cfg.clientId,
                viewId: api.cfg.viewId,
                sessionId: api.getSocketId(),
                locale: api.getLocale(),
                mirrored: api.cfg.mirrorMode,
                directDrawSupported: api.cfg.typedArraysSupported && !(api.cfg.ieVersion && api.cfg.ieVersion <= 10),
                url: window.location.href
            };

            if (!api.cfg.mirrorMode) {
                handshake.documentBase = api.cfg.documentBase;
                handshake.params = api.cfg.params;
                handshake.desktopWidth = canvas.offsetWidth;
                handshake.desktopHeight = canvas.offsetHeight;
            }
            return {
                handshake: handshake
            };
        }

        function getPixels(request) {
            var result = document.createElement("canvas");
            result.width = request.w;
            result.height = request.h;
            var ctx = api.getCanvas().getContext("2d");
            var imgData = ctx.getImageData(request.x, request.y, request.h, request.w);
            var resctx = result.getContext("2d");
            resctx.putImageData(imgData, 0, 0);
            var dataurl = result.toDataURL("image/png");
            return {
                pixelsResponse: {
                    correlationId: request.correlationId,
                    pixels: dataurl
                }
            };
        }

        function errorHandler(error) {
            drawingLock = null;
            throw error;
        }
    };
});
