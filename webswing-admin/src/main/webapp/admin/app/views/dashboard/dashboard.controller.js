(function(define) {
	define([], function f() {
		function DashboardController($scope, $location, $routeParams, configRestService, permissions) {
			var vm = this;
			vm.path = $routeParams.path;
			vm.refresh = refresh;
			vm.clean = clean;
			vm.create = create;
			vm.newPath = '';
			vm.paths = [];
			vm.lastUpdated = null;
			vm.getVisiblePaths = getVisiblePaths;
			vm.permissions = permissions;
			vm.showSingle = showSingle;
			vm.isActive = isActive;
			refresh();

			function refresh() {
				vm.paths = [];
				configRestService.getPaths().then(function(data) {
					vm.paths = data;
					vm.lastUpdated = new Date();
				})
			}

			function getVisiblePaths() {
				if (vm.path != null) {
					return [ '/' + vm.path ];
				} else {
					return vm.paths;
				}
			}

			function showSingle(path) {
				if (path == null || path.length === 0) {
					$location.path('/dashboard');
				} else {
					$location.path('/dashboard/single' + (path.charAt(0) !== '/' ? '/' : '') + path);
				}
			}

			function isActive(path) {
				if (path == null && (vm.path == null || vm.path.length === 0)) {
					return true
				}
				if (path === '/' + vm.path) {
					return true;
				}
				return false;
			}

			function clean() {
				vm.newPath = '';
			}

			function create() {
				configRestService.create(vm.newPath).then(refresh);
			}

		}
		DashboardController.$inject = [ '$scope', '$location', '$routeParams', 'configRestService', 'permissions' ];

		return DashboardController;
	});
})(adminConsole.define);