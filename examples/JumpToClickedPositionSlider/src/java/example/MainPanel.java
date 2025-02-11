// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import com.sun.java.swing.plaf.windows.WindowsSliderUI;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.plaf.SliderUI;
import javax.swing.plaf.basic.BasicSliderUI;
import javax.swing.plaf.metal.MetalSliderUI;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    Box box1 = Box.createHorizontalBox();
    box1.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
    box1.add(new JSlider(SwingConstants.VERTICAL, 0, 1000, 100));
    box1.add(Box.createHorizontalStrut(20));
    box1.add(makeSlider(true));
    box1.add(Box.createHorizontalGlue());
    add(box1, BorderLayout.WEST);

    Box box2 = Box.createVerticalBox();
    box2.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 20));
    box2.add(makeTitledPanel("Default", new JSlider(0, 1000, 100)));
    box2.add(Box.createVerticalStrut(20));
    box2.add(makeTitledPanel("Jump to clicked position", makeSlider(false)));
    box2.add(Box.createVerticalGlue());
    add(box2);

    JMenuBar mb = new JMenuBar();
    mb.add(LookAndFeelUtils.createLookAndFeelMenu());
    EventQueue.invokeLater(() -> getRootPane().setJMenuBar(mb));
    setPreferredSize(new Dimension(320, 240));
  }

  private static JSlider makeSlider(boolean vertical) {
    int orientation = vertical ? SwingConstants.VERTICAL : SwingConstants.HORIZONTAL;
    return new JSlider(orientation, 0, 1000, 500) {
      @Override public void updateUI() {
        super.updateUI();
        SliderUI ui = getUI();
        if (ui instanceof WindowsSliderUI) {
          setUI(new WindowsJumpToClickedPositionSliderUI(this));
        } else if (ui instanceof MetalSliderUI) {
          setUI(new MetalJumpToClickedPositionSliderUI());
        } else {
          setUI(new BasicJumpToClickedPositionSliderUI(this));
        }
        // } else { // NullPointerException ???
        //   UIManager.put("Slider.trackWidth", 0); // Meaningless settings that are not used?
        //   UIManager.put("Slider.majorTickLength", 8); // BasicSliderUI#getTickLength(): 8
        //   UIManager.put("Slider.verticalThumbIcon", UIManager.getIcon("html.missingImage"));
        //   UIManager.put("Slider.horizontalThumbIcon", UIManager.getIcon("html.missingImage"));
        //   setUI(new MetalJumpToClickedPositionSliderUI());
        // }
      }
    };
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

class WindowsJumpToClickedPositionSliderUI extends WindowsSliderUI {
  protected WindowsJumpToClickedPositionSliderUI(JSlider slider) {
    super(slider);
  }

  @Override protected TrackListener createTrackListener(JSlider slider) {
    return new TrackListener() {
      @Override public void mousePressed(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)) {
          scrollToClickInTrack(e);
          super.mousePressed(e); // isDragging = true;
          super.mouseDragged(e);
        } else {
          super.mousePressed(e);
        }
      }

      @Override public boolean shouldScroll(int direction) {
        return false;
      }
    };
  }

  private void scrollToClickInTrack(MouseEvent e) {
    JSlider slider = (JSlider) e.getComponent();
    switch (slider.getOrientation()) {
      case SwingConstants.VERTICAL:
        slider.setValue(valueForYPosition(e.getY()));
        break;
      case SwingConstants.HORIZONTAL:
        slider.setValue(valueForXPosition(e.getX()));
        break;
      default:
        String msg = "orientation must be one of: VERTICAL, HORIZONTAL";
        throw new IllegalArgumentException(msg);
    }
  }
}

class MetalJumpToClickedPositionSliderUI extends MetalSliderUI {
  @Override protected TrackListener createTrackListener(JSlider slider) {
    return new TrackListener() {
      @Override public void mousePressed(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)) {
          scrollToClickInTrack(e);
          super.mousePressed(e); // isDragging = true;
          super.mouseDragged(e);
        } else {
          super.mousePressed(e);
        }
      }

      @Override public boolean shouldScroll(int direction) {
        return false;
      }
    };
  }

  private void scrollToClickInTrack(MouseEvent e) {
    JSlider slider = (JSlider) e.getComponent();
    switch (slider.getOrientation()) {
      case SwingConstants.VERTICAL:
        slider.setValue(valueForYPosition(e.getY()));
        break;
      case SwingConstants.HORIZONTAL:
        slider.setValue(valueForXPosition(e.getX()));
        break;
      default:
        String msg = "orientation must be one of: VERTICAL, HORIZONTAL";
        throw new IllegalArgumentException(msg);
    }
  }
}

class BasicJumpToClickedPositionSliderUI extends BasicSliderUI {
  protected BasicJumpToClickedPositionSliderUI(JSlider slider) {
    super(slider);
  }

  @Override protected TrackListener createTrackListener(JSlider slider) {
    return new JumpTrackListener();
  }

  // // JSlider question: Position after left-click - Stack Overflow
  // // https://stackoverflow.com/questions/518471/jslider-question-position-after-leftclick
  // protected void scrollDueToClickInTrack(int direction) {
  //   int value = slider.getValue();
  //   if (slider.getOrientation() == SwingConstants.HORIZONTAL) {
  //     value = this.valueForXPosition(slider.getMousePosition().x);
  //   } else if (slider.getOrientation() == SwingConstants.VERTICAL) {
  //     value = this.valueForYPosition(slider.getMousePosition().y);
  //   }
  //   slider.setValue(value);
  // }

  protected class JumpTrackListener extends TrackListener {
    @Override public void mousePressed(MouseEvent e) {
      // boolean b = UIManager.getBoolean("Slider.onlyLeftMouseButtonDrag");
      if (SwingUtilities.isLeftMouseButton(e)) {
        JSlider slider = (JSlider) e.getComponent();
        switch (slider.getOrientation()) {
          case SwingConstants.VERTICAL:
            slider.setValue(valueForYPosition(e.getY()));
            break;
          case SwingConstants.HORIZONTAL:
            slider.setValue(valueForXPosition(e.getX()));
            break;
          default:
            String msg = "orientation must be one of: VERTICAL, HORIZONTAL";
            throw new IllegalArgumentException(msg);
        }
        super.mousePressed(e); // isDragging = true;
        super.mouseDragged(e);
      } else {
        super.mousePressed(e);
      }
    }

    @Override public boolean shouldScroll(int direction) {
      return false;
    }
  }
}

final class LookAndFeelUtils {
  private static String lookAndFeel = UIManager.getLookAndFeel().getClass().getName();

  private LookAndFeelUtils() {
    /* Singleton */
  }

  public static JMenu createLookAndFeelMenu() {
    JMenu menu = new JMenu("LookAndFeel");
    ButtonGroup buttonGroup = new ButtonGroup();
    for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
      AbstractButton b = makeButton(info);
      initLookAndFeelAction(info, b);
      menu.add(b);
      buttonGroup.add(b);
    }
    return menu;
  }

  private static AbstractButton makeButton(UIManager.LookAndFeelInfo info) {
    boolean selected = info.getClassName().equals(lookAndFeel);
    return new JRadioButtonMenuItem(info.getName(), selected);
  }

  public static void initLookAndFeelAction(UIManager.LookAndFeelInfo info, AbstractButton b) {
    String cmd = info.getClassName();
    b.setText(info.getName());
    b.setActionCommand(cmd);
    b.setHideActionText(true);
    b.addActionListener(e -> setLookAndFeel(cmd));
  }

  private static void setLookAndFeel(String newLookAndFeel) {
    String oldLookAndFeel = lookAndFeel;
    if (!oldLookAndFeel.equals(newLookAndFeel)) {
      try {
        UIManager.setLookAndFeel(newLookAndFeel);
        lookAndFeel = newLookAndFeel;
      } catch (UnsupportedLookAndFeelException ignored) {
        Toolkit.getDefaultToolkit().beep();
      } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
        Logger.getGlobal().severe(ex::getMessage);
        return;
      }
      updateLookAndFeel();
      // firePropertyChange("lookAndFeel", oldLookAndFeel, newLookAndFeel);
    }
  }

  private static void updateLookAndFeel() {
    for (Window window : Window.getWindows()) {
      SwingUtilities.updateComponentTreeUI(window);
    }
  }
}
