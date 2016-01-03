package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.plaf.metal.MetalSliderUI;
import com.sun.java.swing.plaf.windows.WindowsSliderUI;

public final class MainPanel extends JPanel {
    private static final double MAXD = .8;
    private static final int MAXI = (int) (100 * MAXD);
    private MainPanel() {
        super(new GridLayout(2, 1, 5, 5));

        JSlider slider1 = makeSilder("ChangeListener");
        JSlider slider2 = makeSilder("TrackListener");
        if (slider2.getUI() instanceof WindowsSliderUI) {
            slider2.setUI(new WindowsDragLimitedSliderUI(slider2));
        } else {
            slider2.setUI(new MetalDragLimitedSliderUI());
        }
        add(slider1);
        add(slider2);
        setPreferredSize(new Dimension(320, 240));
    }
    private static JSlider makeSilder(String title) {
        JSlider slider = new JSlider(0, 100, 40);
        slider.setBorder(BorderFactory.createTitledBorder(title));
        slider.setMajorTickSpacing(10);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        Dictionary dictionary = slider.getLabelTable();
        if (dictionary != null) {
            Enumeration elements = dictionary.elements();
            while (elements.hasMoreElements()) {
                JLabel label = (JLabel) elements.nextElement();
                int v = Integer.parseInt(label.getText());
                if (v > MAXI) {
                    label.setForeground(Color.RED);
                }
            }
        }
        slider.getModel().addChangeListener(new ChangeListener() {
            @Override public void stateChanged(ChangeEvent e) {
                BoundedRangeModel m = (BoundedRangeModel) e.getSource();
                if (m.getValue() > MAXI) {
                    m.setValue(MAXI);
                }
            }
        });
        return slider;
    }
    private static class WindowsDragLimitedSliderUI extends WindowsSliderUI {
        protected WindowsDragLimitedSliderUI(JSlider slider) {
            super(slider);
        }
        @Override protected TrackListener createTrackListener(JSlider slider) {
            return new TrackListener() {
                @Override public void mouseDragged(MouseEvent e) {
                    //case HORIZONTAL:
                    int halfThumbWidth = thumbRect.width / 2;
                    int trackLeft   = trackRect.x - halfThumbWidth;
                    int maxPos = trackLeft + (int) (.5 + trackRect.width * MAXD);
                    if (e.getX() > maxPos) {
                        offset = 0;
                        MouseEvent me = new MouseEvent(
                            e.getComponent(), e.getID(), e.getWhen(), e.getModifiers(),
                            maxPos, e.getY(),
                            e.getXOnScreen(), e.getYOnScreen(),
                            e.getClickCount(), e.isPopupTrigger(), e.getButton());
                        e.consume();
                        super.mouseDragged(me);
                    } else {
                        super.mouseDragged(e);
                    }
                }
            };
        }
    }
    private static class MetalDragLimitedSliderUI extends MetalSliderUI {
        @Override protected TrackListener createTrackListener(JSlider slider) {
            return new TrackListener() {
                @Override public void mouseDragged(MouseEvent e) {
                    //case HORIZONTAL:
                    int halfThumbWidth = thumbRect.width / 2;
                    int trackLeft   = trackRect.x - halfThumbWidth;
                    int maxPos = trackLeft + (int) (.5 + trackRect.width * MAXD);
                    if (e.getX() > maxPos) {
                        offset = 0;
                        MouseEvent me = new MouseEvent(
                            e.getComponent(), e.getID(), e.getWhen(), e.getModifiers(),
                            maxPos, e.getY(),
                            e.getXOnScreen(), e.getYOnScreen(),
                            e.getClickCount(), e.isPopupTrigger(), e.getButton());
                        e.consume();
                        super.mouseDragged(me);
                    } else {
                        super.mouseDragged(e);
                    }
                }
            };
        }
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
