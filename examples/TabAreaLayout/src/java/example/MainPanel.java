// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.plaf.LayerUI;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.plaf.synth.Region;
import javax.swing.plaf.synth.SynthConstants;
import javax.swing.plaf.synth.SynthContext;
import javax.swing.plaf.synth.SynthLookAndFeel;
import javax.swing.plaf.synth.SynthStyle;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new GridLayout(2, 1));
    add(makeOverlayTabbedPane());
    add(makeCardLayoutTabbedPane());
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    setPreferredSize(new Dimension(320, 240));
  }

  private Component makeCardLayoutTabbedPane() {
    CardLayoutTabbedPane tabs = new CardLayoutTabbedPane();
    tabs.addTab("1:JTree", new ColorIcon(Color.RED), new JScrollPane(new JTree()));
    tabs.addTab("2:JTable", new ColorIcon(Color.GREEN), new JScrollPane(new JTable(10, 3)));
    tabs.addTab("3:JTextArea", new ColorIcon(Color.BLUE), new JScrollPane(new JTextArea()));
    tabs.addTab("4:JButton", new ColorIcon(Color.CYAN), new JButton("JButton"));
    tabs.addTab("5:JCheckBox", new ColorIcon(Color.ORANGE), new JCheckBox("JCheckBox"));
    tabs.addTab("6:JRadioButton", new ColorIcon(Color.PINK), new JRadioButton("JRadioButton"));
    tabs.addTab("7:JSplitPane", new ColorIcon(Color.YELLOW), new JSplitPane());
    EventQueue.invokeLater(() -> tabs.getTabArea().getHorizontalScrollBar().setVisible(false));

    JPopupMenu popup = new JPopupMenu();
    popup.add("test: add");
    popup.add("test: delete");
    tabs.getTabArea().setComponentPopupMenu(popup);
    return tabs;
  }

  private Component makeOverlayTabbedPane() {
    int buttonSize = 20;
    JTabbedPane tabs = new ClippedTitleTabbedPane() {
      @Override public void updateUI() {
        String key = "TabbedPane.tabAreaInsets";
        UIManager.put(key, null); // uninstall
        super.updateUI();
        UIManager.put(key, getButtonPaddingTabAreaInsets());
        super.updateUI(); // reinstall
      }

      @Override public float getAlignmentX() {
        return LEFT_ALIGNMENT;
      }

      @Override public float getAlignmentY() {
        return TOP_ALIGNMENT;
      }

      private Insets getButtonPaddingTabAreaInsets() {
        Insets ti = getTabInsets();
        Insets ai = getTabAreaInsets();
        // Dimension d = button.getPreferredSize();
        FontMetrics fm = getFontMetrics(getFont());
        int tih = buttonSize - fm.getHeight() - ti.top - ti.bottom - ai.bottom;
        return new Insets(Math.max(ai.top, tih), ai.left, ai.bottom, ai.left + buttonSize);
      }
    };
    tabs.addTab("1:JTree", new ColorIcon(Color.RED), new JScrollPane(new JTree()));
    tabs.addTab("2:JTable", new ColorIcon(Color.GREEN), new JScrollPane(new JTable(10, 3)));
    tabs.addTab("3:JTextArea", new ColorIcon(Color.BLUE), new JScrollPane(new JTextArea()));
    tabs.addTab("4:JButton", new ColorIcon(Color.CYAN), new JButton("JButton"));

    Box box = Box.createHorizontalBox();
    box.setAlignmentX(LEFT_ALIGNMENT);
    // TEST: box.setOpaque(true);
    box.add(Box.createHorizontalGlue());
    box.add(makeDropdownButton(tabs, buttonSize));

    JPanel p = new JPanel();
    p.setLayout(new OverlayLayout(p));
    p.add(box);
    p.add(tabs);
    return p;
  }

  private JButton makeDropdownButton(JTabbedPane tabs, int buttonSize) {
    JButton button = new JButton("⊽") {
      private transient MouseListener handler;

      @Override public void updateUI() {
        removeMouseListener(handler);
        super.updateUI();
        setFocusable(false);
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
        handler = new MouseAdapter() {
          @Override public void mouseEntered(MouseEvent e) {
            setContentAreaFilled(true);
          }

          @Override public void mouseExited(MouseEvent e) {
            setContentAreaFilled(false);
          }
        };
        addMouseListener(handler);
      }

      @Override public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        d.width = buttonSize;
        return d;
      }

      @Override public float getAlignmentY() {
        return TOP_ALIGNMENT;
      }
    };
    button.addActionListener(e -> {
      JButton b = (JButton) e.getSource();
      Point p = b.getLocation();
      JPopupMenu popup = new JPopupMenu();
      int selected = tabs.getSelectedIndex();
      for (int i = 0; i < tabs.getTabCount(); i++) {
        popup.add(makeRadioMenuItem(tabs, i, selected));
      }
      p.x += b.getWidth() - popup.getPreferredSize().width - 1;
      p.y += b.getHeight();
      popup.show(b.getParent(), p.x, p.y);
    });
    return button;
  }

  private JMenuItem makeRadioMenuItem(JTabbedPane tabs, int i, int selected) {
    JMenuItem b = new JRadioButtonMenuItem(tabs.getTitleAt(i), i == selected);
    b.addActionListener(e -> tabs.setSelectedIndex(i));
    return b;
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

class CardLayoutTabbedPane extends JPanel {
  public static final int TAB_AREA_SIZE = 28;
  private final CardLayout cardLayout = new CardLayout();
  private final JPanel tabPanel = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0));
  private final JPanel contentsPanel = new JPanel(cardLayout);
  private final JButton hiddenTabs = new JButton("⊽") {
    private transient MouseListener handler;
    @Override public void updateUI() {
      removeMouseListener(handler);
      super.updateUI();
      setFocusable(false);
      setContentAreaFilled(false);
      setFocusPainted(false);
      setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
      handler = new MouseAdapter() {
        @Override public void mouseEntered(MouseEvent e) {
          setContentAreaFilled(true);
        }

        @Override public void mouseExited(MouseEvent e) {
          setContentAreaFilled(false);
        }
      };
      addMouseListener(handler);
    }

    @Override public float getAlignmentY() {
      return TOP_ALIGNMENT;
    }
  };

  private final ButtonGroup group = new ButtonGroup();
  private final JScrollPane tabArea = new JScrollPane(tabPanel) {
    @Override public boolean isOptimizedDrawingEnabled() {
      return false; // JScrollBar is overlap
    }

    @Override public void updateUI() {
      super.updateUI();
      EventQueue.invokeLater(() -> {
        getVerticalScrollBar().setUI(new OverlappedScrollBarUI());
        getHorizontalScrollBar().setUI(new OverlappedScrollBarUI());
        setLayout(new OverlapScrollPaneLayout());
        setComponentZOrder(getVerticalScrollBar(), 0);
        setComponentZOrder(getHorizontalScrollBar(), 1);
        setComponentZOrder(getViewport(), 2);
      });
      setVerticalScrollBarPolicy(VERTICAL_SCROLLBAR_NEVER);
      setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_ALWAYS);
      getVerticalScrollBar().setOpaque(false);
      getHorizontalScrollBar().setOpaque(false);
      setBackground(Color.DARK_GRAY);
      setViewportBorder(BorderFactory.createEmptyBorder());
      setBorder(BorderFactory.createEmptyBorder());
    }

    @Override public Dimension getPreferredSize() {
      Dimension d = super.getPreferredSize();
      d.height = TAB_AREA_SIZE;
      return d;
    }
  };

  protected CardLayoutTabbedPane() {
    super(new BorderLayout());
    tabPanel.setInheritsPopupMenu(true);
    hiddenTabs.addActionListener(e -> {
      JButton b = (JButton) e.getSource();
      Point p = b.getLocation();
      JPopupMenu popup = new JPopupMenu();
      JViewport viewport = tabArea.getViewport();
      for (int i = 0; i < tabPanel.getComponentCount(); i++) {
        getHiddenTabMenuItem(viewport, tabPanel.getComponent(i)).ifPresent(popup::add);
      }
      p.x += b.getWidth() - popup.getPreferredSize().width - 1;
      p.y += b.getHeight();
      popup.show(b.getParent(), p.x, p.y);
    });
    JPanel buttons = new JPanel(new GridBagLayout());
    buttons.add(hiddenTabs);
    JPanel header = new JPanel(new BorderLayout());
    header.add(new JLayer<>(tabArea, new HorizontalScrollLayerUI()));
    header.add(buttons, BorderLayout.EAST);
    add(header, BorderLayout.NORTH);
    add(contentsPanel);
  }

  @Override public final Component add(Component comp) {
    return super.add(comp);
  }

  @Override public final void add(Component comp, Object constraints) {
    super.add(comp, constraints);
  }

  @Override public void updateUI() {
    super.updateUI();
    setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
    setBackground(new Color(16, 16, 16));
  }

  private Optional<JMenuItem> getHiddenTabMenuItem(JViewport viewport, Component c) {
    return Optional.ofNullable(c)
        .filter(AbstractButton.class::isInstance)
        .map(AbstractButton.class::cast)
        .filter(tab -> !viewport.getViewRect().contains(tab.getBounds()))
        .map(tab -> {
          Rectangle r = tab.getBounds();
          JLabel label = (JLabel) tab.getComponent(0);
          String title = label.getText();
          JMenuItem mi = new JRadioButtonMenuItem(title);
          mi.addActionListener(e -> {
            tab.setSelected(true);
            cardLayout.show(contentsPanel, title);
            Container p = tab.getParent();
            viewport.scrollRectToVisible(SwingUtilities.convertRectangle(p, r, viewport));
          });
          return mi;
        });
  }

  protected JComponent createTabComponent(String title, Icon icon, Component comp) {
    JToggleButton tab = new TabButton();
    tab.setInheritsPopupMenu(true);
    group.add(tab);
    tab.addMouseListener(new MouseAdapter() {
      @Override public void mousePressed(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)) {
          ((AbstractButton) e.getComponent()).setSelected(true);
          cardLayout.show(contentsPanel, title);
        }
      }
    });
    EventQueue.invokeLater(() -> tab.setSelected(true));

    JLabel label = new JLabel(title, icon, SwingConstants.LEADING);
    label.setForeground(Color.WHITE);
    label.setIcon(icon);
    label.setOpaque(false);

    tab.add(label);
    tab.add(makeCloseButton(comp, tab), BorderLayout.EAST);
    return tab;
  }

  private JButton makeCloseButton(Component comp, JToggleButton tab) {
    JButton close = new JButton(new CloseTabIcon(new Color(0xB0_B0_B0))) {
      @Override public Dimension getPreferredSize() {
        return new Dimension(12, 12);
      }
    };
    close.addActionListener(e -> {
      tabPanel.remove(tab);
      contentsPanel.remove(comp);
      boolean oneOrMore = tabPanel.getComponentCount() > 1;
      if (oneOrMore) {
        tabPanel.revalidate();
        TabButton b = (TabButton) tabPanel.getComponent(0);
        b.setSelected(true);
        cardLayout.first(contentsPanel);
      }
      tabPanel.revalidate();
    });
    close.setBorder(BorderFactory.createEmptyBorder());
    close.setFocusable(false);
    close.setOpaque(false);
    // close.setFocusPainted(false);
    close.setContentAreaFilled(false);
    close.setPressedIcon(new CloseTabIcon(new Color(0xFE_FE_FE)));
    close.setRolloverIcon(new CloseTabIcon(new Color(0xA0_A0_A0)));
    return close;
  }

  public void addTab(String title, Icon icon, Component comp) {
    JComponent tab = createTabComponent(title, icon, comp);
    tabPanel.add(tab);
    contentsPanel.add(comp, title);
    cardLayout.show(contentsPanel, title);
    EventQueue.invokeLater(() -> tabPanel.scrollRectToVisible(tab.getBounds()));
  }

  public JScrollPane getTabArea() {
    return tabArea;
  }

  @Override public void doLayout() {
    BoundedRangeModel m = tabArea.getHorizontalScrollBar().getModel();
    hiddenTabs.setVisible(m.getMaximum() - m.getExtent() > 0);
    super.doLayout();
  }
}

class TabButton extends JToggleButton {
  private static final Border EMPTY_BORDER = BorderFactory.createEmptyBorder(2, 4, 4, 4);
  private static final Border SELECTED_BORDER = BorderFactory.createCompoundBorder(
      BorderFactory.createMatteBorder(0, 0, 3, 0, new Color(0xFA_00_AA_FF, true)),
      BorderFactory.createEmptyBorder(2, 4, 1, 4));
  private static final Color PRESSED_COLOR = new Color(32, 32, 32);
  private static final Color SELECTED_COLOR = new Color(48, 32, 32);
  private static final Color ROLLOVER_COLOR = new Color(48, 48, 48);

  protected TabButton() {
    super();
  }

  @Override public void updateUI() {
    super.updateUI();
    setLayout(new BorderLayout());
    setBorder(BorderFactory.createEmptyBorder(2, 4, 4, 4));
    setContentAreaFilled(false);
    setFocusPainted(false);
    setOpaque(true);
  }

  @Override public Dimension getPreferredSize() {
    Dimension d = super.getPreferredSize();
    d.height = CardLayoutTabbedPane.TAB_AREA_SIZE;
    return d;
  }

  @Override protected void fireStateChanged() {
    ButtonModel model = getModel();
    if (model.isEnabled()) {
      if (model.isPressed() || model.isArmed()) {
        setBackground(PRESSED_COLOR);
        setBorder(SELECTED_BORDER);
      } else if (model.isSelected()) {
        setBackground(SELECTED_COLOR);
        setBorder(SELECTED_BORDER);
      } else if (isRolloverEnabled() && model.isRollover()) {
        setBackground(ROLLOVER_COLOR);
        setBorder(EMPTY_BORDER);
      } else {
        setBackground(Color.GRAY);
        setBorder(EMPTY_BORDER);
      }
    } else {
      setBackground(Color.GRAY);
      setBorder(EMPTY_BORDER);
    }
    super.fireStateChanged();
  }
}

class CloseTabIcon implements Icon {
  private final Color color;

  protected CloseTabIcon(Color color) {
    this.color = color;
  }

  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.translate(x, y);
    g2.setPaint(color);
    g2.drawLine(3, 3, 9, 9);
    g2.drawLine(9, 3, 3, 9);
    g2.dispose();
  }

  @Override public int getIconWidth() {
    return 12;
  }

  @Override public int getIconHeight() {
    return 12;
  }
}

class ColorIcon implements Icon {
  private final Color color;

  protected ColorIcon(Color color) {
    this.color = color;
  }

  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.translate(x, y);
    g2.setPaint(color);
    g2.fillOval(1, 1, getIconWidth() - 2, getIconHeight() - 2);
    g2.dispose();
  }

  @Override public int getIconWidth() {
    return 16;
  }

  @Override public int getIconHeight() {
    return 16;
  }
}

class OverlapScrollPaneLayout extends ScrollPaneLayout {
  private static final int BAR_SIZE = 5;

  @Override public void layoutContainer(Container parent) {
    if (parent instanceof JScrollPane) {
      JScrollPane scrollPane = (JScrollPane) parent;
      Rectangle availR = SwingUtilities.calculateInnerArea(scrollPane, null);

      if (Objects.nonNull(colHead) && colHead.isVisible()) {
        Rectangle colHeadR = new Rectangle(0, availR.y, 0, 0);
        int colHeadHeight = Math.min(availR.height, colHead.getPreferredSize().height);
        colHeadR.height = colHeadHeight;
        availR.y += colHeadHeight;
        availR.height -= colHeadHeight;
        colHeadR.width = availR.width;
        colHeadR.x = availR.x;
        colHead.setBounds(colHeadR);
      }

      Optional.ofNullable(viewport).ifPresent(v -> v.setBounds(availR));

      Optional.ofNullable(vsb).ifPresent(sb -> {
        sb.setLocation(availR.x + availR.width - BAR_SIZE, availR.y);
        sb.setSize(BAR_SIZE, availR.height - BAR_SIZE);
        // sb.setVisible(true);
      });

      Optional.ofNullable(hsb).ifPresent(sb -> {
        sb.setLocation(availR.x, availR.y + availR.height - BAR_SIZE);
        sb.setSize(availR.width, BAR_SIZE);
        // sb.setVisible(true);
      });
    }
  }
}

class ZeroSizeButton extends JButton {
  private static final Dimension ZERO_SIZE = new Dimension();

  @Override public Dimension getPreferredSize() {
    return ZERO_SIZE;
  }
}

class OverlappedScrollBarUI extends BasicScrollBarUI {
  private static final Color DEFAULT_COLOR = new Color(0xAA_16_32_64, true);
  // private static final Color DRAGGING_COLOR = new Color(0xFA_FA_FA_FA, true);

  @Override protected JButton createDecreaseButton(int orientation) {
    return new ZeroSizeButton();
  }

  @Override protected JButton createIncreaseButton(int orientation) {
    return new ZeroSizeButton();
  }

  @Override protected void paintTrack(Graphics g, JComponent c, Rectangle r) {
    // Graphics2D g2 = (Graphics2D) g.create();
    // g2.setPaint(new Color(100, 100, 100, 100));
    // g2.fillRect(r.x, r.y, r.width - 1, r.height - 1);
    // g2.dispose();
  }

  @Override protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
    if (c.isEnabled() && !r.isEmpty()) {
      Graphics2D g2 = (Graphics2D) g.create();
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2.setPaint(DEFAULT_COLOR);
      g2.fillRect(r.x, r.y, r.width - 1, r.height - 1);
      g2.dispose();
    }
  }
}

class HorizontalScrollLayerUI extends LayerUI<JScrollPane> {
  private boolean isDragging;

  @Override public void installUI(JComponent c) {
    super.installUI(c);
    if (c instanceof JLayer) {
      ((JLayer<?>) c).setLayerEventMask(AWTEvent.MOUSE_EVENT_MASK
          | AWTEvent.MOUSE_MOTION_EVENT_MASK | AWTEvent.MOUSE_WHEEL_EVENT_MASK);
    }
  }

  @Override public void uninstallUI(JComponent c) {
    if (c instanceof JLayer) {
      ((JLayer<?>) c).setLayerEventMask(0);
    }
    super.uninstallUI(c);
  }

  @Override protected void processMouseEvent(MouseEvent e, JLayer<? extends JScrollPane> l) {
    JScrollBar hsb = l.getView().getHorizontalScrollBar();
    switch (e.getID()) {
      case MouseEvent.MOUSE_ENTERED:
        hsb.setVisible(true);
        break;
      case MouseEvent.MOUSE_EXITED:
        if (!isDragging) {
          hsb.setVisible(false);
        }
        break;
      case MouseEvent.MOUSE_RELEASED:
        if (isDragging) {
          isDragging = false;
          hsb.setVisible(false);
        }
        break;
      default:
        break;
    }
  }

  @Override protected void processMouseMotionEvent(MouseEvent e, JLayer<? extends JScrollPane> l) {
    if (e.getID() == MouseEvent.MOUSE_DRAGGED) {
      isDragging = true;
    }
  }

  @Override protected void processMouseWheelEvent(MouseWheelEvent e, JLayer<? extends JScrollPane> l) {
    JScrollPane scroll = l.getView();
    JScrollBar hsb = scroll.getHorizontalScrollBar();
    JViewport viewport = scroll.getViewport();
    Point vp = viewport.getViewPosition();
    vp.translate(hsb.getBlockIncrement() * e.getWheelRotation(), 0);
    JComponent v = (JComponent) SwingUtilities.getUnwrappedView(viewport);
    v.scrollRectToVisible(new Rectangle(vp, viewport.getSize()));
  }
}

class ClippedTitleTabbedPane extends JTabbedPane {
  private static final int MAX_TAB_WIDTH = 80;

  protected ClippedTitleTabbedPane() {
    super();
  }

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
    Insets tabIns = getTabInsets();
    Insets tabAreaIns = getTabAreaInsets();
    Insets ins = getInsets();
    int areaWidth = getWidth() - tabAreaIns.left - tabAreaIns.right - ins.left - ins.right;
    int tabWidth; // = tabIns.left + tabIns.right + 3;
    int gap;

    int placement = getTabPlacement();
    if (placement == LEFT || placement == RIGHT) {
      tabWidth = areaWidth / 4;
      gap = 0;
    } else { // TOP || BOTTOM
      tabWidth = areaWidth / tabCount;
      gap = areaWidth - tabWidth * tabCount;
    }
    if (tabWidth > MAX_TAB_WIDTH) {
      tabWidth = MAX_TAB_WIDTH;
      gap = 0;
    }

    // "3" is magic number @see BasicTabbedPaneUI#calculateTabWidth
    tabWidth -= tabIns.left + tabIns.right + 3;
    updateAllTabWidth(tabWidth, gap);

    super.doLayout();
  }

  @Override public void insertTab(String title, Icon icon, Component component, String tip, int index) {
    super.insertTab(title, icon, component, Objects.toString(tip, title), index);
    setTabComponentAt(index, new JLabel(title, icon, CENTER));
  }

  private void updateAllTabWidth(int tabWidth, int gap) {
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
}
