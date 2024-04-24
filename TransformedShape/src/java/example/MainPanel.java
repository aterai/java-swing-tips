// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    add(new FontRotateAnimation("A"));
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
    // frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

class FontRotateAnimation extends JComponent {
  private int rotate;
  private transient Shape shape;
  private transient HierarchyListener listener;
  private final Timer animator = new Timer(10, null);

  protected FontRotateAnimation(String str) {
    super();
    Font font = new Font(Font.SERIF, Font.PLAIN, 200);
    FontRenderContext frc = new FontRenderContext(null, true, true);
    Shape outline = new TextLayout(str, font, frc).getOutline(null);
    shape = outline;
    animator.addActionListener(e -> {
      repaint(shape.getBounds()); // clear prev
      Rectangle2D b = outline.getBounds2D();
      double ax = b.getCenterX();
      double ay = b.getCenterY();
      AffineTransform at = AffineTransform.getRotateInstance(Math.toRadians(rotate), ax, ay);
      double cx = getWidth() / 2d - ax;
      double cy = getHeight() / 2d - ay;
      AffineTransform toCenterAt = AffineTransform.getTranslateInstance(cx, cy);

      Shape s1 = at.createTransformedShape(outline);
      shape = toCenterAt.createTransformedShape(s1);
      repaint(shape.getBounds());
      // rotate = rotate >= 360 ? 0 : rotate + 2;
      rotate = (rotate + 2) % 360;
    });
    animator.start();
  }

  @Override public void updateUI() {
    removeHierarchyListener(listener);
    super.updateUI();
    listener = e -> {
      boolean b = (e.getChangeFlags() & HierarchyEvent.DISPLAYABILITY_CHANGED) != 0;
      if (b && !e.getComponent().isDisplayable()) {
        animator.stop();
      }
    };
    addHierarchyListener(listener);
  }

  @Override protected void paintComponent(Graphics g) {
    Graphics2D g2 = (Graphics2D) g.create();
    // g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, VALUE_TEXT_ANTIALIAS_ON);
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setPaint(Color.BLACK);
    g2.fill(shape);
    g2.dispose();
  }
}
