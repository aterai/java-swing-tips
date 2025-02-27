// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout(5, 5));
    JSlider slider1 = makeSlider();
    setCurrentLabelListener(slider1);

    // @SuppressWarnings("JdkObsolete")
    // Dictionary<Integer, Component> labelTable = new Hashtable<>();
    // List<Component> list = Stream.of("A", "B", "C", "D", "E")
    //     .map(JLabel::new).collect(Collectors.toList());
    // IntStream.range(0, list.size()).boxed()
    //     .forEach(i -> labelTable.put(i, list.get(i)));
    List<String> list2 = Arrays.asList("A", "B", "C", "D", "E");
    JSlider slider2 = new JSlider(0, list2.size() - 1, 0);
    setCurrentLabelListener(slider2);
    // slider2.setLabelTable(labelTable);
    slider2.setSnapToTicks(true);
    slider2.setPaintTicks(true);
    slider2.setPaintLabels(true);
    slider2.setMajorTickSpacing(1);
    Object labelTable = slider2.getLabelTable();
    if (labelTable instanceof Map) {
      ((Map<?, ?>) labelTable).forEach((key, value) -> {
        if (key instanceof Integer && value instanceof JLabel) {
          updateLabel(list2, slider2, (Integer) key, (JLabel) value);
        }
      });
    }
    slider2.setLabelTable(slider2.getLabelTable()); // updateLabelUIs()

    Box box = Box.createVerticalBox();
    box.add(Box.createVerticalStrut(5));
    box.add(makeTitledPanel("Default", makeSlider()));
    box.add(Box.createVerticalStrut(5));
    box.add(makeTitledPanel("setMajorTickSpacing(10)", slider1));
    box.add(Box.createVerticalStrut(5));
    box.add(makeTitledPanel("setMajorTickSpacing(0)", slider2));
    box.add(Box.createVerticalGlue());
    add(box);
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    setPreferredSize(new Dimension(320, 240));
    EventQueue.invokeLater(() -> {
      slider1.getModel().setValue(40);
      slider1.repaint();
    });
  }

  private static void updateLabel(List<String> list, JSlider slider, int i, JLabel l) {
    l.setText(list.get(i));
    if (slider.getValue() == i) {
      l.setForeground(Color.RED);
    }
  }

  private static JSlider makeSlider() {
    JSlider slider = new JSlider(0, 100);
    slider.setMajorTickSpacing(10);
    // slider.setMinorTickSpacing(5);
    slider.setPaintLabels(true);
    slider.setSnapToTicks(true);
    slider.setPaintTicks(true);
    return slider;
  }

  private static void setCurrentLabelListener(JSlider slider) {
    AtomicInteger prev = new AtomicInteger(-1);
    slider.getModel().addChangeListener(e -> {
      BoundedRangeModel m = (BoundedRangeModel) e.getSource();
      int i = m.getValue();
      int mts = slider.getMajorTickSpacing();
      if ((mts == 0 || i % mts == 0) && i != prev.get()) {
        Object labelTable = slider.getLabelTable();
        if (labelTable instanceof Map) {
          Map<?, ?> map = (Map<?, ?>) labelTable;
          resetForeground(map.get(i), Color.RED);
          resetForeground(map.get(prev.get()), Color.BLACK);
        }
        slider.repaint();
        prev.set(i);
      }
    });
  }

  private static void resetForeground(Object o, Color c) {
    if (o instanceof Component) {
      ((Component) o).setForeground(c);
    }
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
