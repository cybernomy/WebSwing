(function(define) {
	define([], function f() {
		function SessionController(baseUrl, $scope, $timeout, $location, sessionsRestService, $routeParams, wsUtils) {
			var vm = this;
			vm.path = $routeParams.path;
			vm.sessionId = $routeParams.id;
			vm.session = {};
			vm.lastUpdated = null;
			vm.back = back;
			vm.record = record;
			vm.refresh = refresh;
			vm.kill = kill;
			vm.forceKill = forceKill;
			vm.timer = undefined;
			vm.control = false;
			vm.showThreadDump = showThreadDump;

			load().then(view).then(refresh);

			$scope.$watch('vm.control', function(val) {
				if (window.webswingadmin != null && window.webswingadmin.webswingmirrorview != null && val != null) {
					window.webswingadmin.webswingmirrorview.setControl(val);
				}
			});

			$scope.$on('$destroy', function() {
				$timeout.cancel(vm.timer);
				window.webswingadmin.webswingmirrorview.disconnect();
			});

			function load() {
				return sessionsRestService.getSession(vm.path, vm.sessionId).then(function(data) {
					$timeout.cancel(vm.timer);
					vm.session = data;
					vm.memoryStats = getMemoryStats(data);
					vm.cpuStats = getCpuStats(data);
					vm.bandwidthStats = getBandwidthStats(data);
					vm.latencyStats = getLatencyStats(data);
					vm.lastUpdated = new Date();
				});
			}

			function refresh() {
				return load().then(function() {
					vm.timer = $timeout(refresh, 2000);
					return vm.timer;
				}, function() {
					$timeout.cancel(vm.timer);
					vm.timer = undefined;
				});
			}

			function view() {
				var config = {
					autoStart : false,
					connectionUrl : "/"+vm.path,
					clientId : vm.session.id,
					applicationName : vm.session.application,
					control : vm.control,
					mirrorMode : true
				};
				window.webswingadmin.scan();
				window.webswingadmin.webswingmirrorview.disconnect();
				window.webswingadmin.webswingmirrorview.configure(config);
				window.webswingadmin.webswingmirrorview.start();
			}

			function getMemoryStats(session) {
				return {
					names : [ 'Max Memory', 'Used Memory' ],
					keys : [ 'memoryAllocated', 'memoryUsed' ],
					dataset : wsUtils.getStatsDataset(session.stats, [ 'memoryAllocated', 'memoryUsed' ]),
					tickFormat : function(value, index) {
						if(value>999){
							return (value/1024).toFixed(1) + 'GB';
						}else{
							return (value) + 'MB';
						}
					}
				};
			}
			
			function getCpuStats(session) {
				return {
					names : [ 'CPU Utilization' ],
					keys : [ 'cpuUtilization' ],
					dataset : wsUtils.getStatsDataset(session.stats, [ 'cpuUtilization' ]),
					tickFormat : function(value, index) {
						return Math.floor(value) + '%';
					}
				};
			}

			function getBandwidthStats(session) {
				return {
					names : [ 'Inbound trafic', 'Outbound trafic' ],
					keys : [ 'inboundSize', 'outboundSize' ],
					dataset : wsUtils.getStatsDataset(session.stats, [ 'inboundSize', 'outboundSize' ]),
					tickFormat : function(value, index) {
						return Math.floor(value / 1024) + 'k';
					}
				};
			}

			function getLatencyStats(session) {
				return {
					names : [ 'Network ping latency','Network transfer latency', 'Server rendering latency', 'Client rendering latency' ],
					keys : [ 'latencyPing', 'latencyNetworkTransfer', 'latencyServerRendering', 'latencyClientRendering' ],
					dataset : wsUtils.getStatsDataset(session.stats, ['latencyPing', 'latencyNetworkTransfer', 'latencyServerRendering', 'latencyClientRendering' ]),
					tickFormat : function(value, index) {
						return value + 'ms';
					}
				};
			}

			function kill() {
				return sessionsRestService.killSession(vm.path, vm.sessionId).then(function() {
					back();
				});
			}

			function forceKill() {
				return sessionsRestService.forceKillSession(vm.path, vm.sessionId).then(function() {
					back();
				});
			}

			function record(){
                return sessionsRestService.recordSession(vm.path, vm.sessionId).then(function() {
                    refresh();
                });
            }

			function back() {
				$location.path('/dashboard/overview/' + vm.path);
				$timeout.cancel(vm.timer);
			}

			function showThreadDump(session, key) {
                window.open(sessionsRestService.getStackDumpPath(vm.path, session.id, key), "_blank");
            }
		}
		SessionController.$inject = [ 'baseUrl', '$scope', '$timeout', '$location', 'sessionsRestService', '$routeParams', 'wsUtils' ];

		return SessionController;
	});
})(adminConsole.define);