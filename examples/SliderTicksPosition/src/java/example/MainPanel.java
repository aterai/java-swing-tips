// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import javax.swing.*;
import javax.swing.plaf.LayerUI;
import javax.swing.plaf.basic.BasicSliderUI;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JSlider slider1 = new JSlider(SwingConstants.VERTICAL);
    initSliderTicks(slider1);

    JSlider slider2 = new JSlider(SwingConstants.VERTICAL);
    slider2.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
    initSliderTicks(slider2);

    JSlider slider3 = new JSlider(SwingConstants.HORIZONTAL);
    initSliderTicks(slider3);

    JSlider slider4 = new JSlider(SwingConstants.HORIZONTAL);
    initSliderTicks(slider4);

    add(slider1, BorderLayout.WEST);
    add(slider2, BorderLayout.EAST);
    add(slider3, BorderLayout.NORTH);
    add(new JLayer<>(slider4, new VerticalFlipLayerUI()), BorderLayout.SOUTH);

    JSlider slider5 = new JSlider(SwingConstants.HORIZONTAL) {
      @Override public void updateUI() {
        super.updateUI();
        setUI(new BasicSliderUI(this));
      }
    };
    initSliderTicks(slider5);
    slider5.setPaintLabels(true);

    JSlider slider6 = new JSlider(SwingConstants.HORIZONTAL) {
      @Override public void updateUI() {
        super.updateUI();
        setUI(new UpArrowThumbSliderUI(this));
      }
    };
    initSliderTicks(slider6);
    slider6.setPaintLabels(true);

    JPanel p = new JPanel(new BorderLayout());
    p.add(slider5, BorderLayout.NORTH);
    p.add(slider6, BorderLayout.SOUTH);
    add(p);

    setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
    setPreferredSize(new Dimension(320, 240));
  }

  private void initSliderTicks(JSlider slider) {
    slider.setMajorTickSpacing(20);
    slider.setMinorTickSpacing(10);
    slider.setPaintTicks(true);
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

class VerticalFlipLayerUI extends LayerUI<JComponent> {
  @Override public void paint(Graphics g, JComponent c) {
    if (c instanceof JLayer) {
      Graphics2D g2 = (Graphics2D) g.create();
      g2.setTransform(getAffineTransform(c.getSize()));
      super.paint(g2, c);
      g2.dispose();
    } else {
      super.paint(g, c);
    }
  }

  @Override public void installUI(JComponent c) {
    super.installUI(c);
    if (c instanceof JLayer) {
      JLayer<?> l = (JLayer<?>) c;
      l.setLayerEventMask(AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK);
      // | AWTEvent.MOUSE_WHEEL_EVENT_MASK);
    }
  }

  @Override public void uninstallUI(JComponent c) {
    if (c instanceof JLayer) {
      ((JLayer<?>) c).setLayerEventMask(0);
    }
    super.uninstallUI(c);
  }

  @Override public void eventDispatched(AWTEvent e, JLayer<? extends JComponent> l) {
    if (e instanceof MouseEvent) {
      MouseEvent me = (MouseEvent) e;
      Point2D pt = me.getPoint();
      try {
        pt = getAffineTransform(l.getSize()).inverseTransform(pt, null);
      } catch (NoninvertibleTransformException ex) {
        ex.printStackTrace();
        UIManager.getLookAndFeel().provideErrorFeedback(me.getComponent());
      }
      // Horizontal: me.translatePoint((int) pt.getX() - me.getX(), 0);
      me.translatePoint(0, (int) pt.getY() - me.getY());
      me.getComponent().repaint();
      // or: l.getView().repaint();
    }
    super.eventDispatched(e, l);
  }

  private AffineTransform getAffineTransform(Dimension d) {
    AffineTransform at = AffineTransform.getTranslateInstance(0d, d.height);
    at.scale(1d, -1d);
    return at;
  }
}

class UpArrowThumbSliderUI extends BasicSliderUI {
  protected UpArrowThumbSliderUI(JSlider slider) {
    super(slider);
  }

  @Override protected void calculateTrackRect() {
    if (slider.getOrientation() == SwingConstants.HORIZONTAL) {
      int centerSpacing = thumbRect.height;
      if (slider.getPaintTicks()) {
        centerSpacing -= getTickLength();
      }
      if (slider.getPaintLabels()) {
        centerSpacing -= getHeightOfTallestLabel();
      }
      trackRect.x = contentRect.x + trackBuffer;
      trackRect.y = contentRect.y + (contentRect.height + centerSpacing + 1) / 2;
      trackRect.width = contentRect.width - (trackBuffer * 2);
      trackRect.height = thumbRect.height;
    } else {
      super.calculateTrackRect();
    }
  }

  @Override protected void calculateTickRect() {
    if (slider.getOrientation() == SwingConstants.HORIZONTAL) {
      tickRect.x = trackRect.x;
      // tickRect.y = trackRect.y + trackRect.height;
      tickRect.y = trackRect.y;
      tickRect.width = trackRect.width;
      tickRect.height = slider.getPaintTicks() ? getTickLength() : 0;
    } else {
      super.calculateTickRect();
    }
  }

  @Override protected void calculateLabelRect() {
    if (slider.getPaintLabels()) {
      if (slider.getOrientation() == SwingConstants.HORIZONTAL) {
        labelRect.width = tickRect.width + (trackBuffer * 2);
        labelRect.height = getHeightOfTallestLabel();
        labelRect.x = tickRect.x - trackBuffer;
        labelRect.y = tickRect.y - labelRect.height;
      } else {
        super.calculateLabelRect();
      }
    } else {
      if (slider.getOrientation() == SwingConstants.HORIZONTAL) {
        labelRect.x = tickRect.x;
        labelRect.y = tickRect.y; // + tickRect.height;
        labelRect.width = tickRect.width;
        labelRect.height = 0;
      } else {
        super.calculateLabelRect();
      }
    }
  }

  @Override public void paintThumb(Graphics g) {
    if (slider.getOrientation() == SwingConstants.HORIZONTAL) {
      Graphics2D g2 = (Graphics2D) g.create();
      g2.translate(0, contentRect.y + contentRect.height + thumbRect.height);
      g2.scale(1d, -1d);
      super.paintThumb(g2);
      g2.dispose();
    } else {
      super.paintThumb(g);
    }
  }

  // @Override public void paintThumb(Graphics g) {
  //   Boolean arrowShape = (Boolean) slider.getClientProperty("Slider.paintThumbArrowShape");
  //   boolean isArrow = slider.getPaintTicks() && !Boolean.FALSE.equals(arrowShape);
  //   if (isArrow && slider.getOrientation() == SwingConstants.HORIZONTAL) {
  //     Color bc = slider.getBackground();
  //     g.setColor(slider.isEnabled() ? bc : bc.darker());
  //
  //     Rectangle knobBounds = thumbRect;
  //     Graphics2D g2 = (Graphics2D) g.create();
  //     g2.translate(knobBounds.x, knobBounds.y + knobBounds.height);
  //     g2.scale(1d, -1d);
  //
  //     int w = knobBounds.width;
  //     int h = knobBounds.height;
  //     int cw = w / 2;
  //     g2.fillRect(1, 1, w - 3, h - 1 - cw);
  //     Polygon p = new Polygon();
  //     p.addPoint(1, h - cw);
  //     p.addPoint(cw - 1, h - 1);
  //     p.addPoint(w - 2, h - 1 - cw);
  //     g2.fillPolygon(p);
  //
  //     g2.setColor(Color.WHITE);
  //     g2.drawLine(0, 0, w - 2, 0);
  //     g2.drawLine(0, 1, 0, h - 1 - cw);
  //     g2.drawLine(0, h - cw, cw - 1, h - 1);
  //
  //     g2.setColor(Color.BLACK);
  //     g2.drawLine(w - 1, 0, w - 1, h - 2 - cw);
  //     g2.drawLine(w - 1, h - 1 - cw, w - 1 - cw, h - 1);
  //
  //     g2.setColor(Color.GRAY);
  //     g2.drawLine(w - 2, 1, w - 2, h - 2 - cw);
  //     g2.drawLine(w - 2, h - 1 - cw, w - 1 - cw, h - 2);
  //
  //     g2.dispose();
  //   } else {
  //     super.paintThumb(g);
  //   }
  // }
}
