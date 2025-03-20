// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Optional;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTabbedPane tabbedPane = new JTabbedPane();
    tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
    Action addAction = new AbstractAction() {
      @Override public void actionPerformed(ActionEvent e) {
        JTabbedPane tabs = (JTabbedPane) e.getSource();
        int cnt = tabs.getTabCount();
        tabs.addTab("Untitled-" + cnt, new JScrollPane(new JTextArea()));
        tabs.setSelectedIndex(cnt);
      }
    };
    InputMap im = tabbedPane.getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    ActionMap am = tabbedPane.getActionMap();
    String addKey = "AddTab";
    addAction.putValue(Action.ACTION_COMMAND_KEY, addKey);
    im.put(KeyStroke.getKeyStroke("ctrl N"), addKey);
    am.put(addKey, addAction);

    String removeKey = "RemoveTab";
    Action removeAction = new AbstractAction() {
      @Override public void actionPerformed(ActionEvent e) {
        JTabbedPane tabs = (JTabbedPane) e.getSource();
        tabs.remove(tabs.getSelectedIndex());
      }
    };
    removeAction.putValue(Action.ACTION_COMMAND_KEY, removeKey);
    im.put(KeyStroke.getKeyStroke("ctrl W"), removeKey);
    am.put(removeKey, removeAction);
    tabbedPane.setComponentPopupMenu(new TabbedPanePopupMenu());
    String help = "Double-click in tab area to quickly create a new tab.";
    tabbedPane.addTab("Title", new JScrollPane(new JTextArea(help)));
    tabbedPane.addMouseListener(new MouseAdapter() {
      @Override public void mouseClicked(MouseEvent e) {
        boolean leftButton = SwingUtilities.isLeftMouseButton(e);
        boolean doubleClick = e.getClickCount() >= 2;
        JTabbedPane tabs = (JTabbedPane) e.getComponent();
        int idx = tabs.indexAtLocation(e.getX(), e.getY());
        Rectangle r = getTabAreaBounds(tabs);
        if (leftButton && doubleClick && idx < 0 && r.contains(e.getPoint())) {
          Optional.ofNullable(tabs.getActionMap().get(addKey)).ifPresent(a -> {
            ActionEvent ae = new ActionEvent(tabs, ActionEvent.ACTION_PERFORMED, addKey);
            a.actionPerformed(ae);
          });
        }
      }
    });
    add(tabbedPane);
    setPreferredSize(new Dimension(320, 240));
  }

  private static Rectangle getTabAreaBounds(JTabbedPane tabbedPane) {
    Rectangle r = SwingUtilities.calculateInnerArea(tabbedPane, null);
    Rectangle cr = Optional.ofNullable(tabbedPane.getSelectedComponent())
        .map(Component::getBounds)
        .orElseGet(Rectangle::new);
    int tp = tabbedPane.getTabPlacement();
    Insets i1 = UIManager.getInsets("TabbedPane.tabAreaInsets");
    Insets i2 = UIManager.getInsets("TabbedPane.contentBorderInsets");
    if (tp == SwingConstants.TOP || tp == SwingConstants.BOTTOM) {
      r.height -= cr.height + i1.top + i1.bottom + i2.top + i2.bottom;
      r.y += tp == SwingConstants.TOP ? i1.top : cr.y + cr.height + i1.bottom + i2.bottom;
    } else {
      r.width -= cr.width + i1.top + i1.bottom + i2.left + i2.right;
      r.x += tp == SwingConstants.LEFT ? i1.top : cr.x + cr.width + i1.bottom + i2.right;
    }
    return r;
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

final class TabbedPanePopupMenu extends JPopupMenu {
  private final JMenuItem removeTab;
  private final JMenuItem closeAll;
  private final JMenuItem closeAllButActive;

  /* default */ TabbedPanePopupMenu() {
    super();
    JMenuItem addTab = add("New tab");
    addTab.setActionCommand("AddTab");
    addTab.addActionListener(e -> {
      JTabbedPane tabs = (JTabbedPane) getInvoker();
      String key = e.getActionCommand();
      Optional.ofNullable(tabs.getActionMap().get(key)).ifPresent(a -> {
        ActionEvent ae = new ActionEvent(tabs, ActionEvent.ACTION_PERFORMED, key);
        a.actionPerformed(ae);
      });
    });
    addSeparator();
    removeTab = add("Close");
    removeTab.setActionCommand("RemoveTab");
    removeTab.addActionListener(e -> {
      JTabbedPane tabs = (JTabbedPane) getInvoker();
      String key = e.getActionCommand();
      Optional.ofNullable(tabs.getActionMap().get(key)).ifPresent(a -> {
        ActionEvent ae = new ActionEvent(tabs, ActionEvent.ACTION_PERFORMED, key);
        a.actionPerformed(ae);
      });
    });
    addSeparator();
    closeAll = add("Close all");
    closeAll.addActionListener(e -> {
      JTabbedPane tabs = (JTabbedPane) getInvoker();
      tabs.removeAll();
    });
    closeAllButActive = add("Close all bat active");
    closeAllButActive.addActionListener(e -> {
      JTabbedPane tabs = (JTabbedPane) getInvoker();
      int tabIdx = tabs.getSelectedIndex();
      String title = tabs.getTitleAt(tabIdx);
      Component cmp = tabs.getComponentAt(tabIdx);
      tabs.removeAll();
      tabs.addTab(title, cmp);
    });
  }

  @Override public void show(Component c, int x, int y) {
    if (c instanceof JTabbedPane) {
      JTabbedPane tabs = (JTabbedPane) c;
      removeTab.setEnabled(tabs.indexAtLocation(x, y) >= 0);
      closeAll.setEnabled(tabs.getTabCount() > 0);
      closeAllButActive.setEnabled(tabs.getTabCount() > 0);
      super.show(c, x, y);
    }
  }
}
