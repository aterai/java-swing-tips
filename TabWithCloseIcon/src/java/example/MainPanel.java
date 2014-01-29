package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new GridLayout(3,1));

        JTabbedPaneWithCloseButton tab1 = new JTabbedPaneWithCloseButton();
        JTabbedPaneWithCloseIcons  tab2 = new JTabbedPaneWithCloseIcons();
        CloseableTabbedPane        tab3 = new CloseableTabbedPane();

        tab1.setPreferredSize(new Dimension(320, 80));
        tab2.setPreferredSize(new Dimension(320, 80));
        tab3.setPreferredSize(new Dimension(320, 80));

        tab1.addTab("aaa", new JLabel("aaaaaaaa"));
        tab1.addTab("bbb", new JLabel("bbbbbbbbb"));
        tab2.addTab("aaa", new JLabel("aaaaaaaa"));
        tab2.addTab("bbb", new JLabel("bbbbbbbbb"));
        tab3.addTab("aaa", new JLabel("aaaaaaaa"));
        tab3.addTab("bbb", new JLabel("bbbbbbbbb"));

        add(tab1);
        add(tab2);
        add(tab3);
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
