// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import com.sun.java.swing.plaf.windows.WindowsTabbedPaneUI;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.plaf.basic.BasicTabbedPaneUI;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTabbedPane tabs = new JTabbedPane() {
      @Override public void updateUI() {
        super.updateUI();
        if (getUI() instanceof WindowsTabbedPaneUI) {
          setUI(new WindowsTabHeightTabbedPaneUI());
        } else {
          setUI(new BasicTabHeightTabbedPaneUI());
        }
        setTabLayoutPolicy(SCROLL_TAB_LAYOUT);
      }
    };
    tabs.addTab("00000", new JLabel("aaa aaa aaa aa"));
    tabs.addTab("111112", new JLabel("bbb bbb bbb bbb bb bb"));
    tabs.addTab("22222232", new JScrollPane(new JTree()));
    tabs.addTab("3333333333", new JSplitPane());

    JComboBox<TabPlacements> comboBox = new JComboBox<>(TabPlacements.values());
    comboBox.addItemListener(e -> {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        tabs.setTabPlacement(((TabPlacements) e.getItem()).getTabPlacement());
      }
    });
    Box box = Box.createHorizontalBox();
    box.add(Box.createHorizontalGlue());
    box.add(new JLabel("TabPlacement: "));
    box.add(Box.createHorizontalStrut(2));
    box.add(comboBox);
    box.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

    add(tabs);
    add(box, BorderLayout.SOUTH);
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

enum TabPlacements {
  TOP(SwingConstants.TOP),
  BOTTOM(SwingConstants.BOTTOM),
  LEFT(SwingConstants.LEFT),
  RIGHT(SwingConstants.RIGHT);
  private final int tabPlacement;

  TabPlacements(int tabPlacement) {
    this.tabPlacement = tabPlacement;
  }

  public int getTabPlacement() {
    return tabPlacement;
  }
}

class WindowsTabHeightTabbedPaneUI extends WindowsTabbedPaneUI {
  @Override protected int calculateTabHeight(int tabPlacement, int tabIndex, int fontHeight) {
    return TabHeightUtils.TAB_AREA_HEIGHT;
    // return super.calculateTabHeight(tabPlacement, tabIndex, fontHeight) + 4;
  }

  @Override protected void paintTab(Graphics g, int tabPlacement, Rectangle[] rects, int tabIndex, Rectangle iconRect, Rectangle textRect) {
    Rectangle[] paintRects = TabHeightUtils.getPaintRects(tabPane, tabPlacement, rects, tabIndex);
    super.paintTab(g, tabPlacement, paintRects, tabIndex, iconRect, textRect);
  }
}

class BasicTabHeightTabbedPaneUI extends BasicTabbedPaneUI {
  @Override protected int calculateTabHeight(int tabPlacement, int tabIndex, int fontHeight) {
    return TabHeightUtils.TAB_AREA_HEIGHT;
  }

  @Override protected void paintTab(Graphics g, int tabPlacement, Rectangle[] rects, int tabIndex, Rectangle iconRect, Rectangle textRect) {
    Rectangle[] paintRects = TabHeightUtils.getPaintRects(tabPane, tabPlacement, rects, tabIndex);
    super.paintTab(g, tabPlacement, paintRects, tabIndex, iconRect, textRect);
  }
}

final class TabHeightUtils {
  public static final int TAB_AREA_HEIGHT = 32;
  private static final int LOW_TAB_HEIGHT = TAB_AREA_HEIGHT / 2 + 3;

  private TabHeightUtils() {
    /* Singleton */
  }

  // Returns the tab rectangles used only for painting: an unselected tab of the
  // TOP/BOTTOM placement is shortened (and moved down to the bottom of the tab area
  // in the TOP placement). A copy is returned so that the layout rectangles
  // shared with hit testing and scrolling are not modified while painting.
  public static Rectangle[] getPaintRects(
      JTabbedPane tabPane, int tabPlacement, Rectangle[] rects, int tabIndex) {
    boolean isHorizontal = tabPlacement == SwingConstants.TOP
        || tabPlacement == SwingConstants.BOTTOM;
    Rectangle[] paintRects = rects;
    if (isHorizontal && tabPane.getSelectedIndex() != tabIndex) {
      Rectangle r = new Rectangle(rects[tabIndex]);
      if (tabPlacement == SwingConstants.TOP) {
        r.y += r.height - LOW_TAB_HEIGHT;
      }
      r.height = LOW_TAB_HEIGHT;
      paintRects = rects.clone();
      paintRects[tabIndex] = r;
    }
    return paintRects;
  }
}
