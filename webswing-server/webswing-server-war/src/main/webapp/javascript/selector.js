(function (root) {
    require.config({
        baseUrl: 'javascript',
        map: {
            '*': {
                'jquery': 'jquery-private'
            },
            'jquery-private': {
                'jquery': 'jquery'
            }
        }
    });

    function getParam(name) {
        name = name.replace(/[\[]/, "\\\[").replace(/[\]]/, "\\\]");
        var results = new RegExp("[\\?&]" + name + "=([^&#]*)").exec(location.href);
        return results == null ? null : decodeURIComponent(results[1]);
    }

    try{
        var storedLang = localStorage.getItem("webswingLang") ;
    }catch (e){
        console.log(e);
    }
    var lang= storedLang || (navigator.browserLanguage || navigator.language);

    require({locale: lang}, ['jquery', 'webswing-util', 'webswing-translate'], function ($, util, Translate) {
        var translateModule = new Translate();
        translateModule.ready();
        var translate = translateModule.provides.translate;

        if(!util.checkCookie()){
            $('#webswing-content').html(translate('${dialog.cookiesDisabledDialog}'));
            return;
        }

        var login = util.webswingLogin;
        var user;
        var loginData = {
            securityToken: getParam("token"),
            realm: getParam("realm")
        };
        var url = window.location.href;
        var baseUrl = url.substring(0, Math.min(url.indexOf('#'), url.indexOf('?')));
        login(baseUrl, $('#webswing-content'), loginData, loadApps);


        function loadApps(data, request) {
            user = request.getResponseHeader('webswingUsername');
            $.ajax({
                xhrFields: {
                    withCredentials: true
                },
                type: 'GET',
                url: 'apps',
                success: function (data, textStatus, request) {
                    loadPermissions(function (showAdmin) {
                        show(JSON.parse(data), showAdmin);
                    });
                },
                error: function (data) {
                    if (failCallback != null) {
                        failCallback();
                    }
                }
            });
        }

        function loadPermissions(callback) {
            $.ajax({
                xhrFields: {
                    withCredentials: true
                },
                type: 'GET',
                url: 'rest/permissions',
                success: function (data, textStatus, request) {
                    callback(data && JSON.parse(data).dashboard);
                },
                error: function (data) {
                    callback(false);
                }
            });
        }


        function show(apps, showAdmin) {
            var header = '<h1 class="ws-selector-title">${selector.welcome} <span>' + user + '</span>${selector.message}</h1>';
            var links = (showAdmin ? '${selector.lang}  <a href="admin" id="admin">${selector.admin}</a> | ' : '') + '<a href="logout" id="logout">${selector.logout}</a>';
            var content;
            $('#commonDialog').addClass('ws-selector')
            if (apps == null || apps.length === 0) {
                header = null;
                content = '<p>${selector.noApp}</p>';
            } else {
                content = '<div class="ws-selector-container">';
                for (var i in apps) {
                    var app = apps[i];
                    content += '<div class="ws-selector-btn"><div onclick="window.location.href = \''
                        + app.url
                        + '\';"><img src="' + util.getImageString(app.base64Icon) + '" class="ws-selector-btn-thumb"/><div class="ws-selector-btn-label">' + app.name + '</div></div></div>';
                }
                content += '</div>';
            }
            $('#webswing-header').html(translate(header));
            $('#webswing-links').html(translate(links));
            $('#webswing-content').html(translate(content));
        }

    }, function (err) {
        var failedId = err.requireModules && err.requireModules[0];
        console.error(err);
        throw Error("Error while starting webswing in module '" + failedId + "'. Reason: " + err);
    });
})(this);