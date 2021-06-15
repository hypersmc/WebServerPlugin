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
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class PhPGetter {

    public static void PhPGetter() {
        Main main = JavaPlugin.getPlugin(Main.class);
        String url = "https://windows.php.net//downloads/releases/php-8.0.7-nts-Win32-vs16-x64.zip";
        try {
            downloadphp(url, main.getDataFolder() + "tempfiles/");
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
