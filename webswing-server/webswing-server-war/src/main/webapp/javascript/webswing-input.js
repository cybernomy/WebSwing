define(['jquery', 'webswing-util'], function amdFactory($, util) {
    "use strict";

    return function InputModule() {
        var module = this;
        var api;
        module.injects = api = {
            cfg: 'webswing.config',
            send: 'socket.send',
            getInput: 'canvas.getInput',
            getCanvas: 'canvas.get',
            cut: 'clipboard.cut',
            copy: 'clipboard.copy',
            paste: 'clipboard.paste'
        };
        module.provides = {
            register: register,
            sendInput: sendInput,
            dispose: dispose
        };

        module.ready = function () {
        };

        var registered = false;
        var latestMouseMoveEvent = null;
        var latestMouseWheelEvent = null;
        var latestWindowResizeEvent = null;
        var latestKeyDownEvent = null;
        var mouseDown = 0;
        var inputEvtQueue = [];

        function sendInput(message) {
            enqueueInputEvent();
            var evts = inputEvtQueue;
            inputEvtQueue = [];
            if (message != null) {
                evts.push(message);
            }
            if (evts.length > 0) {
                api.send({
                    events: evts
                });
            }
        }

        function enqueueInputEvent(message) {
            if (api.cfg.hasControl) {
                if (latestMouseMoveEvent != null) {
                    inputEvtQueue.push(latestMouseMoveEvent);
                    latestMouseMoveEvent = null;
                }
                if (latestMouseWheelEvent != null) {
                    inputEvtQueue.push(latestMouseWheelEvent);
                    latestMouseWheelEvent = null;
                }
                if (latestWindowResizeEvent != null) {
                    inputEvtQueue.push(latestWindowResizeEvent);
                    latestWindowResizeEvent = null;
                }
                if (latestKeyDownEvent != null) {
                    inputEvtQueue.push(latestKeyDownEvent);
                    latestKeyDownEvent = null;
                }
                if (message != null) {
                    if (JSON.stringify(inputEvtQueue[inputEvtQueue.length - 1]) !== JSON.stringify(message)) {
                        inputEvtQueue.push(message);
                    }
                }
            }
        }

        function resetInput() {
            latestMouseMoveEvent = null;
            latestMouseWheelEvent = null;
            latestWindowResizeEvent = null;
            latestKeyDownEvent = null;
            mouseDown = 0;
            inputEvtQueue = [];
        }

        function dispose() {
            registered = false;
            resetInput();
            document.removeEventListener('mousedown', mouseDownEventHandler);
            document.removeEventListener('mouseout', mouseOutEventHandler);
            document.removeEventListener('mouseup', mouseUpEventHandler);
        }

        function register() {
            if (registered) {
                return;
            }
            var canvas = api.getCanvas();
            var input = api.getInput();
            resetInput();
            focusInput(input);
            util.bindEvent(canvas, 'mousedown', function (evt) {
                var mousePos = getMousePos(canvas, evt, 'mousedown');
                latestMouseMoveEvent = null;
                enqueueInputEvent(mousePos);
                focusInput(input);
                sendInput();
                return false;
            }, false);
            util.bindEvent(canvas, 'dblclick', function (evt) {
                var mousePos = getMousePos(canvas, evt, 'dblclick');
                latestMouseMoveEvent = null;
                enqueueInputEvent(mousePos);
                focusInput(input);
                sendInput();
                return false;
            }, false);
            util.bindEvent(canvas, 'mousemove', function (evt) {
                var mousePos = getMousePos(canvas, evt, 'mousemove');
                latestMouseMoveEvent = mousePos;
                return false;
            }, false);
            util.bindEvent(canvas, 'mouseup', function (evt) {
                var mousePos = getMousePos(canvas, evt, 'mouseup');
                latestMouseMoveEvent = null;
                enqueueInputEvent(mousePos);
                focusInput(input);
                sendInput();
                return false;
            }, false);
            // IE9, Chrome, Safari, Opera
            util.bindEvent(canvas, "mousewheel", function (evt) {
                var mousePos = getMousePos(canvas, evt, 'mousewheel');
                latestMouseMoveEvent = null;
                if (latestMouseWheelEvent != null) {
                    mousePos.mouse.wheelDelta += latestMouseWheelEvent.mouse.wheelDelta;
                }
                latestMouseWheelEvent = mousePos;
                evt.preventDefault();
                evt.stopPropagation();
                return false;
            }, false);
            // firefox
            util.bindEvent(canvas, "DOMMouseScroll", function (evt) {
                var mousePos = getMousePos(canvas, evt, 'mousewheel');
                latestMouseMoveEvent = null;
                if (latestMouseWheelEvent != null) {
                    mousePos.mouse.wheelDelta += latestMouseWheelEvent.mouse.wheelDelta;
                }
                latestMouseWheelEvent = mousePos;
                evt.preventDefault();
                evt.stopPropagation();
                return false;
            }, false);
            util.bindEvent(canvas, 'contextmenu', function (event) {
                event.preventDefault();
                event.stopPropagation();
                return false;
            });

            util.bindEvent(input, 'keydown', keyDownHandler, false);
            util.bindEvent(canvas, 'keydown', keyDownHandler, false);

            function keyDownHandler(event) {
                var functionKeys = [9/*tab*/, 12/*Numpad5*/, 16/*Shift*/, 17/*ctrl*/, 18/*alt*/, 19/*pause*/, 20/*CapsLock*/, 27/*esc*/,
                    32/*space*/, 33/*pgup*/, 34/*pgdown*/, 35/*end*/, 36/*home*/, 37/*left*/, 38/*up*/, 39/*right*/, 40/*down*/, 44/*prtscr*/,
                    45/*insert*/, 46/*Delete*/, 91/*OSLeft*/, 92/*OSRight*/, 93/*Context*/, 145/*scrlck*/, 225/*altGraph(Linux)*/,
                    112/*F1*/, 113/*F2*/, 114/*F3*/, 115/*F4*/, 116/*F5*/, 117/*F6*/, 118/*F7*/, 119/*F8*/, 120/*F9*/,
                    121/*F10*/, 122/*F11*/, 123/*F12*/, 124/*F13*/, 125/*F14*/, 126/*F15*/, 127/*F16*/, 128/*F17*/, 129/*F18*/, 130/*F19*/, 131/*F20*/,
                    132/*F21*/, 133/*F22*/, 134/*F23*/, 135/*F24*/];

                var kc = event.keyCode;
                if (functionKeys.indexOf(kc) != -1) {
                    if (!api.cfg.virtualKB) {
                        event.preventDefault();
                        event.stopPropagation();
                    }
                }
                var keyevt = getKBKey('keydown', canvas, event);
                if (!isClipboardEvent(keyevt)) { // cut copy paste handled separately
                    // default action prevented
                    if (isCtrlCmd(keyevt) && !keyevt.key.alt) {
                        event.preventDefault();
                    }
                    if (latestKeyDownEvent != null) {
                        enqueueInputEvent();//so we dont lose the previous keydown
                    }
                    latestKeyDownEvent = keyevt;

                    //generate keypress event for alt+key events
                    if (keyevt.key.alt && functionKeys.indexOf(kc) == -1) {
                        event.preventDefault();
                        event.stopPropagation();
                        keyevt = getKBKey('keypress', canvas, event);
                        if (event.key.length == 1) {
                            keyevt.key.character = event.key.charCodeAt(0);
                        } else {
                            var key = keyevt.key.keycode;
                            keyevt.key.character = (!keyevt.key.shift && (key >= 65 && key <= 90)) ? key + 32 : key;
                        }
                        enqueueInputEvent(keyevt);
                    }
                }
                return false;
            }

            util.bindEvent(input, 'keypress', keyPressHandler, false);
            util.bindEvent(canvas, 'keypress', keyPressHandler, false);

            function keyPressHandler(event) {
                var keyevt = getKBKey('keypress', canvas, event);
                if (!isClipboardEvent(keyevt)) { // cut copy paste handled separately
                    event.preventDefault();
                    event.stopPropagation();
                    if (latestKeyDownEvent != null) {
                        latestKeyDownEvent.key.character = keyevt.key.character;
                    }
                    enqueueInputEvent(keyevt);
                }
                return false;
            }

            util.bindEvent(input, 'keyup', keyUpHandler, false);
            util.bindEvent(canvas, 'keyup', keyUpHandler, false);

            function keyUpHandler(event) {
                var keyevt = getKBKey('keyup', canvas, event);
                if (!isClipboardEvent(keyevt)) { // cut copy paste handled separately
                    event.preventDefault();
                    event.stopPropagation();
                    enqueueInputEvent(keyevt);
                    sendInput();
                }
                return false;
            }

            util.bindEvent(input, 'cut', function (event) {
                event.preventDefault();
                event.stopPropagation();
                api.cut(event);
                return false;
            }, false);
            util.bindEvent(input, 'copy', function (event) {
                event.preventDefault();
                event.stopPropagation();
                api.copy(event);
                return false;
            }, false);
            util.bindEvent(input, 'paste', function (event) {
                event.preventDefault();
                event.stopPropagation();
                api.paste(event, false);
                return false;
            }, false);
            util.bindEvent(document, 'mousedown', mouseDownEventHandler);
            util.bindEvent(document, 'mouseout', mouseOutEventHandler);
            util.bindEvent(document, 'mouseup', mouseUpEventHandler);

            registered = true;
        }

        function isClipboardEvent(evt) {
            var ctrlCmd = isCtrlCmd(evt);

            var isCutCopyKey = 'keypress' === evt.key.type ? (evt.key.character == 120 || evt.key.character == 24 || evt.key.character == 99) : (evt.key.character == 88 || evt.key.character == 67);
            var isCutCopyEvt = ctrlCmd && !evt.key.alt && !evt.key.altgr && !evt.key.shift && isCutCopyKey;

            var isPasteKey = 'keypress' === evt.key.type ? (evt.key.character == 118 || evt.key.character == 22 || evt.key.character == 86) : ( evt.key.character == 86);
            var isPasteEvt = ctrlCmd && !evt.key.alt && !evt.key.altgr && !evt.key.shift && isPasteKey;

            return isPasteEvt || isCutCopyEvt;
        }

        function isCtrlCmd(evt) {
            return api.cfg.isMac ? evt.key.meta : evt.key.ctrl;
        }

        function mouseDownEventHandler(evt) {
            mouseDown = mouseDown | Math.pow(2, evt.which);
        }

        function mouseOutEventHandler(evt) {
            mouseDown = 0;
        }

        function mouseUpEventHandler(evt) {
            mouseDown = mouseDown & ~Math.pow(2, evt.which);
        }

        function focusInput(input) {
            // In order to ensure that the browser will fire clipboard events, we always need to have something selected
            input.value = ' ';
            input.focus();
            input.select();
        }

        function getMousePos(canvas, evt, type) {
            var rect = canvas.getBoundingClientRect();
            // return relative mouse position
            var mouseX = Math.round(evt.clientX - rect.left);
            var mouseY = Math.round(evt.clientY - rect.top);
            var delta = 0;
            if (type == 'mousewheel') {
                delta = -Math.max(-1, Math.min(1, (evt.wheelDelta || -evt.detail)));
            }
            return {
                mouse: {
                    x: mouseX,
                    y: mouseY,
                    type: type,
                    wheelDelta: delta,
                    button: type == 'mousemove' ? 0 : evt.which,
                    buttons: mouseDown,
                    ctrl: evt.ctrlKey,
                    alt: evt.altKey,
                    shift: evt.shiftKey,
                    meta: evt.metaKey,
                    timeMilis: Date.now() % 86400000 //86400000 day in milis
                }
            };
        }

        function getKBKey(type, canvas, evt) {
            var char = evt.which;
            if (char == 0 && evt.key != null) {
                char = evt.key.charCodeAt(0);
            }
            var kk = evt.keyCode;
            if (kk == 0) {
                kk = char;
            }
            return {
                key: {
                    type: type,
                    character: char,
                    keycode: kk,
                    alt: evt.altKey,
                    ctrl: evt.ctrlKey,
                    shift: evt.shiftKey,
                    meta: evt.metaKey
                }
            };
        }

    };

});