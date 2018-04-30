(function (define) {
    define([], function f() {
        function errorHandlerService($http, $log, messageService, loginService) {
            return {
                handleRestError: handleRestError
            };


            function handleRestError(operation, data, retry) {
                if (data.status === 403 || data.status === 401) {
                    return loginService.login().then(function () {
                        if (retry) {
                            return $http(data.config).then(success, failed);
                            function success(data) {
                                return data.data;
                            }
                            function failed() {
                                handleRestError(operation, data, false);
                            }
                        }
                    });
                } else {
                    messageService.error('Failed to ' + operation + '. '+data.statusText);
                    $log.error('Failed to ' + operation + ' with status ' + data.status + ' and message :' + data.data);
                    return Promise.reject(new Error());
                }
            }
        }

        errorHandlerService.$inject = ['$http', '$log', 'messageService', 'loginService'];
        return errorHandlerService;
    });
})(adminConsole.define);