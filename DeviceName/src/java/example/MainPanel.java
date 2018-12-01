package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Objects;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout(10, 10));
        String deviceName = "con.txt";

        JButton b1 = new JButton("c:/" + deviceName);
        b1.addActionListener(e -> {
            File file = new File(deviceName);
            try {
                if (file.createNewFile()) {
                    System.out.println("the named file does not exist and was successfully created.");
                } else {
                    System.out.println("the named file already exists.");
                }
            } catch (IOException ex) {
                ex.printStackTrace();
                Object[] obj = {ex.getMessage()};
                JOptionPane.showMessageDialog(getRootPane(), obj, "Error", JOptionPane.INFORMATION_MESSAGE);
            }
            // JFileChooser fileChooser = new JFileChooser();
            // int retvalue = fileChooser.showOpenDialog(getRootPane());
            // if (retvalue == JFileChooser.APPROVE_OPTION) {
            //     File file = fileChooser.getSelectedFile();
            //     System.out.println(file.getAbsolutePath());
            //     try {
            //         file.createNewFile();
            //         file.deleteOnExit();
            //     } catch (IOException ex) {
            //         ex.printStackTrace();
            //     }
            // }
        });
        Component p1 = makeTitledPanel("IOException: before 1.5", b1);

        JButton b2 = new JButton("c:/" + deviceName + ":getCanonicalPath");
        b2.addActionListener(e -> {
            File file = new File(deviceName);
            if (!isCanonicalPath(file)) {
                Object[] obj = {file.getAbsolutePath() + " is not a canonical path."};
                JOptionPane.showMessageDialog(getRootPane(), obj, "Error", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        Component p2 = makeTitledPanel("getCanonicalPath: before 1.5", b2);

        JButton b3 = new JButton("c:/" + deviceName + ":isFile");
        b3.addActionListener(e -> {
            File file = new File(deviceName);
            if (!file.isFile()) {
                Object[] obj = {file.getAbsolutePath() + " is not a file."};
                JOptionPane.showMessageDialog(getRootPane(), obj, "Error", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        Component p3 = makeTitledPanel("isFile: JDK 1.5+", b3);

        JPanel p = new JPanel(new GridLayout(3, 1, 10, 10));
        p.add(p1);
        p.add(p2);
        p.add(p3);
        add(p, BorderLayout.NORTH);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setPreferredSize(new Dimension(320, 240));
    }
    // Before 1.5
    public static boolean isCanonicalPath(File file) {
        if (Objects.isNull(file)) {
            return false;
        }
        try {
            if (Objects.isNull(file.getCanonicalPath())) {
                return false;
            }
        } catch (IOException ex) {
            return false;
        }
        return true;
    }
    private static Component makeTitledPanel(String title, Component c) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder(title));
        p.add(c);
        return p;
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
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
