// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;
import javax.swing.plaf.LayerUI;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super();
    UIDefaults def = UIManager.getLookAndFeelDefaults();
    def.put("Slider.thumbWidth", 40);
    def.put("Slider.thumbHeight", 40);
    JSlider slider0 = makeToggleSlider(def);

    UIDefaults d = makeSliderPainter();
    JSlider slider1 = makeToggleSlider(d);
    slider1.addMouseMotionListener(new MouseAdapter() {
      @Override public void mouseDragged(MouseEvent e) {
        super.mouseDragged(e);
        e.getComponent().repaint();
      }
    });

    add(makeTitledPanel("Default", makeToggleSlider(null)));
    add(makeTitledPanel("Thumb size", slider0));
    add(makeTitledPanel("SliderTrack", slider1));

    JSlider slider2 = makeToggleSlider(d);
    add(makeTitledPanel("JSlider + JLayer", new JLayer<>(slider2, new ToggleSwitchLayerUI())));

    setPreferredSize(new Dimension(320, 240));
  }

  private static JSlider makeToggleSlider(UIDefaults d) {
    JSlider slider = new JSlider(0, 1, 0) {
      @Override public Dimension getPreferredSize() {
        return new Dimension(100, 40);
      }
    };
    slider.setFont(slider.getFont().deriveFont(Font.BOLD, 32f));
    if (d != null) {
      slider.putClientProperty("Nimbus.Overrides", d);
    }
    return slider;
  }

  private static UIDefaults makeSliderPainter() {
    UIDefaults d = new UIDefaults();
    d.put("Slider.thumbWidth", 40);
    d.put("Slider.thumbHeight", 40);
    d.put("Slider:SliderTrack[Enabled].backgroundPainter", (Painter<JSlider>) (g, c, w, h) -> {
      int arc = 40;
      int fillLeft = 2;
      int fillTop = 2;
      int trackWidth = w - fillLeft - fillLeft;
      int trackHeight = h - fillTop - fillTop;
      int baseline = trackHeight - fillTop - fillTop; // c.getBaseline(w, h);
      String off = "Off";

      g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g.setColor(Color.GRAY);
      g.fillRoundRect(fillLeft, fillTop, trackWidth, trackHeight, arc, arc);
      g.setPaint(Color.WHITE);
      g.drawString(off, w - g.getFontMetrics().stringWidth(off) - fillLeft * 5, baseline);

      Rectangle r = new Rectangle(fillLeft, fillTop, trackWidth, trackHeight);
      int fillRight = getPositionForValue(c, r);
      g.setColor(Color.ORANGE);
      g.fillRoundRect(fillLeft + 1, fillTop, fillRight - fillLeft, trackHeight, arc, arc);

      g.setPaint(Color.WHITE);
      if (fillRight - fillLeft > 0) {
        g.drawString("On", fillLeft * 5, baseline);
      }
      g.setStroke(new BasicStroke(2.5f));
      g.drawRoundRect(fillLeft, fillTop, trackWidth, trackHeight, arc, arc);
    });

    Painter<JSlider> thumbPainter = (g, c, w, h) -> {
      int fillLeft = 8;
      int fillTop = 8;
      int trackWidth = w - fillLeft - fillLeft;
      int trackHeight = h - fillTop - fillTop;
      g.setPaint(Color.WHITE);
      g.fillOval(fillLeft, fillTop, trackWidth, trackHeight);
    };
    d.put("Slider:SliderThumb[Disabled].backgroundPainter", thumbPainter);
    d.put("Slider:SliderThumb[Enabled].backgroundPainter", thumbPainter);
    d.put("Slider:SliderThumb[Focused+MouseOver].backgroundPainter", thumbPainter);
    d.put("Slider:SliderThumb[Focused+Pressed].backgroundPainter", thumbPainter);
    d.put("Slider:SliderThumb[Focused].backgroundPainter", thumbPainter);
    d.put("Slider:SliderThumb[MouseOver].backgroundPainter", thumbPainter);
    d.put("Slider:SliderThumb[Pressed].backgroundPainter", thumbPainter);
    return d;
  }

  // @see javax/swing/plaf/basic/BasicSliderUI#xPositionForValue(int value)
  private static int getPositionForValue(JSlider slider, Rectangle trackRect) {
    int value = slider.getValue();
    int min = slider.getMinimum();
    int max = slider.getMaximum();
    int trackLength = trackRect.width;
    int valueRange = max - min;
    float pixelsPerValue = trackLength / (float) valueRange;
    int trackLeft = trackRect.x;
    int trackRight = trackRect.x + trackRect.width - 1;

    int xp = trackLeft;
    xp += Math.round(pixelsPerValue * ((float) value - min));
    xp = Math.max(trackLeft, xp);
    xp = Math.min(trackRight, xp);
    return xp;
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

class ToggleSwitchLayerUI extends LayerUI<JSlider> {
  @Override public void installUI(JComponent c) {
    super.installUI(c);
    if (c instanceof JLayer) {
      JLayer<?> l = (JLayer<?>) c;
      l.setLayerEventMask(AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK);
    }
  }

  @Override public void uninstallUI(JComponent c) {
    if (c instanceof JLayer) {
      ((JLayer<?>) c).setLayerEventMask(0);
    }
    super.uninstallUI(c);
  }

  @Override protected void processMouseEvent(MouseEvent e, JLayer<? extends JSlider> l) {
    if (e.getID() == MouseEvent.MOUSE_PRESSED && SwingUtilities.isLeftMouseButton(e)) {
      e.getComponent().dispatchEvent(new MouseEvent(
          e.getComponent(),
          e.getID(), e.getWhen(),
          InputEvent.BUTTON3_DOWN_MASK, // e.getModifiers(),
          e.getX(), e.getY(),
          e.getXOnScreen(), e.getYOnScreen(),
          e.getClickCount(),
          e.isPopupTrigger(),
          MouseEvent.BUTTON3)); // e.getButton());
      e.consume();
    } else if (e.getID() == MouseEvent.MOUSE_CLICKED && SwingUtilities.isLeftMouseButton(e)) {
      JSlider slider = l.getView();
      int v = slider.getValue();
      if (slider.getMinimum() == v) {
        slider.setValue(slider.getMaximum());
      } else if (slider.getMaximum() == v) {
        slider.setValue(slider.getMinimum());
      }
    }
  }

  @Override protected void processMouseMotionEvent(MouseEvent e, JLayer<? extends JSlider> l) {
    l.getView().repaint();
  }
}
