package org.webswing.toolkit.util;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.naming.OperationNotSupportedException;

import netscape.javascript.JSException;

import org.webswing.model.jslink.JSObjectMsg;
import org.webswing.model.jslink.JavaEvalRequestMsgIn;
import org.webswing.model.jslink.JsEvalRequestMsgOut;
import org.webswing.model.jslink.JsEvalRequestMsgOut.JsEvalRequestType;
import org.webswing.model.jslink.JsParamMsg;
import org.webswing.model.jslink.JsResultMsg;
import org.webswing.model.s2c.AppFrameMsgOut;
import org.webswing.toolkit.jslink.WebJSObject;

public class JsLinkUtil {

	public static AppFrameMsgOut generateEvalRequest(JSObjectMsg jsThis, String s) {
		JsEvalRequestMsgOut msg = new JsEvalRequestMsgOut();
		msg.setType(JsEvalRequestType.eval);
		msg.setEvalString(s);
		return generateAppFrame(msg);
	}

	public static AppFrameMsgOut generateCallRequest(JSObjectMsg jsThis, String methodName, Object[] args) {
		try {
			JsEvalRequestMsgOut msg = new JsEvalRequestMsgOut();
			msg.setType(JsEvalRequestType.call);
			msg.setThisObjectId(jsThis == null ? null : jsThis.getId());
			msg.setEvalString(methodName);
			if (args != null && args.length > 0) {
				List<JsParamMsg> params = new ArrayList<JsParamMsg>();
				for (Object arg : args) {
					params.add(Services.getJsLinkService().generateParam(arg));
				}
				msg.setParams(params);
			}
			return generateAppFrame(msg);
		} catch (Exception e) {
			Logger.error("Failed to generate js Call request:", e);
			throw new JSException("Failed to generate js Call request:" + e.getMessage());
		}
	}

	public static AppFrameMsgOut generateGetMemberRequest(JSObjectMsg jsThis, String name) {
		JsEvalRequestMsgOut msg = new JsEvalRequestMsgOut();
		msg.setType(JsEvalRequestType.getMember);
		msg.setEvalString(name);
		msg.setThisObjectId(jsThis == null ? null : jsThis.getId());
		return generateAppFrame(msg);
	}

	public static AppFrameMsgOut generateSetMemberRequest(JSObjectMsg jsThis, String name, Object value) {
		try {
			JsEvalRequestMsgOut msg = new JsEvalRequestMsgOut();
			msg.setType(JsEvalRequestType.setMember);
			msg.setThisObjectId(jsThis == null ? null : jsThis.getId());
			msg.setEvalString(name);
			if (value != null) {
				List<JsParamMsg> params = new ArrayList<JsParamMsg>();
				params.add(Services.getJsLinkService().generateParam(value));
				msg.setParams(params);
			}
			return generateAppFrame(msg);
		} catch (Exception e) {
			Logger.error("Failed to generate js Call request:", e);
			throw new JSException("Failed to generate js Call request:" + e.getMessage());
		}
	}

	public static AppFrameMsgOut generateRemoveMemberRequest(JSObjectMsg jsThis, String name) {
		JsEvalRequestMsgOut msg = new JsEvalRequestMsgOut();
		msg.setType(JsEvalRequestType.deleteMember);
		msg.setThisObjectId(jsThis == null ? null : jsThis.getId());
		msg.setEvalString(name);
		return generateAppFrame(msg);
	}

	public static AppFrameMsgOut generateSetSlotRequest(JSObjectMsg jsThis, int index, Object value) {
		AppFrameMsgOut msg = generateSetMemberRequest(jsThis, "" + index, value);
		msg.getJsRequest().setType(JsEvalRequestType.setSlot);
		return msg;
	}

	public static AppFrameMsgOut generateGetSlotRequest(JSObjectMsg jsThis, int index) {
		AppFrameMsgOut msg = generateGetMemberRequest(jsThis, "" + index);
		msg.getJsRequest().setType(JsEvalRequestType.getSlot);
		return msg;
	}

	public static Object parseResponse(Object result) throws JSException {
		if (result instanceof JsResultMsg) {
			JsResultMsg param = (JsResultMsg) result;
			if (param.getError() != null) {
				throw new JSException(param.getError());
			} else {
				if (param.getValue() != null) {
					JsParamMsg value = param.getValue();
					return Services.getJsLinkService().parseValue(value);
				} else {
					return null;
				}
			}
		}
		return null;
	}

	private static AppFrameMsgOut generateAppFrame(JsEvalRequestMsgOut msg) {
		AppFrameMsgOut msgout = new AppFrameMsgOut();
		msgout.setJsRequest(msg);
		msg.setGarbageIds(WebJSObject.getGarbage());
		msg.setCorrelationId(UUID.randomUUID().toString());
		return msgout;
	}

	private static AppFrameMsgOut generateAppFrame(JsResultMsg msg) {
		AppFrameMsgOut msgout = new AppFrameMsgOut();
		msgout.setJavaResponse(msg);
		return msgout;
	}

	public static AppFrameMsgOut getErrorResponse(JavaEvalRequestMsgIn javaReq, String error) {
		return generateAppFrame(Services.getJsLinkService().generateJavaErrorResult(javaReq, new OperationNotSupportedException(error)));
	}

	public static AppFrameMsgOut callMatchingMethod(JavaEvalRequestMsgIn javaReq, Object javaRef) {
		if (javaRef != null) {
			Exception exception = null;
			List<Method> candidates = new ArrayList<Method>();
			for (Method m : javaRef.getClass().getMethods()) {
				int requestParamCount = javaReq.getParams() != null ? javaReq.getParams().size() : 0;
				if (m.getName().equals(javaReq.getMethod()) && m.getParameterTypes().length == requestParamCount) {
					candidates.add(m);
				}
			}
			if (candidates.size() > 0) {
				for (Method m : candidates) {
					try {
						Object[] params = Services.getJsLinkService().getCompatibleParams(javaReq, m);
						try {
							Object result = m.invoke(javaRef, params);
							return generateAppFrame(Services.getJsLinkService().generateJavaResult(javaReq, result));
						} catch (Exception invEx) {
							Logger.error("Error while calling java from javascript:", invEx);
							return generateAppFrame(Services.getJsLinkService().generateJavaErrorResult(javaReq, invEx));
						}
					} catch (Exception x) {
						exception = new RuntimeException("Parameters of method " + javaReq.getMethod() + " are not compatible with request.", x);
					}
				}
			} else {
				exception = new RuntimeException("Public method named " + javaReq.getMethod() + " with " + javaReq.getParams().size() + " parameters not found in class " + javaRef.getClass().getCanonicalName());
			}
			Logger.error("Error while calling java from javascript:", exception);
			return generateAppFrame(Services.getJsLinkService().generateJavaErrorResult(javaReq, exception));
		} else {
			NullPointerException e = new NullPointerException("Caller object reference is not valid any more.  Make sure you keep a reference to Java objects sent to Javascript to prevent from being garbage collected. ");
			Logger.error("Error while calling java from javascript:", e);
			return generateAppFrame(Services.getJsLinkService().generateJavaErrorResult(javaReq, e));
		}
	}
}
