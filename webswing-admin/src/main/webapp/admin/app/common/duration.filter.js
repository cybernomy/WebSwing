(function (define) {
    define([], function f() {
        function durationFilter() {
            return function (number) {
                var secondsTotal = number / 1000;
                var seconds = secondsTotal % 60;
                var minutes = ((secondsTotal - seconds) / 60) % 60;
                return minutes + ' min ' + Math.round(seconds) + ' sec';
            };
        }
        return durationFilter;
    });
})(adminConsole.define);
