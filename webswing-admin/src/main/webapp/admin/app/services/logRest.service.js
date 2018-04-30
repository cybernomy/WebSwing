(function(define) {
	define([], function f() {
		function logRestService(baseUrl, $http, errorHandler, messageService) {
			return {
				getLog : getLog,
			};

			function getLog(type, query) {
				return $http.post(baseUrl + '/rest/logs/' + type, query).then(success, failed);
				function success(data) {
					return data.data;
				}
				function failed(data) {
					return errorHandler.handleRestError('load ' + type + ' log', data, true);
				}
			}

			function toPath(path) {
				if (path.substr(0, 4) === 'http') {
					return path;
				}
				if (path.substr(0, 1) !== '/') {
					path = '/' + path
				}
				if (path.length === 1) {
					path = '';
				}
				return path;
			}
		}

		logRestService.$inject = [ 'baseUrl', '$http', 'errorHandler', 'messageService' ];
		return logRestService;
	});
})(adminConsole.define);