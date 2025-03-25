// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.logging.Logger;
import java.util.stream.Stream;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    EventQueue.invokeLater(() -> getRootPane().setJMenuBar(createMenuBar()));
    add(new JScrollPane(new JTextArea()));
    setPreferredSize(new Dimension(320, 240));
  }

  private static JMenuBar createMenuBar() {
    JMenuBar menuBar = new JMenuBar();
    menuBar.setLayout(new MenuBarLayout(2, 2));
    Stream.of(
        "File", "Edit", "View", "Navigate", "Code", "Analyze",
        "Refactor", "Build", "Run", "Help"
    ).map(MainPanel::createMenu).forEach(menuBar::add);
    return menuBar;
  }

  private static JMenu createMenu(String key) {
    JMenu menu = new JMenu(key);
    menu.add("JMenuItem1");
    menu.add("JMenuItem2");
    menu.add("JMenuItem3");
    return menu;
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

class MenuBarLayout extends FlowLayout {
  protected MenuBarLayout(int hgp, int vgp) {
    super(LEADING, hgp, vgp);
  }

  @SuppressWarnings("PMD.AvoidSynchronizedStatement")
  @Override public Dimension preferredLayoutSize(Container target) {
    synchronized (target.getTreeLock()) {
      int targetWidth = target.getSize().width;
      targetWidth = targetWidth == 0 ? Integer.MAX_VALUE : targetWidth;
      Insets insets = target.getInsets();
      int hgp = getHgap();
      int vgp = getVgap();
      int maxWidth = targetWidth - insets.left - insets.right;
      int height = vgp;
      int rowWidth = hgp;
      int rowHeight = 0;
      for (Component m : target.getComponents()) {
        if (m.isVisible()) {
          Dimension d = m.getPreferredSize();
          if (rowWidth + d.width > maxWidth) {
            height += rowHeight;
            rowWidth = hgp;
            rowHeight = 0;
          }
          rowWidth += d.width + hgp;
          rowHeight = Math.max(rowHeight, d.height + vgp);
        }
      }
      height += rowHeight + insets.top + insets.bottom;
      return new Dimension(targetWidth, height);
    }
  }

  // // https://tips4java.wordpress.com/2008/11/06/wrap-layout/
  // // WrapLayout.java Rob Camick on November 6, 2008,
  // private Dimension preferredLayoutSize;
  // @Override public void layoutContainer(Container target) {
  //   Dimension size = preferredLayoutSize(target);
  //   if (size.equals(preferredLayoutSize)) {
  //     super.layoutContainer(target);
  //   } else {
  //     preferredLayoutSize = size;
  //     Container top = target;
  //     while (!(top instanceof Window) && top.getParent() != null) {
  //       top = top.getParent();
  //     }
  //     top.validate();
  //   }
  // }
}
