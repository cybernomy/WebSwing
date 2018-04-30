define(['jquery','webswing-util'], function amdFactory($, util) {
    "use strict";
    return function LoginModule() {
        var module = this;
        var api;
        module.injects = api = {
            cfg: 'webswing.config',
            start: 'webswing.start',
            disconnect: 'webswing.disconnect',
            showDialog: 'dialog.show',
            logingOut: 'dialog.content.logingOut',
            emptyMessage: 'dialog.content.emptyMessage'
        };
        module.provides = {
            login: login,
            logout: logout,
            touchSession: touchSession,
            user: getUser
        };

        var user;

        function login(successCallback) {
            var loginData = {
                securityToken: api.cfg.securityToken,
                realm: api.cfg.realm,
                successUrl: window.top.location.href
            };
            var dialogContent = function () {
                return api.showDialog(api.emptyMessage);
            }
            util.webswingLogin(api.cfg.connectionUrl, dialogContent, loginData, function (data, request) {
                user = request.getResponseHeader('webswingUsername');
                if (successCallback != null) {
                    successCallback();
                }
            });
        }

        function logout() {
            var dialogContent = api.showDialog(api.logingOut);
            util.webswingLogout(api.cfg.connectionUrl, dialogContent, function done(data) {
                api.disconnect();
                api.start();
            });
        }



        function touchSession() {
            $.ajax({
                url: api.cfg.connectionUrl + 'login',
            });
        }

        function getUser() {
            return user;
        }
    };
});