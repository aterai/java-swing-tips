// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import javax.swing.*;
import javax.swing.plaf.basic.BasicSplitPaneUI;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JScrollPane s1 = new JScrollPane(new JTable(6, 3)) {
      @Override public Dimension getMinimumSize() {
        return new Dimension(0, 100);
      }
    };
    JScrollPane s2 = new JScrollPane(new JTree()) {
      @Override public Dimension getMinimumSize() {
        return new Dimension(0, 100);
      }
    };

    JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    splitPane.setTopComponent(s1);
    splitPane.setBottomComponent(s2);
    splitPane.setOneTouchExpandable(true);
    // splitPane.setDividerLocation(0);

    // BasicService bs;
    // try {
    //   bs = (BasicService) ServiceManager.lookup("javax.jnlp.BasicService");
    // } catch (UnavailableServiceException ex) {
    //   bs = null;
    // }
    // if (bs == null) {
    AccessController.doPrivileged(new PrivilegedAction<Void>() {
      @SuppressWarnings("AvoidAccessibilityAlteration")
      @Override public Void run() {
        try {
          splitPane.setDividerLocation(0);
          Method m = BasicSplitPaneUI.class.getDeclaredMethod("setKeepHidden", Boolean.TYPE);
          m.setAccessible(true);
          m.invoke(splitPane.getUI(), Boolean.TRUE);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
          throw new UnsupportedOperationException(ex);
        }
        return null;
      }
    });
    // } else {
    //   // EventQueue.invokeLater(new Runnable() {
    //   //   @Override public void run() {
    //   //     splitPane.setDividerLocation(1d);
    //   //     splitPane.setResizeWeight(1d);
    //   //   }
    //   // });
    //   EventQueue.invokeLater(new Runnable() {
    //     @Override public void run() {
    //       Container divider = ((BasicSplitPaneUI) splitPane.getUI()).getDivider();
    //       for (Component c : divider.getComponents()) {
    //         if (c instanceof JButton) {
    //           ((JButton) c).doClick();
    //           break;
    //         }
    //       }
    //     }
    //   });
    // }
    add(splitPane);
    setPreferredSize(new Dimension(320, 240));
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (UnsupportedLookAndFeelException ignored) {
      Toolkit.getDefaultToolkit().beep();
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
      ex.printStackTrace();
      return;
    }
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}
