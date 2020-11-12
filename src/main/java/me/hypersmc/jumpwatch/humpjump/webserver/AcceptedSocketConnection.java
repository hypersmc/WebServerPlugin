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

import com.sun.istack.internal.Nullable;

import java.io.*;
import java.net.Socket;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.Date;
import java.util.StringTokenizer;
import javax.net.ssl.*;

public class AcceptedSocketConnection extends Thread{
    Socket sock;
    Main plugin;
    String ksName = plugin.getConfig().getString("SSL.SSLJKSName") + ".jks";
    char ksPass[] = plugin.getConfig().getString("SSL.SSLJKSPass").toCharArray();
    char ctPass[] = plugin.getConfig().getString("SSL.SSLJKSKey").toCharArray();
    String DEFAULT_FILE = "index.html";
    String DEFAULT_FILE2 = "index.php";
    public AcceptedSocketConnection(Socket sock, Main plugin){
        this.sock = sock;
        this.plugin = plugin;

    }
    @Override
    public void run() {
        BufferedReader in = null; PrintWriter out = null; BufferedOutputStream dataOut = null;
        String fileRequested = null;
        if (!(plugin.getConfig().getBoolean("EnableSSL"))) {
            try {
                in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
                out = new PrintWriter(sock.getOutputStream());
                dataOut = new BufferedOutputStream(sock.getOutputStream());
                // get first line of the request from the client
                String input = in.readLine();
                // we parse the request with a string tokenizer
                StringTokenizer parse = new StringTokenizer(input);
                String method = parse.nextToken().toUpperCase(); // we get the HTTP method of the client
                // we get file requested
                fileRequested = parse.nextToken().toLowerCase();
                String contentMimeType = "text/html";

                String s;
                int counterr = 0, contentLength = 0;
                //boolean gotEmptyLine = false;//TODO Remember why I did this line lol
                try {
                    while (!(s = in.readLine()).equals("")) {
                        if (counterr == 0 && s.equalsIgnoreCase(Main.closeConnection)) {
                            out.close();
                            in.close();
                            sock.close();

                            return;
                        }
                        if (s.startsWith("Content-Length: ")) {
                            contentLength = Integer.parseInt(s.split("Length: ")[1]);
                        }
                        counterr++;
                    }
                } catch (IOException e) {
                    plugin.getServer().getLogger().info("This is not an error and should not be reported.");
                    plugin.getServer().getLogger().info("Counting failed!");
                }

                String finalString = "";
                for (int i = 0; i < contentLength; i++) {
                    finalString += (char) in.read();
                }

                //This section is the response to the clients request, the web page:
                if (fileRequested.endsWith("/")) {
                    if (plugin.getConfig().getBoolean("UseHtml") && !plugin.getConfig().getBoolean("UsePHP")) {
                        fileRequested += DEFAULT_FILE;
                    } else if (!plugin.getConfig().getBoolean("UseHtml") && plugin.getConfig().getBoolean("UsePHP")) {
                        fileRequested += DEFAULT_FILE2;
                    }
                }

                File file = new File(plugin.getDataFolder() + "/web/", fileRequested);
                int fileLength = (int) file.length();
                String content = getContentType(fileRequested);

                if (method.equals("GET")) { // GET method so we return content
                    try {
                        byte[] fileData = readFileData(file, fileLength);

                        // send HTTP Headers
                        out.write("HTTP/1.1 200 OK");
                        out.write("Server: Java HTTP Server from SSaurel : 1.0");
                        out.println("Set-Cookie: Max-Age=0; Secure; HttpOnly");
                        out.println("Date: " + new Date());
                        out.println("Content-type: " + content);
                        out.println(); // blank line between headers and content, very important !
                        out.flush(); // flush character output stream buffer

                        dataOut.write(fileData, 0, fileLength);
                        dataOut.flush();
                    } catch (IOException e) {
                        plugin.getServer().getLogger().info("This is not an error and should not be reported.");
                        plugin.getServer().getLogger().info("Writing failed!");
                    }
                }
                out.close();
                in.close();
                sock.close();

            } catch (IOException e) {
                if (plugin.getConfig().getBoolean("debug")) {
                    e.printStackTrace();
                }
            }
        }
        if (plugin.getConfig().getBoolean("EnableSSL")) {
            try {
                KeyStore ks = KeyStore.getInstance("JKS");
                ks.load(new FileInputStream(ksName), ksPass);
                KeyManagerFactory kmf =
                        KeyManagerFactory.getInstance("SunX509");
                kmf.init(ks, ctPass);
                SSLContext sc = SSLContext.getInstance("TLS");
                sc.init(kmf.getKeyManagers(), null, null);
                SSLServerSocketFactory ssf = sc.getServerSocketFactory();
                SSLServerSocket s = (SSLServerSocket) ssf.createServerSocket(8888);
                SSLSocket c = (SSLSocket) s.accept();

                in = new BufferedReader(new InputStreamReader(c.getInputStream()));
                out = new PrintWriter(c.getOutputStream());
                dataOut = new BufferedOutputStream(c.getOutputStream());
                // get first line of the request from the client
                String input = in.readLine();
                // we parse the request with a string tokenizer
                StringTokenizer parse = new StringTokenizer(input);
                String method = parse.nextToken().toUpperCase(); // we get the HTTP method of the client
                // we get file requested
                fileRequested = parse.nextToken().toLowerCase();
                String contentMimeType = "text/html";

                String s2;
                int counterr = 0, contentLength = 0;
                //boolean gotEmptyLine = false;//TODO Remember why I did this line lol
                try {
                    while (!(s2 = in.readLine()).equals("")) {
                        if (counterr == 0 && s2.equalsIgnoreCase(Main.closeConnection)) {
                            out.close();
                            in.close();
                            sock.close();

                            return;
                        }
                        if (s2.startsWith("Content-Length: ")) {
                            contentLength = Integer.parseInt(s2.split("Length: ")[1]);
                        }
                        counterr++;
                    }
                } catch (IOException e) {
                    plugin.getServer().getLogger().info("This is not an error and should not be reported.");
                    plugin.getServer().getLogger().info("Counting failed!");
                }

                String finalString = "";
                for (int i = 0; i < contentLength; i++) {
                    finalString += (char) in.read();
                }

                //This section is the response to the clients request, the web page:
                if (fileRequested.endsWith("/")) {
                    if (plugin.getConfig().getBoolean("UseHtml") && !plugin.getConfig().getBoolean("UsePHP")) {
                        fileRequested += DEFAULT_FILE;
                    } else if (!plugin.getConfig().getBoolean("UseHtml") && plugin.getConfig().getBoolean("UsePHP")) {
                        fileRequested += DEFAULT_FILE2;
                    }
                }

                File file = new File(plugin.getDataFolder() + "/web/", fileRequested);
                int fileLength = (int) file.length();
                String content = getContentType(fileRequested);

                if (method.equals("GET")) { // GET method so we return content
                    try {
                        byte[] fileData = readFileData(file, fileLength);

                        // send HTTP Headers
                        out.write("HTTP/1.1 200 OK");
                        out.write("Server: Java HTTP Server from SSaurel : 1.0");
                        out.println("Set-Cookie: Max-Age=0; Secure; HttpOnly");
                        out.println("Date: " + new Date());
                        out.println("Content-type: " + content);
                        out.println(); // blank line between headers and content, very important !
                        out.flush(); // flush character output stream buffer

                        dataOut.write(fileData, 0, fileLength);
                        dataOut.flush();
                    } catch (IOException e) {
                        plugin.getServer().getLogger().info("This is not an error and should not be reported.");
                        plugin.getServer().getLogger().info("Writing failed!");
                    }
                }
                out.close();
                in.close();
                c.close();
            } catch (Exception e) {
                if (plugin.getConfig().getBoolean("debug")) {
                    e.printStackTrace();
                }
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
            plugin.getServer().getLogger().info("This is not an error and should not be reported.");
            plugin.getServer().getLogger().info("File: " + file + " Could not be found!");
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