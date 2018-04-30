(function(define) {
	define([ 'text!common/navigation/navbar.template.html' ], function f(htmlTemplate) {
		function wsNavbarDirective() {
			return {
				restrict : 'E',
				replace : true,
				template : htmlTemplate,
				scope : {
					branding : '@',
					navigation : '@'
				},
				controllerAs : 'vm',
				bindToController : true,
				controller : wsNavbarDirectiveController
			};
		}

		function wsNavbarDirectiveController($scope, navigationService, loginService, baseUrl, permissions) {
			var vm = this;
			vm.isActive = navigationService.isActive;
			vm.isCollapsed = true;
			vm.logout = logout;
			vm.baseUrl = baseUrl;
			vm.locations = [];
			$scope.$on('wsPermissionsReloaded', loadLocations);

			function loadLocations() {
				vm.locations.length = 0;// clean array
				var locations = navigationService.getLocations();
				for (var i = 0; i < locations.length; i++) {
					if (locations[i].permission == null || permissions[locations[i].permission]) {
						vm.locations.push(locations[i]);
					}
				}
			}

			function logout() {
				loginService.logout();
			}

		}
		wsNavbarDirectiveController.$inject = [ '$scope', 'navigationService', 'loginService', 'baseUrl', 'permissions' ];

		return wsNavbarDirective;
	});
})(adminConsole.define);