(function (define) {
    define(['text!shared/basic/stringField.template.html'], function f(htmlTemplate) {
        function wsStringFieldDirective() {
            return {
                restrict: 'E',
                template: htmlTemplate,
                transclude: true,
                scope: {
                    value: '=',
                    items: '=',
                    variables: '=',
                    readonly: '=',
                    discriminator: '=',
                    restricted: '@',
                    label: '@',
                    desc: '@',
                    type: '@'
                },
                controllerAs: 'vm',
                bindToController: true,
                link: link,
                controller: controller
            };
        }

        function link(scope, element, attrs, ctrl) {

        }

        function controller($scope, $attrs) {
            var vm = this;
            vm.originalValue = vm.value;
            vm.required = resolve('required', false);
            vm.requiredMsg = resolve('requiredMsg', 'This value is mandatory!');
            vm.helpVisible = false;
            vm.setChoice = setChoice;
            vm.openHelper = openHelper;
            vm.toggleHelper = toggleHelper;

            vm.onBlur = onBlur;

            $scope.$on('wsHelperClose', function (evt, ctrl) {
                if (vm !== ctrl) {
                    vm.helpVisible = false;
                }
            });

            function toggleHelper() {
                if (vm.variables != null) {
                    vm.helpVisible = !vm.helpVisible;
                    if (vm.helpVisible) {
                        $scope.$emit('wsHelperOpened', vm);
                    }
                }
            }

            function openHelper() {
                if (vm.variables != null) {
                    vm.helpVisible = true;
                    $scope.$emit('wsHelperOpened', vm);
                }
            }

            function setChoice(value) {
                if (vm.value !== value) {
                    vm.value = value;
                    if (vm.discriminator) {
                        requestFormUpdate();
                    }
                }
            }

            function onBlur() {
                if (vm.originalValue !== vm.value) {
                    vm.originalValue = vm.value;
                    if (vm.discriminator) {
                        requestFormUpdate();
                    }
                }
            }

            function requestFormUpdate() {
                $scope.$emit('wsRequestFormUpdate', vm);
            }

            function resolve(name, defaultVal) {
                if ($attrs[name] != null) {
                    return $attrs[name];
                } else {
                    return defaultVal;
                }
            }
        }

        controller.$inject = ['$scope', '$attrs'];

        return wsStringFieldDirective;
    });
})(adminConsole.define);