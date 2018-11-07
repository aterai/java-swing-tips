package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.stream.Stream;
import javax.swing.*;
import javax.swing.text.JTextComponent;

public final class MainPanel extends JPanel {
    private final JTextField field0 = new JTextField("9999999999999999");
    private final JTextField field1 = new JTextField("1111111111111111");
    private final JTextField field2 = new JTextField("9876543210987654");
    private final JButton button0 = new JButton("Default");
    private final JButton button1 = new JButton("setFocusable(false)");
    private final JButton button2 = new JButton("setVerifyInputWhenFocusTarget(false)");

    public MainPanel() {
        super(new BorderLayout(5, 5));
        EventQueue.invokeLater(() -> field0.requestFocusInWindow());
        ActionListener al = e -> {
            Component c = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
            System.out.println(c);
            Stream.of(field0, field1, field2).forEach(tf -> tf.setText(""));
        };
        button0.addActionListener(al);
        button1.addActionListener(al);
        button2.addActionListener(al);

        // button0.setVerifyInputWhenFocusTarget(true);
        button1.setFocusable(false);
        button2.setVerifyInputWhenFocusTarget(false);

        InputVerifier verifier = new IntegerInputVerifier();
        Stream.of(field0, field1, field2).forEach(tf -> {
            tf.setHorizontalAlignment(SwingConstants.RIGHT);
            tf.setInputVerifier(verifier);
        });

        JButton b0 = new JButton("setText(0)");
        b0.addActionListener(e -> {
            Stream.of(field0, field1, field2).forEach(tf -> tf.setText("0"));
            field0.requestFocusInWindow();
        });

        JButton b1 = new JButton("setText(Integer.MAX_VALUE+1)");
        b1.addActionListener(e -> {
            Stream.of(field0, field1, field2).forEach(tf -> tf.setText("2147483648"));
            field0.requestFocusInWindow();
        });

        JPanel bp = new JPanel();
        bp.add(b0);
        bp.add(b1);

        Box box = Box.createVerticalBox();
        box.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        box.add(bp);
        box.add(Box.createVerticalStrut(5));
        box.add(field0);
        box.add(Box.createVerticalStrut(5));
        box.add(field1);
        box.add(Box.createVerticalStrut(5));
        box.add(field2);
        box.add(Box.createVerticalStrut(5));
        box.add(Box.createVerticalGlue());

        JPanel p1 = new JPanel();
        p1.add(button0);
        p1.add(button1);

        JPanel p2 = new JPanel();
        p2.add(button2);

        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder("clear all"));
        p.add(p1, BorderLayout.NORTH);
        p.add(p2, BorderLayout.SOUTH);

        add(box, BorderLayout.NORTH);
        add(p, BorderLayout.SOUTH);
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

// Validating Text and Filtering Documents and Accessibility and the Java Access Bridge Tech Tips
// http://java.sun.com/developer/JDCTechTips/2005/tt0518.html
// Validating with Input Verifiers
class IntegerInputVerifier extends InputVerifier {
    @Override public boolean verify(JComponent c) {
        boolean verified = false;
        if (c instanceof JTextComponent) {
            JTextComponent textField = (JTextComponent) c;
            try {
                Integer.parseInt(textField.getText());
                verified = true;
            } catch (NumberFormatException ex) {
                System.out.println("InputVerifier#verify: false");
                UIManager.getLookAndFeel().provideErrorFeedback(c);
            }
        }
        return verified;
    }
}
