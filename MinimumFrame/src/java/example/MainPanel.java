package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private static final int MW = 320;
    private static final int MH1 = 100;
    private static final int MH2 = 150;

    private MainPanel(JFrame frame) {
        super(new BorderLayout());

        JLabel label = new JLabel();
        label.addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) {
                ((JLabel) e.getComponent()).setText(frame.getSize().toString());
            }
        });

        JCheckBox checkbox1 = new JCheckBox("the minimum size of this window: " + MW + "x" + MH1, true);
        checkbox1.addActionListener(e -> {
            if (!((JCheckBox) e.getSource()).isSelected()) {
                return;
            }
            initFrameSize(frame);
        });

        JCheckBox checkbox2 = new JCheckBox("the minimum size of this window(since 1.6): " + MW + "x" + MH2, true);
        checkbox2.addActionListener(e -> frame.setMinimumSize(checkbox2.isSelected() ? new Dimension(MW, MH2) : null));

        frame.setMinimumSize(new Dimension(MW, MH2));
        frame.addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) {
                if (!checkbox1.isSelected()) {
                    return;
                }
                initFrameSize((JFrame) e.getComponent());
            }
        });

        Box box = Box.createVerticalBox();
        box.add(checkbox1);
        box.add(checkbox2);
        add(box, BorderLayout.NORTH);
        add(label);
        setPreferredSize(new Dimension(320, 240));
    }
    protected static void initFrameSize(JFrame frame) {
        int fw = frame.getSize().width;
        int fh = frame.getSize().height;
        frame.setSize(Math.max(MW, fw), Math.max(MH1, fh));
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
        // int MAX = 500;
        // // frame.setMaximumSize(new Dimension(MAX, MAX));
        // Robot r;
        // Robot r2;
        // try {
        //     r = new Robot();
        // } catch (AWTException ex) {
        //     r = null;
        // }
        // r2 = r;
        // frame.getRootPane().addComponentListener(new ComponentAdapter() {
        //     @Override public void componentResized(ComponentEvent e) {
        //         Point loc = frame.getLocationOnScreen();
        //         Point mouse = MouseInfo.getPointerInfo().getLocation();
        //         if (Objects.nonNull(r2) && (mouse.getX() > loc.getX() + MAX || mouse.getY() > loc.getY() + MAX)) {
        //             r2.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
        //             frame.setSize(Math.min(MAX, frame.getWidth()), Math.min(MAX, frame.getHeight()));
        //         }
        //     }
        // });
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel(frame));
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
