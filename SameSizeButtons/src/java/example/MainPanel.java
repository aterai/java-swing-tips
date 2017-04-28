package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());

        JCheckBox borderCheck = new JCheckBox("OptionPane.buttonAreaBorder");
        JOptionPane op = new JOptionPane("message", JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION);

        JButton button1 = new JButton("default");
        button1.addActionListener(e -> {
            UIManager.getLookAndFeelDefaults().put("OptionPane.sameSizeButtons", false);
            //JOptionPane.showConfirmDialog(getRootPane(), "message");
            //JOptionPane pane1 = new JOptionPane("message", JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION);
            UIDefaults d = new UIDefaults();
            d.put("OptionPane.sameSizeButtons", false);
            op.putClientProperty("Nimbus.Overrides", d);
            op.putClientProperty("Nimbus.Overrides.InheritDefaults", true);
            SwingUtilities.updateComponentTreeUI(op);
            op.createDialog(getRootPane(), "title").setVisible(true);
        });

        JButton button2 = new JButton("sameSizeButtons");
        button2.addActionListener(e -> {
            //UIManager.getLookAndFeelDefaults().put("OptionPane.sameSizeButtons", true);
            //UIManager.put("OptionPane.buttonAreaBorder", BorderFactory.createLineBorder(Color.RED, 10));
            //JOptionPane.showConfirmDialog(getRootPane(), "message");
            UIDefaults d = new UIDefaults();
            if (borderCheck.isSelected()) {
                d.put("OptionPane.buttonAreaBorder", BorderFactory.createLineBorder(Color.RED, 10));
            } else {
                d.put("OptionPane.buttonAreaBorder", BorderFactory.createEmptyBorder());
            }
            d.put("OptionPane.sameSizeButtons", true);
            op.putClientProperty("Nimbus.Overrides", d);
            op.putClientProperty("Nimbus.Overrides.InheritDefaults", true);
            SwingUtilities.updateComponentTreeUI(op);
            op.createDialog(getRootPane(), "title").setVisible(true);
        });

        JPanel p1 = new JPanel();
        p1.add(button1);
        p1.add(button2);

        JPanel p2 = new JPanel();
        p2.add(borderCheck);

        add(p1, BorderLayout.NORTH);
        add(p2, BorderLayout.SOUTH);
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
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
            //UIManager.getLookAndFeelDefaults().put("OptionPane.sameSizeButtons", true);
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
