define(['jquery', 'webswing-util', 'webswing-polyfill', 'webswing-base', 'webswing-socket', 'webswing-files', 'webswing-dialog',
        'webswing-login', 'webswing-canvas', 'webswing-identity', 'webswing-jslink', 'webswing-clipboard', 'webswing-playback', 'webswing-input', 'webswing-touch', 'webswing-inject', 'webswing-translate','webswing-ping'],
    function f($, util, polyfill, Base, Socket, Files, Dialog, Login, Canvas, Identity, JsLink, Clipboard, Playback, Input, Touch, Injector, Translate,Ping) {
        "use strict";

        var globalName = $('[data-webswing-global-var]');
        var global = {};
        var typedArraysSupported = polyfill();

        if (globalName != null && globalName.length !== 0) {
            var name = globalName.data('webswingGlobalVar');
            global = window[name] = {
                scan: scanForInstances,
                bootstrap: bootstrap,
                utils: util,
                $: $
            };
            scanForInstances(window);
        } else {
            scanForInstances(window);
        }

        function scanForInstances(root) {
            root = root != null ? root : global;
            var result = {};
            var instances = $('[data-webswing-instance]');
            instances.each(function (index, instance) {
                var id = $(instance).data('webswingInstance');
                var active = $(instance).data('webswingActive');
                if (!active) {
                    var customization;
                    if (root[id] != null && root[id].options != null) {
                        $(instance).data("webswingOptions", root[id].options);
                        if (root[id].options.customization != null && typeof root[id].options.customization === 'function') {
                            customization = root[id].options.customization;
                        }
                    }
                    $(instance).find("#loading").remove();
                    var wsInstance = bootstrap($(instance), customization);
                    $(instance).attr('data-webswing-active', 'true');
                    if (id != null) {
                        result[id] = wsInstance;
                    }
                }
            });
            for (var exportName in result) {
                root[exportName] = $.extend(root[exportName], result[exportName]);
            }
        }

        function bootstrap(element, customization) {
            var injector = new Injector();
            injector.module('webswing', new WebswingInstance(element));
            injector.module('dialog', new Dialog());
            injector.module('canvas', new Canvas());
            injector.module('base', new Base());
            injector.module('input', new Input());
            injector.module('touch', new Touch());
            injector.module('socket', new Socket());
            injector.module('files', new Files());
            injector.module('login', new Login());
            injector.module('identity', new Identity());
            injector.module('jslink', new JsLink());
            injector.module('clipboard', new Clipboard());
            injector.module('playback', new Playback());
            injector.module('translate', new Translate());
            injector.module('ping', new Ping());
            var externalObj = {
                start: 'webswing.start',
                disconnect: 'webswing.disconnect',
                configure: 'webswing.configure',
                kill: 'base.kill',
                setControl: 'webswing.setControl',
                repaint: 'base.repaint'
            };
            injector.module('external', {
                provides: externalObj,
                injects: externalObj
            });

            if (customization != null) {
                customization(injector);
            }
            injector.injectAndVerify();

            return externalObj;
        }

        function WebswingInstance(rootElement) {
            var module = this;
            var api;
            module.injects = api = {
                cfg: 'webswing.config',
                start: 'webswing.start',
                disconnect: 'webswing.disconnect',
                login: 'login.login',
                connect: 'socket.connect',
                showDialog: 'dialog.show',
                readyDialog: 'dialog.content.readyDialog',
                initializingDialog: 'dialog.content.initializingDialog',
                cookiesDisabledDialog: 'dialog.content.cookiesDisabledDialog',
                disposeIdentity: 'identity.dispose',
                disposeBase: 'base.dispose',
                disposeCanvas: 'canvas.dispose',
                disposeTouch: 'touch.dispose',
                disposeInput: 'input.dispose',
                disposeSocket: 'socket.dispose',
                disposeFileDialog: 'files.close',
                disposeCopyBar: 'clipboard.dispose',
                showPlaybackControls: 'playback.showControls'
            };
            module.provides = {
                config: defaultCtxConfig(),
                start: start,
                newSession: newSession,
                reTrySession: reTrySession,
                disconnect: disconnect,
                setControl: setControl,
                configure: configure
            };
            module.ready = function () {
                configure();
                if (api.cfg.autoStart) {
                    api.start();
                } else {
                    api.showDialog(api.readyDialog);
                }
            };

            function defaultCtxConfig() {
                return {
                    rootElement: setupRootElement(rootElement),
                    autoStart: false,
                    applicationName: null,
                    args: null,
                    connectionUrl: location.origin + location.pathname,
                    mirror: false,
                    recordingPlayback: false,
                    typedArraysSupported: typedArraysSupported,
                    binarySocket: true,
                    recording: false,
                    debugPort: null,
                    javaCallTimeout: 3000,
                    documentBase: document.location.origin + document.location.pathname,
                    ieVersion: util.detectIE(),
                    isMac: util.detectMac(),
                    /* webswing instance context */
                    clientId: null,
                    viewId: null,
                    appName: null,
                    hasControl: false,
                    mirrorMode: false,
                    canPaint: false,
                    virtualKB: false,
                    debugLog: false
                };
            }

            function setupRootElement(rootElement) {
                return rootElement.addClass('webswing-element');

            }

            function start() {
                if(!util.checkCookie()){
                    api.showDialog(api.cookiesDisabledDialog)
                    return;
                }
                api.login(function () {
                    api.showDialog(api.initializingDialog);
                    api.connect();
                });
            }

            function newSession() {
                api.disconnect();
                api.disposeIdentity();
                api.start();
            }

            function reTrySession() {
                api.disconnect();
                api.start();
            }

            function disconnect() {
                api.disposeFileDialog();
                api.disposeBase();
                api.disposeInput();
                api.disposeTouch();
                api.disposeCanvas();
                api.disposeSocket();
                api.disposeCopyBar();
            }

            function configure(options, appletParams) {
                var cfg = api.cfg;
                options = options != null ? options : readOptions(cfg.rootElement);
                if (options != null) {
                    cfg.autoStart = options.autoStart != null ? JSON.parse(options.autoStart) : cfg.autoStart;
                    cfg.applicationName = options.applicationName != null ? options.applicationName : cfg.applicationName;
                    cfg.securityToken = options.securityToken != null ? options.securityToken : cfg.securityToken;
                    cfg.realm = options.realm != null ? options.realm : cfg.realm;
                    cfg.args = options.args != null ? options.args : cfg.args;
                    cfg.binarySocket = options.binarySocket != null ? JSON.parse(options.binarySocket) : cfg.binarySocket;
                    cfg.recording = options.recording != null ? JSON.parse(options.recording) : cfg.recording;
                    cfg.clientId = options.clientId != null ? options.clientId : cfg.clientId;
                    cfg.mirror = options.mirrorMode != null ? JSON.parse(options.mirrorMode) : cfg.mirror;
                    cfg.connectionUrl = options.connectionUrl != null ? options.connectionUrl : cfg.connectionUrl;
                    cfg.debugPort = options.debugPort != null ? options.debugPort : cfg.debugPort;
                    cfg.debugLog = options.debugLog != null ? JSON.parse(options.debugLog) : cfg.debugLog;
                    cfg.javaCallTimeout = options.javaCallTimeout != null ? parseInt(options.javaCallTimeout, 10) : cfg.javaCallTimeout;
                    if (cfg.connectionUrl.substr(cfg.connectionUrl.length - 1) !== '/') {
                        cfg.connectionUrl = cfg.connectionUrl + '/';
                    }
                    if (options.recordingPlayback != null) {
                        cfg.recordingPlayback = cfg.applicationName = options.recordingPlayback;
                        api.showPlaybackControls();
                    }
                }
                appletParams = appletParams != null ? appletParams : readAppletParams(cfg.rootElement);
                if (appletParams != null) {
                    cfg.params = [];
                    for (var prop in appletParams) {
                        if (typeof appletParams[prop] === 'string') {
                            cfg.params.push({
                                name: prop,
                                value: appletParams[prop]
                            });
                        }
                    }
                }
            }

            function setControl(value) {
                api.cfg.hasControl = value ? true : false;
            }

            function readOptions(element) {
                var options = element.data("webswingOptions");
                return options;
            }

            function readAppletParams(element) {
                var result = {};
                var params = $(element).find('webswing-param');
                for (var i = 0; i < params.length; i++) {
                    var name = params[i].getAttribute('name');
                    var val = params[i].getAttribute('value');
                    if (name != null) {
                        result[name] = val;
                    }
                }
                return result;
            }
        }
    });
