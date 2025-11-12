package com.gordonfromblumberg.games.desktop.common;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.gordonfromblumberg.games.core.common.Main;
import com.gordonfromblumberg.games.core.common.debug.DebugOptions;
import com.gordonfromblumberg.games.core.common.factory.AbstractFactory;
import com.gordonfromblumberg.games.core.common.utils.ConfigManager;
import com.gordonfromblumberg.games.core.common.utils.Paths;
import com.gordonfromblumberg.games.desktop.common.factory.DesktopFactory;

import java.io.File;

public class DesktopLauncher {
	public static void main (String[] args) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
//		config.foregroundFPS = 0;
//		config.vSyncEnabled = false;
		config.title = Main.NAME;

		config.preferencesDirectory = config.preferencesDirectory + Main.NAME + '/';

		DesktopFactory.init();
		ConfigManager cfgMgr = AbstractFactory.getInstance().configManager();

		for (String arg : args) {
			if ("-debug".equals(arg)) {
				DebugOptions.DEBUG = true;
				continue;
			}
			if ("-debugui".equals(arg)) {
				DebugOptions.DEBUG_UI = true;
				continue;
			}
			String workDirPrefix = "-workDir=";
			if (arg.startsWith(workDirPrefix)) {
				String workDir = arg.substring(workDirPrefix.length());
				File workDirFile = new File(workDir);
				if (!workDirFile.exists() && !workDirFile.mkdirs()) {
					System.err.println("Incorrect work dir " + workDir);
					continue;
				}
				Paths.setWorkDirPath(workDir);
				continue;
			}
			String prefDirPrefix = "-prefDir=";
			if (arg.startsWith(prefDirPrefix)) {
				String prefDir = arg.substring(prefDirPrefix.length());
				File prefDirFile = new File(prefDir);
				if (!prefDirFile.exists() && !prefDirFile.mkdirs()) {
					System.err.println("Incorrect work dir " + prefDir);
					continue;
				}
				config.preferencesDirectory = prefDir;
				config.preferencesFileType = Files.FileType.Absolute;
				continue;
			}
		}

		new LwjglApplication(Main.createInstance(), config);
	}
}
