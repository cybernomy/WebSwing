(function (define) {
    define([], function f() {
        function PlaybackController(baseUrl, $scope, $location, $routeParams) {
            var vm = this;
            vm.back = back;
            view();

            $scope.$on('$destroy', function () {
                window.webswingadmin.webswingplayback.disconnect();
            });

            function view() {
                var config = {
                    autoStart: false,
                    connectionUrl: baseUrl,
                    applicationName: $routeParams.playback,
                    recordingPlayback: $routeParams.playback,
                    control: false,
                    mirrorMode: true
                };
                window.webswingadmin.scan();
                window.webswingadmin.webswingplayback.disconnect();
                window.webswingadmin.webswingplayback.configure(config);
                window.webswingadmin.webswingplayback.start();
            }

            function back() {
                $location.path('/dashboard/overview');
            }
        }
        PlaybackController.$inject = ['baseUrl', '$scope', '$location', '$routeParams'];

        return PlaybackController;
    });
})(adminConsole.define);