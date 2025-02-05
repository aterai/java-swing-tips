// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.stream.Stream;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    // addComponentListener(new ComponentAdapter() {
    //   @Override public void componentResized(ComponentEvent e) {
    //     System.out.println("componentResized");
    //     ((JComponent) e.getSource()).revalidate();
    //   }
    // });
    // frame.addWindowStateListener(new WindowStateListener() {
    //   @Override public void windowStateChanged(WindowEvent e) {
    //     EventQueue.invokeLater(() -> {
    //       System.out.println("windowStateChanged");
    //       JFrame f = (JFrame) e.getWindow();
    //       f.getContentPane().revalidate();
    //     });
    //   }
    // });
    EventQueue.invokeLater(() -> getRootPane().setJMenuBar(createMenuBar()));
    add(new JScrollPane(new JTextArea()));
    setPreferredSize(new Dimension(320, 240));
  }

  private static JMenuBar createMenuBar() {
    JMenuBar menuBar = new JMenuBar();
    menuBar.setLayout(new FlowLayout(FlowLayout.LEFT, 2, 2) {
      @SuppressWarnings("PMD.AvoidSynchronizedStatement")
      @Override public Dimension preferredLayoutSize(Container target) {
        synchronized (target.getTreeLock()) {
          int targetWidth = target.getSize().width;
          targetWidth = targetWidth == 0 ? Integer.MAX_VALUE : targetWidth;
          Insets insets = target.getInsets();
          int hgap = getHgap();
          int vgap = getVgap();
          int maxWidth = targetWidth - insets.left - insets.right;
          int height = vgap;
          int rowWidth = hgap;
          int rowHeight = 0;
          for (Component m : target.getComponents()) {
            if (m.isVisible()) {
              Dimension d = m.getPreferredSize();
              if (rowWidth + d.width > maxWidth) {
                height += rowHeight;
                rowWidth = hgap;
                rowHeight = 0;
              }
              rowWidth += d.width + hgap;
              rowHeight = Math.max(rowHeight, d.height + vgap);
            }
          }
          height += rowHeight + insets.top + insets.bottom;
          return new Dimension(targetWidth, height);
        }
      }
      // // https://tips4java.wordpress.com/2008/11/06/wrap-layout/
      // // WrapLayout.java
      // // Rob Camick on November 6, 2008,
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
    });
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
