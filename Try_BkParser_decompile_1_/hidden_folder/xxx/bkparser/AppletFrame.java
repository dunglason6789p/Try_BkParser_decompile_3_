import java.applet.Applet;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

class AppletFrame extends Frame {
   AppletFrame(String title, Applet applet, int width, int height) {
      super(title);
      this.addWindowListener(new WindowAdapter() {
         public void windowClosing(WindowEvent e) {
            System.exit(0);
         }
      });
      applet.init();
      applet.setSize(width, height);
      applet.start();
      this.add(applet);
      this.pack();
      this.setVisible(true);
   }
}
