package org.webswing.services.impl;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.webswing.ext.services.JsLinkService;
import org.webswing.model.jslink.JavaEvalRequestMsgIn;
import org.webswing.model.jslink.JavaObjectRefMsg;
import org.webswing.model.jslink.JsParamMsg;
import org.webswing.model.jslink.JsResultMsg;
import org.webswing.toolkit.jslink.WebJSObject;

import com.fasterxml.jackson.databind.ObjectMapper;

import netscape.javascript.JSException;

public class JsLinkServiceImpl implements JsLinkService {
	private static final ObjectMapper mapper = new ObjectMapper();
	private static final Class<?>[] supportedPrimitives = { Number.class, String.class, Boolean.class, Byte.class, Character.class, Number[].class, String[].class, Boolean[].class, Byte[].class, Character[].class, int[].class, byte[].class, short[].class, char[].class, int[].class, long[].class, float[].class, double[].class, byte[].class };

	private static JsLinkServiceImpl impl;

	public static JsLinkServiceImpl getInstance() {
		if (impl == null) {
			impl = new JsLinkServiceImpl();
		}
		return impl;
	}

	private JsLinkServiceImpl() {
	}

	public JsParamMsg generateParam(Object arg) throws Exception {
		return generateParam(arg, true);
	}

	private JsParamMsg generateParam(Object arg, boolean includeArrays) throws Exception {
		JsParamMsg result = new JsParamMsg();
		if (arg != null) {
			if (isPrimitive(arg)) {
				result.setPrimitive(mapper.writeValueAsString(arg));
			} else if (arg instanceof WebJSObject) {
				WebJSObject jsobj = (WebJSObject) arg;
				result.setJsObject(jsobj.getThisId());
			} else if (arg instanceof Iterable<?> && includeArrays) {
				List<JsParamMsg> array = new ArrayList<JsParamMsg>();
				for (Iterator<?> i = ((Iterable<?>) arg).iterator(); i.hasNext();) {
					Object o = i.next();
					array.add(generateParam(o, false));
				}
				result.setArray(array);
			} else if (arg.getClass().isArray() && includeArrays) {
				List<JsParamMsg> array = new ArrayList<JsParamMsg>();
				for (Iterator<?> i = Arrays.asList((Object[]) arg).iterator(); i.hasNext();) {
					Object o = i.next();
					array.add(generateParam(o, false));
				}
				result.setArray(array);
			} else {
				JavaObjectRefMsg ref = toJavaObjectRef(arg);
				result.setJavaObject(ref);
			}
		}
		return result;
	}

	public Object parseValue(JsParamMsg value) throws JSException {
		try {
			if (value == null) {
				return null;
			} else if (value.getPrimitive() != null) {
				return mapper.readValue(value.getPrimitive(), Object.class);
			} else if (value.getJsObject() != null) {
				return new WebJSObject(value.getJsObject());
			} else if (value.getJavaObject() != null) {
				Object obj = WebJSObject.getJavaReference(value.getJavaObject().getId());
				if (obj == null) {
					throw new JSException("Reffered Java object not found. Make sure you keep a reference to Java objects sent to Javascript to prevent from being garbage collected.");
				}
				return obj;
			} else if (value.getArray() != null) {
				Object[] array = new Object[value.getArray().size()];
				for (int i = 0; i < array.length; i++) {
					array[i] = parseValue(value.getArray().get(i));
				}
				return array;
			} else {
				return null;
			}
		} catch (Exception e) {
			throw new JSException(ExceptionUtils.getStackTrace(e));
		}
	}

	public Object[] getCompatibleParams(JavaEvalRequestMsgIn javaReq, Method m) throws Exception {
		Object[] params = new Object[m.getParameterTypes().length];
		for (int i = 0; i < m.getParameterTypes().length; i++) {
			Class<?> type = m.getParameterTypes()[i];
			Object value = parseValue(javaReq.getParams().get(i));
			if (ClassUtils.isAssignable(value == null ? null : value.getClass(), type, true)) {
				params[i] = value;
			} else if (value instanceof String) {
				try {
					params[i] = mapper.readValue((String) value, type);
				} catch (Exception e) {
					throw new RuntimeException("Method " + m + " has incompatible parameter " + i + ". Expected " + type.getName() + ". Error when reading as json:" + e.getLocalizedMessage());
				}
			} else {
				throw new RuntimeException("Method " + m + " has incompatible parameter " + i + ". Expected " + type.getName() + ", got " + (value == null ? "null" : value.getClass().getName()));
			}
		}
		return params;
	}

	public JsResultMsg generateJavaResult(JavaEvalRequestMsgIn javaReq, Object result) throws Exception {
		JsResultMsg msg = new JsResultMsg();
		msg.setCorrelationId(javaReq.getCorrelationId());
		msg.setValue(generateParam(result));
		return msg;
	}

	public JsResultMsg generateJavaErrorResult(JavaEvalRequestMsgIn javaReq, Throwable result) {
		JsResultMsg msg = new JsResultMsg();
		msg.setCorrelationId(javaReq.getCorrelationId());
		msg.setError(ExceptionUtils.getStackTrace(result));
		return msg;
	}

	private static boolean isPrimitive(Object arg) {
		Class<? extends Object> argClass = arg.getClass();
		for (Class<? extends Object> c : supportedPrimitives) {
			if (c.isAssignableFrom(argClass)) {
				return true;
			}
		}
		return false;
	}

	private static JavaObjectRefMsg toJavaObjectRef(Object arg) {
		JavaObjectRefMsg result = new JavaObjectRefMsg();
		Set<String> methods = new HashSet<String>();
		for (Method m : arg.getClass().getMethods()) {
			methods.add(m.getName());
		}
		result.setMethods(new ArrayList<String>(methods));
		String id = WebJSObject.createJavaReference(arg);
		result.setId(id);
		return result;
	}
}
