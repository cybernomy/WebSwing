(function (root) {
    require.config({
        baseUrl: 'javascript',
        map: {
            '*': {
                'jquery': 'jquery-private'
            },
            'jquery-private': {
                'jquery': 'jquery'
            }
        }
    });
    try{
        var storedLang = localStorage.getItem("webswingLang") ;
    }catch (e){
        console.log(e);
    }
    var lang= storedLang || (navigator.browserLanguage || navigator.language);

    require({locale: lang},['webswing'], function () {
    }, function (err) {
        var failedId = err.requireModules && err.requireModules[0];
        console.error(err);
        throw Error("Error while starting webswing in module '" + failedId + "'. Reason: " + err);
    });
})(this);
