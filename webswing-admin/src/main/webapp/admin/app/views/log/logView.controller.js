(function(define) {
	define([], function f() {
		function LogViewController($scope, $timeout, $location, $routeParams, $anchorScroll, logRestService, baseUrl) {
			var vm = this;
			vm.path = $routeParams.path;
			vm.types = [ {
				label : 'Audit',
				url : 'audit',
				delimiter : '|',
				lineDelimiter : '\n',
				headers : [ 'Timestamp', 'Result', 'User', 'Detail', 'Secured path', 'Protocol', 'Source IP', 'Module' ]
			}, {
				label : 'Server',
				url : 'server',
				delimiter : null,
				lineDelimiter : /\n(?=\d{4})/g,
				headers : [ 'Messges' ]
			} ];
			vm.showLog = showLog;
			vm.isActive = isActive;
			vm.split = split;
			vm.timer = undefined;
			vm.filter = '';
			vm.endOffset = -1;
			vm.log = [];
			vm.download = download;

			vm.type = findType();
			load(100 * 1024, -1, true).then(function() {
				$timeout(function() {
					$location.hash('endOfPageAnchor');
					$anchorScroll();
				}, 100);
			}).then(loadDelta);

			$scope.$on('$destroy', function() {
				$timeout.cancel(vm.timer);
			});

			function loadDelta() {
				return load(100 * 1024, vm.endOffset, false).then(function(data) {
					$timeout.cancel(vm.timer);
				}).then(function() {
					vm.timer = $timeout(loadDelta, 1000);
					return vm.timer;
				}, function() {
					$timeout.cancel(vm.timer);
					vm.timer = undefined;
				});
			}

			function load(size, start, backwards) {
				return logRestService.getLog(vm.type.url, {
					backwards : backwards,
					offset : start,
					max : size
				}).then(function(data) {
					if (data.log.length > 0) {
						var log = data.log.split(vm.type.lineDelimiter);
						if (backwards) {
							vm.log = log.concat(vm.log);
							vm.startOffset = data.startOffset;
							vm.endOffset = Math.max(vm.endOffset, data.endOffset);

						} else {
							vm.log = vm.log.concat(log);
							vm.startOffset = Math.min(vm.startOffset, data.startOffset);
							vm.endOffset = data.endOffset;
						}
					}
					vm.lastUpdated = new Date();
				});
			}

			function split(line) {
				if (vm.type.delimiter != null) {
					return line.split(vm.type.delimiter);
				} else {
					return [ line ];
				}
			}

			function findType() {
				for (var int = 0; int < vm.types.length; int++) {
					var t = vm.types[int];
					if (t.url === vm.path) {
						return t;
					}
				}
				return showLog(vm.types[0]);
			}

			function showLog(type) {
				$location.path('/logs/' + type.url);
			}

			function isActive(type) {
				return vm.type.label === type.label;
			}

			function download(type, event) {
				window.open(baseUrl + '/rest/logs/' + type.url, '_blank');
				event.stopPropagation();
			}

		}
		LogViewController.$inject = [ '$scope', '$timeout', '$location', '$routeParams', '$anchorScroll', 'logRestService', 'baseUrl' ];

		return LogViewController;
	});
})(adminConsole.define);