package org.apache.log4j.net;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Vector;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.helpers.CyclicBuffer;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.spi.LoggingEvent;

public class SocketHubAppender extends AppenderSkeleton {
   static final int DEFAULT_PORT = 4560;
   private int port = 4560;
   private Vector oosList = new Vector();
   private SocketHubAppender.ServerMonitor serverMonitor = null;
   private boolean locationInfo = false;
   private CyclicBuffer buffer = null;
   private String application;
   private boolean advertiseViaMulticastDNS;
   private ZeroConfSupport zeroConf;
   public static final String ZONE = "_log4j_obj_tcpaccept_appender.local.";

   public SocketHubAppender() {
   }

   public SocketHubAppender(int _port) {
      this.port = _port;
      this.startServer();
   }

   public void activateOptions() {
      if (this.advertiseViaMulticastDNS) {
         this.zeroConf = new ZeroConfSupport("_log4j_obj_tcpaccept_appender.local.", this.port, this.getName());
         this.zeroConf.advertise();
      }

      this.startServer();
   }

   public synchronized void close() {
      if (!super.closed) {
         LogLog.debug("closing SocketHubAppender " + this.getName());
         super.closed = true;
         if (this.advertiseViaMulticastDNS) {
            this.zeroConf.unadvertise();
         }

         this.cleanUp();
         LogLog.debug("SocketHubAppender " + this.getName() + " closed");
      }
   }

   public void cleanUp() {
      LogLog.debug("stopping ServerSocket");
      this.serverMonitor.stopMonitor();
      this.serverMonitor = null;
      LogLog.debug("closing client connections");

      while(this.oosList.size() != 0) {
         ObjectOutputStream oos = (ObjectOutputStream)this.oosList.elementAt(0);
         if (oos != null) {
            try {
               oos.close();
            } catch (InterruptedIOException var3) {
               Thread.currentThread().interrupt();
               LogLog.error("could not close oos.", var3);
            } catch (IOException var4) {
               LogLog.error("could not close oos.", var4);
            }

            this.oosList.removeElementAt(0);
         }
      }

   }

   public void append(LoggingEvent event) {
      if (event != null) {
         if (this.locationInfo) {
            event.getLocationInformation();
         }

         if (this.application != null) {
            event.setProperty("application", this.application);
         }

         event.getNDC();
         event.getThreadName();
         event.getMDCCopy();
         event.getRenderedMessage();
         event.getThrowableStrRep();
         if (this.buffer != null) {
            this.buffer.add(event);
         }
      }

      if (event != null && this.oosList.size() != 0) {
         for(int streamCount = 0; streamCount < this.oosList.size(); ++streamCount) {
            ObjectOutputStream oos = null;

            try {
               oos = (ObjectOutputStream)this.oosList.elementAt(streamCount);
            } catch (ArrayIndexOutOfBoundsException var5) {
            }

            if (oos == null) {
               break;
            }

            try {
               oos.writeObject(event);
               oos.flush();
               oos.reset();
            } catch (IOException var6) {
               if (var6 instanceof InterruptedIOException) {
                  Thread.currentThread().interrupt();
               }

               this.oosList.removeElementAt(streamCount);
               LogLog.debug("dropped connection");
               --streamCount;
            }
         }

      }
   }

   public boolean requiresLayout() {
      return false;
   }

   public void setPort(int _port) {
      this.port = _port;
   }

   public void setApplication(String lapp) {
      this.application = lapp;
   }

   public String getApplication() {
      return this.application;
   }

   public int getPort() {
      return this.port;
   }

   public void setBufferSize(int _bufferSize) {
      this.buffer = new CyclicBuffer(_bufferSize);
   }

   public int getBufferSize() {
      return this.buffer == null ? 0 : this.buffer.getMaxSize();
   }

   public void setLocationInfo(boolean _locationInfo) {
      this.locationInfo = _locationInfo;
   }

   public boolean getLocationInfo() {
      return this.locationInfo;
   }

   public void setAdvertiseViaMulticastDNS(boolean advertiseViaMulticastDNS) {
      this.advertiseViaMulticastDNS = advertiseViaMulticastDNS;
   }

   public boolean isAdvertiseViaMulticastDNS() {
      return this.advertiseViaMulticastDNS;
   }

   private void startServer() {
      this.serverMonitor = new SocketHubAppender.ServerMonitor(this.port, this.oosList);
   }

   protected ServerSocket createServerSocket(int socketPort) throws IOException {
      return new ServerSocket(socketPort);
   }

   private class ServerMonitor implements Runnable {
      private int port;
      private Vector oosList;
      private boolean keepRunning;
      private Thread monitorThread;

      public ServerMonitor(int _port, Vector _oosList) {
         this.port = _port;
         this.oosList = _oosList;
         this.keepRunning = true;
         this.monitorThread = new Thread(this);
         this.monitorThread.setDaemon(true);
         this.monitorThread.setName("SocketHubAppender-Monitor-" + this.port);
         this.monitorThread.start();
      }

      public synchronized void stopMonitor() {
         if (this.keepRunning) {
            LogLog.debug("server monitor thread shutting down");
            this.keepRunning = false;

            try {
               this.monitorThread.join();
            } catch (InterruptedException var2) {
               Thread.currentThread().interrupt();
            }

            this.monitorThread = null;
            LogLog.debug("server monitor thread shut down");
         }

      }

      private void sendCachedEvents(ObjectOutputStream stream) throws IOException {
         if (SocketHubAppender.this.buffer != null) {
            for(int i = 0; i < SocketHubAppender.this.buffer.length(); ++i) {
               stream.writeObject(SocketHubAppender.this.buffer.get(i));
            }

            stream.flush();
            stream.reset();
         }

      }

      public void run() {
         ServerSocket serverSocket = null;

         try {
            serverSocket = SocketHubAppender.this.createServerSocket(this.port);
            serverSocket.setSoTimeout(1000);
         } catch (Exception var25) {
            if (var25 instanceof InterruptedIOException || var25 instanceof InterruptedException) {
               Thread.currentThread().interrupt();
            }

            LogLog.error("exception setting timeout, shutting down server socket.", var25);
            this.keepRunning = false;
            return;
         }

         try {
            try {
               serverSocket.setSoTimeout(1000);
            } catch (SocketException var27) {
               LogLog.error("exception setting timeout, shutting down server socket.", var27);
               return;
            }

            while(this.keepRunning) {
               Socket socket = null;

               try {
                  socket = serverSocket.accept();
               } catch (InterruptedIOException var22) {
               } catch (SocketException var23) {
                  LogLog.error("exception accepting socket, shutting down server socket.", var23);
                  this.keepRunning = false;
               } catch (IOException var24) {
                  LogLog.error("exception accepting socket.", var24);
               }

               if (socket != null) {
                  try {
                     InetAddress remoteAddress = socket.getInetAddress();
                     LogLog.debug("accepting connection from " + remoteAddress.getHostName() + " (" + remoteAddress.getHostAddress() + ")");
                     ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                     if (SocketHubAppender.this.buffer != null && SocketHubAppender.this.buffer.length() > 0) {
                        this.sendCachedEvents(oos);
                     }

                     this.oosList.addElement(oos);
                  } catch (IOException var26) {
                     if (var26 instanceof InterruptedIOException) {
                        Thread.currentThread().interrupt();
                     }

                     LogLog.error("exception creating output stream on socket.", var26);
                  }
               }
            }

         } finally {
            try {
               serverSocket.close();
            } catch (InterruptedIOException var20) {
               Thread.currentThread().interrupt();
            } catch (IOException var21) {
            }

         }
      }
   }
}
