(function(define) {
	define([], function f() {
		function permService($rootScope, baseUrl, $http, messageService) {
			var permissions = {
				reload : getPermissions
			};
			getPermissions();
			return permissions;

			function getPermissions() {
				return $http.get(baseUrl + '/rest/permissions').then(success, failed);
				function success(data) {
					angular.merge(permissions, data.data);
					$rootScope.$broadcast('wsPermissionsReloaded', permissions);
				}
				function failed(data) {
					messageService.error('Failed to resolve permissions');
				}
			}
		}

		permService.$inject = [ '$rootScope', 'baseUrl', '$http', 'messageService' ];
		return permService;
	});
})(adminConsole.define);