package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class MainPanel extends JPanel{
    private static final Color evenBGColor = new Color(225,255,225);
    private static final Color oddBGColor  = new Color(255,255,255);
    private final JComboBox combo01 = new JComboBox();
    private final JComboBox combo02 = new JComboBox();

    public MainPanel() {
        super(new BorderLayout());
//         // MetalLookAndFeel
//         combo01.setUI(new MetalComboBoxUI() {
//             @Override
//             public PropertyChangeListener createPropertyChangeListener() {
//                 return new MetalPropertyChangeListener() {
//                     @Override
//                     public void propertyChange(PropertyChangeEvent e) {
//                         String propertyName = e.getPropertyName();
//                         if(propertyName=="background") {
//                             Color color = (Color)e.getNewValue();
//                             //arrowButton.setBackground(color);
//                             listBox.setBackground(color);
//                         }else{
//                             super.propertyChange( e );
//                         }
//                     }
//                 };
//             }
//         });
        combo01.setModel(makeModel());
        combo01.setRenderer(new MyListCellRenderer(combo01.getRenderer()));
        combo01.addItemListener(new ItemListener() {
            @Override public void itemStateChanged(ItemEvent e) {
                if(e.getStateChange()!=ItemEvent.SELECTED) return;
                combo01.setBackground(getOEColor(combo01.getSelectedIndex()));
            }
        });
        combo01.setSelectedIndex(0);
        combo01.setBackground(evenBGColor);

        final JTextField field = (JTextField) combo02.getEditor().getEditorComponent();
        field.setOpaque(true);
        field.setBackground(evenBGColor);
        combo02.setEditable(true);
        combo02.setModel(makeModel());
        combo02.setRenderer(new MyListCellRenderer(combo02.getRenderer()));
        combo02.addItemListener(new ItemListener() {
            @Override public void itemStateChanged(ItemEvent e) {
                if(e.getStateChange()!=ItemEvent.SELECTED) return;
                field.setBackground(getOEColor(combo02.getSelectedIndex()));
            }
        });
        combo02.setSelectedIndex(0);

        Box box = Box.createVerticalBox();
        box.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        box.add(makePanel("setEditable(false)", combo01));
        box.add(Box.createVerticalStrut(5));
        box.add(makePanel("setEditable(true)",  combo02));

        add(box, BorderLayout.NORTH);
        setPreferredSize(new Dimension(320,200));
    }
    private static JPanel makePanel(String title, JComponent c) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder(title));
        p.add(c);
        return p;
    }
    private static class MyListCellRenderer extends DefaultListCellRenderer {
        private final ListCellRenderer lcr;
        public MyListCellRenderer(ListCellRenderer lcr) {
            this.lcr = lcr;
        }
        @Override public Component getListCellRendererComponent(JList list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            JLabel cmp = (JLabel)lcr.getListCellRendererComponent(list,value,index,isSelected,cellHasFocus);
            cmp.setOpaque(true);
            if(!isSelected) {
                cmp.setBackground(getOEColor(index));
            }
            return cmp;
        }
    }

    private static Color getOEColor(int index) {
        return (index%2==0)?evenBGColor:oddBGColor;
    }
    private static DefaultComboBoxModel makeModel() {
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        model.addElement("aaaa");
        model.addElement("aaaabbb");
        model.addElement("aaaabbbcc");
        model.addElement("1234123512351234");
        model.addElement("bbb1");
        model.addElement("bbb12");
        return model;
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
// class ColorListCellRenderer implements ListCellRenderer {
//     private static final Color evenBGColor = new Color(225,255,225);
//     private static final Color oddBGColor  = new Color(255,255,255);
//     private final ListCellRenderer lcr;
//     private final JComponent c;
//     public ColorListCellRenderer(JComponent c, ListCellRenderer lcr) {
//         this.c = c;
//         this.lcr = lcr;
//         c.setOpaque(true);
//     }
//     public Component getListCellRendererComponent(JList list, Object value, int index,
//                                                   boolean isSelected, boolean cellHasFocus) {
//         JLabel l = (JLabel)lcr.getListCellRendererComponent(list,value,index,isSelected,cellHasFocus);
//         l.setOpaque(true);
//         if(index<0) {
//             c.setBackground(getOEColor(list.getSelectedIndex()));
//         }else if(!isSelected) {
//             l.setBackground(getOEColor(index));
//         }
//         return l;
//     }
//     private static Color getOEColor(int index) {
//         return (index%2==0)?evenBGColor:oddBGColor;
//     }
// }
