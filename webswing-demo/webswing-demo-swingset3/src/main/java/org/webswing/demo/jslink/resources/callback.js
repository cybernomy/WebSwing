(function() {
    function callbackFunction(string) {
        alert('Callback function invoked with argument:' + string);
    }
    dummyService.methodWithCallback(callbackFunction);
})()