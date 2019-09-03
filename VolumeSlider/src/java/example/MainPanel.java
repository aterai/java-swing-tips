// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;
import javax.swing.plaf.metal.MetalSliderUI;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout(5, 5));

    JSlider slider1 = new JSlider(0, 100, 0);
    slider1.setUI(new TriSliderUI());
    slider1.setMajorTickSpacing(10);
    slider1.setMinorTickSpacing(5);
    slider1.setPaintTicks(true);
    slider1.setPaintLabels(true);

    JSlider slider2 = new JSlider(0, 100, 0);
    slider2.setUI(new MetalSliderUI() {
      @Override protected void paintHorizontalLabel(Graphics g, int v, Component l) {
        // [JDK-5099681] Windows/Motif L&F: JSlider should use foreground color for ticks. - Java Bug System
        // https://bugs.openjdk.java.net/browse/JDK-5099681
        JLabel lbl = (JLabel) l;
        lbl.setForeground(Color.GREEN);
        super.paintHorizontalLabel(g, v, lbl);
      }
    });
    // slider2.setBackground(Color.BLACK);
    slider2.setForeground(Color.BLUE);
    slider2.setMajorTickSpacing(10);
    slider2.setMinorTickSpacing(5);
    slider2.setPaintTicks(true);
    slider2.setPaintLabels(true);

    Box box = Box.createVerticalBox();
    box.add(Box.createVerticalStrut(5));
    box.add(makeTitledPanel("TriangleSliderUI", slider1));
    box.add(Box.createVerticalStrut(5));
    box.add(makeTitledPanel("HorizontalLabelColor", slider2));
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
    // try {
    //   UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    // } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
    //   ex.printStackTrace();
    // }
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

class TriSliderUI extends MetalSliderUI {
  @Override public void paintThumb(Graphics g) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    // Rectangle thumb = thumbRect;
    g2.fillOval(thumbRect.x, thumbRect.y, thumbRect.width, thumbRect.height);
    g2.dispose();
  }

  @Override public void paintTrack(Graphics g) {
    int cy;
    int cw;
    // int pad;
    Rectangle trackBounds = trackRect;
    if (slider.getOrientation() == SwingConstants.HORIZONTAL) {
      Graphics2D g2 = (Graphics2D) g.create();
      // pad = trackBuffer;
      // cx = pad;
      cy = -2 + trackBounds.height / 2;
      cw = trackBounds.width;

      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2.translate(trackBounds.x, trackBounds.y + cy);

      // g2.setPaint(new GradientPaint(0, 0, getShadowColor(), cw, 0, getHighlightColor(), true));
      g2.setPaint(Color.GRAY);
      g2.fillRect(0, -cy, cw, cy * 2);

      int trackLeft = 0;
      int trackRight = trackRect.width - 1;
      // int trackBottom = (trackRect.height - 1) - getThumbOverhang();
      // int trackTop = trackBottom - (getTrackWidth() - 1);

      int middleOfThumb = thumbRect.x + thumbRect.width / 2;
      middleOfThumb -= trackRect.x; // To compensate for the g.translate()
      // fillTop = !slider.isEnabled() ? trackTop : trackTop + 1;
      // fillBottom = !slider.isEnabled() ? trackBottom - 1 : trackBottom - 2;

      // int fillTop = 0;
      // int fillBottom = 0;
      int fillLeft = 0;
      int fillRight = 0;
      if (drawInverted()) {
        fillLeft = middleOfThumb;
        fillRight = slider.isEnabled() ? trackRight - 2 : trackRight - 1;
      } else {
        fillLeft = slider.isEnabled() ? trackLeft + 1 : trackLeft;
        fillRight = middleOfThumb;
      }

      g2.setPaint(new GradientPaint(0, 0, new Color(0, 100, 100), cw, 0, new Color(0, 255, 100), true));
      g2.fillRect(0, -cy, fillRight - fillLeft, cy * 2);

      g2.setPaint(slider.getBackground());
      Polygon polygon = new Polygon();
      polygon.addPoint(0, cy);
      polygon.addPoint(0, -cy);
      polygon.addPoint(cw, -cy);
      g2.fillPolygon(polygon);
      polygon.reset();

      g2.setPaint(Color.WHITE);
      g2.drawLine(0, cy, cw - 1, cy);

      // g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
      // g2.translate(-trackBounds.x, -(trackBounds.y + cy));
      g2.dispose();
    } else {
      super.paintTrack(g);
    }
  }
}

// TEST:
// public JSlider makeSlider() {
//   JSlider slider = new JSlider(0, 100);
//   slider.setMajorTickSpacing(10);
//   slider.setMinorTickSpacing(5);
//   // slider.setPaintTicks(true);
//   slider.setPaintLabels(true);
//   Dictionary<?, ?> dictionary = slider.getLabelTable();
//   if (Objects.nonNull(dictionary)) {
//     Enumeration<?> elements = dictionary.elements();
//     while (elements.hasMoreElements()) {
//       JLabel label = (JLabel) elements.nextElement();
//       label.setIcon(new TickIcon());
//       label.setIconTextGap(0);
//       label.setVerticalAlignment(SwingConstants.TOP);
//       label.setVerticalTextPosition(SwingConstants.BOTTOM);
//       label.setHorizontalAlignment(SwingConstants.CENTER);
//       label.setHorizontalTextPosition(SwingConstants.CENTER);
//       label.setForeground(Color.RED);
//     }
//   }
// }
// class TickIcon implements Icon {
//   @Override public void paintIcon(Component c, Graphics g, int x, int y) {
//     g.setColor(Color.GREEN);
//     g.drawLine(x + 2, y - 1, x + 2, y + 1);
//     g.drawLine(x + 1, y + 0, x + 3, y + 0);
//     g.drawLine(x + 0, y + 1, x + 4, y + 1);
//   }
//   @Override public int getIconWidth() {
//     return 5;
//   }
//   @Override public int getIconHeight() {
//     return 3;
//   }
// }
