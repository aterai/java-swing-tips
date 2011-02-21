package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import javax.swing.*;
import javax.swing.text.*;

public class MainPanel extends JPanel {
    public MainPanel() {
        super(new GridLayout(2,1));

        JTextField textField = new JTextField();
//         textField.setCaret(new DefaultCaret() {
//             @Override public void paint(Graphics g) {}
//         });
        ((DefaultCaret)textField.getCaret()).setBlinkRate(0);

        add(makeTitlePanel(new JTextField(), "default"));
        add(makeTitlePanel(textField, "DefaultCaret#setBlinkRate(0)"));
        setBorder(BorderFactory.createEmptyBorder(10,5,10,5));
        setPreferredSize(new Dimension(320, 200));
    }
    private JComponent makeTitlePanel(JComponent cmp, String title) {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.weightx = 1.0;
        c.fill    = GridBagConstraints.HORIZONTAL;
        c.insets  = new Insets(5, 5, 5, 5);
        p.add(cmp, c);
        p.setBorder(BorderFactory.createTitledBorder(title));
        return p;
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
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
