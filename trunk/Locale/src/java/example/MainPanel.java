package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private static final Locale[] LOCALE_ARRAY = {
        Locale.ENGLISH,
        Locale.FRENCH,
        Locale.GERMAN,
        Locale.ITALIAN,
        Locale.JAPANESE,
        Locale.KOREAN,
        Locale.CHINESE,
        Locale.SIMPLIFIED_CHINESE,
        Locale.TRADITIONAL_CHINESE,
        Locale.FRANCE,
        Locale.GERMANY,
        Locale.ITALY,
        Locale.JAPAN,
        Locale.KOREA,
        Locale.CHINA,
        Locale.PRC,
        Locale.TAIWAN,
        Locale.UK,
        Locale.US,
        Locale.CANADA,
        Locale.CANADA_FRENCH,
    };
    private final JComboBox<Locale> combo = new JComboBox<>(LOCALE_ARRAY);
    private final JPanel panel = new JPanel(new BorderLayout(5, 5));
    private final JFileChooser fileChooser = new JFileChooser();
    public MainPanel() {
        super(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        UIManager.put("FileChooser.readOnly", Boolean.TRUE);
        //Locale.setDefault(new Locale("en", "US"));
        //Locale defaultLocale = JFileChooser.getDefaultLocale();
        //JFileChooser.setDefaultLocale(defaultLocale);
        //JFileChooser.setDefaultLocale(Locale.ENGLISH);
        //JFileChooser.setDefaultLocale(new Locale("en", "US"));
        //fileChooser.setLocale(new Locale("fr", "FR"));

        panel.setBorder(BorderFactory.createTitledBorder("Open JFileChooser"));
        panel.add(combo);
        panel.add(new JButton(new AbstractAction("<-") {
            @Override public void actionPerformed(ActionEvent e) {
                fileChooser.setLocale((Locale)combo.getSelectedItem());
                SwingUtilities.updateComponentTreeUI(fileChooser); //fileChooser.updateUI();
                int retvalue = fileChooser.showOpenDialog(panel);
                System.out.println(retvalue);
            }
        }), BorderLayout.EAST);
        add(panel, BorderLayout.NORTH);
        setPreferredSize(new Dimension(320, 240));
    }
//     private static void printLocale(JFileChooser fileChooser) {
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
        }catch(ClassNotFoundException | InstantiationException |
               IllegalAccessException | UnsupportedLookAndFeelException ex) {
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
