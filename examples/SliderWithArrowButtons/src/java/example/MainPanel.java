// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Map;
import java.util.Objects;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new GridLayout());
    JSlider slider = new JSlider(-900, 900, 0);
    slider.setMajorTickSpacing(10);
    // slider.setPaintTicks(true);
    slider.setSnapToTicks(true);
    slider.setPaintLabels(true);
    updateSliderLabelTable(slider);

    JLabel label = new JLabel("100%", SwingConstants.CENTER) {
      @Override public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        d.width = Math.max(d.width, 32);
        return d;
      }
    };
    slider.addChangeListener(e -> {
      int iv = slider.getValue();
      int pct;
      if (iv >= 0) {
        pct = 100 + iv;
        slider.setMajorTickSpacing(1);
      } else {
        pct = 100 + iv / 10;
        slider.setMajorTickSpacing(10);
      }
      label.setText(pct + "%");
      label.repaint();
    });

    Box box = Box.createHorizontalBox();
    box.add(Box.createHorizontalGlue());
    box.add(makeButton(-5, slider));
    box.add(slider);
    box.add(makeButton(+5, slider));
    box.add(label);
    box.add(Box.createHorizontalGlue());

    add(box);
    setPreferredSize(new Dimension(320, 240));
  }

  private static JButton makeButton(int stepSize, JSlider slider) {
    String title = stepSize > 0 ? "+" : "-";
    JButton button = new JButton(title) {
      @Override public Dimension getPreferredSize() {
        return new Dimension(24, 24);
      }
    };
    button.setMargin(new Insets(4, 4, 4, 4));
    AutoRepeatHandler handler = new AutoRepeatHandler(stepSize, slider);
    button.addActionListener(handler);
    button.addMouseListener(handler);
    return button;
  }

  private static void updateSliderLabelTable(JSlider slider) {
    Object labelTable = slider.getLabelTable();
    if (labelTable instanceof Map) {
      ((Map<?, ?>) labelTable).forEach((key, value) -> {
        if (key instanceof Integer && value instanceof JLabel) {
          int iv = (Integer) key;
          String txt = " ";
          if (iv == 0) {
            txt = "100%";
          } else if (iv == slider.getMinimum()) {
            txt = "10%";
          } else if (iv == slider.getMaximum()) {
            txt = "1000%";
          }
          ((JLabel) value).setText(txt);
        }
      });
    }
    slider.setLabelTable(slider.getLabelTable()); // Update LabelTable
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

class AutoRepeatHandler extends MouseAdapter implements ActionListener {
  private final Timer autoRepeatTimer;
  private final int stepSize;
  private final JSlider slider;
  private JButton arrowButton;

  protected AutoRepeatHandler(int stepSize, JSlider slider) {
    super();
    this.stepSize = stepSize;
    this.slider = slider;
    autoRepeatTimer = new Timer(60, this);
    autoRepeatTimer.setInitialDelay(300);
  }

  @Override public void actionPerformed(ActionEvent e) {
    Object o = e.getSource();
    if (o instanceof Timer) {
      boolean isPressed = Objects.nonNull(arrowButton) && !arrowButton.getModel().isPressed();
      if (isPressed && autoRepeatTimer.isRunning()) {
        autoRepeatTimer.stop();
        // arrowButton = null;
      }
    } else if (o instanceof JButton) {
      arrowButton = (JButton) o;
    }
    int iv = slider.getValue();
    int step;
    if (iv == 0) {
      step = stepSize > 0 ? stepSize * 2 : stepSize * 10;
    } else if (iv > 0) {
      step = stepSize * 2;
    } else {
      step = stepSize * 10;
    }
    slider.setValue(iv + step);
  }

  @Override public void mousePressed(MouseEvent e) {
    if (SwingUtilities.isLeftMouseButton(e) && e.getComponent().isEnabled()) {
      autoRepeatTimer.start();
    }
  }

  @Override public void mouseReleased(MouseEvent e) {
    autoRepeatTimer.stop();
    // arrowButton = null;
  }

  @Override public void mouseExited(MouseEvent e) {
    if (autoRepeatTimer.isRunning()) {
      autoRepeatTimer.stop();
    }
  }
}
