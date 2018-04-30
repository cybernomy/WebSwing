(function(define) {
	define([], function f() {
		function utilsService(baseUrl, $http, errorHandler, messageService) {
			return {
				extractValues : extractValues,
				toJson : toJson,
				getStatsDataset : getStatsDataset,
				getGaugeData : getGaugeData,
				getKeys : getKeys
			};

			function extractValues(config, data) {
				data = config.data || data;
				if (config != null && config.fields != null) {
					var result = data || {};
					for ( var index in config.fields) {
						var field = config.fields[index];
						if (field.value == null) {
							result[field.name] = null;
						} else if (field.type === 'Object') {
							result[field.name] = extractValues(field.value, result[field.name]);
						} else if (field.type === 'ObjectList') {
							var list = [];
							for ( var listIndex in field.value) {
								list.push(extractValues(field.value[listIndex]));
							}
							result[field.name] = list;
						} else if (field.type === 'ObjectMap') {
							var map = {};
							for ( var mapKey in field.value) {
								map[mapKey] = extractValues(field.value[mapKey]);
							}
							result[field.name] = map;
						} else {
							result[field.name] = field.value;
						}
					}
					return result;
				} else {
					return null;
				}
			}

			function toJson(config) {
				return angular.toJson(extractValues(config), true);
			}

			function getStatsDataset(stats, names) {
				var result = [];
				var keysObj = {}
				for (var n = 0; n < names.length; n++) {
					if (stats[names[n]] != null) {
						for ( var item in stats[names[n]]) {
							keysObj[item] = null;
						}
					}
				}
				var keys = getKeys(keysObj).sort();
				for (var int = 0; int < keys.length; int++) {
					var key = keys[int];
					var entry = {
						x : new Date(parseInt(key))
					}
					for (var n = 0; n < names.length; n++) {
						var name = names[n];
						var dataset1 = stats[name];
						var value = dataset1 != null && dataset1[key] != null ? dataset1[key] : 0;
						entry[name] = Math.floor(value);
					}
					result.push(entry);
				}
				return result;
			}

			function getKeys(obj) {
				var keys = [];
				if (obj !== null) {
					for ( var key in obj) {
						if (obj.hasOwnProperty(key)) {
							keys.push(key);
						}
					}
				}
				return keys;
			}

			function getGaugeData(metrics, label, suffix, name, maxName, maxValue, div) {
				var gaugeOptions = {
					thickness : 3,
					mode : "gauge"
				};
				if (metrics != null) {
					var value = Math.floor((metrics[name] != null ? metrics[name] : 0) / div);
					max = metrics[maxName] != null ? metrics[maxName] : maxValue;
					return {
						data : [ {
							label : label,
							value : value,
							suffix : suffix,
							color : '#5cb85c'
						} ],
						options : angular.extend({
							total : max
						}, gaugeOptions)
					}
				}
			}
		}

		utilsService.$inject = [ 'baseUrl', '$http', 'errorHandler', 'messageService' ];
		return utilsService;
	});
})(adminConsole.define);