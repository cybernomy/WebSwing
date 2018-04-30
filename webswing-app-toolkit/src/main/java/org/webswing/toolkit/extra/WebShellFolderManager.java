package org.webswing.toolkit.extra;

import org.webswing.Constants;
import org.webswing.toolkit.util.Logger;
import sun.awt.shell.ShellFolder;
import sun.awt.shell.ShellFolder.Invoker;
import sun.awt.shell.Win32ShellFolderManager2;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("restriction")
public class WebShellFolderManager extends Win32ShellFolderManager2 {

	private boolean windows;
	private Object defaultManager;

	private File root;
	private List<File> roots = new ArrayList<File>();

	public WebShellFolderManager() {
		String path = System.getProperty(Constants.SWING_START_SYS_PROP_TRANSFER_DIR, System.getProperty("user.dir") + "/upload");
		String[] paths = path.split(File.pathSeparator);
		for (int i = 0; i < paths.length; i++) {
			File root = new IsolatedRootFile(paths[i]);
			if (!root.getAbsoluteFile().exists()) {
				root.mkdirs();
			}
			roots.add(root);
			if (i == 0) {
				this.root = root;
				System.setProperty("user.home", root.getAbsolutePath());
			}
		}
		windows = System.getProperty("os.name", "").startsWith("Windows");

		try {
			Class<?> managerClass = ClassLoader.getSystemClassLoader().loadClass("sun.awt.shell.ShellFolderManager");
			Constructor<?> c = managerClass.getDeclaredConstructor();
			c.setAccessible(true);
			defaultManager = c.newInstance();
		} catch (Exception e) {
			System.err.println("Error while instantiating default shell folder manager. " + e.getMessage());
			e.printStackTrace();
		}
	}

	@Override
	public Object get(String paramString) {
		if (paramString.equals("fileChooserDefaultFolder")) {
			return ensureExists(root);
		}
		if (paramString.equals("roots")) {
			return ensureExists(roots.toArray(new File[roots.size()]));
		}
		if (paramString.equals("fileChooserComboBoxFolders")) {
			return ensureExists(roots.toArray(new File[roots.size()]));
		}
		if (paramString.equals("fileChooserShortcutPanelFolders")) {
			return ensureExists(roots.toArray(new File[roots.size()]));
		}
		if (paramString.startsWith("fileChooserIcon ") || paramString.startsWith("optionPaneIcon ") || paramString.startsWith("shell32Icon ")) {
			return super.get(paramString);
		}
		return null;
	}

	private File[] ensureExists(File[] roots) {
		for (File f : roots) {
			ensureExists(f);
		}
		return roots;
	}

	private File ensureExists(File f) {
		if (!f.getAbsoluteFile().exists()) {
			boolean done = f.mkdirs();
			if (done) {
				Logger.error("Isolated filesystem folder " + f.getAbsolutePath() + "not found. Make sure the folder is unique for each session (use ${user} variable) or disable the 'Clear Upload Folder' option in configuration.");
			} else {
				Logger.error("Isolated filesystem folder " + f.getAbsolutePath() + " could not be created. Make sure the path is valid and the process has write access.");
			}
		}
		return f;
	}

	@Override
	public ShellFolder createShellFolder(File paramFile) throws FileNotFoundException {
		try {
			if (isSubfolderOfRoots(paramFile)) {
				if (windows) {
					return super.createShellFolder(paramFile);
				} else {
					try {
						Method m = defaultManager.getClass().getDeclaredMethod("createShellFolder", File.class);
						m.setAccessible(true);
						return (ShellFolder) m.invoke(defaultManager, paramFile);
					} catch (Exception e) {
						System.err.println("Failed to invoke createShellFolder method on default shell folder manager: " + e.getMessage());
						e.printStackTrace();
						return null;
					}
				}
			} else {
				throw new FileNotFoundException("Path is outside the allowed Webswing Filesystem isolation folder. (" + paramFile.getCanonicalPath() + ")");
			}
		} catch (IOException e) {
			System.err.println("Error while creating ShellFolder. " + e.getMessage());
			if (e instanceof FileNotFoundException) {
				throw (FileNotFoundException) e;
			} else {
				throw new FileNotFoundException("Error while creating ShellFolder. " + e.getMessage());
			}
		}
	}

	private boolean isSubfolderOfRoots(File paramFile) throws IOException {
		String cp = paramFile.getCanonicalPath();
		for (File root : roots) {
			if (cp.startsWith(root.getCanonicalPath())) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected Invoker createInvoker() {
		if (windows) {
			return super.createInvoker();
		} else {
			try {
				Method m = defaultManager.getClass().getDeclaredMethod("createInvoker");
				m.setAccessible(true);
				return (Invoker) m.invoke(defaultManager);
			} catch (Exception e) {
				System.err.println("Failed to invoke createInvoker method on default shell folder manager: " + e.getMessage());
				e.printStackTrace();
				return null;
			}
		}
	}

	@Override
	public boolean isComputerNode(File paramFile) {
		if (windows) {
			return super.isComputerNode(paramFile);
		} else {
			try {
				Method m = defaultManager.getClass().getDeclaredMethod("isComputerNode", File.class);
				m.setAccessible(true);
				return (Boolean) m.invoke(defaultManager, paramFile);
			} catch (Exception e) {
				System.err.println("Failed to invoke isComputerNode method on default shell folder manager: " + e.getMessage());
				e.printStackTrace();
				return false;
			}
		}
	}

	@Override
	public boolean isFileSystemRoot(File paramFile) {
		try {
			for (File root : roots) {
				if (root.getCanonicalPath().equals(paramFile.getCanonicalPath())) {
					return true;
				}
			}
			return false;
		} catch (IOException e1) {
			if (windows) {
				return super.isFileSystemRoot(paramFile);
			} else {
				try {
					Method m = defaultManager.getClass().getDeclaredMethod("isFileSystemRoot", File.class);
					m.setAccessible(true);
					return (Boolean) m.invoke(defaultManager, paramFile);
				} catch (Exception e) {
					System.err.println("Failed to invoke isFileSystemRoot method on default shell folder manager: " + e.getMessage());
					e.printStackTrace();
					return false;
				}
			}
		}

	}

	@SuppressWarnings("rawtypes")
	public void sortFiles(List paramList) {
		if (windows) {
			try {
				Method m = super.getClass().getDeclaredMethod("sortFiles", List.class);
				m.setAccessible(true);
				m.invoke(defaultManager, paramList);
			} catch (Exception e) {
				System.err.println("Failed to invoke sortFiles method on default shell folder manager: " + e.getMessage());
				e.printStackTrace();
			}
		} else {
			try {
				Method m = defaultManager.getClass().getDeclaredMethod("sortFiles", List.class);
				m.setAccessible(true);
				m.invoke(defaultManager, paramList);
			} catch (Exception e) {
				System.err.println("Failed to invoke sortFiles method on default shell folder manager: " + e.getMessage());
				e.printStackTrace();
			}
		}
	}
}
