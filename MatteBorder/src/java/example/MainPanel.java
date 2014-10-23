package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;

public final class MainPanel extends JPanel {
    private final JLabel label = new JLabel("MANIFEST.MF");
    public MainPanel() {
        super(new BorderLayout());
        Border outside = BorderFactory.createMatteBorder(0, 10, 1, 0, new Color(50, 200, 50));
        Border inside  = BorderFactory.createEmptyBorder(0, 5, 0, 0);
        label.setBorder(BorderFactory.createCompoundBorder(outside, inside));

        Font font = label.getFont();
        label.setFont(new Font(font.getFontName(), font.getStyle(), font.getSize() * 2));

        JPanel p = new JPanel(new BorderLayout(2, 2));
        p.add(label, BorderLayout.NORTH);
        p.add(makeInfoBox(), BorderLayout.SOUTH);
        add(p, BorderLayout.NORTH);
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        setPreferredSize(new Dimension(320, 180));
    }

    private static Box makeInfoBox() {
        Box box = Box.createVerticalBox();
        box.add(new JLabel("Manifest-Version: 1.0"));
        box.add(new JLabel("Ant-Version: Apache Ant 1.6.2"));
        box.add(new JLabel("Created-By: 1.4.2_06-b03 (Sun Microsystems Inc.)"));
        box.add(new JLabel("Main-Class: example.MainPanel"));
        box.add(new JLabel("Implementation-Title: Example"));
        box.add(new JLabel("Implementation-Version: 1.0.32"));
        box.add(new JLabel("Class-Path: ."));
        Box box2 = Box.createHorizontalBox();
        box2.add(Box.createHorizontalStrut(10));
        box2.add(box);
        return box2;
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
