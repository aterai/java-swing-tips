// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout(5, 5));
    JScrollPane scroll = new JScrollPane(new JTable(8, 5)) {
      private static final int ARC = 8;

      @Override protected void paintComponent(Graphics g) {
        Border b = getBorder();
        if (!isOpaque() && b instanceof PlaqueBorder) {
          Graphics2D g2 = (Graphics2D) g.create();
          g2.setPaint(getBackground());
          int w = getWidth() - 1;
          int h = getHeight() - 1;
          g2.fill(((PlaqueBorder) b).getBorderShape(0, 0, w, h, ARC));
          g2.dispose();
        }
        super.paintComponent(g);
      }

      @Override public void updateUI() {
        super.updateUI();
        setOpaque(false);
        setBackground(Color.WHITE);
        getViewport().setBackground(getBackground());
        setBorder(new PlaqueBorder(ARC) {
          @Override protected Shape getBorderShape(double x, double y, double w, double h, double r) {
            return makeBorderShape(x, y, w, h, r);
          }
        });
      }
    };

    JTextField field = new JTextField("JTextField") {
      private static final int ARC = 4;

      @Override protected void paintComponent(Graphics g) {
        Border b = getBorder();
        if (!isOpaque() && b instanceof PlaqueBorder) {
          Graphics2D g2 = (Graphics2D) g.create();
          g2.setPaint(getBackground());
          int w = getWidth() - 1;
          int h = getHeight() - 1;
          g2.fill(((PlaqueBorder) b).getBorderShape(0, 0, w, h, ARC));
          g2.dispose();
        }
        super.paintComponent(g);
      }

      @Override public void updateUI() {
        super.updateUI();
        setOpaque(false);
        setBorder(new PlaqueBorder(ARC));
      }
    };

    add(scroll);
    add(field, BorderLayout.SOUTH);
    // setBackground(Color.RED);
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    setPreferredSize(new Dimension(320, 240));
  }

  private static Shape makeBorderShape(double x, double y, double w, double h, double r) {
    double rr = r * .5522;
    Path2D path = new Path2D.Double();
    path.moveTo(0, r);
    path.curveTo(rr, r, r, rr, r, y);
    path.lineTo(w - r, 0);
    path.curveTo(w - r, rr, w - rr, r, w, r);
    path.lineTo(w, h - r);
    path.curveTo(w - rr, h - r, w - r, h - rr, w - r, h);
    path.lineTo(r, h);
    path.curveTo(r, h - rr, rr, h - r, 0, h - r);
    // path.lineTo(0, r);
    path.closePath();
    return AffineTransform.getTranslateInstance(x, y).createTransformedShape(path);
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

class PlaqueBorder extends EmptyBorder {
  protected PlaqueBorder(int arc) {
    super(arc, arc, arc, arc);
  }

  @Override public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setPaint(Color.GRAY);
    Insets i = getBorderInsets(c);
    int arc = Math.min(i.top, Math.min(width / 2, height / 2));
    g2.draw(getBorderShape(x, y, width - 1d, height - 1d, arc));
    g2.dispose();
  }

  protected Shape getBorderShape(double x, double y, double w, double h, double r) {
    double a = r + r;
    Area rect = new Area(new Rectangle2D.Double(0, 0, w, h));
    rect.subtract(new Area(new Ellipse2D.Double(-r, -r, a, a)));
    rect.subtract(new Area(new Ellipse2D.Double(w - r, -r, a, a)));
    rect.subtract(new Area(new Ellipse2D.Double(-r, h - r, a, a)));
    rect.subtract(new Area(new Ellipse2D.Double(w - r, h - r, a, a)));
    return AffineTransform.getTranslateInstance(x, y).createTransformedShape(rect);
  }
}
