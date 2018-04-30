define([], function amdFactory() {

    return function JsLinkModule() {
        var module = this;
        var api;
        module.injects = api = {
            cfg: 'webswing.config',
            external: 'external',
            send: 'socket.send',
            awaitResponse: 'socket.awaitResponse'
        };
        module.provides = {
            process: process
        };
        module.ready = function () {
            referenceCache['instanceObject'] = api.external;
        };

        var idMemberName = '__webswing_jslink_id';
        var referenceCache = {};

        function process(jsRequest) {
            var response;
            try {
                if (jsRequest.type === 'eval') {
                    var indirectEval = eval;
                    var result = indirectEval(jsRequest.evalString);
                    response = buildResponse(result, jsRequest.correlationId);
                } else if (jsRequest.type === 'call') {
                    var ref = jsRequest.thisObjectId != null ? referenceCache[jsRequest.thisObjectId] : window;
                    var args = decodeParams(jsRequest);
                    var result = ref[jsRequest.evalString].apply(ref, args);
                    response = buildResponse(result, jsRequest.correlationId);
                } else if (jsRequest.type === 'setMember' || jsRequest.type === 'setSlot') {
                    var ref = jsRequest.thisObjectId != null ? referenceCache[jsRequest.thisObjectId] : window;
                    var args = decodeParams(jsRequest);
                    ref[jsRequest.evalString] = args != null ? args[0] : null;
                    response = buildResponse(null, jsRequest.correlationId);
                } else if (jsRequest.type === 'getMember' || jsRequest.type === 'getSlot') {
                    var ref = jsRequest.thisObjectId != null ? referenceCache[jsRequest.thisObjectId] : window;
                    response = buildResponse(ref[jsRequest.evalString], jsRequest.correlationId);
                } else if (jsRequest.type === 'deleteMember') {
                    var ref = jsRequest.thisObjectId != null ? referenceCache[jsRequest.thisObjectId] : window;
                    delete ref[jsRequest.evalString];
                    response = buildResponse(null, jsRequest.correlationId);
                }
                if (jsRequest.garbageIds != null) {
                    jsRequest.garbageIds.forEach(function (id) {
                        delete referenceCache[id];
                    });
                }
                api.send(response);
            } catch (e) {
                response = buildResponse(e, jsRequest.correlationId);
                api.send(response);
                throw e;
            }
        }

        function buildResponse(obj, correlationId) {
            var result = {
                jsResponse: serializeObject(obj)
            };
            result.jsResponse.correlationId = correlationId;
            return result;
        }

        function serializeObject(object) {
            var jsResponse = {};
            if (object == null) {
                return jsResponse;
            } else if (Object.prototype.toString.call(object) === '[object Error]') {
                jsResponse.error = object.toString();
                return jsResponse;
            } else {
                jsResponse.value = {};
                if (typeof object === 'number' || typeof object === 'string' || typeof object === 'boolean') {
                    jsResponse.value.primitive = JSON.stringify(object);
                } else if (object instanceof JavaObjectRef) {
                    jsResponse.value.javaObject = {
                        id: object.id
                    };
                } else {
                    if (object[idMemberName] == null) {
                        object[idMemberName] = GUID();
                        referenceCache[object[idMemberName]] = object;
                    }
                    jsResponse.value.jsObject = {
                        id: object[idMemberName]
                    };
                }
                return jsResponse;
            }
        }

        function decodeParams(jsRequest) {
            var result = [];
            var args = jsRequest.params;
            if (args != null) {
                for (var i = 0; i < args.length; i++) {
                    result.push(decodeJsParam(args[i]));
                }
            }
            return result;
        }

        function decodeJsParam(param) {
            if (param.primitive != null) {
                return JSON.parse(param.primitive);
            } else if (param.jsObject != null) {
                return referenceCache[param.jsObject.id];
            } else if (param.javaObject != null) {
                return new JavaObjectRef(param.javaObject);
            } else if (param.array != null) {
                var array = [];
                for (var j = 0; j < param.array.length; j++) {
                    array.push(decodeJsParam(param.array[j]));
                }
                return array;
            }
            return null;
        }

        function GUID() {
            var S4 = function () {
                return Math.floor(Math.random() * 0x10000).toString(16);
            };
            return (S4() + S4() + S4());
        }

        function JavaObjectRef(javaRefMsg) {
            this.id = javaRefMsg.id;
            if (javaRefMsg.methods != null) {
                for (var i = 0; i < javaRefMsg.methods.length; i++) {
                    var methodName = javaRefMsg.methods[i];
                    this[methodName] = function (m) {
                        return function () {
                            var currentArguments = arguments;
                            return new Promise(function (resolve, reject) {
                                var jCorrelationId = GUID();
                                var params = [];
                                for (var i = 0; i < currentArguments.length; i++) {
                                    var serializedObject = serializeObject(currentArguments[i]);
                                    params[i] = serializedObject != null ? serializedObject.value : null;
                                }
                                var request = {
                                    javaRequest: {
                                        correlationId: jCorrelationId,
                                        objectId: javaRefMsg.id,
                                        method: m,
                                        params: params
                                    }
                                };

                                api.awaitResponse(function (result) {
                                    if (Object.prototype.toString.call(result) === '[object Error]') {
                                        reject(result);
                                    } else if (result.error != null) {
                                        reject(new Error(result.error));
                                    } else if (result.value != null) {
                                        resolve(decodeJsParam(result.value));
                                    }
                                }, request, jCorrelationId, api.cfg.javaCallTimeout);
                            });
                        };
                    }(methodName);
                }
            }
        }
    };
});