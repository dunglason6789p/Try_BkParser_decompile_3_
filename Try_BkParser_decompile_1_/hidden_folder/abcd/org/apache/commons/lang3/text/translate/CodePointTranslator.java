/*
 * Decompiled with CFR 0.146.
 */
package org.apache.commons.lang3.text.translate;

import java.io.IOException;
import java.io.Writer;
import org.apache.commons.lang3.text.translate.CharSequenceTranslator;

public abstract class CodePointTranslator
extends CharSequenceTranslator {
    public final int translate(CharSequence input, int index, Writer out) throws IOException {
        int codepoint = Character.codePointAt(input, index);
        boolean consumed = this.translate(codepoint, out);
        return consumed;
    }

    public abstract boolean translate(int var1, Writer var2) throws IOException;
}

