package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class MainPanel extends JPanel {
    public MainPanel() {
        super(new BorderLayout());

        JLabel label1 = new JLabel("Mail Adress:", SwingConstants.RIGHT);
        label1.setDisplayedMnemonic('M');
        JComponent textField1 = new JTextField(12);
        label1.setLabelFor(textField1);

        JLabel label2 = new JLabel("Password:", SwingConstants.RIGHT);
        label2.setDisplayedMnemonic('P');
        JComponent textField2 = new JPasswordField(12);
        label2.setLabelFor(textField2);

        JLabel label3 = new JLabel("Dummy:", SwingConstants.RIGHT);
        JComponent textField3 = new JTextField(12);

        JLabel label4 = new JLabel("ComboBox:", SwingConstants.RIGHT);
        label4.setDisplayedMnemonic('C');
        final JComponent comboBox = new JComboBox();

        GridBagConstraints c = new GridBagConstraints();
        JPanel panel = new JPanel(new GridBagLayout());

        c.gridheight = 1;
        c.gridwidth  = 1;
        c.gridy = 0;
        addRow(label1, textField1, panel, c);

        c.gridy = 1;
        addRow(label2, textField2, panel, c);

        c.gridy = 2;
        addRow(label3, textField3, panel, c);

        c.gridy = 3;
        addRow(label4, comboBox, panel, c);

        add(new JButton(new AbstractAction("JComboBox#requestFocusInWindow() Test") {
            @Override public void actionPerformed(ActionEvent e) {
                comboBox.requestFocusInWindow();
            }
        }), BorderLayout.SOUTH);
        add(panel, BorderLayout.NORTH);
        setPreferredSize(new Dimension(320, 240));
    }
    private static void addRow(JComponent c1, JComponent c2, JPanel p, GridBagConstraints c) {
        c.gridx = 0;
        c.weightx = 0.0;
        c.insets = new Insets(5, 5, 5, 0);
        c.anchor = GridBagConstraints.EAST;
        p.add(c1, c);

        c.gridx = 1;
        c.weightx = 1.0;
        c.insets = new Insets(5, 5, 5, 5);
        c.fill = GridBagConstraints.HORIZONTAL;
        p.add(c2, c);
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
        }catch(Exception e) {
            e.printStackTrace();
        }
        JFrame frame = new JFrame("@title@");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
