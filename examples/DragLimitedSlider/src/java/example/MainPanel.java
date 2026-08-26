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
  private static final int LOWER_LIMIT = 40;
  private static final int UPPER_LIMIT = 80;

  private MainPanel() {
    super(new GridLayout(2, 1, 5, 5));
    JSlider slider1 = new JSlider(0, 100, LOWER_LIMIT);
    initSlider(slider1);
    slider1.setBorder(BorderFactory.createTitledBorder("ChangeListener"));
    add(slider1);

    JSlider slider2 = new DragLimitedSlider(0, 100, LOWER_LIMIT, LOWER_LIMIT, UPPER_LIMIT);
    initSlider(slider2);
    slider2.setBorder(BorderFactory.createTitledBorder("ChangeListener + DragLimitedSlider"));
    add(slider2);
    setPreferredSize(new Dimension(320, 240));
  }

  private static void initSlider(JSlider slider) {
    slider.setMajorTickSpacing(10);
    slider.setPaintTicks(true);
    slider.setPaintLabels(true);
    Object labelTable = slider.getLabelTable();
    if (labelTable instanceof Map) {
      ((Map<?, ?>) labelTable).forEach((key, value) -> {
        if (value instanceof JLabel) {
          highlightOutOfRange((JLabel) value);
        }
      });
    }
    // The keyboard and the mouse wheel can still change the value,
    // so the model also limits the value
    slider.getModel().addChangeListener(e -> {
      BoundedRangeModel m = (BoundedRangeModel) e.getSource();
      m.setValue(Math.min(Math.max(m.getValue(), LOWER_LIMIT), UPPER_LIMIT));
      // Java 21: m.setValue(Math.clamp(m.getValue(), LOWER_LIMIT, UPPER_LIMIT));
    });
  }

  private static void highlightOutOfRange(JLabel label) {
    int value = Integer.parseInt(label.getText());
    if (value < LOWER_LIMIT || value > UPPER_LIMIT) {
      label.setForeground(Color.RED);
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

// Limit the range in which the value can be changed by mouse dragging
class DragLimitedSlider extends JSlider {
  private final int lowerDragLimit;
  private final int upperDragLimit;

  protected DragLimitedSlider(int min, int max, int value, int lower, int upper) {
    super(min, max, value);
    lowerDragLimit = lower;
    upperDragLimit = upper;
  }

  // BasicSliderUI.TrackListener#mouseDragged(...) moves the thumb to the raw
  // mouse location, and BasicSliderUI#calculateThumbLocation() is only called
  // while BasicSliderUI#isDragging() returns false. Consuming MOUSE_PRESSED,
  // MOUSE_DRAGGED and MOUSE_RELEASED keeps the TrackListener from starting a
  // thumb drag, so the LookAndFeel itself places the thumb at the position of
  // the limited value.
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

  @Override protected void processMouseMotionEvent(MouseEvent e) {
    if (e.getID() == MouseEvent.MOUSE_DRAGGED && isThumbDragEvent(e)) {
      setValue(getLimitedValue(e.getPoint()));
    } else {
      // MOUSE_MOVED is delegated to the TrackListener to keep the rollover state
      super.processMouseMotionEvent(e);
    }
  }

  private void startThumbDrag(MouseEvent e) {
    if (isRequestFocusEnabled()) {
      requestFocusInWindow();
    }
    setValueIsAdjusting(true);
    setValue(getLimitedValue(e.getPoint()));
  }

  private boolean isThumbDragEvent(MouseEvent e) {
    return isEnabled() && getUI() instanceof BasicSliderUI
        && SwingUtilities.isLeftMouseButton(e);
  }

  // Clamp the value under the mouse cursor to the draggable range
  private int getLimitedValue(Point pt) {
    BasicSliderUI ui = (BasicSliderUI) getUI();
    boolean horizontal = getOrientation() == HORIZONTAL;
    int value = horizontal ? ui.valueForXPosition(pt.x) : ui.valueForYPosition(pt.y);
    return Math.min(Math.max(value, lowerDragLimit), upperDragLimit);
    // Java 21: return Math.clamp(value, lowerDragLimit, upperDragLimit);
  }
}
