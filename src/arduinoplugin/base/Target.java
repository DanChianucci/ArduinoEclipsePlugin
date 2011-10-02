/* -*- mode: jde; c-basic-offset: 2; indent-tabs-mode: nil -*- */

/*
 Target - represents a hardware platform
 Part of the Arduino project - http://www.arduino.cc/

 Copyright (c) 2009 David A. Mellis

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

 $Id$
 */

package arduinoplugin.base;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;


public class Target {
	
	/**The folder where boards.txt and programmers.txt reside*/
	private File folder;

	/**Holds Map&ltBOARD,Map&ltKEY,VALUE&gt&gt*/
	private Map<String,Map<String,String>> boards;	
	
	/**Holds Map&ltProggrammer,Map&ltKEY,VALUE&gt&gt*/
	private Map<String,Map<String,String>> programmers;

	/**
	 * A target is a the hardware that the sketch is being compiled for
	 * @param folder the project folder
	 */
	public Target(File folder) {
		this.folder=folder;
		this.boards = new LinkedHashMap<String,Map<String,String>>();
		this.programmers = new LinkedHashMap<String,Map<String,String>>();

		File boardsFile = new File(folder, "boards.txt");
		try {
			if (boardsFile.exists()) {
				Map<String,String> boardPreferences = new LinkedHashMap<String,String>();
				load(new FileInputStream(boardsFile),boardPreferences);
				for (Object k : boardPreferences.keySet()) {
					String key = (String) k;
					String board = key.substring(0, key.indexOf('.'));
					if (!boards.containsKey(board))
						boards.put(board, new HashMap<String,String>());
					((Map<String,String>) boards.get(board)).put(key.substring(key.indexOf('.') + 1),boardPreferences.get(key));
				}
			}
		} catch (Exception e) {
			System.err.println("Error loading boards from " + boardsFile + ": "
					+ e);
		}

		File programmersFile = new File(folder, "programmers.txt");
		try {
			if (programmersFile.exists()) {
				Map<String,String> programmerPreferences = new LinkedHashMap<String,String>();
				load(new FileInputStream(programmersFile),
						programmerPreferences);
				for (Object k : programmerPreferences.keySet()) {
					String key = (String) k;
					String programmer = key.substring(0, key.indexOf('.'));
					if (!programmers.containsKey(programmer))
						programmers.put(programmer, new HashMap<String,String>());
					((Map<String,String>) programmers.get(programmer)).put(key.substring(key.indexOf('.') + 1),programmerPreferences.get(key));
				}
			}
		} catch (Exception e) {
			System.err.println("Error loading programmers from "
					+ programmersFile + ": " + e);
		}
	}

	/**
	 * @return a Map of the boards info in the form of Map&ltBOARD, Map&ltKEY,VALUE&gt&gt
	 */
	public Map<String, Map<String, String>> getBoards() {
		return boards;
	}
	
	/**
	 * @param board the name of the board to get settings for
	 * @return the settings to which the specified board is mapped, 
	 * 			or null if this map contains no mapping for the board
	  *			Map&ltKEY,VALUE&gt
	 */
	public Map<String,String> getBoardSettings(String board)
	{
		return boards.get(board);		
	}
	/**
	 * @param programmer the name of the programmer to get settings for
	 * @return the settings to which the specified programmer is mapped, 
	 * 			or null if this map contains no mapping for the programmer
	  *			Map&ltKEY,VALUE&gt
	 */
	public Map<String,String> getProgrammerSettings(String programmer)
	{
		return programmers.get(programmer);		
	}

	
	/**
	 * @return the boards.txt / programmers.txt folder
	 */
	public File getFolder() 
	{
		return folder;
	}
	
	/**
	 * @return a Map of the proggrammers info in the form of Map&ltBOARD, Map&ltKEY,VALUE&gt&gt
	 */
	public Map<String, Map<String, String>> getProgrammers() {
		return programmers;
	}
	
	
	

	/**Loads the input stream to a Map, ignoring any lines that start with a # <p>
	 * Taken from preferences.java in the arduino source
	 * @param input the input stream to load
	 * @param table the Map to load the values to
	 * @throws IOException when something goes wrong??
	 */
	static public void load(InputStream input, Map<String,String> table) throws IOException {  
	    String[] lines = loadStrings(input);  // Reads as UTF-8
	    for (String line : lines) {
	      if ((line.length() == 0) ||
	          (line.charAt(0) == '#')) continue;

	      // this won't properly handle = signs being in the text
	      int equals = line.indexOf('=');
	      if (equals != -1) {
	        String key = line.substring(0, equals).trim();
	        String value = line.substring(equals + 1).trim();
	        table.put(key, value);
	      }
	    }
	  }
	
	
	//Taken from PApplet.java
	 /**Loads an input stream into an array of strings representing each line of the input stream
	 * @param input the input stream to load
	 * @return the array of strings representing the inputStream
	 */
	static public String[] loadStrings(InputStream input) {
		    try {
		      BufferedReader reader =
		        new BufferedReader(new InputStreamReader(input, "UTF-8"));

		      String lines[] = new String[100];
		      int lineCount = 0;
		      String line = null;
		      while ((line = reader.readLine()) != null) {
		        if (lineCount == lines.length) {
		          String temp[] = new String[lineCount << 1];
		          System.arraycopy(lines, 0, temp, 0, lineCount);
		          lines = temp;
		        }
		        lines[lineCount++] = line;
		      }
		      reader.close();

		      if (lineCount == lines.length) {
		        return lines;
		      }

		      // resize array to appropriate amount for these lines
		      String output[] = new String[lineCount];
		      System.arraycopy(lines, 0, output, 0, lineCount);
		      return output;

		    } catch (IOException e) {
		      e.printStackTrace();
		      //throw new RuntimeException("Error inside loadStrings()");
		    }
		    return null;
		  }

	/**
	 * Returns the Maped name of the board which has a name value of the given string
	 * @param boardName	the name of the board, to get the base name of 
	 * @return the base name of the board that has the name equal to boardName
	 */
	public String getBoardNamed(String boardName) 
	{
		for(Entry<String,Map<String,String>> entry : boards.entrySet())
		{
			for(Entry<String,String> e2: entry.getValue().entrySet())
			{
				if(e2.getValue().equals(boardName))
					return entry.getKey();
			}
					
		}
		return null;
	}
	
	/**
	 * Returns the mapped name of the programmer which has a name value of the given string
	 * @param programmerName	the name of the board, to get the base name of 
	 * @return the base name of the programmer that has the name equal to programmerName
	 */
	public String getProgrammerNamed(String programmerName) 
	{
		for(Entry<String,Map<String,String>> entry : programmers.entrySet())
		{
			for(Entry<String,String> e2: entry.getValue().entrySet())
			{
				if(e2.getValue().equals(programmerName))
					return entry.getKey();
			}
					
		}
		return null;
	}
}