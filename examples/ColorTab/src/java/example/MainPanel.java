// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTabbedPane tabs = new JTabbedPane();
    tabs.setComponentPopupMenu(new TabbedPanePopupMenu());
    tabs.addChangeListener(new TabChangeListener());
    tabs.addTab("Title", new JLabel("Tab"));
    add(tabs);
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

class TabChangeListener implements ChangeListener {
  @Override public void stateChanged(ChangeEvent e) {
    JTabbedPane tabs = (JTabbedPane) e.getSource();
    if (tabs.getTabCount() > 0) {
      int selectedIdx = tabs.getSelectedIndex();
      for (int i = 0; i < tabs.getTabCount(); i++) {
        if (i == selectedIdx && tabs.getTitleAt(selectedIdx).endsWith("1")) {
          tabs.setForegroundAt(i, Color.GREEN);
        } else if (i == selectedIdx) {
          Color sc = selectedIdx % 2 == 0 ? Color.RED : Color.BLUE;
          tabs.setForegroundAt(i, sc);
        } else {
          tabs.setForegroundAt(i, Color.BLACK);
        }
      }
    }
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
      JTabbedPane tabs = (JTabbedPane) getInvoker();
      tabs.addTab("Title: " + count, new JLabel("Tab: " + count));
      tabs.setSelectedIndex(tabs.getTabCount() - 1);
      count++;
    });
    addSeparator();
    closePage = add("Close");
    closePage.addActionListener(e -> {
      JTabbedPane tabs = (JTabbedPane) getInvoker();
      tabs.remove(tabs.getSelectedIndex());
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
      // JDK 1.3: tabIndex = tabs.getUI().tabForCoordinate(tabs, x, y);
      closePage.setEnabled(tabs.indexAtLocation(x, y) >= 0);
      closeAll.setEnabled(tabs.getTabCount() > 0);
      closeAllButActive.setEnabled(tabs.getTabCount() > 0);
      super.show(c, x, y);
    }
  }
}
