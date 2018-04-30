define([], function amdFactory() {
	"use strict";

	return function Injector() {
		var api = this;
		api.module = module;
		api.injectAndVerify = injectAndVerify;

		var services = api.services = {};
		var inject = api.inject = {};
		var ready = api.ready = {};

		function module(name, m) {
			if (m != null) {
				if (m.provides != null) {
					services[name] = m.provides;
				}
				if (m.injects != null) {
					inject[name] = m.injects;
				}
				if (m.ready != null) {
					ready[name] = m.ready;
				}
			}
		}

		function injectAndVerify() {
			var errors = '';
			for ( var key in inject) {
				try {
					injectObject(inject[key]);
				} catch (e) {
					if (e instanceof InjectError) {
						errors += '\tModule ' + key + ' :\n' + e + '\n';
					} else {
						throw e;
					}
				}
			}
			if (errors.length > 0) {
				throw new InjectError("Dependency injection errors:\n" + errors);
			}
			for ( var key in ready) {
				try {
					ready[key]();
				} catch (e) {
					if (e instanceof InjectError) {
						errors += '\tModule ' + key + ' ready function :\n' + e + '\n';
					} else {
						throw e;
					}
				}
			}
			if (errors.length > 0) {
				throw new InjectError("Starting modules failed:\n" + errors);
			}
		}

		function injectObject(object) {
			var errors = '';
			for ( var key in object) {
				try {
					var serviceName = object[key];
					if (typeof serviceName === 'string') {
						var value = resolve(services, serviceName);
						object[key] = value;
					}
				} catch (e) {
					if (e instanceof InjectError) {
						errors += '\t\tField ' + key + ' not injected: ' + e + '\n';
					} else {
						throw e;
					}
				}
			}
			if (errors.length > 0) {
				throw new InjectError(errors);
			}
		}

		function resolve(obj, path) {
			try {
				return path.split('.').reduce(index, obj);
			} catch (e) {
				throw new InjectError('service ' + path + ' not found.');
			}
			function index(obj, i) {
				if (obj.hasOwnProperty(i)) {
					return obj[i];
				} else {
					throw new Error();
				}
			}
		}

		function InjectError(msg) {
			this.name = 'Dependency Error';
			this.message = msg;
			this.toString = function() {
				return this.message;
			};
		}
	};
});