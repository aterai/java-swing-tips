package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.Enumeration;
import javax.swing.*;
import javax.swing.tree.*;

public final class MainPanel extends JPanel {
    private final JCheckBox folderCheck = new JCheckBox("OpenIcon, ClosedIcon");
    private final JCheckBox leafCheck   = new JCheckBox("LeafIcon");
    //private final Icon emptyIcon = new EmptyIcon();
    private final JTree tree = new JTree();
    public MainPanel() {
        super(new BorderLayout());

        for (int i = 0; i < tree.getRowCount(); i++) {
            tree.expandRow(i);
        }

        DefaultTreeCellRenderer r = (DefaultTreeCellRenderer) tree.getCellRenderer();
        r.setOpenIcon(null);
        r.setClosedIcon(null);
        r.setLeafIcon(null);
        allNodesChanged(tree);

        folderCheck.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                DefaultTreeCellRenderer r = (DefaultTreeCellRenderer) tree.getCellRenderer();
                if (((JCheckBox) e.getSource()).isSelected()) {
                    r.setOpenIcon(r.getDefaultOpenIcon());
                    r.setClosedIcon(r.getDefaultClosedIcon());
                } else {
                    r.setOpenIcon(null);
                    r.setClosedIcon(null);
                }
                allNodesChanged(tree);
            }
        });
        leafCheck.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                DefaultTreeCellRenderer r = (DefaultTreeCellRenderer) tree.getCellRenderer();
                if (((JCheckBox) e.getSource()).isSelected()) {
                    r.setLeafIcon(r.getDefaultLeafIcon());
                } else {
                    r.setLeafIcon(null);
                }
                allNodesChanged(tree);
            }
        });

//         tree.setCellRenderer(new DefaultTreeCellRenderer() {
//             @Override public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
//                 super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
//                 if (leaf) {
//                     if (!leafCheck.isSelected()) {
//                         setIcon(null);
//                     } else {
//                         setIcon(getDefaultLeafIcon());
//                     }
//                 } else {
//                     if (!folderCheck.isSelected()) {
//                         setIcon(null);
//                     } else {
//                         if (expanded) {
//                             setIcon(getDefaultOpenIcon());
//                         } else {
//                             setIcon(getDefaultClosedIcon());
//                         }
//                     }
//                 }
//                 return this;
//             }
//         });

        JPanel np = new JPanel();
        np.add(folderCheck);
        np.add(leafCheck);

        add(np, BorderLayout.NORTH);
        add(new JScrollPane(tree));
        setPreferredSize(new Dimension(320, 240));
    }

    private static void allNodesChanged(JTree tree) {
        DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();
        Enumeration depth = root.depthFirstEnumeration();
        while (depth.hasMoreElements()) {
            model.nodeChanged((TreeNode) depth.nextElement());
        }
        //tree.revalidate();
        //tree.repaint();
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
// class EmptyIcon implements Icon, Serializable {
//     @Override public void paintIcon(Component c, Graphics g, int x, int y) { /* Empty icon */ }
//     @Override public int getIconWidth() { return 0; }
//     @Override public int getIconHeight() { return 0; }
// }
