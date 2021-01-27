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
package me.hypersmc.jumpwatch.humpjump.webserver;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.FileUtil;

import javax.net.ssl.SSLSocket;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Logger;

public class Main extends JavaPlugin implements Listener {
    public static String prefix = ChatColor.translateAlternateColorCodes('&', "&7[&3&lWebPlugin&7]&r");

    //Webserver things
    private boolean debug;
    public static String closeConnection = "!Close Connection!";
    private int listeningport;
    private Main m = this;
    private Thread acceptor;
    private boolean acceptorRunning;
    private ServerSocket ss;
    public static String ver;
    private int version = 2;
    private synchronized boolean getAcceptorRunning() {
        return acceptorRunning;
    }
    public void sethtmlfiles(){
        saveResource("web/index.html", false);
        saveResource("web/assets/css/font-awesome.min.css", false);
        saveResource("web/assets/css/main.css", false);
        saveResource("web/assets/fonts/FontAwesome.otf", false);
        saveResource("web/assets/fonts/fontawesome-webfont.eot", false);
        saveResource("web/assets/fonts/fontawesome-webfont.svg", false);
        saveResource("web/assets/fonts/fontawesome-webfont.ttf", false);
        saveResource("web/assets/fonts/fontawesome-webfont.woff", false);
        saveResource("web/assets/fonts/fontawesome-webfont.woff2", false);
        saveResource("web/assets/js/jquery.min.js", false);
        saveResource("web/assets/js/jquery.poptrox.min.js", false);
        saveResource("web/assets/js/main.js", false);
        saveResource("web/assets/js/skel.min.js", false);
        saveResource("web/images/bg.jpg", false);
    }
    @Override
    public void onEnable() {
        if (!(getConfig().contains("ConfigVersion", true))) {
            this.getLogger().warning("No config version found. Either config corrupt or never existed.");
            this.getLogger().info("In case on existed backup is being made.");
            File backup = new File(getDataFolder(), "config.yml");
            this.getLogger().info("Making backup");
            FileUtil.copy(backup, new File(backup + ".backup"));
            backup.delete();
            this.getLogger().info("Creating config from internal storage.");
            getConfig().options().copyDefaults();
            saveDefaultConfig();
        }else if ((getConfig().contains("ConfigVersion")) && (getConfig().getInt("ConfigVersion") != version)) {
            this.getLogger().warning("Config is not right. Config was missing an update or you changed it!");
            this.getLogger().info("An backup will be made.");
            File backup = new File(getDataFolder(), "config.yml");
            this.getLogger().info("Making backup");
            FileUtil.copy(backup, new File(backup + ".backup"));
            backup.delete();
            this.getLogger().info("Done!");
            this.getLogger().info("config was not up to date.");
            this.getLogger().info("RECREATING");
            saveResource("config.yml", true);

        }else {
            this.getLogger().info("Config up to date!");

        }
        getConfig().options().copyDefaults();
        saveDefaultConfig();
        Logger logger = this.getLogger();
        new UpdateChecker(this, 85640).getVersion(version -> {
            if (this.getDescription().getVersion().equalsIgnoreCase(version)) {
                logger.info("There is not a new update available.");
                ver  = version;

            } else {
                logger.info("There is a new update available.");
                logger.info("Your version is " + this.getDescription().getVersion() + " newest version " + version);
                ver = version;
            }
        });
        this.getCommand("WebP").setExecutor(new MainCommand());

        if (this.getConfig().getBoolean("UseHtml") && this.getConfig().getBoolean("UsePHP")) {
            logger.warning("Cannot have both HTML and PHP enabled at once! disabling!");
            Bukkit.getScheduler().cancelTasks(this);
            Bukkit.getPluginManager().disablePlugin(this);
        }else if (this.getConfig().getBoolean("UseHtml") || !this.getConfig().getBoolean("UsePHP")){
            if (!new File(getDataFolder() + "/web/", "index.html").exists()) {
                sethtmlfiles();
            }
        }else if (!this.getConfig().getBoolean("UseHtml") || this.getConfig().getBoolean("UsePHP")){
            if (!new File(getDataFolder() + "/web/", "index.php").exists()) {
                saveResource("web/index.php", false);
            }
        }else {
            logger.warning("Neither HTML or PHP was enabled! disabling plugin!");
            Bukkit.getScheduler().cancelTasks(this);
            Bukkit.getPluginManager().disablePlugin(this);
        }

        File file = new File("plugins/WebPlugin/web/index.html");
        File file2 = new File("plugins/WebPlugin/web/index.php");
        if (!file.exists()){
            logger.warning("No index for html was found!");
            logger.info("This error can be ignored if you use PHP");
        }
        if (!file2.exists()){
            logger.warning("No index for php was found!");
            logger.info("This error can be ignored if you use HTML");

        }
        debug = getConfig().getBoolean("debug");
        if (new File("plugins/WebPlugin/ssl/").exists()){
            logger.info("SSL Folder exist!");
        }else {
            logger.info("SSL Folder doesn't exist!");
            logger.info("Making!");
            if (!new File(getDataFolder() + "/SSL/", "removeme.txt").exists()) {
                saveResource("SSL/removeme.txt", false);
            }

        }
        if (new File("plugins/WebPlugin/ssl/" + getConfig().getString("SSLJKSName") + ".jks").exists()){
            logger.info("SSL File exist!");
        } else {
            logger.info("SSL File doesn't exist!");
        }
        if (getConfig().getBoolean("EnableWebserver")) {
            if (getConfig().isSet("listeningport")) {
                logger.info(ChatColor.GRAY + "Found a listening port!");
                try {
                    listeningport = getConfig().getInt("listeningport");
                    ss = new ServerSocket(listeningport);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                if (getConfig().contains("listeningport")) {
                    logger.warning(ChatColor.YELLOW + "Listening port for WebServer NOT FOUND! Using internal default value!");
                    try {
                        listeningport = getConfig().getInt("listeningport");
                        ss = new ServerSocket(listeningport);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    logger.warning(ChatColor.DARK_RED + "Plugin disabled! NO VALUE WAS FOUND FOR LISTENING PORT!");
                    Bukkit.getPluginManager().disablePlugin(this);
                    return;
                }
            }
        }
        acceptorRunning = true;
        if (!(getConfig().getBoolean("EnableSSL"))) {
            acceptor = new Thread(new Runnable() {
                @Override
                public void run() {
                    Socket sock;
                    SSLSocket sslSocket;
                    logger.info(ChatColor.AQUA + "accepting connections");
                    while (getAcceptorRunning()) {
                        try {
                            sock = ss.accept();
                            new AcceptedSocketConnection(sock, m).start();

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    logger.info(ChatColor.LIGHT_PURPLE + "Done accepting connections");
                }
            });
            acceptor.start();
        }
        if (getConfig().getBoolean("EnableSSL")){
            new SSLAcceptedSocketConnection().run();
        }
    }

    @Override
    public void onDisable() {
        acceptorRunning = false;
        Socket sockCloser;
        try {
            sockCloser = new Socket("localhost", listeningport);
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(sockCloser.getOutputStream()));
            out.write(Main.closeConnection);
            out.close();
            sockCloser.close();
            getLogger().info(ChatColor.DARK_GREEN + "Closed listening web server successfully!");
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            ss.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
