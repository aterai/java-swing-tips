package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());

        JTextArea log = new JTextArea();
        // TEST: UIManager.put("OptionPane.isYesLast", Boolean.TRUE);

        JButton defaultButton = new JButton("Default");
        defaultButton.addActionListener(e -> {
            UIManager.put("OptionPane.buttonOrientation", SwingConstants.CENTER);
            String v = JOptionPane.showInputDialog(getRootPane(), "OptionPane.buttonOrientation: CENTER");
            log.setText(v);
        });

        JButton rightButton = new JButton("RIGHT");
        rightButton.addActionListener(e -> {
            UIManager.put("OptionPane.buttonOrientation", SwingConstants.RIGHT);
            String v = JOptionPane.showInputDialog(getRootPane(), "OptionPane.buttonOrientation: RIGHT");
            log.setText(v);
        });

        JButton leftButton = new JButton("LEFT");
        leftButton.addActionListener(e -> {
            UIManager.put("OptionPane.buttonOrientation", SwingConstants.LEFT);
            String v = JOptionPane.showInputDialog(getRootPane(), "OptionPane.buttonOrientation: LEFT");
            log.setText(v);
        });

        JPanel p = new JPanel();
        p.setBorder(BorderFactory.createTitledBorder("JOptionPane"));
        p.add(defaultButton);
        p.add(rightButton);
        p.add(leftButton);
        add(p, BorderLayout.NORTH);
        add(new JScrollPane(log));
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
