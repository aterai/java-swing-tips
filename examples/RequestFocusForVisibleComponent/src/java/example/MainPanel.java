// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import com.sun.java.swing.plaf.windows.WindowsTabbedPaneUI;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import javax.swing.plaf.metal.MetalTabbedPaneUI;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new GridLayout(2, 1));
    JTabbedPane tabbedPane = new RequestFocusTabbedPane();
    // tabbedPane.addMouseListener(new MouseAdapter() {
    //   @Override public void mousePressed(MouseEvent e) {
    //     boolean b1 = SwingUtilities.isLeftMouseButton(e);
    //     boolean isDouble = e.getClickCount() >= 2;
    //     JTabbedPane tabs = (JTabbedPane) e.getComponent();
    //     int idx = tabs.indexAtLocation(e.getX(), e.getY());
    //     if (b1 && idx >= 0) {
    //       String cmd = isDouble ? "requestFocus" : "requestFocusForVisibleComponent";
    //       ActionEvent a = new ActionEvent(tabs, ActionEvent.ACTION_PERFORMED, cmd);
    //       EventQueue.invokeLater(() -> tabs.getActionMap().get(cmd).actionPerformed(a));
    //     }
    //   }
    // });
    add(makeTabbedPane(new JTabbedPane(), "Default"));
    add(makeTabbedPane(tabbedPane, "requestFocusForVisibleComponent"));
    setPreferredSize(new Dimension(320, 240));
  }

  private static JTabbedPane makeTabbedPane(JTabbedPane tabs, String title) {
    tabs.setComponentPopupMenu(new TabbedPanePopupMenu());
    tabs.addTab(title, new JTextArea(title));
    tabs.addTab("000", new JTextArea("000000000000000000000000"));
    tabs.addTab("111", new JTextArea("1111111111111"));
    tabs.addTab("222", new JTextArea("2222222222"));
    return tabs;
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

class RequestFocusTabbedPane extends JTabbedPane {
  @Override public void updateUI() {
    super.updateUI();
    if (getUI() instanceof WindowsTabbedPaneUI) {
      setUI(new WindowsTabbedPaneUI() {
        @Override protected MouseListener createMouseListener() {
          return new TabSelectionMouseListener(this) {
            @Override public void mouseEntered(MouseEvent e) {
              setRolloverTab(tabForCoordinate(tabPane, e.getX(), e.getY()));
            }

            @Override public void mouseExited(MouseEvent e) {
              setRolloverTab(-1);
            }
          };
        }
      });
    } else {
      setUI(new MetalTabbedPaneUI() {
        @Override protected MouseListener createMouseListener() {
          return new TabSelectionMouseListener(this) {
            @Override public void mouseEntered(MouseEvent e) {
              setRolloverTab(tabForCoordinate(tabPane, e.getX(), e.getY()));
            }

            @Override public void mouseExited(MouseEvent e) {
              setRolloverTab(-1);
            }
          };
        }
      });
    }
  }
}

class TabSelectionMouseListener extends MouseAdapter {
  private final BasicTabbedPaneUI ui;

  protected TabSelectionMouseListener(BasicTabbedPaneUI ui) {
    super();
    this.ui = ui;
  }

  @Override public void mousePressed(MouseEvent e) {
    JTabbedPane tabs = (JTabbedPane) e.getComponent();
    boolean b = !SwingUtilities.isRightMouseButton(e); // && !e.isPopupTrigger();
    int idx = ui.tabForCoordinate(tabs, e.getX(), e.getY());
    if (b && isEnabledAt(tabs, idx)) {
      if (idx != tabs.getSelectedIndex() && e.getClickCount() < 2) {
        tabs.setSelectedIndex(idx);
        String cmd = "requestFocusForVisibleComponent";
        ActionEvent a = new ActionEvent(tabs, ActionEvent.ACTION_PERFORMED, cmd);
        EventQueue.invokeLater(() -> tabs.getActionMap().get(cmd).actionPerformed(a));
      } else if (tabs.isRequestFocusEnabled()) {
        // tabs.requestFocus();
        tabs.requestFocusInWindow();
      }
    }
  }

  private static boolean isEnabledAt(JTabbedPane tabs, int index) {
    return tabs.isEnabled() && index >= 0 && tabs.isEnabledAt(index);
  }
}

final class TabbedPanePopupMenu extends JPopupMenu {
  private transient int count;
  private final JMenuItem closePage;
  private final JMenuItem closeAll;
  private final JMenuItem closeAllButActive;

  /* default */ TabbedPanePopupMenu() {
    super();
    add("New tab").addActionListener(e -> {
      JTabbedPane t = (JTabbedPane) getInvoker();
      t.addTab("Title: " + count, new JTextArea("Tab: " + count));
      t.setSelectedIndex(t.getTabCount() - 1);
      count++;
    });
    addSeparator();
    closePage = add("Close");
    closePage.addActionListener(e -> {
      JTabbedPane t = (JTabbedPane) getInvoker();
      t.remove(t.getSelectedIndex());
    });
    addSeparator();
    closeAll = add("Close all");
    closeAll.addActionListener(e -> {
      JTabbedPane t = (JTabbedPane) getInvoker();
      t.removeAll();
    });
    closeAllButActive = add("Close all bat active");
    closeAllButActive.addActionListener(e -> {
      JTabbedPane t = (JTabbedPane) getInvoker();
      int tabIdx = t.getSelectedIndex();
      String title = t.getTitleAt(tabIdx);
      Component cmp = t.getComponentAt(tabIdx);
      t.removeAll();
      t.addTab(title, cmp);
    });
  }

  @Override public void show(Component c, int x, int y) {
    if (c instanceof JTabbedPane) {
      JTabbedPane t = (JTabbedPane) c;
      closePage.setEnabled(t.indexAtLocation(x, y) >= 0);
      closeAll.setEnabled(t.getTabCount() > 0);
      closeAllButActive.setEnabled(t.getTabCount() > 0);
      super.show(c, x, y);
    }
  }
}
