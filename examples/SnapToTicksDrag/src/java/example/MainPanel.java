// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import com.sun.java.swing.plaf.windows.WindowsSliderUI;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelListener;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.plaf.metal.MetalSliderUI;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JSlider slider0 = new JSlider(0, 100, 50);
    slider0.setBorder(BorderFactory.createTitledBorder("Default SnapToTicks"));

    JSlider slider1 = new JSlider(0, 100, 50) {
      private transient MouseWheelListener handler;

      @Override public void updateUI() {
        removeMouseWheelListener(handler);
        super.updateUI();
        if (getUI() instanceof WindowsSliderUI) {
          setUI(new WindowsSnapToTicksDragSliderUI(this));
        } else {
          setUI(new MetalSnapToTicksDragSliderUI());
        }
        handler = e -> {
          JSlider s = (JSlider) e.getComponent();
          boolean hasMinorTick = s.getMinorTickSpacing() > 0;
          int tickSpacing = hasMinorTick ? s.getMinorTickSpacing() : s.getMajorTickSpacing();
          s.setValue(s.getValue() - e.getWheelRotation() * tickSpacing);
        };
        addMouseWheelListener(handler);
      }
    };
    slider1.setBorder(BorderFactory.createTitledBorder("Custom SnapToTicks"));
    slider1.getInputMap().put(KeyStroke.getKeyStroke("RIGHT"), "RIGHT_ARROW");
    slider1.getActionMap().put("RIGHT_ARROW", new AbstractAction() {
      @Override public void actionPerformed(ActionEvent e) {
        JSlider s = (JSlider) e.getSource();
        s.setValue(s.getValue() + s.getMajorTickSpacing());
      }
    });
    slider1.getInputMap().put(KeyStroke.getKeyStroke("LEFT"), "LEFT_ARROW");
    slider1.getActionMap().put("LEFT_ARROW", new AbstractAction() {
      @Override public void actionPerformed(ActionEvent e) {
        JSlider s = (JSlider) e.getSource();
        s.setValue(s.getValue() - s.getMajorTickSpacing());
      }
    });

    List<JSlider> list = Arrays.asList(initSlider(slider0), initSlider(slider1));
    JCheckBox check = new JCheckBox("JSlider.setMinorTickSpacing(5)");
    check.addActionListener(e -> {
      int mts = ((JCheckBox) e.getSource()).isSelected() ? 5 : 0;
      list.forEach(s -> s.setMinorTickSpacing(mts));
    });

    Box box = Box.createVerticalBox();
    box.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    for (JSlider s : list) {
      box.add(s);
      box.add(Box.createVerticalStrut(10));
    }
    box.add(check);
    box.add(Box.createVerticalGlue());

    add(box);
    setPreferredSize(new Dimension(320, 240));
  }

  private static JSlider initSlider(JSlider slider) {
    slider.setMajorTickSpacing(10);
    slider.setSnapToTicks(true);
    slider.setPaintTicks(true);
    slider.setPaintLabels(true);
    return slider;
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

class WindowsSnapToTicksDragSliderUI extends WindowsSliderUI {
  protected WindowsSnapToTicksDragSliderUI(JSlider slider) {
    super(slider);
  }

  @Override protected TrackListener createTrackListener(JSlider slider) {
    return new SnapTrackListener(slider);
  }

  private static float getTickPixels(JSlider slider, int trackLength) {
    // int tickSp = slider.getMajorTickSpacing();
    // float tickPixels = trackLength * tickSp / (float) slider.getMaximum();

    // a problem if you choose to set a negative MINIMUM for the JSlider;
    // the calculated drag-positions are wrong.
    // Fixed by bobndrew:
    int possibleTickPos = slider.getMaximum() - slider.getMinimum();
    boolean isMinorTickSp = slider.getMinorTickSpacing() > 0;
    int tickSp = isMinorTickSp ? slider.getMinorTickSpacing() : slider.getMajorTickSpacing();
    return trackLength * tickSp / (float) possibleTickPos;
  }

  private final class SnapTrackListener extends TrackListener {
    private final JSlider slider;

    private SnapTrackListener(JSlider slider) {
      super();
      this.slider = slider;
    }

    @Override public void mouseDragged(MouseEvent e) {
      if (slider.getSnapToTicks() && slider.getMajorTickSpacing() > 0) {
        boolean horizontal = slider.getOrientation() == SwingConstants.HORIZONTAL;
        int sx = horizontal ? getHorizontalSnapped(e.getX()) - e.getX() : 0;
        int sy = 0; // horizontal ? 0 : getVerticalSnapped(e.getY()) - e.getY();
        e.translatePoint(sx, sy);
      }
      super.mouseDragged(e);
    }

    private int getHorizontalSnapped(int mouseXpt) {
      int halfThumbWidth = thumbRect.width / 2;
      int trackLength = trackRect.width;
      int trackLeft = trackRect.x - halfThumbWidth;
      int trackRight = trackRect.x + trackRect.width - 1 + halfThumbWidth;
      int pos = mouseXpt;
      int snappedPos;
      if (pos <= trackLeft) {
        snappedPos = trackLeft;
      } else if (pos >= trackRight) {
        snappedPos = trackRight;
      } else {
        float tickPixels = getTickPixels(slider, trackLength);
        pos -= trackLeft;
        snappedPos = Math.round(Math.round(pos / tickPixels) * tickPixels) + trackLeft;
        offset = 0;
      }
      return snappedPos;
    }
  }
}

class MetalSnapToTicksDragSliderUI extends MetalSliderUI {
  @Override protected TrackListener createTrackListener(JSlider slider) {
    return new SnapTrackListener(slider);
  }

  private static float getTickPixels(JSlider slider, int trackLength) {
    int possibleTickPos = slider.getMaximum() - slider.getMinimum();
    boolean isMinorTickSp = slider.getMinorTickSpacing() > 0;
    int tickSp = isMinorTickSp ? slider.getMinorTickSpacing() : slider.getMajorTickSpacing();
    return trackLength * tickSp / (float) possibleTickPos;
  }

  private final class SnapTrackListener extends TrackListener {
    private final JSlider slider;

    private SnapTrackListener(JSlider slider) {
      super();
      this.slider = slider;
    }

    @Override public void mouseDragged(MouseEvent e) {
      if (slider.getSnapToTicks() && slider.getMajorTickSpacing() > 0) {
        boolean horizontal = slider.getOrientation() == SwingConstants.HORIZONTAL;
        int sx = horizontal ? getHorizontalSnapped(e.getX()) - e.getX() : 0;
        int sy = 0;
        e.translatePoint(sx, sy);
      }
      super.mouseDragged(e);
    }

    private int getHorizontalSnapped(int mouseXpt) {
      int halfThumbWidth = thumbRect.width / 2;
      int trackLength = trackRect.width;
      int trackLeft = trackRect.x - halfThumbWidth;
      int trackRight = trackRect.x + trackRect.width - 1 + halfThumbWidth;
      int pos = mouseXpt;
      int snappedPos;
      if (pos <= trackLeft) {
        snappedPos = trackLeft;
      } else if (pos >= trackRight) {
        snappedPos = trackRight;
      } else {
        float tickPixels = getTickPixels(slider, trackLength);
        pos -= trackLeft;
        snappedPos = Math.round(Math.round(pos / tickPixels) * tickPixels) + trackLeft;
        offset = 0;
      }
      return snappedPos;
    }
  }
}
