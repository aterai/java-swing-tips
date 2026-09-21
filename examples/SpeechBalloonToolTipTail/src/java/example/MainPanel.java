// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.util.Arrays;
import java.util.logging.Logger;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout(2, 2));
    JTabbedPane tabs = new BalloonToolTipTabbedPane();
    tabs.addTab("000", new ColorIcon(Color.RED), new JScrollPane(new JTree()), "00000");
    tabs.addTab("111", new ColorIcon(Color.GREEN), new JSplitPane(), "11111");
    tabs.addTab("222", new ColorIcon(Color.BLUE), new JScrollPane(new JTable(5, 5)), "222");
    tabs.addTab("333", new ColorIcon(Color.ORANGE), new JLabel("6"), "33333333333333");
    tabs.addTab("444", new ColorIcon(Color.CYAN), new JLabel("7"), "4444444444444444444");
    tabs.addTab("555", new ColorIcon(Color.PINK), new JLabel("8"), "555555555555555555555");

    JMenu menu = new JMenu("TabPlacement");
    ButtonGroup bg = new ButtonGroup();
    Arrays.asList(TabPlacement.values()).forEach(tp -> {
      JMenuItem item = new JRadioButtonMenuItem(tp.name(), tp == TabPlacement.TOP);
      item.addActionListener(e -> tabs.setTabPlacement(tp.getPlacement()));
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
  private transient BalloonToolTip tip;

  protected BalloonToolTipTabbedPane() {
    super(TOP, SCROLL_TAB_LAYOUT);
  }

  @Override public Point getToolTipLocation(MouseEvent e) {
    int idx = indexAtLocation(e.getX(), e.getY());
    String txt = idx >= 0 ? getToolTipTextAt(idx) : null;
    Point pt = null;
    if (txt != null) {
      // ToolTipManager calls this before createToolTip() and setTipText(...),
      // so set the text here to get the size of the balloon for this tab.
      tip.setTipText(txt);
      tip.setTailPlacement(getTabPlacement());
      pt = getTipLocation(getBoundsAt(idx), tip.getPreferredSize());
    }
    return pt;
  }

  // Place the tip of the tail at the center of the tab edge facing the content.
  private Point getTipLocation(Rectangle tabRect, Dimension tipSize) {
    double dx;
    double dy;
    switch (getTabPlacement()) {
      case LEFT:
        dx = tabRect.getMaxX();
        dy = tabRect.getCenterY() - tipSize.getHeight() / 2d;
        break;
      case RIGHT:
        dx = tabRect.getMinX() - tipSize.getWidth();
        dy = tabRect.getCenterY() - tipSize.getHeight() / 2d;
        break;
      case BOTTOM:
        dx = tabRect.getCenterX() - tipSize.getWidth() / 2d;
        dy = tabRect.getMinY() - tipSize.getHeight();
        break;
      default: // case TOP:
        dx = tabRect.getCenterX() - tipSize.getWidth() / 2d;
        dy = tabRect.getMaxY();
    }
    return new Point((int) Math.round(dx), (int) Math.round(dy));
  }

  @Override public JToolTip createToolTip() {
    return tip;
  }

  @Override public void updateUI() {
    super.updateUI();
    // The cached tip is not a child of this pane, so recreate it for the new LookAndFeel
    tip = new BalloonToolTip();
    tip.setComponent(this);
  }
}

class BalloonToolTip extends JToolTip {
  private static final int TAIL_SIZE = 4;
  private static final double ARC = 4d;
  private JLabel label;
  private transient HierarchyListener listener;
  private int tailPlacement = SwingConstants.TOP;

  @Override public void updateUI() {
    removeHierarchyListener(listener);
    super.updateUI();
    if (label == null) {
      // The text is painted by the JLabel instead of the ToolTipUI so that
      // the LookAndFeel (e.g. NimbusLookAndFeel) does not paint its own background
      label = new JLabel("", SwingConstants.CENTER);
      label.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
      setLayout(new BorderLayout());
      add(label);
    }
    LookAndFeel.installColorsAndFont(
        label, "ToolTip.background", "ToolTip.foreground", "ToolTip.font");
    listener = e -> {
      Component c = e.getComponent();
      if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && c.isShowing()) {
        Window w = SwingUtilities.getWindowAncestor(c);
        if (isTranslucencyCapablePopup(w)) {
          // Popup$HeavyWeightWindow: make the area outside the balloon transparent
          w.setBackground(new Color(0x0, true));
        }
      }
    };
    addHierarchyListener(listener);
    setOpaque(false);
    // Leave room for the tail on every side
    setBorder(BorderFactory.createEmptyBorder(TAIL_SIZE, TAIL_SIZE, TAIL_SIZE, TAIL_SIZE));
  }

  private static boolean isTranslucencyCapablePopup(Window w) {
    GraphicsConfiguration gc = w == null ? null : w.getGraphicsConfiguration();
    return gc != null && gc.isTranslucencyCapable() && w.getType() == Window.Type.POPUP;
  }

  @Override public void setTipText(String tipText) {
    super.setTipText(tipText);
    label.setText(tipText);
  }

  /**
   * Sets the side of the balloon on which the tail is drawn.
   *
   * @param placement one of {@code SwingConstants.TOP}, {@code LEFT},
   *                  {@code BOTTOM} or {@code RIGHT}
   */
  public void setTailPlacement(int placement) {
    if (tailPlacement != placement) {
      tailPlacement = placement;
      repaint();
    }
  }

  @Override public Dimension getPreferredSize() {
    return getLayout().preferredLayoutSize(this);
  }

  @Override protected void paintComponent(Graphics g) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(
        RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    Shape balloon = createBalloonShape();
    g2.setPaint(getBackground());
    g2.fill(balloon);
    g2.setPaint(getForeground());
    g2.draw(balloon);
    g2.dispose();
    // super.paintComponent(g);
  }

  private Shape createBalloonShape() {
    Insets i = getInsets();
    // -1: keep the 1px outline inside the component bounds
    double w = getWidth() - i.left - i.right - 1d;
    double h = getHeight() - i.top - i.bottom - 1d;
    double cx = w / 2d;
    double cy = h / 2d;
    Path2D tail = new Path2D.Double();
    switch (tailPlacement) {
      case SwingConstants.LEFT:
        tail.moveTo(0, cy - TAIL_SIZE);
        tail.lineTo(-TAIL_SIZE, cy);
        tail.lineTo(0, cy + TAIL_SIZE);
        break;
      case SwingConstants.RIGHT:
        tail.moveTo(w, cy - TAIL_SIZE);
        tail.lineTo(w + TAIL_SIZE, cy);
        tail.lineTo(w, cy + TAIL_SIZE);
        break;
      case SwingConstants.BOTTOM:
        tail.moveTo(cx - TAIL_SIZE, h);
        tail.lineTo(cx, h + TAIL_SIZE);
        tail.lineTo(cx + TAIL_SIZE, h);
        break;
      default: // case SwingConstants.TOP:
        tail.moveTo(cx - TAIL_SIZE, 0);
        tail.lineTo(cx, -TAIL_SIZE);
        tail.lineTo(cx + TAIL_SIZE, 0);
    }
    Area area = new Area(new RoundRectangle2D.Double(0, 0, w, h, ARC, ARC));
    area.add(new Area(tail));
    AffineTransform at = AffineTransform.getTranslateInstance(i.left, i.top);
    return at.createTransformedShape(area);
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
      AbstractButton b = createButton(info);
      initLookAndFeelAction(info, b);
      menu.add(b);
      buttonGroup.add(b);
    }
    return menu;
  }

  private static AbstractButton createButton(UIManager.LookAndFeelInfo info) {
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
