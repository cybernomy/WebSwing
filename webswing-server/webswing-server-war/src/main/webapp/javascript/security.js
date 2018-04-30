(function (root) {
    var baseUrl = root.webswingRequestBaseUrl ? (root.webswingRequestBaseUrl + '/') : '';
    require.config({
        baseUrl: baseUrl + 'javascript',
        map: {
            '*': {
                'jquery': 'jquery-private'
            },
            'jquery-private': {
                'jquery': 'jquery'
            }
        }
    });

    try{
        var storedLang = localStorage.getItem("webswingLang") ;
    }catch (e){
        console.log(e);
    }
    var lang= storedLang || (navigator.browserLanguage || navigator.language);

    require({locale: lang}, ['jquery', 'webswing-translate'], function ($, Translate) {
        var translateModule = new Translate();
        translateModule.ready();
        var translate = translateModule.provides.translate;

        showPartial(window.location.href, $('#webswing-content'), root.webswingPartialHtml);

        function showPartial(url, element, html) {
            element.html(translate(html));
            var form = element.find('form').first();
            if (form != null) {
                form.submit(function (event) {
                    loadPartial(url, element, form.serialize());
                    event.preventDefault();
                });
            }
        }

        function loadPartial(url, element, data) {
            if (typeof element === 'function') {
                element = element();
            }
            $.ajax({
                xhrFields: {
                    withCredentials: true
                },
                type: 'POST',
                url: url,
                contentType: 'application/x-www-form-urlencoded; charset=UTF-8',
                data: data,
                complete: function (xhr) {
                    var response = xhr.responseText;
                    var loginMsg = {};
                    if (response != null) {
                        try {
                            loginMsg = JSON.parse(response);
                        } catch (error) {
                            if (baseUrl) {
                                window.top.location.href = baseUrl;
                                return;
                            } else {
                                element.html(translate("<p>Invalid request</p>"))
                            }
                        }
                        if (loginMsg.redirectUrl != null) {
                            window.top.location.href = loginMsg.redirectUrl;
                        } else if (loginMsg.partialHtml != null) {
                            showPartial(url, element, loginMsg.partialHtml);
                        } else {
                            element.html(translate("<p>${login.unexpectedError}</p>"));
                        }
                    } else {
                        element.html(translate("<p>${login.serverNotAvailable}</p>"));
                    }
                }
            });
        }

    }, function (err) {
        var failedId = err.requireModules && err.requireModules[0];
        console.error(err);
        throw Error("Error while starting webswing in module '" + failedId + "'. Reason: " + err);
    });
})(this);