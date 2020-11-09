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

import me.rayzr522.jsonmessage.JSONMessage;
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
            sender.sendMessage(main.prefix + " Webserver info" + "\n" + Bukkit.getServer().getIp() + main.getConfig().getString("listeningport"));
            return true;
        }else {
            if (cmd.getName().equalsIgnoreCase("WebP")) {
                Player p = (Player) sender;
                if (args.length == 0) {
                    sender.sendMessage(main.prefix + " WebServer info: " + "\n" + "Link: " + JSONMessage.create("Go to ").color(ChatColor.DARK_AQUA).then("your webserver").openURL(Bukkit.getServer().getIp() + main.getConfig().getString("listeningport")));
                    return true;

                }

                if (args[0].equalsIgnoreCase("reload")) {
                    main.reloadConfig();
                    sender.sendMessage(main.prefix + " WebServer info: " + "\n" + "Link: " + JSONMessage.create("Go to ").color(ChatColor.DARK_AQUA).then("your webserver").openURL(Bukkit.getServer().getIp() + main.getConfig().getString("listeningport")));
                    return true;
                }
            }
        }
        return true;
    }
}