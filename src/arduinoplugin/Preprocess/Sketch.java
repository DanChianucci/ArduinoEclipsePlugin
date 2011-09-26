/* -*- mode: java; c-basic-offset: 2; indent-tabs-mode: nil -*- */

/*
 Part of the Processing project - http://processing.org

 Copyright (c) 2004-10 Ben Fry and Casey Reas
 Copyright (c) 2001-04 Massachusetts Institute of Technology

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software Foundation,
 Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package arduinoplugin.Preprocess;

import java.io.*;
import java.lang.reflect.Array;
import java.util.*;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;

import arduinoplugin.base.PluginBase;
import arduinoplugin.base.SettingsManager;
import arduinoplugin.builders.RunnerException;
import arduinoplugin.builders.Compiler;
import arduinoplugin.builders.Sizer;
import arduinoplugin.pages.SettingKeys;
import arduinoplugin.uploader.AvrdudeUploader;
import arduinoplugin.uploader.SerialException;
import arduinoplugin.uploader.Uploader;

/**
 * Stores information about files in the current sketch
 */
public class Sketch {

	/** The build folder...not temporary in */
	static private File OutputFolder;

	/** List of library folders. */
	ArrayList<File> importedLibraries;

	/** main pde file for this sketch. */
	private File primaryFile;
	

	/** folder that contains this sketch */
	private File folder;

	/**
	 * Name of sketch, which is the name of main file (without .pde extension)
	 */
	private String name;

	/**
	 * Number of sketchCode objects (tabs) in the current sketch. Note that this
	 * will be the same as code.length, because the getCode() method returns
	 * just the code[] array, rather than a copy of it, or an array that's been
	 * resized to just the relevant files themselves.
	 * http://dev.processing.org/bugs/show_bug.cgi?id=940
	 */
	private int codeCount;
	private File[] code;
	
	private IProject project;


	/**
	 * path is location of the main .pde file, because this is also simplest to
	 * use when opening the file from the finder/explorer.
	 * 
	 * BuildPath is the output folder for all compiled objects
	 * 
	 */
	public Sketch(String path, String BuildPath) throws IOException {

		primaryFile = new File(path);
		project = ResourcesPlugin.getWorkspace().getRoot().getProject(primaryFile.getParentFile().getName()); 
		
		// get the name of the sketch by chopping .pde or .java
		// off of the main file name
		String mainFilename = primaryFile.getName();
		int suffixLength = getDefaultExtension().length() + 1;
		name = mainFilename.substring(0, mainFilename.length() - suffixLength);
		OutputFolder = new File(BuildPath);
		folder = new File(primaryFile.getParent());
		load();
	}
	
	public Sketch()
	{
		
	}

	/**
	 * Build the list of files.
	 * <P>
	 * Generally this is only done once, rather than each time a change is made,
	 * because otherwise it gets to be a nightmare to keep track of what files
	 * went where, because not all the data will be saved to disk.
	 * <P>
	 * This also gets called when the main sketch file is renamed, because the
	 * sketch has to be reloaded from a different folder.
	 * <P>
	 * Another exception is when an external editor is in use, in which case the
	 * load happens each time "run" is hit.
	 */
	protected void load() {

		// get list of files in the sketch folder
		String list[] = folder.list();

		// reset these because load() may be called after an
		// external editor event. (fix for 0099)
		codeCount = 0;

		code = new File[list.length];

		String[] extensions = getExtensions();

		for (String filename : list) {
			// Ignoring the dot prefix files is especially important to avoid
			// files
			// with the ._ prefix on Mac OS X. (You'll see this with Mac files
			// on
			// non-HFS drives, i.e. a thumb drive formatted FAT32.)
			if (filename.startsWith(".")) //$NON-NLS-1$
				continue;

			// Don't let some wacko name a directory blah.pde or bling.java.
			if (new File(folder, filename).isDirectory())
				continue;

			// figure out the name without any extension
			String base = filename;
			// now strip off the .pde and .java extensions
			for (String extension : extensions) {
				if (base.toLowerCase().endsWith("." + extension)) { //$NON-NLS-1$
					base = base.substring(0,
							base.length() - (extension.length() + 1));

					// Don't allow people to use files with invalid names, since
					// on load,
					// it would be otherwise possible to sneak in nasty
					// filenames. [0116]
					if (Sketch.isSanitaryName(base)) {
						code[codeCount++] = new File(folder, filename);
					}
				}
			}
		}
		// Remove any code that wasn't proper
		code = (File[]) subset(code, 0, codeCount);

		// move the main class to the first tab
		// start at 1, if it's at zero, don't bother
		for (int i = 1; i < codeCount; i++) {
			// if (code[i].file.getName().equals(mainFilename)) {
			if (code[i].equals(primaryFile)) {
				File temp = code[0];
				code[0] = code[i];
				code[i] = temp;
				break;
			}
		}

		// sort the entries at the top
		sortCode();
	}

	protected void sortCode() {
		// cheap-ass sort of the rest of the files
		// it's a dumb, slow sort, but there shouldn't be more than ~5 files
		for (int i = 1; i < codeCount; i++) {
			int who = i;
			for (int j = i + 1; j < codeCount; j++) {
				if (code[j].getName().compareTo(code[who].getName()) < 0) {
					who = j; // this guy is earlier in the alphabet
				}
			}
			if (who != i) { // swap with someone if changes made
				File temp = code[who];
				code[who] = code[i];
				code[i] = temp;
			}
		}
	}

	// Copied from PApplet in arduino source code
	static public Object subset(Object list, int start, int count) {
		Class<?> type = list.getClass().getComponentType();
		Object outgoing = Array.newInstance(type, count);
		System.arraycopy(list, start, outgoing, 0, count);
		return outgoing;
	}

	/**
	 * Build all the code for this sketch.
	 * 
	 * In an advanced program, the returned class name could be different, which
	 * is why the className is set based on the return value. A compilation
	 * error will burp up a RunnerException.
	 * 
	 * 
	 * @param buildPath
	 *            Location to copy all the .java files
	 * @return null if compilation failed, main class name if not
	 */
	public String preprocess(String buildPath) throws RunnerException {
		return preprocess(buildPath, new PdePreprocessor());
	}

	public String preprocess(String buildPath, PdePreprocessor preprocessor)
			throws RunnerException {

		String[] codeFolderPackages = null;
		// String classPath = buildPath;

		// 1. concatenate all .pde files to the 'main' pde
		// store line number for starting point of each code bit

		StringBuffer bigCode = new StringBuffer();
		// int bigCount = 0;
		for (File sc : code) {
			// if extension is ".pde"
			if (getExtensionOfFile(sc).equals(getDefaultExtension())) 
			{
				// TODO sc.setPreprocOffset(bigCount);
				// getProgram returns the text of the program as a string
				try {
					bigCode.append(getProgram(sc));
				} catch (IOException e) {
					e.printStackTrace();
				}
				bigCode.append('\n');
				// number of nlines in the file + 1... base.java
				// countlines(String program)
				// TODO bigCount += sc.getLineCount();
			}
		}

		// Note that the headerOffset isn't applied until compile and run,
		// because
		// it only applies to the code after it's been written to the .java
		// file.
		@SuppressWarnings("unused")
		int headerOffset = 0;
		// PdePreprocessor preprocessor = new PdePreprocessor();
		try {
			headerOffset = preprocessor.writePrefix(bigCode.toString(),
					buildPath, name, codeFolderPackages);
		} catch (FileNotFoundException fnfe) {
			fnfe.printStackTrace();
			String msg = "Build folder disappeared or could not be written"; //$NON-NLS-1$
			throw new RunnerException(msg);
		}

		// 2. run preproc on that code using the sugg class name
		// to create a single .java file and write to buildpath

		String primaryClassName = null;

		try {
			// if (i != 0) preproc will fail if a pde file is not
			// java mode, since that's required
			String className = preprocessor.write();

			if (className == null) {
				throw new RunnerException("Could not find main class"); //$NON-NLS-1$
				// this situation might be perfectly fine,
				// (i.e. if the file is empty)
				// System.out.println("No class found in " + code[i].name);
				// System.out.println("(any code in that file will be ignored)");
				// System.out.println();

				// } else {
				// code[0].setPreprocName(className + ".java");
			}

			// store this for the compiler and the runtime
			primaryClassName = className + ".cpp"; //$NON-NLS-1$

		} catch (FileNotFoundException fnfe) {
			fnfe.printStackTrace();
			String msg = "Build folder disappeared or could not be written"; //$NON-NLS-1$
			throw new RunnerException(msg);
		} catch (RunnerException pe) {
			// RunnerExceptions are caught here and re-thrown, so that they
			// don't
			// get lost in the more general "Exception" handler below.
			throw pe;

		} catch (Exception ex) {
			// XXX better method for handling this?
			System.err.println("Uncaught exception type:" + ex.getClass()); //$NON-NLS-1$
			ex.printStackTrace();
			throw new RunnerException(ex.toString());
		}

		// grab the imports from the code just preproc'd

		importedLibraries = new ArrayList<File>();

		for (String item : preprocessor.getExtraImports()) {
			File libFolder = PluginBase.getLibraryFolder(item,folder);			
			if (libFolder != null && !importedLibraries.contains(libFolder)) {
				importedLibraries.add(libFolder);
			}
		}

		// 3. then loop over the code[] and save each .java file

		for (File sc : code) {
			if (getExtensionOfFile(sc).equals("c") //$NON-NLS-1$
					|| getExtensionOfFile(sc).equals("cpp") //$NON-NLS-1$
					|| getExtensionOfFile(sc).equals("h")) { //$NON-NLS-1$
				// no pre-processing services necessary for java files
				// just write the the contents of 'program' to a .java file
				// into the build directory. uses byte stream and reader/writer
				// shtuff so that unicode bunk is properly handled
				String filename = sc.getName(); // code[i].name + ".java";
				try {
					copyFile(sc, new File(buildPath,filename));
				} catch (IOException e) {
					e.printStackTrace();
					throw new RunnerException("Problem moving " + filename //$NON-NLS-1$
							+ " to the build folder"); //$NON-NLS-1$
				}
				// TODO sc.setPreprocName(filename);

			} else if (getExtensionOfFile(sc).equals("pde")) { //$NON-NLS-1$
				// The compiler and runner will need this to have a proper
				// offset
				// TODO sc.addPreprocOffset(headerOffset);
			}
		}
		return primaryClassName;
	}

	//Copied from base.java in arduino source
	static public void copyFile(File sourceFile,
			File targetFile) throws IOException {
		InputStream from =
				new BufferedInputStream(new FileInputStream(sourceFile));
		OutputStream to =
				new BufferedOutputStream(new FileOutputStream(targetFile));
		byte[] buffer = new byte[16 * 1024];
		int bytesRead;
		while ((bytesRead = from.read(buffer)) != -1) {
			to.write(buffer, 0, bytesRead);
		}
		to.flush();
		from.close(); // ??
		from = null;
		to.close(); // ??
		to = null;

		targetFile.setLastModified(sourceFile.lastModified());
	}

	// returns the string representing the file's code
	// taken from
	// http://stackoverflow.com/questions/326390/how-to-create-a-java-string-from-the-contents-of-a-file
	private String getProgram(File sc) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(sc));
		String line = null;
		StringBuilder stringBuilder = new StringBuilder();
		String ls = System.getProperty("line.separator"); //$NON-NLS-1$
		while ((line = reader.readLine()) != null) {
			stringBuilder.append(line);
			stringBuilder.append(ls);
		}
		return stringBuilder.toString();

	}

	// Returns the Extension of the file
	private String getExtensionOfFile(File f) {
		String path = f.getName();
		int i = path.lastIndexOf('.');
		if (i == -1)// no extension
			return ""; //$NON-NLS-1$
		// returns the extension without the .
		path = path.substring(i + 1, path.length());
		return path;
	}

	public ArrayList<File> getImportedLibraries() {
		return importedLibraries;
	}

	/**
	 * Map an error from a set of processed .java files back to its location in
	 * the actual sketch.
	 * 
	 * @param message
	 *            The error message.
	 * @param filename
	 *            The .java file where the exception was found.
	 * @param line
	 *            Line number of the .java file for the exception (0-indexed!)
	 * @return A RunnerException to be sent to the editor, or null if it wasn't
	 *         possible to place the exception to the sketch code.
	 */
	/*
	 * public RunnerException placeException(String message, String
	 * dotJavaFilename, int dotJavaLine) { int codeIndex = 0; //-1; int codeLine
	 * = -1;
	 * 
	 * // System.out.println("placing " + dotJavaFilename + " " + dotJavaLine);
	 * // System.out.println("code count is " + getCodeCount());
	 * 
	 * // first check to see if it's a .java file for (int i = 0; i <
	 * getCodeCount(); i++) { SketchCode code = getCode(i); if
	 * (!code.isExtension(getDefaultExtension())) { if
	 * (dotJavaFilename.equals(code.getFileName())) { codeIndex = i; codeLine =
	 * dotJavaLine; return new RunnerException(message, codeIndex, codeLine); }
	 * } }
	 * 
	 * // If not the preprocessed file at this point, then need to get out if
	 * (!dotJavaFilename.equals(name + ".cpp")) { return null; }
	 * 
	 * // if it's not a .java file, codeIndex will still be 0 // this section
	 * searches through the list of .pde files codeIndex = 0; for (int i = 0; i
	 * < getCodeCount(); i++) { SketchCode code = getCode(i);
	 * 
	 * if (code.isExtension(getDefaultExtension())) { //
	 * System.out.println("preproc offset is " + code.getPreprocOffset()); //
	 * System.out.println("looking for line " + dotJavaLine); if
	 * (code.getPreprocOffset() <= dotJavaLine) { codeIndex = i; //
	 * System.out.println("i'm thinkin file " + i); codeLine = dotJavaLine -
	 * code.getPreprocOffset(); } } } // could not find a proper line number, so
	 * deal with this differently. // but if it was in fact the .java file we're
	 * looking for, though, // send the error message through. // this is
	 * necessary because 'import' statements will be at a line // that has a
	 * lower number than the preproc offset, for instance. // if (codeLine == -1
	 * && !dotJavaFilename.equals(name + ".java")) { // return null; // } return
	 * new RunnerException(message, codeIndex, codeLine); }
	 */

	/**
	 * Run the build inside the temporary build folder.
	 * 
	 * @return null if compilation failed, main class name if not
	 * @throws RunnerException
	 */
	public String build(boolean verbose) throws RunnerException {
		return build(OutputFolder.getAbsolutePath(), verbose);
	}

	/**
	 * Preprocess and compile all the code for this sketch.
	 * 
	 * In an advanced program, the returned class name could be different, which
	 * is why the className is set based on the return value. A compilation
	 * error will burp up a RunnerException.
	 * 
	 * @return null if compilation failed, main class name if not
	 */
	public String build(String buildPath, boolean verbose)
			throws RunnerException {

		// run the preprocessor
		String primaryClassName = preprocess(buildPath);

		// compile the program. errors will happen as a RunnerException
		// that will bubble up to whomever called build().
		Compiler compiler = new Compiler(project);
		System.out.println("calling compiler"); //$NON-NLS-1$
		if (compiler.compile(this, buildPath, primaryClassName, verbose)) {
			size(buildPath, primaryClassName);
			return primaryClassName;
		}
		return null;
	}

	protected void size(String buildPath, String suggestedClassName)
			throws RunnerException {
		long size = 0;
		String maxsizeString = SettingsManager.getSetting(SettingKeys.uploadMaxSizeKey,project);
		if (maxsizeString == null)
			return;
		long maxsize = Integer.parseInt(maxsizeString);
		Sizer sizer = new Sizer(buildPath, suggestedClassName);
		try {
			size = sizer.computeSize();
			System.out.println("Binary sketch size: " + size + " bytes (of a " //$NON-NLS-1$ //$NON-NLS-2$
					+ maxsize + " byte maximum)"); //$NON-NLS-1$
		} catch (RunnerException e) {
			System.err.println("Couldn't determine program size: " //$NON-NLS-1$
					+ e.getMessage());
		}

		if (size > maxsize)
			throw new RunnerException(
					"Sketch too big; see http://www.arduino.cc/en/Guide/Troubleshooting#size for tips on reducing it."); //$NON-NLS-1$
	}

	/*
	 * protected String upload(String buildPath, String suggestedClassName,
	 * boolean verbose) throws RunnerException, SerialException {
	 * 
	 * Uploader uploader;
	 * 
	 * // download the program // uploader = new AvrdudeUploader(); boolean
	 * success = uploader.uploadUsingPreferences(buildPath, suggestedClassName,
	 * verbose);
	 * 
	 * return success ? suggestedClassName : null; }
	 */

	/**
	 * Replace all commented portions of a given String as spaces. Utility
	 * function used here and in the preprocessor.
	 */
	static public String scrubComments(String what) {
		char p[] = what.toCharArray();

		int index = 0;
		while (index < p.length) {
			// for any double slash comments, ignore until the end of the line
			if ((p[index] == '/') && (index < p.length - 1)
					&& (p[index + 1] == '/')) {
				p[index++] = ' ';
				p[index++] = ' ';
				while ((index < p.length) && (p[index] != '\n')) {
					p[index++] = ' ';
				}

				// check to see if this is the start of a new multiline comment.
				// if it is, then make sure it's actually terminated somewhere.
			} else if ((p[index] == '/') && (index < p.length - 1)
					&& (p[index + 1] == '*')) {
				p[index++] = ' ';
				p[index++] = ' ';
				boolean endOfRainbow = false;
				while (index < p.length - 1) {
					if ((p[index] == '*') && (p[index + 1] == '/')) {
						p[index++] = ' ';
						p[index++] = ' ';
						endOfRainbow = true;
						break;

					} else {
						// continue blanking this area
						p[index++] = ' ';
					}
				}
				if (!endOfRainbow) {
					throw new RuntimeException(
							"Missing the */ from the end of a " //$NON-NLS-1$
									+ "/* comment */"); //$NON-NLS-1$
				}
			} else { // any old character, move along
				index++;
			}
		}
		return new String(p);
	}

	/**
	 * Returns the default extension for this editor setup.
	 */
	public String getDefaultExtension() {
		return "pde"; //$NON-NLS-1$
	}

	/**
	 * Returns a String[] array of proper extensions.
	 */
	public String[] getExtensions() {
		return new String[] { "pde", "c", "cpp", "h" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	}

	/**
	 * Return true if the name is valid for a Processing sketch.
	 */
	static public boolean isSanitaryName(String name) {
		return sanitizeName(name).equals(name);
	}

	/**
	 * Produce a sanitized name that fits our standards for likely to work.
	 * <p/>
	 * Java classes have a wider range of names that are technically allowed
	 * (supposedly any Unicode name) than what we support. The reason for going
	 * more narrow is to avoid situations with text encodings and converting
	 * during the process of moving files between operating systems, i.e.
	 * uploading from a Windows machine to a Linux server, or reading a FAT32
	 * partition in OS X and using a thumb drive.
	 * <p/>
	 * This helper function replaces everything but A-Z, a-z, and 0-9 with
	 * underscores. Also disallows starting the sketch name with a digit.
	 */
	static public String sanitizeName(String origName) {
		char c[] = origName.toCharArray();
		StringBuffer buffer = new StringBuffer();

		// can't lead with a digit, so start with an underscore
		if ((c[0] >= '0') && (c[0] <= '9')) {
			buffer.append('_');
		}
		for (int i = 0; i < c.length; i++) {
			if (((c[i] >= '0') && (c[i] <= '9'))
					|| ((c[i] >= 'a') && (c[i] <= 'z'))
					|| ((c[i] >= 'A') && (c[i] <= 'Z'))) {
				buffer.append(c[i]);

			} else {
				buffer.append('_');
			}
		}
		// let's not be ridiculous about the length of filenames.
		// in fact, Mac OS 9 can handle 255 chars, though it can't really
		// deal with filenames longer than 31 chars in the Finder.
		// but limiting to that for sketches would mean setting the
		// upper-bound on the character limit here to 25 characters
		// (to handle the base name + ".class")
		if (buffer.length() > 63) {
			buffer.setLength(63);
		}
		return buffer.toString();
	}
	
	  public String upload(String buildPath, String suggestedClassName, boolean verbose,IProject proj)
			    throws RunnerException, SerialException {

			    Uploader uploader;

			    // download the program
			    //
			    uploader = new AvrdudeUploader();
			    suggestedClassName = suggestedClassName.substring(0, suggestedClassName.indexOf(".pde")); //$NON-NLS-1$
			    suggestedClassName = suggestedClassName +".cpp"; //$NON-NLS-1$
			    boolean success = uploader.uploadUsingPreferences(buildPath,
			                                                      suggestedClassName,
			                                                      verbose,proj);

			    return success ? suggestedClassName : null;
			  }
}
