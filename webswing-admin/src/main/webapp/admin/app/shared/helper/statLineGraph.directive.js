(function(define) {
	define([ 'text!shared/helper/statLineGraph.template.html' ], function f(htmlTemplate) {
		function wsStatLineGraphDirective() {
			return {
				restrict : 'E',
				template : htmlTemplate,
				scope : {
					value : '='
				},
				controllerAs : 'vm',
				bindToController : true,
				controller : wsStatLineGraphDirectiveController
			};
		}

		function wsStatLineGraphDirectiveController($scope, $attrs) {
			var vm = this;
			var colors = [ '#5cb85c', '#337ab7', '#f0ad4e', '#d9534f' ];
			vm.data = vm.series = [];
			vm.options = {
				series : vm.series,
				axes : {
					x : {
						key : 'x',
						type : 'date',
						ticks : 2
					},
					y : {
						min : 0,
						ticks : 2
					}
				}
			};

			$scope.$watch('vm.value', function(newValue) {
				vm.data = processValue(newValue);
			})

			function processValue(value) {
				vm.series.splice(0, vm.series.length);
				if (value != null) {
					for (var int = 0; int < value.names.length; int++) {
						var serie = value.names[int];
						var key = value.keys[int]
						vm.series.push({
							dataset : 'dataset',
							key : key,
							label : serie,
							type : [ 'line', 'area' ],
							defined : function(v) {
								return v != null
							},
							color : colors[int],
						});
					}
					if (value.tickFormat != null) {
						vm.options.axes.y.tickFormat = value.tickFormat;
					}
				}
				return value;
			}
		}

		wsStatLineGraphDirectiveController.$inject = [ '$scope', '$attrs' ];

		return wsStatLineGraphDirective;
	});
})(adminConsole.define);