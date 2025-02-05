// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import com.sun.java.swing.plaf.windows.WindowsSliderUI;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.Map;
import javax.swing.*;
import javax.swing.plaf.basic.BasicSliderUI;

public final class MainPanel extends JPanel {
  public static final int MAXI = 80;
  public static final int MINI = 40;

  private MainPanel() {
    super(new GridLayout(2, 1, 5, 5));
    JSlider slider1 = initSlider(new JSlider(0, 100, 40));
    slider1.setBorder(BorderFactory.createTitledBorder("ChangeListener"));
    add(slider1);

    JSlider slider = new JSlider(0, 100, 40) {
      @Override public void updateUI() {
        super.updateUI();
        if (getUI() instanceof WindowsSliderUI) {
          setUI(new WindowsDragLimitedSliderUI(this));
        } else {
          setUI(new BasicDragLimitedSliderUI(this));
        }
      }
    };
    JSlider slider2 = initSlider(slider);
    slider2.setBorder(BorderFactory.createTitledBorder("TrackListener"));
    add(slider2);
    setPreferredSize(new Dimension(320, 240));
  }

  private static JSlider initSlider(JSlider slider) {
    // JSlider slider = new JSlider(0, 100, 40);
    slider.setMajorTickSpacing(10);
    slider.setPaintTicks(true);
    slider.setPaintLabels(true);
    Object labelTable = slider.getLabelTable();
    if (labelTable instanceof Map) {
      ((Map<?, ?>) labelTable).forEach((key, value) -> {
        if (value instanceof JLabel) {
          updateForeground((JLabel) value);
        }
      });
    }
    // Dictionary<?, ?> dictionary = slider.getLabelTable();
    // if (Objects.nonNull(dictionary)) {
    //   Collections.list(dictionary.elements()).stream()
    //       .filter(JLabel.class::isInstance)
    //       .map(JLabel.class::cast)
    //       .forEach(MainPanel::updateForeground);
    // }
    slider.getModel().addChangeListener(e -> {
      BoundedRangeModel m = (BoundedRangeModel) e.getSource();
      m.setValue(Math.max(MINI, Math.min(m.getValue(), MAXI)));
    });
    return slider;
  }

  private static void updateForeground(JLabel label) {
    int v = Integer.parseInt(label.getText());
    if (v > MAXI || v < MINI) {
      label.setForeground(Color.RED);
    }
  }

  private static class WindowsDragLimitedSliderUI extends WindowsSliderUI {
    protected WindowsDragLimitedSliderUI(JSlider slider) {
      super(slider);
    }

    @Override protected TrackListener createTrackListener(JSlider slider) {
      return new TrackListener() {
        @Override public void mouseDragged(MouseEvent e) {
          // case HORIZONTAL:
          int halfThumbWidth = thumbRect.width / 2;
          int thumbLeft = e.getX() - offset;
          int maxPos = xPositionForValue(MAXI) - halfThumbWidth;
          int minPos = xPositionForValue(MINI) - halfThumbWidth;
          if (thumbLeft > maxPos) {
            e.translatePoint(maxPos + offset - e.getX(), 0);
          } else if (thumbLeft < minPos) {
            e.translatePoint(minPos + offset - e.getX(), 0);
          }
          super.mouseDragged(e);
        }
      };
    }
  }

  private static class BasicDragLimitedSliderUI extends BasicSliderUI {
    protected BasicDragLimitedSliderUI(JSlider slider) {
      super(slider);
    }

    @Override protected TrackListener createTrackListener(JSlider slider) {
      return new TrackListener() {
        @Override public void mouseDragged(MouseEvent e) {
          // case HORIZONTAL:
          int halfThumbWidth = thumbRect.width / 2;
          int thumbLeft = e.getX() - offset;
          int maxPos = xPositionForValue(MAXI) - halfThumbWidth;
          int minPos = xPositionForValue(MINI) - halfThumbWidth;
          if (thumbLeft > maxPos) {
            e.translatePoint(maxPos + offset - e.getX(), 0);
          } else if (thumbLeft < minPos) {
            e.translatePoint(minPos + offset - e.getX(), 0);
          }
          super.mouseDragged(e);
        }
      };
    }
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
