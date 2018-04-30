({
	baseUrl : "${basedir}/src/main/webapp/admin/app",
	dir : "${project.build.directory}/require-build/admin/app",
	optimize : 'none',
	map : {
		'*' : {
			'text' : 'libs/text'
		}
	},
	paths : {
		'angular' : 'libs/angular',
		'chart' : 'libs/chart',
		'angular-chart' : 'libs/angular-chart'
	},
	modules : [ {
		name : "admin",
		include : [ 'main', 'text' ],
		create : true
	},{
		name : "shared",
		include : [ 'text', 'common/common.module', 'shared/shared.module', 'services/services.module' ],
		create : true
	}],
	wrap : {
		start : "(function(require,define,requirejs) {",
		end : "}(adminConsole.require,adminConsole.define,adminConsole.requirejs));"
	}
})