(function(define) {
	define([], function f() {
		function loginService(baseUrl, $rootScope, messageService, $http, $log, permissions) {

			return {
				login : login,
				logout : logout
			};

			function login() {
				return new Promise(function(resolve) {
					$rootScope.$broadcast('wsLoginRequestEvent', doLogin);
					function doLogin(element, loginData, successCallback) {
                        webswingadmin.utils.webswingLogin(baseUrl + "/", element, loginData, success);
						function success(data) {
							if (successCallback != null) {
								successCallback();
							}
							permissions.reload();
							resolve(data);
							return data;
						}
					}
				});
			}

			function logout(simple) {
                webswingadmin.utils.webswingLogout(baseUrl + "/", $('<div></div>'), success);
				function success() {
					if (!simple) {
						login();
					}
				}
			}


		}
		loginService.$inject = [ 'baseUrl', '$rootScope', 'messageService', '$http', '$log', 'permissions' ];
		return loginService;
	});
})(adminConsole.define);