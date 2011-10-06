/* -*- mode: java; c-basic-offset: 2; indent-tabs-mode: nil -*- */

/*
 PdeKeywords - handles text coloring and links to html reference
 Part of the Processing project - http://processing.org

 Copyright (c) 2004-06 Ben Fry and Casey Reas
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

package arduinoplugin.editors;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;

import arduinoplugin.Preprocess.PdePreprocessor;
import arduinoplugin.Preprocess.Sketch;
import arduinoplugin.base.PluginBase;

public class PdeKeywords {

	// used to mark a string literal (eg, C mode uses this to mark "..."
	// literals)
	public static final String LITERAL1 = "LITERAL1";
	// literal (eg, Java mode uses this to mark true, false, etc)
	public static final String LITERAL2 = "LITERAL2";
	// This should be used for general language constructs.
	public static final String KEYWORD1 = "KEYWORD1";
	// preprocessor commands, or variables.
	public static final String KEYWORD2 = "KEYWORD2";
	// Data types
	public static final String KEYWORD3 = "KEYWORD3";

	// lookup table that maps keywords to their html reference pages
	static Map<String, String> keywordToReference;
	protected static IFile setFile;
	IProject project;

	public PdeKeywords(IProject proj) 
	{
		project = proj;
	}

	/**
	 * Handles loading of keywords file.
	 * <P>
	 * Uses getKeywords() method because that's part of the TokenMarker classes.
	 * <P>
	 * It is recommended that a # sign be used for comments inside keywords.txt.
	 */
	public Map<String, String> getKeywords() {

		keywordToReference = new Hashtable<String, String>();
		String importRegexp = "^\\s*#include\\s+[<\"](\\S+)[\">]";
	    List<String> programImports = new ArrayList<String>();
	    
	    //TODO get the file that is in the editor instead of this non working thing
	    String n=ResourcesPlugin.getWorkspace().getRoot().getProjects()[0].getName();
	    File file = new File(ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString(),n+File.separator+n+".pde");
	    
	    String program = "";	    
	    if(file.exists())
			try {
				program=Sketch.getProgram((File) file);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
	    	
	    String[][] pieces = PdePreprocessor.matchAll(program, importRegexp);

	    if (pieces != null)
	      for (int i = 0; i < pieces.length; i++)
	        programImports.add(pieces[i][1]);  // the package name
		
		for (String lib : programImports) 
		{
			File libFolder = PluginBase.getLibraryFolder(lib, new File(project.getLocation().toOSString()));
			File keywords = new File(libFolder, "keywords.txt");
			if (keywords.exists())
				try {
					getKeywords(new FileInputStream(keywords));
				} catch (Exception e) {
					e.printStackTrace();
				}
		}
		return keywordToReference;
	}

	static private void getKeywords(InputStream input) throws Exception {
		InputStreamReader isr = new InputStreamReader(input);
		BufferedReader reader = new BufferedReader(isr);

		String line = null;
		while ((line = reader.readLine()) != null) 
		{
			String pieces[] = split(line, '\t');
			if (pieces.length >= 2) {
				String keyword = pieces[0].trim();
				String coloring = pieces[1].trim();

				if (coloring.length() > 0) {
					String t = coloring;

					keywordToReference.put(keyword, t);
				}
			}
		}
		reader.close();
	}

	/**
	 * Split a string into pieces along a specific character. Most commonly used
	 * to break up a String along a space or a tab character.
	 * <P>
	 * This operates differently than the others, where the single delimeter is
	 * the only breaking point, and consecutive delimeters will produce an empty
	 * string (""). This way, one can split on tab characters, but maintain the
	 * column alignments (of say an excel file) where there are empty columns.
	 */
	static public String[] split(String what, char delim) {
		// do this so that the exception occurs inside the user's
		// program, rather than appearing to be a bug inside split()
		if (what == null)
			return null;
		char chars[] = what.toCharArray();
		int splitCount = 0; // 1;

		for (int i = 0; i < chars.length; i++) {
			if (chars[i] == delim)
				splitCount++;
		}

		if (splitCount == 0) {
			String splits[] = new String[1];
			splits[0] = new String(what);
			return splits;
		}

		String splits[] = new String[splitCount + 1];
		int splitIndex = 0;
		int startIndex = 0;
		for (int i = 0; i < chars.length; i++) {
			if (chars[i] == delim) {
				splits[splitIndex++] = new String(chars, startIndex, i
						- startIndex);
				startIndex = i + 1;
			}
		}
		splits[splitIndex] = new String(chars, startIndex, chars.length
				- startIndex);
		return splits;
	}
}
