// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import com.sun.java.swing.plaf.windows.WindowsTabbedPaneUI;

import java.awt.*;
import java.util.Arrays;
import java.util.List;
import javax.swing.*;
import javax.swing.plaf.basic.BasicTabbedPaneUI;

public final class MainPanel extends JPanel {
  private final JTabbedPane tabbedPane = new JTabbedPane() {
    @Override public String getToolTipTextAt(int index) {
      return getTitleAt(index);
    }

    @Override public void insertTab(String title, Icon icon, Component component, String tip, int index) {
      super.insertTab(title, icon, component, title, index);
    }

    @Override public void updateUI() {
      super.updateUI();
      if (getUI() instanceof WindowsTabbedPaneUI) {
        setUI(new WindowsClippedTitleTabbedPaneUI());
      } else {
        setUI(new BasicClippedTitleTabbedPaneUI());
      }
    }
  };

  private MainPanel() {
    super(new BorderLayout());
    List<? extends JTabbedPane> list = Arrays.asList(
        makeTabbedPane(new JTabbedPane()),
        makeTabbedPane(tabbedPane));
    JPanel p = new JPanel(new GridLayout(2, 1));
    list.forEach(p::add);

    JCheckBox check = new JCheckBox("LEFT");
    check.addActionListener(e -> {
      int tabPlacement = ((JCheckBox) e.getSource()).isSelected() ? JTabbedPane.LEFT : JTabbedPane.TOP;
      list.forEach(t -> t.setTabPlacement(tabPlacement));
    });

    add(check, BorderLayout.NORTH);
    add(p);
    setPreferredSize(new Dimension(320, 240));
  }

  private static JTabbedPane makeTabbedPane(JTabbedPane tabbedPane) {
    // tabbedPane.setTabPlacement(JTabbedPane.RIGHT);
    // tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
    tabbedPane.addTab("1111111111111111111111111111", new JLabel("aaaaaaaaaaa"));
    tabbedPane.addTab("2", new JLabel("bbbbbbbbb"));
    tabbedPane.addTab("33333333333333333333333333333333333333333333", new JLabel("cccccccccc"));
    tabbedPane.addTab("444444444444", new JLabel("dddddddddddddddd"));
    return tabbedPane;
  }

  public static void main(String... args) {
    EventQueue.invokeLater(new Runnable() {
      @Override public void run() {
        createAndShowGui();
      }
    });
  }

  public static void createAndShowGui() {
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

class BasicClippedTitleTabbedPaneUI extends BasicTabbedPaneUI {
  // protected Insets tabInsets;
  // protected Insets selectedTabPadInsets;
  // protected Insets tabAreaInsets;
  // protected Insets contentBorderInsets;

  @Override protected int calculateTabWidth(int tabPlacement, int tabIndex, FontMetrics metrics) {
    Insets insets = tabPane.getInsets();
    // Insets tabAreaInsets = getTabAreaInsets(tabPlacement);
    int width = tabPane.getWidth() - tabAreaInsets.left - tabAreaInsets.right - insets.left - insets.right;
    if (tabPlacement == LEFT || tabPlacement == RIGHT) {
      return (int) (width / 4);
    } else { // TOP || BOTTOM
      return (int) (width / tabPane.getTabCount());
    }
  }

  @Override protected void paintText(Graphics g, int tabPlacement, Font font, FontMetrics metrics, int tabIndex, String title, Rectangle textRect, boolean isSelected) {
    int fw = (int) font.getSize();
    Rectangle tabRect = rects[tabIndex];
    Rectangle rect = new Rectangle(textRect.x + fw / 2, textRect.y, tabRect.width - fw, textRect.height);
    String clippedText = SwingUtilities.layoutCompoundLabel(
        metrics, title, null,
        SwingConstants.CENTER, SwingConstants.CENTER,
        SwingConstants.CENTER, SwingConstants.TRAILING,
        rect, new Rectangle(), rect, 0);
    if (title.equals(clippedText)) {
      super.paintText(g, tabPlacement, font, metrics, tabIndex, title, textRect, isSelected);
    } else {
      rect = new Rectangle(textRect.x + fw / 2, textRect.y, tabRect.width - fw, textRect.height);
      super.paintText(g, tabPlacement, font, metrics, tabIndex, clippedText, rect, isSelected);
    }
  }
}

class WindowsClippedTitleTabbedPaneUI extends WindowsTabbedPaneUI {
  // protected Insets tabInsets;
  // protected Insets selectedTabPadInsets;
  // protected Insets tabAreaInsets;
  // protected Insets contentBorderInsets;
  @Override protected int calculateTabWidth(int tabPlacement, int tabIndex, FontMetrics metrics) {
    Insets insets = tabPane.getInsets();
    // Insets tabAreaInsets = getTabAreaInsets(tabPlacement);
    int width = tabPane.getWidth() - tabAreaInsets.left - tabAreaInsets.right - insets.left - insets.right;
    if (tabPlacement == LEFT || tabPlacement == RIGHT) {
      return (int) (width / 4);
    } else { // TOP || BOTTOM
      return (int) (width / tabPane.getTabCount());
    }
  }

  @Override protected void paintText(Graphics g, int tabPlacement, Font font, FontMetrics metrics, int tabIndex, String title, Rectangle textRect, boolean isSelected) {
    Rectangle tabRect = rects[tabIndex];
    Rectangle rect = new Rectangle(
        textRect.x + tabInsets.left, textRect.y,
        tabRect.width - tabInsets.left - tabInsets.right, textRect.height);
    String clippedText = SwingUtilities.layoutCompoundLabel(
        metrics, title, null, SwingConstants.CENTER, SwingConstants.CENTER, SwingConstants.CENTER,
        SwingConstants.TRAILING, rect, new Rectangle(), rect, 0);
    if (title.equals(clippedText)) {
      super.paintText(g, tabPlacement, font, metrics, tabIndex, title, textRect, isSelected);
    } else {
      rect = new Rectangle(
        textRect.x + tabInsets.left, textRect.y, tabRect.width - tabInsets.left - tabInsets.right, textRect.height);
      super.paintText(g, tabPlacement, font, metrics, tabIndex, clippedText, rect, isSelected);
    }
  }
}
