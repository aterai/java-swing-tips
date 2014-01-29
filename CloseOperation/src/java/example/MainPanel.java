package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private static int number = 0;
    public static JFrame createFrame(String title) {
        JFrame frame = new JFrame((title==null)?"Frame #"+number:title);
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        number++;
        frame.addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) {
                number--;
                if(number==0) {
                    JFrame f = (JFrame)e.getWindow();
                    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                }
            }
        });
        return frame;
    }

    private MainPanel() {
        super(new BorderLayout());
        JButton button = new JButton(new AbstractAction("New Frame") {
            @Override public void actionPerformed(final ActionEvent ae) {
                JButton button = (JButton)ae.getSource();
                JFrame frame   = createFrame(null);
                frame.getContentPane().add(new MainPanel());
                frame.pack();
                JFrame parent = (JFrame)SwingUtilities.getWindowAncestor(button);
                Point pt = parent.getLocation();
                frame.setLocation(pt.x, pt.y+frame.getSize().height);
                //frame.setLocationByPlatform(true);
                frame.setVisible(true);
            }
        });
        add(button);
        setPreferredSize(new Dimension(320, 100));
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
        }catch(ClassNotFoundException | InstantiationException |
               IllegalAccessException | UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        }
        JFrame frame = createFrame("@title@"); //new JFrame("@title@");
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
