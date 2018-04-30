package org.webswing.toolkit;

import sun.awt.datatransfer.DataTransferer;
import sun.awt.datatransfer.ToolkitThreadBlockedHandler;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

@SuppressWarnings("restriction")
public class WebDataTransfer extends DataTransferer {
	private static WebDataTransfer transfer;
	private final ToolkitThreadBlockedHandler handler = new WebToolkitThreadBlockedHandler();

	public static WebDataTransfer getInstanceImpl() {
		if (transfer == null) {
			synchronized (WebDataTransfer.class) {
				if (transfer == null) {
					transfer = new WebDataTransfer();
				}
			}
		}
		return transfer;
	}

	@Override
	public String getDefaultUnicodeEncoding() {
		return "utf-16";
	}

	@Override
	public boolean isLocaleDependentTextFormat(long paramLong) {
		return false;
	}

	@Override
	public boolean isFileFormat(long paramLong) {
		return false;
	}

	@Override
	public boolean isImageFormat(long paramLong) {
		return false;
	}

	@Override
	protected Long getFormatForNativeAsLong(String paramString) {
		return 1l;
	}

	@Override
	protected String getNativeForFormat(long paramLong) {
		return "";
	}

	@Override
	protected String[] dragQueryFile(byte[] paramArrayOfByte) {
		return null;
	}

	protected Image platformImageBytesToImage(byte[] bytes, long l) throws IOException {
		return null;
	}

	protected Image platformImageBytesOrStreamToImage(InputStream paramInputStream, byte[] paramArrayOfByte, long paramLong) throws IOException {
		return null;
	}

	@Override
	protected byte[] imageToPlatformBytes(Image paramImage, long paramLong) throws IOException {
		return null;
	}

	@Override
	public ToolkitThreadBlockedHandler getToolkitThreadBlockedHandler() {
		return handler;
	}

	protected ByteArrayOutputStream convertFileListToBytes(ArrayList<String> arg0) throws IOException {
		return null;
	}
}
