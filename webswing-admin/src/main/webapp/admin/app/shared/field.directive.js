(function(define) {
	define([], function f() {
		function wsFieldDirective($compile) {

			var stringTemplate = '<ws-string-field></ws-string-field>';
			var boolTemplate = '<ws-boolean-field></ws-boolean-field>';
			var stringListTemplate = '<ws-string-list-field></ws-string-list-field>';
			var stringMapTemplate = '<ws-string-map-field></ws-string-map-field>';
			var objectTemplate = '<ws-object-field></ws-object-field>';
			var objectListTemplate = '<ws-object-list-field></ws-object-list-field>';
			var objectListAsTableTemplate = '<ws-object-list-as-table-field></ws-object-list-as-table-field>';
			var objectMapTemplate = '<ws-object-map-field></ws-object-map-field>';
			var jsonTemplate = '<ws-json-field></ws-json-field>';
			function linker(scope, element, attrs, ctrl) {
				var vm = scope.vm;

				if (vm.field != null) {
					var commonAttrs = {
						readonly : 'vm.readonly',
						label : vm.field.label,
						desc : vm.field.description,
						discriminator : 'vm.field.discriminator',
						items : 'vm.field.presets',
						field : 'vm.field',
						value : 'vm.field.value'
					};
					if (vm.field.restricted === 'true') {
						commonAttrs.restricted = 'true';
					}
					
					if (vm.field.type === 'String') {
						var template = angular.element(stringTemplate);
						commonAttrs.variables='vm.variables[\''+vm.field.variables+'\']';
						template.attr(commonAttrs);
						element.append(template).show();
					}
					if (vm.field.type === 'Number') {
						var template = angular.element(stringTemplate);
						template.attr(commonAttrs);
						template.attr('type', 'number');
						element.append(template).show();
					}
					if (vm.field.type === 'Boolean') {
						var template = angular.element(boolTemplate);
						template.attr(commonAttrs);
						element.append(template).show();
					}
					if (vm.field.type === 'StringList') {
						var template = angular.element(stringListTemplate);
						commonAttrs.variables='vm.variables[\''+vm.field.variables+'\']';
						template.attr(commonAttrs);
						element.append(template).show();
					}
					if (vm.field.type === 'StringMap') {
						var template = angular.element(stringMapTemplate);
						commonAttrs.variables='vm.variables[\''+vm.field.variables+'\']';
						template.attr(commonAttrs);
						element.append(template).show();
					}
					if (vm.field.type === 'Object') {
						var template = angular.element(objectTemplate);
						commonAttrs.variables = 'vm.variables';
						template.attr(commonAttrs);
						element.append(template).show();
					}
					if (vm.field.type === 'ObjectList') {
						var template = angular.element(objectListTemplate);
						commonAttrs.variables = 'vm.variables';
						template.attr(commonAttrs);
						element.append(template).show();
					}
					if (vm.field.type === 'ObjectListAsTable') {
						var template = angular.element(objectListAsTableTemplate);
						commonAttrs.variables = 'vm.variables';
						template.attr(commonAttrs);
						element.append(template).show();
					}
					if (vm.field.type === 'ObjectMap') {
						var template = angular.element(objectMapTemplate);
						commonAttrs.variables = 'vm.variables';
						template.attr(commonAttrs);
						element.append(template).show();
					}
					if (vm.field.type === 'Generic') {
						var template = angular.element(jsonTemplate);
						template.attr(commonAttrs);
						element.append(template).show();
					}
					$compile(element.contents())(scope);
				}
			}

			return {
				restrict : 'E',
				link : linker,
				scope : {
					field : '=',
					variables : '=',
					readonly : '='
				},
				controllerAs : 'vm',
				bindToController : true,
				controller : wsFieldDirectiveController
			};
		}

		function wsFieldDirectiveController($scope, $element, $attrs) {
			var vm = this;
		}

		wsFieldDirectiveController.$inject = [ '$scope', '$element', '$attrs' ];
		wsFieldDirective.$inject = [ '$compile' ];

		return wsFieldDirective;
	});
})(adminConsole.define);