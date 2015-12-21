package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());
        add(new JButton(new AbstractAction("show frame title") {
            @Override public void actionPerformed(ActionEvent e) {
                JButton btn  = (JButton) e.getSource();
                Window w = SwingUtilities.getWindowAncestor(btn);
                //Container c = btn.getTopLevelAncestor();
                //Frame f = JOptionPane.getFrameForComponent(btn);
                if (w instanceof JFrame) {
                    JFrame frame = (JFrame) w;
                    JOptionPane.showMessageDialog(frame, "parentFrame.getTitle(): " + frame.getTitle(), "title", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        }));
        setPreferredSize(new Dimension(320, 100));
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
        JFrame frame1 = new JFrame("@title@");
        frame1.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame1.getContentPane().add(new MainPanel());
        frame1.pack();
        frame1.setLocationRelativeTo(null);

        JFrame frame2 = new JFrame("frame2");
        frame2.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame2.getContentPane().add(new MainPanel());
        frame2.pack();
        Point pt = frame1.getLocation();
        frame2.setLocation(pt.x, pt.y + frame1.getSize().height);

        frame1.setVisible(true);
        frame2.setVisible(true);
    }
}
