package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import javax.swing.*;

public class MainPanel extends JPanel {
    private final JTextField field      = new JTextField(20);
    private final JPasswordField passwd = new JPasswordField(20);
    private final JComboBox combo1      = new JComboBox();
    private final JComboBox combo2      = new JComboBox();
    private final JComboBox combo3      = new JComboBox();
    private final JSpinner spinner      = new JSpinner();

    public MainPanel() {
        super();
        SpringLayout layout = new SpringLayout();
        setLayout(layout);

        ((JSpinner.DefaultEditor)spinner.getEditor()).getTextField().setColumns(20);

        combo1.setEditable(true);
        ((JTextField)combo1.getEditor().getEditorComponent()).setColumns(20);

        combo2.setEditable(true);

        layout.putConstraint(SpringLayout.WEST, field,    10, SpringLayout.WEST,  this);
        layout.putConstraint(SpringLayout.WEST, passwd,   10, SpringLayout.WEST,  this);
        layout.putConstraint(SpringLayout.WEST, spinner,  10, SpringLayout.WEST,  this);
        layout.putConstraint(SpringLayout.WEST, combo1,   10, SpringLayout.WEST,  this);
        layout.putConstraint(SpringLayout.WEST, combo2,   10, SpringLayout.WEST,  this);
        layout.putConstraint(SpringLayout.WEST, combo3,   10, SpringLayout.WEST,  this);
        layout.putConstraint(SpringLayout.NORTH, field,   10, SpringLayout.NORTH, this);
        layout.putConstraint(SpringLayout.NORTH, passwd,  10, SpringLayout.SOUTH, field);
        layout.putConstraint(SpringLayout.NORTH, spinner, 10, SpringLayout.SOUTH, passwd);
        layout.putConstraint(SpringLayout.NORTH, combo1,  10, SpringLayout.SOUTH, spinner);
        layout.putConstraint(SpringLayout.NORTH, combo2,  10, SpringLayout.SOUTH, combo1);
        layout.putConstraint(SpringLayout.NORTH, combo3,  10, SpringLayout.SOUTH, combo2);

        field.setToolTipText("setColumns(20)");
        passwd.setToolTipText("setColumns(20)");
        spinner.setToolTipText("setColumns(20)");
        combo1.setToolTipText("setEditable(true), setColumns(20)");
        combo2.setToolTipText("setEditable(true), default");
        combo3.setToolTipText("setEditable(false), default");

        add(field);
        add(passwd);
        add(spinner);
        add(combo1);
        add(combo2);
        add(combo3);

        setPreferredSize(new Dimension(320, 200));
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
        JFrame frame = new JFrame("@title@");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
