// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Objects;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    setFocusable(true);
    addMouseListener(new MouseAdapter() {
      @Override public void mouseClicked(MouseEvent e) {
        boolean isDoubleClick = e.getClickCount() >= 2;
        if (isDoubleClick) {
          toggleFullScreenWindow();
        }
      }
    });
    String key = "full-screen";
    getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0), key);
    getActionMap().put(key, new AbstractAction() {
      @Override public void actionPerformed(ActionEvent e) {
        toggleFullScreenWindow();
      }
    });
    getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "close");
    getActionMap().put("close", new AbstractAction() {
      @Override public void actionPerformed(ActionEvent e) {
        System.out.println("ESC KeyEvent:");
        // int mode = 2;
        // if (mode == 0) {
        //   // dialog.dispose();
        //   // triggered windowClosed
        // } else if (mode == 1) {
        //   // // When DISPOSE_ON_CLOSE met WebStart > www.pushing-pixels.org/?p=232
        //   // // Web Start thread is a non-daemon thread so the JVM cannot exit.
        //   // // JVM shutdown
        //   // System.exit(0);
        // } else {

        // // click on the X
        // Component c = SwingUtilities.getRoot(getRootPane());
        Container c = getTopLevelAncestor();
        if (c instanceof Window) {
          Window d = (Window) c;
          d.dispatchEvent(new WindowEvent(d, WindowEvent.WINDOW_CLOSING));
        }
        // triggered windowClosing
      }
    });
    String help1 = "F11 or Double Click: toggle full-screen";
    String help2 = "ESC: exit";
    add(new JLabel(String.format("<html>%s<br/>%s", help1, help2)), BorderLayout.NORTH);
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    setPreferredSize(new Dimension(320, 240));
  }

  public void toggleFullScreenWindow() {
    // Component c = SwingUtilities.getRoot(getRootPane());
    Container c = getTopLevelAncestor();
    if (c instanceof Dialog) {
      Dialog dialog = (Dialog) c;
      GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
      GraphicsDevice gd = ge.getDefaultScreenDevice();
      if (Objects.isNull(gd.getFullScreenWindow())) {
        dialog.dispose(); // destroy the native resources
        dialog.setUndecorated(true);
        dialog.setVisible(true); // rebuilding the native resources
        gd.setFullScreenWindow(dialog);
      } else {
        gd.setFullScreenWindow(null);
        dialog.dispose();
        dialog.setUndecorated(false);
        dialog.setVisible(true);
        dialog.repaint();
      }
    }
    requestFocusInWindow(); // for Ubuntu
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
    JDialog dialog = new JDialog();
    dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    dialog.addWindowListener(new WindowAdapter() {
      // @SuppressWarnings("PMD.DoNotCallSystemExit")
      // @Override public void windowClosing(WindowEvent e) {
      //   System.out.println("windowClosing:");
      //   System.out.println("  triggered only when you click on the X");
      //   System.out.println("  or on the close menu item in the window's system menu.'");
      //   System.out.println("System.exit(0);");
      //   System.exit(0); // WebStart
      // }

      @Override public void windowClosing(WindowEvent e) {
        System.out.println("windowClosing:");
        System.out.println("  triggered only when you click on the X");
        System.out.println("  or on the close menu item in the window's system menu.'");
      }

      @Override public void windowClosed(WindowEvent e) {
        System.out.println("windowClosed & rebuild:");
      }
    });
    dialog.getContentPane().add(new MainPanel());
    dialog.pack();
    dialog.setLocationRelativeTo(null);
    dialog.setVisible(true);
  }
}
