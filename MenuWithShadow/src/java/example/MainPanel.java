package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@
import java.awt.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());
        add(new JLabel(new ImageIcon(getClass().getResource("test.png"))));
        setPreferredSize(new Dimension(320, 240));
    }
    private static JMenuBar makeMenuBar() {
        JMenuBar mb = new JMenuBar();
        JMenu menu = new JMenu("File");
        mb.add(menu);
        menu.add("Open");
        menu.add("Save");
        menu.add("Close");
        menu.add("Exit");
        menu = new JMenu("Edit");
        mb.add(menu);
        menu.add("Cut");
        menu.add("Copy");
        menu.add("Paste");
        JMenu smenu = new JMenu("Edit");
        smenu.add("Cut");
        smenu.add("Copy");
        smenu.add("Paste");
        menu.add(smenu);
        return mb;
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
        UIManager.put("PopupMenuUI", "example.CustomPopupMenuUI");
        // contrib.com.jgoodies.looks.common.ShadowPopupFactory.install();

        JFrame frame = new JFrame("@title@");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setJMenuBar(makeMenuBar());
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
