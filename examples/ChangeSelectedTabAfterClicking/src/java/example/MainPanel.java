// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Objects;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.plaf.LayerUI;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new GridLayout(0, 1));
    JTabbedPane tabs0 = new JTabbedPane();
    initTabbedPane(tabs0);
    add(tabs0);

    JTabbedPane tabs1 = new TabLabelTabbedPane();
    initTabbedPane(tabs1);
    add(tabs1);

    JTabbedPane tabs2 = new JTabbedPane();
    initTabbedPane(tabs2);
    add(new JLayer<>(tabs2, new TabClickLayerUI()));

    // JTabbedPane tabs3 = new JTabbedPane() {
    //   @Override public void updateUI() {
    //     super.updateUI();
    //     setUI(new WindowsTabbedPaneUI() {
    //       @Override protected MouseListener createMouseListener() {
    //         return new TabClickListener();
    //       }
    //     });
    //   }
    // };
    // initTabbedPane(tabs3);
    // add(tabs3);
    setPreferredSize(new Dimension(320, 240));
  }

  private static void initTabbedPane(JTabbedPane tabs) {
    tabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
    tabs.addTab("Tab: 1", new JScrollPane(new JTextArea("1")));
    tabs.addTab("Tab: 2", new JScrollPane(new JTextArea("2")));
    tabs.addTab("Tab: 3", new JScrollPane(new JTextArea("3")));
    tabs.setComponentPopupMenu(makeTabPopupMenu());
  }

  private static JPopupMenu makeTabPopupMenu() {
    JPopupMenu popup = new JPopupMenu();
    popup.add("New tab").addActionListener(e -> {
      JTabbedPane tabs = (JTabbedPane) popup.getInvoker();
      String title = "Title: " + tabs.getTabCount();
      tabs.addTab(title, new JScrollPane(new JTextArea()));
      tabs.setSelectedIndex(tabs.getTabCount() - 1);
    });
    popup.addSeparator();
    JMenuItem close = popup.add("Close");
    close.addActionListener(e -> {
      JTabbedPane tabs = (JTabbedPane) popup.getInvoker();
      tabs.remove(tabs.getSelectedIndex());
    });
    JMenuItem closeAll = popup.add("Close all");
    closeAll.addActionListener(e -> {
      JTabbedPane tabs = (JTabbedPane) popup.getInvoker();
      tabs.removeAll();
    });
    JMenuItem closeAllButActive = popup.add("Close all bat active");
    closeAllButActive.addActionListener(e -> closeAllButActiveTab(popup));
    return popup;
  }

  private static void closeAllButActiveTab(JPopupMenu popup) {
    JTabbedPane tabs = (JTabbedPane) popup.getInvoker();
    int idx = tabs.getSelectedIndex();
    String title = tabs.getTitleAt(idx);
    Component cmp = tabs.getComponentAt(idx);
    tabs.removeAll();
    tabs.addTab(title, cmp);
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

// class TabClickListener extends MouseAdapter {
//   @Override public void mousePressed(MouseEvent e) {
//     e.consume();
//   }
//
//   @Override public void mouseClicked(MouseEvent e) {
//     JTabbedPane tabs = (JTabbedPane) e.getComponent();
//     updateSelectedTab(tabs, e.getPoint());
//   }
//
//   private static void updateSelectedTab(JTabbedPane tabs, Point pt) {
//     int index = tabs.indexAtLocation(pt.x, pt.y);
//     if (index >= 0 && tabs.isEnabledAt(index)) {
//       tabs.setSelectedIndex(index);
//       if (tabs.isRequestFocusEnabled()) {
//        // tabs.requestFocus(FocusEvent.Cause.MOUSE_EVENT);
//        tabs.requestFocus();
//       }
//     }
//   }
// }

class TabLabelTabbedPane extends JTabbedPane {
  @Override public void addTab(String title, Component content) {
    JButton button = new JButton(title);
    button.setContentAreaFilled(false);
    button.setFocusPainted(false);
    button.setOpaque(false);
    button.setInheritsPopupMenu(true);
    // button.setComponentPopupMenu(getComponentPopupMenu());
    button.setBorder(BorderFactory.createEmptyBorder());
    button.addActionListener(e -> updateSelectedTab(button));
    button.addMouseMotionListener(new MouseAdapter() {
      @Override public void mouseMoved(MouseEvent e) {
        updateRolloverTab(e);
      }
    });
    super.addTab(title, content);
    setTabComponentAt(getTabCount() - 1, button);
  }

  private void updateSelectedTab(Component button) {
    int index = indexOfTabComponent(button);
    if (index >= 0 && isEnabledAt(index)) {
      setSelectedIndex(index);
      if (isRequestFocusEnabled()) {
        // requestFocus(FocusEvent.Cause.MOUSE_EVENT);
        requestFocus();
      }
    }
  }

  private void updateRolloverTab(MouseEvent e) {
    Component c = e.getComponent();
    dispatchEvent(SwingUtilities.convertMouseEvent(c, e, this));
  }
}

class TabClickLayerUI extends LayerUI<JTabbedPane> {
  @Override public void installUI(JComponent c) {
    super.installUI(c);
    if (c instanceof JLayer) {
      ((JLayer<?>) c).setLayerEventMask(AWTEvent.MOUSE_EVENT_MASK);
    }
  }

  @Override public void uninstallUI(JComponent c) {
    if (c instanceof JLayer) {
      ((JLayer<?>) c).setLayerEventMask(0);
    }
    super.uninstallUI(c);
  }

  @Override protected void processMouseEvent(MouseEvent e, JLayer<? extends JTabbedPane> l) {
    JTabbedPane tabs = l.getView();
    if (Objects.equals(tabs, e.getComponent())) {
      if (e.getID() == MouseEvent.MOUSE_CLICKED) {
        updateSelectedTab(tabs, e);
      } else if (e.getID() == MouseEvent.MOUSE_PRESSED) {
        e.consume();
      }
    }
  }

  private static void updateSelectedTab(JTabbedPane tabs, MouseEvent e) {
    int index = SwingUtilities.isLeftMouseButton(e)
        ? tabs.indexAtLocation(e.getX(), e.getY())
        : -1;
    if (index >= 0 && tabs.isEnabledAt(index)) {
      tabs.setSelectedIndex(index);
      if (tabs.isRequestFocusEnabled()) {
        // tabs.requestFocus(FocusEvent.Cause.MOUSE_EVENT);
        tabs.requestFocus();
      }
    }
  }
}
