package org.webswing.ext.services;

import java.lang.reflect.Method;

import netscape.javascript.JSException;

import org.webswing.model.jslink.JavaEvalRequestMsgIn;
import org.webswing.model.jslink.JsParamMsg;
import org.webswing.model.jslink.JsResultMsg;

public interface JsLinkService {

	public JsParamMsg generateParam(Object arg) throws Exception;

	public JsResultMsg generateJavaResult(JavaEvalRequestMsgIn javaReq, Object result) throws Exception;

	public JsResultMsg generateJavaErrorResult(JavaEvalRequestMsgIn javaReq, Throwable result);

	public Object parseValue(JsParamMsg value) throws JSException;

	public Object[] getCompatibleParams(JavaEvalRequestMsgIn javaReq, Method m) throws Exception;

}
