package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import javax.swing.*;
import javax.swing.text.JTextComponent;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new GridLayout(2, 1));

        JTextField field = new JTextField("aaaaaaaaa");
        field.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                ((JTextComponent) e.getComponent()).selectAll();
            }
        });

        add(makeTitledPanel("focusGained: selectAll", field));
        add(makeTitledPanel("default", new JTextField("bbbbbbbbb")));
        setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));
        setPreferredSize(new Dimension(320, 240));
    }
    private static Component makeTitledPanel(String title, Component cmp) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createTitledBorder(title));
        GridBagConstraints c = new GridBagConstraints();
        c.weightx = 1d;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(5, 5, 5, 5);
        p.add(cmp, c);
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
