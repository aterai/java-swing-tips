package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class MainPanel extends JPanel {
    private static final Color evenBGColor = new Color(225,255,225);
    private static final Color oddBGColor  = new Color(255,255,255);
    private final JComboBox<String> combo01 = makeComboBox();
    private final JComboBox<String> combo02 = makeComboBox();

    public MainPanel() {
        super(new BorderLayout());
//         // MetalLookAndFeel
//         combo01.setUI(new MetalComboBoxUI() {
//             @Override public PropertyChangeListener createPropertyChangeListener() {
//                 return new MetalPropertyChangeListener() {
//                     @Override public void propertyChange(PropertyChangeEvent e) {
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
        combo01.setSelectedIndex(0);
        combo01.setBackground(evenBGColor);

        JTextField field = (JTextField) combo02.getEditor().getEditorComponent();
        field.setOpaque(true);
        field.setBackground(evenBGColor);
        combo02.setEditable(true);
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
    private static Color getAlternateRowColor(int index) {
        return (index%2==0)?evenBGColor:oddBGColor;
    }
    private static class AlternateRowColorListCellRenderer extends DefaultListCellRenderer {
        @Override public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel cmp = (JLabel)super.getListCellRendererComponent(list,value,index,isSelected,cellHasFocus);
            cmp.setOpaque(true);
            if(!isSelected) {
                cmp.setBackground(getAlternateRowColor(index));
            }
            return cmp;
        }
    }
    private static JComboBox<String> makeComboBox() {
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
        model.addElement("aaaa");
        model.addElement("aaaabbb");
        model.addElement("aaaabbbcc");
        model.addElement("1234123512351234");
        model.addElement("bbb1");
        model.addElement("bbb12");

        JComboBox<String> combo = new JComboBox<>(model);
        combo.setRenderer(new AlternateRowColorListCellRenderer());
        combo.addItemListener(new ItemListener() {
            @Override public void itemStateChanged(ItemEvent e) {
                if(e.getStateChange()!=ItemEvent.SELECTED) {
                    return;
                }
                JComboBox cb = (JComboBox)e.getSource();
                Color rc = getAlternateRowColor(cb.getSelectedIndex());
                if(cb.isEditable()) {
                    JTextField field = (JTextField)cb.getEditor().getEditorComponent();
                    field.setBackground(rc);
                }else{
                    cb.setBackground(rc);
                }
            }
        });
        return combo;
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
