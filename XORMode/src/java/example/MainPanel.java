// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    ImageIcon icon = new ImageIcon(getClass().getResource("test.png"));
    add(new ImageIconPanel(icon));
    setPreferredSize(new Dimension(320, 240));
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
      ex.printStackTrace();
      Toolkit.getDefaultToolkit().beep();
    }
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

class ImageIconPanel extends JPanel {
  private transient RubberBandingListener rbl;
  private final transient BasicStroke stroke = new BasicStroke(2f);
  private final Path2D rubberBand = new Path2D.Double();
  private final ImageIcon icon;

  protected ImageIconPanel(ImageIcon icon) {
    super();
    this.icon = icon;
  }

  @Override public void updateUI() {
    removeMouseListener(rbl);
    removeMouseMotionListener(rbl);
    super.updateUI();
    rbl = new RubberBandingListener();
    addMouseMotionListener(rbl);
    addMouseListener(rbl);
  }

  @Override protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    Graphics2D g2 = (Graphics2D) g.create();

    int iw = icon.getIconWidth();
    int ih = icon.getIconHeight();
    Dimension dim = getSize();
    int x = (dim.width - iw) / 2;
    int y = (dim.height - ih) / 2;
    g.drawImage(icon.getImage(), x, y, iw, ih, this);

    g2.setPaint(Color.RED);
    g2.fillOval(10, 10, 32, 32);

    g2.setPaint(Color.GREEN);
    g2.fillOval(50, 10, 32, 32);

    g2.setPaint(Color.BLUE);
    g2.fillOval(90, 10, 32, 32);

    g2.setPaint(Color.PINK);
    g2.fillOval(130, 10, 32, 32);

    g2.setPaint(Color.CYAN);
    g2.fillOval(170, 10, 32, 32);

    g2.setPaint(Color.ORANGE);
    g2.fillOval(210, 10, 32, 32);

    g2.setXORMode(Color.PINK);
    g2.fill(rubberBand);

    g2.setPaintMode();
    g2.setStroke(stroke);
    g2.setPaint(Color.WHITE);
    g2.draw(rubberBand);
    g2.dispose();
  }

  protected Path2D getRubberBand() {
    return rubberBand;
  }

  private class RubberBandingListener extends MouseAdapter {
    private final Point srcPoint = new Point();

    @Override public void mouseDragged(MouseEvent e) {
      Point destPoint = e.getPoint();
      Path2D rb = getRubberBand();
      rb.reset();
      rb.moveTo(srcPoint.x, srcPoint.y);
      rb.lineTo(destPoint.x, srcPoint.y);
      rb.lineTo(destPoint.x, destPoint.y);
      rb.lineTo(srcPoint.x, destPoint.y);
      rb.closePath();
      e.getComponent().repaint();
    }

    @Override public void mouseReleased(MouseEvent e) {
      e.getComponent().repaint();
    }

    @Override public void mousePressed(MouseEvent e) {
      getRubberBand().reset();
      srcPoint.setLocation(e.getPoint());
      e.getComponent().repaint();
    }
  }
}
