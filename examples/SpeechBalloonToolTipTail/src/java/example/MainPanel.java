// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.util.Arrays;
import java.util.Optional;
import java.util.logging.Logger;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout(2, 2));
    JTabbedPane tabs = new BalloonToolTipTabbedPane();
    tabs.addTab("000", new ColorIcon(Color.RED), new JScrollPane(new JTree()), "00000");
    tabs.addTab("111", new ColorIcon(Color.GREEN), new JSplitPane(), "1111");
    tabs.addTab("222", new ColorIcon(Color.BLUE), new JScrollPane(new JTable(5, 5)), "222");
    tabs.addTab("333", new ColorIcon(Color.ORANGE), new JLabel("6"), "33333333333333333333");
    tabs.addTab("444", new ColorIcon(Color.CYAN), new JLabel("7"), "44444444444444444444444");
    tabs.addTab("555", new ColorIcon(Color.PINK), new JLabel("8"), "5555555555555555555555555");

    JMenu menu = new JMenu("TabPlacement");
    ButtonGroup bg = new ButtonGroup();
    ItemListener handler = e -> {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        ButtonModel m = bg.getSelection();
        TabPlacement tp = TabPlacement.valueOf(m.getActionCommand());
        tabs.setTabPlacement(tp.getPlacement());
      }
    };
    Arrays.asList(TabPlacement.values()).forEach(tp -> {
      String name = tp.name();
      boolean selected = tp == TabPlacement.TOP;
      JMenuItem item = new JRadioButtonMenuItem(name, selected);
      item.addItemListener(handler);
      item.setActionCommand(name);
      menu.add(item);
      bg.add(item);
    });

    JMenuBar mb = new JMenuBar();
    mb.add(LookAndFeelUtils.createLookAndFeelMenu());
    mb.add(menu);
    EventQueue.invokeLater(() -> getRootPane().setJMenuBar(mb));

    add(tabs);
    setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
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

class BalloonToolTipTabbedPane extends JTabbedPane {
  private transient JToolTip tip;

  protected BalloonToolTipTabbedPane() {
    super(TOP, SCROLL_TAB_LAYOUT);
  }

  @Override public Point getToolTipLocation(MouseEvent e) {
    int idx = indexAtLocation(e.getX(), e.getY());
    String txt = idx >= 0 ? getToolTipTextAt(idx) : null;
    return Optional.ofNullable(txt).map(toolTipText -> {
      JToolTip toolTip = createToolTip();
      toolTip.setTipText(toolTipText);
      Component c = toolTip.getComponent(0);
      if (c instanceof JLabel) {
        ((JLabel) c).setText(toolTipText);
      }
      if (toolTip instanceof BalloonToolTip) {
        ((BalloonToolTip) toolTip).updateBalloonShape(getTabPlacement());
      }
      return getToolTipPoint(getBoundsAt(idx), toolTip.getPreferredSize());
    }).orElse(null);
  }

  private Point getToolTipPoint(Rectangle r, Dimension d) {
    double dx;
    double dy;
    switch (getTabPlacement()) {
      case LEFT:
        dx = r.getMaxX();
        dy = r.getCenterY() - d.getHeight() / 2d;
        break;
      case RIGHT:
        dx = r.getMinX() - d.getWidth();
        dy = r.getCenterY() - d.getHeight() / 2d;
        break;
      case BOTTOM:
        dx = r.getCenterX() - d.getWidth() / 2d;
        dy = r.getMinY() - d.getHeight();
        break;
      default: // case TOP:
        dx = r.getCenterX() - d.getWidth() / 2d;
        dy = r.getMaxY() + 8d;
    }
    return new Point((int) (dx + .5), (int) (dy + .5));
  }

  @Override public JToolTip createToolTip() {
    // if (tip == null) {
    //   tip = new BalloonToolTip();
    //   LookAndFeel.installColorsAndFont(
    //       label, "ToolTip.background", "ToolTip.foreground", "ToolTip.font");
    //   tip.add(label);
    //   tip.updateBalloonShape(getTabPlacement());
    //   tip.setComponent(this);
    // }
    return tip;
  }

  @Override public void updateUI() {
    // tip = null;
    super.updateUI();
    BalloonToolTip toolTip = new BalloonToolTip();
    JLabel label = new JLabel(" ", CENTER);
    LookAndFeel.installColorsAndFont(
        label, "ToolTip.background", "ToolTip.foreground", "ToolTip.font");
    toolTip.add(label);
    toolTip.updateBalloonShape(getTabPlacement());
    toolTip.setComponent(this);
    tip = toolTip;
  }
}

class BalloonToolTip extends JToolTip {
  private static final int SIZE = 4;
  private static final double ARC = 4d;
  private transient HierarchyListener listener;
  private transient Shape shape;

  @Override public void updateUI() {
    removeHierarchyListener(listener);
    super.updateUI();
    setLayout(new BorderLayout());
    listener = e -> {
      Component c = e.getComponent();
      if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && c.isShowing()) {
        Optional.ofNullable(SwingUtilities.getWindowAncestor(c))
            .filter(w -> {
              boolean isHeavyWeight = w.getType() == Window.Type.POPUP;
              GraphicsConfiguration gc = w.getGraphicsConfiguration();
              return gc != null && gc.isTranslucencyCapable() && isHeavyWeight;
            })
            .ifPresent(w -> w.setBackground(new Color(0x0, true)));
      }
    };
    addHierarchyListener(listener);
    // UIDefaults d = new UIDefaults();
    // d.put("ToolTip[Enabled].backgroundPainter", (Painter<JToolTip>) (g, o, w, h) -> {
    //   /* empty painter */
    // });
    // putClientProperty("Nimbus.Overrides", d);
    setOpaque(false);
    setBorder(BorderFactory.createEmptyBorder(SIZE, SIZE, SIZE, SIZE));
  }

  @Override public Dimension getPreferredSize() {
    Dimension d = super.getPreferredSize();
    d.width += SIZE;
    d.height += SIZE;
    return d;
  }

  @Override protected void paintComponent(Graphics g) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setColor(getBackground());
    g2.fill(shape);
    g2.setPaint(getForeground());
    g2.draw(shape);
    g2.dispose();
    // super.paintComponent(g);
  }

  public void updateBalloonShape(int placement) {
    Insets i = getInsets();
    Dimension d = getPreferredSize();
    Path2D tail = new Path2D.Double();
    double w = d.getWidth() - i.left - i.right - 1d;
    double h = d.getHeight() - i.top - i.bottom - 1d;
    double cx = w / 2d;
    double cy = h / 2d;
    switch (placement) {
      case SwingConstants.LEFT:
        tail.moveTo(0, cy - SIZE);
        tail.lineTo(-SIZE, cy);
        tail.lineTo(0, cy + SIZE);
        break;
      case SwingConstants.RIGHT:
        tail.moveTo(w, cy - SIZE);
        tail.lineTo(w + SIZE, cy);
        tail.lineTo(w, cy + SIZE);
        break;
      case SwingConstants.BOTTOM:
        tail.moveTo(cx - SIZE, h);
        tail.lineTo(cx, h + SIZE);
        tail.lineTo(cx + SIZE, h);
        break;
      default: // case SwingConstants.TOP:
        tail.moveTo(cx - SIZE, 0);
        tail.lineTo(cx, -SIZE);
        tail.lineTo(cx + SIZE, 0);
    }
    Area area = new Area(new RoundRectangle2D.Double(0, 0, w, h, ARC, ARC));
    area.add(new Area(tail));
    AffineTransform at = AffineTransform.getTranslateInstance(i.left, i.top);
    shape = at.createTransformedShape(area);
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
    g2.fillRect(1, 2, getIconWidth() - 2, getIconHeight() - 2);
    g2.dispose();
  }

  @Override public int getIconWidth() {
    return 16;
  }

  @Override public int getIconHeight() {
    return 16;
  }
}

enum TabPlacement {
  TOP(SwingConstants.TOP),
  LEFT(SwingConstants.LEFT),
  BOTTOM(SwingConstants.BOTTOM),
  RIGHT(SwingConstants.RIGHT);

  private final int placement;

  TabPlacement(int placement) {
    this.placement = placement;
  }

  public int getPlacement() {
    return placement;
  }
}

final class LookAndFeelUtils {
  private static String lookAndFeel = UIManager.getLookAndFeel().getClass().getName();

  private LookAndFeelUtils() {
    /* Singleton */
  }

  public static JMenu createLookAndFeelMenu() {
    JMenu menu = new JMenu("LookAndFeel");
    ButtonGroup buttonGroup = new ButtonGroup();
    for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
      AbstractButton b = makeButton(info);
      initLookAndFeelAction(info, b);
      menu.add(b);
      buttonGroup.add(b);
    }
    return menu;
  }

  private static AbstractButton makeButton(UIManager.LookAndFeelInfo info) {
    boolean selected = info.getClassName().equals(lookAndFeel);
    return new JRadioButtonMenuItem(info.getName(), selected);
  }

  public static void initLookAndFeelAction(UIManager.LookAndFeelInfo info, AbstractButton b) {
    String cmd = info.getClassName();
    b.setText(info.getName());
    b.setActionCommand(cmd);
    b.setHideActionText(true);
    b.addActionListener(e -> setLookAndFeel(cmd));
  }

  private static void setLookAndFeel(String newLookAndFeel) {
    String oldLookAndFeel = lookAndFeel;
    if (!oldLookAndFeel.equals(newLookAndFeel)) {
      try {
        UIManager.setLookAndFeel(newLookAndFeel);
        lookAndFeel = newLookAndFeel;
      } catch (UnsupportedLookAndFeelException ignored) {
        Toolkit.getDefaultToolkit().beep();
      } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
        Logger.getGlobal().severe(ex::getMessage);
        return;
      }
      updateLookAndFeel();
      // firePropertyChange("lookAndFeel", oldLookAndFeel, newLookAndFeel);
    }
  }

  private static void updateLookAndFeel() {
    for (Window window : Window.getWindows()) {
      SwingUtilities.updateComponentTreeUI(window);
    }
  }
}
