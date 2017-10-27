package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.plaf.*;
import javax.swing.plaf.metal.MetalSliderUI;
import com.sun.java.swing.plaf.windows.WindowsSliderUI;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());
        JSlider slider0 = new JSlider();
        slider0.putClientProperty("Slider.paintThumbArrowShape", Boolean.TRUE);

        JSlider slider1 = new JSlider() {
            @Override public void updateUI() {
                super.updateUI();
                if (getUI() instanceof WindowsSliderUI) {
                    setUI(new WindowsSliderUI(this) {
                        @Override protected TrackListener createTrackListener(JSlider slider) {
                            return new TrackListener() {
                                @Override public boolean shouldScroll(int direction) {
                                    return false;
                                }
                            };
                        }
                    });
                } else {
                    setUI(new MetalSliderUI() {
                        @Override protected TrackListener createTrackListener(JSlider slider) {
                            return new TrackListener() {
                                @Override public boolean shouldScroll(int direction) {
                                    return false;
                                }
                            };
                        }
                    });
                }
            }
        };
        slider1.putClientProperty("Slider.paintThumbArrowShape", Boolean.TRUE);

        // https://ateraimemo.com/Swing/OnlyLeftMouseButtonDrag.html
        UIManager.put("Slider.onlyLeftMouseButtonDrag", false);
        JSlider slider2 = new JSlider();
        slider2.putClientProperty("Slider.paintThumbArrowShape", Boolean.TRUE);

        Box box = Box.createVerticalBox();
        box.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        box.add(makeTitledPanel("Default", slider0));
        box.add(Box.createVerticalStrut(20));

        JLabel label = new JLabel(" disable scroll due to click in track");
        box.add(label);
        box.add(Box.createVerticalStrut(10));
        box.add(makeTitledPanel("Override TrackListener#shouldScroll(...): false",  slider1));
        box.add(Box.createVerticalStrut(10));
        box.add(makeTitledPanel("JLayer + Slider.onlyLeftMouseButtonDrag: false", new JLayer<>(slider2, new DisableLeftPressedLayerUI<>())));
        box.add(Box.createVerticalGlue());
        for (Component c: box.getComponents()) {
            ((JComponent) c).setAlignmentX(0f);
        }
        add(box, BorderLayout.NORTH);
        setPreferredSize(new Dimension(320, 240));
    }
    private static JComponent makeTitledPanel(String title, JComponent c) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder(title));
        p.add(c, BorderLayout.NORTH);
        return p;
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

class DisableLeftPressedLayerUI<V extends Component> extends LayerUI<V> {
    @Override public void installUI(JComponent c) {
        super.installUI(c);
        if (c instanceof JLayer) {
            ((JLayer) c).setLayerEventMask(AWTEvent.MOUSE_EVENT_MASK);
        }
    }
    @Override public void uninstallUI(JComponent c) {
        if (c instanceof JLayer) {
            ((JLayer) c).setLayerEventMask(0);
        }
        super.uninstallUI(c);
    }
    @Override protected void processMouseEvent(MouseEvent e, JLayer<? extends V> l) {
        if (e.getID() == MouseEvent.MOUSE_PRESSED && SwingUtilities.isLeftMouseButton(e)) {
            e.getComponent().dispatchEvent(new MouseEvent(
                e.getComponent(),
                e.getID(), e.getWhen(),
                InputEvent.BUTTON3_DOWN_MASK, //e.getModifiers(),
                e.getX(), e.getY(),
                e.getXOnScreen(), e.getYOnScreen(),
                e.getClickCount(),
                e.isPopupTrigger(),
                MouseEvent.BUTTON3)); //e.getButton());
            e.consume();
        }
    }
}
