package org.apache.log4j.spi;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.helpers.LogLog;

/** @deprecated */
public final class RootCategory extends Logger {
   public RootCategory(Level level) {
      super("root");
      this.setLevel(level);
   }

   public final Level getChainedLevel() {
      return super.level;
   }

   public final void setLevel(Level level) {
      if (level == null) {
         LogLog.error("You have tried to set a null level to root.", new Throwable());
      } else {
         super.level = level;
      }

   }

   public final void setPriority(Level level) {
      this.setLevel(level);
   }
}
