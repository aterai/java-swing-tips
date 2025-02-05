// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new GridLayout(2, 1));
    JSlider slider = new JSlider(0, 100, 50);
    JSpinner spinner = new JSpinner(new SpinnerNumberModel(500, 0, 1000, 10));

    slider.setMajorTickSpacing(10);
    slider.setMinorTickSpacing(1);
    slider.setPaintTicks(true);
    slider.setPaintLabels(true);
    slider.addChangeListener(e -> {
      JSlider source = (JSlider) e.getSource();
      spinner.setValue(source.getValue() * 10);
    });
    slider.addMouseWheelListener(e -> {
      JSlider source = (JSlider) e.getComponent();
      slider.setValue(source.getValue() - e.getWheelRotation());
      // int intValue = source.getValue() - e.getWheelRotation();
      // BoundedRangeModel model = source.getModel();
      // if (model.getMaximum() >= intValue && model.getMinimum() <= intValue) {
      //   slider.setValue(intValue);
      // }
    });

    spinner.addChangeListener(e -> {
      JSpinner source = (JSpinner) e.getSource();
      Integer newValue = (Integer) source.getValue();
      slider.setValue(newValue / 10);
    });
    spinner.addMouseWheelListener(e -> {
      JSpinner source = (JSpinner) e.getComponent();
      SpinnerNumberModel model = (SpinnerNumberModel) source.getModel();
      Integer oldValue = (Integer) source.getValue();
      Integer intValue = oldValue - e.getWheelRotation() * model.getStepSize().intValue();
      Integer max = (Integer) model.getMaximum(); // 1000
      Integer min = (Integer) model.getMinimum(); // 0
      if (min <= intValue && intValue <= max) {
        source.setValue(intValue);
      }
    });

    add(makeTitledPanel("MouseWheel+JSpinner", spinner));
    add(makeTitledPanel("MouseWheel+JSlider", slider));
    setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));
    setPreferredSize(new Dimension(320, 240));
  }

  private static Component makeTitledPanel(String title, Component cmp) {
    JPanel p = new JPanel(new GridBagLayout());
    p.setBorder(BorderFactory.createTitledBorder(title));
    GridBagConstraints c = new GridBagConstraints();
    c.weightx = 1d;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.insets = new Insets(5, 5, 5, 5);
    p.add(cmp, c);
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
