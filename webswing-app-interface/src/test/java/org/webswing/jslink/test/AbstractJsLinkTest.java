package org.webswing.jslink.test;

import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.concurrent.ExecutionException;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.junit.Before;
import org.webswing.Constants;
import org.webswing.ext.services.JsLinkService;
import org.webswing.ext.services.ServerConnectionService;
import org.webswing.model.c2s.InputEventsFrameMsgIn;
import org.webswing.model.jslink.JavaEvalRequestMsgIn;
import org.webswing.services.impl.JsLinkServiceImpl;
import org.webswing.toolkit.jslink.WebJSObject;
import org.webswing.toolkit.util.Services;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import netscape.javascript.JSObject;

public abstract class AbstractJsLinkTest {
	static {
		System.setProperty(Constants.SWING_START_SYS_PROP_ALLOW_JSLINK, "true");
	}
	private static final ObjectMapper mapper = new ObjectMapper();
	ScriptEngine engine = null;
	JSObject root = new WebJSObject(null);

	public abstract void specificSetUp();

	@Before
	public void setUp() throws Exception {
		JsLinkService jsLinkServiceImpl = JsLinkServiceImpl.getInstance();
		ScriptEngineManager factory = new ScriptEngineManager();
		final ScriptEngine engine = factory.getEngineByName("JavaScript");
		this.engine = engine;
		ServerConnectionService serverServiceImpl = new ServerConnectionService() {

			@Override
			public Object sendObjectSync(Serializable o, String correlationId) throws IOException {
				try {
					engine.put("data", mapper.writeValueAsString(o));
					engine.eval("data=JSON.parse(data)");
					engine.eval("api.jslink.process(data.jsRequest)");
					return mapper.readValue((String) engine.eval("JSON.stringify(result)"), InputEventsFrameMsgIn.class).getJsResponse();
				} catch (Exception e) {
					throw new IOException(e);
				}
			}

			@Override
			public void sendObject(Serializable jsonPaintRequest) {
				try {
					engine.put("data", mapper.writeValueAsString(jsonPaintRequest));
					engine.eval("data=JSON.parse(data)");
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}

			@Override
			public void disconnect() {
				
			}

			@Override
			public void resetInactivityTimers() {

			}

			@Override
			public void messageApiPublish(Serializable o) throws IOException {

			}
		};
		Services.initialize(null, null, serverServiceImpl, null, null, jsLinkServiceImpl);

		engine.eval("var define =function(array,f){ this['JsLink']=f()}");
		engine.eval("self=this; this.setTimeout=function(f,t){f()}");
		engine.eval(new FileReader("../webswing-server/webswing-server-war/src/main/webapp/javascript/es6promise.js"));
		engine.eval("ES6Promise.polyfill()");
		engine.eval(new FileReader("../webswing-server/webswing-server-war/src/main/webapp/javascript/webswing-jslink.js"));
		engine.put("sendJava", this);
		engine.eval("var result=null;var window={test:'test'};");
		engine.eval("var cfg={javaCallTimeout:0}");
		engine.eval("var send=function(obj){result=obj;return result;}");
		engine.eval("var awaitResponse=function(callback, request, timeout){sendJava.send(JSON.stringify(request.javaRequest));callback(data.javaResponse);}");
		engine.eval("var jslModule=new JsLink();");
		engine.eval("jslModule.injects.cfg=cfg;");
		engine.eval("jslModule.injects.external={};");
		engine.eval("jslModule.injects.send=send;");
		engine.eval("jslModule.injects.awaitResponse=awaitResponse;");
		engine.eval("var api= {jslink:jslModule.provides}");

		specificSetUp();
	}

	public void send(String obj) throws JsonParseException, JsonMappingException, IOException {
		try {
			WebJSObject.evaluateJava(mapper.readValue(obj, JavaEvalRequestMsgIn.class)).get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
	}

}
