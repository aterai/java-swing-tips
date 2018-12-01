package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

import java.awt.*;
import java.util.stream.Stream;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());
        JTextField field = new JTextField(20);
        field.setToolTipText("setColumns(20)");

        JPasswordField passwd = new JPasswordField(20);
        passwd.setToolTipText("setColumns(20)");

        JSpinner spinner = new JSpinner();
        spinner.setToolTipText("setColumns(20)");
        ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField().setColumns(20);

        JComboBox<String> combo1 = new JComboBox<>();
        combo1.setEditable(true);
        combo1.setToolTipText("setEditable(true), setColumns(20)");
        ((JTextField) combo1.getEditor().getEditorComponent()).setColumns(20);

        JComboBox<String> combo2 = new JComboBox<>();
        combo2.setToolTipText("setEditable(true), default");
        combo2.setEditable(true);

        JComboBox<String> combo3 = new JComboBox<>();
        combo3.setToolTipText("setEditable(false), default");

        SpringLayout layout = new SpringLayout();
        JPanel p = new JPanel(layout);
        layout.putConstraint(SpringLayout.WEST, field, 10, SpringLayout.WEST, p);
        layout.putConstraint(SpringLayout.WEST, passwd, 10, SpringLayout.WEST, p);
        layout.putConstraint(SpringLayout.WEST, spinner, 10, SpringLayout.WEST, p);
        layout.putConstraint(SpringLayout.WEST, combo1, 10, SpringLayout.WEST, p);
        layout.putConstraint(SpringLayout.WEST, combo2, 10, SpringLayout.WEST, p);
        layout.putConstraint(SpringLayout.WEST, combo3, 10, SpringLayout.WEST, p);
        layout.putConstraint(SpringLayout.NORTH, field, 10, SpringLayout.NORTH, p);
        layout.putConstraint(SpringLayout.NORTH, passwd, 10, SpringLayout.SOUTH, field);
        layout.putConstraint(SpringLayout.NORTH, spinner, 10, SpringLayout.SOUTH, passwd);
        layout.putConstraint(SpringLayout.NORTH, combo1, 10, SpringLayout.SOUTH, spinner);
        layout.putConstraint(SpringLayout.NORTH, combo2, 10, SpringLayout.SOUTH, combo1);
        layout.putConstraint(SpringLayout.NORTH, combo3, 10, SpringLayout.SOUTH, combo2);

        Stream.of(field, passwd, spinner, combo1, combo2, combo3).forEach(p::add);
        add(p);
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
