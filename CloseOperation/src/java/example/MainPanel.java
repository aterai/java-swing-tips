package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.*;

public final class MainPanel extends JPanel {
    public static final AtomicInteger COUNTER = new AtomicInteger(0);
    public static JFrame createFrame(String title) {
        JFrame frame = new JFrame(Objects.toString(title, "Frame #" + COUNTER));
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        COUNTER.getAndIncrement();
        frame.addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) {
                if (COUNTER.getAndDecrement() == 0) {
                    Window w = e.getWindow();
                    if (w instanceof JFrame) {
                        ((JFrame) w).setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
                    }
                }
            }
        });
        return frame;
    }

    private MainPanel() {
        super(new BorderLayout());
        JButton button = new JButton("New Frame");
        button.addActionListener(e -> {
            JButton b = (JButton) e.getSource();
            JFrame f = createFrame(null);
            f.getContentPane().add(new MainPanel());
            f.pack();
            Container c = b.getTopLevelAncestor();
            if (c instanceof Window) {
                Point pt = ((Window) c).getLocation();
                f.setLocation(pt.x, pt.y + f.getSize().height);
            }
            // f.setLocationByPlatform(true);
            f.setVisible(true);
        });
        add(button);
        setPreferredSize(new Dimension(320, 100));
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
        JFrame frame = createFrame("@title@"); // new JFrame("@title@");
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
