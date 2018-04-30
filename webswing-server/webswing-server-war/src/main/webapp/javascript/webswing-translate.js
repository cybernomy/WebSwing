define(['optional-i18n!nls/msg', 'optional-i18n!nls/select'], function amdFactory(msg, langs) {
    "use strict";
    return function TranslateModule() {
        var module = this;
        var api;
        module.injects = api = {};
        module.provides = {
            translate: translate,
            getLocale: getLocale
        };
        module.ready = function () {
            if (langs != null && Object.keys(langs).length > 1) {
                var locale = getLocale();
                var onchange = "localStorage.setItem('webswingLang',this.options.item(this.selectedIndex).value);location.reload();"
                var selector = '<select class="webswing-lang-selector" onchange="' + onchange + '">';
                for (var key in langs) {
                    var selected = locale == key ? 'selected' : '';
                    selector += '<option ' + selected + ' value="' + key + '">' + langs[key] + '</option>';
                }
                selector += '</select>'
                msg['selector.lang'] = '${selector.lang.label}' + selector;
            } else {
                msg['selector.lang'] = '';
            }
        }

        function getLocale() {
            try{
                var storedLang = localStorage.getItem("webswingLang") ;
            }catch (e){
                console.log(e);
            }
            var lang= storedLang || (navigator.browserLanguage || navigator.language);
            return lang;
        }

        function translate(key) {
            var result = key;
            return replace(result, msg);
        }

        function replace(string, object) {
            if (string != null && string.indexOf('${') > -1) {
                for (var i = 0; i < 3; i++) {
                    var res = string;
                    for (var key in object) {
                        res = replaceAll(res, '${' + key + '}', object[key]);
                    }
                    if (res === string) {
                        return res;
                    } else {
                        string = res;
                    }
                }
            } else if (string != null && object[string.trim()] != null) {
                return object[string];
            }
            return string;
        }

        function escapeRegExp(string) {
            return string.replace(/([.*+?^=!:${}()|\[\]\/\\])/g, "\\$1");
        }

        function replaceAll(string, find, replace) {
            return string.replace(new RegExp(escapeRegExp(find), 'g'), replace);
        }
    };
});