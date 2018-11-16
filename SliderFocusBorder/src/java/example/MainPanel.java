package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.*;

import com.sun.java.swing.plaf.windows.WindowsSliderUI;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());
        // UIManager.put("Slider.border", BorderFactory.createLineBorder(Color.GREEN));
        // UIManager.put("Slider.focus", UIManager.get("Slider.background"));
        // UIManager.put("Slider.focusInsets", new Insets(5, 15, 5, 15));

        JSlider slider1 = new JSlider(0, 100, 0);
        initSlider(slider1);
        // TEST: slider1.setBorder(BorderFactory.createLineBorder(Color.RED));

        JSlider slider2 = new JSlider(0, 100, 0) {
            private transient FocusListener listener;
            @Override public void updateUI() {
                removeFocusListener(listener);
                super.updateUI();
                if (getUI() instanceof WindowsSliderUI) {
                    setUI(new WindowsSliderUI(this) {
                        @Override public void paintFocus(Graphics g) {
                            // // TEST:
                            // Graphics2D g2 = (Graphics2D) g.create();
                            // g2.setPaint(new Color(255, 255, 255, 100));
                            // g2.fill(focusRect);
                            // g2.dispose();
                        }
                        // @Override protected Color getHighlightColor() {
                        //     Color c = super.getHighlightColor();
                        //     return slider.hasFocus() ? Color.GREEN : Color.RED;
                        // }
                    });
                    Color bgc = getBackground();
                    listener = new FocusListener() {
                        @Override public void focusGained(FocusEvent e) {
                            setBackground(bgc.brighter());
                        }
                        @Override public void focusLost(FocusEvent e) {
                            setBackground(bgc);
                        }
                    };
                    addFocusListener(listener);
                }
            }
        };
        initSlider(slider2);

        Box box = Box.createVerticalBox();
        box.add(Box.createVerticalStrut(20));
        box.add(makeTitledPanel("Default", slider1));
        box.add(Box.createVerticalStrut(20));
        box.add(makeTitledPanel("Override SilderUI#paintFocus(...)", slider2));
        box.add(Box.createVerticalGlue());

        add(box, BorderLayout.NORTH);
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        setPreferredSize(new Dimension(320, 240));
    }
    private static JSlider initSlider(JSlider slider) {
        slider.setMajorTickSpacing(10);
        slider.setMinorTickSpacing(5);
        slider.setPaintTicks(true);
        return slider;
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
