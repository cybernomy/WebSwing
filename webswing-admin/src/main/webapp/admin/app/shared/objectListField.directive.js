(function(define) {
	define([ 'text!shared/objectListField.template.html' ], function f(htmlTemplate) {
		function wsObjectListFieldDirective() {

			return {
				restrict : 'E',
				template : htmlTemplate,
				scope : {
					field : '=',
					value : '=',
					variables : '=',
					readonly : '=',
					restricted : '@',
					desc : '@',
					label : '@'
				},
				controllerAs : 'vm',
				bindToController : true,
				controller : wsObjectListFieldDirectiveController
			};
		}

		function wsObjectListFieldDirectiveController($scope, $element, $attrs) {
			var vm = this;
			vm.addObject = addObject;
			vm.removeObject = removeObject;
			vm.addFirstObject = addFirstObject;

			function addObject(index) {
				vm.value.splice(index + 1, 0, {
					fields : []
				});
				requestFormUpdate();
			}

			function addFirstObject() {
				vm.field.value = [ {
					fields : []
				} ];
				requestFormUpdate();
			}

			function removeObject(index) {
				vm.value.splice(index, 1);
			}

			function requestFormUpdate() {
				$scope.$emit('wsRequestFormUpdate', vm);
			}

		}

		wsObjectListFieldDirectiveController.$inject = [ '$scope', '$element', '$attrs' ];

		return wsObjectListFieldDirective;
	});
})(adminConsole.define);