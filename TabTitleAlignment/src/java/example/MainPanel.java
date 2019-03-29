// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import com.sun.java.swing.plaf.windows.WindowsTabbedPaneUI;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.swing.*;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.plaf.metal.MetalTabbedPaneUI;
import javax.swing.plaf.synth.Region;
import javax.swing.plaf.synth.SynthConstants;
import javax.swing.plaf.synth.SynthContext;
import javax.swing.plaf.synth.SynthLookAndFeel;
import javax.swing.plaf.synth.SynthStyle;
import javax.swing.text.View;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());

    JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.LEFT);
    if (tabbedPane.getUI() instanceof WindowsTabbedPaneUI) {
      tabbedPane.setUI(new LeftAlignmentWindowsTabbedPaneUI());
    } else {
      tabbedPane.setUI(new LeftAlignmentTabbedPaneUI());
    }

    List<? extends JTabbedPane> list = Arrays.asList(
        makeTestTabbedPane(new JTabbedPane(JTabbedPane.LEFT)),
        makeTestTabbedPane(tabbedPane),
        makeTestTabbedPane(new ClippedTitleTabbedPane(JTabbedPane.LEFT)));

    JPanel p = new JPanel(new GridLayout(list.size(), 1));
    list.forEach(p::add);

    JCheckBox check = new JCheckBox("TOP");
    check.addActionListener(e -> {
      int tabPlacement = ((JCheckBox) e.getSource()).isSelected() ? JTabbedPane.TOP : JTabbedPane.LEFT;
      list.forEach(t -> t.setTabPlacement(tabPlacement));
    });

    add(check, BorderLayout.NORTH);
    add(p);
    setPreferredSize(new Dimension(320, 240));
  }

  private static JTabbedPane makeTestTabbedPane(JTabbedPane jtp) {
    jtp.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
    jtp.addTab("1111111111111111111", new ColorIcon(Color.RED), new JScrollPane(new JTree()));
    jtp.addTab("2", new ColorIcon(Color.GREEN), new JLabel("bbbbbbbbb"));
    jtp.addTab("33333333333333", new ColorIcon(Color.BLUE), new JScrollPane(new JTree()));
    jtp.addTab("444444444444444", new ColorIcon(Color.ORANGE), new JLabel("dddddddddd"));
    jtp.addTab("55555555555555555555555555555555", new ColorIcon(Color.CYAN), new JLabel("e"));
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
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
      ex.printStackTrace();
      Toolkit.getDefaultToolkit().beep();
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

  protected Insets getTabInsets() {
    Insets insets = UIManager.getInsets("TabbedPane.tabInsets");
    if (Objects.nonNull(insets)) {
      return insets;
    } else {
      SynthStyle style = SynthLookAndFeel.getStyle(this, Region.TABBED_PANE_TAB);
      SynthContext context = new SynthContext(this, Region.TABBED_PANE_TAB, style, SynthConstants.ENABLED);
      return style.getInsets(context, null);
    }
  }

  protected Insets getTabAreaInsets() {
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
      tabWidth = areaWidth / 2;
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
    setTabComponentAt(index, new ButtonTabComponent(this));
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

class LeftAlignmentWindowsTabbedPaneUI extends WindowsTabbedPaneUI {
  @Override protected void layoutLabel(int tabPlacement, FontMetrics metrics, int tabIndex, String title, Icon icon, Rectangle tabRect, Rectangle iconRect, Rectangle textRect, boolean isSelected) {
    textRect.setLocation(0, 0);
    iconRect.setLocation(0, 0);
    View v = getTextViewForTab(tabIndex);
    String html = "html";
    if (Objects.nonNull(v)) {
      tabPane.putClientProperty(html, v);
    }
    SwingUtilities.layoutCompoundLabel(
        tabPane,
        metrics, title, icon,
        SwingConstants.CENTER,
        SwingConstants.LEFT, // CENTER, <----
        SwingConstants.CENTER,
        SwingConstants.TRAILING,
        tabRect,
        iconRect,
        textRect,
        textIconGap);
    tabPane.putClientProperty(html, null);

    textRect.translate(tabInsets.left + 2, 0); // <----
    iconRect.translate(tabInsets.left + 2, 0); // <----

    int xnudge = getTabLabelShiftX(tabPlacement, tabIndex, isSelected);
    int ynudge = getTabLabelShiftY(tabPlacement, tabIndex, isSelected);
    iconRect.x += xnudge;
    iconRect.y += ynudge;
    textRect.x += xnudge;
    textRect.y += ynudge;
  }
}

class LeftAlignmentTabbedPaneUI extends MetalTabbedPaneUI {
  @Override protected void layoutLabel(int tabPlacement, FontMetrics metrics, int tabIndex, String title, Icon icon, Rectangle tabRect, Rectangle iconRect, Rectangle textRect, boolean isSelected) {
    textRect.setLocation(0, 0);
    iconRect.setLocation(0, 0);
    View v = getTextViewForTab(tabIndex);
    if (Objects.nonNull(v)) {
      tabPane.putClientProperty("html", v);
    }
    SwingUtilities.layoutCompoundLabel(
        tabPane,
        metrics, title, icon,
        SwingConstants.CENTER,
        SwingConstants.LEFT, // CENTER, <----
        SwingConstants.CENTER,
        SwingConstants.TRAILING,
        tabRect,
        iconRect,
        textRect,
        textIconGap);
    tabPane.putClientProperty("html", null);

    textRect.translate(tabInsets.left + 2, 0); // <----
    iconRect.translate(tabInsets.left + 2, 0); // <----

    int xnudge = getTabLabelShiftX(tabPlacement, tabIndex, isSelected);
    int ynudge = getTabLabelShiftY(tabPlacement, tabIndex, isSelected);
    iconRect.x += xnudge;
    iconRect.y += ynudge;
    textRect.x += xnudge;
    textRect.y += ynudge;
  }
}

// How to Use Tabbed Panes (The Java™ Tutorials > Creating a GUI With JFC/Swing > Using Swing Components)
// https://docs.oracle.com/javase/tutorial/uiswing/components/tabbedpane.html
class ButtonTabComponent extends JPanel {
  protected final JTabbedPane tabs;

  protected ButtonTabComponent(JTabbedPane pane) {
    super(new BorderLayout()); // FlowLayout(FlowLayout.LEFT, 0, 0));
    this.tabs = Optional.ofNullable(pane).orElseThrow(() -> new IllegalArgumentException("TabbedPane cannot be null"));
    setOpaque(false);
    JLabel label = new JLabel() {
      @Override public String getText() {
        int i = tabs.indexOfTabComponent(ButtonTabComponent.this);
        if (i != -1) {
          return tabs.getTitleAt(i);
        }
        return null;
      }

      @Override public Icon getIcon() {
        int i = tabs.indexOfTabComponent(ButtonTabComponent.this);
        if (i != -1) {
          return tabs.getIconAt(i);
        }
        return null;
      }
    };
    add(label);
    label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
    JButton button = new TabButton();
    TabButtonHandler handler = new TabButtonHandler();
    button.addActionListener(handler);
    button.addMouseListener(handler);
    add(button, BorderLayout.EAST);
    setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
  }

  private class TabButtonHandler extends MouseAdapter implements ActionListener {
    @Override public void actionPerformed(ActionEvent e) {
      int i = tabs.indexOfTabComponent(ButtonTabComponent.this);
      if (i != -1) {
        tabs.remove(i);
      }
    }

    @Override public void mouseEntered(MouseEvent e) {
      Component component = e.getComponent();
      if (component instanceof AbstractButton) {
        AbstractButton button = (AbstractButton) component;
        button.setBorderPainted(true);
      }
    }

    @Override public void mouseExited(MouseEvent e) {
      Component component = e.getComponent();
      if (component instanceof AbstractButton) {
        AbstractButton button = (AbstractButton) component;
        button.setBorderPainted(false);
      }
    }
  }
}

class TabButton extends JButton {
  private static final int SIZE = 17;
  private static final int DELTA = 6;

  protected TabButton() {
    super();
    setUI(new BasicButtonUI());
    setToolTipText("close this tab");
    setContentAreaFilled(false);
    setFocusable(false);
    setBorder(BorderFactory.createEtchedBorder());
    setBorderPainted(false);
    setRolloverEnabled(true);
  }

  @Override public Dimension getPreferredSize() {
    return new Dimension(SIZE, SIZE);
  }

  @Override public void updateUI() {
    // we don't want to update UI for this button
  }

  @Override protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setStroke(new BasicStroke(2));
    g2.setPaint(Color.BLACK);
    if (getModel().isRollover()) {
      g2.setPaint(Color.ORANGE);
    }
    if (getModel().isPressed()) {
      g2.setPaint(Color.BLUE);
    }
    g2.drawLine(DELTA, DELTA, getWidth() - DELTA - 1, getHeight() - DELTA - 1);
    g2.drawLine(getWidth() - DELTA - 1, DELTA, DELTA, getHeight() - DELTA - 1);
    g2.dispose();
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
