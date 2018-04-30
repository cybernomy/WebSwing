'use strict';

/* Controllers */

angular.module('ws-player.controllers', [])
	.controller('Application', ['$scope', '$location', '$http', '$modal', 'protoDecoder',
		function($scope, $location, $http, $modal, protoDecoder) {
			$scope.selectedFrame = {};
			$scope.selectedWebImage = {};
			$scope.selectedWindow = {};
			$scope.selectedCanvasSource = {src:{}};
			$scope.pageindex = 0;
			$scope.currentIndex = 0;
			$scope.timeline = {
				currentFrames: [],
				keyFrames: []
			};
			
			$http.get('/recordings').
			success(function(data, status, headers, config) {
				$scope.files = data;
				$scope.loadDialog = $modal.open({
					templateUrl: 'partials/load-dialog.html',
					scope: $scope,
					backdrop: 'static',
					keyboard: false,
					windowClass: 'mirror-view-modal-dialog'
				});
				$scope.load = function(file) {
					$http.get('/recordings?file=' + file, {
						responseType: "arraybuffer"
					}).
					success(function(buffer, status, headers, config) {
						var dataArray = [];
						var currentOffset = 0;
						while (currentOffset < buffer.byteLength) {
							var dataviewlength = new DataView(buffer, currentOffset, 4).getUint32(0);
							var data = new Uint8Array(buffer, currentOffset + 4, dataviewlength);
							dataArray.push(data);
							currentOffset += 4 + dataviewlength;
						}
						$scope.dataArray = dataArray;
						$scope.loadDialog.close();
					});
				}

				$scope.loadDialog.result.then(function() {
					$scope.loadFrames($scope.pageindex);
				});

				$scope.prev = function() {
					if ($scope.pageindex > 0) {
						$scope.pageindex -= 1
						$scope.loadFrames($scope.pageindex);
					}
				}
				$scope.next = function() {
					if ($scope.pageindex * 10 < $scope.dataArray.length) {
						$scope.pageindex += 1
						$scope.loadFrames($scope.pageindex);
					}
				}

				$scope.selectFrame = function(frame) {
					$scope.selectedFrame = frame;
					$scope.selectedCanvasSource.src = frame.canvas;
					if (frame.msg != null && frame.msg.windows[0] != null) {
						$scope.selectWindow(frame.msg.windows[0])
					}
				}

				$scope.selectWindow = function(window) {
					$scope.selectedWindow = window;
					if (window.directDrawB64 != null) {
						$scope.selectedWebImage = protoDecoder.b64(window.directDrawB64);
					}
				}


				$scope.loadFrames = function(pageindex) {
					if (pageindex != 0 && $scope.timeline.keyFrames[pageindex - 1] == null) {
						$scope.loadFrames(pageindex - 1);
					}
					$scope.timeline.currentFrames.forEach(function(element) {
						if (element != null) {
							element.ws.dispose();
						}
					});
					$scope.timeline.currentFrames.length = 0;
					var currentindex = pageindex * 10 - 1;
					var previousFrame = pageindex == 0 ? null : $scope.timeline.keyFrames[pageindex - 1];
					var load = function() {
						currentindex++;
						if (currentindex < (pageindex + 1) * 10 && currentindex < $scope.dataArray.length) {
							previousFrame = $scope.loadFrame(previousFrame, currentindex, load);
							$scope.timeline.currentFrames[currentindex % 10] = previousFrame;
							if (currentindex == pageindex * 10 + 9) {
								$scope.timeline.keyFrames[pageindex] = previousFrame;
							}
						}
					}
					load();
				};
				$scope.loadFrame = function(previousFrame, frameindex, doneCallback) {
					if ($scope.dataArray[frameindex] == null) {
						return null;
					}
					var frame = {
						index: frameindex,
						done: false,
					};
					if (previousFrame == null && frameindex == 0) {
						frame.canvas = document.createElement("canvas");
						frame.ws = new WebswingBase({
							mirrorMode: true,
							hasControl: false,
							send: function(msg) {
								if (msg.indexOf('paintAck') == 0) {
									frame.done = true;
									frame.img = $scope.scaledImage(frame.canvas, 0.25);
									$scope.$apply(function() {
										doneCallback();
									});
								}
							}
						});
						frame.ws.setCanvas(frame.canvas);
						frame.ws.canPaint(true);
					} else {
						frame.canvas = $scope.cloneCanvas(previousFrame.canvas);
						var imageholdercopy = {};
						var directDrawConfig = {
							constantPoolCache: {}
						};
						for (var iHolder in previousFrame.ws.getWindowImageHolders()) {
							imageholdercopy[iHolder] = $scope.cloneCanvas(previousFrame.ws.getWindowImageHolders()[iHolder]);
						}
						for (var constantPoolEntry in previousFrame.ws.getDirectDraw().getConstantPoolCache()) {
							directDrawConfig.constantPoolCache[constantPoolEntry] = previousFrame.ws.getDirectDraw().getConstantPoolCache()[constantPoolEntry];
						}
						frame.ws = new WebswingBase({
							mirrorMode: true,
							hasControl: false,
							windowImageHolders: imageholdercopy,
							directDrawConfig: directDrawConfig,
							send: function(msg) {
								if (msg.indexOf('paintAck') == 0) {
									frame.done = true;
									frame.img = $scope.scaledImage(frame.canvas, 0.25);
									$scope.$apply(function() {
										doneCallback();
									});
								}
							}
						});
						frame.ws.setCanvas(frame.canvas);
						frame.ws.canPaint(true);
					}
					var msg = $scope.convertToString($scope.dataArray[frameindex]);
					if (msg.indexOf('{') == 0) {
						frame.msg = angular.fromJson(msg);
						frame.ws.processJsonMessage(frame.msg);
					} else {
						frame.msg = msg;
						frame.ws.processTxtMessage(msg);
						frame.ws.ack();
					}
					return frame;
				};

				$scope.cloneCanvas = function(canvas) {
					var newCanvas = document.createElement("canvas");
					newCanvas.width = canvas.width;
					newCanvas.height = canvas.height;
					newCanvas.getContext('2d').drawImage(canvas, 0, 0);
					return newCanvas;
				};

				$scope.convertToString = function(data) {
					var max = 65536;
					var msg = '';
					var offset = 0;
					var uint16 = new Uint16Array(data);
					while (offset < data.length) {
						if (data.length - offset < max) {
							msg += String.fromCharCode.apply(null, uint16.subarray(offset));
							offset = data.length;
						} else {
							msg += String.fromCharCode.apply(null, uint16.subarray(offset, offset + max));
							offset += max;
						}
					}
					return msg;
				};

				$scope.scaledImage = function(canvas, scale) {
					var newCanvas = document.createElement("canvas");
					newCanvas.width = canvas.width * scale;
					newCanvas.height = canvas.height * scale;
					var ctx = newCanvas.getContext("2d");
					ctx.scale(scale, scale);
					ctx.drawImage(canvas, 0, 0);
					return newCanvas.toDataURL("image/png");
				};

				$scope.getKeys = function(object) {
					var result = [];
					for (var key in object) {
						if (object[key] != null) {
							result.push(key);
						}
					}
					return result;
				}
			})


		}
	])


.controller('WebImage', ['$scope', '$modal', '$timeout',
	function($scope, $modal, $timeout) {

	}
])