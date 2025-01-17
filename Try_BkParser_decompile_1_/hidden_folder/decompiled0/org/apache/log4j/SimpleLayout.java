/*
 * Decompiled with CFR 0.146.
 */
package org.apache.log4j;

import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;

public class SimpleLayout
extends Layout {
    StringBuffer sbuf = new StringBuffer(128);

    public void activateOptions() {
    }

    public String format(LoggingEvent event) {
        this.sbuf.setLength(0);
        this.sbuf.append(event.getLevel().toString());
        this.sbuf.append(" - ");
        this.sbuf.append(event.getRenderedMessage());
        this.sbuf.append(Layout.LINE_SEP);
        return this.sbuf.toString();
    }

    public boolean ignoresThrowable() {
        return true;
    }
}

