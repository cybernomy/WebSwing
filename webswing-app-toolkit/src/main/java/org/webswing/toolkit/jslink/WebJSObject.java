package org.webswing.toolkit.jslink;

import java.applet.Applet;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeoutException;

import netscape.javascript.JSException;
import netscape.javascript.JSObject;

import org.webswing.Constants;
import org.webswing.model.jslink.JSObjectMsg;
import org.webswing.model.jslink.JavaEvalRequestMsgIn;
import org.webswing.model.s2c.AppFrameMsgOut;
import org.webswing.toolkit.util.DeamonThreadFactory;
import org.webswing.toolkit.util.JsLinkUtil;
import org.webswing.toolkit.util.Services;
import org.webswing.toolkit.util.WeakValueHashMap;

public class WebJSObject extends JSObject {

	private static final Map<String, WeakReference<JSObjectMsg>> jsGarbageCollectionMap = new HashMap<String, WeakReference<JSObjectMsg>>();
	private static final WeakValueHashMap<String, Object> javaReferences = new WeakValueHashMap<String, Object>();
	private static boolean jsLinkAllowed = Boolean.getBoolean(Constants.SWING_START_SYS_PROP_ALLOW_JSLINK);
	private static ScheduledExecutorService javaEvalThread = Executors.newSingleThreadScheduledExecutor(DeamonThreadFactory.getInstance("Webswing JsLing Processor"));
	private JSObjectMsg jsThis;

	public WebJSObject(JSObjectMsg jsThis) {
		this.jsThis = jsThis;
		if (jsThis != null) {
			synchronized (jsGarbageCollectionMap) {
				jsGarbageCollectionMap.put(jsThis.getId() + "", new WeakReference<JSObjectMsg>(jsThis));
			}
		}
	}

	@Override
	public Object call(String methodName, Object[] args) throws JSException {
		AppFrameMsgOut msg = JsLinkUtil.generateCallRequest(jsThis, methodName, args);
		return sendJsRequest(msg);
	}

	@Override
	public Object eval(String s) throws JSException {
		AppFrameMsgOut msg = JsLinkUtil.generateEvalRequest(jsThis, s);
		return sendJsRequest(msg);
	}

	@Override
	public Object getMember(String name) throws JSException {
		AppFrameMsgOut msg = JsLinkUtil.generateGetMemberRequest(jsThis, name);
		return sendJsRequest(msg);
	}

	@Override
	public void setMember(String name, Object value) throws JSException {
		AppFrameMsgOut msg = JsLinkUtil.generateSetMemberRequest(jsThis, name, value);
		sendJsRequest(msg);
	}

	@Override
	public void removeMember(String name) throws JSException {
		AppFrameMsgOut msg = JsLinkUtil.generateRemoveMemberRequest(jsThis, name);
		sendJsRequest(msg);
	}

	@Override
	public Object getSlot(int index) throws JSException {
		AppFrameMsgOut msg = JsLinkUtil.generateGetSlotRequest(jsThis, index);
		return sendJsRequest(msg);
	}

	@Override
	public void setSlot(int index, Object value) throws JSException {
		AppFrameMsgOut msg = JsLinkUtil.generateSetSlotRequest(jsThis, index, value);
		sendJsRequest(msg);
	}

	public static JSObject getWindow(Applet paramApplet) throws JSException {
		return new WebJSObject(null);
	}

	private static Object sendJsRequest(AppFrameMsgOut msg) {
		try {
			Object result = Services.getConnectionService().sendObjectSync(msg, msg.getJsRequest().getCorrelationId());
			Object parsedResult = JsLinkUtil.parseResponse(result);
			return parsedResult;
		} catch (TimeoutException e) {
			throw new JSException(e.getMessage());
		} catch (Exception e) {
			throw new JSException(e.getMessage());
		}
	}

	public static List<String> getGarbage() {
		ArrayList<String> result = new ArrayList<String>();
		synchronized (jsGarbageCollectionMap) {
			for (Iterator<String> i = jsGarbageCollectionMap.keySet().iterator(); i.hasNext();) {
				String key = i.next();
				if (jsGarbageCollectionMap.get(key).isEnqueued()) {
					result.add(key);
					i.remove();
				}
			}
		}
		return result;
	}

	public JSObjectMsg getThisId() {
		return jsThis;
	}

	public static String createJavaReference(Object arg) {
		return createJavaReference(arg, UUID.randomUUID().toString());
	}

	public static String createJavaReference(Object arg, String newId) {
		if (javaReferences.containsValue(arg)) {
			String id = null;
			for (String key : javaReferences.keySet()) {
				if (javaReferences.get(key) == arg) {
					id = key;
				}
			}
			return id;
		} else {
			String id = newId;
			javaReferences.put(id, arg);
			return id;
		}
	}

	public static Object getJavaReference(String id) {
		Object o = javaReferences.get(id);
		return o;
	}

	public static Future<?> evaluateJava(final JavaEvalRequestMsgIn javaReq) {
		return javaEvalThread.submit(new Runnable() {

			@Override
			public void run() {
				if (jsLinkAllowed) {
					Object javaRef = javaReferences.get(javaReq.getObjectId());
					AppFrameMsgOut result = JsLinkUtil.callMatchingMethod(javaReq, javaRef);
					Services.getConnectionService().sendObject(result);
				} else {
					Serializable result = JsLinkUtil.getErrorResponse(javaReq, "JsLink is not allowed for this application. Set the 'allowJsLink' to true in webswing.config to enable it.");
					Services.getConnectionService().sendObject(result);
				}
			}
		});

	}
}
