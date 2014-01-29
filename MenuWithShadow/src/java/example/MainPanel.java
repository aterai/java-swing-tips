package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());
        add(new JLabel(new ImageIcon(getClass().getResource("test.png"))));
        setPreferredSize(new Dimension(320, 200));
    }
    private static JMenuBar makeMenuBar() {
        JMenuBar mb = new JMenuBar();
        JMenu menu = new JMenu("File");
        mb.add(menu);
        menu.add(new JMenuItem("Open"));
        menu.add(new JMenuItem("Save"));
        menu.add(new JMenuItem("Close"));
        menu.add(new JMenuItem("Exit"));
        menu = new JMenu("Edit");
        mb.add(menu);
        menu.add(new JMenuItem("Cut"));
        menu.add(new JMenuItem("Copy"));
        menu.add(new JMenuItem("Paste"));
        JMenu smenu = new JMenu("Edit");
        smenu.add(new JMenuItem("Cut"));
        smenu.add(new JMenuItem("Copy"));
        smenu.add(new JMenuItem("Paste"));
        menu.add(smenu);
        return mb;
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
        UIManager.put("PopupMenuUI","example.CustomPopupMenuUI");
        //contrib.com.jgoodies.looks.common.ShadowPopupFactory.install();

        JFrame frame = new JFrame("@title@");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setJMenuBar(makeMenuBar());
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
