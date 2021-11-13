// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTextArea log = new JTextArea();
    add(new JScrollPane(log));
    setPreferredSize(new Dimension(320, 240));

    Toolkit tk = Toolkit.getDefaultToolkit();
    String fmt = "  Frame.MAXIMIZED_%s: %s%n";
    log.append("Toolkit#isFrameStateSupported(int)\n");
    log.append(String.format(fmt, "HORIZ", tk.isFrameStateSupported(Frame.MAXIMIZED_HORIZ)));
    log.append(String.format(fmt, "VERT", tk.isFrameStateSupported(Frame.MAXIMIZED_VERT)));
    log.append(String.format(fmt, "BOTH", tk.isFrameStateSupported(Frame.MAXIMIZED_BOTH)));

    EventQueue.invokeLater(() -> {
      Container c = getTopLevelAncestor();
      if (c instanceof Window) {
        Window window = (Window) c;
        window.addWindowStateListener(e -> {
          String ws;
          switch (e.getNewState()) {
            case Frame.NORMAL:
              ws = "NORMAL";
              break;
            case Frame.ICONIFIED:
              ws = "ICONIFIED";
              break;
            case Frame.MAXIMIZED_HORIZ:
              ws = "MAXIMIZED_HORIZ";
              break;
            case Frame.MAXIMIZED_VERT:
              ws = "MAXIMIZED_VERT";
              break;
            case Frame.MAXIMIZED_BOTH:
              ws = "MAXIMIZED_BOTH";
              break;
            default:
              ws = "ERROR";
              break;
          }
          log.append(String.format("WindowStateListener: %s%n", ws));
        });

        window.addWindowListener(new WindowListener() {
          @Override public void windowOpened(WindowEvent e) {
            log.append("windowOpened\n");
          }

          @Override public void windowClosing(WindowEvent e) {
            log.append("windowClosing\n");
          }

          @Override public void windowClosed(WindowEvent e) {
            log.append("windowClosed\n");
          }

          @Override public void windowIconified(WindowEvent e) {
            log.append("windowIconified\n");
          }

          @Override public void windowDeiconified(WindowEvent e) {
            log.append("windowDeiconified\n");
          }

          @Override public void windowActivated(WindowEvent e) {
            log.append("windowActivated\n");
          }

          @Override public void windowDeactivated(WindowEvent e) {
            log.append("windowDeactivated\n");
          }
        });
      }
    });
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
      ex.printStackTrace();
      Toolkit.getDefaultToolkit().beep();
    }
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}
