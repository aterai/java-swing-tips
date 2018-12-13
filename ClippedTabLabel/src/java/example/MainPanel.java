// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import javax.swing.*;
import javax.swing.plaf.synth.Region;
import javax.swing.plaf.synth.SynthConstants;
import javax.swing.plaf.synth.SynthContext;
import javax.swing.plaf.synth.SynthLookAndFeel;
import javax.swing.plaf.synth.SynthStyle;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());

    List<? extends JTabbedPane> list = Arrays.asList(
        makeTestTabbedPane(new JTabbedPane()),
        makeTestTabbedPane(new ClippedTitleTabbedPane()));

    JPanel p = new JPanel(new GridLayout(list.size(), 1));
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

  private static JTabbedPane makeTestTabbedPane(JTabbedPane jtp) {
    jtp.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
    jtp.addTab("1111111111111111111", new JScrollPane(new JTree()));
    jtp.addTab("2", new JLabel("bbbbbbbbb"));
    jtp.addTab("33333333333333", new JScrollPane(new JTree()));
    jtp.addTab("444444444444444", new JLabel("dddddddddd"));
    jtp.addTab("55555555555555555555555555555555", new JLabel("e"));
    return jtp;
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
    } catch (ClassNotFoundException | InstantiationException
         | IllegalAccessException | UnsupportedLookAndFeelException ex) {
      ex.printStackTrace();
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
  protected ClippedTitleTabbedPane() {
    super();
  }

  protected ClippedTitleTabbedPane(int tabPlacement) {
    super(tabPlacement);
  }

  private Insets getTabInsets() {
    Insets insets = UIManager.getInsets("TabbedPane.tabInsets");
    if (Objects.nonNull(insets)) {
      return insets;
    } else {
      SynthStyle style = SynthLookAndFeel.getStyle(this, Region.TABBED_PANE_TAB);
      SynthContext context = new SynthContext(this, Region.TABBED_PANE_TAB, style, SynthConstants.ENABLED);
      return style.getInsets(context, null);
    }
  }

  private Insets getTabAreaInsets() {
    Insets insets = UIManager.getInsets("TabbedPane.tabAreaInsets");
    if (Objects.nonNull(insets)) {
      return insets;
    } else {
      SynthStyle style = SynthLookAndFeel.getStyle(this, Region.TABBED_PANE_TAB_AREA);
      SynthContext context = new SynthContext(this, Region.TABBED_PANE_TAB_AREA, style, SynthConstants.ENABLED);
      return style.getInsets(context, null);
    }
  }

  @Override public void doLayout() {
    int tabCount = getTabCount();
    if (tabCount == 0 || !isVisible()) {
      super.doLayout();
      return;
    }
    Insets tabInsets = getTabInsets();
    Insets tabAreaInsets = getTabAreaInsets();
    Insets insets = getInsets();
    int tabPlacement = getTabPlacement();
    int areaWidth = getWidth() - tabAreaInsets.left - tabAreaInsets.right - insets.left - insets.right;
    int tabWidth = 0; // = tabInsets.left + tabInsets.right + 3;
    int gap = 0;

    if (tabPlacement == LEFT || tabPlacement == RIGHT) {
      tabWidth = areaWidth / 4;
      gap = 0;
    } else { // TOP || BOTTOM
      tabWidth = areaWidth / tabCount;
      gap = areaWidth - tabWidth * tabCount;
    }

    // "3" is magic number @see BasicTabbedPaneUI#calculateTabWidth
    tabWidth -= tabInsets.left + tabInsets.right + 3;
    updateAllTabWidth(tabWidth, gap);

    super.doLayout();
  }

  @Override public void insertTab(String title, Icon icon, Component component, String tip, int index) {
    super.insertTab(title, icon, component, Objects.toString(tip, title), index);
    setTabComponentAt(index, new JLabel(title, SwingConstants.CENTER));
  }

  protected void updateAllTabWidth(int tabWidth, int gap) {
    Dimension dim = new Dimension();
    int rest = gap;
    for (int i = 0; i < getTabCount(); i++) {
      JComponent tab = (JComponent) getTabComponentAt(i);
      if (Objects.nonNull(tab)) {
        int a = (i == getTabCount() - 1) ? rest : 1;
        int w = rest > 0 ? tabWidth + a : tabWidth;
        dim.setSize(w, tab.getPreferredSize().height);
        tab.setPreferredSize(dim);
        rest -= a;
      }
    }
  }
}
