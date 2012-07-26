package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
//import javax.swing.plaf.basic.*;

public class MainPanel extends JPanel {
    public MainPanel() {
        super(new BorderLayout());
        add(makeUI(), BorderLayout.NORTH);
        setPreferredSize(new Dimension(320, 240));
    }
    @SuppressWarnings("unchecked")
    public JComponent makeUI() {
        Box p = Box.createVerticalBox();
        Object[] items = {"JComboBox 11111:", "JComboBox 222:", "JComboBox 33:"};

        JComboBox combo1 = new JComboBox(items) {
            @Override public void updateUI() {
                super.updateUI();
                JLabel r = (JLabel)getRenderer();
                r.setPreferredSize(new Dimension(0, 32));
            }
        };

        JComboBox combo2 = new JComboBox(items);
        final Dimension dim = ((JLabel)combo2.getRenderer()).getPreferredSize();
        combo2.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(
                    JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                c.setPreferredSize(new Dimension(100, index<0?dim.height:32));
                return c;
            }
        });

//         JComboBox combo3 = new JComboBox(items);
//         combo3.setUI(new BasicComboBoxUI() {
//             protected ComboPopup createPopup() {
//                 return new BasicComboPopup(combo) {
//                     protected void configureList() {
//                         super.configureList();
//                         list.setFixedCellHeight(60);
//                     }
//                 };
//             }
//         });
        p.add(makeTitledPanel("setPreferredSize", combo1));
        p.add(Box.createVerticalStrut(5));
        p.add(makeTitledPanel("getListCellRendererComponent", combo2));
        p.add(Box.createVerticalStrut(5));
        //p.add(combo3); p.add(Box.createVerticalStrut(5));
        //p.setBorder(BorderFactory.createTitledBorder("JComboBox"));
        return p;
    }
    private static JComponent makeTitledPanel(String title, JComponent c) {
        JPanel p = new JPanel(new BorderLayout());
        p.add(c);
        p.setBorder(BorderFactory.createTitledBorder(title));
        return p;
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
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
