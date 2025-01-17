package org.apache.log4j.helpers;

import java.io.IOException;
import java.io.Writer;
import org.apache.log4j.spi.ErrorHandler;

public class CountingQuietWriter extends QuietWriter {
   protected long count;

   public CountingQuietWriter(Writer writer, ErrorHandler eh) {
      super(writer, eh);
   }

   public void write(String string) {
      try {
         super.out.write(string);
         this.count += (long)string.length();
      } catch (IOException var3) {
         super.errorHandler.error("Write failure.", var3, 1);
      }

   }

   public long getCount() {
      return this.count;
   }

   public void setCount(long count) {
      this.count = count;
   }
}
