package org.apache.log4j.helpers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.util.Set;
import org.apache.log4j.pattern.LogEvent;
import org.apache.log4j.spi.LoggingEvent;

public final class MDCKeySetExtractor {
   private final Method getKeySetMethod;
   public static final MDCKeySetExtractor INSTANCE = new MDCKeySetExtractor();

   private MDCKeySetExtractor() {
      Method getMethod = null;

      try {
         getMethod = (class$org$apache$log4j$spi$LoggingEvent == null ? (class$org$apache$log4j$spi$LoggingEvent = class$("org.apache.log4j.spi.LoggingEvent")) : class$org$apache$log4j$spi$LoggingEvent).getMethod("getPropertyKeySet", (Class[])null);
      } catch (Exception var3) {
         getMethod = null;
      }

      this.getKeySetMethod = getMethod;
   }

   public Set getPropertyKeySet(LoggingEvent event) throws Exception {
      Set keySet = null;
      if (this.getKeySetMethod != null) {
         keySet = (Set)this.getKeySetMethod.invoke(event, (Object[])null);
      } else {
         ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
         ObjectOutputStream os = new ObjectOutputStream(outBytes);
         os.writeObject(event);
         os.close();
         byte[] raw = outBytes.toByteArray();
         String subClassName = (class$org$apache$log4j$pattern$LogEvent == null ? (class$org$apache$log4j$pattern$LogEvent = class$("org.apache.log4j.pattern.LogEvent")) : class$org$apache$log4j$pattern$LogEvent).getName();
         if (raw[6] == 0 || raw[7] == subClassName.length()) {
            for(int i = 0; i < subClassName.length(); ++i) {
               raw[8 + i] = (byte)subClassName.charAt(i);
            }

            ByteArrayInputStream inBytes = new ByteArrayInputStream(raw);
            ObjectInputStream is = new ObjectInputStream(inBytes);
            Object cracked = is.readObject();
            if (cracked instanceof LogEvent) {
               keySet = ((LogEvent)cracked).getPropertyKeySet();
            }

            is.close();
         }
      }

      return keySet;
   }
}
