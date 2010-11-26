package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.plaf.metal.*;

public class MainPanel extends JPanel{
    private final JSlider slider1 = new JSlider(JSlider.VERTICAL,0,1000,500);
    private final JSlider slider2 = new JSlider(0,1000,500);

    public MainPanel() {
        super(new BorderLayout());
        setSilderUI(slider1);
        setSilderUI(slider2);

        Box box1 = Box.createHorizontalBox();
        box1.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
        box1.add(new JSlider(JSlider.VERTICAL,0,1000,100));
        box1.add(Box.createHorizontalStrut(20));
        box1.add(slider1);
        box1.add(Box.createHorizontalGlue());

        Box box2 = Box.createVerticalBox();
        box2.setBorder(BorderFactory.createEmptyBorder(20,0,20,20));
        box2.add(makeTitledPanel("Default", new JSlider(0,1000,100)));
        box2.add(Box.createVerticalStrut(20));
        box2.add(makeTitledPanel("Jump to clicked position", slider2));
        box2.add(Box.createVerticalGlue());

        add(box1, BorderLayout.WEST);
        add(box2);
        //setBorder(BorderFactory.createEmptyBorder(5,20,5,10));
        setPreferredSize(new Dimension(320, 240));
    }
    private static void setSilderUI(JSlider slider) {
        if(slider.getUI() instanceof com.sun.java.swing.plaf.windows.WindowsSliderUI) {
            slider.setUI(new com.sun.java.swing.plaf.windows.WindowsSliderUI(slider) {
//                 // JSlider question: Position after leftclick - Stack Overflow
//                 // http://stackoverflow.com/questions/518471/jslider-question-position-after-leftclick
//                 protected void scrollDueToClickInTrack(int direction) {
//                     int value = slider.getValue();
//                     if (slider.getOrientation() == JSlider.HORIZONTAL) {
//                         value = this.valueForXPosition(slider.getMousePosition().x);
//                     } else if (slider.getOrientation() == JSlider.VERTICAL) {
//                         value = this.valueForYPosition(slider.getMousePosition().y);
//                     }
//                     slider.setValue(value);
//                 }
                protected TrackListener createTrackListener(JSlider slider) {
                    return new TrackListener() {
                        @Override public void mousePressed(MouseEvent e) {
                            JSlider slider = (JSlider)e.getSource();
                            switch (slider.getOrientation()) {
                              case JSlider.VERTICAL:
                                slider.setValue(valueForYPosition(e.getY()));
                                break;
                              case JSlider.HORIZONTAL:
                                slider.setValue(valueForXPosition(e.getX()));
                                break;
                            }
                            super.mousePressed(e); //isDragging = true;
                            super.mouseDragged(e);
                        }
                        @Override public boolean shouldScroll(int direction) {
                            return false;
                        }
                    };
                }
            });
        }else{
            slider.setUI(new MetalSliderUI() {
                protected TrackListener createTrackListener(JSlider slider) {
                    return new TrackListener() {
                        @Override public void mousePressed(MouseEvent e) {
                            JSlider slider = (JSlider)e.getSource();
                            switch (slider.getOrientation()) {
                              case JSlider.VERTICAL:
                                slider.setValue(valueForYPosition(e.getY()));
                                break;
                              case JSlider.HORIZONTAL:
                                slider.setValue(valueForXPosition(e.getX()));
                                break;
                            }
                            super.mousePressed(e); //isDragging = true;
                            super.mouseDragged(e);
                        }
                        @Override public boolean shouldScroll(int direction) {
                            return false;
                        }
                    };
                }
            });
        }
//         slider.setSnapToTicks(false);
//         slider.setPaintTicks(true);
//         slider.setPaintLabels(true);
    }
    private static JComponent makeTitledPanel(String title, JComponent c) {
        //JPanel p = new JPanel(new BorderLayout());
        c.setBorder(BorderFactory.createTitledBorder(title));
        //p.add(c, BorderLayout.NORTH);
        return c;
    }
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                createAndShowGUI();
            }
        });
    }
    public static void createAndShowGUI() {
        try{
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }catch(Exception e) {
            e.printStackTrace();
        }
        JFrame frame = new JFrame("@title@");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
