(function (define) {
    define([], function f() {
        function OverviewController($scope, $timeout, $location, sessionsRestService, $routeParams, wsUtils) {
            var vm = this;
            vm.path = $routeParams.path;
            vm.sessions = [];
            vm.closedSessions = [];
            vm.view = view;
            vm.record = record;
            vm.lastUpdated = null;
            vm.refresh = refresh;
            vm.timer = undefined;
            vm.play = play;
            vm.back = back;
            vm.kill = kill;
            vm.toConfig = toConfig;
            vm.sortExp = 'startedAt';
            vm.sortReverse = true;
            vm.sortBy = sortBy;
            vm.sortByMetrics = sortByMetrics;
            vm.sortByBandwidth = sortByBandwidth;
            vm.sortByLatency = sortByLatency;
            vm.sortFinishedExp = 'endedAt';
            vm.sortFinishedReverse = true;
            vm.sortFinishedBy = sortFinishedBy;
            vm.getOsIcon = getOsIcon;
            vm.getBrowserIcon = getBrowserIcon;
            vm.showThreadDump = showThreadDump;
            vm.requestThreadDump = requestThreadDump;
            refresh();

            $scope.$on('$destroy', function () {
                $timeout.cancel(vm.timer);
            });

            $scope.$watch('vm.sessions', function (value) {
                if (value != null) {
                    for (var i = 0; i < value.length; i++) {
                        var session = value[i];
                        session.gauge = {};
                        session.gauge.cpu = wsUtils.getGaugeData(session.metrics, 'Cpu', '%', 'cpuUtilization', null, 100, 1);
                    }
                }
            });

            function refresh() {
                return sessionsRestService.getSessions(vm.path).then(function (data) {
                    $timeout.cancel(vm.timer);
                    vm.sessions = data.sessions;
                    vm.closedSessions = data.closedSessions;
                    vm.lastUpdated = new Date();
                }).then(function () {
                    vm.timer = $timeout(refresh, 2000);
                    return vm.timer;
                }, function () {
                    $timeout.cancel(vm.timer);
                    vm.timer = undefined;
                });
            }

            function sortBy(exp) {
                if (vm.sortExp === exp) {
                    vm.sortReverse = !vm.sortReverse;
                } else {
                    vm.sortExp = exp;
                    vm.sortReverse = false;
                }
            }

            function sortByMetrics(aggreg) {
                var exp = 'metrics.memoryUsed';
                if (aggreg != null) {
                    exp = "metrics['memoryUsed." + aggreg + "']";
                }
                sortBy(exp);
            }

            function sortByLatency(aggreg) {
                var exp = 'metrics.latency';
                if (aggreg != null) {
                    exp = "metrics['latency." + aggreg + "']";
                }
                sortBy(exp);
            }

            function sortByBandwidth(aggreg) {
                var exp = 'metrics.inboundSize + metrics.outboundSize';
                if (aggreg != null) {
                    exp = "metrics['inboundSize." + aggreg + "'] + metrics['outboundSize." + aggreg + "']";
                }
                sortBy(exp);
            }

            function sortFinishedBy(exp) {
                if (vm.sortFinishedExp === exp) {
                    vm.sortFinishedReverse = !vm.sortFinishedReverse;
                } else {
                    vm.sortFinishedExp = exp;
                    vm.sortFinishedReverse = false;
                }
            }

            function getOsIcon(os) {
                if (os != null) {
                    if (os === 'Windows') {
                        return 'wsa-icon-windows';
                    }
                    if (os === 'Mac' || os === 'IPhone') {
                        return 'wsa-icon-apple';
                    }
                    if (os === 'Linux') {
                        return 'wsa-icon-linux';
                    }
                    if (os === 'Android') {
                        return 'wsa-icon-android';
                    }
                    return 'wsa-icon-unknown';
                }
            }

            function getBrowserIcon(b) {
                if (b != null) {
                    if (b.indexOf('IE') >= 0) {
                        return 'wsa-icon-ie';
                    }
                    if (b.indexOf('Safari') >= 0) {
                        return 'wsa-icon-safari';
                    }
                    if (b.indexOf('Opera') >= 0) {
                        return 'wsa-icon-opera';
                    }
                    if (b.indexOf('Chrome') >= 0) {
                        return 'wsa-icon-chrome';
                    }
                    if (b.indexOf('Firefox') >= 0) {
                        return 'wsa-icon-firefox';
                    }
                    return 'wsa-icon-unknown';
                }
            }

            function kill(session) {
                return sessionsRestService.killSession(vm.path, session.id).then(function () {
                    refresh();
                });
            }

            function view(session) {
                $location.search('id', session.id);
                $location.path('/dashboard/session/' + vm.path);
            }

            function record(session) {
                return sessionsRestService.recordSession(vm.path, session.id).then(function () {
                    refresh();
                });
            }

            function play(session) {
                $location.url('/dashboard/playback?playback=' + session.recordingFile);
            }

            function back() {
                $location.path('/dashboard/single/' + vm.path);
                $timeout.cancel(vm.timer);
            }

            function toConfig() {
                $location.path('/config/swing/' + vm.path);
                $timeout.cancel(vm.timer);
            }

            function showThreadDump(session, key) {
                window.open(sessionsRestService.getStackDumpPath(vm.path, session.id, key), "_blank");
            }

            function requestThreadDump(session){
                sessionsRestService.requestThreadDump(vm.path, session.id);
            }
        }

        OverviewController.$inject = ['$scope', '$timeout', '$location', 'sessionsRestService', '$routeParams', 'wsUtils'];

        return OverviewController;
    });
})(adminConsole.define);