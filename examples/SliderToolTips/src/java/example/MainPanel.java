// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.plaf.basic.BasicSliderUI;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JSlider slider = new JumpToClickedPositionSlider(SwingConstants.HORIZONTAL, 0, 100, 50) {
      private transient MouseAdapter handler;

      @Override public void updateUI() {
        removeMouseMotionListener(handler);
        removeMouseListener(handler);
        super.updateUI();
        handler = new SliderPopupListener();
        addMouseMotionListener(handler);
        addMouseListener(handler);
      }
    };

    Box box = Box.createVerticalBox();
    box.add(Box.createVerticalStrut(5));
    box.add(createTitledPanel("Default", initSlider(new JSlider(0, 100, 50))));
    box.add(Box.createVerticalStrut(25));
    box.add(createTitledPanel("Show ToolTip", initSlider(slider)));
    box.add(Box.createVerticalGlue());

    add(box, BorderLayout.NORTH);
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    setPreferredSize(new Dimension(320, 240));
  }

  private static JSlider initSlider(JSlider slider) {
    slider.setPaintTicks(true);
    slider.setMajorTickSpacing(10);
    slider.setMinorTickSpacing(5);
    slider.addMouseWheelListener(e -> {
      JSlider s = (JSlider) e.getComponent();
      s.setValue(s.getValue() - e.getWheelRotation());
    });
    return slider;
  }

  private static Component createTitledPanel(String title, Component c) {
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

// Slide the thumb to the clicked position and keep it draggable
class JumpToClickedPositionSlider extends JSlider {
  protected JumpToClickedPositionSlider(int orientation, int min, int max, int value) {
    super(orientation, min, max, value);
  }

  @Override protected void processMouseEvent(MouseEvent e) {
    if (e.getID() == MouseEvent.MOUSE_PRESSED && isJumpEvent(e)) {
      setValue(getValueForPoint(e.getPoint()));
    }
    super.processMouseEvent(e);
  }

  private boolean isJumpEvent(MouseEvent e) {
    return isEnabled() && getUI() instanceof BasicSliderUI
        && SwingUtilities.isLeftMouseButton(e);
  }

  private int getValueForPoint(Point pt) {
    BasicSliderUI ui = (BasicSliderUI) getUI();
    boolean horizontal = getOrientation() == HORIZONTAL;
    return horizontal ? ui.valueForXPosition(pt.x) : ui.valueForYPosition(pt.y);
  }
}

class SliderPopupListener extends MouseAdapter {
  private final JWindow toolTip = new JWindow();
  private final JLabel label = new JLabel(" ", SwingConstants.CENTER) {
    @Override public Dimension getPreferredSize() {
      Dimension d = super.getPreferredSize();
      d.width = 32;
      return d;
    }
  };
  private int prevValue = -1;

  protected SliderPopupListener() {
    super();
    label.setOpaque(true);
    label.setBackground(UIManager.getColor("ToolTip.background"));
    label.setBorder(UIManager.getBorder("ToolTip.border"));
    toolTip.add(label);
    toolTip.pack();
  }

  protected void updateToolTip(MouseEvent e) {
    JSlider slider = (JSlider) e.getComponent();
    int intValue = slider.getValue();
    if (prevValue != intValue) {
      label.setText(String.format("%03d", slider.getValue()));
      Point pt = e.getPoint();
      pt.y = (int) SwingUtilities.calculateInnerArea(slider, null).getCenterY();
      SwingUtilities.convertPointToScreen(pt, e.getComponent());
      // int gap = 2;
      // int thumbHeight = UIManager.getInt("Slider.thumbHeight");
      // int trackHeight = 6;
      // int h2 = gap + (thumbHeight + trackHeight) / 2;
      int h2 = slider.getPreferredSize().height / 2;
      Dimension d = label.getPreferredSize();
      pt.translate(-d.width / 2, -d.height - h2);
      toolTip.setLocation(pt);
    }
    prevValue = intValue;
  }

  @Override public void mouseDragged(MouseEvent e) {
    updateToolTip(e);
  }

  @Override public void mousePressed(MouseEvent e) {
    if (SwingUtilities.isLeftMouseButton(e)) {
      toolTip.setVisible(true);
      updateToolTip(e);
    }
  }

  @Override public void mouseReleased(MouseEvent e) {
    toolTip.setVisible(false);
  }
}
