(function(define) {
	define([], function f() {
		function SwingConfigController($scope, $timeout, $location, configRestService, $routeParams, permissions, loading, extValue) {
			var vm = this;
			vm.config = {
				hide : [ 'path' ],
				enable : null
			};
			vm.variables = {};
			vm.reset = reset;
			vm.apply = apply;
			vm.back = back;
			vm.toSessions = toSessions;
			vm.path = $routeParams.path;
			vm.permissions = permissions;
			vm.readonly = !vm.permissions.configSwingEdit;
			activate();

			$scope.$on('wsStatusChanged', function(evt, ctl) {
				if (ctl.startable != vm.stopped) {
					vm.stopped = ctl.startable;
					vm.config.message = null;
					activate();
				}
			});

			function activate() {
				if (!loading.isLoading()) {
					loading.startLoading();
					vm.config = angular.extend({}, vm.config, {
						fields : []
					});
					return configRestService.getConfig(vm.path).then(function(data) {
						vm.config = angular.extend({}, vm.config, data);
					}).then(function() {
						return configRestService.getVariables(vm.path, 'Basic');
					}).then(function(data) {
						vm.variables['Basic'] = data;
					}).then(function() {
						return configRestService.getVariables(vm.path, 'SwingApp');
					}).then(function(data) {
						vm.variables['SwingApp'] = data;
					}).then(function() {
						return configRestService.getVariables(vm.path, 'SwingInstance');
					}).then(function(data) {
						vm.variables['SwingInstance'] = data;
					}).then(function() {
						loading.stopLoading();
					});
				}
			}

			function reset() {
				return activate();
			}

			function apply(config) {
				return configRestService.setConfig(vm.path, config).then(activate);
			}

			function back() {
				$location.path('/dashboard/single/' + vm.path);
			}

			function toSessions() {
				$location.path('/dashboard/overview/' + vm.path);
			}
		}
		SwingConfigController.$inject = [ '$scope', '$timeout', '$location', 'configRestService', '$routeParams', 'permissions', 'loading', 'extValue' ];

		return SwingConfigController;
	});
})(adminConsole.define);