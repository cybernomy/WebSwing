(function(define) {
	define([ 'views/log/logView.controller' ], function f(logViewCtrl) {
		var module = angular.module('wsLogView', []);

		module.controller('LogViewController', logViewCtrl);

		module.run([ 'navigationService', 'permissions', function(navigationService, permissions) {
			var primary = navigationService.addLocation('Logs', '#/logs', 'logsView');
		} ]);

		module.config([ '$routeProvider', function($routeProvider) {
			$routeProvider.when('/logs/:path*', {
				controller : 'LogViewController',
				controllerAs : 'vm',
				templateUrl : 'app/views/log/logView.template.html'
			});
			$routeProvider.when('/logs', {
				controller : 'LogViewController',
				controllerAs : 'vm',
				templateUrl : 'app/views/log/logView.template.html'
			});
		} ]);
	});
})(adminConsole.define);