package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;

public class MainPanel extends JPanel {
    public MainPanel() {
        super(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        UIManager.put("FileChooser.readOnly", Boolean.TRUE);
        //Locale.setDefault(new Locale("en", "US"));

        DefaultComboBoxModel model = new DefaultComboBoxModel();
        model.addElement(Locale.ENGLISH);
        model.addElement(Locale.FRENCH);
        model.addElement(Locale.GERMAN);
        model.addElement(Locale.ITALIAN);
        model.addElement(Locale.JAPANESE);
        model.addElement(Locale.KOREAN);
        model.addElement(Locale.CHINESE);
        model.addElement(Locale.SIMPLIFIED_CHINESE);
        model.addElement(Locale.TRADITIONAL_CHINESE);
        model.addElement(Locale.FRANCE);
        model.addElement(Locale.GERMANY);
        model.addElement(Locale.ITALY);
        model.addElement(Locale.JAPAN);
        model.addElement(Locale.KOREA);
        model.addElement(Locale.CHINA);
        model.addElement(Locale.PRC);
        model.addElement(Locale.TAIWAN);
        model.addElement(Locale.UK);
        model.addElement(Locale.US);
        model.addElement(Locale.CANADA);
        model.addElement(Locale.CANADA_FRENCH);
        //model.addElement(Locale.ROOT);
        final JComboBox combo = new JComboBox(model);

        Box box = Box.createHorizontalBox();
        box.setBorder(BorderFactory.createEmptyBorder(0,0,5,0));
        box.add(new JLabel("Open JFileChooser"));
//         final Locale defaultLocale = JFileChooser.getDefaultLocale();
//         box.add(new JButton(new AbstractAction(defaultLocale.toString()) {
//             public void actionPerformed(ActionEvent ae) {
//                 JFileChooser.setDefaultLocale(defaultLocale);
//                 JFileChooser fileChooser = new JFileChooser();
//                 int retvalue = fileChooser.showOpenDialog(MainPanel.this);
//             }
//         }));
//         box.add(new JButton(new AbstractAction("en_US") {
//             public void actionPerformed(ActionEvent ae) {
//                 //JFileChooser.setDefaultLocale(Locale.ENGLISH);
//                 JFileChooser.setDefaultLocale(new Locale("en", "US"));
//                 JFileChooser fileChooser = new JFileChooser();
//                 int retvalue = fileChooser.showOpenDialog(MainPanel.this);
//             }
//         }));
        box.add(Box.createHorizontalStrut(200));
        //box.add(Box.createHorizontalGlue());

        add(box, BorderLayout.NORTH);
        add(combo);

        final JFileChooser fileChooser = new JFileChooser();
        add(new JButton(new AbstractAction("<-") {
            @Override public void actionPerformed(ActionEvent ae) {
                //JFileChooser fileChooser = new JFileChooser();
                //fileChooser.setLocale(new Locale("fr", "FR"));
                fileChooser.setLocale((Locale)combo.getSelectedItem());
                fileChooser.updateUI();
                int retvalue = fileChooser.showOpenDialog(MainPanel.this);
                System.out.println(retvalue);
            }
        }), BorderLayout.EAST);
    }
//     private void printLocale(JFileChooser fileChooser) {
//         System.out.println("Locale: "+fileChooser.getLocale());
//         System.out.println("DefaultLocale: "+fileChooser.getDefaultLocale());
//     }
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
