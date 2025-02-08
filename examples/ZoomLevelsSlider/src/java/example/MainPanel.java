// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import com.sun.java.swing.plaf.windows.WindowsSliderUI;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.Map;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.plaf.metal.MetalSliderUI;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JSlider slider0 = new JSlider(-100, 100, 0);
    initSlider(slider0);
    slider0.setBorder(BorderFactory.createTitledBorder("Default"));

    JSlider slider1 = new JSlider(-100, 100, 0) {
      @Override public void updateUI() {
        super.updateUI();
        if (getUI() instanceof WindowsSliderUI) {
          setUI(new WindowsZoomLevelsSliderUI(this));
        } else { // NullPointerException ???
          UIManager.put("Slider.trackWidth", 0); // Meaningless settings that are not used?
          UIManager.put("Slider.majorTickLength", 8); // BasicSliderUI#getTickLength(): 8
          Icon missingIcon = UIManager.getIcon("html.missingImage");
          UIManager.put("Slider.verticalThumbIcon", missingIcon);
          UIManager.put("Slider.horizontalThumbIcon", missingIcon);
          setUI(new MetalZoomLevelsSliderUI());
        }
      }
    };
    initSlider(slider1);
    String help1 = "Dragged: Snap to the center";
    String help2 = "Clicked: Double-click the thumb to reset its value";
    slider1.setBorder(BorderFactory.createTitledBorder("<html>" + help1 + "<br>" + help2));

    Box box = Box.createVerticalBox();
    box.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    box.add(slider0);
    box.add(Box.createVerticalStrut(20));
    box.add(slider1);
    add(box);
    setPreferredSize(new Dimension(320, 240));
  }

  private static void initSlider(JSlider slider) {
    slider.setMajorTickSpacing(20);
    slider.setPaintTicks(true);
    slider.setPaintLabels(true);
    Object labelTable = slider.getLabelTable();
    if (labelTable instanceof Map) {
      ((Map<?, ?>) labelTable).forEach((key, value) -> {
        if (key instanceof Integer && value instanceof JLabel) {
          ((JLabel) value).setText(getLabelText(slider, (Integer) key));
        }
      });
    }
    slider.setLabelTable(slider.getLabelTable()); // Update LabelTable
  }

  private static String getLabelText(JSlider slider, Integer iv) {
    String txt = " ";
    if (iv == 0) {
      txt = "100%";
    } else if (iv == slider.getMinimum()) {
      txt = "5%";
    } else if (iv == slider.getMaximum()) {
      txt = "800%";
    }
    return txt;
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

class WindowsZoomLevelsSliderUI extends WindowsSliderUI {
  protected WindowsZoomLevelsSliderUI(JSlider slider) {
    super(slider);
  }

  @Override protected TrackListener createTrackListener(JSlider slider) {
    return new WindowsTrackHandler();
  }

  private final class WindowsTrackHandler extends TrackListener {
    @Override public void mouseClicked(MouseEvent e) {
      boolean isLeftDoubleClick = SwingUtilities.isLeftMouseButton(e) && e.getClickCount() >= 2;
      if (isLeftDoubleClick && thumbRect.contains(e.getPoint())) {
        slider.setValue(0);
      } else {
        super.mouseClicked(e);
      }
    }

    @Override public void mouseDragged(MouseEvent e) {
      // case HORIZONTAL:
      int halfThumbWidth = thumbRect.width / 2;
      int trackLength = trackRect.width;
      int pos = e.getX() + halfThumbWidth;
      int possibleTickPos = slider.getMaximum() - slider.getMinimum();
      int tickSp = Math.max(slider.getMajorTickSpacing(), 10);
      int tickPixels = trackLength * tickSp / possibleTickPos;
      int tickPixels2 = tickPixels / 2;
      int trackCenter = (int) trackRect.getCenterX();
      if (trackCenter - tickPixels2 < pos && pos < trackCenter + tickPixels2) {
        e.translatePoint(trackCenter - halfThumbWidth - e.getX(), 0);
        offset = 0;
      }
      super.mouseDragged(e);
    }
  }
}

class MetalZoomLevelsSliderUI extends MetalSliderUI {
  protected MetalZoomLevelsSliderUI() {
    super();
  }

  @Override protected TrackListener createTrackListener(JSlider slider) {
    return new MetalTrackHandler();
  }

  private final class MetalTrackHandler extends TrackListener {
    @Override public void mouseClicked(MouseEvent e) {
      boolean isLeftDoubleClick = SwingUtilities.isLeftMouseButton(e) && e.getClickCount() >= 2;
      if (isLeftDoubleClick && thumbRect.contains(e.getPoint())) {
        slider.setValue(0);
      } else {
        super.mouseClicked(e);
      }
    }

    @Override public void mouseDragged(MouseEvent e) {
      // case HORIZONTAL:
      int halfThumbWidth = thumbRect.width / 2;
      int trackLength = trackRect.width;
      int pos = e.getX() + halfThumbWidth;
      int possibleTickPos = slider.getMaximum() - slider.getMinimum();
      int tickSp = Math.max(slider.getMajorTickSpacing(), 10);
      int tickPixels = trackLength * tickSp / possibleTickPos;
      int tickPixels2 = tickPixels / 2;
      int trackCenter = (int) trackRect.getCenterX();
      if (trackCenter - tickPixels2 < pos && pos < trackCenter + tickPixels2) {
        e.translatePoint(trackCenter - halfThumbWidth - e.getX(), 0);
        offset = 0;
      }
      super.mouseDragged(e);
    }
  }
}
