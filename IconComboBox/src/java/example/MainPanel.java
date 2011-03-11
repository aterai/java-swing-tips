package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;

class MainPanel extends JPanel {
    private final ImageIcon image;
    public MainPanel() {
        super(new GridLayout(2,1));
        image = new ImageIcon(getClass().getResource("16x16.png"));

        JComboBox combo01 = new JComboBox(makeModel());

        JComboBox combo02 = new JComboBox(makeModel());
        combo02.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                JLabel cmp = (JLabel)super.getListCellRendererComponent(list,value,index,isSelected,cellHasFocus);
                cmp.setIcon(image);
                return cmp;
            }
        });

        JComboBox combo03 = new JComboBox(makeModel());
        combo03.setEditable(true);
        combo03.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                JLabel cmp = (JLabel)super.getListCellRendererComponent(list,value,index,isSelected,cellHasFocus);
                cmp.setIcon(image);
                return cmp;
            }
        });

        JComboBox combo04 = new JComboBox(makeModel());
        combo04.setEditable(true);
        combo04.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                JLabel cmp = (JLabel)super.getListCellRendererComponent(list,value,index,isSelected,cellHasFocus);
                cmp.setIcon(image);
                return cmp;
            }
        });
        combo04.setBorder(makeIconComboBorder(combo04, image));

        add(makeTitlePanel("setEditable(false)", Arrays.asList(combo01, combo02)));
        add(makeTitlePanel("setEditable(false)", Arrays.asList(combo03, combo04)));
        setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        setPreferredSize(new Dimension(320, 200));
    }
    private static Border makeIconComboBorder(JComponent comp, final ImageIcon icon) {
        final int w = icon.getIconWidth();
        final int h = icon.getIconHeight();
        final Insets is = comp.getInsets();
        final int ch = comp.getPreferredSize().height-is.top-is.bottom;
        final int yy = (ch-h>0)?(ch-h)/2:0;
        ImageIcon wrappedicon = new ImageIcon() {
            @Override public int getIconWidth()  { return w;  }
            @Override public int getIconHeight() { return ch; }
            @Override public void paintIcon(Component c, Graphics g, int x, int y) {
                g.translate(x, y);
                g.drawImage(icon.getImage(), 0, yy, c);
                g.translate(-x, -y);
            }
        };
        Border b1 = BorderFactory.createMatteBorder(0,w,0,0,wrappedicon);
        Border b2 = BorderFactory.createEmptyBorder(0,5,0,0);
        Border b3 = BorderFactory.createCompoundBorder(b1, b2);
        return BorderFactory.createCompoundBorder(comp.getBorder(), b3);
    }
    private static DefaultComboBoxModel makeModel() {
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        model.addElement("aaaa");
        model.addElement("aaaabbb");
        model.addElement("aaaabbbcc");
        model.addElement("ccccccccccccccc");
        model.addElement("bbb1");
        model.addElement("bbb12");
        return model;
    }
    private JComponent makeTitlePanel(String title, java.util.List<? extends JComponent> list) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createTitledBorder(title));
        GridBagConstraints c = new GridBagConstraints();
        c.fill    = GridBagConstraints.HORIZONTAL;
        c.insets  = new Insets(5, 5, 5, 5);
        c.weightx = 1.0;
        c.gridy   = 0;
        for(JComponent cmp:list) {
            p.add(cmp, c);
            c.gridy++;
        }
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
