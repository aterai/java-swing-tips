package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class MainPanel extends JPanel {
    private final JTextArea log = new JTextArea();
    public MainPanel() {
        super(new BorderLayout());
        final JPanel p = new JPanel();
        p.setBorder(BorderFactory.createTitledBorder("JFileChooser"));
        p.add(new JButton(new AbstractAction("readOnly") {
            @Override public void actionPerformed(ActionEvent ae) {
                UIManager.put("FileChooser.readOnly", Boolean.TRUE);
                JFileChooser fileChooser = new JFileChooser();
                int retvalue = fileChooser.showOpenDialog(p);
                if(retvalue==JFileChooser.APPROVE_OPTION) {
                    log.setText(fileChooser.getSelectedFile().getAbsolutePath());
                }
            }
        }));
        p.add(new JButton(new AbstractAction("Default") {
            @Override public void actionPerformed(ActionEvent ae) {
                UIManager.put("FileChooser.readOnly", Boolean.FALSE);
                JFileChooser fileChooser = new JFileChooser();
                int retvalue = fileChooser.showOpenDialog(p);
                if(retvalue==JFileChooser.APPROVE_OPTION) {
                    log.setText(fileChooser.getSelectedFile().getAbsolutePath());
                }
            }
        }));
        add(p, BorderLayout.NORTH);
        add(new JScrollPane(log));
        setPreferredSize(new Dimension(320, 160));
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
            //UIManager.put("FileChooser.readOnly", Boolean.TRUE);
        }catch(Exception e) {
            e.printStackTrace();
        }
        JFrame frame = new JFrame("@title@");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
