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

        JButton button1 = new JButton("noPlacesBar");
        button1.addActionListener(e -> {
            UIManager.put("FileChooser.noPlacesBar", Boolean.TRUE);
            JFileChooser fileChooser = new JFileChooser();
            int retvalue = fileChooser.showOpenDialog(getRootPane());
            if (retvalue == JFileChooser.APPROVE_OPTION) {
                log.setText(fileChooser.getSelectedFile().getAbsolutePath());
            }
        });

        JButton button2 = new JButton("Default");
        button2.addActionListener(e -> {
            UIManager.put("FileChooser.noPlacesBar", Boolean.FALSE);
            JFileChooser fileChooser = new JFileChooser();
            int retvalue = fileChooser.showOpenDialog(getRootPane());
            if (retvalue == JFileChooser.APPROVE_OPTION) {
                log.setText(fileChooser.getSelectedFile().getAbsolutePath());
            }
        });

        JPanel p = new JPanel();
        p.setBorder(BorderFactory.createTitledBorder("JFileChooser"));
        p.add(button1);
        p.add(button2);
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
            //UIManager.put("FileChooser.readOnly", Boolean.TRUE);
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
