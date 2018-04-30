(function(define) {
	define([ 'text!common/navigation/sidebar.template.html' ], function f(htmlTemplate) {
		function wsSidebarDirective() {
			return {
				restrict : 'E',
				replace : true,
				template : htmlTemplate,
				transclude : true,
				scope : {},
				controllerAs : 'vm',
				bindToController : true,
				controller : wsSidebarDirectiveController
			};
		}

		function wsSidebarDirectiveController($scope, navigationService) {
			var vm = this;
			vm.locations = resolveLocations();
			vm.isActive = navigationService.isActive;

			$scope.$on('$locationChangeSuccess', resolveLocations);

			function resolveLocations() {
				var path = navigationService.getLocationPath();
				vm.locations = path.length > 0 ? path[0].childLocations : [];
			}
		}
		wsSidebarDirectiveController.$inject = [ '$scope', 'navigationService' ];

		return wsSidebarDirective;
	});
})(adminConsole.define);