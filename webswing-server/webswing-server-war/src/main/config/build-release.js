({
    baseUrl: "${project.build.directory}/${project.build.finalName}/javascript",
    dir: "${project.build.directory}/require-build/javascript/",
    paths: {
        requireLib: 'require'
    },
    namespace: 'webswingRequirejs',
    modules: [{
        name: "webswing-embed",
        include: ['requireLib', 'main', 'jquery-private'],
        create: true
    }, {
        name: "webswing-selector",
        include: ['requireLib', 'selector', 'jquery-private'],
        create: true
    }, {
        name: "webswing-security",
        include: ['requireLib', 'security', 'jquery-private'],
        create: true
    }],
    wrap: {
        start: "/*! Webswing version ${project.version} (${git.commit.id.describe})*/ \n try{",
        endFile: "${project.basedir}/src/main/config/parts/end.frag"
    }
})