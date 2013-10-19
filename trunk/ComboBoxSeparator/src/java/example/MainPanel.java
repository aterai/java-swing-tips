package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class MainPanel extends JPanel {
    public MainPanel() {
        super(new BorderLayout());
        Box box = Box.createVerticalBox();
        box.add(makeComboBox());
        box.setBorder(BorderFactory.createTitledBorder("ComboBoxSeparator"));
        add(box, BorderLayout.NORTH);
        add(new JScrollPane(new JTextArea("dummy")));
        setPreferredSize(new Dimension(320, 200));
    }
    @SuppressWarnings("unchecked")
    private static JComboBox makeComboBox() {
        DefaultComboBoxModel model = new DefaultComboBoxModel() {
            @Override public void setSelectedItem(Object anObject) {
                if(!(anObject instanceof JSeparator)) {
                    super.setSelectedItem(anObject);
                }
            }
        };
        model.addElement("aaaa");
        model.addElement("aaaabbb");
        model.addElement("aaaabbbcc");
        model.addElement("aaaabbbccddddddd");
        model.addElement(new JSeparator());
        model.addElement("bbb1");
        model.addElement("bbb12");
        model.addElement("bbb33333");
        model.addElement(new JSeparator());
        model.addElement("11111");
        model.addElement("2222222");

        JComboBox combo = new JComboBox(model);
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                if(value instanceof JSeparator) {
                    return (Component)value;
                }else{
                    return super.getListCellRendererComponent(list,value,index,isSelected,cellHasFocus);
                }
            }
        });
        Action up = new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                JComboBox cb = (JComboBox)e.getSource();
                int index = cb.getSelectedIndex();
                if(index==0) return;
                Object o = cb.getItemAt(index-1);
                if(o instanceof JSeparator) {
                    cb.setSelectedIndex(index-2);
                }else{
                    cb.setSelectedIndex(index-1);
                }
            }
        };
        Action down = new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                JComboBox cb = (JComboBox)e.getSource();
                int index = cb.getSelectedIndex();
                if(index==cb.getItemCount()-1) return;
                Object o = cb.getItemAt(index+1);
                if(o instanceof JSeparator) {
                    cb.setSelectedIndex(index+2);
                }else{
                    cb.setSelectedIndex(index+1);
                }
            }
        };
        ActionMap am = combo.getActionMap();
        am.put("selectPrevious3", up);
        am.put("selectNext3", down);

        InputMap im = combo.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0),      "selectPrevious3");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_KP_UP, 0),   "selectPrevious3");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0),    "selectNext3");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_KP_DOWN, 0), "selectNext3");

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
