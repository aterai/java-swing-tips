package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@
import java.awt.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());
        ImageIcon image = new ImageIcon(getClass().getResource("16x16.png"));
        JLabel label1 = new JLabel(image);
        JTextField field1 = new JTextField("bbbbbbbbbb") {
            @Override public void updateUI() {
                super.updateUI();
                add(label1);
            }
        };

        int w = image.getIconWidth();
        Insets m = field1.getMargin();
        field1.setMargin(new Insets(m.top, m.left + w, m.bottom, m.right));
        label1.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        label1.setBorder(BorderFactory.createEmptyBorder());
        label1.setBounds(m.left, m.top, w, image.getIconHeight());

        JLabel label2 = new JLabel(image);
        label2.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        label2.setBorder(BorderFactory.createEmptyBorder());
        JTextField field2 = new JTextField("cccccccccccccccccccccccccccccccccccc") {
            @Override public void updateUI() {
                super.updateUI();
                removeAll();
                SpringLayout l = new SpringLayout();
                setLayout(l);
                Spring fw = l.getConstraint(SpringLayout.WIDTH, this);
                Spring fh = l.getConstraint(SpringLayout.HEIGHT, this);
                SpringLayout.Constraints c = l.getConstraints(label2);
                c.setConstraint(SpringLayout.WEST, fw);
                c.setConstraint(SpringLayout.SOUTH, fh);
                add(label2);
            }
        };
        m = field2.getMargin();
        field2.setMargin(new Insets(m.top + 2, m.left, m.bottom, m.right + w));

        Box box = Box.createVerticalBox();
        box.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        box.add(makeTitledPanel("Default", new JTextField("aaaaaaaaaaaa")));
        box.add(Box.createVerticalStrut(5));
        box.add(makeTitledPanel("add Image(JLabel)", field1));
        box.add(Box.createVerticalStrut(5));
        box.add(makeTitledPanel("SpringLayout", field2));

        add(box, BorderLayout.NORTH);
        setPreferredSize(new Dimension(320, 240));
    }
    private static Component makeTitledPanel(String title, Component c) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder(title));
        p.add(c);
        return p;
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
