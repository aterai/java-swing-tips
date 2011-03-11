package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class MainPanel extends JPanel {
    public MainPanel() {
        super(new BorderLayout());
        JComboBox combobox1 = makeTestComboBox();
        JComboBox combobox2 = makeTestComboBox();
        combobox2.setEditable(true);

        Box box1 = Box.createVerticalBox();
        box1.setBorder(BorderFactory.createTitledBorder("setEditable(false)"));
        box1.add(combobox1);

        Box box2 = Box.createVerticalBox();
        box2.setBorder(BorderFactory.createTitledBorder("setEditable(true)"));
        box2.add(combobox2);

        setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        add(box1, BorderLayout.NORTH);
        add(box2, BorderLayout.SOUTH);
        setPreferredSize(new Dimension(320, 200));
    }
    private static JComboBox makeTestComboBox() {
        JComboBox combobox = new JComboBox();
        combobox.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                MyItem item = (MyItem)value;
                JLabel label = (JLabel)super.getListCellRendererComponent(list,item,index,isSelected,cellHasFocus);
                if(index!=-1 && item.hasSeparator()) {
                    label.setBorder(BorderFactory.createMatteBorder(1,0,0,0,Color.GRAY));
                }else{
                    label.setBorder(BorderFactory.createEmptyBorder());
                }
                return label;
            }
        });
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        model.addElement(new MyItem("aaaa"));
        model.addElement(new MyItem("aaaabbb"));
        model.addElement(new MyItem("aaaabbbcc"));
        model.addElement(new MyItem("eeeeeeeee", true));
        model.addElement(new MyItem("bbb1"));
        model.addElement(new MyItem("bbb12"));
        combobox.setModel(model);
        return combobox;
    }

    static class MyItem{
        private final String  item;
        private final boolean flag;
        public MyItem(String str) {
            this(str, false);
        }
        public MyItem(String str, boolean flg) {
            item = str;
            flag = flg;
        }
        public String toString() {
            return item;
        }
        public boolean hasSeparator() {
            return flag;
        }
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
