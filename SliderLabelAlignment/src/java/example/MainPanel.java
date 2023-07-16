// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Map;
import java.util.Objects;
import javax.swing.*;
import javax.swing.plaf.LayerUI;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout(5, 5));
    JSlider slider1 = makeSlider();
    Object labelTable1 = slider1.getLabelTable();
    if (labelTable1 instanceof Map) {
      ((Map<?, ?>) labelTable1).forEach((key, value) -> {
        if (key instanceof Integer && value instanceof JLabel) {
          ((JLabel) value).setText(getLabel(slider1, key));
        }
      });
    }
    slider1.setLabelTable(slider1.getLabelTable());

    JSlider slider2 = makeSlider();
    Object labelTable2 = slider2.getLabelTable();
    if (labelTable2 instanceof Map) {
      ((Map<?, ?>) labelTable2).forEach((key, value) -> {
        if (key instanceof Integer && value instanceof JLabel) {
          ((JLabel) value).setText(" ");
        }
      });
    }
    slider2.setLabelTable(slider2.getLabelTable());
    JLayer<JSlider> layer = new JLayer<>(slider2, new SliderLabelLayerUI());

    add(makeTitledPanel("Default", slider1), BorderLayout.NORTH);
    add(makeTitledPanel("JLayer", layer), BorderLayout.SOUTH);
    setBorder(BorderFactory.createEmptyBorder(25, 50, 25, 50));
    setPreferredSize(new Dimension(320, 240));
  }

  private static String getLabel(JSlider slider, Object key) {
    String txt = "";
    if (Objects.equals(key, slider.getMinimum())) {
      txt = "Short";
    } else if (Objects.equals(key, slider.getMaximum())) {
      txt = "Long";
    }
    return txt;
  }

  private JSlider makeSlider() {
    JSlider slider = new JSlider(0, 4);
    slider.setMajorTickSpacing(1);
    slider.setPaintLabels(true);
    slider.setPaintTicks(true);
    slider.setSnapToTicks(true);
    return slider;
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
      // UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
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

class SliderLabelLayerUI extends LayerUI<JSlider> {
  private final JLabel min = new JLabel("Short");
  private final JLabel max = new JLabel("Long");

  @Override public void paint(Graphics g, JComponent c) {
    super.paint(g, c);
    if (c instanceof JLayer) {
      JSlider s = (JSlider) ((JLayer<?>) c).getView();
      Graphics2D g2 = (Graphics2D) g.create();
      Dimension d = c.getSize();
      Dimension d2 = min.getPreferredSize();
      FontMetrics metrics = s.getFontMetrics(s.getFont());
      int yy = s.getUI().getBaseline(s, d.width, d.height) - metrics.getAscent();
      int xx = 2;
      int w2 = d2.width;
      int h2 = d2.height;
      SwingUtilities.paintComponent(g2, min, s, xx, yy, w2, h2);
      Dimension d3 = max.getPreferredSize();
      int w3 = d3.width;
      int h3 = d3.height;
      SwingUtilities.paintComponent(g2, max, s, d.width - w3 - xx, yy, w3, h3);
      g2.dispose();
    }
  }
}
