(function(define) {
	define([ 'text!shared/configView.template.html' ], function f(htmlTemplate) {
		function wsConfigViewDirective() {

			return {
				restrict : 'E',
				template : htmlTemplate,
				scope : {
					path : '=',
					value : '=',
					variables : '=',
					readonly : '=',
					apply : '=',
					reset : '='
				},
				controllerAs : 'vm',
				bindToController : true,
				controller : wsConfigViewDirectiveController
			};
		}

		function wsConfigViewDirectiveController($scope, $element, $attrs, $timeout, $location, messageService, wsUtils, configRestService, loading) {
			var vm = this;
			vm.showJson = showJson;
			vm.showForm = showForm;
			vm.isForm = true;
			vm.json = {};
			vm.aceLoaded = aceLoaded;
			vm.applyConfig = applyConfig;
			vm.resetConfig = resetConfig;

			$scope.$on('wsHelperOpened', function(evt, data, i) {
				$scope.$broadcast('wsHelperClose', data, i);
			});

			$scope.$on('wsRequestFormUpdate', function(evt, data, i) {
				$timeout(function() {
					refreshForm(wsUtils.extractValues(vm.value));
				}, 10);
			});

			function refreshForm(config) {
				loading.startLoading();
				var e = $element.find('.ws-panel-heading').first();
				configRestService.getMeta(vm.path, config).then(function(data) {
					vm.value = angular.extend({}, vm.value, data);
					vm.isForm = true;
					$timeout(function() {
						e.height('auto');
						loading.stopLoading();
					}, 0);
				});
				var height = e.innerHeight();
				e.height(height);
			}

			function showJson() {
				vm.isForm = false;
				vm.json = wsUtils.toJson(vm.value);
			}

			function aceLoaded(editor) {
				editor.setReadOnly(vm.readonly);
				$scope.$watch("vm.readonly", function(value) {
					editor.setReadOnly(value);
				});
				vm.updateJson = function() {
					if (!vm.isForm) {
						editor.setValue(wsUtils.toJson(vm.value));
						editor.gotoLine(0);
					}
				}
				$scope.$watch('vm.value', vm.updateJson, true);

			}

			function showForm() {
				var config = vm.value;
				try {
					refreshForm(JSON.parse(vm.json));
				} catch (e) {
					messageService.error('Failed to parse JSON. Back to last valid version.');
				}
			}

			function applyConfig() {
				if (!vm.isForm) {
					vm.apply(JSON.parse(vm.json)).then(vm.updateJson);
				} else {
					vm.apply(wsUtils.extractValues(vm.value));
				}
			}

			function resetConfig() {
				vm.reset().then(vm.updateJson);
			}

		}

		wsConfigViewDirectiveController.$inject = [ '$scope', '$element', '$attrs', '$timeout', '$location', 'messageService', 'wsUtils', 'configRestService', 'loading' ];

		return wsConfigViewDirective;
	});
})(adminConsole.define);