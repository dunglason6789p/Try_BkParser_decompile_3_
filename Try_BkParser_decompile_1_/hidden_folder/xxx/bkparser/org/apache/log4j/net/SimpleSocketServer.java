package org.apache.log4j.net;

import java.net.ServerSocket;
import java.net.Socket;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.xml.DOMConfigurator;

public class SimpleSocketServer {
   static Logger cat;
   static int port;

   public SimpleSocketServer() {
   }

   public static void main(String[] argv) {
      if (argv.length == 2) {
         init(argv[0], argv[1]);
      } else {
         usage("Wrong number of arguments.");
      }

      try {
         cat.info("Listening on port " + port);
         ServerSocket serverSocket = new ServerSocket(port);

         while(true) {
            cat.info("Waiting to accept a new client.");
            Socket socket = serverSocket.accept();
            cat.info("Connected to client at " + socket.getInetAddress());
            cat.info("Starting new socket node.");
            (new Thread(new SocketNode(socket, LogManager.getLoggerRepository()), "SimpleSocketServer-" + port)).start();
         }
      } catch (Exception var3) {
         var3.printStackTrace();
      }
   }

   static void usage(String msg) {
      System.err.println(msg);
      System.err.println("Usage: java " + (class$org$apache$log4j$net$SimpleSocketServer == null ? (class$org$apache$log4j$net$SimpleSocketServer = class$("org.apache.log4j.net.SimpleSocketServer")) : class$org$apache$log4j$net$SimpleSocketServer).getName() + " port configFile");
      System.exit(1);
   }

   static void init(String portStr, String configFile) {
      try {
         port = Integer.parseInt(portStr);
      } catch (NumberFormatException var3) {
         var3.printStackTrace();
         usage("Could not interpret port number [" + portStr + "].");
      }

      if (configFile.endsWith(".xml")) {
         DOMConfigurator.configure(configFile);
      } else {
         PropertyConfigurator.configure(configFile);
      }

   }

   static {
      cat = Logger.getLogger(class$org$apache$log4j$net$SimpleSocketServer == null ? (class$org$apache$log4j$net$SimpleSocketServer = class$("org.apache.log4j.net.SimpleSocketServer")) : class$org$apache$log4j$net$SimpleSocketServer);
   }
}
