(function (define) {
    define([
        'shared/appView.directive',
        'shared/statusBar.directive',
        'shared/configView.directive',
        'shared/field.directive',
        'shared/objectField.directive',
        'shared/objectListField.directive',
        'shared/objectListAsTableField.directive',
        'shared/objectMapField.directive',
        'shared/basic/jsonField.directive',
        'shared/basic/stringField.directive',
        'shared/basic/stringListField.directive',
        'shared/basic/booleanField.directive',
        'shared/basic/stringMapField.directive',
        'shared/helper/substitutorHelper.directive',
        'shared/helper/statLineGraph.directive',
        'shared/helper/substitutor.filter'
    ], function f(appView, statusBar, configView, field, objectField, objectListField, objectListAsTableField, objectMapField, jsonField, stringField, stringListField, booleanField,wsStringMapField, substitutorHelper, statLineGraph, substitutorFilter) {
        var module = angular.module('wsShared', []);

        module.directive('wsAppView',appView)
        module.directive('wsStatusBar',statusBar)
        module.directive('wsConfigView',configView)
        module.directive('wsField',field)
        module.directive('wsObjectField',objectField)
        module.directive('wsObjectListField',objectListField)
        module.directive('wsObjectListAsTableField',objectListAsTableField)
        module.directive('wsObjectMapField',objectMapField)
        module.directive('wsJsonField', jsonField);
        module.directive('wsStringField', stringField);
        module.directive('wsStringListField', stringListField);
        module.directive('wsBooleanField', booleanField);
        module.directive('wsStringMapField', wsStringMapField);
        module.directive('wsSubstitutorHelper', substitutorHelper);
        module.directive('wsLineGraph', statLineGraph);
        module.filter('substitutor', substitutorFilter);

    });
})(adminConsole.define);