package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.plaf.basic.BasicSliderUI;
import javax.swing.plaf.metal.MetalSliderUI;
import com.sun.java.swing.plaf.windows.WindowsSliderUI;

public class MainPanel extends JPanel {
    private static String str = "Can only edit last line, version 0.0\n";
    public MainPanel() {
        super(new BorderLayout());
        JSlider slider1 = makeSlider();
        JSlider slider2 = makeSlider();
        slider2.setModel(slider1.getModel());
        setSilderUI(slider2);

        MouseAdapter ma = new SliderPopupListener();
        slider2.addMouseMotionListener(ma);
        slider2.addMouseListener(ma);

        Box box = Box.createVerticalBox();
        box.add(Box.createVerticalStrut(5));
        box.add(makeTitledPanel("Default", slider1));
        box.add(Box.createVerticalStrut(25));
        box.add(makeTitledPanel("Show ToolTip", slider2));
        box.add(Box.createVerticalGlue());

        add(box, BorderLayout.NORTH);
        setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        setPreferredSize(new Dimension(320, 240));
    }
    private static JSlider makeSlider() {
        JSlider slider = new JSlider(0,100,0);
        slider.setMajorTickSpacing(10);
        slider.setMinorTickSpacing(5);
        slider.setPaintTicks(true);
        //slider.setPaintLabels(true);
        slider.addMouseWheelListener(new MouseWheelListener() {
            @Override public void mouseWheelMoved(MouseWheelEvent e) {
                JSlider source = (JSlider)e.getSource();
                int intValue = (int)source.getValue()-e.getWheelRotation();
                BoundedRangeModel model = source.getModel();
                if(model.getMaximum()>=intValue && model.getMinimum()<=intValue) {
                    source.setValue(intValue);
                }
            }
        });
        return slider;
    }
    private JComponent makeTitledPanel(String title, JComponent c) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder(title));
        p.add(c);
        return p;
    }
    private static void setSilderUI(JSlider slider) {
        if(slider.getUI() instanceof WindowsSliderUI) {
            slider.setUI(new WindowsSliderUI(slider) {
                @Override protected TrackListener createTrackListener(JSlider slider) {
                    return new TrackListener() {
                        @Override public void mousePressed(MouseEvent e) {
                            JSlider slider = (JSlider)e.getSource();
                            switch(slider.getOrientation()) {
                              case JSlider.VERTICAL:
                                slider.setValue(valueForYPosition(e.getY()));
                                break;
                              case JSlider.HORIZONTAL:
                                slider.setValue(valueForXPosition(e.getX()));
                                break;
                              default:
                                throw new IllegalArgumentException("orientation must be one of: VERTICAL, HORIZONTAL");
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
                @Override protected TrackListener createTrackListener(JSlider slider) {
                    return new TrackListener() {
                        @Override public void mousePressed(MouseEvent e) {
                            JSlider slider = (JSlider)e.getSource();
                            switch(slider.getOrientation()) {
                              case JSlider.VERTICAL:
                                slider.setValue(valueForYPosition(e.getY()));
                                break;
                              case JSlider.HORIZONTAL:
                                slider.setValue(valueForXPosition(e.getX()));
                                break;
                              default:
                                throw new IllegalArgumentException("orientation must be one of: VERTICAL, HORIZONTAL");
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
    }
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                createAndShowGUI();
            }
        });
    }
    public static void createAndShowGUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch(Exception e) {
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

class SliderPopupListener extends MouseAdapter{
    private final JWindow toolTip = new JWindow();
    private final JLabel label = new JLabel("", SwingConstants.CENTER);
    private final Dimension size = new Dimension(30, 20);
    public SliderPopupListener() {
        label.setOpaque(false);
        label.setBackground(UIManager.getColor("ToolTip.background"));
        label.setBorder(UIManager.getBorder("ToolTip.border"));
        toolTip.add(label);
        toolTip.setSize(size);
    }
    private int prevValue = -1;
    protected void updateToolTip(MouseEvent me) {
        JSlider slider = (JSlider)me.getSource();
        int intValue = (int)slider.getValue();
        if(prevValue!=intValue) {
            label.setText(String.format("%03d", slider.getValue()));
            Point pt = me.getPoint();
            pt.y = -size.height;
            SwingUtilities.convertPointToScreen(pt, (Component)me.getSource());
            pt.translate(-size.width/2, 0);
            toolTip.setLocation(pt);
        }
        prevValue = intValue;
    }
    @Override public void mouseDragged(MouseEvent me) {
        updateToolTip(me);
    }
    @Override public void mousePressed(MouseEvent me) {
        toolTip.setVisible(true);
        updateToolTip(me);
    }
    @Override public void mouseReleased(MouseEvent me) {
        toolTip.setVisible(false);
    }
}
