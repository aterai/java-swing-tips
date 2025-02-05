// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
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
  private static final String CLOSE_CURRENT_TAB = "close_current_tab";

  private MainPanel() {
    super(new BorderLayout());
    JTabbedPane tabbedPane = new ClippedTitleTabbedPane() {
      private final List<Component> history = new ArrayList<>(5);
      @Override public void setSelectedIndex(int index) {
        super.setSelectedIndex(index);
        Component component = getComponentAt(index);
        history.remove(component);
        history.add(0, component);
      }

      @Override public void removeTabAt(int index) {
        Component component = getComponentAt(index);
        super.removeTabAt(index);
        history.remove(component);
        if (!history.isEmpty()) {
          setSelectedComponent(history.get(0));
        }
      }
    };
    tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
    tabbedPane.addTab("aa aa", new JLabel("aaa"));
    tabbedPane.addTab("bbb bbb", new JLabel("bbb"));
    tabbedPane.addTab("ccc", new JLabel("ccc"));
    tabbedPane.addTab("d", new JLabel("ddd"));
    tabbedPane.addTab("ee", new JLabel("eee"));

    int modifiers = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
    // Java 10: int modifiers = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
    InputMap im = tabbedPane.getInputMap(WHEN_IN_FOCUSED_WINDOW);
    im.put(KeyStroke.getKeyStroke(KeyEvent.VK_W, modifiers), CLOSE_CURRENT_TAB);
    im.put(KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.CTRL_DOWN_MASK), CLOSE_CURRENT_TAB);

    tabbedPane.getActionMap().put(CLOSE_CURRENT_TAB, new AbstractAction() {
      @Override public void actionPerformed(ActionEvent e) {
        JTabbedPane t = (JTabbedPane) e.getSource();
        int idx = t.getSelectedIndex();
        if (idx >= 0) {
          t.removeTabAt(idx);
        }
      }
    });

    JButton button1 = new JButton("add tab");
    button1.addActionListener(e -> {
      String title = LocalTime.now(ZoneId.systemDefault()).toString();
      tabbedPane.addTab(title, new JLabel(title));
    });

    JButton button2 = new JButton("add tab with focus");
    button2.addActionListener(e -> {
      String title = LocalTime.now(ZoneId.systemDefault()).toString();
      tabbedPane.addTab(title, new JLabel(title));
      tabbedPane.setSelectedIndex(tabbedPane.getTabCount() - 1);
    });

    add(tabbedPane);
    JPanel p = new JPanel(new GridLayout(1, 2, 2, 2));
    p.add(button1);
    p.add(button2);
    add(p, BorderLayout.SOUTH);
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

// https://ateraimemo.com/Swing/ClippedTabLabel.html
class ClippedTitleTabbedPane extends JTabbedPane {
  private static final int MAX_TAB_WIDTH = 200;
  private static final int MIN_TAB_WIDTH = 50;

  protected ClippedTitleTabbedPane() {
    super();
  }

  // protected ClippedTitleTabbedPane(int tabPlacement) {
  //   super(tabPlacement);
  // }

  private Insets getSynthInsets(Region region) {
    SynthStyle style = SynthLookAndFeel.getStyle(this, region);
    SynthContext context = new SynthContext(this, region, style, SynthConstants.ENABLED);
    return style.getInsets(context, null);
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
    if (tabCount == 0 || !isVisible()) {
      super.doLayout();
      return;
    }
    Insets tabInsets = getTabInsets();
    Insets tabAreaInsets = getTabAreaInsets();
    Insets ins = getInsets();
    int areaWidth = getWidth() - tabAreaInsets.left - tabAreaInsets.right - ins.left - ins.right;
    // int tabWidth = tabInsets.left + tabInsets.right + 3;
    int gap = 0;

    boolean isTopBottom = isTopBottomTabPlacement(getTabPlacement());
    int tw = isTopBottom ? areaWidth / tabCount : areaWidth / 3;
    int tabWidth = Math.min(MAX_TAB_WIDTH, Math.max(MIN_TAB_WIDTH, tw));
    if (isTopBottom && tabWidth < MAX_TAB_WIDTH) {
      gap = areaWidth - tabWidth * tabCount;
    }

    // "3" is magic number @see BasicTabbedPaneUI#calculateTabWidth
    tabWidth -= tabInsets.left + tabInsets.right + 3;
    updateAllTabWidth(tabWidth, gap);

    super.doLayout();
  }

  @Override public void insertTab(String title, Icon icon, Component component, String tip, int index) {
    super.insertTab(title, icon, component, Objects.nonNull(tip) ? tip : title, index);
    setTabComponentAt(index, new ButtonTabComponent(this));
  }

  protected void updateAllTabWidth(int tabWidth, int gap) {
    Dimension dim = new Dimension();
    int rest = gap;
    for (int i = 0; i < getTabCount(); i++) {
      Component c = getTabComponentAt(i);
      if (c instanceof JComponent) {
        JComponent tab = (JComponent) c;
        int a = i == getTabCount() - 1 ? rest : 1;
        int w = rest > 0 ? tabWidth + a : tabWidth;
        dim.setSize(w, tab.getPreferredSize().height);
        tab.setPreferredSize(dim);
        rest -= a;
      }
    }
  }

  private static boolean isTopBottomTabPlacement(int tabPlacement) {
    return tabPlacement == TOP || tabPlacement == BOTTOM;
  }
}

// How to Use Tabbed Panes (The Java™ Tutorials > ... > Using Swing Components)
// https://docs.oracle.com/javase/tutorial/uiswing/components/tabbedpane.html
class ButtonTabComponent extends JPanel {
  private final JTabbedPane tabbedPane;

  protected ButtonTabComponent(JTabbedPane tabbedPane) {
    super(new BorderLayout());
    this.tabbedPane = Objects.requireNonNull(tabbedPane, "TabbedPane cannot be null");
    add(makeTitleLabel());
    add(makeCloseButton(), BorderLayout.EAST);
  }

  private JLabel makeTitleLabel() {
    return new JLabel() {
      @Override public String getText() {
        String txt = null;
        int i = tabbedPane.indexOfTabComponent(ButtonTabComponent.this);
        if (i != -1) {
          txt = tabbedPane.getTitleAt(i);
        }
        return txt;
      }

      @Override public Icon getIcon() {
        Icon icn = null;
        int i = tabbedPane.indexOfTabComponent(ButtonTabComponent.this);
        if (i != -1) {
          icn = tabbedPane.getIconAt(i);
        }
        return icn;
      }

      @Override public void updateUI() {
        super.updateUI();
        setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 2));
      }
    };
  }

  private JButton makeCloseButton() {
    JButton button = new JButton(new CloseTabIcon(Color.BLACK));
    button.setRolloverIcon(new CloseTabIcon(Color.ORANGE));
    button.setBorder(BorderFactory.createEmptyBorder());
    button.setFocusPainted(false);
    button.setBorderPainted(false);
    button.setContentAreaFilled(false);
    TabButtonHandler handler = new TabButtonHandler();
    button.addActionListener(handler);
    button.addMouseListener(handler);
    return button;
  }

  @Override public final Component add(Component comp) {
    return super.add(comp);
  }

  @Override public final void add(Component comp, Object constraints) {
    super.add(comp, constraints);
  }

  @Override public void updateUI() {
    super.updateUI();
    setOpaque(false);
    setBorder(BorderFactory.createEmptyBorder(1, 0, 0, 0));
  }

  private final class TabButtonHandler extends MouseAdapter implements ActionListener {
    @Override public void actionPerformed(ActionEvent e) {
      int i = tabbedPane.indexOfTabComponent(ButtonTabComponent.this);
      if (i != -1) {
        tabbedPane.remove(i);
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

class CloseTabIcon implements Icon {
  private final Color color;

  protected CloseTabIcon(Color color) {
    this.color = color;
  }

  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.translate(x, y);
    g2.setPaint(color);
    g2.drawLine(4, 4, 11, 11);
    g2.drawLine(4, 5, 10, 11);
    g2.drawLine(5, 4, 11, 10);
    g2.drawLine(11, 4, 4, 11);
    g2.drawLine(11, 5, 5, 11);
    g2.drawLine(10, 4, 4, 10);
    g2.dispose();
  }

  @Override public int getIconWidth() {
    return 16;
  }

  @Override public int getIconHeight() {
    return 16;
  }
}
