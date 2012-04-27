package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.tree.*;

public class MainPanel extends JPanel{
    private final TreeComboBox combo = new TreeComboBox();
    private final JTextField field = new JTextField("1,2,5");
    @SuppressWarnings("unchecked")
    public MainPanel() {
        super(new BorderLayout());
        DefaultComboBoxModel model1 = new DefaultComboBoxModel();
        DefaultComboBoxModel model2 = new DefaultComboBoxModel();
        TreeModel tm = makeModel();
        DefaultMutableTreeNode root = (DefaultMutableTreeNode)tm.getRoot();
//         Enumeration depth = root.depthFirstEnumeration();
//         while(depth.hasMoreElements()) {
//             DefaultMutableTreeNode node = (DefaultMutableTreeNode)depth.nextElement();
//             if(node.isRoot()) break;
//             model.insertElementAt(node, 0);
//         }
        makeComboBoxModel(model1, root);
        makeComboBoxModel(model2, root);
        combo.setModel(model2);
        combo.setSelectedIndex(-1);
        //combo.setMaximumRowCount(100);

        Box box = Box.createVerticalBox();
        box.add(createPanel(new JComboBox(model1), "default:"));
        box.add(Box.createVerticalStrut(5));
        box.add(createPanel(combo, "Tree ComboBoxModel:"));
        box.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        add(box, BorderLayout.NORTH);
        setPreferredSize(new Dimension(320, 240));
    }
    @SuppressWarnings("unchecked")
    private static void makeComboBoxModel(DefaultComboBoxModel model, DefaultMutableTreeNode node) {
        if(!node.isRoot()) {
            model.addElement(node);
        }
        if(!node.isLeaf() && node.getChildCount()>=0) {
            Enumeration e = node.children();
            while(e.hasMoreElements()) {
                makeComboBoxModel(model, (DefaultMutableTreeNode)e.nextElement());
            }
        }
    }
    private static TreeModel makeModel() {
        return new JTree().getModel();
//         DefaultMutableTreeNode root = new DefaultMutableTreeNode("Root");
//         DefaultMutableTreeNode set1 = new DefaultMutableTreeNode("Set 001");
//         DefaultMutableTreeNode set2 = new DefaultMutableTreeNode("Set 002");
//         DefaultMutableTreeNode set3 = new DefaultMutableTreeNode("Set 003");
//         set1.add(new DefaultMutableTreeNode("111111111"));
//         set1.add(new DefaultMutableTreeNode("22222222222"));
//         set1.add(new DefaultMutableTreeNode("33333"));
//         set2.add(set3);
//         set2.add(new DefaultMutableTreeNode("asdfasdfas"));
//         set2.add(new DefaultMutableTreeNode("asdf"));
//         set3.add(new DefaultMutableTreeNode("asdfasdfasdf"));
//         set3.add(new DefaultMutableTreeNode("qwerqwer"));
//         set3.add(new DefaultMutableTreeNode("zvxcvzxcvzxzxcvzxcv"));
//         root.add(set1);
//         root.add(set2);
//         return new DefaultTreeModel(root);
    }
    private static JComponent createPanel(JComponent cmp, String str) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(str));
        panel.add(cmp);
        return panel;
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

class TreeComboBox extends JComboBox {
    //private JTree tree = new JTree();
    @SuppressWarnings("unchecked")
    public TreeComboBox() {
        super();
        setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JComponent c;
                if(value instanceof DefaultMutableTreeNode) {
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
                    int indent = 2 + (index<0?0:(node.getPath().length-2)*16);
                    //String str = node.toString();
                    if(node.isLeaf()) {
                        c = (JComponent)super.getListCellRendererComponent(list,value,index,isSelected,cellHasFocus);
                    }else{
                        c = (JComponent)super.getListCellRendererComponent(list,value,index,false,false);
                        //c = (JComponent)tree.getCellRenderer().getTreeCellRendererComponent(tree,value,isSelected,true,false,0,cellHasFocus);
                        JLabel l = (JLabel)c;
                        l.setForeground(Color.WHITE);
                        l.setBackground(Color.GRAY.darker());
                    }
                    c.setBorder(BorderFactory.createEmptyBorder(0,indent,0,0));
                }else{
                    c = (JComponent)super.getListCellRendererComponent(list,value,index,isSelected,cellHasFocus);
                }
                return c;
            }
        });
        Action up = new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                int si = getSelectedIndex();
                for(int i = si-1;i>=0;i--) {
                    Object o = getItemAt(i);
                    if(o instanceof TreeNode && ((TreeNode)o).isLeaf()) {
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
                    Object o = getItemAt(i);
                    if(o instanceof TreeNode && ((TreeNode)o).isLeaf()) {
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
    private boolean isNotSelectableIndex = false;
    @Override public void setPopupVisible(boolean v) {
        if(!v && isNotSelectableIndex) {
            isNotSelectableIndex = false;
        }else{
            super.setPopupVisible(v);
        }
    }
    @Override public void setSelectedIndex(int index) {
        Object o = getItemAt(index);
        if(o instanceof TreeNode && !((TreeNode)o).isLeaf()) {
            isNotSelectableIndex = true;
        }else{
            super.setSelectedIndex(index);
        }
    }
}
