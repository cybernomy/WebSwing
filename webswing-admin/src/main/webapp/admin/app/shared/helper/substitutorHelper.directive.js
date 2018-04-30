(function(define) {
	define([], function f(htmlTemplate) {
		function wsSubstitutorHelperDirective(Textcomplete) {
			return {
				restrict : 'A',
				scope : {
					variables : '=',
					message : '='
				},
				link : function(scope, iElement, iAttrs) {
					var textcomplete = new Textcomplete(iElement, [ {
						match : /\$\{([.\w]*)$/,
						search : function(term, callback) {
							var mentions = scope.variables == null ? [] : Object.keys(scope.variables);
							callback($.map(mentions, function(mention) {
								return mention.toLowerCase().indexOf(term.toLowerCase()) !== -1 ? mention : null;
							}));
						},
						index : 1,
						replace : function(mention) {
							return '${' + mention + '}';
						},
						template : function(value) {
							return '<div class="ws-subs-key">${' + value + '}</div><div class="ws-subs-value">' + scope.variables[value] + '</div>';
						}
					} ]);

					$(textcomplete).on({
						'textComplete:select' : function(e, value) {
							scope.$apply(function() {
								scope.message = value
							})
						},
						'textComplete:show' : function(e) {
							$(this).data('autocompleting', true);
						},
						'textComplete:hide' : function(e) {
							$(this).data('autocompleting', false);
						}
					});
				},
				controller : wsSubstitutorHelperDirectiveController
			}
		}

		function wsSubstitutorHelperDirectiveController($scope) {
			var vm = $scope.vm = {};
		}

		wsSubstitutorHelperDirectiveController.$inject = [ '$scope' ];
		wsSubstitutorHelperDirective.$inject = [ 'Textcomplete' ];

		return wsSubstitutorHelperDirective;
	});
})(adminConsole.define);