// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.Map;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.plaf.basic.BasicSliderUI;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JSlider slider0 = new JSlider(-100, 100, 0);
    initSlider(slider0);
    slider0.setBorder(BorderFactory.createTitledBorder("Default"));

    JSlider slider1 = new ZoomLevelsSlider(-100, 100, 0);
    initSlider(slider1);
    String help1 = "Dragged: Snap to the center";
    String help2 = "Double-clicked: Reset to the initial value";
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

// Snap the thumb to the initial value while dragging
// and reset the value with a double click
class ZoomLevelsSlider extends JSlider {
  private static final int MIN_TICK_SPACING = 10;
  private final int defaultValue;

  protected ZoomLevelsSlider(int min, int max, int value) {
    super(min, max, value);
    defaultValue = value;
  }

  // BasicSliderUI.TrackListener#mouseDragged(...) moves the thumb to the raw
  // mouse location, and BasicSliderUI#calculateThumbLocation() is only called
  // while BasicSliderUI#isDragging() returns false. Consuming MOUSE_PRESSED,
  // MOUSE_DRAGGED and MOUSE_RELEASED keeps the TrackListener from starting a
  // thumb drag, so the LookAndFeel itself places the thumb at the position of
  // the snapped value.
  @Override protected void processMouseEvent(MouseEvent e) {
    int id = e.getID();
    boolean isPressed = id == MouseEvent.MOUSE_PRESSED;
    boolean isReleased = id == MouseEvent.MOUSE_RELEASED;
    if ((isPressed || isReleased) && isThumbDragEvent(e)) {
      if (isPressed) {
        startThumbDrag(e);
      } else {
        setValueIsAdjusting(false);
      }
    } else {
      super.processMouseEvent(e);
    }
  }

  private void startThumbDrag(MouseEvent e) {
    if (isRequestFocusEnabled()) {
      requestFocusInWindow();
    }
    setValueIsAdjusting(true);
    boolean isDoubleClick = e.getClickCount() >= 2;
    setValue(isDoubleClick ? defaultValue : getSnappedValue(e.getPoint()));
  }

  @Override protected void processMouseMotionEvent(MouseEvent e) {
    if (e.getID() == MouseEvent.MOUSE_DRAGGED && isThumbDragEvent(e)) {
      setValue(getSnappedValue(e.getPoint()));
    } else {
      // MOUSE_MOVED is delegated to the TrackListener to keep the rollover state
      super.processMouseMotionEvent(e);
    }
  }

  private boolean isThumbDragEvent(MouseEvent e) {
    return isEnabled() && getUI() instanceof BasicSliderUI
        && SwingUtilities.isLeftMouseButton(e);
  }

  // Snap the value to the initial value if the mouse cursor is
  // within half a tick from it
  private int getSnappedValue(Point pt) {
    BasicSliderUI ui = (BasicSliderUI) getUI();
    boolean horizontal = getOrientation() == HORIZONTAL;
    int value = horizontal ? ui.valueForXPosition(pt.x) : ui.valueForYPosition(pt.y);
    int tickSpacing = Math.max(getMajorTickSpacing(), MIN_TICK_SPACING);
    boolean nearDefaultValue = Math.abs(value - defaultValue) < tickSpacing / 2;
    return nearDefaultValue ? defaultValue : value;
  }
}

// class WindowsZoomLevelsSliderUI extends WindowsSliderUI {
//   protected WindowsZoomLevelsSliderUI(JSlider slider) {
//     super(slider);
//   }
//
//   @Override protected TrackListener createTrackListener(JSlider slider) {
//     return new WindowsTrackHandler();
//   }
//
//   private final class WindowsTrackHandler extends TrackListener {
//     @Override public void mouseClicked(MouseEvent e) {
//       boolean isLeftDoubleClick =
//           SwingUtilities.isLeftMouseButton(e) && e.getClickCount() >= 2;
//       if (isLeftDoubleClick && thumbRect.contains(e.getPoint())) {
//         slider.setValue(0);
//       } else {
//         super.mouseClicked(e);
//       }
//     }
//
//     @Override public void mouseDragged(MouseEvent e) {
//       // case HORIZONTAL:
//       int halfThumbWidth = thumbRect.width / 2;
//       int trackLength = trackRect.width;
//       int pos = e.getX() + halfThumbWidth;
//       int possibleTickPos = slider.getMaximum() - slider.getMinimum();
//       int tickSp = Math.max(slider.getMajorTickSpacing(), 10);
//       int tickPixels = trackLength * tickSp / possibleTickPos;
//       int tickPixels2 = tickPixels / 2;
//       int trackCenter = (int) trackRect.getCenterX();
//       if (trackCenter - tickPixels2 < pos && pos < trackCenter + tickPixels2) {
//         e.translatePoint(trackCenter - halfThumbWidth - e.getX(), 0);
//         offset = 0;
//       }
//       super.mouseDragged(e);
//     }
//   }
// }
