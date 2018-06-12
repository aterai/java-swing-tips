package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@
import java.awt.*;
import java.text.ParseException;
import java.util.Arrays;
import javax.swing.*;
import javax.swing.text.*;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());
        JFormattedTextField field0 = new JFormattedTextField();

        JFormattedTextField field1 = new JFormattedTextField();
        field1.setFocusLostBehavior(JFormattedTextField.REVERT);

        JFormattedTextField field2 = new JFormattedTextField();
        field2.setFocusLostBehavior(JFormattedTextField.COMMIT);

        JFormattedTextField field3 = new JFormattedTextField();
        field3.setFocusLostBehavior(JFormattedTextField.PERSIST);

        JCheckBox check = new JCheckBox("setCommitsOnValidEdit");
        try {
            MaskFormatter formatter = new MaskFormatter("UUUUUUUUUU");
            // formatter.setAllowsInvalid(true);
            // formatter.setCommitsOnValidEdit(true);
            // formatter.setPlaceholder("_");
            // formatter.setPlaceholderCharacter('?');
            // DefaultFormatterFactory ff = new DefaultFormatterFactory(formatter);
            Arrays.asList(field0, field1, field2, field3).forEach(f -> f.setFormatterFactory(new DefaultFormatterFactory(formatter)));
            check.addActionListener(e -> formatter.setCommitsOnValidEdit(((JCheckBox) e.getSource()).isSelected()));
        } catch (ParseException ex) {
            ex.printStackTrace();
        }

        Box box = Box.createVerticalBox();
        box.add(makeTitledPanel("COMMIT_OR_REVERT(default)", field0));
        box.add(Box.createVerticalStrut(5));
        box.add(makeTitledPanel("REVERT", field1));
        box.add(Box.createVerticalStrut(5));
        box.add(makeTitledPanel("COMMIT", field2));
        box.add(Box.createVerticalStrut(5));
        box.add(makeTitledPanel("PERSIST", field3));
        box.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        add(box, BorderLayout.NORTH);
        add(check, BorderLayout.SOUTH);
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
