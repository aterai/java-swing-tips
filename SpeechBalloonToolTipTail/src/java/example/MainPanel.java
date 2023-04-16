// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.ItemEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.util.Optional;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout(2, 2));
    JTabbedPane tabbedPane = makeTabbedPane();
    JComboBox<TabPlacement> cb = new JComboBox<>(TabPlacement.values());
    cb.addItemListener(e -> {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        tabbedPane.setTabPlacement(cb.getItemAt(cb.getSelectedIndex()).getPlacement());
      }
    });
    add(cb, BorderLayout.NORTH);
    add(tabbedPane);
    setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
    setPreferredSize(new Dimension(320, 240));
  }

  private static JTabbedPane makeTabbedPane() {
    JTabbedPane tabs = new JTabbedPane(SwingConstants.TOP, JTabbedPane.SCROLL_TAB_LAYOUT) {
      private transient BalloonToolTip tip;

      @Override public Point getToolTipLocation(MouseEvent e) {
        int idx = indexAtLocation(e.getX(), e.getY());
        String txt = idx >= 0 ? getToolTipTextAt(idx) : null;
        return Optional.ofNullable(txt).map(toolTipText -> {
          JToolTip tips = createToolTip();
          tips.setTipText(toolTipText);
          if (tips instanceof BalloonToolTip) {
            ((BalloonToolTip) tips).updateBalloonShape(getTabPlacement());
          }
          return getToolTipPoint(getBoundsAt(idx), tips.getPreferredSize());
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
            dx = r.getMinX() - d.width;
            dy = r.getCenterY() - d.getHeight() / 2d;
            break;
          case BOTTOM:
            dx = r.getCenterX() - d.getWidth() / 2d;
            dy = r.getMinY() - d.height;
            break;
          default: // case TOP:
            dx = r.getCenterX() - d.getWidth() / 2d;
            dy = r.getMaxY();
        }
        return new Point((int) (dx + .5), (int) (dy + .5));
      }

      @Override public JToolTip createToolTip() {
        if (tip == null) {
          tip = new BalloonToolTip();
          tip.updateBalloonShape(getTabPlacement());
          tip.setComponent(this);
        }
        return tip;
      }
    };
    tabs.addTab("000", new ColorIcon(Color.RED), new JScrollPane(new JTree()), "00000");
    tabs.addTab("111", new ColorIcon(Color.GREEN), new JScrollPane(new JSplitPane()), "11111");
    tabs.addTab("222", new ColorIcon(Color.BLUE), new JScrollPane(new JTable(5, 5)), "22222");
    tabs.addTab("333", new ColorIcon(Color.ORANGE), new JLabel("6"), "33333");
    tabs.addTab("444", new ColorIcon(Color.CYAN), new JLabel("7"), "44444");
    tabs.addTab("555", new ColorIcon(Color.PINK), new JLabel("8"), "55555");
    return tabs;
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
  private transient HierarchyListener listener;
  private transient Shape shape;

  @Override public void updateUI() {
    removeHierarchyListener(listener);
    super.updateUI();
    listener = e -> {
      Component c = e.getComponent();
      if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && c.isShowing()) {
        Optional.ofNullable(SwingUtilities.getWindowAncestor(c))
            .filter(w -> w.getType() == Window.Type.POPUP)
            .ifPresent(w -> w.setBackground(new Color(0x0, true)));
      }
    };
    addHierarchyListener(listener);
    setOpaque(false);
  }

  @Override public Dimension getPreferredSize() {
    Dimension d = super.getPreferredSize();
    d.height = 24;
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
    super.paintComponent(g);
  }

  public void updateBalloonShape(int placement) {
    Rectangle r = getVisibleRect();
    r.setSize(getPreferredSize());
    Path2D tail = new Path2D.Double();
    double w = r.getWidth() - 1d;
    double h = r.getHeight() - 1d;
    double arc = 10d;
    Shape bubble;
    switch (placement) {
      case SwingConstants.LEFT:
        setBorder(BorderFactory.createEmptyBorder(2, 2 + SIZE, 2, 2));
        tail.moveTo(r.getMinX() + SIZE, r.getCenterY() - SIZE);
        tail.lineTo(r.getMinX(), r.getCenterY());
        tail.lineTo(r.getMinX() + SIZE, r.getCenterY() + SIZE);
        w -= SIZE;
        bubble = new RoundRectangle2D.Double(r.getMinX() + SIZE, r.getMinY(), w, h, arc, arc);
        break;
      case SwingConstants.RIGHT:
        setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2 + SIZE));
        tail.moveTo(r.getMaxX() - SIZE - 1d, r.getCenterY() - SIZE);
        tail.lineTo(r.getMaxX(), r.getCenterY());
        tail.lineTo(r.getMaxX() - SIZE - 1d, r.getCenterY() + SIZE);
        w -= SIZE;
        bubble = new RoundRectangle2D.Double(r.getMinX(), r.getMinY(), w, h, arc, arc);
        break;
      case SwingConstants.BOTTOM:
        setBorder(BorderFactory.createEmptyBorder(2, 2, 2 + SIZE, 2));
        tail.moveTo(r.getCenterX() - SIZE, r.getMaxY() - SIZE - 1d);
        tail.lineTo(r.getCenterX(), r.getMaxY());
        tail.lineTo(r.getCenterX() + SIZE, r.getMaxY() - SIZE - 1d);
        h -= SIZE;
        bubble = new RoundRectangle2D.Double(r.getMinX(), r.getMinY(), w, h, arc, arc);
        break;
      default: // case SwingConstants.TOP:
        setBorder(BorderFactory.createEmptyBorder(2 + SIZE, 2, 2, 2));
        tail.moveTo(r.getCenterX() - SIZE, r.getMinY() + SIZE);
        tail.lineTo(r.getCenterX(), r.getMinY());
        tail.lineTo(r.getCenterX() + SIZE, r.getMinY() + SIZE);
        h -= SIZE;
        bubble = new RoundRectangle2D.Double(r.getMinX(), r.getMinY() + SIZE, w, h, arc, arc);
    }
    Area area = new Area(bubble);
    area.add(new Area(tail));
    shape = area;
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
  BOTTOM(SwingConstants.BOTTOM),
  LEFT(SwingConstants.LEFT),
  RIGHT(SwingConstants.RIGHT);

  private final int placement;

  TabPlacement(int placement) {
    this.placement = placement;
  }

  public int getPlacement() {
    return placement;
  }
}
