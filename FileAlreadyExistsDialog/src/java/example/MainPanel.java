package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;

public class MainPanel extends JPanel {
    private final JFileChooser fileChooser;
    private final JPanel p = new JPanel(new GridBagLayout());
    public MainPanel() {
        super(new BorderLayout());
        fileChooser = new JFileChooser() {
            @Override public void approveSelection() {
                File f = getSelectedFile();
                if(f.exists() && getDialogType() == SAVE_DIALOG) {
                    //@see https://forums.oracle.com/thread/1391852 How to react on events fired by a JFileChooser?
                    //@see http://stackoverflow.com/questions/3651494/jfilechooser-with-confirmation-dialog
                    //String m = "Replace file: " + f.getAbsolutePath() + "?";
                    //String m = "The file exists, overwrite?";
                    String m = String.format("<html>%s already exists.<br>Do you want to replace it?", f.getAbsolutePath());
                    int rv = JOptionPane.showConfirmDialog(this, m, "Save As", JOptionPane.YES_NO_OPTION);
                    if(rv!=JOptionPane.YES_OPTION) {
                        return;
                    }
                }
                super.approveSelection();
            }
        };
        p.setBorder(BorderFactory.createTitledBorder("JFileChooser#showSaveDialog(...)"));
        p.add(new JButton(new AbstractAction("Override JFileChooser#approveSelection()") {
            @Override public void actionPerformed(ActionEvent e) {
                int retvalue = fileChooser.showSaveDialog(p);
                if(retvalue==JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();
                    System.out.println(file);
                }
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
