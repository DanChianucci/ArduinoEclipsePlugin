/* -*- mode: jde; c-basic-offset: 2; indent-tabs-mode: nil -*- */

/*
 AvrdudeUploader - uploader implementation using avrdude
 Part of the Arduino project - http://www.arduino.cc/

 Copyright (c) 2004-05
 Hernando Barragan

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

//

//
package arduinoplugin.uploader;

//I think target is always going to be arduino...
//Maybe make a setting so users can change it if they want to?
import java.io.*;
import java.util.*;
import org.eclipse.core.resources.IProject;

import arduinoplugin.base.PluginBase;
import arduinoplugin.base.SettingsManager;
import arduinoplugin.base.Target;
import arduinoplugin.builders.RunnerException;

public class AvrdudeUploader extends Uploader {
	//IProject thisProj = null;

	public AvrdudeUploader() {
	}

	public boolean uploadUsingPreferences(String buildPath, String className,
			boolean verbose,IProject thisProj) throws RunnerException, SerialException {
		this.verbose = verbose;
		// Map<String, String> boardPreferences = Base.getBoardPreferences();

		// Gets project setting falls back on workspace setting
		String uploadUsing = "bootloader";//SettingsManager.getSetting("upload.using",
				//thisProj);

		if (uploadUsing.equals("bootloader")) {
			return uploadViaBootloader(buildPath, className, thisProj);
		} 
		else
		{
			System.out.print("UPLOAD USING BOOTLOADER IS THE ONLY SUPPORTED UPOLOAD METHOD");
			return false;
//		 Target t;
//		
//		 if (uploadUsing.indexOf(':') == -1) {
//		 t = Base.getTarget(); // the current target (associated with the
//		 board)
//		 }
//		 else {
//		 String targetName = uploadUsing.substring(0,
//		 uploadUsing.indexOf(':'));
//		 t = Base.targetsTable.get(targetName);
//		 uploadUsing = uploadUsing
//		 .substring(uploadUsing.indexOf(':') + 1);
//		 }
//		
//		 Collection<String> params = getProgrammerCommands(t, uploadUsing);
//		 params.add("-Uflash:w:" + buildPath + File.separator + className
//		 + ".hex:i");
//		 return avrdude(params);
//		 }
	}
	}

	private boolean uploadViaBootloader(String buildPath, String className, IProject thisProj)
			throws RunnerException, SerialException {
		// TODO actual project not null get the target
		Target target = new Target(new File(thisProj.getLocation().toOSString()));
		Map<String, String> boardPreferences = target.getBoardSettings(SettingsManager.getSetting("BoardType", thisProj));
		List<String> commandDownloader = new ArrayList<String>();
		
		if(SettingsManager.getSetting("BoardType",thisProj).equals("Custom"))
		{
			boardPreferences = new LinkedHashMap<String,String>();
			boardPreferences.put("upload.protocol", SettingsManager.getSetting("UploadProtocall", thisProj));
			boardPreferences.put("upload.speed", SettingsManager.getSetting("UploadBaud", thisProj));
			boardPreferences.put("upload.disable_flushing", SettingsManager.getSetting("flushing", thisProj));
		}
			
			
			
		String protocol = boardPreferences.get("upload.protocol");

		// avrdude wants "stk500v1" to distinguish it from stk500v2
		if (protocol.equals("stk500"))
			protocol = "stk500v1";
		commandDownloader.add("-c" + protocol);
		commandDownloader.add(
		// TODO get actual project?
				"-P" + (PluginBase.isWindows() ? "\\\\.\\" : "")+"COM3");
						//+ SettingsManager.getSetting("serial.port", thisProj));
		commandDownloader.add("-b"
				+ Integer.parseInt(boardPreferences.get("upload.speed")));
		commandDownloader.add("-D"); // don't erase
		commandDownloader.add("-Uflash:w:" + buildPath + File.separator
				+ className + ".hex:i");

		if (boardPreferences.get("upload.disable_flushing") == null
				|| boardPreferences.get("upload.disable_flushing")
						.toLowerCase().equals("false")) {
			flushSerialBuffer();
		}

		return avrdude(commandDownloader,thisProj);
	}

	// public boolean burnBootloader(String targetName, String programmer)
	// throws RunnerException {
	// return burnBootloader(getProgrammerCommands(
	// Base.targetsTable.get(targetName), programmer));
	// }

//	private Collection<String> getProgrammerCommands(Target target, String programmer) {
//		Map<String, String> programmerPreferences = target.getProgrammers()
//				.get(programmer);
//		List<String> params = new ArrayList<String>();
//		params.add("-c" + programmerPreferences.get("protocol"));
//
//		if ("usb".equals(programmerPreferences.get("communication"))) {
//			params.add("-Pusb");
//		} else if ("serial".equals(programmerPreferences.get("communication"))) {
//			// TODO might have to make this project specific
//			params.add("-P" + (PluginBase.isWindows() ? "\\\\.\\" : "")
//					+ SettingsManager.getSetting("serial.port", thisProj));
//			if (programmerPreferences.get("speed") != null) {
//				params.add("-b"
//						+ Integer.parseInt(programmerPreferences.get("speed")));
//			}
//		}
//		// XXX: add support for specifying the port address for parallel
//		// programmers, although avrdude has a default that works in most cases.
//
//		if (programmerPreferences.get("force") != null
//				&& programmerPreferences.get("force").toLowerCase()
//						.equals("true"))
//			params.add("-F");
//
//		if (programmerPreferences.get("delay") != null)
//			params.add("-i" + programmerPreferences.get("delay"));
//
//		return params;
//	}

	// protected boolean burnBootloader(Collection<String> params)
	// throws RunnerException {
	// // TODO change to board text path
	// Target target = new Target(new File(PluginBase.getBoardProgPath(),
	// "boards.txt"));
	// Map<String, String> boardPreferences = target
	// .getBoardSettings(SettingsManager.getSetting("BoardType",
	// thisProj));
	// List<String> fuses = new ArrayList<String>();
	// fuses.add("-e"); // erase the chip
	// fuses.add("-Ulock:w:" + boardPreferences.get("bootloader.unlock_bits")
	// + ":m");
	// if (boardPreferences.get("bootloader.extended_fuses") != null)
	// fuses.add("-Uefuse:w:"
	// + boardPreferences.get("bootloader.extended_fuses") + ":m");
	// fuses.add("-Uhfuse:w:" + boardPreferences.get("bootloader.high_fuses")
	// + ":m");
	// fuses.add("-Ulfuse:w:" + boardPreferences.get("bootloader.low_fuses")
	// + ":m");
	//
	// if (!avrdude(params, fuses))
	// return false;
	//
	// try {
	// Thread.sleep(1000);
	// } catch (InterruptedException e) {
	// }
	//
	// Target t;
	// String bootloaderPath = boardPreferences.get("bootloader.path");
	//
	// if (bootloaderPath.indexOf(':') == -1) {
	// t = Base.getTarget(); // the current target (associated with the
	// // board)
	// } else {
	// String targetName = bootloaderPath.substring(0,
	// bootloaderPath.indexOf(':'));
	// t = Base.targetsTable.get(targetName);
	// bootloaderPath = bootloaderPath.substring(bootloaderPath
	// .indexOf(':') + 1);
	// }
	//
	// File bootloadersFile = new File(t.getFolder(), "bootloaders");
	// File bootloaderFile = new File(bootloadersFile, bootloaderPath);
	// bootloaderPath = bootloaderFile.getAbsolutePath();
	//
	// List<String> bootloader = new ArrayList<String>();
	// bootloader.add("-Uflash:w:" + bootloaderPath + File.separator
	// + boardPreferences.get("bootloader.file") + ":i");
	// bootloader.add("-Ulock:w:"
	// + boardPreferences.get("bootloader.lock_bits") + ":m");
	//
	// return avrdude(params, bootloader);
	// }
	//
	// protected boolean burnBootloader(Collection<String> params)
	// throws RunnerException {
	// // TODO change to board text path
	// Target target = new Target(new File(PluginBase.getBoardProgPath(),
	// "boards.txt"));
	// Map<String, String> boardPreferences = target
	// .getBoardSettings(SettingsManager.getSetting("BoardType",
	// thisProj));
	// List<String> fuses = new ArrayList<String>();
	// fuses.add("-e"); // erase the chip
	// fuses.add("-Ulock:w:" + boardPreferences.get("bootloader.unlock_bits")
	// + ":m");
	// if (boardPreferences.get("bootloader.extended_fuses") != null)
	// fuses.add("-Uefuse:w:"
	// + boardPreferences.get("bootloader.extended_fuses") + ":m");
	// fuses.add("-Uhfuse:w:" + boardPreferences.get("bootloader.high_fuses")
	// + ":m");
	// fuses.add("-Ulfuse:w:" + boardPreferences.get("bootloader.low_fuses")
	// + ":m");
	//
	// if (!avrdude(params, fuses))
	// return false;
	//
	// try {
	// Thread.sleep(1000);
	// } catch (InterruptedException e) {
	// }
	//
	// Target t;
	// String bootloaderPath = boardPreferences.get("bootloader.path");
	//
	// if (bootloaderPath.indexOf(':') == -1) {
	// t = Base.getTarget(); // the current target (associated with the
	// // board)
	// } else {
	// String targetName = bootloaderPath.substring(0,
	// bootloaderPath.indexOf(':'));
	// t = Base.targetsTable.get(targetName);
	// bootloaderPath = bootloaderPath.substring(bootloaderPath
	// .indexOf(':') + 1);
	// }
	//
	// File bootloadersFile = new File(t.getFolder(), "bootloaders");
	// File bootloaderFile = new File(bootloadersFile, bootloaderPath);
	// bootloaderPath = bootloaderFile.getAbsolutePath();
	//
	// List<String> bootloader = new ArrayList<String>();
	// bootloader.add("-Uflash:w:" + bootloaderPath + File.separator
	// + boardPreferences.get("bootloader.file") + ":i");
	// bootloader.add("-Ulock:w:"
	// + boardPreferences.get("bootloader.lock_bits") + ":m");
	//
	// return avrdude(params, bootloader);
	// }

	public boolean avrdude(Collection<String> p1, Collection<String> p2,IProject thisProj)
			throws RunnerException {
		ArrayList<String> p = new ArrayList<String>(p1);
		p.addAll(p2);
		return avrdude(p,thisProj);
	}

	public boolean avrdude(Collection<String> params,IProject thisProj) throws RunnerException {
		List<String> commandDownloader = new ArrayList<String>();
		commandDownloader.add("avrdude");

		// Point avrdude at its config file since it's in a non-standard
		// location.
		if (PluginBase.isLinux()) {
			// ???: is it better to have Linux users install avrdude themselves,
			// in
			// a way that it can find its own configuration file?
			commandDownloader.add("-C" + PluginBase.getHardwarePath()
					+ "/tools/avrdude.conf");
		} else {
			commandDownloader.add("-C" + PluginBase.getHardwarePath()
					+ "/tools/avr/etc/avrdude.conf");
		}

		if (verbose
				|| SettingsManager.getSetting("upload.verbose", thisProj) == "true") {
			commandDownloader.add("-v");
			commandDownloader.add("-v");
			commandDownloader.add("-v");
			commandDownloader.add("-v");
		} else {
			commandDownloader.add("-q");
			commandDownloader.add("-q");
		}
		// TODO get actual project not null
		commandDownloader.add("-p" + SettingsManager.getSetting("mcu", thisProj));
		commandDownloader.addAll(params);

		return executeUploadCommand(commandDownloader);
	}

	@Override
	public boolean burnBootloader(String target, String programmer)
			throws RunnerException {
		// TODO Auto-generated method stub
		return false;
	}
}
