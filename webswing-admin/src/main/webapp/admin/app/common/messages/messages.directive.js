(function (define) {
    define(['text!common/messages/messages.template.html'], function f(htmlTemplate) {
        function wsMessagesDirective() {
            return {
                restrict: 'E',
                replace: true,
                template: htmlTemplate,
                scope: {},
                controllerAs: 'vm',
                bindToController: true,
                controller: wsMessagesDirectiveController
            };
        }

        function wsMessagesDirectiveController($scope, messageService, $timeout) {
            var vm = this;
            vm.messages = [];
            vm.removeMessage = removeMessage;
            $scope.$on('wsMessageEvent', displayMessage);
            function displayMessage(evt, message, type) {
                if (vm.messages.length > 3) {
                    removeMessage(0);
                }
                var m = {
                    time: new Date(),
                    text: message,
                    type: type
                };
                m.timer = $timeout(closeMessage, 7000);
                vm.messages.push(m);
                function closeMessage() {
                    vm.messages.splice(vm.messages.indexOf(m), 1);
                }
            }

            function removeMessage(index) {
                var m = vm.messages[index];
                if (m != null) {
                    vm.messages.splice(vm.messages.indexOf(m), 1);
                    $timeout.cancel(m.timer);
                }
            }
        }
        wsMessagesDirectiveController.$inject = ['$scope', 'messageService', '$timeout'];
        return wsMessagesDirective;
    });
})(adminConsole.define);