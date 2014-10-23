package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new GridLayout(1, 2, 5, 5));

        JPanel p1 = new JPanel();
        p1.setBorder(BorderFactory.createTitledBorder("left"));
        p1.add(new JTextField(16));
        p1.add(new JTextField(16));
        p1.add(new JTextField(16));
        p1.add(new JTextField(16));
        p1.add(new JTextField(16));
        p1.add(new JTextField(16));
        //p1.setFocusTraversalPolicyProvider(true);
        p1.setFocusCycleRoot(true);

        JPanel p2 = new JPanel();
        p2.setBorder(BorderFactory.createTitledBorder("right"));
        p2.add(new JTextField(16));
        p2.add(new JTextField(16));
        p2.add(new JTextField(16));
        p2.add(new JTextField(16));
        p2.add(new JTextField(16));
        p2.add(new JTextField(16));
        p2.setFocusTraversalPolicyProvider(true);
        p2.setFocusCycleRoot(true);

        p2.setFocusTraversalKeys(
            KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS,
            Collections.<AWTKeyStroke>emptySet());
        p2.setFocusTraversalKeys(
            KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS,
            new HashSet<AWTKeyStroke>(Arrays.asList(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, InputEvent.SHIFT_DOWN_MASK))));
        p2.setFocusTraversalKeys(
            KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS,
            new HashSet<AWTKeyStroke>(Arrays.asList(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0))));

        add(p1);
        //p1.add(p2);
        add(p2);
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
        } catch (ClassNotFoundException | InstantiationException |
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
