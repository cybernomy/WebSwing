define(['es6promise', 'typedarray'], function f(es6promise) {
    return function polyfill() {
        var typedArraysSupported = false;
        if (isArrayBufferSupported()) {
            typedArraysSupported = true;
        }
        if (!isPromisesSupported()) {
            es6promise.polyfill();
        }

        return typedArraysSupported;

        function isPromisesSupported() {
            if (typeof Promise !== "undefined" && Promise.toString().indexOf("[native code]") !== -1) {
                return true;
            }
            return false;
        }

        function isArrayBufferSupported() {
            if ('ArrayBuffer' in window && ArrayBuffer.toString().indexOf("[native code]") !== -1) {
                return true;
            }
            return false;
        }
    };
});
