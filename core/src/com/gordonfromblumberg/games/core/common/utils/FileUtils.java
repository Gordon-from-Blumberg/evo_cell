package com.gordonfromblumberg.games.core.common.utils;

import java.io.*;
import java.nio.ByteBuffer;

public class FileUtils {
    public static String readFile(String path) {
        String text = null;
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            StringBuilder sb = new StringBuilder();
            String ls = System.getProperty("line.separator"), line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append(ls);
            }
            text = sb.toString();
        } catch (FileNotFoundException e) {
            System.err.println("File " + path + " not found");
        } catch (IOException e) {
            System.err.println("Error during file reading");
            e.printStackTrace();
        }

        return text;
    }

    public static String getNameWithoutExtension(File file) {
        String name = file.getName();
        int lastDotIndex = name.lastIndexOf('.');
        return lastDotIndex > -1 ? name.substring(0, lastDotIndex) : name;
    }

    public static void saveToFile(ByteBuffer bb, File file) {
        try (FileOutputStream os = new FileOutputStream(file)) {
            os.getChannel().write(bb);
        } catch (IOException e) {
            throw new RuntimeException("Could not write to file " + file.getPath(), e);
        }
    }

    public static void readFile(ByteBuffer bb, File file) {
        try (FileInputStream is = new FileInputStream(file)) {
            is.getChannel().read(bb);
        } catch (IOException e) {
            throw new RuntimeException("Could not read file " + file.getPath(), e);
        }
    }
}
