// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.logging.Logger;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private static final Color BG_WINDOW = new Color(10, 10, 10);

  private MainPanel() {
    super(new GridBagLayout());
    LocalTime now = LocalTime.now(ZoneId.systemDefault());
    FlipPair hourPair = new FlipPair(now.getHour());
    FlipPair minPair = new FlipPair(now.getMinute());
    FlipPair secPair = new FlipPair(now.getSecond());
    JPanel container = new JPanel(new FlowLayout(FlowLayout.CENTER, 2, 2));
    container.setOpaque(false);
    container.add(hourPair);
    container.add(FlipPair.createColonLabel());
    container.add(minPair);
    container.add(FlipPair.createColonLabel());
    container.add(secPair);
    add(container);
    new Timer(100, e -> {
      LocalTime t = LocalTime.now(ZoneId.systemDefault());
      hourPair.setValue(t.getHour());
      minPair.setValue(t.getMinute());
      secPair.setValue(t.getSecond());
    }).start();
    setBackground(BG_WINDOW);
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

class FlipPair extends JPanel {
  private static final Color CARD_UPPER_BG = new Color(45, 45, 45);
  private static final Color CARD_LOWER_BG = new Color(25, 25, 25);
  private static final Color TEXT_COLOR = new Color(230, 230, 230);
  private static final Color SLIT_COLOR = new Color(10, 10, 10, 220);
  private static final Color EDGE_LIGHT = new Color(255, 255, 255, 60);
  private static final int SHADOW_MAX_ALPHA = 130;
  private static final int PNL_WIDTH = 80;
  private static final int PNL_HEIGHT = 100;
  private static final int SLIT_HEIGHT = 4;
  private static final int FONT_SIZE = 64;
  private static final String FONT_NAME = "Impact"; // "Impact" or "Arial"

  private int currentVal;
  private int nextVal;
  private double angle;
  private boolean isAnimating;
  private final Timer animTimer = new Timer(15, null);

  protected FlipPair(int startValue) {
    super();
    this.currentVal = startValue;
    this.nextVal = startValue;
    animTimer.addActionListener(e -> {
      angle -= 15;
      if (angle <= 0) {
        angle = 0;
        isAnimating = false;
        currentVal = nextVal;
        animTimer.stop();
      }
      repaint();
    });
  }

  public void setValue(int newValue) {
    if (newValue != nextVal && !isAnimating) {
      nextVal = newValue;
      angle = 180;
      isAnimating = true;
      animTimer.start();
    }
  }

  @Override public Dimension getPreferredSize() {
    Dimension d = super.getPreferredSize();
    d.setSize(PNL_WIDTH, PNL_HEIGHT);
    return d;
  }

  @Override public boolean isOpaque() {
    return false;
  }

  @Override protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(
        RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setRenderingHint(
        RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    g2.setFont(new Font(FONT_NAME, Font.PLAIN, FONT_SIZE));
    AffineTransform at = AffineTransform.getScaleInstance(1d, 1.5);
    g2.setFont(g2.getFont().deriveFont(at));

    int cx = getWidth() / 2;
    int cy = getHeight() / 2;
    String curStr = String.format("%02d", currentVal);
    String nxtStr = String.format("%02d", nextVal);
    drawHalf(g2, nxtStr, cx, cy, true, CARD_UPPER_BG);
    drawHalf(g2, curStr, cx, cy, false, CARD_LOWER_BG);
    if (isAnimating) {
      double rad = Math.toRadians(angle);
      double scaleY = Math.abs(Math.cos(rad));
      g2.translate(0, cy);
      // g2.transform(AffineTransform.getScaleInstance(1d, scaleY));
      g2.scale(1d, scaleY);
      boolean isTop = angle > 90;
      Color bg = isTop ? CARD_LOWER_BG : CARD_UPPER_BG;
      drawHalf(g2, curStr, cx, 0, isTop, bg);
      int alpha = (int) ((1d - scaleY) * SHADOW_MAX_ALPHA);
      drawShadow(g2, cx, 0, isTop, alpha);
      drawEdgeLight(g2, cx, cy);
    }
    g2.dispose();
  }

  private void drawEdgeLight(Graphics2D g2, int cx, int cy) {
    int sx = cx - PNL_WIDTH / 2;
    int sy = cy - SLIT_HEIGHT / 2;
    g2.setColor(SLIT_COLOR);
    g2.fillRect(sx, sy, PNL_WIDTH, SLIT_HEIGHT);
    g2.setColor(EDGE_LIGHT);
    g2.drawLine(sx + 5, sy, sx + PNL_WIDTH - 5, sy);
  }

  private void drawHalf(Graphics g, String txt, int cx, int cy, boolean isTop, Color bg) {
    int x = cx - PNL_WIDTH / 2;
    int height = PNL_HEIGHT / 2 - SLIT_HEIGHT / 2;
    if (isTop) {
      g.setClip(x, cy - PNL_HEIGHT / 2, PNL_WIDTH, height);
    } else {
      g.setClip(x, cy + SLIT_HEIGHT / 2, PNL_WIDTH, height);
    }
    g.setColor(bg);
    g.fillRoundRect(x, cy - PNL_HEIGHT / 2, PNL_WIDTH, PNL_HEIGHT, 18, 18);
    g.setColor(TEXT_COLOR);
    FontMetrics fm = g.getFontMetrics();
    g.drawString(txt, cx - fm.stringWidth(txt) / 2, cy + fm.getAscent() / 2 - 12);
  }

  private void drawShadow(Graphics g, int cx, int cy, boolean isTop, int alpha) {
    g.setColor(new Color(0, 0, 0, Math.min(255, alpha)));
    int h = PNL_HEIGHT / 2 - SLIT_HEIGHT / 2;
    if (isTop) {
      g.fillRect(cx - PNL_WIDTH / 2, cy - PNL_HEIGHT / 2, PNL_WIDTH, h);
    } else {
      g.fillRect(cx - PNL_WIDTH / 2, cy + SLIT_HEIGHT / 2, PNL_WIDTH, h);
    }
  }

  public static JLabel createColonLabel() {
    JLabel colon = new JLabel(":");
    colon.setFont(colon.getFont().deriveFont(Font.BOLD, FONT_SIZE));
    colon.setForeground(TEXT_COLOR);
    return colon;
  }
}
