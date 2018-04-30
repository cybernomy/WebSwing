(function (define) {
    define([], function f() {
        function substitutorFilter() {
            return function (string, object) {
                if (string != null && string.indexOf('${') > -1) {
                    for (var i = 0; i < 3; i++) {
                        var res = string;
                        for (key in object) {
                            res = replaceAll(res, '${' + key + '}', object[key]);
                        }
                        if (res === string) {
                            return res;
                        } else {
                            string = res;
                        }
                    }
                }
                return string;
            };
        }
        function escapeRegExp(string) {
            return string.replace(/([.*+?^=!:${}()|\[\]\/\\])/g, "\\$1");
        }
        function replaceAll(string, find, replace) {
            return string.replace(new RegExp(escapeRegExp(find), 'g'), replace);
        }
        return substitutorFilter;
    });
})(adminConsole.define);
