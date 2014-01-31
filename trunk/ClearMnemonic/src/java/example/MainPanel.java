package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class MainPanel extends JPanel {
    private final JTextField textField = new JTextField("B");
    public MainPanel() {
        super();

        final JButton button = new JButton(new AbstractAction("Button") {
            @Override public void actionPerformed(ActionEvent e) {
                Toolkit.getDefaultToolkit().beep();
            }
        });

        JPanel p = new JPanel();
        p.setBorder(BorderFactory.createTitledBorder("setMnemonic"));
        p.add(textField);
        p.add(new JButton(new AbstractAction("setMnemonic(...)") {
            @Override public void actionPerformed(ActionEvent e) {
                String str = textField.getText().trim();
                if(str.isEmpty()) {
                    str = button.getText();
                }
                button.setMnemonic(str.charAt(0));
            }
        }));
        p.add(new JButton(new AbstractAction("clear Mnemonic") {
            @Override public void actionPerformed(ActionEvent e) {
                button.setMnemonic(0);
            }
        }));

        add(button);
        add(p);
        setPreferredSize(new Dimension(320, 240));
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
