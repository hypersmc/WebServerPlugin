/*
 * ******************************************************
 *  *Copyright (c) 2021. Jesper Henriksen mhypers@gmail.com
 *
 *  * This file is part of WebServer project
 *  *
 *  * WebServer can not be copied and/or distributed without the express
 *  * permission of Jesper Henriksen
 *  ******************************************************
 */

package me.hypersmc.jumpwatch.humpjump.webserver.PHP;

import me.hypersmc.jumpwatch.humpjump.webserver.Main;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class PhPUnzipper {
    private static final int BUFFER_SIZE = 4096;

    /*
    Windows
     */
    public static void PhPUnzipper(){//nginx Windows
        Main main = JavaPlugin.getPlugin(Main.class);
        String zipFilePath = main.getDataFolder() + "/tempfiles/nginx.zip";

        String destDir = main.getDataFolder() + "/phpcore/";
        try {
            unzip(zipFilePath, destDir);
        } catch (IOException e) {
            if (main.getConfig().getBoolean("debug")) {
                e.printStackTrace();
            }
        }

    }
    public static void PhPUnzipper2(){ //PHP Windows
        Main main = JavaPlugin.getPlugin(Main.class);
        String zipFilePath = main.getDataFolder() + "/tempfiles/php.zip";

        String destDir = main.getDataFolder() + "/php/";
        try {
            unzip(zipFilePath, destDir);
        } catch (IOException e) {
            if (main.getConfig().getBoolean("debug")) {
                e.printStackTrace();
            }
        }

    }

    /*
    Linux
     */

    public static void PhPUnzipper3(){ //nginx Linux
        Main main = JavaPlugin.getPlugin(Main.class);
        String zipFilePath = main.getDataFolder() + "/tempfiles/nginxlinux.zip";
        String destDir = main.getDataFolder() + "/phpcorelinux/";
        try {
            unzip(zipFilePath, destDir);
        } catch (IOException e) {
            if (main.getConfig().getBoolean("debug")) {
                e.printStackTrace();
            }
        }
    }

    public static void PhPUnzipper4(){//PHP Linux
        Main main = JavaPlugin.getPlugin(Main.class);
        String zipFilePath = main.getDataFolder() + "/tempfiles/phplinux.zip";
        String destDir = main.getDataFolder() + "/phplinux/";
        try {
            unzip(zipFilePath, destDir);
        } catch (IOException e) {
            if (main.getConfig().getBoolean("debug")) {
                e.printStackTrace();
            }
        }
    }


    /*
    .zip file unzipper
     */

    public static void unzip(String zipFilePath, String destDirectory) throws IOException {
        File destDir = new File(destDirectory);
        if (!destDir.exists()) {
            destDir.mkdir();
        }
        ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFilePath));
        ZipEntry entry = zipIn.getNextEntry();
        // iterates over entries in the zip file
        while (entry != null) {
            String filePath = destDirectory + File.separator + entry.getName();
            if (!entry.isDirectory()) {
                // if the entry is a file, extracts it
                extractFile(zipIn, filePath);
            } else {
                // if the entry is a directory, make the directory
                File dir = new File(filePath);
                dir.mkdirs();
            }
            zipIn.closeEntry();
            entry = zipIn.getNextEntry();
        }
        zipIn.close();
    }
    private static void extractFile(ZipInputStream zipIn, String filePath) throws IOException {
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
        byte[] bytesIn = new byte[BUFFER_SIZE];
        int read = 0;
        while ((read = zipIn.read(bytesIn)) != -1) {
            bos.write(bytesIn, 0, read);
        }
        bos.close();
    }
}
