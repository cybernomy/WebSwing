package org.webswing;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class RecordingsServlet extends HttpServlet {
	/**
	 *
	 */
	private static final long serialVersionUID = 3917004355300462665L;
	private static final int DEFAULT_BUFFER_SIZE = 10240; // 10KB.

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String fileName = request.getParameter("file");
		if (fileName == null || fileName.isEmpty()) {
			List<String> files = new ArrayList<String>();
			scanFolder(new File(System.getProperty("path")), files);
			response.getWriter().write(files.toString());
		} else {
			File file = new File(fileName);
			response.reset();
			response.setBufferSize(DEFAULT_BUFFER_SIZE);
			response.setContentType("application/octet-stream");
			response.setHeader("Content-Length", String.valueOf(file.length()));
			BufferedInputStream input = null;
			BufferedOutputStream output = null;

			try {
				input = new BufferedInputStream(new FileInputStream(file), DEFAULT_BUFFER_SIZE);
				output = new BufferedOutputStream(response.getOutputStream(), DEFAULT_BUFFER_SIZE);

				byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
				int length;
				while ((length = input.read(buffer)) > 0) {
					output.write(buffer, 0, length);
				}
			} finally {
				close(output);
				close(input);
			}
		}
	}

	private static void close(Closeable resource) {
		if (resource != null) {
			try {
				resource.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	static void scanFolder(File folder, List<String> all) throws IOException {
		File[] children = folder.listFiles();
		if (children != null) {
			for (File child : children) {
				if (child.getName().endsWith(".wss") && child.isFile()) {
					all.add("\"" + child.getCanonicalPath().replaceAll("\\\\", "/") + "\"");
				}
				if (child.isDirectory()) {
					scanFolder(child, all);
				}
			}
		}
	}
}
