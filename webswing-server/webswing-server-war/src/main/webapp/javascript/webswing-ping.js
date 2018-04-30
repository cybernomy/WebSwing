define(['jquery', 'text!templates/network.html', 'webswing-util',], function amdFactory($, html, util) {
    "use strict";
    return function PingModule() {
        var module = this;
        var api;
        var worker, ping, networkBar, severity, mute = 0;
        var count = 6, interval = 5, maxLatency = 500, notifyIf = 3;
        module.injects = api = {
            cfg: 'webswing.config',
            translate: 'translate.translate',
            sendInput: 'input.sendInput'
        };
        module.provides = {};
        module.ready = function () {
            ping = getArrayWithLimitedLength(count);
            var connectionUrl=api.cfg.connectionUrl;
            if (connectionUrl.indexOf('http') != 0) {
                var host = window.location.protocol + "//" + window.location.hostname + (window.location.port ? ':' + window.location.port : '')
                connectionUrl = host + connectionUrl;
            }
            var blobURL = URL.createObjectURL(new Blob(['(', PingMonitor.toString(), ')("' + connectionUrl + '",' + interval + ')'], {type: 'application/javascript'}));
            worker = new Worker(blobURL);
            worker.onmessage = function (e) {
                var p = JSON.parse(e.data);
                ping.push(p);
                if (p.result === 'ok') {
                    api.sendInput({
                        timestamps: {
                            ping: p.latency
                        }
                    });
                    //first success after offline status - clear pings
                    if (severity == 2) {
                        ping = getArrayWithLimitedLength(count);
                    }
                }
                evaluateNetworkStatus();
            };
        };

        function evaluateNetworkStatus() {
            var msg = null;
            var currentSeverity = 0;
            var warns = 0;
            var fails = 0;
            for (var i in ping) {
                var p = ping[i];
                if ("error" === p.result || "timeout" === p.result) {
                    warns++;
                    fails++;
                } else if (p.latency > maxLatency) {
                    warns++;
                }
            }
            if (fails == count) {
                msg = '<span class="ws-message--error"><span class="ws-icon-warn"></span>${dialog.networkMonitor.offline}</span>';
                severity = 2;
            } else if (warns >= notifyIf) {
                msg = '<span class="ws-message--warning"><span class="ws-icon-warn"></span>${dialog.networkMonitor.warn}</span>';
                severity = 1;
            } else {
                severity = 0;
            }

            if (msg != null && severity != mute) {
                display(api.translate(msg));
            } else {
                close();
            }
        }


        function display(msg) {
            if (networkBar == null) {
                api.cfg.rootElement.append(api.translate(html));
                networkBar = api.cfg.rootElement.find('div[data-id="networkBar"]');
                networkBar.find('a[data-id="hide"]').on('click', function (evt) {
                    mute = severity;
                    close();
                });
            }
            networkBar.find('span[data-id="message"]').html(msg);
            networkBar.show("fast");
        }

        function close() {
            if (networkBar != null) {
                networkBar.hide("fast");
                networkBar.remove();
                networkBar = null;
            }
        }

        function PingMonitor(connectionUrl, is) {
            var interval = is * 1000;

            setInterval(ping, interval);

            function ping(success, failure) {
                var startTime = new Date().getTime();
                var oReq = new XMLHttpRequest();
                oReq.addEventListener("load", function (e) {
                    result("ok", new Date().getTime() - startTime);
                });
                oReq.addEventListener("error", function (e) {
                    result("error", new Date().getTime() - startTime);
                });
                oReq.addEventListener("timeout", function (e) {
                    result("timeout", new Date().getTime() - startTime);
                });
                oReq.open("GET", connectionUrl + "rest/ping");
                oReq.timeout = interval;
                oReq.send();
            }

            function result(status, ms) {
                postMessage(JSON.stringify({result: status, latency: ms}));
            }
        }


        function getArrayWithLimitedLength(length) {
            var array = new Array();
            array.push = function () {
                if (this.length >= length) {
                    this.shift();
                }
                return Array.prototype.push.apply(this, arguments);
            }
            return array;
        }

    };
});