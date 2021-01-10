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
import org.bukkit.plugin.java.JavaPlugin;

import javax.net.ssl.*;
import java.io.*;
import java.security.KeyStore;
import java.util.Date;
import java.util.StringTokenizer;

public class SSLAcceptedSocketConnection{

    Main main = JavaPlugin.getPlugin(Main.class);


    String DEFAULT_FILE = "index.html";
    String DEFAULT_FILE2 = "index.php";

    public void run() {
        String ksName = "plugins/WebPlugin/ssl/" + main.getConfig().getString("SSLJKSName").toString() + ".jks";
        char ksPass[] = main.getConfig().getString("SSLJKSPass").toString().toCharArray();
        char ctPass[] = main.getConfig().getString("SSLJKSKey").toString().toCharArray();
        BufferedReader in = null; PrintWriter out = null; BufferedOutputStream dataOut = null;
        String fileRequested = null;
        try {
            main.getServer().getLogger().info("trying to start SSL");
            if (!(new File("plugins/WebPlugin/ssl/" + main.getConfig().getString("SSLJKSName") + ".jks").exists())){
                main.getLogger().info("SSL key not found!");
                main.getLogger().info("Shutting down plugin to prevent damage!");
                Bukkit.getScheduler().cancelTasks(main);
                Bukkit.getPluginManager().disablePlugin(main);
            }
            KeyStore ks = KeyStore.getInstance("JKS");
            ks.load(new FileInputStream(ksName), ksPass);
            KeyManagerFactory kmf =
                    KeyManagerFactory.getInstance("SunX509");
            kmf.init(ks, ctPass);
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(kmf.getKeyManagers(), null, null);
            SSLServerSocketFactory ssf = sc.getServerSocketFactory();
            SSLServerSocket s = (SSLServerSocket) ssf.createServerSocket(main.getConfig().getInt("listeningportssl"));
            SSLSocket c = (SSLSocket) s.accept();
            main.getServer().getLogger().info("SSL websocket started");


            in = new BufferedReader(new InputStreamReader(c.getInputStream()));
            out = new PrintWriter(c.getOutputStream());
            dataOut = new BufferedOutputStream(c.getOutputStream());
            // get first line of the request from the client
            String input = in.readLine();
            // we parse the request with a string tokenizer
            StringTokenizer parse = new StringTokenizer(input);
            // we get file requested
            fileRequested = parse.nextToken().toLowerCase();
            String contentMimeType = "text/html";

            String s2;
            int counterr = 0, contentLength = 0;
            try {
                while (!(s2 = in.readLine()).equals("")) {
                    if (counterr == 0 && s2.equalsIgnoreCase(Main.closeConnection)) {
                        out.close();
                        in.close();
                        s.close();

                        return;
                    }
                    if (s2.startsWith("Content-Length: ")) {
                        contentLength = Integer.parseInt(s2.split("Length: ")[1]);
                    }
                    counterr++;
                }
            } catch (IOException e) {
                main.getServer().getLogger().info("This is not an error and should not be reported.");
                main.getServer().getLogger().info("Counting failed!");
            }

            String finalString = "";
            for (int i = 0; i < contentLength; i++) {
                finalString += (char) in.read();
            }

            //This section is the response to the clients request, the web page:
            if (fileRequested.endsWith("/")) {
                if (main.getConfig().getBoolean("UseHtml") && !main.getConfig().getBoolean("UsePHP")) {
                    fileRequested += DEFAULT_FILE;
                } else if (!main.getConfig().getBoolean("UseHtml") && main.getConfig().getBoolean("UsePHP")) {
                    fileRequested += DEFAULT_FILE2;
                }
            }

            File file = new File(main.getDataFolder() + "/web/", fileRequested);
            int fileLength = (int) file.length();
            String content = getContentType(fileRequested);

            try {
                byte[] fileData = readFileData(file, fileLength);

                // send HTTPS Headers
                out.write("HTTP/1.1 200 OK");
                out.write("Server: Java HTTP Server from SSaurel : 1.0");
                out.write("HTTP/1.1 200 OK");
                out.println("Content-type: " + content);
                out.println(); // blank line between headers and content, very important !
                out.flush(); // flush character output stream buffer

                dataOut.write(fileData, 0, fileLength);
                dataOut.flush();
            } catch (IOException e) {
                main.getServer().getLogger().info("This is not an error and should not be reported.");
                main.getServer().getLogger().info("Writing failed!");
            }
            out.close();
            in.close();
            c.close();
        } catch (Exception e) {
            if (main.getConfig().getBoolean("debug")) {
                e.printStackTrace();
            }
        }
    }

    private byte[] readFileData(File file, int fileLength) throws IOException {
        FileInputStream fileIn = null;
        byte[] fileData = new byte[fileLength];

        try {
            fileIn = new FileInputStream(file);
            fileIn.read(fileData);
        } catch (IOException e){
            main.getServer().getLogger().info("This is not an error and should not be reported.");
            main.getServer().getLogger().info("File: " + file + " Could not be found!");
        }finally {
            if (fileIn != null)
                fileIn.close();
        }

        return fileData;
    }

    // return supported MIME Types
    private String getContentType(String fileRequested) {
        if (fileRequested.endsWith(".htm")  ||  fileRequested.endsWith(".html")) {
            return "text/html";
        }else if (fileRequested.endsWith(".css")) {
            return "text/css";
        }else if (fileRequested.endsWith(".js")) {
            return "application/x-javascript";
        }else if (fileRequested.endsWith(".svg")){
            return "image/svg+xml";
        }else{
            return "text/plain";
        }

    }
}


