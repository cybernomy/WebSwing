$.ajax({
	url : "/draw/tests",
}).done(function(d) {
    ES6Promise.polyfill();
	var data = $.parseJSON(d);

	var selected = getUrlParameter('n');

	data.forEach(function(method, index, array) {
		if (selected == null || (selected != null && index == selected)) {
			document.body.innerHTML += '<div id="' + method + 'label"></div><canvas id="' + method + '" width="1000" height="100"></canvas>';
		}
	});
	var dd = new WebswingDirectDraw();
	var sequence = Promise.resolve();
	data.forEach(function(method, index) {
		sequence = sequence.then(function(resolved) {
			return executeMethod(method,index);
		});
	});

	function executeMethod(method, index) {
		return new Promise(function(resolve, reject) {
			if (selected == null || (selected != null && index == selected)) {
				$.ajax({
					url : "/draw?reset&test=" + method,
				}).done(function(d) {
					var canvas = document.getElementById(method);
					var ctx = canvas.getContext("2d");
					var json = $.parseJSON(d);
					addInfo(json, index, document.getElementById(method + "label"));
					var sequence = Promise.resolve();
					json.protoImg.forEach(function(img, index) {
						sequence = sequence.then(function(resolved) {
							console.log('started ' + method);
							return dd.draw64(img);
						}).then(function(resultCanvas) {
							console.log('finished ' + method);
							ctx.drawImage(resultCanvas, 0, 0);
						}, function(error) {
							console.log(error);
						});
					});
					sequence.then(function() {
						resolve();
					});
					json.originalImg.forEach(function(img) {
						drawImage(canvas, img);
					});
				});
			}else{
				resolve();
			}
		});
	}

});

function addInfo(json, index, element) {
	element.innerHTML = index + ". " + ((json.protoRenderSize / json.originalRenderSize) * 100).toPrecision(4) + "% in size, "
			+ ((json.protoRenderTime / json.originalRenderTime) * 100).toPrecision(4) + "% in time";
}

function drawImage(canvas, b64image) {
	return new Promise(function(resolve, reject) {
		var imageObj;
		imageObj = new Image();
		imageObj.onload = function() {
			var context = canvas.getContext("2d");
			context.drawImage(imageObj, 500, 0);
			imageObj.onload = null;
			imageObj.src = '';
			resolve();
		};
		imageObj.src = 'data:image/png;base64,' + b64image;
	});

}

function getUrlParameter(sParam) {
	var sPageURL = window.location.search.substring(1);
	var sURLVariables = sPageURL.split('&');
	for ( var i = 0; i < sURLVariables.length; i++) {
		var sParameterName = sURLVariables[i].split('=');
		if (sParameterName[0] == sParam) {
			return sParameterName[1];
		}
	}
}