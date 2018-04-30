(function() {
    dummyService.setDummyString("newDummyString").then(function() {
        return dummyService.echoString("hello echo!");
    }).then(function(result) {
        alert('Sending echo message:"' + result + '" back to swing app.');
        dummyService.displayMessage(result);
    });
})()