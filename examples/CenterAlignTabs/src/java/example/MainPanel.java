// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import java.util.stream.IntStream;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTabbedPane tabbedPane = makeTabbedPane();
    tabbedPane.setComponentPopupMenu(new TabbedPanePopupMenu());
    add(tabbedPane);
    setPreferredSize(new Dimension(320, 240));
  }

  private JTabbedPane makeTabbedPane() {
    JTabbedPane tabbedPane = new CenteredTabbedPane();
    tabbedPane.addTab("JTree", new JScrollPane(new JTree()));
    tabbedPane.addTab("JSplitPane", new JSplitPane());
    tabbedPane.addTab("JTable", new JScrollPane(new JTable(5, 3)));
    return tabbedPane;
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
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

class CenteredTabbedPane extends JTabbedPane {
  @Override public void doLayout() {
    int placement = getTabPlacement();
    if (placement == TOP || placement == BOTTOM) {
      EventQueue.invokeLater(this::updateTabAreaMargins);
    }
    super.doLayout();
  }

  private void updateTabAreaMargins() {
    int allWidth = IntStream.range(0, getTabCount())
        .map(i -> getBoundsAt(i).width).sum();
    Rectangle r = SwingUtilities.calculateInnerArea(this, null);
    int w2 = Math.max(0, (r.width - allWidth) / 2);
    Insets ins = new Insets(3, w2, 4, 0);
    UIDefaults d = new UIDefaults();
    d.put("TabbedPane:TabbedPaneTabArea.contentMargins", ins);
    putClientProperty("Nimbus.Overrides", d);
    putClientProperty("Nimbus.Overrides.InheritDefaults", Boolean.TRUE);
  }
}

final class TabbedPanePopupMenu extends JPopupMenu {
  private final JMenuItem closePage;
  private final JMenuItem closeAll;
  private final JMenuItem closeAllButActive;

  /* default */ TabbedPanePopupMenu() {
    super();
    AtomicInteger counter = new AtomicInteger();
    add("New tab").addActionListener(e -> {
      JTabbedPane tabbedPane = (JTabbedPane) getInvoker();
      int iv = counter.getAndIncrement();
      tabbedPane.addTab("Title: " + iv, new JLabel("Tab: " + iv));
      tabbedPane.setSelectedIndex(tabbedPane.getTabCount() - 1);
    });
    addSeparator();
    closePage = add("Close");
    closePage.addActionListener(e -> {
      JTabbedPane tabbedPane = (JTabbedPane) getInvoker();
      tabbedPane.remove(tabbedPane.getSelectedIndex());
    });
    addSeparator();
    closeAll = add("Close all");
    closeAll.addActionListener(e -> {
      JTabbedPane tabbedPane = (JTabbedPane) getInvoker();
      tabbedPane.removeAll();
    });
    closeAllButActive = add("Close all bat active");
    closeAllButActive.addActionListener(e -> {
      JTabbedPane tabbedPane = (JTabbedPane) getInvoker();
      int idx = tabbedPane.getSelectedIndex();
      String title = tabbedPane.getTitleAt(idx);
      Component cmp = tabbedPane.getComponentAt(idx);
      tabbedPane.removeAll();
      tabbedPane.addTab(title, cmp);
    });
  }

  @Override public void show(Component c, int x, int y) {
    if (c instanceof JTabbedPane) {
      JTabbedPane tabbedPane = (JTabbedPane) c;
      closePage.setEnabled(tabbedPane.indexAtLocation(x, y) >= 0);
      closeAll.setEnabled(tabbedPane.getTabCount() > 0);
      closeAllButActive.setEnabled(tabbedPane.getTabCount() > 0);
      super.show(c, x, y);
    }
  }
}
