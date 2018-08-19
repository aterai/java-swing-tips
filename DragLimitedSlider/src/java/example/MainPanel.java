package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Objects;
import javax.swing.*;
import javax.swing.plaf.metal.MetalSliderUI;

import com.sun.java.swing.plaf.windows.WindowsSliderUI;

public final class MainPanel extends JPanel {
    public static final int MAXI = 80;
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
        Dictionary<?, ?> dictionary = slider.getLabelTable();
        if (Objects.nonNull(dictionary)) {
            Enumeration<?> elements = dictionary.elements();
            while (elements.hasMoreElements()) {
                JLabel label = (JLabel) elements.nextElement();
                int v = Integer.parseInt(label.getText());
                if (v > MAXI) {
                    label.setForeground(Color.RED);
                }
            }
        }
        slider.getModel().addChangeListener(e -> {
            BoundedRangeModel m = (BoundedRangeModel) e.getSource();
            if (m.getValue() > MAXI) {
                m.setValue(MAXI);
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
                    // case HORIZONTAL:
                    int halfThumbWidth = thumbRect.width / 2;
                    int thumbLeft = e.getX() - offset;
                    int maxPos = xPositionForValue(MAXI) - halfThumbWidth;
                    if (thumbLeft > maxPos) {
                        e.translatePoint(maxPos + offset - e.getX(), 0);
                    }
                    super.mouseDragged(e);
                }
            };
        }
    }
    private static class MetalDragLimitedSliderUI extends MetalSliderUI {
        @Override protected TrackListener createTrackListener(JSlider slider) {
            return new TrackListener() {
                @Override public void mouseDragged(MouseEvent e) {
                    // case HORIZONTAL:
                    int halfThumbWidth = thumbRect.width / 2;
                    int thumbLeft = e.getX() - offset;
                    int maxPos = xPositionForValue(MAXI) - halfThumbWidth;
                    if (thumbLeft > maxPos) {
                        e.translatePoint(maxPos + offset - e.getX(), 0);
                    }
                    super.mouseDragged(e);
                }
            };
        }
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
