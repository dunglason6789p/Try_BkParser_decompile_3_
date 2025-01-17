package org.apache.log4j.chainsaw;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.apache.log4j.Logger;

class ExitAction extends AbstractAction {
   private static final Logger LOG;
   public static final ExitAction INSTANCE;

   private ExitAction() {
   }

   public void actionPerformed(ActionEvent aIgnore) {
      LOG.info("shutting down");
      System.exit(0);
   }

   static {
      LOG = Logger.getLogger(class$org$apache$log4j$chainsaw$ExitAction == null ? (class$org$apache$log4j$chainsaw$ExitAction = class$("org.apache.log4j.chainsaw.ExitAction")) : class$org$apache$log4j$chainsaw$ExitAction);
      INSTANCE = new ExitAction();
   }
}
