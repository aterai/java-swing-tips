// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import com.sun.java.swing.plaf.windows.WindowsTabbedPaneUI;
import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import javax.swing.*;
import javax.swing.plaf.basic.BasicTabbedPaneUI;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTabbedPane tabbedPane = new JTabbedPane() {
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
    List<? extends JTabbedPane> list = Arrays.asList(
        makeTabbedPane(new JTabbedPane()),
        makeTabbedPane(tabbedPane));
    JPanel p = new JPanel(new GridLayout(2, 1));
    list.forEach(p::add);

    JCheckBox check = new JCheckBox("LEFT");
    check.addActionListener(e -> {
      boolean b = ((JCheckBox) e.getSource()).isSelected();
      int tabPlacement = b ? SwingConstants.LEFT : SwingConstants.TOP;
      list.forEach(t -> t.setTabPlacement(tabPlacement));
    });

    add(check, BorderLayout.NORTH);
    add(p);
    setPreferredSize(new Dimension(320, 240));
  }

  private static JTabbedPane makeTabbedPane(JTabbedPane tabbedPane) {
    // tabbedPane.setTabPlacement(SwingConstants.RIGHT);
    // tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
    tabbedPane.addTab("1111111111111111111111111111", new JLabel("1"));
    tabbedPane.addTab("2", new JLabel("2"));
    tabbedPane.addTab("33333333333333333333333333333333333333333333", new JLabel("3"));
    tabbedPane.addTab("444444444444", new JLabel("4"));
    return tabbedPane;
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

class BasicClippedTitleTabbedPaneUI extends BasicTabbedPaneUI {
  // protected Insets tabInsets;
  // protected Insets selectedTabPadInsets;
  // protected Insets tabAreaInsets;
  // protected Insets contentBorderInsets;

  @Override protected int calculateTabWidth(int tabPlacement, int tabIndex, FontMetrics metrics) {
    // Insets i = tabPane.getInsets();
    // int w = tabPane.getWidth() - tabAreaInsets.left - tabAreaInsets.right - i.left - i.right;
    Rectangle r = SwingUtilities.calculateInnerArea(tabPane, null);
    r.width -= tabAreaInsets.left + tabAreaInsets.right;
    int tabWidth;
    if (tabPlacement == LEFT || tabPlacement == RIGHT) {
      tabWidth = r.width / 4;
    } else { // TOP || BOTTOM
      tabWidth = r.width / tabPane.getTabCount();
    }
    return tabWidth;
  }

  @Override protected void paintText(Graphics g, int tabPlacement, Font font, FontMetrics metrics, int tabIndex, String title, Rectangle textRect, boolean isSelected) {
    Rectangle tabRect = rects[tabIndex];
    int x = tabRect.x + tabInsets.left;
    int y = textRect.y;
    int w = tabRect.width - tabInsets.left - tabInsets.right;
    int h = textRect.height;
    Rectangle viewR = new Rectangle(x, y, w, h);
    Rectangle iconR = new Rectangle();
    Rectangle textR = new Rectangle();
    String clippedText = SwingUtilities.layoutCompoundLabel(
        metrics,
        title,
        null,
        CENTER,
        CENTER,
        CENTER,
        TRAILING,
        viewR,
        iconR,
        textR,
        0);
    if (Objects.equals(title, clippedText)) {
      super.paintText(g, tabPlacement, font, metrics, tabIndex, title, textRect, isSelected);
    } else {
      textR.x = textRect.x + tabInsets.left;
      super.paintText(g, tabPlacement, font, metrics, tabIndex, clippedText, textR, isSelected);
    }
  }
}

class WindowsClippedTitleTabbedPaneUI extends WindowsTabbedPaneUI {
  @Override protected int calculateTabWidth(int tabPlacement, int tabIndex, FontMetrics metrics) {
    Insets i = tabPane.getInsets();
    int tabWidth;
    int w = tabPane.getWidth() - tabAreaInsets.left - tabAreaInsets.right - i.left - i.right;
    if (tabPlacement == LEFT || tabPlacement == RIGHT) {
      tabWidth = w / 4;
    } else { // TOP || BOTTOM
      tabWidth = w / tabPane.getTabCount();
    }
    return tabWidth;
  }

  @Override protected void paintText(Graphics g, int tabPlacement, Font font, FontMetrics metrics, int tabIndex, String title, Rectangle textRect, boolean isSelected) {
    Rectangle tabRect = rects[tabIndex];
    int x = tabRect.x + tabInsets.left;
    int y = textRect.y;
    int w = tabRect.width - tabInsets.left - tabInsets.right;
    int h = textRect.height;
    Rectangle viewR = new Rectangle(x, y, w, h);
    Rectangle iconR = new Rectangle();
    Rectangle textR = new Rectangle();
    String clippedText = SwingUtilities.layoutCompoundLabel(
        metrics,
        title,
        null,
        CENTER,
        CENTER,
        CENTER,
        TRAILING,
        viewR,
        iconR,
        textR,
        0);
    if (Objects.equals(title, clippedText)) {
      super.paintText(g, tabPlacement, font, metrics, tabIndex, title, textRect, isSelected);
    } else {
      textR.x = textRect.x + tabInsets.left;
      super.paintText(g, tabPlacement, font, metrics, tabIndex, clippedText, textR, isSelected);
    }
  }
}
