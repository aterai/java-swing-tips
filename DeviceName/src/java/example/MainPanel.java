package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.Objects;
import javax.swing.*;

public class MainPanel extends JPanel {
    protected static final String DEVICE_NAME = "con.txt";
    public MainPanel() {
        super(new BorderLayout(10, 10));
        JPanel p = new JPanel(new GridLayout(3, 1, 10, 10));
        p.add(makeTitledPanel("IOException", new JButton(new AbstractAction("c:/con.txt") {
            @Override public void actionPerformed(ActionEvent e) {
                File file = new File(DEVICE_NAME);
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
//                 JFileChooser fileChooser = new JFileChooser();
//                 int retvalue = fileChooser.showOpenDialog(getRootPane());
//                 if (retvalue == JFileChooser.APPROVE_OPTION) {
//                     File file = fileChooser.getSelectedFile();
//                     System.out.println(file.getAbsolutePath());
//                     try {
//                         file.createNewFile();
//                         file.deleteOnExit();
//                     } catch (IOException ex) {
//                         ex.printStackTrace();
//                     }
//                 }
            }
        })));
        p.add(makeTitledPanel("getCanonicalPath", new JButton(new AbstractAction("c:/con.txt:getCanonicalPath") {
            @Override public void actionPerformed(ActionEvent e) {
                File file = new File(DEVICE_NAME);
                if (!isCanonicalPath(file)) {
                    Object[] obj = {file.getAbsolutePath() + " is not a canonical path."};
                    JOptionPane.showMessageDialog(getRootPane(), obj, "Error", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        })));
        p.add(makeTitledPanel("isFile: JDK 1.5+ ", new JButton(new AbstractAction("c:/con.txt:isFile") {
            @Override public void actionPerformed(ActionEvent e) {
                File file = new File(DEVICE_NAME);
                if (!file.isFile()) {
                    Object[] obj = {file.getAbsolutePath() + " is not a file."};
                    JOptionPane.showMessageDialog(getRootPane(), obj, "Error", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        })));
        add(p, BorderLayout.NORTH);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setPreferredSize(new Dimension(320, 240));
    }
    protected boolean isCanonicalPath(File file) {
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
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
