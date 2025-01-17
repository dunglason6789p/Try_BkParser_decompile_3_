package org.apache.log4j;

import org.apache.log4j.helpers.FileWatchdog;

class PropertyWatchdog extends FileWatchdog {
   PropertyWatchdog(String filename) {
      super(filename);
   }

   public void doOnChange() {
      (new PropertyConfigurator()).doConfigure(super.filename, LogManager.getLoggerRepository());
   }
}
