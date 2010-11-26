package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

public class MainPanel extends JPanel{
    private final JList list = new JList();
    private final DefaultListModel model = new DefaultListModel();
    private final JTextField field = new JTextField("1,2,5");
    private final HashSet<Integer> disableIndexSet = new HashSet<Integer>();

    public MainPanel() {
        super(new BorderLayout(5,5));
        final ListCellRenderer r = list.getCellRenderer();
        list.setCellRenderer(new ListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList list, Object value,
                                 int index, boolean isSelected, boolean cellHasFocus) {
                Component c;
                if(disableIndexSet.contains(index)) {
                    c = r.getListCellRendererComponent(list,value,index,false,false);
                    c.setEnabled(false);
                }else{
                    c = r.getListCellRendererComponent(list,value,index,isSelected,cellHasFocus);
                }
                return c;
            }
        });

//         list.setSelectionModel(new DefaultListSelectionModel() {
//             public boolean isSelectedIndex(int index) {
//                 if(disableIndexSet.contains(index)) return false;
//                 return super.isSelectedIndex(index);
//             }
//         });

        list.setModel(model);
        model.addElement("aaaaaaaaaaaa");
        model.addElement("bbbbbbbbbbbbbbbbbb");
        model.addElement("ccccccccccc");
        model.addElement("dddddddddddd");
        model.addElement("eeeeeeeeeeeeeeeeeee");
        model.addElement("fffffffffffffffffffffff");
        model.addElement("ggggggggg");

        ActionMap am = list.getActionMap();
        am.put("selectNextRow", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent ae) {
                int index = list.getSelectedIndex();
                for(int i = index+1;i<list.getModel().getSize();i++) {
                    if(!disableIndexSet.contains(i)) {
                        list.setSelectedIndex(i);
                        break;
                    }
                }
            }
        });
        am.put("selectPreviousRow", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent ae) {
                int index = list.getSelectedIndex();
                for(int i = index-1;i>=0;i--) {
                    if(!disableIndexSet.contains(i)) {
                        list.setSelectedIndex(i);
                        break;
                    }
                }
            }
        });
        initDisableIndex(disableIndexSet);

        Box box = Box.createHorizontalBox();
        box.add(new JLabel("Disabled Item Index:"));
        box.add(field);
        box.add(Box.createHorizontalStrut(2));
        box.add(new JButton(new AbstractAction("init") {
            @Override public void actionPerformed(ActionEvent e) {
                initDisableIndex(disableIndexSet);
                list.repaint();
            }
        }));

        add(new JScrollPane(list));
        add(box, BorderLayout.NORTH);
        setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        setPreferredSize(new Dimension(320, 200));
    }
    private void initDisableIndex(HashSet<Integer> set) {
        StringTokenizer st = new StringTokenizer(field.getText(), ",");
        set.clear();
        try{
            while(st.hasMoreTokens()) {
                set.add(new Integer(st.nextToken().trim()));
            }
        }catch(NumberFormatException nfe) {
            java.awt.Toolkit.getDefaultToolkit().beep();
            JOptionPane.showMessageDialog(field, "invalid value.\n"+nfe.getMessage(),
                                          "Error", JOptionPane.ERROR_MESSAGE);
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
