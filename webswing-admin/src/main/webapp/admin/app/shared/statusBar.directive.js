(function (define) {
    define(['text!shared/statusBar.template.html'], function f(htmlTemplate) {
        function wsStatusBarDirective() {

            return {
                restrict: 'E',
                template: htmlTemplate,
                scope: {
                    status: '='
                },
                controllerAs: 'vm',
                bindToController: true,
                controller: wsStatusBarDirectiveController
            };
        }

        function wsStatusBarDirectiveController($scope) {
            var vm = this;
            vm.panelStatusClass = panelStatusClass;

            function panelStatusClass(className) {
                if (vm.status != null) {
                    var s = vm.status.status;
                    if (s === 'Running') {
                        return className + '-success';
                    }
                    if (s === 'Stopped') {
                        return className + '-default';
                    }
                    if (s === 'Error') {
                        return className + '-danger';
                    }
                    return className + '-warning';
                }
            }
        }

        wsStatusBarDirectiveController.$inject = ['$scope'];

        return wsStatusBarDirective;
    });
})(adminConsole.define);