package org.webswing;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class JsonMsg implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1190136636718443351L;
	public List<String> originalImg=new ArrayList<String>();
	public List<String> protoImg=new ArrayList<String>();

	public int originalRenderTime;
	public int protoRenderTime;
	
	public int originalRenderSize;
	public int protoRenderSize;
}
