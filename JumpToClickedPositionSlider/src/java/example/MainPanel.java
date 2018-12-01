package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

import com.sun.java.swing.plaf.windows.WindowsSliderUI;

import java.awt.*;
import java.awt.event.MouseEvent;
import javax.swing.*;
import javax.swing.plaf.metal.MetalSliderUI;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());

        JSlider slider1 = new JSlider(SwingConstants.VERTICAL, 0, 1000, 500);
        setSilderUI(slider1);

        JSlider slider2 = new JSlider(0, 1000, 500);
        setSilderUI(slider2);

        Box box1 = Box.createHorizontalBox();
        box1.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        box1.add(new JSlider(SwingConstants.VERTICAL, 0, 1000, 100));
        box1.add(Box.createHorizontalStrut(20));
        box1.add(slider1);
        box1.add(Box.createHorizontalGlue());

        Box box2 = Box.createVerticalBox();
        box2.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 20));
        box2.add(makeTitledPanel("Default", new JSlider(0, 1000, 100)));
        box2.add(Box.createVerticalStrut(20));
        box2.add(makeTitledPanel("Jump to clicked position", slider2));
        box2.add(Box.createVerticalGlue());

        add(box1, BorderLayout.WEST);
        add(box2);
        // setBorder(BorderFactory.createEmptyBorder(5, 20, 5, 10));
        setPreferredSize(new Dimension(320, 240));
    }
    private static void setSilderUI(JSlider slider) {
        if (slider.getUI() instanceof WindowsSliderUI) {
            slider.setUI(new WindowsJumpToClickedPositionSliderUI(slider));
        } else {
            slider.setUI(new MetalJumpToClickedPositionSliderUI());
        }
        // slider.setSnapToTicks(false);
        // slider.setPaintTicks(true);
        // slider.setPaintLabels(true);
    }
    private static Component makeTitledPanel(String title, Component c) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder(title));
        p.add(c);
        return p;
    }
    public static void main(String... args) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                createAndShowGui();
            }
        });
    }
    public static void createAndShowGui() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException
               | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
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
    // // JSlider question: Position after leftclick - Stack Overflow
    // // https://stackoverflow.com/questions/518471/jslider-question-position-after-leftclick
    // // TEST:
    // protected void scrollDueToClickInTrack(int direction) {
    //     int value = slider.getValue();
    //     if (slider.getOrientation() == SwingConstants.HORIZONTAL) {
    //         value = this.valueForXPosition(slider.getMousePosition().x);
    //     } else if (slider.getOrientation() == SwingConstants.VERTICAL) {
    //         value = this.valueForYPosition(slider.getMousePosition().y);
    //     }
    //     slider.setValue(value);
    // }
    @Override protected TrackListener createTrackListener(JSlider slider) {
        return new TrackListener() {
            @Override public void mousePressed(MouseEvent e) {
                if (UIManager.getBoolean("Slider.onlyLeftMouseButtonDrag") && SwingUtilities.isLeftMouseButton(e)) {
                    JSlider slider = (JSlider) e.getComponent();
                    switch (slider.getOrientation()) {
                        case SwingConstants.VERTICAL:
                            slider.setValue(valueForYPosition(e.getY()));
                            break;
                        case SwingConstants.HORIZONTAL:
                            slider.setValue(valueForXPosition(e.getX()));
                            break;
                        default:
                            throw new IllegalArgumentException("orientation must be one of: VERTICAL, HORIZONTAL");
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
        };
    }
}

class MetalJumpToClickedPositionSliderUI extends MetalSliderUI {
    @Override protected TrackListener createTrackListener(JSlider slider) {
        return new TrackListener() {
            @Override public void mousePressed(MouseEvent e) {
                if (UIManager.getBoolean("Slider.onlyLeftMouseButtonDrag") && SwingUtilities.isLeftMouseButton(e)) {
                    JSlider slider = (JSlider) e.getComponent();
                    switch (slider.getOrientation()) {
                        case SwingConstants.VERTICAL:
                            slider.setValue(valueForYPosition(e.getY()));
                            break;
                        case SwingConstants.HORIZONTAL:
                            slider.setValue(valueForXPosition(e.getX()));
                            break;
                        default:
                            throw new IllegalArgumentException("orientation must be one of: VERTICAL, HORIZONTAL");
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
        };
    }
}
