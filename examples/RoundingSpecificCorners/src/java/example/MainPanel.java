// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.EnumSet;
import java.util.Set;
import java.util.logging.Logger;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private final Set<Corner> corners = EnumSet.allOf(Corner.class);
  private final Set<Type> types = EnumSet.allOf(Type.class);

  private MainPanel() {
    super();
    JPopupMenu popup = new JPopupMenu();
    corners.forEach(c -> popup.add(createCheckBox(c, corners)));
    popup.addSeparator();
    types.forEach(t -> popup.add(createCheckBox(t, types)));
    setComponentPopupMenu(popup);
    setPreferredSize(new Dimension(320, 240));
  }

  private <E extends Enum<E>> JCheckBoxMenuItem createCheckBox(E item, Set<E> selected) {
    JCheckBoxMenuItem check = new JCheckBoxMenuItem(item.name(), selected.contains(item));
    check.addActionListener(e -> {
      if (((AbstractButton) e.getSource()).isSelected()) {
        selected.add(item);
      } else {
        selected.remove(item);
      }
      repaint();
    });
    return check;
  }

  @Override protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    double x = getWidth() * .1;
    double y = getHeight() * .1;
    double w = getWidth() * .8;
    double h = getHeight() * .8;
    double aw = w * .4;
    double ah = h * .4;
    Rectangle2D rect = new Rectangle2D.Double(x, y, w, h);

    if (types.contains(Type.ROUND_RECTANGLE2D)) {
      g2.setColor(Color.BLACK);
      g2.draw(new RoundRectangle2D.Double(x, y, w, h, aw, ah));
    }

    if (types.contains(Type.SUBTRACT)) {
      g2.setColor(Color.RED);
      Shape shape0 = ShapeUtils.createRoundRectBySubtraction(rect, aw, ah, corners);
      g2.draw(shape0);
    }

    if (types.contains(Type.PATH2D1)) {
      g2.setColor(Color.GREEN);
      Shape shape1 = ShapeUtils.createRoundRectWithCornerControlPoints(rect, aw, ah, corners);
      g2.draw(shape1);
    }

    if (types.contains(Type.PATH2D2)) {
      g2.setColor(Color.BLUE);
      Shape shape2 = ShapeUtils.createRoundRectWithKappa(rect, aw, ah, corners);
      g2.draw(shape2);
    }

    g2.dispose();
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

// https://stackoverflow.com/questions/77889724/how-to-create-half-rounded-border
enum Corner { TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT }

enum Type { ROUND_RECTANGLE2D, SUBTRACT, PATH2D1, PATH2D2 }

final class ShapeUtils {
  // Bezier control point ratio for approximating a quarter circle: 4 * (sqrt(2) - 1) / 3
  private static final double KAPPA = 4d * (Math.sqrt(2d) - 1d) / 3d; // = 0.55228...

  private ShapeUtils() {
    /* Singleton */
  }

  public static Shape createRoundRectBySubtraction(
      Rectangle2D r, double aw, double ah, Set<Corner> corners) {
    double x = r.getX();
    double y = r.getY();
    double w = r.getWidth();
    double h = r.getHeight();
    double arw = aw * .5;
    double arh = ah * .5;
    Area area = new Area(r);
    if (corners.contains(Corner.TOP_LEFT)) {
      Area tl = new Area(new Rectangle2D.Double(x, y, arw, arh));
      tl.subtract(new Area(new Ellipse2D.Double(x, y, aw, ah)));
      area.subtract(tl);
    }
    if (corners.contains(Corner.TOP_RIGHT)) {
      Area tr = new Area(new Rectangle2D.Double(x + w - arw, y, arw, arh));
      tr.subtract(new Area(new Ellipse2D.Double(x + w - aw, y, aw, ah)));
      area.subtract(tr);
    }
    if (corners.contains(Corner.BOTTOM_LEFT)) {
      Area bl = new Area(new Rectangle2D.Double(x, y + h - arh, arw, arh));
      bl.subtract(new Area(new Ellipse2D.Double(x, y + h - ah, aw, ah)));
      area.subtract(bl);
    }
    if (corners.contains(Corner.BOTTOM_RIGHT)) {
      Area br = new Area(new Rectangle2D.Double(x + w - arw, y + h - arh, arw, arh));
      br.subtract(new Area(new Ellipse2D.Double(x + w - aw, y + h - ah, aw, ah)));
      area.subtract(br);
    }
    return area;
  }

  public static Shape createRoundRectWithCornerControlPoints(
      Rectangle2D r, double aw, double ah, Set<Corner> corners) {
    double x = r.getX();
    double y = r.getY();
    double w = r.getWidth();
    double h = r.getHeight();
    double arw = aw * .5;
    double arh = ah * .5;
    Path2D.Double path = new Path2D.Double();
    // The first control point coincides with the start point and the second one
    // is the corner of the rectangle, so the curve does not match a quarter ellipse.
    if (corners.contains(Corner.TOP_LEFT)) {
      path.moveTo(x, y + arh);
      path.curveTo(x, y + arh, x, y, x + arw, y);
    } else {
      path.moveTo(x, y);
    }
    if (corners.contains(Corner.TOP_RIGHT)) {
      path.lineTo(x + w - arw, y);
      path.curveTo(x + w - arw, y, x + w, y, x + w, y + arh);
    } else {
      path.lineTo(x + w, y);
    }
    if (corners.contains(Corner.BOTTOM_RIGHT)) {
      path.lineTo(x + w, y + h - arh);
      path.curveTo(x + w, y + h - arh, x + w, y + h, x + w - arw, y + h);
    } else {
      path.lineTo(x + w, y + h);
    }
    if (corners.contains(Corner.BOTTOM_LEFT)) {
      path.lineTo(x + arw, y + h);
      path.curveTo(x + arw, y + h, x, y + h, x, y + h - arh);
    } else {
      path.lineTo(x, y + h);
    }
    path.closePath();
    return path;
  }

  public static Shape createRoundRectWithKappa(
      Rectangle2D r, double aw, double ah, Set<Corner> corners) {
    double x = r.getX();
    double y = r.getY();
    double w = r.getWidth();
    double h = r.getHeight();
    double arw = aw * .5;
    double arh = ah * .5;
    double akw = arw * KAPPA;
    double akh = arh * KAPPA;
    Path2D.Double p = new Path2D.Double();
    if (corners.contains(Corner.TOP_LEFT)) {
      p.moveTo(x, y + arh);
      p.curveTo(x, y + arh - akh, x + arw - akw, y, x + arw, y);
    } else {
      p.moveTo(x, y);
    }
    if (corners.contains(Corner.TOP_RIGHT)) {
      p.lineTo(x + w - arw, y);
      p.curveTo(x + w - arw + akw, y, x + w, y + arh - akh, x + w, y + arh);
    } else {
      p.lineTo(x + w, y);
    }
    if (corners.contains(Corner.BOTTOM_RIGHT)) {
      p.lineTo(x + w, y + h - arh);
      p.curveTo(x + w, y + h - arh + akh, x + w - arw + akw, y + h, x + w - arw, y + h);
    } else {
      p.lineTo(x + w, y + h);
    }
    if (corners.contains(Corner.BOTTOM_LEFT)) {
      p.lineTo(x + arw, y + h);
      p.curveTo(x + arw - akw, y + h, x, y + h - arh + akh, x, y + h - arh);
    } else {
      p.lineTo(x, y + h);
    }
    p.closePath();
    return p;
  }
}
