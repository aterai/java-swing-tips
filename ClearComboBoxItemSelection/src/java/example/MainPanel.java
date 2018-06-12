package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@
import java.awt.*;
import java.util.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());

        JComboBox<String> combo = new JComboBox<>(new String[] {"AAAAA", "BBBB", "CCC", "DD", "E"});
        combo.setSelectedIndex(-1);

        JButton b1 = new JButton("setSelectedIndex(0)");
        JButton b2 = new JButton("setSelectedIndex(-1)");
        JButton b3 = new JButton("setSelectedItem(null)");

        b1.addActionListener(e -> combo.setSelectedIndex(0));
        b2.addActionListener(e -> combo.setSelectedIndex(-1));
        b3.addActionListener(e -> combo.setSelectedItem(null));

        JPanel box = new JPanel(new GridLayout(0, 1, 10, 10));
        Arrays.asList(b1, b2, b3, combo).forEach(box::add);

        add(box, BorderLayout.NORTH);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setPreferredSize(new Dimension(320, 240));
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
        JFrame frame = new JFrame("@title@");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
