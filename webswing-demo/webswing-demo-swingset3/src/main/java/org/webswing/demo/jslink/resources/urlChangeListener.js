(function() {
    window.onhashchange = function(evt){
        window.UrlChanageService.onUrlChanged(evt.newURL );
    }
    return "done!";
})()