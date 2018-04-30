package netscape.javascript;

import java.applet.Applet;

import org.webswing.toolkit.jslink.WebJSObject;

public abstract class JSObject {
	public abstract Object call(String paramString, Object[] paramArrayOfObject) throws JSException;

	public abstract Object eval(String paramString) throws JSException;

	public abstract Object getMember(String paramString) throws JSException;

	public abstract void setMember(String paramString, Object paramObject) throws JSException;

	public abstract void removeMember(String paramString) throws JSException;

	public abstract Object getSlot(int paramInt) throws JSException;

	public abstract void setSlot(int paramInt, Object paramObject) throws JSException;

	public static JSObject getWindow(Applet paramApplet) throws JSException {
		return new WebJSObject(null);
	}
}
