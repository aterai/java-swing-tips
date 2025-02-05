// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Map;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout(5, 5));
    JSlider slider = makeSlider();
    // // slider.updateUI();
    // // @see JSlider#updateUI()
    // public void updateUI() {
    //   setUI((SliderUI) UIManager.getUI(this));
    //   // The labels preferred size may be derived from the font
    //   // of the slider, so we must update the UI of the slider first, then
    //   // that of labels.  This way when setSize is called the right
    //   // font is used.
    //   updateLabelUIs();
    // }
    //
    // // @see JSlider#updateLabelUIs()
    // protected void updateLabelUIs() {
    //   Dictionary<?, ?> labelTable = getLabelTable();
    //   if (labelTable == null) {
    //     return;
    //   }
    //   Enumeration<?> labels = labelTable.keys();
    //   while (labels.hasMoreElements()) {
    //     JComponent component = (JComponent) labelTable.get(labels.nextElement());
    //     component.updateUI();
    //     component.setSize(component.getPreferredSize());
    //   }
    // }
    // SwingUtilities.updateComponentTreeUI(slider);

    slider.setLabelTable(slider.getLabelTable());
    // @see JSlider#setLabelTable(...)
    // public void setLabelTable(Dictionary labels) {
    //   Dictionary oldTable = labelTable;
    //   labelTable = labels;
    //   updateLabelUIs();
    //   firePropertyChange("labelTable", oldTable, labelTable );
    //   if (labels != oldTable) {
    //     revalidate();
    //     repaint();
    //   }
    // }

    Box box = Box.createVerticalBox();
    box.add(Box.createVerticalStrut(5));
    box.add(makeTitledPanel("Default", makeSlider()));
    box.add(Box.createVerticalStrut(5));
    box.add(makeTitledPanel("JSlider#updateLabelUIs()", slider));
    box.add(Box.createVerticalGlue());
    add(box);
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    setPreferredSize(new Dimension(320, 240));
  }

  private JSlider makeSlider() {
    JSlider slider = new JSlider(0, 10_000);
    slider.putClientProperty("Slider.paintThumbArrowShape", Boolean.TRUE);
    slider.setMajorTickSpacing(2500);
    slider.setMinorTickSpacing(500);
    slider.setPaintLabels(true);
    slider.setPaintTicks(true);
    slider.setSnapToTicks(true);
    // slider.setBorder(BorderFactory.createLineBorder(Color.WHITE, 10));
    Object labelTable = slider.getLabelTable();
    if (labelTable instanceof Map) {
      ((Map<?, ?>) labelTable).forEach((key, value) -> {
        if (key instanceof Integer && value instanceof JLabel) {
          JLabel label = (JLabel) value;
          label.setText(Integer.toString((Integer) key / 100));
          // TEST: label.setHorizontalAlignment(SwingConstants.LEFT);
        }
      });
    }
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
