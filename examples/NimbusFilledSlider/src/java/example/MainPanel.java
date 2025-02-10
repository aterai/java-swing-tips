// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    UIDefaults d = new UIDefaults();
    d.put("Slider:SliderTrack[Enabled].backgroundPainter", new Painter<JSlider>() {
      @Override public void paint(Graphics2D g, JSlider c, int w, int h) {
        // if (c.isInverted() || c.getOrientation() == SwingConstants.VERTICAL) {
        //   super.paint(g, c, w, h);
        //   return;
        // }
        int arc = 10;
        int trackHeight = 8;
        int trackWidth = w - 2;
        int fillTop = 4;
        int fillLeft = 1;

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setStroke(new BasicStroke(1.5f));
        g.setColor(Color.GRAY);
        g.fillRoundRect(fillLeft, fillTop, trackWidth, trackHeight, arc, arc);

        int fillBottom = fillTop + trackHeight;
        Rectangle r = new Rectangle(fillLeft, fillTop, trackWidth, fillBottom - fillTop);
        int fillRight = getPositionForValue(c, r);
        g.setColor(Color.ORANGE);
        g.fillRect(fillLeft + 1, fillTop + 1, fillRight - fillLeft, fillBottom - fillTop);

        g.setColor(Color.WHITE);
        g.drawRoundRect(fillLeft, fillTop, trackWidth, trackHeight, arc, arc);
      }

      // @see javax/swing/plaf/basic/BasicSliderUI#xPositionForValue(int value)
      private int getPositionForValue(JSlider slider, Rectangle trackRect) {
        float value = slider.getValue();
        float min = slider.getMinimum();
        float max = slider.getMaximum();
        float pixelsPerValue = trackRect.width / (max - min);
        int trackLeft = trackRect.x;
        int trackRight = trackRect.x + trackRect.width - 1;
        int pos = trackLeft + Math.round(pixelsPerValue * (value - min));
        return Math.max(trackLeft, Math.min(trackRight, pos));
      }
    });

    JSlider slider = new JSlider();
    slider.putClientProperty("Nimbus.Overrides", d);

    Box box = Box.createVerticalBox();
    box.add(Box.createVerticalStrut(5));
    box.add(makeTitledPanel("Default", new JSlider()));
    box.add(Box.createVerticalStrut(5));
    box.add(makeTitledPanel("Nimbus JSlider.isFilled", slider));
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
