(function(define) {
	define([], function f() {
		function navigationService($location) {
			var service = this;
			service.getLocationPath = getLocationPath;
			service.addLocation = addLocation;
			service.getLocations = getLocations;
			service.getCurrentLocation = getCurrentLocation;
			service.isActive = isActive;

			var locations = [];

			function Location(name, url, permission) {
				this.name = name;
				this.url = url;
				this.permission = permission;
				this.childLocations = [];
				this.addLocation = function addLocation(name, url, permission, isDefault) {
					var location = new Location(name, url, permission, this);
					location.parent = this;
					this.childLocations.push(location);
					if (isDefault === true) {
						this.defaultChild = location;
					}
					return location;
				};
				this.getDefaultUrl = function getDefaultUrl() {
					function defaultChild(l) {
						if (l.defaultChild == null) {
							return l;
						} else {
							return defaultChild(l.defaultChild);
						}
					}
					return defaultChild(this).url;
				};
			}

			function addLocation(name, url, permission) {
				var location = new Location(name, url, permission);
				locations.push(location);
				return location;
			}

			function getLocations() {
				return locations;
			}

			function getCurrentLocation() {
				var hash = '#' + $location.url();
				function findLocation(locations) {
					for (var i = 0; i < locations.length; i++) {
						if (hash.indexOf(locations[i].url) === 0) {
							var found = findLocation(locations[i].childLocations);
							return found == null ? locations[i] : found;
						}
					}
					return null;
				}
				return findLocation(locations);
			}

			function getLocationPath() {
				function getPath(location) {
					if (location == null) {
						return [];
					} else {
						return getPath(location.parent).concat([ location ]);
					}
				}
				return getPath(service.getCurrentLocation());
			}

			function isActive(location) {
				var position = $.inArray(location, service.getLocationPath());
				return position !== -1;
			}
		}

		navigationService.$inject = [ '$location' ];
		return navigationService;
	});
})(adminConsole.define);