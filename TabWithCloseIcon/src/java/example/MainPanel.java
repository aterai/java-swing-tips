package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.util.Arrays;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new GridLayout(3, 1));

        JTabbedPaneWithCloseButton tab1 = new JTabbedPaneWithCloseButton();
        JTabbedPaneWithCloseIcons  tab2 = new JTabbedPaneWithCloseIcons();
        CloseableTabbedPane        tab3 = new CloseableTabbedPane();

        for (JTabbedPane t: Arrays.asList(tab1, tab2, tab3)) {
            t.addTab("aaa",  new JLabel("aaaaaaaa"));
            t.addTab("bbb",  new JLabel("bbbbbbbbb"));
            t.addTab("c",    new JLabel("ccc"));
            t.addTab("dddd", new JLabel("ddddddd"));
            add(t);
        }
        setPreferredSize(new Dimension(320, 240));
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
        } catch (ClassNotFoundException | InstantiationException |
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
