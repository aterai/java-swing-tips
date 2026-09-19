// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Map;
import java.util.function.IntFunction;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.plaf.LayerUI;

public final class MainPanel extends JPanel {
  private static final String MIN_TEXT = "Short";
  private static final String MAX_TEXT = "Long";

  private MainPanel() {
    super(new BorderLayout(5, 5));
    JSlider slider1 = createSlider();
    setLabelText(slider1, value -> getLabelText(slider1, value));

    JSlider slider2 = createSlider();
    // hide all the label text but keep the label area
    setLabelText(slider2, value -> " ");
    JLayer<JSlider> layer = new JLayer<>(slider2, new SliderLabelLayerUI(MIN_TEXT, MAX_TEXT));

    add(createTitledPanel("Default", slider1), BorderLayout.NORTH);
    add(createTitledPanel("JLayer", layer), BorderLayout.SOUTH);
    setBorder(BorderFactory.createEmptyBorder(25, 50, 25, 50));
    setPreferredSize(new Dimension(320, 240));
  }

  private static String getLabelText(JSlider slider, int value) {
    String txt;
    if (value == slider.getMinimum()) {
      txt = MIN_TEXT;
    } else if (value == slider.getMaximum()) {
      txt = MAX_TEXT;
    } else {
      txt = "";
    }
    return txt;
  }

  private static void setLabelText(JSlider slider, IntFunction<String> mapper) {
    Object labelTable = slider.getLabelTable();
    if (labelTable instanceof Map) {
      ((Map<?, ?>) labelTable).forEach((key, value) -> {
        if (key instanceof Integer && value instanceof JLabel) {
          ((JLabel) value).setText(mapper.apply((Integer) key));
        }
      });
    }
    // fire a labelTable property change so that the UI recalculates the label size
    slider.setLabelTable(slider.getLabelTable());
  }

  private static JSlider createSlider() {
    JSlider slider = new JSlider(0, 4);
    slider.setMajorTickSpacing(1);
    slider.setPaintLabels(true);
    slider.setPaintTicks(true);
    slider.setSnapToTicks(true);
    return slider;
  }

  private static Component createTitledPanel(String title, Component c) {
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

class SliderLabelLayerUI extends LayerUI<JSlider> {
  private static final int PADDING = 2;
  private final JLabel minLabel = new JLabel();
  private final JLabel maxLabel = new JLabel();

  protected SliderLabelLayerUI(String minText, String maxText) {
    super();
    minLabel.setText(minText);
    maxLabel.setText(maxText);
  }

  @Override public void paint(Graphics g, JComponent c) {
    super.paint(g, c);
    if (c instanceof JLayer) {
      JSlider slider = (JSlider) ((JLayer<?>) c).getView();
      int width = slider.getWidth();
      int height = slider.getHeight();
      Font font = slider.getFont();
      minLabel.setFont(font);
      maxLabel.setFont(font);
      // align the text with the baseline of the slider labels
      int y = slider.getBaseline(width, height) - slider.getFontMetrics(font).getAscent();
      Dimension minSize = minLabel.getPreferredSize();
      SwingUtilities.paintComponent(
          g, minLabel, slider, PADDING, y, minSize.width, minSize.height);
      Dimension maxSize = maxLabel.getPreferredSize();
      int x = width - maxSize.width - PADDING;
      SwingUtilities.paintComponent(
          g, maxLabel, slider, x, y, maxSize.width, maxSize.height);
    }
  }
}
