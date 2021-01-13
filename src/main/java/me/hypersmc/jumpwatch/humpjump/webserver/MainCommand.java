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
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.FileUtil;

import java.io.File;

public class MainCommand implements CommandExecutor {
    Main main = JavaPlugin.getPlugin(Main.class);

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {
        if (sender instanceof ConsoleCommandSender) {
            if (main.getServer().getIp().isEmpty()) {
                sender.sendMessage(main.prefix + ChatColor.RED + " MISSING IP FROM server.properties!");
                if (main.getConfig().getBoolean("EnableSSL")) {
                    sender.sendMessage(main.prefix + " WebServer info: " + "\n" + "Link: https://localhost" + ":" + main.getConfig().getInt("listeningport") + "/");

                }else {
                    sender.sendMessage(main.prefix + " WebServer info: " + "\n" + "Link: http://localhost" + ":" + main.getConfig().getInt("listeningport") + "/");
                }
            }else {
                if (main.getConfig().getBoolean("EnableSSL")) {
                    sender.sendMessage(main.prefix + " WebServer info: " + "\n" + "Link: https://" + Bukkit.getServer().getIp() + ":" + main.getConfig().getInt("listeningport") + "/");
                }else {
                    sender.sendMessage(main.prefix + " WebServer info: " + "\n" + "Link: http://" + Bukkit.getServer().getIp() + ":" + main.getConfig().getInt("listeningport") + "/");

                }
            }
            return true;
        }else {
            if (cmd.getName().equalsIgnoreCase("WebP")) {
                Player p = (Player) sender;
                if (args.length == 0) {
                    main.getServer().getIp();
                    if (main.getServer().getIp().isEmpty()) {
                        sender.sendMessage(main.prefix + ChatColor.RED + " MISSING IP FROM server.properties!");
                        if (main.getConfig().getBoolean("EnableSSL")) {
                            sender.sendMessage(main.prefix + " WebServer info: " + "\n" + "Link: https://localhost" + ":" + main.getConfig().getInt("listeningport") + "/");

                        }else {
                            sender.sendMessage(main.prefix + " WebServer info: " + "\n" + "Link: http://localhost" + ":" + main.getConfig().getInt("listeningport") + "/");
                        }
                    }else {
                        if (main.getConfig().getBoolean("EnableSSL")) {
                            sender.sendMessage(main.prefix + " WebServer info: " + "\n" + "Link: https://" + Bukkit.getServer().getIp() + ":" + main.getConfig().getInt("listeningport") + "/");
                        }else {
                            sender.sendMessage(main.prefix + " WebServer info: " + "\n" + "Link: http://" + Bukkit.getServer().getIp() + ":" + main.getConfig().getInt("listeningport") + "/");

                        }
                    }
                    return true;

                }else if (args[0].equalsIgnoreCase("reload")) {
                    main.reloadConfig();
                    sender.sendMessage(main.prefix + " Configuration file reloaded.");
                    return true;
                }else if (args[0].equalsIgnoreCase("reset")) {
                    sender.sendMessage(main.prefix + " Starting config reset!");
                    resetconfig(((Player) sender).getPlayer());
                    return true;
                }else if (args[0].equalsIgnoreCase("dev")) {
                    sender.sendMessage(main.prefix + " This plugin is developed by " + main.getDescription().getAuthors());
                    sender.sendMessage(main.prefix + " Your running version: " + ChatColor.RED + main.getDescription().getVersion());
                    return true;
                }else if (args[0].equalsIgnoreCase("help")) {
                    sender.sendMessage(main.prefix + " Commands: ");
                    sender.sendMessage(main.prefix + " /webp reload reloads the plugin's configuration (NOT RESET)");
                    sender.sendMessage(main.prefix + " /webp dev gets who developed this plugin and plugin version");
                    sender.sendMessage(main.prefix + " /webp ver gets plugin version and checks if there is a new version");
                    sender.sendMessage(main.prefix + " /webp help to get this again.");
                    sender.sendMessage(main.prefix + " /webp reset (WARNING This command WILL reset your config! Backup will be made)");
                    return true;
                }else if (args[0].equalsIgnoreCase("ver")) {
                    sender.sendMessage(main.prefix + " Your running version: " + ChatColor.RED + main.getDescription().getVersion() + ChatColor.RESET + " and the newest version is: " + main.ver);
                    return true;
                }else if (args.length < 323232323) {
                    main.getServer().getIp();
                    if (main.getServer().getIp().isEmpty()) {
                        sender.sendMessage(main.prefix + ChatColor.RED + " MISSING IP FROM server.properties!");
                        if (main.getConfig().getBoolean("EnableSSL")) {
                            sender.sendMessage(main.prefix + " WebServer info: " + "\n" + "Link: https://localhost" + ":" + main.getConfig().getInt("listeningport") + "/");

                        }else {
                            sender.sendMessage(main.prefix + " WebServer info: " + "\n" + "Link: http://localhost" + ":" + main.getConfig().getInt("listeningport") + "/");
                        }
                    }else {
                        if (main.getConfig().getBoolean("EnableSSL")) {
                            sender.sendMessage(main.prefix + " WebServer info: " + "\n" + "Link: https://" + Bukkit.getServer().getIp() + ":" + main.getConfig().getInt("listeningport") + "/");
                        }else {
                            sender.sendMessage(main.prefix + " WebServer info: " + "\n" + "Link: http://" + Bukkit.getServer().getIp() + ":" + main.getConfig().getInt("listeningport") + "/");

                        }
                    }
                    return true;
                }


            }
        }
        return true;
    }
    public void resetconfig(Player sender){
        sender.sendMessage("WARNING You are now resetting your config.yml");
        sender.sendMessage("An backup will be made!");
        File backup = new File(main.getDataFolder(), "config.yml");
        sender.sendMessage("Making backup");
        FileUtil.copy(backup, new File(backup + ".backup"));
        backup.delete();
        sender.sendMessage("Done!");
        sender.sendMessage("config was not up to date.");
        sender.sendMessage("RECREATING");
        main.saveResource("config.yml", true);
    }
}