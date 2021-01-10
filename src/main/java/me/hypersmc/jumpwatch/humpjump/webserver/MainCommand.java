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
}