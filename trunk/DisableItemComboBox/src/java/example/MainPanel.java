package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

public class MainPanel extends JPanel{
    private final MyComboBox combo = makeComboBox();
    private final JTextField field = new JTextField("1,2,5");
    public MainPanel() {
        super(new BorderLayout());

        combo.setDisableIndex(getDisableIndexFromTextField());

        Box box = Box.createHorizontalBox();
        box.add(new JLabel("Disabled Item Index:"));
        box.add(field);
        box.add(Box.createHorizontalStrut(2));
        box.add(new JButton(new AbstractAction("init") {
            @Override public void actionPerformed(ActionEvent e) {
                combo.setDisableIndex(getDisableIndexFromTextField());
            }
        }));
        add(box, BorderLayout.SOUTH);
        add(combo, BorderLayout.NORTH);
        setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        setPreferredSize(new Dimension(320, 200));
    }

    @SuppressWarnings("unchecked")
    private static MyComboBox makeComboBox() {
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        model.addElement("0000000000000");
        model.addElement("111111");
        model.addElement("222222222222");
        model.addElement("33");
        model.addElement("4444444444444444");
        model.addElement("555555555555555555555555");
        model.addElement("6666666666");
        MyComboBox combo = new MyComboBox();
        combo.setModel(model);
        return combo;
    }
    private HashSet<Integer> getDisableIndexFromTextField() {
        StringTokenizer st = new StringTokenizer(field.getText(), ",");
        HashSet<Integer> set = new HashSet<Integer>();
        try{
            while(st.hasMoreTokens()) {
                set.add(new Integer(st.nextToken().trim()));
            }
        }catch(NumberFormatException nfe) {
            Toolkit.getDefaultToolkit().beep();
            JOptionPane.showMessageDialog(field, "invalid value.\n"+nfe.getMessage(),
                                          "Error", JOptionPane.ERROR_MESSAGE);
        }
        return set;
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

class MyComboBox extends JComboBox {
    @SuppressWarnings("unchecked")
    public MyComboBox() {
        super();
        setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList list, Object value,
                                 int index, boolean isSelected, boolean cellHasFocus) {
                Component c;
                if(disableIndexSet.contains(index)) {
                    c = super.getListCellRendererComponent(list,value,index,false,false);
                    c.setEnabled(false);
                }else{
                    c = super.getListCellRendererComponent(list,value,index,isSelected,cellHasFocus);
                    c.setEnabled(true);
                }
                return c;
            }
        });
        Action up = new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                int si = getSelectedIndex();
                for(int i = si-1;i>=0;i--) {
                    if(!disableIndexSet.contains(i)) {
                        setSelectedIndex(i);
                        break;
                    }
                }
            }
        };
        Action down = new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                int si = getSelectedIndex();
                for(int i = si+1;i<getModel().getSize();i++) {
                    if(!disableIndexSet.contains(i)) {
                        setSelectedIndex(i);
                        break;
                    }
                }
            }
        };
        ActionMap am = getActionMap();
        am.put("selectPrevious3", up);
        am.put("selectNext3", down);
        InputMap im = getInputMap();
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0),      "selectPrevious3");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_KP_UP, 0),   "selectPrevious3");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0),    "selectNext3");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_KP_DOWN, 0), "selectNext3");
    }
    private final HashSet<Integer> disableIndexSet = new HashSet<Integer>();
    private boolean isDisableIndex = false;
    public void setDisableIndex(HashSet<Integer> set) {
        disableIndexSet.clear();
        for(Integer i:set) {
            disableIndexSet.add(i);
        }
    }
    @Override public void setPopupVisible(boolean v) {
        if(!v && isDisableIndex) {
            isDisableIndex = false;
        }else{
            super.setPopupVisible(v);
        }
    }
    @Override public void setSelectedIndex(int index) {
        if(disableIndexSet.contains(index)) {
            isDisableIndex = true;
        }else{
            //isDisableIndex = false;
            super.setSelectedIndex(index);
        }
    }
}
