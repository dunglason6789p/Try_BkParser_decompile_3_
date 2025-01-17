/*
 * Decompiled with CFR 0.146.
 */
package org.apache.log4j.net;

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.net.SocketException;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.apache.log4j.spi.LoggerRepository;
import org.apache.log4j.spi.LoggingEvent;

public class SocketNode
implements Runnable {
    Socket socket;
    LoggerRepository hierarchy;
    ObjectInputStream ois;
    static Logger logger = Logger.getLogger(class$org$apache$log4j$net$SocketNode == null ? (class$org$apache$log4j$net$SocketNode = SocketNode.class$("org.apache.log4j.net.SocketNode")) : class$org$apache$log4j$net$SocketNode);
    static /* synthetic */ Class class$org$apache$log4j$net$SocketNode;

    public SocketNode(Socket socket, LoggerRepository hierarchy) {
        this.socket = socket;
        this.hierarchy = hierarchy;
        try {
            this.ois = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
        }
        catch (InterruptedIOException e) {
            Thread.currentThread().interrupt();
            logger.error("Could not open ObjectInputStream to " + socket, e);
        }
        catch (IOException e) {
            logger.error("Could not open ObjectInputStream to " + socket, e);
        }
        catch (RuntimeException e) {
            logger.error("Could not open ObjectInputStream to " + socket, e);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void run() {
        try {
            if (this.ois != null) {
                do {
                    LoggingEvent event = (LoggingEvent)this.ois.readObject();
                    Logger remoteLogger = this.hierarchy.getLogger(event.getLoggerName());
                    if (!event.getLevel().isGreaterOrEqual(remoteLogger.getEffectiveLevel())) continue;
                    remoteLogger.callAppenders(event);
                } while (true);
            }
        }
        catch (EOFException e) {
            logger.info("Caught java.io.EOFException closing conneciton.");
        }
        catch (SocketException e) {
            logger.info("Caught java.net.SocketException closing conneciton.");
        }
        catch (InterruptedIOException e) {
            Thread.currentThread().interrupt();
            logger.info("Caught java.io.InterruptedIOException: " + e);
            logger.info("Closing connection.");
        }
        catch (IOException e) {
            logger.info("Caught java.io.IOException: " + e);
            logger.info("Closing connection.");
        }
        catch (Exception e) {
            logger.error("Unexpected exception. Closing conneciton.", e);
        }
        finally {
            if (this.ois != null) {
                try {
                    this.ois.close();
                }
                catch (Exception e) {
                    logger.info("Could not close connection.", e);
                }
            }
            if (this.socket != null) {
                try {
                    this.socket.close();
                }
                catch (InterruptedIOException e) {
                    Thread.currentThread().interrupt();
                }
                catch (IOException ex) {}
            }
        }
    }

    static /* synthetic */ Class class$(String x0) {
        try {
            return Class.forName(x0);
        }
        catch (ClassNotFoundException x1) {
            throw new NoClassDefFoundError(x1.getMessage());
        }
    }
}

