package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;

public class MainPanel extends JPanel {
    private final JFileChooser fileChooser = new JFileChooser();
    public MainPanel() {
        super(new BorderLayout());
        fileChooser.addChoosableFileFilter(new javax.swing.filechooser.FileFilter() {
            @Override public boolean accept(File file) {
                if(file.isDirectory()) return true;
                if(file.getName().toLowerCase().endsWith(".png")) return true;
                return false;
            }
            @Override public String getDescription() {
                return "PNG(*.png)";
            }
        });
        fileChooser.addChoosableFileFilter(new javax.swing.filechooser.FileFilter() {
            @Override public boolean accept(File file) {
                if(file.isDirectory()) return true;
                if(file.getName().toLowerCase().endsWith(".jpg")) return true;
                return false;
            }
            @Override public String getDescription() {
                return "JPEG(*.jpg)";
            }
        });
        //JDK 6
        //FileFilter filter = new FileNameExtensionFilter("JPEG(*.jpg)", "jpg", "jpeg");
        //fileChooser.addChoosableFileFilter(filter);

        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder("JFileChooser#showOpenDialog(...)"));
        p.add(new JButton(new AbstractAction("showOpenDialog") {
            @Override public void actionPerformed(ActionEvent ae) {
                int retvalue = fileChooser.showOpenDialog(MainPanel.this);
                System.out.println(retvalue);
                //if(retvalue==JFileChooser.APPROVE_OPTION) {
                //    File file = fileChooser.getSelectedFile();
                //    ((DefaultComboBoxModel)combo1.getModel()).insertElementAt(file.getAbsolutePath(), 0);
                //    combo1.setSelectedIndex(0);
                //}
            }
        }));
        add(p);
        setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
        setPreferredSize(new Dimension(320, 200));
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
            UIManager.put("FileChooser.readOnly", Boolean.TRUE);
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
