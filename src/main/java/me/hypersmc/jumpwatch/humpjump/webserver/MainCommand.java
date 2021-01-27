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
            if (main.getConfig().getString("ServerIP").equals("localhost")) {
                sender.sendMessage(main.prefix + ChatColor.RED + " ServerIP not changed in config!");
                if (main.getConfig().getBoolean("EnableSSL")) {
                    sender.sendMessage(main.prefix + " WebServer info: " + "\n" + "Link: https://localhost" + ":" + main.getConfig().getInt("listeningport") + "/");

                }else {
                    sender.sendMessage(main.prefix + " WebServer info: " + "\n" + "Link: http://localhost" + ":" + main.getConfig().getInt("listeningport") + "/");
                }
            }else {
                if (main.getConfig().getBoolean("EnableSSL")) {
                    sender.sendMessage(main.prefix + " WebServer info: " + "\n" + "Link: https://" + main.getConfig().getString("ServerIP") + ":" + main.getConfig().getInt("listeningport") + "/");
                }else {
                    sender.sendMessage(main.prefix + " WebServer info: " + "\n" + "Link: http://" + main.getConfig().getString("ServerIP") + ":" + main.getConfig().getInt("listeningport") + "/");

                }
            }
            return true;
        }else {
            if (cmd.getName().equalsIgnoreCase("WebP")) {
                Player p = (Player) sender;
                if (args.length == 0) {
                    if (main.getConfig().getString("ServerIP").equals("localhost")) {
                        sender.sendMessage(main.prefix + ChatColor.RED + " ServerIP not changed in config!");
                        if (main.getConfig().getBoolean("EnableSSL")) {
                            sender.sendMessage(main.prefix + " WebServer info: " + "\n" + "Link: https://localhost" + ":" + main.getConfig().getInt("listeningport") + "/");

                        }else {
                            sender.sendMessage(main.prefix + " WebServer info: " + "\n" + "Link: http://localhost" + ":" + main.getConfig().getInt("listeningport") + "/");
                        }
                    }else {
                        if (main.getConfig().getBoolean("EnableSSL")) {
                            sender.sendMessage(main.prefix + " WebServer info: " + "\n" + "Link: https://" + main.getConfig().getString("ServerIP") + ":" + main.getConfig().getInt("listeningport") + "/");
                        }else {
                            sender.sendMessage(main.prefix + " WebServer info: " + "\n" + "Link: http://" + main.getConfig().getString("ServerIP") + ":" + main.getConfig().getInt("listeningport") + "/");

                        }
                    }
                    return true;

                }
                if (args[0].equalsIgnoreCase("reload")) {
                    if (sender.hasPermission("web.reload") || sender.hasPermission("web.*")) {
                        main.reloadConfig();
                        sender.sendMessage(main.prefix + " Configuration file reloaded.");
                        return true;
                    }else {
                        sender.sendMessage(main.prefix + " It appears you do not have the right permissions to do this!");
                        return true;
                    }
                }
                if (args[0].equalsIgnoreCase("reset")) {
                    if (sender.hasPermission("web.reset") || sender.hasPermission("web.*")) {
                        sender.sendMessage(main.prefix + " Starting config reset!");
                        resetconfig(((Player) sender).getPlayer());
                        return true;
                    }else {
                        sender.sendMessage(main.prefix + " It appears you do not have the right permissions to do this!");
                        return true;
                    }
                }
                if (args[0].equalsIgnoreCase("dev")) {
                    if (sender.hasPermission("web.dev") || sender.hasPermission("web.*")) {
                        sender.sendMessage(main.prefix + " This plugin is developed by " + main.getDescription().getAuthors());
                        sender.sendMessage(main.prefix + " Your running version: " + ChatColor.RED + main.getDescription().getVersion());
                        return true;
                    }else {
                        sender.sendMessage(main.prefix + " It appears you do not have the right permissions to do this!");
                        return true;
                    }
                }
                if (args[0].equalsIgnoreCase("help")) {
                    if (sender.hasPermission("web.help") || sender.hasPermission("web.*")) {
                        sender.sendMessage(main.prefix + " Commands: ");
                        sender.sendMessage(main.prefix + " /webp reload reloads the plugin's configuration (NOT RESET)");
                        sender.sendMessage(main.prefix + " /webp dev gets who developed this plugin and plugin version");
                        sender.sendMessage(main.prefix + " /webp ver gets plugin version and checks if there is a new version");
                        sender.sendMessage(main.prefix + " /webp help to get this again.");
                        sender.sendMessage(main.prefix + " /webp reset (WARNING This command WILL reset your config! Backup will be made)");
                        return true;
                    } else {
                        sender.sendMessage(main.prefix + " It appears you do not have the right permissions to do this!");
                        return true;
                    }
                }
                if (args[0].equalsIgnoreCase("ver")) {
                    if (sender.hasPermission("web.ver") || sender.hasPermission("web.*")) {
                        sender.sendMessage(main.prefix + " Your running version: " + ChatColor.RED + main.getDescription().getVersion() + ChatColor.RESET + getver());
                        return true;
                    }else {
                        sender.sendMessage(main.prefix + " It appears you do not have the right permissions to do this!");
                        return true;
                    }
                }
            }
        }
        return true;
    }
    public String getver(){
        if (main.getDescription().getVersion().equals(main.ver)) {
            return " and your running the newest version!";
        }else {
            return " and the newest version is: " + ChatColor.RED + main.ver;
        }
    }
    public void resetconfig(Player sender){
        sender.sendMessage("WARNING You are now resetting your config.yml");
        sender.sendMessage("An backup will be made!");
        File backup = new File(main.getDataFolder(), "config.yml");
        sender.sendMessage("Making backup");
        FileUtil.copy(backup, new File(backup + ".backup"));
        backup.delete();
        sender.sendMessage("Done!");
        sender.sendMessage("config was set to reset!.");
        sender.sendMessage("RECREATING");
        main.saveResource("config.yml", true);
    }
}