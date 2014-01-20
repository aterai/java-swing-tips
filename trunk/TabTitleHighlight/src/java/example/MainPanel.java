package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class MainPanel extends JPanel {
    public MainPanel() {
        super(new BorderLayout());
        final JTabbedPane jtp = new JTabbedPane();
        jtp.addTab("11111", new JScrollPane(new JTree()));
        jtp.addTab("22222", new JScrollPane(new JLabel("asdfasdfsadf")));
        jtp.addTab("33333", new JScrollPane(new JTree()));
        jtp.addTab("44444", new JScrollPane(new JLabel("qerwqerqwerqwe")));
        jtp.addTab("55555", new JScrollPane(new JTree()));

        jtp.addMouseMotionListener(new MouseMotionAdapter() {
            @Override public void mouseMoved(MouseEvent e) {
                JTabbedPane source = (JTabbedPane)e.getSource();
                int num = source.indexAtLocation(e.getX(), e.getY());
                for(int i=0;i<source.getTabCount();i++) {
                    source.setForegroundAt(i, i==num ? Color.GREEN
                                                     : Color.BLACK);
                }
            }
        });
        add(jtp);
        setPreferredSize(new Dimension(320, 200));
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
        JFrame frame = new JFrame("@title@");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
