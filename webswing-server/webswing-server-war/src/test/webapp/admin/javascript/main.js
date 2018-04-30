(function(root) {
	require.config({
		baseUrl : '../javascript',
		map : {
			'*' : {
				'jquery' : 'jquery-private'
			},
			'jquery-private' : {
				'jquery' : 'jquery'
			}
		}
	});

	require([ 'webswing' ], function(result) {

	}, function(err) {
		var failedId = err.requireModules && err.requireModules[0];
		throw Error("Error while starting webswing in module '" + failedId + "'. Reason:" + err);
	});


})(this);
