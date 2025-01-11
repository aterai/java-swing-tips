// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Objects;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

public final class MainPanel extends JPanel {
  private transient Enumeration<TreePath> expandedState;

  private MainPanel() {
    super(new BorderLayout());
    DefaultMutableTreeNode root = makeTreeRoot();
    JTree tree = new JTree(new DefaultTreeModel(root));

    // // TEST:
    // FileSystemView fileSystemView = FileSystemView.getFileSystemView();
    // DefaultTreeModel treeModel = new DefaultTreeModel(root);
    // for (File fileSystemRoot : fileSystemView.getRoots()) {
    //   DefaultMutableTreeNode node = new DefaultMutableTreeNode(fileSystemRoot);
    //   root.add(node);
    //   for (File file : fileSystemView.getFiles(fileSystemRoot, true)) {
    //     if (file.isDirectory()) {
    //       node.add(new DefaultMutableTreeNode(file));
    //     }
    //   }
    // }
    // tree.setModel(treeModel);
    // tree.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
    // tree.setRootVisible(false);
    // tree.addTreeSelectionListener(new FolderSelectionListener(fileSystemView));
    // tree.setCellRenderer(new FileTreeCellRenderer(tree.getCellRenderer(), fileSystemView));
    // tree.expandRow(0);

    TreePath rootPath = new TreePath(root);

    JButton save = new JButton("Save");
    save.addActionListener(e -> setExpandedState(tree.getExpandedDescendants(rootPath)));

    JButton load = new JButton("Load");
    load.addActionListener(e -> {
      visitAll(tree, rootPath, false);
      if (Objects.isNull(expandedState)) {
        return;
      }
      Collections.list(getExpandedState()).forEach(tree::expandPath);
      setExpandedState(tree.getExpandedDescendants(rootPath));
    });

    JButton expand = new JButton("Expand");
    expand.addActionListener(e -> visitAll(tree, rootPath, true));

    JButton collapse = new JButton("Collapse");
    collapse.addActionListener(e -> visitAll(tree, rootPath, false));

    JPanel box = new JPanel(new GridLayout(1, 4));
    box.add(save);
    box.add(load);
    box.add(expand);
    box.add(collapse);
    add(box, BorderLayout.SOUTH);
    add(new JScrollPane(tree));
    setPreferredSize(new Dimension(320, 240));
  }

  public void setExpandedState(Enumeration<TreePath> state) {
    this.expandedState = state;
  }

  public Enumeration<TreePath> getExpandedState() {
    return expandedState;
  }

  public static void visitAll(JTree tree, TreePath parent, boolean expand) {
    TreeNode node = (TreeNode) parent.getLastPathComponent();
    if (!node.isLeaf()) {
      // Java 9: Collections.list(node.children())
      Collections.list((Enumeration<?>) node.children())
          .forEach(n -> visitAll(tree, parent.pathByAddingChild(n), expand));
    }
    if (expand) {
      tree.expandPath(parent);
    } else if (tree.isRootVisible() || Objects.nonNull(parent.getParentPath())) {
      tree.collapsePath(parent);
    }
  }

  public static DefaultMutableTreeNode makeTreeRoot() {
    DefaultMutableTreeNode set4 = new DefaultMutableTreeNode("Set 004");
    set4.add(new DefaultMutableTreeNode("22222222222"));
    set4.add(new DefaultMutableTreeNode("eee eee eee eee"));
    set4.add(new DefaultMutableTreeNode("bbb bbb bbb"));
    set4.add(new DefaultMutableTreeNode("zzz zz zz"));

    DefaultMutableTreeNode set1 = new DefaultMutableTreeNode("Set 001");
    set1.add(new DefaultMutableTreeNode("3333333333333333"));
    set1.add(new DefaultMutableTreeNode("111111111"));
    set1.add(new DefaultMutableTreeNode("22222222222"));
    set1.add(set4);
    set1.add(new DefaultMutableTreeNode("222222"));
    set1.add(new DefaultMutableTreeNode("222222222"));

    DefaultMutableTreeNode set2 = new DefaultMutableTreeNode("Set 002");
    set2.add(new DefaultMutableTreeNode("eee eee eee ee ee"));
    set2.add(new DefaultMutableTreeNode("bbb bbb"));

    DefaultMutableTreeNode set3 = new DefaultMutableTreeNode("Set 003");
    set3.add(new DefaultMutableTreeNode("zzz zz zz"));
    set3.add(new DefaultMutableTreeNode("aaa aaa aaa aaa"));
    set3.add(new DefaultMutableTreeNode("ccc ccc ccc"));

    DefaultMutableTreeNode root = new DefaultMutableTreeNode("Root");
    root.add(new DefaultMutableTreeNode("xxx xxx xxx xx xx"));
    root.add(set3);
    root.add(new DefaultMutableTreeNode("eee eee eee ee ee"));
    root.add(set1);
    root.add(set2);
    root.add(new DefaultMutableTreeNode("222222222222"));
    root.add(new DefaultMutableTreeNode("bbb bbb bbb bbb"));
    return root;
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (UnsupportedLookAndFeelException ignored) {
      Toolkit.getDefaultToolkit().beep();
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
      Logger.getGlobal().severe(ex::getMessage);
      return;
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
// //   private JFrame frame = null;
//   private final FileSystemView fileSystemView;
//   protected FolderSelectionListener(FileSystemView fileSystemView) {
//     this.fileSystemView = fileSystemView;
//   }
//
//   @Override public void valueChanged(TreeSelectionEvent e) {
//     JTree tree = (JTree) e.getSource();
//     DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.getPath().getLastPathComponent();
// //     if (Objects.isNull(frame)) {
// //       frame = (JFrame) SwingUtilities.getWindowAncestor(tree);
// //       frame.setGlassPane(new LockingGlassPane());
// //     }
// //     frame.getGlassPane().setVisible(true);
//
//     DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
//     // TreePath path = e.getPath();
//
//     if (!node.isLeaf()) {
//       return;
//     }
//     File parent = (File) node.getUserObject();
//     if (!parent.isDirectory()) {
//       return;
//     }
//
//     SwingWorker<String, File> worker = new SwingWorker<String, File>() {
//       @Override protected String doInBackground() throws InterruptedException {
//         File[] children = fileSystemView.getFiles(parent, true);
//         for (File child : children) {
//           if (child.isDirectory()) {
//             publish(child);
// //          Thread.sleep(500);
//           }
//         }
//         return "done";
//       }
//
//       @Override protected void process(List<File> chunks) {
//         for (File file : chunks) {
//           node.add(new DefaultMutableTreeNode(file));
//         }
//         model.reload(parent); // = model.nodeStructureChanged(parent);
//         // tree.expandPath(path);
//       }
//
// //        @Override protected void done() {
// //          // frame.getGlassPane().setVisible(false);
// //          // tree.setCursor(Cursor.getDefaultCursor());
// //        }
//     };
//     worker.execute();
//   }
// }
//
// class FileTreeCellRenderer extends DefaultTreeCellRenderer {
//   private final TreeCellRenderer renderer;
//   private final FileSystemView fileSystemView;
//   protected FileTreeCellRenderer(TreeCellRenderer renderer, FileSystemView fileSystemView) {
//     this.renderer = renderer;
//     this.fileSystemView = fileSystemView;
//   }
//
//   @Override public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
//     JLabel c = (JLabel) renderer.getTreeCellRendererComponent(
//         tree, value, selected, expanded, leaf, row, hasFocus);
//     if (selected) {
//       c.setOpaque(false);
//       c.setForeground(getTextSelectionColor());
//       // c.setBackground(Color.BLUE); // getBackgroundSelectionColor());
//     } else {
//       c.setOpaque(true);
//       c.setForeground(getTextNonSelectionColor());
//       c.setBackground(getBackgroundNonSelectionColor());
//     }
//     if (value instanceof DefaultMutableTreeNode) {
//       DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
//       Object o = node.getUserObject();
//       if (o instanceof File) {
//         File file = (File) o;
//         c.setIcon(fileSystemView.getSystemIcon(file));
//         c.setText(fileSystemView.getSystemDisplayName(file));
//         c.setToolTipText(file.getPath());
//       }
//     }
//     return c;
//   }
// }
