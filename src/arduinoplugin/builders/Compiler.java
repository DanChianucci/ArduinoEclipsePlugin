package arduinoplugin.builders;

/* -*- mode: java; c-basic-offset: 2; indent-tabs-mode: nil -*- */

/*
 Part of the Processing project - http://processing.org

 Copyright (c) 2004-08 Ben Fry and Casey Reas
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
//This file has been modified to work with the Eclipse Arduino Plugin
import java.io.*;
import java.util.*;

import arduinoplugin.Preprocess.Sketch;
import arduinoplugin.base.PluginBase;

public class Compiler implements MessageConsumer {

	String buildPath;
	String primaryClassName;
	boolean verbose;
	RunnerException exception;
	private static String MCU = null;
	private static String F_CPU = null;
	
	public Compiler() 
	{
	}

	/**
	 * Compile with avr-gcc.
	 * 
	 * @param sketch
	 *            Sketch object to be compiled.
	 * @param buildPath
	 *            Where the temporary files live and will be built from.
	 * @param primaryClassName
	 *            the name of the combined sketch file w/ extension
	 * @return true if successful.
	 * @throws RunnerException
	 *             Only if there's a problem. Only then.
	 */
	public boolean compile(Sketch sketch, String buildPath, String primaryClassName,
			boolean verbose) throws RunnerException {

		this.buildPath = buildPath;
		this.primaryClassName = primaryClassName;
		this.verbose = verbose;

		String avrBasePath = PluginBase.getAVRPath();
		MCU = PluginBase.getMCU();
		F_CPU = PluginBase.getFrequency();
		if (!boardSettingsFull()) {
			RunnerException re = new RunnerException(
					"No board selected; please choose a board from the Tools > Board menu.");
			re.hideStackTrace();
			throw re;
		}
		String corePath = PluginBase.getCorePath();

		List<File> objectFiles = new ArrayList<File>();

		// 0. include paths for core + all libraries
		List<String> includePaths = new ArrayList<String>();
		includePaths.add(corePath);

		for (File file : sketch.getImportedLibraries()) 
		{
			includePaths.add(file.getPath());
		}

		System.out.println("compile the sketch");
		// 1. compile the sketch (already in the buildPath)
		objectFiles.addAll(compileFiles(avrBasePath, buildPath, includePaths,
				findFilesInPath(buildPath, "S", false),
				findFilesInPath(buildPath, "c", false),
				findFilesInPath(buildPath, "cpp", false)));

		// 2. compile the libraries, outputting .o files to:
		// <buildPath>/<library>/
		System.out.println("compile the libraries, outputting .o");
		for (File libraryFolder : sketch.getImportedLibraries()) {
			File outputFolder = new File(buildPath, libraryFolder.getName());
			File utilityFolder = new File(libraryFolder, "utility");
			createFolder(outputFolder);
			// this library can use includes in its utility/ folder
			includePaths.add(utilityFolder.getAbsolutePath());
			objectFiles.addAll(compileFiles(avrBasePath,
					outputFolder.getAbsolutePath(), includePaths,
					findFilesInFolder(libraryFolder, "S", false),
					findFilesInFolder(libraryFolder, "c", false),
					findFilesInFolder(libraryFolder, "cpp", false)));
			
			outputFolder = new File(outputFolder, "utility");
			createFolder(outputFolder);
			objectFiles.addAll(compileFiles(avrBasePath,
					outputFolder.getAbsolutePath(), includePaths,
					findFilesInFolder(utilityFolder, "S", false),
					findFilesInFolder(utilityFolder, "c", false),
					findFilesInFolder(utilityFolder, "cpp", false)));
			// other libraries should not see this library's utility/ folder
			includePaths.remove(includePaths.size() - 1);
		}

		// 3. compile the core, outputting .o files to <buildPath> and then
		// collecting them into the core.a library file.
		System.out.println("Compile the core .o files");
		includePaths.clear();
		includePaths.add(corePath); // include path for core only
		List<File> coreObjectFiles = compileFiles(avrBasePath, buildPath,
				includePaths, findFilesInPath(corePath, "S", true),
				findFilesInPath(corePath, "c", true),
				findFilesInPath(corePath, "cpp", true));

		String runtimeLibraryName = buildPath + File.separator + "core.a";
		List<String> baseCommandAR = new ArrayList<String>(
				Arrays.asList(new String[] { avrBasePath + "avr-ar", "rcs",
						runtimeLibraryName }));
		for (File file : coreObjectFiles) {
			List<String> commandAR = new ArrayList<String>(baseCommandAR);
			commandAR.add(file.getAbsolutePath());
			execAsynchronously(commandAR);
		}

		// 4. link it all together into the .elf file
		System.out.println("link into elf");
		List<String> baseCommandLinker = new ArrayList<String>(
				Arrays.asList(new String[] { avrBasePath + "avr-gcc", "-O"+PluginBase.getOptimize(),
						"-Wl,--gc-sections", "-mmcu=" + MCU, "-o",
						buildPath + File.separator + primaryClassName + ".elf" }));

		for (File file : objectFiles) {
			baseCommandLinker.add(file.getAbsolutePath());
		}

		baseCommandLinker.add(runtimeLibraryName);
		baseCommandLinker.add("-L" + buildPath);
		baseCommandLinker.add("-lm");

		execAsynchronously(baseCommandLinker);

		List<String> baseCommandObjcopy = new ArrayList<String>(
				Arrays.asList(new String[] { avrBasePath + "avr-objcopy", "-O",
						"-R", }));

		List<String> commandObjcopy;

		// 5. extract EEPROM data (from EEMEM directive) to .eep file.
		System.out.println("Extract EEPROM to .eep");
		commandObjcopy = new ArrayList<String>(baseCommandObjcopy);
		commandObjcopy.add(2, "ihex");
		commandObjcopy.set(3, "-j");
		commandObjcopy.add(".eeprom");
		commandObjcopy.add("--set-section-flags=.eeprom=alloc,load");
		commandObjcopy.add("--no-change-warnings");
		commandObjcopy.add("--change-section-lma");
		commandObjcopy.add(".eeprom=0");
		commandObjcopy.add(buildPath + File.separator + primaryClassName
				+ ".elf");
		commandObjcopy.add(buildPath + File.separator + primaryClassName
				+ ".eep");
		execAsynchronously(commandObjcopy);

		// 6. build the .hex file
		System.out.println("Build the Hex file");
		commandObjcopy = new ArrayList<String>(baseCommandObjcopy);
		commandObjcopy.add(2, "ihex");
		commandObjcopy.add(".eeprom"); // remove eeprom data
		commandObjcopy.add(buildPath + File.separator + primaryClassName
				+ ".elf");
		commandObjcopy.add(buildPath + File.separator + primaryClassName
				+ ".hex");
		execAsynchronously(commandObjcopy);

		return true;
	}

	private boolean boardSettingsFull() {
		if (MCU != null && MCU != "" && F_CPU != null && F_CPU != "")
			return true;
		return false;
	}

	private List<File> compileFiles(String avrBasePath, String buildPath,
			List<String> includePaths, List<File> sSources,
			List<File> cSources, List<File> cppSources) throws RunnerException {

		List<File> objectPaths = new ArrayList<File>();

		for (File file : sSources) {
			String objectPath = buildPath + File.separator + file.getName()
					+ ".o";
			objectPaths.add(new File(objectPath));
			execAsynchronously(getCommandCompilerS(avrBasePath, includePaths,
					file.getAbsolutePath(), objectPath));
		}

		for (File file : cSources) {
			String objectPath = buildPath + File.separator + file.getName()
					+ ".o";
			objectPaths.add(new File(objectPath));
			execAsynchronously(getCommandCompilerC(avrBasePath, includePaths,
					file.getAbsolutePath(), objectPath));
		}

		for (File file : cppSources) {
			String objectPath = buildPath + File.separator + file.getName()
					+ ".o";
			objectPaths.add(new File(objectPath));
			execAsynchronously(getCommandCompilerCPP(avrBasePath, includePaths,
					file.getAbsolutePath(), objectPath));
		}

		return objectPaths;
	}

	boolean firstErrorFound;
	boolean secondErrorFound;

	/**
	 * Either succeeds or throws a RunnerException fit for public consumption.
	 */
	private void execAsynchronously(List<String> commandList)
			throws RunnerException {
		String[] command = new String[commandList.size()];
		commandList.toArray(command);
		int result = 0;

		if (verbose) {
			for (int j = 0; j < command.length; j++) {
				System.out.print(command[j] + " ");
			}
			System.out.println();
		}

		firstErrorFound = false; // haven't found any errors yet
		secondErrorFound = false;

		Process process;

		try {
			process = Runtime.getRuntime().exec(command);
		} catch (IOException e) {
			RunnerException re = new RunnerException(e.getMessage());
			re.hideStackTrace();
			throw re;
		}

		MessageSiphon in = new MessageSiphon(process.getInputStream(), this);
		MessageSiphon err = new MessageSiphon(process.getErrorStream(), this);

		// wait for the process to finish. if interrupted
		// before waitFor returns, continue waiting
		boolean compiling = true;
		while (compiling) {
			try {
				if (in.thread != null)
					in.thread.join();
				if (err.thread != null)
					err.thread.join();
				result = process.waitFor();
				// System.out.println("result is " + result);
				compiling = false;
			} catch (InterruptedException ignored) {
			}
		}

		// an error was queued up by message(), barf this back to compile(),
		// which will barf it back to Editor. if you're having trouble
		// discerning the imagery, consider how cows regurgitate their food
		// to digest it, and the fact that they have five stomaches.
		//
		// System.out.println("throwing up " + exception);
		if (exception != null) {
			throw exception;
		}

		if (result > 1) {
			// a failure in the tool (e.g. unable to locate a sub-executable)
			System.err.println(command[0] + " returned " + result);
		}

		if (result != 0) {
			RunnerException re = new RunnerException("Error compiling.");
			re.hideStackTrace();
			throw re;
		}
	}

	/**
	 * Part of the MessageConsumer interface, this is called whenever a piece
	 * (usually a line) of error message is spewed out from the compiler. The
	 * errors are parsed for their contents and line number, which is then
	 * reported back to Editor.
	 */
	/*
	 * public void message(String s) { int i;
	 * 
	 * // remove the build path so people only see the filename // can't use
	 * replaceAll() because the path may have characters in it // which // have
	 * meaning in a regular expression. if (!verbose) { while ((i =
	 * s.indexOf(buildPath + File.separator)) != -1) { s = s.substring(0, i) +
	 * s.substring(i + (buildPath + File.separator).length()); } }
	 * 
	 * // look for error line, which contains file name, line number, // and at
	 * least the first line of the error message String errorFormat =
	 * "([\\w\\d_]+.\\w+):(\\d+):\\s*error:\\s*(.*)\\s*"; String[] pieces =
	 * PApplet.match(s, errorFormat);
	 * 
	 * // if (pieces != null && exception == null) { // exception =
	 * sketch.placeException(pieces[3], pieces[1], //
	 * PApplet.parseInt(pieces[2]) - 1); // if (exception != null)
	 * exception.hideStackTrace(); // }
	 * 
	 * if (pieces != null) { RunnerException e =
	 * sketch.placeException(pieces[3], pieces[1], PApplet.parseInt(pieces[2]) -
	 * 1);
	 * 
	 * // replace full file path with the name of the sketch tab (unless //
	 * we're // in verbose mode, in which case don't modify the compiler output)
	 * if (e != null && !verbose) { SketchCode code =
	 * sketch.getCode(e.getCodeIndex()); String fileName = code
	 * .isExtension(sketch.getDefaultExtension()) ? code .getPrettyName() :
	 * code.getFileName(); s = fileName + ":" + e.getCodeLine() + ": error: " +
	 * e.getMessage(); }
	 * 
	 * if (pieces[3].trim().equals("SPI.h: No such file or directory")) { e =
	 * new RunnerException(
	 * "Please import the SPI library from the Sketch > Import Library menu.");
	 * s +=
	 * "\nAs of Arduino 0019, the Ethernet library depends on the SPI library."
	 * +
	 * "\nYou appear to be using it or another library that depends on the SPI library."
	 * ; }
	 * 
	 * if (exception == null && e != null) { exception = e;
	 * exception.hideStackTrace(); } }
	 * 
	 * System.err.print(s); }
	 */
	@Override
	public void message(String s) {

	}

	// ///////////////////////////////////////////////////////////////////////////

	static private List<String> getCommandCompilerS(String avrBasePath,
			List<String> includePaths, String sourceName, String objectName) {
		List<String> baseCommandCompiler = new ArrayList<String>(
				Arrays.asList(new String[] {
						avrBasePath + "avr-gcc",
						"-c", // compile, don't link
						"-g", // include debugging info (so errors include line
								// numbers)
						"-assembler-with-cpp", "-mmcu=" + MCU,
						"-DF_CPU=" + F_CPU, }));

		for (int i = 0; i < includePaths.size(); i++) {
			baseCommandCompiler.add("-I" + (String) includePaths.get(i));
		}

		baseCommandCompiler.add(sourceName);
		baseCommandCompiler.add("-o" + objectName);

		return baseCommandCompiler;
	}

	static private List<String> getCommandCompilerC(String avrBasePath,
			List<String> includePaths, String sourceName, String objectName) {

		List<String> baseCommandCompiler = new ArrayList<String>(
				Arrays.asList(new String[] { avrBasePath + "avr-gcc", "-c", // compile,
																			// don't
																			// link
						"-g", // include debugging info (so errors include line
								// numbers)
						"-O"+PluginBase.getOptimize(), // optimize for size
						"-w", // surpress all warnings
						"-ffunction-sections", // place each function in its own
												// section
						"-fdata-sections", "-mmcu=" + MCU, "-DF_CPU=" + F_CPU, }));

		for (int i = 0; i < includePaths.size(); i++) {
			baseCommandCompiler.add("-I" + (String) includePaths.get(i));
		}

		baseCommandCompiler.add(sourceName);
		baseCommandCompiler.add("-o" + objectName);

		return baseCommandCompiler;
	}

	static private List<String> getCommandCompilerCPP(String avrBasePath,
			List<String> includePaths, String sourceName, String objectName) {

		List<String> baseCommandCompilerCPP = new ArrayList<String>(
				Arrays.asList(new String[] { avrBasePath + "avr-g++", "-c", // compile,
																			// don't
																			// link
						"-g", // include debugging info (so errors include line
								// numbers)
						"-O"+PluginBase.getOptimize(), // optimize for size
						"-w", // surpress all warnings
						"-fno-exceptions", "-ffunction-sections", // place each
																	// function
																	// in its
																	// own
																	// section
						"-fdata-sections", "-mmcu=" + MCU, "-DF_CPU=" + F_CPU, }));

		for (int i = 0; i < includePaths.size(); i++) {
			baseCommandCompilerCPP.add("-I" + (String) includePaths.get(i));
		}

		baseCommandCompilerCPP.add(sourceName);
		baseCommandCompilerCPP.add("-o" + objectName);

		return baseCommandCompilerCPP;
	}

	// ///////////////////////////////////////////////////////////////////////////

	static private void createFolder(File folder) throws RunnerException {
		if (folder.isDirectory())
			return;
		if (!folder.mkdir())
			throw new RunnerException("Couldn't create: " + folder);
	}

	/**
	 * Given a folder, return a list of the header files in that folder (but not
	 * the header files in its sub-folders, as those should be included from
	 * within the header files at the top-level).
	 */
	static public String[] headerListFromIncludePath(String path) {
		FilenameFilter onlyHFiles = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".h");
			}
		};

		return (new File(path)).list(onlyHFiles);
	}

	static public ArrayList<File> findFilesInPath(String path,
			String extension, boolean recurse) {
		return findFilesInFolder(new File(path), extension, recurse);
	}

	static public ArrayList<File> findFilesInFolder(File folder,
			String extension, boolean recurse) {
		ArrayList<File> files = new ArrayList<File>();

		if (folder.listFiles() == null)
			return files;

		for (File file : folder.listFiles()) {
			if (file.getName().startsWith("."))
				continue; // skip hidden files

			if (file.getName().endsWith("." + extension))
				files.add(file);

			if (recurse && file.isDirectory()) {
				files.addAll(findFilesInFolder(file, extension, true));
			}
		}

		return files;
	}
}
