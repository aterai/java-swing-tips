// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Stream;
import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.filechooser.FileSystemView;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeCellRenderer;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    FileSystemView fileSystemView = FileSystemView.getFileSystemView();
    DefaultMutableTreeNode root = new DefaultMutableTreeNode();
    Stream.of(fileSystemView.getRoots()).forEach(fileSystemRoot -> {
      DefaultMutableTreeNode node = new DefaultMutableTreeNode(fileSystemRoot);
      root.add(node);
      Stream.of(fileSystemView.getFiles(fileSystemRoot, true))
          .filter(File::isDirectory)
          .map(DefaultMutableTreeNode::new)
          .forEach(node::add);
    });

    JTree tree = new JTree(root) {
      @Override public void updateUI() {
        setCellRenderer(null);
        super.updateUI();
        setCellRenderer(new FileTreeCellRenderer(fileSystemView));
      }
    };
    tree.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
    tree.setRootVisible(false);
    ToolTipManager.sharedInstance().registerComponent(tree);
    tree.addTreeSelectionListener(new FolderSelectionListener(fileSystemView));
    tree.expandRow(0);

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
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

class FileTreeCellRenderer implements TreeCellRenderer {
  private final DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
  private final FileSystemView fileSystemView;

  protected FileTreeCellRenderer(FileSystemView fileSystemView) {
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
      JLabel l = (JLabel) c;
      l.setOpaque(!selected);
      Object o = node.getUserObject();
      if (o instanceof File) {
        File file = (File) o;
        l.setIcon(fileSystemView.getSystemIcon(file));
        l.setText(fileSystemView.getSystemDisplayName(file));
        l.setToolTipText(file.getPath());
      }
    }
    return c;
  }
}

class FolderSelectionListener implements TreeSelectionListener {
  private final FileSystemView fileSystemView;

  protected FolderSelectionListener(FileSystemView fileSystemView) {
    this.fileSystemView = fileSystemView;
  }

  @Override public void valueChanged(TreeSelectionEvent e) {
    Object c = e.getSource();
    Object o = e.getPath().getLastPathComponent();
    if (e.isAddedPath() && c instanceof JTree && o instanceof DefaultMutableTreeNode) {
      DefaultMutableTreeNode node = (DefaultMutableTreeNode) o;
      Object userObject = node.getUserObject();
      if (node.isLeaf() && userObject instanceof File) {
        resolveLinkLocation((File) userObject)
            .filter(File::isDirectory)
            .ifPresent(dir -> insertBackground((JTree) c, dir, node));
      }
    }
  }

  private void insertBackground(JTree tree, File dir, MutableTreeNode node) {
    DefaultTreeModel m = (DefaultTreeModel) tree.getModel();
    new BackgroundTask(fileSystemView, dir) {
      @Override protected void process(List<File> chunks) {
        if (tree.isDisplayable() && !isCancelled()) {
          chunks.stream()
              .map(DefaultMutableTreeNode::new)
              .forEach(c -> m.insertNodeInto(c, node, node.getChildCount()));
        } else {
          cancel(true);
        }
      }
    }.execute();
  }

  // Java 8: sun.awt.shell.ShellFolder
  // Java 9 or later: --add-exports=java.desktop/sun.awt.shell=ALL-UNNAMED
  private static Optional<File> resolveLinkLocation(File file) {
    Optional<File> location;
    try {
      sun.awt.shell.ShellFolder sf = sun.awt.shell.ShellFolder.getShellFolder(file);
      location = Optional.ofNullable(sf.isLink() ? sf.getLinkLocation() : file);
    } catch (FileNotFoundException ex) {
      location = Optional.empty();
    }
    return location;
  }

  // // Java 9 or later: FileSystemView#isLink(File), FileSystemView#getLinkLocation(File)
  // private Optional<File> resolveLinkLocation(File file) {
  //   Optional<File> location;
  //   try {
  //     boolean isLink = fileSystemView.isLink(file);
  //     location = Optional.ofNullable(isLink ? fileSystemView.getLinkLocation(file) : file);
  //   } catch (FileNotFoundException ex) {
  //     location = Optional.empty();
  //   }
  //   return location;
  // }
}

class BackgroundTask extends SwingWorker<String, File> {
  private final FileSystemView fileSystemView;
  private final File parent;

  protected BackgroundTask(FileSystemView fileSystemView, File parent) {
    super();
    this.fileSystemView = fileSystemView;
    this.parent = parent;
  }

  @Override public String doInBackground() {
    Stream.of(fileSystemView.getFiles(parent, true))
        .filter(File::isDirectory)
        .forEach(this::publish);
    return "done";
  }
}
