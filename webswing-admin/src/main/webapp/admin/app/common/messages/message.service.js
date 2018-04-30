(function (define) {
    define([], function f() {
        function messageService($rootScope) {

            return {
                success: emitSuccessMessage,
                info: emitInfoMessage,
                warn: emitWarnMessage,
                error: emitErrorMessage
            };

            function emitSuccessMessage(message) {
                emitMessage(message, 'success');
            }

            function emitInfoMessage(message) {
                emitMessage(message, 'info');
            }

            function emitWarnMessage(message) {
                emitMessage(message, 'warning');
            }

            function emitErrorMessage(message) {
                emitMessage(message, 'danger');
            }

            function emitMessage(message, severity) {
                $rootScope.$broadcast('wsMessageEvent', message, severity);
            }
        }
        messageService.$inject = ['$rootScope'];
        return messageService;
    });
})(adminConsole.define);