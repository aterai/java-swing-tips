// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
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
            double rr = r * .5522;
            Path2D path = new Path2D.Double();
            path.moveTo(x, y + r);
            path.curveTo(x + rr, y + r, x + r, y + rr, x + r, y);
            path.lineTo(x + w - r, y);
            path.curveTo(x + w - r, y + rr, x + w - rr, y + r, x + w, y + r);
            path.lineTo(x + w, y + h - r);
            path.curveTo(x + w - rr, y + h - r, x + w - r, y + h - rr, x + w - r, y + h);
            path.lineTo(x + r, y + h);
            path.curveTo(x + r, y + h - rr, x + rr, y + h - r, x, y + h - r);
            // path.lineTo(x, y + r);
            path.closePath();
            return path;
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
    Area rect = new Area(new Rectangle2D.Double(x, y, w, h));
    rect.subtract(new Area(new Ellipse2D.Double(x - r, y - r, r + r, r + r)));
    rect.subtract(new Area(new Ellipse2D.Double(x + w - r, y - r, r + r, r + r)));
    rect.subtract(new Area(new Ellipse2D.Double(x - r, y + h - r, r + r, r + r)));
    rect.subtract(new Area(new Ellipse2D.Double(x + w - r, y + h - r, r + r, r + r)));
    return rect;
  }
}
