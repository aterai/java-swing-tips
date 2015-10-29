package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
//import javax.swing.plaf.basic.BasicSliderUI;
import javax.swing.plaf.metal.MetalSliderUI;
import com.sun.java.swing.plaf.windows.WindowsSliderUI;

public final class MainPanel extends JPanel {
    private MainPanel() {
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
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        setPreferredSize(new Dimension(320, 240));
    }
    private static JSlider makeSlider() {
        JSlider slider = new JSlider(0, 100, 0);
        slider.setMajorTickSpacing(10);
        slider.setMinorTickSpacing(5);
        slider.setPaintTicks(true);
        //slider.setPaintLabels(true);
        slider.addMouseWheelListener(new SliderMouseWheelListener());
        return slider;
    }
    private static JComponent makeTitledPanel(String title, JComponent c) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder(title));
        p.add(c);
        return p;
    }
    private static void setSilderUI(JSlider slider) {
        if (slider.getUI() instanceof WindowsSliderUI) {
            slider.setUI(new WindowsTooltipSliderUI(slider));
        } else {
            slider.setUI(new MetalTooltipSliderUI());
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

class WindowsTooltipSliderUI extends WindowsSliderUI {
    protected WindowsTooltipSliderUI(JSlider slider) {
        super(slider);
    }
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
                    super.mousePressed(e); //isDragging = true;
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

class MetalTooltipSliderUI extends MetalSliderUI {
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
                    super.mousePressed(e); //isDragging = true;
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

class SliderPopupListener extends MouseAdapter {
    private final JWindow toolTip = new JWindow();
    private final JLabel label = new JLabel("", SwingConstants.CENTER);
    private final Dimension size = new Dimension(30, 20);
    private int prevValue = -1;

    protected SliderPopupListener() {
        super();
        label.setOpaque(false);
        label.setBackground(UIManager.getColor("ToolTip.background"));
        label.setBorder(UIManager.getBorder("ToolTip.border"));
        toolTip.add(label);
        toolTip.setSize(size);
    }
    protected void updateToolTip(MouseEvent me) {
        JSlider slider = (JSlider) me.getComponent();
        int intValue = (int) slider.getValue();
        if (prevValue != intValue) {
            label.setText(String.format("%03d", slider.getValue()));
            Point pt = me.getPoint();
            pt.y = -size.height;
            SwingUtilities.convertPointToScreen(pt, me.getComponent());
            pt.translate(-size.width / 2, 0);
            toolTip.setLocation(pt);
        }
        prevValue = intValue;
    }
    @Override public void mouseDragged(MouseEvent me) {
        updateToolTip(me);
    }
    @Override public void mousePressed(MouseEvent me) {
        if (UIManager.getBoolean("Slider.onlyLeftMouseButtonDrag") && SwingUtilities.isLeftMouseButton(me)) {
            toolTip.setVisible(true);
            updateToolTip(me);
        }
    }
    @Override public void mouseReleased(MouseEvent me) {
        toolTip.setVisible(false);
    }
}

class SliderMouseWheelListener implements MouseWheelListener {
    @Override public void mouseWheelMoved(MouseWheelEvent e) {
        JSlider s = (JSlider) e.getComponent();
        int i = (int) s.getValue() - e.getWheelRotation();
        BoundedRangeModel m = s.getModel();
        s.setValue(Math.min(Math.max(i, m.getMinimum()), m.getMaximum()));
    }
}
