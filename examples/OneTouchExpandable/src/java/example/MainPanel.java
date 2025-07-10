// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.plaf.basic.BasicSplitPaneUI;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    split.setTopComponent(new MinimumSizeScrollPane(new JTable(6, 3)));
    split.setBottomComponent(new MinimumSizeScrollPane(new JTree()));
    split.setOneTouchExpandable(true);
    // split.setDividerLocation(0);

    // BasicService bs;
    // try {
    //   bs = (BasicService) ServiceManager.lookup("javax.jnlp.BasicService");
    // } catch (UnavailableServiceException ex) {
    //   bs = null;
    // }
    // if (bs == null) {
    AccessController.doPrivileged((PrivilegedAction<Void>) () -> keepHidden(split));
    // } else {
    //   // EventQueue.invokeLater(new Runnable() {
    //   //   @Override public void run() {
    //   //     split.setDividerLocation(1d);
    //   //     split.setResizeWeight(1d);
    //   //   }
    //   // });
    //   EventQueue.invokeLater(new Runnable() {
    //     @Override public void run() {
    //       Container divider = ((BasicSplitPaneUI) split.getUI()).getDivider();
    //       for (Component c : divider.getComponents()) {
    //         if (c instanceof JButton) {
    //           ((JButton) c).doClick();
    //           break;
    //         }
    //       }
    //     }
    //   });
    // }
    add(split);
    setPreferredSize(new Dimension(320, 240));
  }

  @SuppressWarnings("PMD.AvoidAccessibilityAlteration")
  private static Void keepHidden(JSplitPane split) {
    try {
      split.setDividerLocation(0);
      Method m = BasicSplitPaneUI.class.getDeclaredMethod("setKeepHidden", Boolean.TYPE);
      m.setAccessible(true);
      m.invoke(split.getUI(), true);
    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
      throw new UnsupportedOperationException(ex);
    }
    return null;
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
      Logger.getGlobal().severe(ex::getMessage);
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

class MinimumSizeScrollPane extends JScrollPane {
  protected MinimumSizeScrollPane(Component view) {
    super(view);
  }

  @Override public Dimension getMinimumSize() {
    return new Dimension(0, 100);
  }
}
