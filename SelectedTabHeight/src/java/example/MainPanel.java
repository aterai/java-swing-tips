// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import com.sun.java.swing.plaf.windows.WindowsTabbedPaneUI;

import java.awt.*;
import java.awt.event.ItemEvent;
import javax.swing.*;
import javax.swing.plaf.basic.BasicTabbedPaneUI;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());

    JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT) {
      @Override public void updateUI() {
        super.updateUI();
        if (getUI() instanceof WindowsTabbedPaneUI) {
          setUI(new WindowsTabHeightTabbedPaneUI());
        } else {
          setUI(new BasicTabHeightTabbedPaneUI());
        }
      }
    };
    tabbedPane.addTab("00000", new JLabel("aaa aaa aaa aa"));
    tabbedPane.addTab("111112", new JLabel("bbb bbb bbb bbb bb bb"));
    tabbedPane.addTab("22222232", new JScrollPane(new JTree()));
    tabbedPane.addTab("3333333333", new JSplitPane());

    JComboBox<? extends Enum<?>> comboBox = new JComboBox<>(TabPlacements.values());
    comboBox.addItemListener(e -> {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        tabbedPane.setTabPlacement(((TabPlacements) e.getItem()).tabPlacement);
      }
    });
    Box box = Box.createHorizontalBox();
    box.add(Box.createHorizontalGlue());
    box.add(new JLabel("TabPlacement: "));
    box.add(Box.createHorizontalStrut(2));
    box.add(comboBox);
    box.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

    add(tabbedPane);
    add(box, BorderLayout.SOUTH);
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

enum TabPlacements {
  TOP(JTabbedPane.TOP),
  BOTTOM(JTabbedPane.BOTTOM),
  LEFT(JTabbedPane.LEFT),
  RIGHT(JTabbedPane.RIGHT);
  public final int tabPlacement;
  TabPlacements(int tabPlacement) {
    this.tabPlacement = tabPlacement;
  }
}

class WindowsTabHeightTabbedPaneUI extends WindowsTabbedPaneUI {
  private static final int TAB_AREA_HEIGHT = 32;

  @Override protected int calculateTabHeight(int tabPlacement, int tabIndex, int fontHeight) {
    return TAB_AREA_HEIGHT; // super.calculateTabHeight(tabPlacement, tabIndex, fontHeight) + 4;
  }

  // @Override public Rectangle getTabBounds(JTabbedPane pane, int i) {
  //   Rectangle tabRect = super.getTabBounds(pane, i);
  //   tabRect.translate(0, -16);
  //   tabRect.height = 16;
  //   return tabRect;
  // }

  @Override protected void paintTab(Graphics g, int tabPlacement, Rectangle[] rects, int tabIndex, Rectangle iconRect, Rectangle textRect) {
    boolean isTopOrBottom = tabPlacement == SwingConstants.TOP || tabPlacement == SwingConstants.BOTTOM;
    if (isTopOrBottom && tabPane.getSelectedIndex() != tabIndex) {
      int tabHeight = TAB_AREA_HEIGHT / 2 + 3;
      rects[tabIndex].height = tabHeight;
      if (tabPlacement == JTabbedPane.TOP) {
        rects[tabIndex].y = TAB_AREA_HEIGHT - tabHeight + 3;
      }
    }
    super.paintTab(g, tabPlacement, rects, tabIndex, iconRect, textRect);
  }
}

class BasicTabHeightTabbedPaneUI extends BasicTabbedPaneUI {
  private static final int TAB_AREA_HEIGHT = 32;

  @Override protected int calculateTabHeight(int tabPlacement, int tabIndex, int fontHeight) {
    return TAB_AREA_HEIGHT;
  }

  @Override protected void paintTab(Graphics g, int tabPlacement, Rectangle[] rects, int tabIndex, Rectangle iconRect, Rectangle textRect) {
    boolean isTopOrBottom = tabPlacement == SwingConstants.TOP || tabPlacement == SwingConstants.BOTTOM;
    if (isTopOrBottom && tabPane.getSelectedIndex() != tabIndex) {
      int tabHeight = TAB_AREA_HEIGHT / 2 + 3;
      rects[tabIndex].height = tabHeight;
      if (tabPlacement == JTabbedPane.TOP) {
        rects[tabIndex].y = TAB_AREA_HEIGHT - tabHeight + 3;
      }
    }
    super.paintTab(g, tabPlacement, rects, tabIndex, iconRect, textRect);
  }
}
