package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.text.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.text.*;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());

        //http://bugs.java.com/bugdatabase/view_bug.do?bug_id=4403182
        //JTabbedPane tabbedPane0 = makeTestTabbedPane();
        //TEST: tabbedPane0.setVerifyInputWhenFocusTarget(true);
        //TEST: tabbedPane0.setFocusCycleRoot(false);

        JTextField field0 = new JTextField("---");
        JTextField field1 = new JTextField("100");
        List<JTextField> list = Arrays.asList(field0, field1);

        EventQueue.invokeLater(() -> field0.requestFocusInWindow());

        for (JTextField tf: list) {
            tf.setHorizontalAlignment(SwingConstants.RIGHT);
            tf.setInputVerifier(new IntegerInputVerifier());
        }

        JButton button0 = new JButton("Dummy");
        JButton button1 = new JButton("setText(0)");
        button1.addActionListener(e -> {
            for (JTextField tf: list) {
                tf.setText("0");
            }
        });

        button1.setVerifyInputWhenFocusTarget(false);

        JPanel bp = new JPanel();
        bp.add(button0);
        bp.add(button1);

        Box box = Box.createVerticalBox();
        box.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        box.add(field0);
        box.add(Box.createVerticalStrut(5));
        box.add(field1);
        box.add(Box.createVerticalStrut(5));
        box.add(bp);
        box.add(Box.createVerticalGlue());

        JPanel p = new JPanel(new BorderLayout());
        p.add(box, BorderLayout.NORTH);
        p.setInputVerifier(new InputVerifier() {
            @Override public boolean verify(JComponent c) {
                if (c.isShowing()) {
                    return list.stream().allMatch(tf -> tf.getInputVerifier().verify(tf));
                }
                return true;
            }
        });

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Integer only", new JScrollPane(p));
        tabbedPane.addTab("JTree", new JScrollPane(new JTree()));
        tabbedPane.addTab("JSplitPane", new JSplitPane());
        tabbedPane.setSelectedIndex(0);

        JCheckBox check = new JCheckBox("override SingleSelectionModel#setSelectedIndex(int)");
        SingleSelectionModel ssm = tabbedPane.getModel();
        check.addActionListener(e -> {
            if (((JCheckBox) e.getSource()).isSelected()) {
                // https://stackoverflow.com/questions/34315657/java-swing-jtextfield-setinputverifier-keep-focus-on-textfield
                tabbedPane.setModel(new DefaultSingleSelectionModel() {
                    @Override public void setSelectedIndex(int index) {
                        InputVerifier verifier = p.getInputVerifier();
                        if (Objects.nonNull(verifier) && !verifier.shouldYieldFocus(p)) {
                            UIManager.getLookAndFeel().provideErrorFeedback(p);
                            JOptionPane.showMessageDialog(p, "InputVerifier#verify(...): false");
                            return;
                        }
                        super.setSelectedIndex(index);
                    }
                });
            } else {
                tabbedPane.setModel(ssm);
            }
        });
        add(check, BorderLayout.NORTH);
        add(tabbedPane);
        setPreferredSize(new Dimension(320, 240));
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

//Validating Text and Filtering Documents and Accessibility and the Java Access Bridge Tech Tips
//>http://java.sun.com/developer/JDCTechTips/2005/tt0518.html
//Validating with Input Verifiers
class IntegerInputVerifier extends InputVerifier {
    @Override public boolean verify(JComponent c) {
        boolean verified = false;
        if (c instanceof JTextComponent) {
            JTextComponent textField = (JTextComponent) c;
            try {
                Integer.parseInt(textField.getText());
                verified = true;
            } catch (NumberFormatException ex) {
                verified = false;
                //System.out.println("InputVerifier#verify: false");
                //UIManager.getLookAndFeel().provideErrorFeedback(c);
            }
        }
        return verified;
    }
    @Override public boolean shouldYieldFocus(JComponent input) {
        boolean verified = verify(input);
        if (!verified) {
            UIManager.getLookAndFeel().provideErrorFeedback(input);
            //JOptionPane.showMessageDialog(input.getParent(), "InputVerifier#verify(...): false");
        }
        return verified;
    }
}
