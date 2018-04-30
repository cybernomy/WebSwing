(function(define) {
	define([], function f() {
		var loading = false;

		function isLoading() {
			return loading;
		}

		function startLoading() {
			loading = true;
		}

		function stopLoading() {
			loading = false;
		}

		return {
			isLoading : isLoading,
			startLoading : startLoading,
			stopLoading : stopLoading
		};
	});
})(adminConsole.define);