/*
 * ******************************************************
 *  *Copyright (c) 2020. Jesper Henriksen mhypers@gmail.com
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
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.logging.Logger;

public class Main extends JavaPlugin implements Listener {
    public static String prefix = ChatColor.translateAlternateColorCodes('&', "&7[&3&lWebPlugin&7]&r");
    public static Plugin plugin;
    public FileConfiguration config = getConfig();

    //Werbserver things
    private boolean debug;
    public static String closeConnection = "!Close Connection!";
    private int listeningport;
    private Main m = this;
    private Thread acceptor;
    private boolean acceptorRunning;
    private ServerSocket ss;
    Logger logger2 = this.getLogger();
    private synchronized boolean getAcceptorRunning() {
        return acceptorRunning;
    }
    @Override
    public void onEnable() {
        getConfig().options().copyDefaults();
        saveDefaultConfig();
        Logger logger = this.getLogger();
        /*new UpdateChecker(this, 12345).getVersion(version -> {
            if (this.getDescription().getVersion().equalsIgnoreCase(version)) {
                logger.info("There is not a new update available.");

            } else {
                logger.info("There is a new update available.");
            }
        });*/
        this.getCommand("WebP").setExecutor(new MainCommand());
        if (this.getConfig().getBoolean("UseHtml") && this.getConfig().getBoolean("UsePHP")) {
            logger.warning("Cannot have both HTML and PHP enabled at once! disabling!");
            Bukkit.getScheduler().cancelTasks(this);
            Bukkit.getPluginManager().disablePlugin(this);
        }else if (this.getConfig().getBoolean("UseHtml") || !this.getConfig().getBoolean("UsePHP")){
            if (!new File(getDataFolder() + "/web/", "index.html").exists()) {
                saveResource("web/index.html", false);
            }
        }else if (!this.getConfig().getBoolean("UseHtml") || this.getConfig().getBoolean("UsePHP")){
            if (!new File(getDataFolder() + "/web/", "index.php").exists()) {
                saveResource("web/index.php", false);
            }
        }
        File file = new File("plugins/WebPlugin/web/index.html");
        File file2 = new File("plugins/WebPlugin/web/index.php");
        if (!file.exists()){
            Bukkit.getServer().getLogger().warning("No index for html was found!");
            Bukkit.getServer().getLogger().info("This error can be ignored if you use PHP");
        }
        if (!file2.exists()){
            Bukkit.getServer().getLogger().warning("No index for php was found!");
            Bukkit.getServer().getLogger().info("This error can be ignored if you use HTML");

        }
        debug = getConfig().getBoolean("debug");
        if (getConfig().getBoolean("EnableWebserver")) {
            if (getConfig().isSet("listeningport")) {
                Bukkit.getServer().getLogger().info(ChatColor.GRAY + "Found a listening port!");
                try {
                    listeningport = getConfig().getInt("listeningport");
                    ss = new ServerSocket(listeningport);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                if (getConfig().contains("listeningport")) {
                    Bukkit.getServer().getLogger().warning(ChatColor.YELLOW + "Listening port for WebServer NOT FOUND! Using internal default value!");
                    try {
                        listeningport = getConfig().getInt("listeningport");
                        ss = new ServerSocket(listeningport);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    Bukkit.getServer().getLogger().warning(ChatColor.DARK_RED + "Plugin disabled! NO VALUE WAS FOUND FOR LISTENING PORT!");
                    Bukkit.getPluginManager().disablePlugin(this);
                    return;
                }
            }
        }
        acceptorRunning = true;
        acceptor = new Thread(new Runnable() {
            @Override
            public void run() {
                Socket sock;
                Bukkit.getServer().getLogger().info(ChatColor.AQUA + "accepting connections");
                while (getAcceptorRunning()) {
                    try {
                        sock = ss.accept();
                        new AcceptedSocketConnection(sock, m).start();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                Bukkit.getServer().getLogger().info(ChatColor.LIGHT_PURPLE + "Done accepting connections");
            }
        });
        acceptor.start();
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
            Bukkit.getServer().getLogger().info(ChatColor.DARK_GREEN + "Closed listening web server successfully!");
        } catch (UnknownHostException e) {
            e.printStackTrace();
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
