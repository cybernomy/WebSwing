'use strict';

/* Directives */


angular.module('ws-player.directives', [])
	.directive('canvasSrc', [

		function() {
			return {
				restrict: 'A',
				link: function(scope, lElement, attrs) {
					scope.$watch(attrs.canvasSrc + '.src', function(src) {
						if (src != null && src.tagName != null) {
							var ctx = lElement.context.getContext("2d")
							if (src.tagName.toUpperCase() == 'CANVAS' || src.tagName.toUpperCase() == 'IMG') {
								lElement.context.width = src.width;
								lElement.context.height = src.height;
								ctx.drawImage(src, 0, 0);
							}
						}
					});
				}
			};
		}
	])
	.directive('ddInstruction', ['$compile', 'protoDecoder',

		function($compile, protoDecoder) {
			return {
				restrict: 'A',
				replace: true,
				scope: {
					instruction: '=ddInstruction',
					constants: '=constants',
					canvas: '=canvas',
					ws:'=ws'
				},
				link: function(scope, element, attrs) {
					if (scope.instruction != null && scope.instruction.webImage != null) {
						scope.webimage = protoDecoder.bin(scope.instruction.webImage);
						$compile('<div dd-web-image="webimage"  canvas="canvas" ws="ws"></div>')(scope, function(cloned, scope) {
							element.append(cloned);
						});
					}
					scope.cached = function(number) {
						if (scope.constants[number] == null) {
							return true;
						}
					}
				},
				templateUrl: 'partials/ddInstructions.html'
			};
		}
	])
	.directive('ddImage', ['$compile', 'protoDecoder',

		function($compile, protoDecoder) {
			return {
				restrict: 'A',
				scope: {
					image: '=ddImage',
					canvas: '=canvas'
				},
				link: function(scope, element, attrs) {
					element.bind('click', function(evt) {
						if (scope.image != null && scope.image.data != null) {
							protoDecoder.loadImage(scope.image.data, function(img) {
								scope.$apply(function() {
									scope.canvas.src = img;
								});
							})
						}
					});
				},
				templateUrl: 'partials/ddImage.html'
			};
		}
	])
	.directive('ddWebImage', [

		function() {
			return {
				restrict: 'A',
				replace: true,
				scope: {
					webimage: '=ddWebImage',
					canvas: '=canvas',
					ws: '=ws'
				},
				link: function(scope, element, attrs) {
					scope.currentViews = ['instructions', 'constants'];
					scope.currentView = 'instructions';
					scope.pool = {};


					if (scope.webimage.constants != null) {
						scope.webimage.constants.forEach(function(constant, index) {
							scope.pool[constant.id] = constant;
						});
					}
					scope.draw = function() {
						var dd = new WebswingDirectDraw({
							constantPoolCache: scope.ws.getDirectDraw().getConstantPoolCache()
						});
						dd.drawProto(scope.webimage).then(function(result) {
							scope.$apply(function() {
								scope.canvas.src = result;
							});
						});
					};
					scope.hasChunks = function(img) {
						img.chunks != null && img.chunks.lenght > 0
					};
					if (scope.hasChunks(scope.webimage)) {
						var parent = element.find('#chunks');
						for (var i; i < scope.webimage.chunks.length; i++) {
							$compile('<div dd-web-image="webimage.chunks[' + i + ']"  canvas="canvas" ws="ws"></div>')(scope, function(cloned, scope) {
								element.append(cloned);
							});
						}
					}
				},
				templateUrl: 'partials/ddWebImageDirective.html'
			};
		}
	]);