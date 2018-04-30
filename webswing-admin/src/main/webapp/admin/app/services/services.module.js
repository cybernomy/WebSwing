(function (define) {
    define([
        'services/errorHandler.service',
        'services/sessionsRest.service',
        'services/configRest.service',
        'services/logRest.service',
        'services/wsUtils.service',
        'services/permissions.service'
    ], function f(errorHandlerService, sessionsRestService, configRestService, logRest, wsUtils, permissions) {
        var module = angular.module('wsServices', []);
        module.service('errorHandler', errorHandlerService);
        module.service('sessionsRestService', sessionsRestService);
        module.service('configRestService', configRestService);
        module.service('logRestService', logRest);
        module.service('wsUtils', wsUtils);
        module.service('permissions', permissions);
    });
})(adminConsole.define);