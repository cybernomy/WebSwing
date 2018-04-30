(function(root) {
	require.config({
		baseUrl : 'app',
		map : {
			'*' : {
				'text' : 'libs/text',
			}
		},
		shim : {
			'libs/angular' : {
				exports : 'angular',
				deps : [ 'libs/jquery' ]
			},
			'libs/bootstrap' : {
				deps : [ 'libs/jquery' ]
			},
			'libs/angular-route' : {
				deps : [ 'libs/angular' ]
			},
			'libs/ui-ace' : {
				deps : [ 'libs/acejs/ace', 'libs/angular' ]
			},
			'libs/ui-bootstrap' : {
				deps : [ 'libs/angular' ]
			},
			'libs/ui-utils' : {
				deps : [ 'libs/angular' ]
			},
			'libs/d3' : {
				exports : 'd3',
				deps : [ 'libs/jquery' ]
			},
			'libs/pie-chart' : {
				deps : [ 'libs/angular', 'libs/d3' ]
			},
			'libs/line-chart' : {
				deps : [ 'libs/angular', 'libs/d3' ]
			},
			'libs/ng-textcomplete' : {
				deps : [ 'libs/angular' ]
			}
		}
	});
	require([ 'libs/jquery', 'angular', 'libs/bootstrap', 'libs/angular-route', 'libs/ui-ace', 'libs/ui-bootstrap', 'libs/ui-utils', 'libs/d3', 'libs/pie-chart', 'libs/line-chart', 'libs/ng-textcomplete' ], function() {
		require([ 'app.external','app.module' ], function() {
            jQuery("#loading").remove();
            angular.bootstrap(root, [ 'wsAdmin' ]);
		}, errorHandler);
	}, errorHandler);

	function errorHandler(err) {
		var failedId = err.requireModules && err.requireModules[0];
		throw Error("Error while starting ws-admin in module '" + failedId + "'. Reason:" + err);
	}

})(document);