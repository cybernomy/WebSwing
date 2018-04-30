(function(define) {
	define([ 'text!shared/basic/jsonField.template.html' ], function f(htmlTemplate) {
		function wsJsonFieldDirective() {
			return {
				restrict : 'E',
				template : htmlTemplate,
				scope : {
					value : '=',
					readonly : '=',
					label : '@',
					desc : '@'
				},
				controllerAs : 'vm',
				bindToController : true,
				controller : wsJsonFieldDirectiveController
			};
		}

		function wsJsonFieldDirectiveController($scope, $attrs) {
			var vm = this;
			vm.json = '';
			vm.aceLoaded = aceLoaded;

			$scope.$watch('vm.json', function(value) {
				try {
					var newConfig = angular.fromJson(value);
					vm.value = newConfig;
				} catch (e) {
				}
			}, true);

			function aceLoaded(editor) {
				editor.setReadOnly(vm.readonly);
				$scope.$watch("vm.readonly", function(value) {
					editor.setReadOnly(value);
				});
				$scope.$watch('vm.value', function() {
					updateJson();
					editor.resize(true);
				}, true);
			}

			function updateJson() {
				vm.json = angular.toJson(vm.value, true);
			}
		}

		wsJsonFieldDirectiveController.$inject = [ '$scope', '$attrs' ];

		return wsJsonFieldDirective;
	});
})(adminConsole.define);