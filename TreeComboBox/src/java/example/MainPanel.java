package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.tree.*;

public final class MainPanel extends JPanel {
    private final TreeComboBox<DefaultMutableTreeNode> combo = new TreeComboBox<>();
    public MainPanel() {
        super(new BorderLayout());
        DefaultComboBoxModel<DefaultMutableTreeNode> model1 = new DefaultComboBoxModel<>();
        DefaultComboBoxModel<DefaultMutableTreeNode> model2 = new DefaultComboBoxModel<>();
        TreeModel tm = makeModel();
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) tm.getRoot();
//         Enumeration<?> depth = root.depthFirstEnumeration();
//         while (depth.hasMoreElements()) {
//             DefaultMutableTreeNode node = (DefaultMutableTreeNode) depth.nextElement();
//             if (node.isRoot()) {
//                 break;
//             }
//             model.insertElementAt(node, 0);
//         }
        makeComboBoxModel(model1, root);
        makeComboBoxModel(model2, root);
        combo.setModel(model2);
        combo.setSelectedIndex(-1);

        Box box = Box.createVerticalBox();
        box.add(createPanel(new JComboBox<>(model1), "default:"));
        box.add(Box.createVerticalStrut(5));
        box.add(createPanel(combo, "Tree ComboBoxModel:"));
        box.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        add(box, BorderLayout.NORTH);
        setPreferredSize(new Dimension(320, 240));
    }
    private static void makeComboBoxModel(DefaultComboBoxModel<DefaultMutableTreeNode> model, DefaultMutableTreeNode node) {
        if (!node.isRoot()) {
            model.addElement(node);
        }
        if (!node.isLeaf() && node.getChildCount() > 0) {
            Collections.list((Enumeration<?>) node.children()).stream()
                .filter(DefaultMutableTreeNode.class::isInstance)
                .map(DefaultMutableTreeNode.class::cast)
                .forEach(n -> makeComboBoxModel(model, n));
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
//         set2.add(new DefaultMutableTreeNode("asdfasdfas"));
//         set2.add(set3);
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
    public static void main(String... args) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                createAndShowGUI();
            }
        });
    }
    public static void createAndShowGUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException
               | IllegalAccessException | UnsupportedLookAndFeelException ex) {
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

class TreeComboBox<E extends TreeNode> extends JComboBox<E> {
    private boolean isNotSelectableIndex;
    private final Action up = new AbstractAction() {
        @Override public void actionPerformed(ActionEvent e) {
            int si = getSelectedIndex();
            for (int i = si - 1; i >= 0; i--) {
                //E o = getItemAt(i);
                //if (o instanceof TreeNode && ((TreeNode) o).isLeaf()) {
                if (getItemAt(i).isLeaf()) {
                    setSelectedIndex(i);
                    break;
                }
            }
        }
    };
    private final Action down = new AbstractAction() {
        @Override public void actionPerformed(ActionEvent e) {
            int si = getSelectedIndex();
            for (int i = si + 1; i < getModel().getSize(); i++) {
                if (getItemAt(i).isLeaf()) {
                    setSelectedIndex(i);
                    break;
                }
            }
        }
    };
    @Override public void updateUI() {
        super.updateUI();
        ListCellRenderer<? super E> cellRenderer = getRenderer();
        setRenderer(new ListCellRenderer<E>() {
            private final Icon h20Icon = new H20Icon();
            @Override public Component getListCellRendererComponent(JList<? extends E> list, E value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel c;
                if (value instanceof DefaultMutableTreeNode) {
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
                    int indent = index < 0 ? 0 : (node.getPath().length - 2) * 16;
                    if (node.isLeaf()) {
                        c = (JLabel) cellRenderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    } else {
                        c = (JLabel) cellRenderer.getListCellRendererComponent(list, value, index, false, false);
                        JLabel l = (JLabel) c;
                        l.setForeground(Color.WHITE);
                        l.setBackground(Color.GRAY.darker());
                    }
                    c.setBorder(BorderFactory.createEmptyBorder(0, 2 + indent, 0, 0));
                } else {
                    c = (JLabel) cellRenderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                }
                c.setIcon(index >= 0 ? h20Icon : null);
                return c;
            }
        });
        EventQueue.invokeLater(() -> {
            ActionMap am = getActionMap();
            am.put("selectPrevious3", up);
            am.put("selectNext3", down);
            InputMap im = getInputMap();
            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0),      "selectPrevious3");
            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_KP_UP, 0),   "selectPrevious3");
            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0),    "selectNext3");
            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_KP_DOWN, 0), "selectNext3");
        });
    }
    @Override public void setPopupVisible(boolean v) {
        if (!v && isNotSelectableIndex) {
            isNotSelectableIndex = false;
        } else {
            super.setPopupVisible(v);
        }
    }
    @Override public void setSelectedIndex(int index) {
        TreeNode node = getItemAt(index);
        if (Objects.nonNull(node) && node.isLeaf()) {
            super.setSelectedIndex(index);
        } else {
            isNotSelectableIndex = true;
        }
    }
}

class H20Icon implements Icon {
    @Override public void paintIcon(Component c, Graphics g, int x, int y) { /* Empty icon */ }
    @Override public int getIconWidth() {
        return 0;
    }
    @Override public int getIconHeight() {
        return 20;
    }
}
