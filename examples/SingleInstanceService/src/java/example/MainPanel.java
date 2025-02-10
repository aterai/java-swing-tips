// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.jnlp.ServiceManager;
import javax.jnlp.SingleInstanceListener;
import javax.jnlp.SingleInstanceService;
import javax.jnlp.UnavailableServiceException;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    add(new JScrollPane(new JTree()));
    setPreferredSize(new Dimension(320, 240));
  }

  public static void main(String[] args) {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (UnsupportedLookAndFeelException ignored) {
      Toolkit.getDefaultToolkit().beep();
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
      ex.printStackTrace();
      return;
    }
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    JFrame frame = new JFrame("@title@");
    try {
      String serviceName = "javax.jnlp.SingleInstanceService";
      SingleInstanceService sis = (SingleInstanceService) ServiceManager.lookup(serviceName);
      sis.addSingleInstanceListener(new SingleInstanceListener() {
        private int count;
        @Override public void newActivation(String[] args) {
          // System.out.println(EventQueue.isDispatchThread());
          EventQueue.invokeLater(() -> {
            JOptionPane.showMessageDialog(frame, "already running: " + count);
            frame.setTitle("title:" + count);
            count++;
          });
        }
      });
    } catch (UnavailableServiceException ex) {
      ex.printStackTrace();
      return;
    }
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setResizable(false);
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}
