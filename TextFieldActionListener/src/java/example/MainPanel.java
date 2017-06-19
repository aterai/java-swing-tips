package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;

public final class MainPanel extends JPanel {
    private final JTextArea log = new JTextArea();

    private MainPanel() {
        super(new BorderLayout());

        JButton button = new JButton("JButton");
        button.addActionListener(e -> append("JButton clicked"));
        JCheckBox check = new JCheckBox("setDefaultButton");
        check.addActionListener(e -> button.getRootPane().setDefaultButton(button));

        JTextField textField1 = new JTextField("addDocumentListener");
        textField1.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) {
                append("insertUpdate");
            }
            @Override public void removeUpdate(DocumentEvent e) {
                append("removeUpdate");
            }
            @Override public void changedUpdate(DocumentEvent e) { /* not needed */ }
        });

        JTextField textField2 = new JTextField("addActionListener");
        textField2.addActionListener(e -> append(((JTextField) e.getSource()).getText()));

        Box box = Box.createVerticalBox();
        box.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        box.add(new JTextField("dummy"));
        box.add(Box.createVerticalStrut(10));
        box.add(textField1);
        box.add(Box.createVerticalStrut(10));
        box.add(textField2);

        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        p.add(check);
        p.add(button);
        add(box, BorderLayout.NORTH);
        add(new JScrollPane(log));
        add(p, BorderLayout.SOUTH);
        setPreferredSize(new Dimension(320, 240));
    }
    protected void append(String text) {
        log.append(text + "\n");
        log.setCaretPosition(log.getDocument().getLength());
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
