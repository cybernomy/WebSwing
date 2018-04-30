(function(define) {
	define([ 'views/dashboard/dashboard.controller',
	         'views/dashboard/overview.controller', 
	         'views/dashboard/session.controller', 
	         'views/dashboard/playback.controller' 
	         ],
	         function f(DashboardController, DashboardOverviewController, DashboardSessionController, DashboardPlaybackController) {
		var module = angular.module('wsDashboard', []);

		module.controller('DashboardController', DashboardController);
		module.controller('DashboardOverviewController', DashboardOverviewController);
		module.controller('DashboardSessionController', DashboardSessionController);
		module.controller('DashboardPlaybackController', DashboardPlaybackController);

		module.run([ 'navigationService', function(navigationService) {
			var dashboard = navigationService.addLocation('Dashboard', '#/dashboard');
			// dashboard.addLocation("Overview", '#/dashboard/overview', true);
		} ]);

		module.config([ '$routeProvider', function($routeProvider) {
			$routeProvider.when('/dashboard', {
				controller : 'DashboardController',
				controllerAs : 'vm',
				templateUrl : 'app/views/dashboard/dashboard.template.html'
			});
			$routeProvider.when('/dashboard/single/:path*', {
				controller : 'DashboardController',
				controllerAs : 'vm',
				templateUrl : 'app/views/dashboard/dashboard.template.html'
			});
			$routeProvider.when('/dashboard/overview/:path*', {
				controller : 'DashboardOverviewController',
				controllerAs : 'vm',
				templateUrl : 'app/views/dashboard/overview.template.html'
			});
			$routeProvider.when('/dashboard/session/:path*', {
				controller : 'DashboardSessionController',
				controllerAs : 'vm',
				templateUrl : 'app/views/dashboard/session.template.html'
			});
			$routeProvider.when('/dashboard/playback', {
				controller : 'DashboardPlaybackController',
				controllerAs : 'vm',
				templateUrl : 'app/views/dashboard/playback.template.html'
			});
			$routeProvider.otherwise('/dashboard');
		} ]);

	});
})(adminConsole.define);