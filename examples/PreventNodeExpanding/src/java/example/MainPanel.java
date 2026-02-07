// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.io.File;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Stream;
import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.filechooser.FileSystemView;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    FileSystemView fileSystemView = FileSystemView.getFileSystemView();
    DefaultMutableTreeNode root = new DefaultMutableTreeNode();
    DefaultTreeModel treeModel = new DefaultTreeModel(root);
    Stream.of(fileSystemView.getRoots()).forEach(fileSystemRoot -> {
      DefaultMutableTreeNode node = new DefaultMutableTreeNode(fileSystemRoot);
      root.add(node);
      Stream.of(fileSystemView.getFiles(fileSystemRoot, true))
          .filter(File::isDirectory)
          .map(DefaultMutableTreeNode::new)
          .forEach(node::add);
    });

    JTree tree = new JTree(treeModel) {
      @Override public void updateUI() {
        setCellRenderer(null);
        super.updateUI();
        setCellRenderer(new FileSystemTreeCellRenderer(fileSystemView));
      }
    };
    tree.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
    tree.setRootVisible(false);
    // java - File Browser GUI - Stack Overflow
    // https://stackoverflow.com/questions/6182110/file-browser-gui
    tree.addTreeSelectionListener(new FolderSelectionListener(fileSystemView));
    tree.expandRow(0);
    // tree.setToggleClickCount(1);

    tree.addTreeWillExpandListener(new DirectoryExpandVetoListener());

    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    add(new JScrollPane(tree));
    setPreferredSize(new Dimension(320, 240));
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
    frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    // frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

class FileSystemTreeCellRenderer implements TreeCellRenderer {
  private final DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
  private final FileSystemView fileSystemView;

  protected FileSystemTreeCellRenderer(FileSystemView fileSystemView) {
    this.fileSystemView = fileSystemView;
  }

  @Override public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
    Component c = renderer.getTreeCellRendererComponent(
        tree, value, selected, expanded, leaf, row, hasFocus);
    if (selected) {
      c.setForeground(renderer.getTextSelectionColor());
    } else {
      c.setForeground(renderer.getTextNonSelectionColor());
      c.setBackground(renderer.getBackgroundNonSelectionColor());
    }
    if (value instanceof DefaultMutableTreeNode && c instanceof JLabel) {
      DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
      Object o = node.getUserObject();
      if (o instanceof File) {
        File file = (File) o;
        c.setEnabled(!file.getName().startsWith("."));
        JLabel l = (JLabel) c;
        l.setOpaque(!selected);
        l.setIcon(fileSystemView.getSystemIcon(file));
        l.setText(fileSystemView.getSystemDisplayName(file));
        l.setToolTipText(file.getPath());
        // StringIndexOutOfBoundsException:
        // c.setEnabled(file.getName().codePointAt(0) != '.');
        // String name = file.getName();
        // c.setEnabled(name.isEmpty() || name.codePointAt(0) != '.');
      }
    }
    return c;
  }
}

class DirectoryExpandVetoListener implements TreeWillExpandListener {
  @Override public void treeWillExpand(TreeExpansionEvent e) throws ExpandVetoException {
    TreePath path = e.getPath();
    Object o = path.getLastPathComponent();
    if (o instanceof DefaultMutableTreeNode) {
      DefaultMutableTreeNode node = (DefaultMutableTreeNode) o;
      File file = (File) node.getUserObject();
      String name = file.getName();
      // System.out.println(name);
      if (!name.isEmpty() && name.codePointAt(0) == '.') {
        throw new ExpandVetoException(e, "Tree expansion cancelled");
      }
    }
  }

  @Override public void treeWillCollapse(TreeExpansionEvent e) { // throws ExpandVetoException {
    // throw new ExpandVetoException(e, "Tree collapse cancelled");
  }
}

class FolderSelectionListener implements TreeSelectionListener {
  // private JFrame frame = null;
  private final FileSystemView fileSystemView;

  protected FolderSelectionListener(FileSystemView fileSystemView) {
    this.fileSystemView = fileSystemView;
  }

  @Override public void valueChanged(TreeSelectionEvent e) {
    TreePath path = e.getPath();
    DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
    File parent = (File) node.getUserObject();
    if (node.isLeaf() && parent.isDirectory()) {
      JTree tree = (JTree) e.getSource();
      DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
      new BackgroundTask(fileSystemView, parent) {
        @Override protected void process(List<File> chunks) {
          if (tree.isDisplayable() && !isCancelled()) {
            chunks.stream().map(DefaultMutableTreeNode::new)
                .forEach(child -> {
                  int count = node.getChildCount();
                  model.insertNodeInto(child, node, count);
                });
            // model.reload(node);
          } else {
            cancel(true);
          }
        }
      }.execute();
    }
  }
}

class BackgroundTask extends SwingWorker<String, File> {
  private final FileSystemView fileSystemView;
  private final File parent;

  protected BackgroundTask(FileSystemView fileSystemView, File parent) {
    super();
    this.fileSystemView = fileSystemView;
    this.parent = parent;
  }

  @Override protected String doInBackground() {
    Stream.of(fileSystemView.getFiles(parent, true))
        .filter(File::isDirectory)
        .forEach(this::publish);
    return "done";
  }
}

// class FileTreeCellRenderer extends DefaultTreeCellRenderer {
//   private final TreeCellRenderer renderer;
//   private final FileSystemView fileSystemView;
//
//   protected FileTreeCellRenderer(TreeCellRenderer renderer, FileSystemView fileSystemView) {
//     super();
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
//         c.setEnabled(!file.getName().startsWith("."));
//         // String name = file.getName();
//         // c.setEnabled(name.isEmpty() || name.codePointAt(0) != '.');
//       }
//     }
//     return c;
//   }
// }
