/*
 * Decompiled with CFR 0.146.
 */
package org.apache.commons.lang3.time;

import org.apache.commons.lang3.time.DurationFormatUtils;

public class StopWatch {
    private static final long NANO_2_MILLIS = 1000000L;
    private static final int STATE_UNSTARTED = 0;
    private static final int STATE_RUNNING = 1;
    private static final int STATE_STOPPED = 2;
    private static final int STATE_SUSPENDED = 3;
    private static final int STATE_UNSPLIT = 10;
    private static final int STATE_SPLIT = 11;
    private int runningState = 0;
    private int splitState = 10;
    private long startTime;
    private long startTimeMillis;
    private long stopTime;

    public void start() {
        if (this.runningState == 2) {
            throw new IllegalStateException("Stopwatch must be reset before being restarted. ");
        }
        if (this.runningState != 0) {
            throw new IllegalStateException("Stopwatch already started. ");
        }
        this.startTime = System.nanoTime();
        this.startTimeMillis = System.currentTimeMillis();
        this.runningState = 1;
    }

    public void stop() {
        if (this.runningState != 1 && this.runningState != 3) {
            throw new IllegalStateException("Stopwatch is not running. ");
        }
        if (this.runningState == 1) {
            this.stopTime = System.nanoTime();
        }
        this.runningState = 2;
    }

    public void reset() {
        this.runningState = 0;
        this.splitState = 10;
    }

    public void split() {
        if (this.runningState != 1) {
            throw new IllegalStateException("Stopwatch is not running. ");
        }
        this.stopTime = System.nanoTime();
        this.splitState = 11;
    }

    public void unsplit() {
        if (this.splitState != 11) {
            throw new IllegalStateException("Stopwatch has not been split. ");
        }
        this.splitState = 10;
    }

    public void suspend() {
        if (this.runningState != 1) {
            throw new IllegalStateException("Stopwatch must be running to suspend. ");
        }
        this.stopTime = System.nanoTime();
        this.runningState = 3;
    }

    public void resume() {
        if (this.runningState != 3) {
            throw new IllegalStateException("Stopwatch must be suspended to resume. ");
        }
        this.startTime += System.nanoTime() - this.stopTime;
        this.runningState = 1;
    }

    public long getTime() {
        return this.getNanoTime() / 1000000L;
    }

    public long getNanoTime() {
        if (this.runningState == 2 || this.runningState == 3) {
            return this.stopTime - this.startTime;
        }
        if (this.runningState == 0) {
            return 0L;
        }
        if (this.runningState == 1) {
            return System.nanoTime() - this.startTime;
        }
        throw new RuntimeException("Illegal running state has occured. ");
    }

    public long getSplitTime() {
        return this.getSplitNanoTime() / 1000000L;
    }

    public long getSplitNanoTime() {
        if (this.splitState != 11) {
            throw new IllegalStateException("Stopwatch must be split to get the split time. ");
        }
        return this.stopTime - this.startTime;
    }

    public long getStartTime() {
        if (this.runningState == 0) {
            throw new IllegalStateException("Stopwatch has not been started");
        }
        return this.startTimeMillis;
    }

    public String toString() {
        return DurationFormatUtils.formatDurationHMS(this.getTime());
    }

    public String toSplitString() {
        return DurationFormatUtils.formatDurationHMS(this.getSplitTime());
    }
}

