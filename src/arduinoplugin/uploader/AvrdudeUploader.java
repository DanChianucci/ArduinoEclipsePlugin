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
import arduinoplugin.pages.SettingKeys;

public class AvrdudeUploader extends Uploader {
	//IProject thisProj = null;

	public AvrdudeUploader() {
	}

	public boolean uploadUsingPreferences(String buildPath, String className,
			boolean verbose,IProject thisProj) throws RunnerException, SerialException {
		this.verbose = verbose;
		// Map<String, String> boardPreferences = Base.getBoardPreferences();

		// Gets project setting falls back on workspace setting
		String uploadUsing = "bootloader";//SettingsManager.getSetting("upload.using", //$NON-NLS-1$
				//thisProj);

		if (uploadUsing.equals("bootloader")) { //$NON-NLS-1$
			return uploadViaBootloader(buildPath, className, thisProj);
		} 
		else
		{
			System.out.print("UPLOAD USING BOOTLOADER IS THE ONLY SUPPORTED UPLOAD METHOD"); //$NON-NLS-1$
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
		Target target = new Target(new File(PluginBase.getBoardProgPath()));
		String mapName = target.getBoardNamed(SettingsManager.getSetting(SettingKeys.BoardTypeKey, thisProj));
		Map<String, String> boardPreferences = target.getBoardSettings(mapName);
		List<String> commandDownloader = new ArrayList<String>();
		
		if(SettingsManager.getSetting(SettingKeys.BoardTypeKey,thisProj).equals("Custom")) //$NON-NLS-2$
		{
			boardPreferences = new LinkedHashMap<String,String>();
			boardPreferences.put(SettingKeys.UploadProtocolKey, SettingsManager.getSetting(SettingKeys.UploadProtocolKey, thisProj));
			boardPreferences.put(SettingKeys.UploadSpeedKey, SettingsManager.getSetting(SettingKeys.UploadSpeedKey, thisProj));
			boardPreferences.put(SettingKeys.disableFlushingKey,SettingsManager.getSetting(SettingKeys.disableFlushingKey, thisProj));
		}
			
			
			
		String protocol = boardPreferences.get(SettingKeys.UploadProtocolKey);

		// avrdude wants "stk500v1" to distinguish it from stk500v2
		if (protocol.equals("stk500")) //$NON-NLS-1$
			protocol = "stk500v1"; //$NON-NLS-1$
		commandDownloader.add("-c" + protocol); //$NON-NLS-1$
		commandDownloader.add(

				"-P" + (PluginBase.isWindows() ? "\\\\.\\" : "")+"COM3"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
						//+ SettingsManager.getSetting("serial.port", thisProj));
		commandDownloader.add("-b" //$NON-NLS-1$
				+ Integer.parseInt(boardPreferences.get(SettingKeys.UploadSpeedKey)));
		commandDownloader.add("-D"); // don't erase //$NON-NLS-1$
		commandDownloader.add("-Uflash:w:" + buildPath + File.separator //$NON-NLS-1$
				+ className + ".hex:i"); //$NON-NLS-1$

		if (boardPreferences.get(SettingKeys.disableFlushingKey) == null
				|| boardPreferences.get(SettingKeys.disableFlushingKey)
						.toLowerCase().equals("false")) { //$NON-NLS-1$
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
		commandDownloader.add("avrdude"); //$NON-NLS-1$

		// Point avrdude at its config file since it's in a non-standard
		// location.
		if (PluginBase.isLinux()) {
			// ???: is it better to have Linux users install avrdude themselves,
			// in
			// a way that it can find its own configuration file?
			commandDownloader.add("-C" + PluginBase.getHardwarePath() //$NON-NLS-1$
					+ "/tools/avrdude.conf"); //$NON-NLS-1$
		} else {
			commandDownloader.add("-C" + PluginBase.getHardwarePath() //$NON-NLS-1$
					+ "tools"+File.separator+"avr"+File.separator+"etc"+File.separator+"avrdude.conf"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		}

		if (verbose
				|| SettingsManager.getSetting(SettingKeys.uploadVerboseKey, thisProj) == "true") { //$NON-NLS-2$
			commandDownloader.add("-v"); //$NON-NLS-1$
			commandDownloader.add("-v"); //$NON-NLS-1$
			commandDownloader.add("-v"); //$NON-NLS-1$
			commandDownloader.add("-v"); //$NON-NLS-1$
		} else {
			commandDownloader.add("-q"); //$NON-NLS-1$
			commandDownloader.add("-q"); //$NON-NLS-1$
		}
		commandDownloader.add("-p" + SettingsManager.getSetting(SettingKeys.ProcessorTypeKey, thisProj)); //$NON-NLS-1$
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
