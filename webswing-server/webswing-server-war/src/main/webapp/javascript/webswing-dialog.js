define(['jquery', 'text!templates/dialog.html'], function amdFactory($, html, cssBootstrap) {
    "use strict";

    return function DialogModule() {
        var module = this;
        var api;
        module.injects = api = {
            cfg: 'webswing.config',
            continueSession: 'base.continueSession',
            kill: 'base.kill',
            newSession: 'webswing.newSession',
            reTrySession: 'webswing.reTrySession',
            logout: 'login.logout',
            translate: 'translate.translate'
        };
        module.provides = {
            show: show,
            hide: hide,
            current: current,
            content: configuration(),
            currentBar: currentBar,
            showBar: showBar,
            hideBar: hideBar
        };

        var currentContent;
        var dialog, content, header, backdrop, spinnerTimer;

        var currentContentBar;
        var bar, barContent, barHeader;

        function configuration() {
            return {
                emptyMessage: messageDialog('dialog.emptyMessage', true),
                logingOut: messageDialog('dialog.logingOut'),
                readyDialog: messageDialog('dialog.readyDialog'),
                initializingDialog: messageDialog('dialog.initializingDialog', true),
                startingDialog: messageDialog('dialog.startingDialog', true),
                connectingDialog: messageDialog('dialog.connectingDialog', true),
                applicationBusyDialog: messageDialog('dialog.applicationBusyDialog', true),
                unauthorizedAccess: messageDialog('dialog.unauthorizedAccess'),
                applicationAlreadyRunning: retryMessageDialog('dialog.applicationAlreadyRunning'),
                sessionStolenNotification: retryMessageDialog('dialog.sessionStolenNotification'),
                disconnectedDialog: retryMessageDialog('dialog.disconnectedDialog'),
                connectionErrorDialog: retryMessageDialog('dialog.connectionErrorDialog'),
                tooManyClientsNotification: retryMessageDialog('dialog.tooManyClientsNotification'),
                stoppedDialog: finalMessageDialog('dialog.stoppedDialog'),
                timedoutDialog: finalMessageDialog('dialog.timedoutDialog'),
                cookiesDisabledDialog: messageDialog('dialog.cookiesDisabledDialog'),
                continueOldSessionDialog: {
                    header: '<span class="ws-message--neutral">${dialog.continueH} <span data-id="dialogHide" class="ws-icon-cancel-circled ws-right"></span></span>',
                    content: '<p data-id="continueMsg">${dialog.continueC1}<a href="javascript:void(0)" data-id="restartLink">${dialog.continueC2}</a></p>',
                    events: {
                        'restartLink_click':  function () {
                            api.kill();
                            api.newSession();
                        },
                        'dialogHide_click':hideBar,
                        'continueMsg_mouseenter': function(){
                            currentContentBar.focused=true;
                        },
                        'continueMsg_mouseleave': function(){
                            currentContentBar.focused=false;
                        }
                    }
                },
                longPollingWarningDialog: {
                    header: '<span class="ws-message--warning"><span class="ws-icon-warn"></span> ${dialog.longPollingH}</span>',
                    content: '<p>${dialog.longPollingC} </p>',
                    buttons: [{
                        label: '<span class="ws-icon-cancel-circled"></span>  ${dialog.longPollingB}',
                        action: function () {
                            hideBar();
                        }
                    }]
                },
                inactivityTimeoutWarningDialog: {
                    header: '<span class="ws-message--warning"><span class="ws-icon-warn"></span> ${dialog.inactivityTimeoutH}</span>',
                    content: '<p>${dialog.inactivityTimeoutC}  </p>',
                    buttons: [{
                        label: '<span class="ws-icon-cancel-circled"></span>  ${dialog.inactivityTimeoutB}',
                        action: function () {
                            hideBar();
                        }
                    }]
                }
            };
        }

        function messageDialog(msg, withSpinner) {
            var content = '<p>${' + msg + '}</p>';
            if (withSpinner) {
                content = '<div class="ws-spinner"><div class="ws-spinner-dot-1"></div> <div class="ws-spinner-dot-2"></div></div>' + content;
            }
            return {
                content: content
            };
        }

        function finalMessageDialog(msg) {
            return {
                content: '<p>${' + msg + '}</p>',
                buttons: [{
                    label: '<span class="ws-icon-certificate"></class> ${dialog.finalB1}',
                    action: function () {
                        api.newSession();
                    }
                }, {
                    label: '<span class="ws-icon-logout"></class> ${dialog.finalB2}',
                    action: function () {
                        api.logout();
                    }
                }]
            };
        }

        function retryMessageDialog(msg) {
            return {
                content: '<p>${' + msg + '}</p>',
                buttons: [{
                    label: '<span class="ws-icon-arrows-cw"></class> ${dialog.retryB1}',
                    action: function () {
                        api.reTrySession();
                    }
                }, {
                    label: '<span class="ws-icon-logout"></class> ${dialog.retryB2}',
                    action: function () {
                        api.logout();
                    }
                }]
            };
        }

        function setup() {
            api.cfg.rootElement.append(html);
            backdrop = api.cfg.rootElement.find('div[data-id="commonDialogBackDrop"]');
            dialog = api.cfg.rootElement.find('div[data-id="commonDialog"]');
            content = dialog.find('div[data-id="content"]');
            header = dialog.find('div[data-id="header"]');

            bar = api.cfg.rootElement.find('div[data-id="commonBar"]');
            barContent = bar.find('div[data-id="content"]');
            barHeader = bar.find('div[data-id="header"]');
            bar.hide();

            $(document).ajaxStart(function () {
                spinnerTimer = setTimeout(function () {
                    if (dialog.is(":visible")) {
                        $('#ajaxProgress').slideDown('fast');
                    }
                }, 200);
            }).ajaxComplete(function () {
                clearTimeout(spinnerTimer);
                $('#ajaxProgress').hide();
            });
        }

        function show(msg) {
            if (msg == null) {
                return;
            }
            if (dialog == null) {
                setup();
            }
            currentContent = msg;
            generateContent(msg, dialog, header, content);
            backdrop.show();
            dialog.slideDown('fast');
            return content;
        }

        function showBar(msg) {
            if (msg == null) {
                return;
            }
            if (bar == null) {
                setup();
            }
            currentContentBar = msg;
            generateContent(msg, bar, barHeader, barContent);
            bar.slideDown('fast');
            return barContent;
        }

        function generateContent(msg, dialog, header, content) {
            if (msg.header != null) {
                header.html(api.translate(msg.header));
                if (dialog.is(":visible")) {
                    header.fadeIn(200);
                } else {
                    header.show();
                }
            } else {
                header.hide();
                header.html('');
            }

            content.html(api.translate(msg.content));

            for (var e in msg.events) {
                var element = dialog.find('*[data-id="' + e.substring(0, e.lastIndexOf('_')) + '"]');
                element.bind(e.substring(e.lastIndexOf('_') + 1), msg.events[e]);
            }

            for (var b in msg.buttons) {
                var btn = msg.buttons[b];
                var button = $('<button class="ws-btn">' + api.translate(btn.label) + '</button><span> </span>');
                button.on('click', btn.action);
                content.append(button);
            }

            if (dialog.is(":visible")) {
                content.fadeIn('fast');
            }
        }


        function hide() {
            currentContent = null;
            content.html('');
            dialog.fadeOut('fast');
            backdrop.fadeOut('fast');
        }

        function hideBar() {
            currentContentBar = null;
            barContent.html('');
            bar.fadeOut('fast');
        }

        function current() {
            return currentContent;
        }

        function currentBar() {
            return currentContentBar;
        }

    };
});
