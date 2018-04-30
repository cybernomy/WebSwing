(function(define) {
	define([ 'text!common/loading/loadingAnimation.template.html' ], function f(html) {
		function wsLoadingAnimationDirective() {
			return {
				restrict : 'E',
				template : html,
				scope : {},
				controllerAs : 'vm',
				bindToController : true,
				controller : wsLoadingAnimationDirectiveController
			};
		}

		function wsLoadingAnimationDirectiveController(loading) {
			var vm = this;
			vm.isLoading = loading.isLoading;
		}
		wsLoadingAnimationDirectiveController.$inject = [ 'loading' ];

		return wsLoadingAnimationDirective;
	});
})(adminConsole.define);