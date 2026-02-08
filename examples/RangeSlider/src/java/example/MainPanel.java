// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.Objects;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.basic.BasicSliderUI;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new GridBagLayout());
    add(new RangeSliderPanel(0, 100, 25, 75));
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

class TriangleUI extends BasicSliderUI {
  private final boolean isUpward;

  protected TriangleUI(JSlider b, boolean isUpward) {
    super(b);
    this.isUpward = isUpward;
  }

  @Override public void paintTrack(Graphics g) {
    // nothing to paint
  }

  @Override public void paintFocus(Graphics g) {
    // nothing to paint
  }

  @Override protected void calculateTrackBuffer() {
    if (slider.getOrientation() == JSlider.HORIZONTAL) {
      trackBuffer = RangeBar.PAD; // + thumbRect.width / 2;
    } else {
      super.calculateTrackBuffer();
    }
  }

  @Override public void paintThumb(Graphics g) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setColor(new Color(40, 44, 52));
    Rectangle r = SwingUtilities.calculateInnerArea(slider, null);
    int h = 8;
    // int x = thumbRect.x + (thumbRect.width - w) / 2;
    // int y = isUpward ? thumbRect.y : thumbRect.y + thumbRect.height - h;
    int x = thumbRect.x; // + (thumbRect.width - w) / 2;
    int y = isUpward ? r.y : r.y + r.height - h;
    int[] xps = {x, x + thumbRect.width / 2, x + thumbRect.width};
    int[] yps = isUpward ? new int[] {y + h, y, y + h} : new int[] {y, y + h, y};
    g2.fillPolygon(xps, yps, xps.length);
    g2.dispose();
  }
}

class RangeSliderPanel extends JPanel {
  private final JSlider lowerSlider;
  private final JSlider upperSlider;

  protected RangeSliderPanel(int min, int max, int lowInit, int highInit) {
    super(new BorderLayout(0, 0));
    upperSlider = createSlider(min, max, highInit, false);
    lowerSlider = createSlider(min, max, lowInit, true);

    ChangeListener cl = e -> {
      if (lowerSlider.getValue() > upperSlider.getValue()) {
        if (Objects.equals(e.getSource(), lowerSlider)) {
          lowerSlider.setValue(upperSlider.getValue());
        } else {
          upperSlider.setValue(lowerSlider.getValue());
        }
      }
      repaint();
    };
    lowerSlider.addChangeListener(cl);
    upperSlider.addChangeListener(cl);

    add(upperSlider, BorderLayout.NORTH);
    add(new RangeBar(lowerSlider, upperSlider));
    add(lowerSlider, BorderLayout.SOUTH);
  }

  @Override public final Component add(Component comp) {
    return super.add(comp);
  }

  @Override public final void add(Component comp, Object constraints) {
    super.add(comp, constraints);
  }

  private static JSlider createSlider(int min, int max, int val, boolean isUp) {
    return new JSlider(min, max, val) {
      @Override public void updateUI() {
        super.updateUI();
        setUI(new TriangleUI(this, isUp));
        setOpaque(false);
        setPaintTicks(false);
        setPaintLabels(false);
      }

      @Override public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        d.height = 10;
        return d;
      }
    };
  }
}

class RangeBar extends JLabel {
  public static final int BAR_HEIGHT = 24;
  public static final int PAD = 20;
  private static final Color MAJOR_TICK_COLOR = new Color(180, 180, 185);
  private static final Color MINOR_TICK_COLOR = new Color(210, 210, 215);
  private static final Color TRACK_BGC = new Color(230, 230, 235);
  private static final Color RANGE_COLOR = new Color(0, 180, 255, 120);
  private final JSlider low;
  private final JSlider up;
  private final Point dragStartPt = new Point(0, 0);
  private int slLow;
  private int slUp;
  private transient MouseAdapter dragListener;

  protected RangeBar(JSlider low, JSlider up) {
    super();
    this.low = low;
    this.up = up;
  }

  @Override public void updateUI() {
    removeMouseListener(dragListener);
    removeMouseMotionListener(dragListener);
    super.updateUI();
    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    dragListener = new MouseAdapter() {
      @Override public void mousePressed(MouseEvent e) {
        dragStartPt.setLocation(e.getPoint());
        slLow = low.getValue();
        slUp = up.getValue();
        repaint();
      }

      @Override public void mouseReleased(MouseEvent e) {
        dragStartPt.setLocation(-100, -100);
        repaint();
      }

      @Override public void mouseDragged(MouseEvent e) {
        if (dragStartPt.x >= 0) {
          updateRange(e.getX() - dragStartPt.x);
        }
        repaint();
      }
    };
    addMouseListener(dragListener);
    addMouseMotionListener(dragListener);
  }

  private void updateRange(int diff) {
    double trackW = low.getWidth() - PAD * 2d;
    int range = low.getMaximum() - low.getMinimum();
    int delta = (int) Math.round(diff * range / trackW);
    int ln = slLow + delta;
    int un = slUp + delta;
    if (ln >= low.getMinimum() && un <= low.getMaximum()) {
      low.setValue(ln);
      up.setValue(un);
    }
  }

  @Override public Dimension getPreferredSize() {
    return new Dimension(300, BAR_HEIGHT);
  }

  @Override protected void paintComponent(Graphics g) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    int w = getWidth() - PAD * 2 - 1;
    int cy = getHeight() / 2;
    int barH = BAR_HEIGHT - 1;
    // 1. paint Track
    paintTrack(g2, w, cy, barH);
    // 2. paint Ticks
    paintTicks(g2, w, cy, barH);
    int lx = getPositionX(low);
    int ux = getPositionX(up);
    // 3. Range bar
    paintRangeBar(g2, cy, barH, lx, ux);
    // 4. Numeric text
    paintNumber(g2, cy, lx, ux);
    g2.dispose();
  }

  private static void paintTrack(Graphics2D g2, int w, int cy, int barH) {
    g2.setColor(TRACK_BGC);
    Shape track = new RoundRectangle2D.Float(PAD, cy - barH / 2f, w, barH, 4f, 4f);
    g2.fill(track);
    g2.setColor(TRACK_BGC.darker());
    g2.draw(track);
  }

  private static void paintTicks(Graphics2D g2, int w, int cy, int barH) {
    // g2.setStroke(new BasicStroke(1f));
    for (int i = 0; i <= 100; i += 2) {
      int tx = PAD + (i * w / 100);
      if (i % 10 == 0) {
        // MajorTick
        g2.setColor(MAJOR_TICK_COLOR);
        g2.drawLine(tx, cy - barH / 2, tx, cy + barH / 2);
      } else {
        // MinorTick
        g2.setColor(MINOR_TICK_COLOR);
        g2.drawLine(tx, cy - 4, tx, cy + 4);
      }
    }
  }

  private static void paintRangeBar(Graphics2D g2, int cy, int barH, int lx, int ux) {
    g2.setPaint(RANGE_COLOR);
    Shape bar = new RoundRectangle2D.Float(lx, cy - barH / 2f, ux - lx, barH, 4f, 4f);
    g2.fill(bar);
    g2.setColor(RANGE_COLOR.darker());
    g2.draw(bar);
  }

  private void paintNumber(Graphics2D g2, int cy, int lx, int ux) {
    g2.setColor(UIManager.getColor("Button.foreground"));
    String txtLow = String.valueOf(low.getValue());
    String txtUp = String.valueOf(up.getValue());
    FontMetrics fm = g2.getFontMetrics();
    int gap = 2;
    int ty = cy + fm.getAscent() / 2 - 1;
    g2.drawString(txtLow, lx - fm.stringWidth(txtLow) - gap, ty);
    g2.drawString(txtUp, ux + gap, ty);
  }

  private static int getPositionX(JSlider slider) {
    int iv = slider.getValue() - slider.getMinimum();
    int range = slider.getMaximum() - slider.getMinimum();
    double v = (double) iv / range;
    Rectangle r = SwingUtilities.calculateInnerArea(slider, null);
    return PAD + (int) (v * (r.width - PAD * 2d));
  }
}
