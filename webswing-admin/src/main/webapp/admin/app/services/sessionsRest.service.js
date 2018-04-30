(function (define) {
    define([], function f() {
        function sessionsRestService(baseUrl, $http, errorHandler, messageService) {
            return {
                getSessions: getSessions,
                getSession: getSession,
                recordSession: recordSession,
                killSession: killSession,
                forceKillSession: forceKillSession,
                getStackDumpPath: getStackDumpPath,
                requestThreadDump: requestThreadDump
            };

            function getSessions(path) {
                return $http.get(toPath(path) + '/rest/sessions/').then(success, failed);

                function success(data) {
                    return data.data;
                }

                function failed(data) {
                    return errorHandler.handleRestError('load sessions', data, true);
                }
            }

            function getSession(path, id) {
                return $http.get(toPath(path) + '/rest/session/' + id).then(success, failed);

                function success(data) {
                    return data.data;
                }

                function failed(data) {
                    return errorHandler.handleRestError('load session ' + id, data, true);
                }
            }

            function recordSession(path, id) {
                return $http.get(toPath(path) + '/rest/record/' + id).then(success, failed);

                function success(data) {
                    return data.data;
                }

                function failed(data) {
                    return errorHandler.handleRestError('start session recording', data, true);
                }
            }

            function forceKillSession(path, id) {
                return $http.delete(toPath(path) + '/rest/session/' + id + '?force=true').then(success, failed);

                function success(data) {
                    messageService.info('Application process has been forcefully terminated.');
                }

                function failed(data) {
                    return errorHandler.handleRestError('Swing application process shutdown', data, true);
                }
            }

            function killSession(path, id) {
                return $http.delete(toPath(path) + '/rest/session/' + id).then(success, failed);

                function success(data) {
                    messageService.info('Application signalled to exit.');
                }

                function failed(data) {
                    return errorHandler.handleRestError('Swing application shutdown', data, true);
                }
            }

            function getStackDumpPath(path, instanceId, id) {
                return toPath(path) + '/rest/threadDump/' + instanceId + '?id=' + id;
            }

            function requestThreadDump(path,instanceId){
                return $http.post(toPath(path) + '/rest/threadDump/' + instanceId).then(success, failed);

                function success(data) {
                    messageService.info('Thread Dump requested. Result will appear in Warnings dialog.');
                }

                function failed(data) {
                    return errorHandler.handleRestError('Thread Dump request', data, true);
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

        sessionsRestService.$inject = ['baseUrl', '$http', 'errorHandler', 'messageService'];
        return sessionsRestService;
    });
})(adminConsole.define);