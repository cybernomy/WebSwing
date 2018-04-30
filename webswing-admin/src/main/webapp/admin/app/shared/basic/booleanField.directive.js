(function(define) {
	define([ 'text!shared/basic/booleanField.template.html' ], function f(htmlTemplate) {
		function wsBooleanFieldDirective() {
			return {
				restrict : 'E',
				template : htmlTemplate,
				scope : {
					value : '=',
					readonly : '=',
					discriminator : '=',
					restricted : '@',
					label : '@',
					desc : '@'
				},
				controllerAs : 'vm',
				bindToController : true,
				controller : wsBooleanFieldDirectiveController
			};
		}

		function wsBooleanFieldDirectiveController($scope, $attrs) {
			var vm = this;
			vm.onChange = onChange;

			function onChange(){
				if(vm.discriminator){
					requestFormUpdate();
				}
			}
			
			function requestFormUpdate() {
				$scope.$emit('wsRequestFormUpdate', vm);
			}
		}

		wsBooleanFieldDirectiveController.$inject = [ '$scope', '$attrs' ];

		return wsBooleanFieldDirective;
	});
})(adminConsole.define);