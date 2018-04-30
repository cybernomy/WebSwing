'use strict';

/* Filters */

angular.module('ws-player.filters', [])
	.filter('size', [

		function() {
			return function(number) {
				if (number < 1024) {
					return number + 'b';
				} else {
					return (number / 1024).toFixed(1) + 'kb';
				}
			};
		}
	])
	.filter('instName', ['protoDecoder',
		function(protoDecoder) {
			var InstructionProto = protoDecoder.getType("org.webswing.directdraw.proto.DrawInstructionProto.InstructionProto");
			return function(number) {
				for (name in InstructionProto) {
					if (InstructionProto[name] == number) {
						return name;
					}
				}
			};
		}
	]);