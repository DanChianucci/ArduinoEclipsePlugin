package arduinoplugin.base;

import java.io.File;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;

public class PluginBase {
	/**
	 * modified from findFileInFolder from arduino Compiler.java
	 * 
	 * @param folder
	 *            the folder to look in
	 * @param name
	 *            the name of the file being searched for
	 * @param recurse
	 *            whether to look in sub folders also
	 * @return the found file, or null if not found
	 */
	private static File findFileInFolder(File folder, String name,
			boolean recurse) {
		// if no files in that folder
		if (folder.listFiles() == null) {
			return null;
		}

		File found = null;

		for (File file : folder.listFiles()) {
			if (file.getName().startsWith("."))
				continue; // skip hidden files
			if (found != null) // && found.getName().endsWith(name))
			{ // barf up found to top recursive level
				return found;
			}
			// If file has same name
			if (file.getAbsolutePath().endsWith(name)) {
				found = file;
				return found;
			}
			// If folder has another folder in it look in there also
			// unless it is an examples folder
			// TODO might run into trouble with people naming projects examples
			// or bin
			if (recurse && file.isDirectory()
					&& !file.getPath().endsWith("examples")
					&& !file.getPath().endsWith("bin")) {
				found = findFileInFolder(file, name, true);
			}

		}
		return found;
	}

	public static String getArduinoLibPath() {
		return getArduinoPath() + "libraries";
	}
	
	public static String getBoardProgPath()
	{
		return getHardwarePath()+"arduino"+File.separator;
		
	}

	public static String getArduinoPath() {
		return SettingsManager.getSetting("ArduinoPath",null)+File.separator;//PATH + File.separator;
	}

	public static String getAVRLibCPath() {
		return getHardwarePath() + "tools" + File.separator + "avr"
				+ File.separator + "avr" + File.separator + "include";
	}

	public static String getAVRPath() {
		return getHardwarePath() + "tools" + File.separator + "avr"
				+ File.separator + "bin" + File.separator;
	}



	/*public static String getBoardType() {
		return BOARD_TYPE;
	}*/

	public static String getCorePath() {
		return getHardwarePath() + "arduino" + File.separator + "cores"
				+ File.separator + "arduino" + File.separator;
	}

	/*public static String getFrequency() {
		return FREQ;
	}*/

	public static String getHardwarePath() {
		return getArduinoPath() + "hardware" + File.separator;
	}

	/**
	 * Searches in the Arduino libraries folder and the Workspace's Libraries
	 * Folder to find the correct header file
	 * <p>
	 * TODO make library finder more efficient
	 * 
	 * @param imported
	 *            the imported file to be found
	 * @param the
	 *            current project folder
	 * @return the File corresponding to the folder in which the header resides
	 */
	public static File getLibraryFolder(String imported, File project) {
		// replace forward and backslash chars with default file separator
		imported = imported.replace('/', File.separatorChar);
		imported = imported.replace('\\', File.separatorChar);

		// Lookin Workspacelib
		File lookinFolder = new File(getWorkspaceLibPath());
		File foundFile = findFileInFolder(lookinFolder, imported, true);

		// look in arduino libs
		if (foundFile == null) {
			lookinFolder = new File(getArduinoLibPath());
			foundFile = findFileInFolder(lookinFolder, imported, true);
		}

		if (foundFile != null)
			return foundFile.getParentFile();
		else
			return null;

	}

	/*public static String getMCU() {
		return MCU;
	}*/

	/*public static String getOptimize() {
		return OPT;
	}*/

	public static String[] getProcessorArray() {
		return new String[] { "atmega128", "atmega1280", "atmega1281",
				"atmega1284p", "atmega16", "atmega163", "atmega164p",
				"atmega165", "atmega165p", "atmega168", "atmega169",
				"atmega169p", "atmega2560", "atmega2561", "atmega32",
				"atmega324p", "atmega325", "atmega3250", "atmega328",
				"atmega328p", "atmega329", "atmega3290", "atmega48",
				"atmega64", "atmega640", "atmega644", "atmega644p",
				"atmega645", "atmega6450", "atmega649", "atmega6490",
				"atmega8", "atmega8515", "atmega8535", "atmega88" };
	}

	/*public static String getUploadBaud() {
		return UPLOAD_BAUD;
	}*/

	/*public static String getUploadProtocall() {
		return UPLOAD_PROT;
	}*/

	private static String getWorkspaceLibPath() {
		File file = new File(ResourcesPlugin.getWorkspace().getRoot()
				.getLocation().toOSString(), "Libraries");
		return file.getAbsolutePath();
	}

	/**
	 * copied from arduino base.java Recursively remove all files within a
	 * directory, used with removeDir(), or when the contents of a dir should be
	 * removed, but not the directory itself. (i.e. when cleaning temp files
	 * from lib/build)
	 */
	static public void removeDescendants(File dir) {
		if (!dir.exists())
			return;

		String files[] = dir.list();
		for (int i = 0; i < files.length; i++) {
			if (files[i].equals(".") || files[i].equals(".."))
				continue;
			File dead = new File(dir, files[i]);
			if (!dead.isDirectory()) {
				if (!dead.delete()) {
					// temporarily disabled
					System.err.println("Could not delete " + dead);
				}
			} else {
				removeDir(dead);
			}
		}
	}

	/**
	 * Copied from arduino base.java Remove all files in a directory and the
	 * directory itself.
	 */
	static public void removeDir(File dir) {
		if (dir.exists()) {
			removeDescendants(dir);
			if (!dir.delete()) {
				System.err.println("Could not delete " + dir);
			}
		}
	}

	public PluginBase() {
	}

	
	//IS OD taken from processings base.java

	  static public boolean isMacOS() {
	    //return PApplet.platform == PConstants.MACOSX;
	    return System.getProperty("os.name").indexOf("Mac") != -1;
	  }

	  static public boolean isWindows() {
	    //return PApplet.platform == PConstants.WINDOWS;
	    return System.getProperty("os.name").indexOf("Windows") != -1;
	  }

	  static public boolean isLinux() {
	    //return PApplet.platform == PConstants.LINUX;
	    return System.getProperty("os.name").indexOf("Linux") != -1;
	  }
	  
	  static public IProject getProject()
	  {
		return ResourcesPlugin.getWorkspace().getRoot().getProject();
		  
	  }
}
