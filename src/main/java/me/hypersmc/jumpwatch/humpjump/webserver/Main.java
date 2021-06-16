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

import me.hypersmc.jumpwatch.humpjump.webserver.PHP.PhPGetter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.FileUtil;

import javax.net.ssl.SSLSocket;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;
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
    private int version = 3;
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
    private static String OS = System.getProperty("os.name").toLowerCase();
    public static boolean isWindows() {
        return (OS.indexOf("win") >= 0);
    }
    public static boolean isUnix() {
        return (OS.indexOf("nix") >= 0
                || OS.indexOf("nux") >= 0
                || OS.indexOf("aix") > 0);
    }

    @Override
    public void onEnable() {
        this.getLogger().info("Your current OS name: " + OS);
        /*
        Checking config version and makes sure it matches up to default version.
         */
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
        /*
        Checking if there is any plugin update available.
         */
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
        /*
        Adds plugin command.
         */
        this.getCommand("WebP").setExecutor(new MainCommand());
        /*
        Checking config values, and make sure that some that shouldn't be enabled aren't on startup.
         */
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
        /*
        When all checks matches up we can now start the actual webserver.
        We make sure to check if ssl is on or not.
         */

        if (getConfig().getBoolean("UsePHP")){
            if (new File("plugins/WebPlugin/phpcore").exists()) {
                logger.info("php files exist.");
            }else {
                logger.info("started downloading phpcore");
                PhPGetter.PhPGetter();
            }
        }
        if (getConfig().getBoolean("UseHtml")) {
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
            if (getConfig().getBoolean("EnableSSL")) {
                new SSLAcceptedSocketConnection().run();
            }
        }else if (getConfig().getBoolean("UsePHP")){
            runweb(this);
        }
    }

    @Override
    public void onDisable() {
        if (getConfig().getBoolean("UseHtml")) {
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
        stopphpcore();
    }
    public void stopphpcore(){
        try {
            String command = this.getDataFolder() + "/phpcore/nginx-1.20.1/nginx.exe -s quit -p " + getDataFolder() + "/phpcore/nginx-1.20.1/";
            String line;
            if (isWindows()) {
                Process p = Runtime.getRuntime().exec(command);
                if (this.getConfig().getBoolean("debug")) {
                    BufferedReader bri = new BufferedReader
                            (new InputStreamReader(p.getInputStream()));
                    BufferedReader bre = new BufferedReader
                            (new InputStreamReader(p.getErrorStream()));
                    while ((line = bri.readLine()) != null) {
                        System.out.println(line);
                    }
                    bri.close();
                    while ((line = bre.readLine()) != null) {
                        System.out.println(line);
                    }
                    bre.close();
                    p.waitFor();
                    System.out.println("Done.");
                }
            }else if (isUnix()) {
                this.getLogger().info("Your os is: "+OS);
                this.getLogger().info("Sorry but for now only Windows is supported on php. Linux is comming soon!");

            }
        } catch (IOException | InterruptedException e) {
            if (this.getConfig().getBoolean("debug")) {
                e.printStackTrace();
            }
        }
    }
    public void reloadphpcore(){
        try {
            String command = this.getDataFolder() + "/phpcore/nginx-1.20.1/nginx.exe -s reload -p " + getDataFolder() + "/phpcore/nginx-1.20.1/";
            String line;
            if (isWindows()) {
                Process p = Runtime.getRuntime().exec(command);
                if (this.getConfig().getBoolean("debug")) {
                    BufferedReader bri = new BufferedReader
                            (new InputStreamReader(p.getInputStream()));
                    BufferedReader bre = new BufferedReader
                            (new InputStreamReader(p.getErrorStream()));
                    while ((line = bri.readLine()) != null) {
                        System.out.println(line);
                    }
                    bri.close();
                    while ((line = bre.readLine()) != null) {
                        System.out.println(line);
                    }
                    bre.close();
                    p.waitFor();
                    System.out.println("Done.");
                }
            }else if (isUnix()) {
                this.getLogger().info("Your os is: "+OS);
                this.getLogger().info("Sorry but for now only Windows is supported on php. Linux is comming soon!");

            }
        } catch (IOException | InterruptedException e) {
            if (this.getConfig().getBoolean("debug")) {
                e.printStackTrace();
            }
        }
    }
    public void startphpcore(){
        this.getLogger().info("checking file permissions");
        FilePermissions();
        this.getLogger().info("Making sure that right ip and port is set.");
        Changeconf(this.getDataFolder() + "/phpcore/nginx-1.20.1/conf/nginx.conf", "localhost", this.getConfig().getString("listeningport") + "");
        Changeconf(this.getDataFolder() + "/phpcore/nginx-1.20.1/conf/nginx.conf", "80", this.getConfig().getString("ServerIP") + "");
        this.getLogger().info("Success!");

        try {

            String command = this.getDataFolder() + "/phpcore/nginx-1.20.1/nginx.exe -p " + getDataFolder() + "/phpcore/nginx-1.20.1/";
            String line;

            if (isWindows()) {
                System.out.println("found windows.");
                Process p = Runtime.getRuntime().exec(command);
                if (this.getConfig().getBoolean("debug")) {
                    BufferedReader bri = new BufferedReader
                            (new InputStreamReader(p.getInputStream()));
                    BufferedReader bre = new BufferedReader
                            (new InputStreamReader(p.getErrorStream()));
                    while ((line = bri.readLine()) != null) {
                        System.out.println(line);
                    }
                    bri.close();
                    while ((line = bre.readLine()) != null) {
                        System.out.println(line);
                    }
                    bre.close();
                    p.waitFor();
                    System.out.println("Done.");
                }
            } else if (isUnix()) {
                this.getLogger().info("Your os is: "+OS);
                this.getLogger().info("Sorry but for now only Windows is supported on php. Linux is comming soon!");

            }




            this.getLogger().info("Started PHP webserver on:");
            this.getLogger().info(this.getConfig().getString("ServerIP") + ":" + this.getConfig().getString("listeningport"));
        } catch (IOException | InterruptedException e) {
            if (this.getConfig().getBoolean("debug")) {
                e.printStackTrace();
            }
        }
    }
    public void runweb(JavaPlugin instance) {
        new BukkitRunnable() {
            @Override
            public void run() {
                startphpcore();
            }

        }.runTaskAsynchronously(instance);
    }

    static void Changeconf(String filePath, String oldString, String newString)
    {
        File fileToBeModified = new File(filePath);

        String oldContent = "";

        BufferedReader reader = null;

        FileWriter writer = null;

        try
        {
            reader = new BufferedReader(new FileReader(fileToBeModified));

            //Reading all the lines of input text file into oldContent

            String line = reader.readLine();

            while (line != null)
            {
                oldContent = oldContent + line + System.lineSeparator();

                line = reader.readLine();
            }

            //Replacing oldString with newString in the oldContent

            String newContent = oldContent.replaceAll(oldString, newString);

            //Rewriting the input text file with newContent

            writer = new FileWriter(fileToBeModified);

            writer.write(newContent);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                //Closing the resources

                reader.close();

                writer.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }
    /*public static void changeProperty(String filename, String key, String value) throws IOException {
        final File tmpFile = new File(filename + ".tmp");
        final File file = new File(filename);
        PrintWriter pw = new PrintWriter(tmpFile);
        BufferedReader br = new BufferedReader(new FileReader(file));
        boolean found = false;
        final String toAdd = key + ' ' + value;
        for (String line; (line = br.readLine()) != null; ) {
            if (line.startsWith(key + ' ')) {
                line = toAdd;
                found = true;
            }
            pw.println(line);
        }
        if (!found)
            pw.println(toAdd);
        br.close();
        pw.close();
        tmpFile.renameTo(file);
    }*/

    private void FilePermissions(){
        File nginx = new File(this.getDataFolder() + "/phpcore/nginx-1.20.1/nginx.exe");
        File nginxfolder = new File(this.getDataFolder() + "/phpcore/nginx-1.20.1/");
        File corefolder = new File(this.getDataFolder() + "/phpcore/");
        try {
            nginx.setExecutable(true, false);
            nginx.setReadable(true, false);
            nginx.setWritable(true, false);
            nginxfolder.setExecutable(true, false);
            nginxfolder.setReadable(true, false);
            nginxfolder.setWritable(true, false);
            corefolder.setExecutable(true, false);
            corefolder.setReadable(true, false);
            corefolder.setWritable(true, false);
            this.getLogger().info("File permission check success!");
        } catch (Exception e) {
            this.getLogger().info("Failed to check file permission. Please enable debug mode.");
            if (this.getConfig().getBoolean("debug")) {
                e.printStackTrace();
            }
        }
    }
}
