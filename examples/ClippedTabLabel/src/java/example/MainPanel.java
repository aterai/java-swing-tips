// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.plaf.synth.Region;
import javax.swing.plaf.synth.SynthConstants;
import javax.swing.plaf.synth.SynthContext;
import javax.swing.plaf.synth.SynthLookAndFeel;
import javax.swing.plaf.synth.SynthStyle;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    List<JTabbedPane> list = Arrays.asList(
        createTestTabbedPane(new JTabbedPane()),
        createTestTabbedPane(new ClippedTitleTabbedPane()));

    JPanel p = new JPanel(new GridLayout(list.size(), 1));
    list.forEach(p::add);

    JCheckBox check = new JCheckBox("LEFT");
    check.addActionListener(e -> {
      int placement = check.isSelected() ? SwingConstants.LEFT : SwingConstants.TOP;
      list.forEach(t -> t.setTabPlacement(placement));
    });

    add(check, BorderLayout.NORTH);
    add(p);
    setPreferredSize(new Dimension(320, 240));
  }

  private static JTabbedPane createTestTabbedPane(JTabbedPane jtp) {
    jtp.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
    jtp.addTab("1111111111111111111", new ColorIcon(Color.RED), new JScrollPane(new JTree()));
    jtp.addTab("2", new ColorIcon(Color.GREEN), new JLabel("666666666"));
    jtp.addTab("33333333333333", new ColorIcon(Color.BLUE), new JScrollPane(new JTree()));
    jtp.addTab("444444444444444", new ColorIcon(Color.ORANGE), new JLabel("7777777777"));
    jtp.addTab("55555555555555555555555555555555", new ColorIcon(Color.CYAN), new JLabel("8"));
    return jtp;
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
    frame.setMinimumSize(new Dimension(256, 200));
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

class ClippedTitleTabbedPane extends JTabbedPane {
  private Insets getSynthInsets(Region region) {
    SynthStyle style = SynthLookAndFeel.getStyle(this, region);
    SynthContext ctx = new SynthContext(this, region, style, SynthConstants.ENABLED);
    return style.getInsets(ctx, null);
  }

  protected Insets getTabInsets() {
    return Optional.ofNullable(UIManager.getInsets("TabbedPane.tabInsets"))
        .orElseGet(() -> getSynthInsets(Region.TABBED_PANE_TAB));
  }

  protected Insets getTabAreaInsets() {
    return Optional.ofNullable(UIManager.getInsets("TabbedPane.tabAreaInsets"))
        .orElseGet(() -> getSynthInsets(Region.TABBED_PANE_TAB_AREA));
  }

  @Override public void doLayout() {
    int tabCount = getTabCount();
    if (tabCount > 0 && isVisible()) {
      Insets tabAreaInsets = getTabAreaInsets();
      Insets insets = getInsets();
      int areaWidth = getWidth() - tabAreaInsets.left - tabAreaInsets.right
          - insets.left - insets.right;
      int tabPlacement = getTabPlacement();
      boolean horizontal = tabPlacement == TOP || tabPlacement == BOTTOM;
      int tabWidth = horizontal ? areaWidth / tabCount : areaWidth / 4;
      int gap = horizontal ? areaWidth - tabWidth * tabCount : 0;
      Insets tabInsets = getTabInsets();
      // This 3 is the magic number defined in BasicTabbedPaneUI#calculateTabWidth(...)
      tabWidth -= tabInsets.left + tabInsets.right + 3;
      updateAllTabWidths(tabWidth, gap);
    }
    super.doLayout();
  }

  @Override public void insertTab(String title, Icon icon, Component component, String tip, int index) {
    super.insertTab(title, icon, component, Objects.toString(tip, title), index);
    setTabComponentAt(index, new JLabel(title, icon, CENTER));
  }

  protected void updateAllTabWidths(int tabWidth, int gap) {
    for (int i = 0; i < getTabCount(); i++) {
      Component c = getTabComponentAt(i);
      if (c instanceof JComponent) {
        JComponent tab = (JComponent) c;
        // Distribute the remainder (gap < tabCount) one pixel each to the leading tabs
        int w = i < gap ? tabWidth + 1 : tabWidth;
        // Each tab needs its own Dimension: setPreferredSize(...) keeps the reference
        tab.setPreferredSize(new Dimension(w, tab.getPreferredSize().height));
      }
    }
  }
}

class ColorIcon implements Icon {
  private final Color color;

  protected ColorIcon(Color color) {
    this.color = color;
  }

  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.translate(x, y);
    g2.setPaint(color);
    g2.fillRect(1, 1, getIconWidth() - 2, getIconHeight() - 2);
    g2.dispose();
  }

  @Override public int getIconWidth() {
    return 16;
  }

  @Override public int getIconHeight() {
    return 16;
  }
}
