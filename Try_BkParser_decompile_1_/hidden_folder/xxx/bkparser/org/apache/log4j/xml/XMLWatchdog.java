package org.apache.log4j.xml;

import org.apache.log4j.LogManager;
import org.apache.log4j.helpers.FileWatchdog;

class XMLWatchdog extends FileWatchdog {
   XMLWatchdog(String filename) {
      super(filename);
   }

   public void doOnChange() {
      (new DOMConfigurator()).doConfigure(super.filename, LogManager.getLoggerRepository());
   }
}
