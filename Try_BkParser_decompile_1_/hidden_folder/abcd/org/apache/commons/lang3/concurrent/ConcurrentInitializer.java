/*
 * Decompiled with CFR 0.146.
 */
package org.apache.commons.lang3.concurrent;

import org.apache.commons.lang3.concurrent.ConcurrentException;

public interface ConcurrentInitializer<T> {
    public T get() throws ConcurrentException;
}
