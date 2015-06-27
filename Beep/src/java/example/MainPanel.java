package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private final JButton button = new JButton(new AbstractAction("showMessageDialog") {
        @Override public void actionPerformed(ActionEvent e) {
            Toolkit.getDefaultToolkit().beep();
            JButton b = (JButton) e.getSource();
            JOptionPane.showMessageDialog(b.getRootPane(), "Error Message", "Title", JOptionPane.ERROR_MESSAGE);
        }
    });
    public MainPanel() {
        super(new BorderLayout());
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder("Toolkit.getDefaultToolkit().beep()"));
        p.add(button);
        add(p);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
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
