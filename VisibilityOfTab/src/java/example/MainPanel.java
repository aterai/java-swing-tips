// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Arrays;
import java.util.List;
import javax.swing.*;

public final class MainPanel extends JPanel {
  public static final int MIN_TAB_WIDTH = 100;

  private MainPanel() {
    super(new BorderLayout());
    JPanel p = new JPanel(new GridLayout(0, 1, 0, 2));

    JTabbedPane tabbedPane1 = new JTabbedPane() {
      @Override public void removeTabAt(int index) {
        if (getTabCount() > 0) {
          setSelectedIndex(0);
          super.removeTabAt(index);
          setSelectedIndex(index - 1);
        } else {
          super.removeTabAt(index);
        }
      }
    };

    JTabbedPane tabbedPane2 = new JTabbedPane() {
      private Component getScrollableViewport() {
        Component cmp = null;
        for (Component c : getComponents()) {
          if ("TabbedPane.scrollableViewport".equals(c.getName())) {
            cmp = c;
            break;
          }
        }
        return cmp;
      }

      private void resetViewportPosition(int idx) {
        if (getTabCount() <= 0) {
          return;
        }
        Component o = getScrollableViewport();
        if (o instanceof JViewport) {
          JViewport viewport = (JViewport) o;
          JComponent c = (JComponent) viewport.getView();
          c.scrollRectToVisible(getBoundsAt(idx));
        }
      }

      @Override public void removeTabAt(int index) {
        if (getTabCount() > 0) {
          resetViewportPosition(0);
          super.removeTabAt(index);
          resetViewportPosition(index - 1);
        } else {
          super.removeTabAt(index);
        }
      }
    };

    List<JTabbedPane> list = Arrays.asList(new JTabbedPane(), tabbedPane1, tabbedPane2);
    list.forEach(tabs -> {
      tabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
      tabs.addTab("00000000", new JLabel("0"));
      tabs.addTab("11111111", new JLabel("1"));
      tabs.addTab("22222222", new JLabel("2"));
      tabs.addTab("33333333", new JLabel("3"));
      tabs.addTab("44444444", new JLabel("4"));
      tabs.addTab("55555555", new JLabel("5"));
      tabs.addTab("66666666", new JLabel("6"));
      tabs.addTab("77777777", new JLabel("7"));
      tabs.addTab("88888888", new JLabel("8"));
      tabs.addTab("99999999", new JLabel("9"));
      tabs.setSelectedIndex(tabs.getTabCount() - 1);
      // // TEST:
      // EventQueue.invokeLater(() -> {
      //   tabs.setSelectedIndex(tabs.getTabCount() - 1);
      // });
      p.add(tabs);
    });

    JButton button = new JButton("Remove");
    button.addActionListener(e -> {
      list.forEach(tabs -> {
        if (tabs.getTabCount() > 0) {
          tabs.removeTabAt(tabs.getTabCount() - 1);
        }
      });
    });

    add(p);
    add(button, BorderLayout.SOUTH);

    setPreferredSize(new Dimension(320, 240));
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
