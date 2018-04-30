(function(define) {
	define([], function f() {
		function configRestService(baseUrl, $http, errorHandler, messageService) {
			return {
				getPaths : getPaths,
				getInfo : getInfo,
				getConfig : getConfig,
				setConfig : setConfig,
				getVariables : getVariables,
				start : start,
				stop : stop,
				remove : remove,
				create : create,
				getMeta : getMeta
			};

			function getPaths() {
				return $http.get(baseUrl + '/rest/paths').then(success, failed);
				function success(data) {
					return data.data;
				}
				function failed(data) {
					return errorHandler.handleRestError('load installed apps', data, true);
				}
			}

			function getInfo(path) {
				return $http.get(toPath(path) + '/info').then(success, failed);
				function success(data) {
					return data.data;
				}
				function failed(data) {
					return errorHandler.handleRestError('load status information', data, true);
				}
			}

			function getConfig(path) {
				return $http.get(toPath(path) + '/rest/config').then(success, failed);
				function success(data) {
					return data.data;
				}
				function failed(data) {
					return errorHandler.handleRestError('load configuration', data, true);
				}
			}

			function setConfig(path, config) {
				return $http.post(toPath(path) + '/rest/config', config).then(success, failed);
				function success() {
					messageService.success('Configuration saved.');
				}
				function failed(data) {
					return errorHandler.handleRestError('save path configuration', data);
				}
			}

			function start(path) {
				return $http.get(toPath(path) + '/start').then(success, failed);
				function success() {
					messageService.success('Application ' + toPath(path) + ' started.');
				}
				function failed(data) {
					return errorHandler.handleRestError('start ' + path + ' application', data);
				}
			}

			function stop(path) {
				return $http.get(toPath(path) + '/stop').then(success, failed);
				function success() {
					messageService.success('Application ' + toPath(path) + ' stopped.');
				}
				function failed(data) {
					return errorHandler.handleRestError('stop ' + path + ' application', data);
				}
			}

			function create(path) {
				return $http.get(baseUrl + '/rest/create' + toPath(path)).then(success, failed);
				function success() {
					messageService.success('Application ' + toPath(path) + ' created.');
				}
				function failed(data) {
					return errorHandler.handleRestError('create application', data);
				}
			}

			function remove(path) {
				return $http.get(baseUrl + '/rest/remove' + toPath(path)).then(success, failed);
				function success() {
					messageService.success('Application ' + toPath(path) + ' removed.');
				}
				function failed(data) {
					return errorHandler.handleRestError('uninstall ' + path + ' application', data);
				}
			}

			function getVariables(path,type) {
				return $http.get(toPath(path) + '/rest/variables/'+type).then(success, failed);
				function success(data) {
					return data.data;
				}
				function failed(data) {
					return errorHandler.handleRestError('load available configuration variables', data, true);
				}
			}

			function getMeta(path,config) {
				return $http.post(toPath(path) + '/rest/metaConfig', config).then(success, failed);
				function success(data) {
					return data.data;
				}
				function failed(data) {
					return errorHandler.handleRestError('reload settings view', data, true);
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

		configRestService.$inject = [ 'baseUrl', '$http', 'errorHandler', 'messageService' ];
		return configRestService;
	});
})(adminConsole.define);