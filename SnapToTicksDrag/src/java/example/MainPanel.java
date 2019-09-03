// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import com.sun.java.swing.plaf.windows.WindowsSliderUI;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.List;
import javax.swing.*;
import javax.swing.plaf.metal.MetalSliderUI;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());

    JSlider s = makeSlider("Custom SnapToTicks");
    initSlider(s);
    List<JSlider> list = Arrays.asList(makeSlider("Default SnapToTicks"), s);

    JCheckBox check = new JCheckBox("JSlider.setMinorTickSpacing(5)");
    check.addActionListener(e -> {
      int mts = ((JCheckBox) e.getSource()).isSelected() ? 5 : 0;
      list.forEach(slider -> slider.setMinorTickSpacing(mts));
    });

    Box box = Box.createVerticalBox();
    box.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    for (JSlider slider: list) {
      box.add(slider);
      box.add(Box.createVerticalStrut(10));
    }
    box.add(check);
    box.add(Box.createVerticalGlue());

    add(box);
    setPreferredSize(new Dimension(320, 240));
  }

  private static JSlider makeSlider(String title) {
    JSlider slider = new JSlider(0, 100, 50); // new JSlider(-50, 50, 0);
    slider.setBorder(BorderFactory.createTitledBorder(title));
    slider.setMajorTickSpacing(10);
    slider.setSnapToTicks(true);
    slider.setPaintTicks(true);
    slider.setPaintLabels(true);
    return slider;
  }

  private static void initSlider(JSlider slider) {
    slider.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0, false), "RIGHT_ARROW");
    slider.getActionMap().put("RIGHT_ARROW", new AbstractAction() {
      @Override public void actionPerformed(ActionEvent e) {
        JSlider s = (JSlider) e.getSource();
        s.setValue(s.getValue() + s.getMajorTickSpacing());
      }
    });
    slider.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0, false), "LEFT_ARROW");
    slider.getActionMap().put("LEFT_ARROW", new AbstractAction() {
      @Override public void actionPerformed(ActionEvent e) {
        JSlider s = (JSlider) e.getSource();
        s.setValue(s.getValue() - s.getMajorTickSpacing());
      }
    });
    slider.addMouseWheelListener(e -> {
      JSlider s = (JSlider) e.getComponent();
      boolean hasMinorTickSpacing = s.getMinorTickSpacing() > 0;
      int tickSpacing = hasMinorTickSpacing ? s.getMinorTickSpacing() : s.getMajorTickSpacing();
      s.setValue(s.getValue() - e.getWheelRotation() * tickSpacing);
      // int v = s.getValue() - e.getWheelRotation() * tickSpacing;
      // BoundedRangeModel m = s.getModel();
      // s.setValue(Math.min(m.getMaximum(), Math.max(v, m.getMinimum())));
    });
    if (slider.getUI() instanceof WindowsSliderUI) {
      slider.setUI(new WindowsSnapToTicksDragSliderUI(slider));
    } else {
      slider.setUI(new MetalSnapToTicksDragSliderUI());
    }
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
      ex.printStackTrace();
      Toolkit.getDefaultToolkit().beep();
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
    return new TrackListener() {
      @Override public void mouseDragged(MouseEvent e) {
        if (!slider.getSnapToTicks() || slider.getMajorTickSpacing() == 0) {
          super.mouseDragged(e);
          return;
        }
        // case HORIZONTAL:
        int halfThumbWidth = thumbRect.width / 2;
        int trackLength = trackRect.width;
        int trackLeft = trackRect.x - halfThumbWidth;
        int trackRight = trackRect.x + trackRect.width - 1 + halfThumbWidth;
        int xpos = e.getX();
        int snappedPos = xpos;
        if (xpos <= trackLeft) {
          snappedPos = trackLeft;
        } else if (xpos >= trackRight) {
          snappedPos = trackRight;
        } else {
          // int tickSpacing = slider.getMajorTickSpacing();
          // float actualPixelsForOneTick = trackLength * tickSpacing / (float) slider.getMaximum();

          // a problem if you choose to set a negative MINIMUM for the JSlider;
          // the calculated drag-positions are wrong.
          // Fixed by bobndrew:
          int possibleTickPositions = slider.getMaximum() - slider.getMinimum();
          boolean hasMinorTickSpacing = slider.getMinorTickSpacing() > 0;
          int tickSpacing = hasMinorTickSpacing ? slider.getMinorTickSpacing() : slider.getMajorTickSpacing();
          float actualPixelsForOneTick = trackLength * tickSpacing / (float) possibleTickPositions;
          xpos -= trackLeft;
          snappedPos = (int) (Math.round(xpos / actualPixelsForOneTick) * actualPixelsForOneTick + .5) + trackLeft;
          offset = 0;
          // System.out.println(snappedPos);
        }
        e.translatePoint(snappedPos - e.getX(), 0);
        super.mouseDragged(e);
        // MouseEvent me = new MouseEvent(
        //   e.getComponent(), e.getID(), e.getWhen(), e.getModifiers() | e.getModifiersEx(),
        //   snappedPos, e.getY(),
        //   e.getXOnScreen(), e.getYOnScreen(),
        //   e.getClickCount(), e.isPopupTrigger(), e.getButton());
        // e.consume();
        // super.mouseDragged(me);
      }
    };
  }
}

class MetalSnapToTicksDragSliderUI extends MetalSliderUI {
  @Override protected TrackListener createTrackListener(JSlider slider) {
    return new TrackListener() {
      @Override public void mouseDragged(MouseEvent e) {
        if (!slider.getSnapToTicks() || slider.getMajorTickSpacing() == 0) {
          super.mouseDragged(e);
          return;
        }
        // case HORIZONTAL:
        int halfThumbWidth = thumbRect.width / 2;
        int trackLength = trackRect.width;
        int trackLeft = trackRect.x - halfThumbWidth;
        int trackRight = trackRect.x + trackRect.width - 1 + halfThumbWidth;
        int xpos = e.getX();
        int snappedPos = xpos;
        if (xpos <= trackLeft) {
          snappedPos = trackLeft;
        } else if (xpos >= trackRight) {
          snappedPos = trackRight;
        } else {
          // int tickSpacing = slider.getMajorTickSpacing();
          // float actualPixelsForOneTick = trackLength * tickSpacing / (float) slider.getMaximum();

          // a problem if you choose to set a negative MINIMUM for the JSlider;
          // the calculated drag-positions are wrong.
          // Fixed by bobndrew:
          int possibleTickPositions = slider.getMaximum() - slider.getMinimum();
          boolean hasMinorTickSpacing = slider.getMinorTickSpacing() > 0;
          int tickSpacing = hasMinorTickSpacing ? slider.getMinorTickSpacing() : slider.getMajorTickSpacing();
          float actualPixelsForOneTick = trackLength * tickSpacing / (float) possibleTickPositions;
          xpos -= trackLeft;
          snappedPos = (int) (Math.round(xpos / actualPixelsForOneTick) * actualPixelsForOneTick + .5) + trackLeft;
          offset = 0;
          // System.out.println(snappedPos);
        }
        e.translatePoint(snappedPos - e.getX(), 0);
        super.mouseDragged(e);
        // MouseEvent me = new MouseEvent(
        //   e.getComponent(), e.getID(), e.getWhen(), e.getModifiers() | e.getModifiersEx(),
        //   snappedPos, e.getY(),
        //   e.getXOnScreen(), e.getYOnScreen(),
        //   e.getClickCount(), e.isPopupTrigger(), e.getButton());
        // super.mouseDragged(me);
      }
    };
  }
}
