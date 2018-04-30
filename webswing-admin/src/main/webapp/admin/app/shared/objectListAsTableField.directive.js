(function(define) {
	define([ 'text!shared/objectListAsTableField.template.html' ], function f(htmlTemplate) {
		function wsObjectListAsTableFieldDirective() {

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
				controller : wsObjectListAsTableFieldDirectiveController
			};
		}

		function wsObjectListAsTableFieldDirectiveController($scope, $element, $attrs) {
			var vm = this;
			vm.addObject = addObject;
			vm.removeObject = removeObject;
			vm.addString = addString;
			vm.deleteString = deleteString;
			vm.edit = edit;

			vm.editingIndex = null;

			function addObject(index) {
				if (vm.value == null) {
					vm.value = [];
				}
				vm.value.splice(index + 1, 0, getDefaultObject());
			}

			function removeObject(index) {
				vm.value.splice(index, 1);
			}

			function getDefaultObject() {
				var result = {};
				for ( var i in vm.field.tableColumns) {
					var col = vm.field.tableColumns[i];
					result[col.name] = col.value;
				}
				return result;
			}

			function addString(val, index) {
				val.splice(index + 1, 0, '');
			}

			function deleteString(val, index) {
				val.splice(index, 1);
			}

			function edit(i) {
				vm.editingIndex = i;
			}
		}

		wsObjectListAsTableFieldDirectiveController.$inject = [ '$scope', '$element', '$attrs' ];

		return wsObjectListAsTableFieldDirective;
	});
})(adminConsole.define);