// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.logging.Logger;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTabbedPane tabbedPane = new HoverCloseButtonTabbedPane();
    tabbedPane.setComponentPopupMenu(new TabbedPanePopupMenu());
    tabbedPane.addTab("JTree", new JScrollPane(new JTree()));
    tabbedPane.addTab("JLabel", new JScrollPane(new JLabel("JLabel")));
    tabbedPane.addTab("JSplitPane", new JSplitPane());
    add(tabbedPane);
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

class HoverCloseButtonTabbedPane extends JTabbedPane {
  // private final Insets tabInsets = UIManager.getInsets("TabbedPane.tabInsets");
  private transient MouseMotionListener hoverHandler;

  protected HoverCloseButtonTabbedPane() {
    super(TOP, SCROLL_TAB_LAYOUT);
  }

  // protected HoverCloseButtonTabbedPane(int tabPlacement) {
  //   super(tabPlacement, SCROLL_TAB_LAYOUT);
  // }

  // protected HoverCloseButtonTabbedPane(int tabPlacement, int tabLayoutPolicy) {
  //   super(tabPlacement, SCROLL_TAB_LAYOUT);
  // }

  @Override public void updateUI() {
    removeMouseMotionListener(hoverHandler);
    super.updateUI();
    // setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
    hoverHandler = new MouseAdapter() {
      private int prev = -1;
      @Override public void mouseMoved(MouseEvent e) {
        JTabbedPane source = (JTabbedPane) e.getComponent();
        int focused = source.indexAtLocation(e.getX(), e.getY());
        if (focused == prev) {
          return;
        }
        for (int i = 0; i < source.getTabCount(); i++) {
          Component c = source.getTabComponentAt(i);
          if (c instanceof TabPanel) {
            ((TabPanel) c).setButtonVisible(i == focused);
          }
        }
        prev = focused;
      }
    };
    addMouseMotionListener(hoverHandler);
  }

  @Override public void addTab(String title, Component content) {
    super.addTab(title, content);
    setTabComponentAt(getTabCount() - 1, new TabPanel(this, title, content));
  }
}

class TabPanel extends JPanel {
  protected static final int TAB_WIDTH = 80;
  private final JButton button = new JButton(new CloseTabIcon()) {
    @Override public void updateUI() {
      super.updateUI();
      setBorder(BorderFactory.createEmptyBorder());
      setBorderPainted(false);
      setFocusPainted(false);
      setContentAreaFilled(false);
      setFocusable(false);
      setVisible(false);
    }
  };

  protected TabPanel(JTabbedPane tabs, String title, Component content) {
    super(new BorderLayout());
    JLabel label = new JLabel() {
      @Override public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        int buttonWidth = button.isVisible() ? button.getPreferredSize().width : 0;
        return new Dimension(TAB_WIDTH - buttonWidth, d.height);
      }
    };
    label.setText(title);
    label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 1));

    button.addActionListener(e -> {
      int idx = tabs.indexOfComponent(content);
      tabs.removeTabAt(idx);
      if (tabs.getTabCount() > idx) {
        Component c = tabs.getTabComponentAt(idx);
        if (c instanceof TabPanel) {
          ((TabPanel) c).setButtonVisible(true);
        }
      }
    });
    add(label);
    add(button, BorderLayout.EAST);
  }

  @Override public final Component add(Component comp) {
    return super.add(comp);
  }

  @Override public final void add(Component comp, Object constraints) {
    super.add(comp, constraints);
  }

  @Override public void updateUI() {
    super.updateUI();
    setOpaque(false);
  }

  public void setButtonVisible(boolean flag) {
    button.setVisible(flag);
  }
}

class CloseTabIcon implements Icon {
  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.translate(x, y);
    g2.setPaint(Color.ORANGE);
    g2.drawLine(2, 3, 9, 10);
    g2.drawLine(2, 4, 8, 10);
    g2.drawLine(3, 3, 9, 9);
    g2.drawLine(9, 3, 2, 10);
    g2.drawLine(9, 4, 3, 10);
    g2.drawLine(8, 3, 2, 9);
    g2.dispose();
  }

  @Override public int getIconWidth() {
    return 12;
  }

  @Override public int getIconHeight() {
    return 12;
  }
}

final class TabbedPanePopupMenu extends JPopupMenu {
  private transient int count;
  private final JMenuItem closeAll;

  /* default */ TabbedPanePopupMenu() {
    super();
    add("Add").addActionListener(e -> {
      JTabbedPane tabbedPane = (JTabbedPane) getInvoker();
      tabbedPane.addTab("Title" + count, new JLabel("Tab" + count));
      tabbedPane.setSelectedIndex(tabbedPane.getTabCount() - 1);
      count++;
    });
    addSeparator();
    closeAll = add("Close All");
    closeAll.addActionListener(e -> ((JTabbedPane) getInvoker()).removeAll());
  }

  @Override public void show(Component c, int x, int y) {
    if (c instanceof JTabbedPane) {
      closeAll.setEnabled(((JTabbedPane) c).getTabCount() > 0);
      super.show(c, x, y);
    }
  }
}
