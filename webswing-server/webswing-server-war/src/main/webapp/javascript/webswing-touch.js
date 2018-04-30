define(['jquery', 'text!templates/touch.html', 'webswing-util', 'hammer'], function amdFactory($, html, util, Hammer) {
    "use strict";

    return function TouchModule() {
        var module = this;
        var api;
        module.injects = api = {
            cfg: 'webswing.config',
            send: 'socket.send',
            getInput: 'canvas.getInput',
            getCanvas: 'canvas.get'
        };
        module.provides = {
            register: register,
            dispose: dispose
        };
        module.ready = function () {
            util.preventGhosts(api.cfg.rootElement);// prevent ghost mouse events to be fired
        };

        var hammer;
        var touchBar;
        var compositionText = "";
        var composition = false;
        var registered = false;

        function register() {
            if (!registered) {
                var canvas = api.getCanvas();

                hammer = new Hammer(canvas, {
                    touchAction: 'manipulation'
                });

                hammer.get('tap').set({
                    threshold: 4
                });
                hammer.on('tap', function (ev) {
                    if (ev.pointerType === 'touch') {
                        var eventMsg = getTouchPos(canvas, ev, 1);
                        api.send(eventMsg);
                        display();
                        canvas.focus();
                    }

                });
                hammer.on('press', function (ev) {
                    if (ev.pointerType === 'touch') {
                        var eventMsg = getTouchPos(canvas, ev, 3);
                        api.send(eventMsg);
                    }
                });
                registered = true;
            }
        }

        function focusInput(input) {
            // In order to ensure that the browser will fire clipboard events,
            // we always need to have something selected
            input.value = ' ';
            input.focus();
            input.select();
        }

        function getTouchPos(canvas, evt, button) {
            var rect = canvas.getBoundingClientRect();
            // return relative mouse position
            var mouseX = Math.round(evt.center.x - rect.left);
            var mouseY = Math.round(evt.center.y - rect.top);

            return {
                events: [mouseEvent('mousedown'), mouseEvent('mouseup')]
            };

            function mouseEvent(type) {
                return {
                    mouse: {
                        x: mouseX,
                        y: mouseY,
                        type: type,
                        button: button,
                        buttons: button == 1 ? 2 : 8
                    }
                }
            }
        }

        function display() {
            if (touchBar != null) {
                close();
            }
            api.cfg.rootElement.append(html);
            touchBar = api.cfg.rootElement.find('div[data-id="touchBar"]');
            touchBar.find('button[data-id="kbdBtn"]').on('click', function (evt) {
                api.cfg.virtualKB = true;
                focusInput(api.getInput());
                close();
                $(api.getInput()).on('input compositionstart compositionupdate compositionend', processCompositionEvent);
                $(api.getInput()).on('blur', function (evt) {
                    api.cfg.virtualKB = false;
                    $(api.getInput()).off('compositionstart compositionupdate compositionend');
                });
            });
            touchBar.show("fast");
        }

        function processCompositionEvent(evt) {
            if (evt.type === 'compositionstart') {
                compositionText = evt.originalEvent.data;
                composition = true;
            }
            if (evt.type === 'compositionupdate' || evt.type === 'compositionend') {
                var newText = evt.originalEvent.data;
                if (newText.indexOf(compositionText) == 0) {
                    sendString(newText.substring(compositionText.length));
                } else {
                    sendBackspace(compositionText.length);
                    sendString(newText);
                }
                compositionText = newText;
            }
            if (evt.type === 'compositionend') {
                compositionText = "";
                setTimeout(function () {
                    evt.currentTarget.value = "";
                    composition = false;
                }, 0)
            }
            if (evt.type === 'input') {
                if (!composition) {
                    var newText = evt.originalEvent.target.value;
                    sendString(newText);
                    evt.currentTarget.value = "";
                }
            }
        }

        function close() {
            if (touchBar != null) {
                touchBar.hide("fast");
                touchBar.remove();
                touchBar = null;
            }
        }

        function sendString(s) {
            for (var i = 0, len = s.length; i < len; i++) {
                var char = 0;
                char = s.charCodeAt(i);
                api.send({
                    events: [keyEvent('keydown', char), keyEvent('keypress', char), keyEvent('keyup', char)]
                });
            }
        }

        function sendBackspace(no) {
            var evts = [];
            for (var i = 0; i < no; i++) {
                evts.push(keyEvent("keydown", 8, 8));
                evts.push(keyEvent("keyup", 8, 8));
            }
            api.send({
                events: evts
            });

        }

        function keyEvent(type, char, kkode) {
            return {
                key: {
                    type: type,
                    character: char,
                    keycode: kkode != null ? kkode : 0,
                    alt: false,
                    ctrl: false,
                    shift: false,
                    meta: false
                }
            };
        }

        function dispose() {
            close();
            hammer=null;
            compositionText='';
            composition = false;
            registered=false;
        }
    }
});