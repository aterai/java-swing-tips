package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;
import java.util.List;
import javax.swing.*;
import javax.swing.plaf.metal.MetalSliderUI;
import com.sun.java.swing.plaf.windows.WindowsSliderUI;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());
        final List<JSlider> list = Arrays.asList(
            makeSilder("Default SnapToTicks"),
            makeSilder("Custom SnapToTicks"));
        Box b = Box.createVerticalBox();
        b.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        for (JSlider slider: list) {
            b.add(slider);
            b.add(Box.createVerticalStrut(10));
        }
        b.add(new JCheckBox(new AbstractAction("JSlider.setMinorTickSpacing(5)") {
            @Override public void actionPerformed(ActionEvent e) {
                JCheckBox cb = (JCheckBox) e.getSource();
                for (JSlider slider: list) {
                    slider.setMinorTickSpacing(cb.isSelected() ? 5 : 0);
                }
            }
        }));
        b.add(Box.createVerticalGlue());
        add(b);
        setPreferredSize(new Dimension(320, 240));
    }
    private static JSlider makeSilder(String title) {
        JSlider slider = new JSlider(0, 100, 50);
        //JSlider slider = new JSlider(-50, 50, 0);
        slider.setBorder(BorderFactory.createTitledBorder(title));
        slider.setMajorTickSpacing(10);
        slider.setSnapToTicks(true);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        if (title.startsWith("Default")) {
            return slider;
        }
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
        slider.addMouseWheelListener(new MouseWheelListener() {
            @Override public void mouseWheelMoved(MouseWheelEvent e) {
                JSlider s = (JSlider) e.getComponent();
                int intValue = s.getValue() - e.getWheelRotation() * s.getMajorTickSpacing();
                BoundedRangeModel model = s.getModel();
                if (model.getMaximum() >= intValue && model.getMinimum() <= intValue) {
                    s.setValue(intValue);
                }
            }
        });
        if (slider.getUI() instanceof WindowsSliderUI) {
            slider.setUI(new WindowsSnapToTicksDragSliderUI(slider));
        } else {
            slider.setUI(new MetalSnapToTicksDragSliderUI());
        }
        return slider;
    }
    public static void main(String... args) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                createAndShowGUI();
            }
        });
    }
    public static void createAndShowGUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException |
                 IllegalAccessException | UnsupportedLookAndFeelException ex) {
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

class WindowsSnapToTicksDragSliderUI extends WindowsSliderUI {
    public WindowsSnapToTicksDragSliderUI(JSlider slider) {
        super(slider);
    }
    @Override protected TrackListener createTrackListener(final JSlider slider) {
        return new TrackListener() {
            @Override public void mouseDragged(MouseEvent e) {
                if (!slider.getSnapToTicks() || slider.getMajorTickSpacing() == 0) {
                    super.mouseDragged(e);
                    return;
                }
                //case JSlider.HORIZONTAL:
                int halfThumbWidth = thumbRect.width / 2;
                final int trackLength = trackRect.width;
                final int trackLeft   = trackRect.x - halfThumbWidth;
                final int trackRight  = trackRect.x + trackRect.width - 1 + halfThumbWidth;
                int xPos = e.getX();
                int snappedPos = xPos;
                if (xPos <= trackLeft) {
                    snappedPos = trackLeft;
                } else if (xPos >= trackRight) {
                    snappedPos = trackRight;
                } else {
                    //int tickSpacing = slider.getMajorTickSpacing();
                    //float actualPixelsForOneTick = trackLength * tickSpacing / (float) slider.getMaximum();

                    // a problem if you choose to set a negative MINIMUM for the JSlider;
                    // the calculated drag-positions are wrong.
                    // Fixed by bobndrew:
                    int possibleTickPositions = slider.getMaximum() - slider.getMinimum();
                    int tickSpacing = (slider.getMinorTickSpacing() == 0)
                                    ? slider.getMajorTickSpacing()
                                    : slider.getMinorTickSpacing();
                    float actualPixelsForOneTick = trackLength * tickSpacing / (float) possibleTickPositions;
                    xPos -= trackLeft;
                    snappedPos = (int) (Math.round(xPos / actualPixelsForOneTick) * actualPixelsForOneTick + 0.5) + trackLeft;
                    offset = 0;
                    //System.out.println(snappedPos);
                }
                MouseEvent me = new MouseEvent(
                    e.getComponent(), e.getID(), e.getWhen(), e.getModifiers(),
                    snappedPos, e.getY(),
                    e.getXOnScreen(), e.getYOnScreen(),
                    e.getClickCount(), e.isPopupTrigger(), e.getButton());
                e.consume();
                super.mouseDragged(me);
            }
        };
    }
}

class MetalSnapToTicksDragSliderUI extends MetalSliderUI {
    @Override protected TrackListener createTrackListener(final JSlider slider) {
        return new TrackListener() {
            @Override public void mouseDragged(MouseEvent e) {
                if (!slider.getSnapToTicks() || slider.getMajorTickSpacing() == 0) {
                    super.mouseDragged(e);
                    return;
                }
                //case JSlider.HORIZONTAL:
                int halfThumbWidth = thumbRect.width / 2;
                final int trackLength = trackRect.width;
                final int trackLeft   = trackRect.x - halfThumbWidth;
                final int trackRight  = trackRect.x + trackRect.width - 1 + halfThumbWidth;
                int xPos = e.getX();
                int snappedPos = xPos;
                if (xPos <= trackLeft) {
                    snappedPos = trackLeft;
                } else if (xPos >= trackRight) {
                    snappedPos = trackRight;
                } else {
                    //int tickSpacing = slider.getMajorTickSpacing();
                    //float actualPixelsForOneTick = trackLength * tickSpacing / (float) slider.getMaximum();

                    // a problem if you choose to set a negative MINIMUM for the JSlider;
                    // the calculated drag-positions are wrong.
                    // Fixed by bobndrew:
                    int possibleTickPositions = slider.getMaximum() - slider.getMinimum();
                    int tickSpacing = (slider.getMinorTickSpacing() == 0)
                                    ? slider.getMajorTickSpacing()
                                    : slider.getMinorTickSpacing();
                    float actualPixelsForOneTick = trackLength * tickSpacing / (float) possibleTickPositions;
                    xPos -= trackLeft;
                    snappedPos = (int) (Math.round(xPos / actualPixelsForOneTick) * actualPixelsForOneTick + 0.5) + trackLeft;
                    offset = 0;
                    //System.out.println(snappedPos);
                }
                MouseEvent me = new MouseEvent(
                    e.getComponent(), e.getID(), e.getWhen(), e.getModifiers(),
                    snappedPos, e.getY(),
                    e.getXOnScreen(), e.getYOnScreen(),
                    e.getClickCount(), e.isPopupTrigger(), e.getButton());
                super.mouseDragged(me);
            }
        };
    }
}
