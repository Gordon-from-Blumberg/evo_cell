package com.gordonfromblumberg.games.core.common.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

public class Paths {
    private static String WORK_DIR_PATH;
    private static FileHandle WORK_DIR;

    public static void setWorkDirPath(String path) {
        WORK_DIR_PATH = path;
    }

    public static void initWorkDir() {
        WORK_DIR = Gdx.files.absolute(WORK_DIR_PATH);
        if (!WORK_DIR.exists()) {
            WORK_DIR.mkdirs();
        }
    }

    public static String workDirPath() {
        return WORK_DIR_PATH;
    }

    public static FileHandle workDir() {
        return WORK_DIR;
    }
}
