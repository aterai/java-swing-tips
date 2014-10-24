package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.util.Arrays;
import java.util.List;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());
        Box box = Box.createVerticalBox();
        List<String> list = Arrays.asList(
            "no_disposal_specified",
            "do_not_dispose",
            "restore_to_background_color",
            "restore_to_previous");
        for (String s: list) {
            Icon i = new ImageIcon(getClass().getResource(s + ".gif"));
            box.add(new JLabel(s, i, JLabel.LEFT));
            box.add(Box.createVerticalStrut(20));
        }
        box.add(Box.createVerticalGlue());
        add(box);
        setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        setPreferredSize(new Dimension(320, 240));
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
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
