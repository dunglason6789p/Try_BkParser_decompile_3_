/*
 * Decompiled with CFR 0.146.
 */
package org.apache.log4j.helpers;

import java.io.InterruptedIOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.helpers.OptionConverter;

public class Loader {
    static final String TSTR = "Caught Exception while in Loader.getResource. This may be innocuous.";
    private static boolean java1;
    private static boolean ignoreTCL;
    static /* synthetic */ Class class$org$apache$log4j$helpers$Loader;
    static /* synthetic */ Class class$java$lang$Thread;

    public static URL getResource(String resource, Class clazz) {
        return Loader.getResource(resource);
    }

    public static URL getResource(String resource) {
        ClassLoader classLoader = null;
        URL url = null;
        try {
            if (!java1 && !ignoreTCL && (classLoader = Loader.getTCL()) != null) {
                LogLog.debug("Trying to find [" + resource + "] using context classloader " + classLoader + ".");
                url = classLoader.getResource(resource);
                if (url != null) {
                    return url;
                }
            }
            if ((classLoader = (class$org$apache$log4j$helpers$Loader == null ? (class$org$apache$log4j$helpers$Loader = Loader.class$("org.apache.log4j.helpers.Loader")) : class$org$apache$log4j$helpers$Loader).getClassLoader()) != null) {
                LogLog.debug("Trying to find [" + resource + "] using " + classLoader + " class loader.");
                url = classLoader.getResource(resource);
                if (url != null) {
                    return url;
                }
            }
        }
        catch (IllegalAccessException t) {
            LogLog.warn(TSTR, t);
        }
        catch (InvocationTargetException t) {
            if (t.getTargetException() instanceof InterruptedException || t.getTargetException() instanceof InterruptedIOException) {
                Thread.currentThread().interrupt();
            }
            LogLog.warn(TSTR, t);
        }
        catch (Throwable t) {
            LogLog.warn(TSTR, t);
        }
        LogLog.debug("Trying to find [" + resource + "] using ClassLoader.getSystemResource().");
        return ClassLoader.getSystemResource(resource);
    }

    public static boolean isJava1() {
        return java1;
    }

    private static ClassLoader getTCL() throws IllegalAccessException, InvocationTargetException {
        Method method = null;
        try {
            method = (class$java$lang$Thread == null ? (class$java$lang$Thread = Loader.class$("java.lang.Thread")) : class$java$lang$Thread).getMethod("getContextClassLoader", null);
        }
        catch (NoSuchMethodException e) {
            return null;
        }
        return (ClassLoader)method.invoke(Thread.currentThread(), null);
    }

    public static Class loadClass(String clazz) throws ClassNotFoundException {
        if (java1 || ignoreTCL) {
            return Class.forName(clazz);
        }
        try {
            return Loader.getTCL().loadClass(clazz);
        }
        catch (InvocationTargetException e) {
            if (e.getTargetException() instanceof InterruptedException || e.getTargetException() instanceof InterruptedIOException) {
                Thread.currentThread().interrupt();
            }
        }
        catch (Throwable t) {
            // empty catch block
        }
        return Class.forName(clazz);
    }

    static /* synthetic */ Class class$(String x0) {
        try {
            return Class.forName(x0);
        }
        catch (ClassNotFoundException x1) {
            throw new NoClassDefFoundError(x1.getMessage());
        }
    }

    static {
        int i;
        String ignoreTCLProp;
        java1 = true;
        ignoreTCL = false;
        String prop = OptionConverter.getSystemProperty("java.version", null);
        if (prop != null && (i = prop.indexOf(46)) != -1 && prop.charAt(i + 1) != '1') {
            java1 = false;
        }
        if ((ignoreTCLProp = OptionConverter.getSystemProperty("log4j.ignoreTCL", null)) != null) {
            ignoreTCL = OptionConverter.toBoolean(ignoreTCLProp, true);
        }
    }
}

