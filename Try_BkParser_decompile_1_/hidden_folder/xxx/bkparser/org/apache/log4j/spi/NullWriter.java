package org.apache.log4j.spi;

import java.io.Writer;

/** @deprecated */
class NullWriter extends Writer {
   NullWriter() {
   }

   public void close() {
   }

   public void flush() {
   }

   public void write(char[] cbuf, int off, int len) {
   }
}
