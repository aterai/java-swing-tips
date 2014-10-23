package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
// import java.io.*;
import java.util.*;
// import java.util.List;
import javax.swing.*;
// import javax.swing.event.*;
// import javax.swing.filechooser.FileSystemView;
import javax.swing.tree.*;

public final class MainPanel extends JPanel {
//*
    private final DefaultMutableTreeNode root = makeTreeRoot();
    private final JTree tree = new JTree(new DefaultTreeModel(root));
/*/
    private final DefaultMutableTreeNode root = new DefaultMutableTreeNode();
    private final JTree tree = new JTree();
//*/
    private Enumeration<TreePath> expandedState;
    public MainPanel() {
        super(new BorderLayout());
//         //TEST:
//         FileSystemView fileSystemView = FileSystemView.getFileSystemView();
//         DefaultTreeModel treeModel = new DefaultTreeModel(root);
//         for (File fileSystemRoot: fileSystemView.getRoots()) {
//             DefaultMutableTreeNode node = new DefaultMutableTreeNode(fileSystemRoot);
//             root.add(node);
//             for (File file: fileSystemView.getFiles(fileSystemRoot, true)) {
//                 if (file.isDirectory()) {
//                     node.add(new DefaultMutableTreeNode(file));
//                 }
//             }
//         }
//         tree.setModel(treeModel);
//         tree.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
//         tree.setRootVisible(false);
//         tree.addTreeSelectionListener(new FolderSelectionListener(fileSystemView));
//         tree.setCellRenderer(new FileTreeCellRenderer(tree.getCellRenderer(), fileSystemView));
//         tree.expandRow(0);

        final TreePath rootPath = new TreePath(root);
        JPanel box = new JPanel(new GridLayout(1, 4));
        box.add(new JButton(new AbstractAction("Save") {
            @Override public void actionPerformed(ActionEvent e) {
                expandedState = tree.getExpandedDescendants(rootPath);
            }
        }));
        box.add(new JButton(new AbstractAction("Load") {
            @Override public void actionPerformed(ActionEvent e) {
                visitAll(tree, rootPath, false);
                if (expandedState == null) {
                    return;
                }
                while (expandedState.hasMoreElements()) {
                    tree.expandPath(expandedState.nextElement());
                }
                expandedState = tree.getExpandedDescendants(rootPath);
            }
        }));
        box.add(new JButton(new AbstractAction("Expand") {
            @Override public void actionPerformed(ActionEvent e) {
                visitAll(tree, rootPath, true);
            }
        }));
        box.add(new JButton(new AbstractAction("Collapse") {
            @Override public void actionPerformed(ActionEvent e) {
                visitAll(tree, rootPath, false);
                //tree.expandPath(rootPath);
            }
        }));
        add(box, BorderLayout.SOUTH);
        add(new JScrollPane(tree));
        setPreferredSize(new Dimension(320, 240));
    }
    private static void visitAll(JTree tree, TreePath parent, boolean expand) {
        TreeNode node = (TreeNode) parent.getLastPathComponent();
        if (!node.isLeaf() && node.getChildCount() >= 0) {
            Enumeration e = node.children();
            while (e.hasMoreElements()) {
                TreeNode n = (TreeNode) e.nextElement();
                TreePath path = parent.pathByAddingChild(n);
                visitAll(tree, path, expand);
            }
        }
        if (expand) {
            tree.expandPath(parent);
        } else if (tree.isRootVisible() || parent.getParentPath() != null) {
            tree.collapsePath(parent);
        }
    }

    private static DefaultMutableTreeNode makeTreeRoot() {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Root");
        DefaultMutableTreeNode set1 = new DefaultMutableTreeNode("Set 001");
        DefaultMutableTreeNode set2 = new DefaultMutableTreeNode("Set 002");
        DefaultMutableTreeNode set3 = new DefaultMutableTreeNode("Set 003");
        DefaultMutableTreeNode set4 = new DefaultMutableTreeNode("Set 004");

        set1.add(new DefaultMutableTreeNode("3333333333333333"));
        set1.add(new DefaultMutableTreeNode("111111111"));
        set1.add(new DefaultMutableTreeNode("22222222222"));
        set1.add(set4);
        set1.add(new DefaultMutableTreeNode("222222"));
        set1.add(new DefaultMutableTreeNode("222222222"));
        set2.add(new DefaultMutableTreeNode("eeeeeeeeeeeee"));
        set2.add(new DefaultMutableTreeNode("bbbbbbbbbbbb"));
        set3.add(new DefaultMutableTreeNode("zzzzzzz"));
        set3.add(new DefaultMutableTreeNode("aaaaaaaaaaaa"));
        set3.add(new DefaultMutableTreeNode("ccccccccc"));

        set4.add(new DefaultMutableTreeNode("22222222222"));
        set4.add(new DefaultMutableTreeNode("eeeeeeeeeeeee"));
        set4.add(new DefaultMutableTreeNode("bbbbbbbbbbbb"));
        set4.add(new DefaultMutableTreeNode("zzzzzzz"));

        root.add(new DefaultMutableTreeNode("xxxxxxxxxxxxx"));
        root.add(set3);
        root.add(new DefaultMutableTreeNode("eeeeeeeeeeeee"));
        root.add(set1);
        root.add(set2);
        root.add(new DefaultMutableTreeNode("222222222222"));
        root.add(new DefaultMutableTreeNode("bbbbbbbbbbbb"));
        return root;
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
        } catch (ClassNotFoundException | InstantiationException |
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

// class FolderSelectionListener implements TreeSelectionListener {
// //     private JFrame frame = null;
//     private final FileSystemView fileSystemView;
//     public FolderSelectionListener(FileSystemView fileSystemView) {
//         this.fileSystemView = fileSystemView;
//     }
//     @Override public void valueChanged(TreeSelectionEvent e) {
//         final JTree tree = (JTree) e.getSource();
//         final DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.getPath().getLastPathComponent();
// //         if (frame == null) {
// //             frame = (JFrame) SwingUtilities.getWindowAncestor(tree);
// //             frame.setGlassPane(new LockingGlassPane());
// //         }
// //         frame.getGlassPane().setVisible(true);
//
//         final DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
//         //final TreePath path = e.getPath();
//
//         if (!node.isLeaf()) { return; }
//         final File parent = (File) node.getUserObject();
//         if (!parent.isDirectory()) return;
//
//         SwingWorker<String, File> worker = new SwingWorker<String, File>() {
//             @Override public String doInBackground() {
//                 File[] children = fileSystemView.getFiles(parent, true);
//                 for (File child: children) {
//                     if (child.isDirectory()) {
//                         publish(child);
// //                         try {
// //                             Thread.sleep(500);
// //                         } catch (InterruptedException ex) {
// //                             ex.printStackTrace();
// //                         }
//                     }
//                 }
//                 return "done";
//             }
//             @Override protected void process(List<File> chunks) {
//                 for (File file: chunks) {
//                     node.add(new DefaultMutableTreeNode(file));
//                 }
//                 model.reload(parent); //= model.nodeStructureChanged(parent);
//                 //tree.expandPath(path);
//             }
// //              @Override public void done() {
// //                  //frame.getGlassPane().setVisible(false);
// //                  //tree.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
// //              }
//         };
//         worker.execute();
//     }
// }
//
// class FileTreeCellRenderer extends DefaultTreeCellRenderer {
//     private final TreeCellRenderer renderer;
//     private final FileSystemView fileSystemView;
//     public FileTreeCellRenderer(TreeCellRenderer renderer, FileSystemView fileSystemView) {
//         this.renderer = renderer;
//         this.fileSystemView = fileSystemView;
//     }
//     @Override public Component getTreeCellRendererComponent(JTree tree, Object value, boolean isSelected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
//         JLabel c = (JLabel) renderer.getTreeCellRendererComponent(tree, value, isSelected, expanded, leaf, row, hasFocus);
//         if (isSelected) {
//             c.setOpaque(false);
//             c.setForeground(getTextSelectionColor());
//             //c.setBackground(Color.BLUE); //getBackgroundSelectionColor());
//         } else {
//             c.setOpaque(true);
//             c.setForeground(getTextNonSelectionColor());
//             c.setBackground(getBackgroundNonSelectionColor());
//         }
//         if (value instanceof DefaultMutableTreeNode) {
//             DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
//             Object o = node.getUserObject();
//             if (o instanceof File) {
//                 File file = (File) o;
//                 c.setIcon(fileSystemView.getSystemIcon(file));
//                 c.setText(fileSystemView.getSystemDisplayName(file));
//                 c.setToolTipText(file.getPath());
//             }
//         }
//         return c;
//     }
// }
