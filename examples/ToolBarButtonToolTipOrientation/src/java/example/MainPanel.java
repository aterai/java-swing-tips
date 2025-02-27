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
import java.util.List;
import java.util.Optional;
import javax.swing.*;
import javax.swing.plaf.basic.BasicToolBarUI;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JToolBar toolBar = new JToolBar("ToolBarButton");
    boolean leftToRight = toolBar.getComponentOrientation().isLeftToRight();
    JCheckBox check = new JCheckBox("", leftToRight);
    check.setToolTipText("isLeftToRight");
    check.addActionListener(e -> {
      ComponentOrientation orientation = ((JCheckBox) e.getSource()).isSelected()
          ? ComponentOrientation.LEFT_TO_RIGHT
          : ComponentOrientation.RIGHT_TO_LEFT;
      toolBar.setComponentOrientation(orientation);
      toolBar.revalidate();
    });
    makeList().forEach(i -> toolBar.add(createToolBarButton(i)));
    toolBar.add(check);
    add(toolBar, BorderLayout.NORTH);
    add(new JScrollPane(new JTree()));
    setPreferredSize(new Dimension(320, 240));
  }

  private Component createToolBarButton(ColorItem item) {
    JButton button = new JButton(item.getIcon()) {
      private transient JToolTip tip;

      @Override public Point getToolTipLocation(MouseEvent e) {
        String txt = getToolTipText();
        return Optional.ofNullable(txt).map(toolTipText -> {
          JToolTip toolTip = createToolTip();
          toolTip.setTipText(toolTipText);
          Component c = toolTip.getComponent(0);
          if (c instanceof JLabel) {
            ((JLabel) c).setText(toolTipText);
          }
          Container bar = SwingUtilities.getAncestorOfClass(JToolBar.class, this);
          String constraint = calculateConstraint(bar.getParent(), bar);
          if (toolTip instanceof BalloonToolTip) {
            ((BalloonToolTip) toolTip).updateBalloonShape(constraint);
          }
          Dimension btnSize = getPreferredSize();
          Dimension tipSize = toolTip.getPreferredSize();
          return getToolTipPoint(btnSize, tipSize, constraint);
        }).orElse(null);
      }

      @Override public JToolTip createToolTip() {
        return tip;
      }

      @Override public void updateUI() {
        super.updateUI();
        BalloonToolTip toolTip = new BalloonToolTip();
        JLabel label = new JLabel(" ", CENTER);
        LookAndFeel.installColorsAndFont(
            label, "ToolTip.background", "ToolTip.foreground", "ToolTip.font");
        toolTip.add(label);
        toolTip.setComponent(this);
        // EventQueue.invokeLater(() -> {
        //   Container bar = SwingUtilities.getAncestorOfClass(JToolBar.class, this);
        //   String constraint = calculateConstraint(bar.getParent(), bar);
        //   toolTip.updateBalloonShape(constraint);
        // });
        tip = toolTip;
      }
    };
    button.setOpaque(false);
    button.setToolTipText(item.getTitle());
    button.setFocusPainted(false);
    return button;
  }

  private static String calculateConstraint(Container source, Component toolBar) {
    String constraint = null;
    JToolBar bar = (JToolBar) toolBar;
    if (((BasicToolBarUI) bar.getUI()).isFloating()) {
      if (bar.getOrientation() == SwingConstants.VERTICAL) {
        boolean leftToRight = bar.getComponentOrientation().isLeftToRight();
        constraint = leftToRight ? BorderLayout.WEST : BorderLayout.EAST;
      }
    } else {
      LayoutManager lm = source.getLayout();
      if (lm instanceof BorderLayout) {
        constraint = (String) ((BorderLayout) lm).getConstraints(toolBar);
      }
    }
    return constraint == null ? BorderLayout.NORTH : constraint;
  }

  private static Point getToolTipPoint(Dimension btnSz, Dimension tipSz, String constraint) {
    double dx;
    double dy;
    switch (constraint) {
      case BorderLayout.WEST:
        dx = btnSz.getWidth();
        dy = (btnSz.getHeight() - tipSz.getHeight()) / 2d;
        break;
      case BorderLayout.EAST:
        dx = -tipSz.getWidth();
        dy = (btnSz.getHeight() - tipSz.getHeight()) / 2d;
        break;
      case BorderLayout.SOUTH:
        dx = (btnSz.getWidth() - tipSz.getWidth()) / 2d;
        dy = -tipSz.getHeight();
        break;
      default: // case BorderLayout.NORTH:
        dx = (btnSz.getWidth() - tipSz.getWidth()) / 2d;
        dy = btnSz.getHeight();
    }
    return new Point((int) (dx + .5), (int) (dy + .5));
  }

  private static List<ColorItem> makeList() {
    return Arrays.asList(
        new ColorItem("red", new ColorIcon(Color.RED)),
        new ColorItem("green", new ColorIcon(Color.GREEN)),
        new ColorItem("blue", new ColorIcon(Color.BLUE)),
        new ColorItem("cyan", new ColorIcon(Color.CYAN)),
        new ColorItem("magenta", new ColorIcon(Color.MAGENTA)),
        new ColorItem("orange", new ColorIcon(Color.ORANGE)),
        new ColorItem("pink", new ColorIcon(Color.PINK)),
        new ColorItem("yellow", new ColorIcon(Color.YELLOW))
    );
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

  public void updateBalloonShape(String placement) {
    Insets i = getInsets();
    Dimension d = getPreferredSize();
    Path2D tail = new Path2D.Double();
    double w = d.getWidth() - i.left - i.right - 1d;
    double h = d.getHeight() - i.top - i.bottom - 1d;
    double cx = w / 2d;
    double cy = h / 2d;
    switch (placement) {
      case BorderLayout.WEST:
        tail.moveTo(0, cy - SIZE);
        tail.lineTo(-SIZE, cy);
        tail.lineTo(0, cy + SIZE);
        break;
      case BorderLayout.EAST:
        tail.moveTo(w, cy - SIZE);
        tail.lineTo(w + SIZE, cy);
        tail.lineTo(w, cy + SIZE);
        break;
      case BorderLayout.SOUTH:
        tail.moveTo(cx - SIZE, h);
        tail.lineTo(cx, h + SIZE);
        tail.lineTo(cx + SIZE, h);
        break;
      default: // case BorderLayout.NORTH:
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

class ColorItem {
  private final String title;
  private final Icon icon;

  protected ColorItem(String title, Icon icon) {
    this.title = title;
    this.icon = icon;
  }

  public String getTitle() {
    return title;
  }

  public Icon getIcon() {
    return icon;
  }
}
