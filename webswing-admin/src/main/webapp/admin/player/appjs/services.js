'use strict';

/* Services */

angular.module('ws-player.services', [])
	.factory('protoDecoder', [

		function() {
			var proto = dcodeIO.ProtoBuf.loadProtoFile("/directdraw.proto");
			var WebImageProto = proto.build("org.webswing.directdraw.proto.WebImageProto");

			return {
				b64: function(data) {
					return WebImageProto.decode64(data);
				},
				bin: function(data) {
					var offset = data.offset;
					var result= WebImageProto.decode(data);
					data.offset = offset;
					return result;
				},
				getType : function(type){
					return proto.build(type);
				},
				loadImage: function(data, callback){
					var img = new Image();
					img.onload = function(){
						callback(img);
					}
					img.src = 'data:image/png;base64,' + data.toBase64()
					return img;
				}

			}
		}
	]);