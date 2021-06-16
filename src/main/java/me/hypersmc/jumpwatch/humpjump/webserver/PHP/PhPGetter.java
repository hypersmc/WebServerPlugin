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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class PhPGetter {

    public static void PhPGetter() {
        Main main = JavaPlugin.getPlugin(Main.class);
        String url = "http://nginx.org/download/nginx-1.20.1.zip";
        try {
            File dir = new File(main.getDataFolder() + "/tempfiles/");
            dir.mkdirs();
            downloadphp(url, main.getDataFolder() + "/tempfiles/" + "nginx.zip");
            main.getLogger().info("File downloaded!");
            PhPUnzipper.PhPUnzipper();
        } catch (IOException e) {
            if (main.getConfig().getBoolean("debug")) {
                e.printStackTrace();
            }
        }
    }

    private static void downloadphp(String urld, String location) throws IOException {
        URL url = new URL(urld);
        BufferedInputStream inputStream = new BufferedInputStream(url.openStream());
        FileOutputStream outputStream = new FileOutputStream(location);
        byte[] buffer = new byte[1024];
        int count=0;
        while ((count = inputStream.read(buffer,0,1024)) != -1){
            outputStream.write(buffer, 0, count);
        }
        inputStream.close();
        outputStream.close();
    }
}
