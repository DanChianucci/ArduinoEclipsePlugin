package arduinoplugin.base;

import java.io.File;
import org.eclipse.core.resources.ResourcesPlugin;
public class PluginBase 
{	
	private static String MCU = "";
	private static String FREQ = "";
	private static String PATH = "";
	private static String BOARD_TYPE = "";
	private static String OPT = "";
	private static String UPLOAD_PROT = "";
	private static String UPLOAD_BAUD = "";
	



	public PluginBase()
	{
	}
	
	
 	/**modified from findFileInFolder from arduino Compiler.java
	 * 
	 * @param folder the folder to look in
	 * @param name the name of the file being searched for
	 * @param recurse whether to look in sub folders also
	 * @return the found file, or null if not found
	 */
	private static File findFileInFolder(File folder, String name,
			boolean recurse) {
		// if no files in that folder
		if (folder.listFiles() == null) 
		{
			return null;
		}

		File found=null;

		//TODO break out of the look completely if found==null
		for (File file : folder.listFiles()) 
		{
			if (file.getName().startsWith("."))
				continue; // skip hidden files			
			if(found!= null) //&& found.getName().endsWith(name))
			{	//barf up found to top recursive level
				return found;
			}
			// If file has same name
			if (file.getAbsolutePath().endsWith(name)) 
			{
				found = file;
				return found;
			}
			// If folder has another folder in it look in there also
			// unless it is an examples folder
			//TODO might run into trouble with people naming projects examples
			//or bin
			if (recurse && file.isDirectory() && !file.getPath().endsWith("examples") && !file.getPath().endsWith("bin")) {
				found = findFileInFolder(file, name, true);
			}
			

		}
			return found;
	}
	
 	public static String getArduinoLibPath()
 	{
 		return getArduinoPath()+"libraries";
 	}
 	
 	//TODO read from settings file
	public static String getArduinoPath()
	{
		return PATH+File.separator;
	}
 	public static String getAVRLibCPath()
	{
		return getHardwarePath()+"tools"+ File.separator+"avr"+ File.separator+"avr"+File.separator+"include";
	}
	public static String getAVRPath()
	{
		return getHardwarePath()+"tools"+ File.separator+"avr"+ File.separator+"bin"+File.separator;
	}
	public static String getCorePath() {
		return getHardwarePath()+"arduino"+File.separator+"cores"+File.separator+"arduino"+File.separator;
	}
	private static String getWorkspaceLibPath()
 	{
 		File file = new File(ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString(),
 				"Libraries");
 		return file.getAbsolutePath();
 	}
	public static String getHardwarePath()
	{
		return getArduinoPath()+"hardware"+File.separator;
	}
	
	public static String[] getBoardsArray() {
		// TODO read from boards.txt
		return new String[] {"ArduinoUNO","Custom"};
	}
	public static String[] getProcessorArray() {
		return new String[] {"atmega328p", "atmega168","atmega2456", "atmega1256"};
	}
	
	
	public static String getBoardType(){
		return BOARD_TYPE;
	}
	public static String getFrequency() 
	{

		return FREQ;
	}

	
     /**Searches in the Arduino libraries folder
	 * and the Workspace's Libraries Folder to find the correct 
	 * header file
	 * TODO clean this up maybe have to add librariy folder of libc
	 * @param imported the imported file to be found
	 * @param the current project folder
	 * @return the File corresponding to the folder in which the header resides
	 */
	public static File getLibraryFolder(String imported, File project)
	{
		//replace forward and backslash chars with default file separator
		imported = imported.replace('/', File.separatorChar);
		imported = imported.replace('\\',File.separatorChar);
		
		//Lookin Workspacelib
		File lookinFolder = new File(getWorkspaceLibPath());
		File foundFile = findFileInFolder(lookinFolder,imported, true);	

		//look in arduino libs
		if(foundFile == null)
		{
			lookinFolder = new File(getArduinoLibPath());
			foundFile = findFileInFolder(lookinFolder,imported, true);
		}

		if(foundFile!=null)
			return foundFile.getParentFile();	
		else
			return null;
		
	}
	
	public static String getMCU() 
	{
		return MCU;
	}
	public static String getOptimize(){
		return OPT;
	}
	public static String getUploadBaud(){
		return UPLOAD_BAUD;
	}
	public static String getUploadProtocall(){
		return UPLOAD_PROT;
	}
	
	//TODO need to write to a file instead of local variable
	public static void setArduinoPath(String nPATH){
		PATH=nPATH;
	}
	public static void setBoardType(String nBT)
	{
		BOARD_TYPE=nBT;
	}
	public static void setFreq(String nFREQ)
	{
		if(BOARD_TYPE.equalsIgnoreCase("Custom"))
			FREQ=nFREQ;
		else
			System.out.println("With Board Type set to "+BOARD_TYPE+" Frequency is locked to "+FREQ);
	}
	public static void setMCU(String nMCU)
	{
		if(BOARD_TYPE.equalsIgnoreCase("Custom"))
			MCU=nMCU;
		else
			System.out.println("With Board Type set to "+BOARD_TYPE+" MCU is locked to "+MCU);
	}
	public static void setOptimize(String nOPT)
	{
		OPT=nOPT;
	}	
	public static void setUploadBaud(String nUB){
		if(BOARD_TYPE.equalsIgnoreCase("Custom"))
			UPLOAD_BAUD=nUB;
		else
			System.out.println("With Board Type set to "+BOARD_TYPE+" Upload Baud is locked to "+UPLOAD_BAUD);
	}
	public static void setUploadProtocall(String nUP)
	{
		if(BOARD_TYPE.equalsIgnoreCase("Custom"))
			UPLOAD_PROT=nUP;
		else
			System.out.println("With Board Type set to "+BOARD_TYPE+" Upload Protocall is locked to "+UPLOAD_PROT);
	}
	
	
	  /**
	   * Copied from arduino base.java
	   * Remove all files in a directory and the directory itself.
	   */
	  static public void removeDir(File dir) {
	    if (dir.exists()) {
	      removeDescendants(dir);
	      if (!dir.delete()) {
	        System.err.println("Could not delete " + dir);
	      }
	    }
	  }
	  
	  /**
	   * copied from arduino base.java
	   * Recursively remove all files within a directory,
	   * used with removeDir(), or when the contents of a dir
	   * should be removed, but not the directory itself.
	   * (i.e. when cleaning temp files from lib/build)
	   */
	  static public void removeDescendants(File dir) {
	    if (!dir.exists()) return;

	    String files[] = dir.list();
	    for (int i = 0; i < files.length; i++) {
	      if (files[i].equals(".") || files[i].equals("..")) continue;
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
}



