// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    UIDefaults d = new UIDefaults();
    d.put("Slider.thumbWidth", 24);
    d.put("Slider.thumbHeight", 24);
    Painter<JSlider> thumbPainter = (g, c, w, h) -> {
      g.setPaint(new Color(0x21_98_F6));
      g.fillOval(0, 0, w, h);
      NumberIcon icon = new NumberIcon(c.getValue());
      int xx = (w - icon.getIconWidth()) / 2;
      int yy = (h - icon.getIconHeight()) / 2;
      icon.paintIcon(c, g, xx, yy);
    };
    d.put("Slider:SliderThumb[Disabled].backgroundPainter", thumbPainter);
    d.put("Slider:SliderThumb[Enabled].backgroundPainter", thumbPainter);
    d.put("Slider:SliderThumb[Focused+MouseOver].backgroundPainter", thumbPainter);
    d.put("Slider:SliderThumb[Focused+Pressed].backgroundPainter", thumbPainter);
    d.put("Slider:SliderThumb[Focused].backgroundPainter", thumbPainter);
    d.put("Slider:SliderThumb[MouseOver].backgroundPainter", thumbPainter);
    d.put("Slider:SliderThumb[Pressed].backgroundPainter", thumbPainter);
    d.put("Slider:SliderTrack[Enabled].backgroundPainter", new SliderTrackPainter());

    JSlider slider = new JSlider();
    slider.setSnapToTicks(true);
    slider.setMajorTickSpacing(10);
    slider.addMouseMotionListener(new MouseAdapter() {
      @Override public void mouseDragged(MouseEvent e) {
        super.mouseDragged(e);
        e.getComponent().repaint();
      }
    });
    slider.putClientProperty("Nimbus.Overrides", d);

    Box box = Box.createVerticalBox();
    box.add(Box.createVerticalStrut(5));
    box.add(makeTitledPanel("Default", new JSlider()));
    box.add(Box.createVerticalStrut(5));
    box.add(makeTitledPanel("Paint major tick marks on the track", slider));
    box.add(Box.createVerticalGlue());
    add(box);
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    setPreferredSize(new Dimension(320, 240));
  }

  private static Component makeTitledPanel(String title, Component c) {
    JPanel p = new JPanel(new BorderLayout());
    p.setBorder(BorderFactory.createTitledBorder(title));
    p.add(c);
    return p;
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
      // UIManager.put("JSlider.isFilled", Boolean.TRUE);
      // UIManager.put("Slider.paintValue", Boolean.TRUE);
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

class SliderTrackPainter implements Painter<JSlider> {
  @Override public void paint(Graphics2D g, JSlider c, int w, int h) {
    int arc = 10;
    int thumbSize = 24;
    int trackHeight = 8;
    int trackWidth = w - thumbSize;
    int fillTop = (thumbSize - trackHeight) / 2;
    int fillLeft = thumbSize / 2;

    // Paint track
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g.setColor(new Color(0xC6_E4_FC));
    g.fillRoundRect(fillLeft, fillTop + 2, trackWidth, trackHeight - 4, arc, arc);

    int fillBottom = fillTop + trackHeight;
    Rectangle r = new Rectangle(fillLeft, fillTop, trackWidth, fillBottom - fillTop);

    // Paint the major tick marks on the track
    g.setColor(new Color(0x31_A8_F8));
    int value = c.getMinimum();
    int tickSize = 4;
    while (value <= c.getMaximum()) {
      int xpt = getPositionForValue(c, r, value);
      g.fillOval(xpt, (int) r.getCenterY() - tickSize / 2, tickSize, tickSize);
      // Overflow checking
      if (Integer.MAX_VALUE - c.getMajorTickSpacing() < value) {
        break;
      }
      value += c.getMajorTickSpacing();
    }

    // JSlider.isFilled
    int fillRight = getPositionForValue(c, r, c.getValue());
    g.setColor(new Color(0x21_98_F6));
    g.fillRoundRect(fillLeft, fillTop, fillRight - fillLeft, fillBottom - fillTop, arc, arc);
  }

  // @see javax/swing/plaf/basic/BasicSliderUI#xPositionForValue(int value)
  private int getPositionForValue(JSlider slider, Rectangle trackRect, float value) {
    float min = slider.getMinimum();
    float max = slider.getMaximum();
    float pixelsPerValue = trackRect.width / (max - min);
    int trackLeft = trackRect.x;
    int trackRight = trackRect.x + trackRect.width - 1;
    int pos = trackLeft + Math.round(pixelsPerValue * (value - min));
    return Math.max(trackLeft, Math.min(trackRight, pos));
  }
}

class NumberIcon implements Icon {
  private final int value;

  protected NumberIcon(int value) {
    this.value = value;
  }

  protected Shape getTextShape(Graphics2D g2) {
    // Java 12:
    // NumberFormat fmt = NumberFormat.getCompactNumberInstance(
    //    Locale.US, NumberFormat.Style.SHORT);
    // String txt = fmt.format(value);
    String txt = value > 999 ? "1K" : Integer.toString(value);
    AffineTransform at = txt.length() < 3 ? null : AffineTransform.getScaleInstance(.66, 1d);
    return new TextLayout(txt, g2.getFont(), g2.getFontRenderContext()).getOutline(at);
  }

  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.translate(x, y);

    Shape shape = getTextShape(g2);
    Rectangle2D b = shape.getBounds2D();
    double tx = getIconWidth() / 2d - b.getCenterX();
    double ty = getIconHeight() / 2d - b.getCenterY();
    AffineTransform toCenterAt = AffineTransform.getTranslateInstance(tx, ty);
    g2.setPaint(Color.WHITE);
    g2.fill(toCenterAt.createTransformedShape(shape));
    g2.dispose();
  }

  @Override public int getIconWidth() {
    return 20;
  }

  @Override public int getIconHeight() {
    return 20;
  }
}
