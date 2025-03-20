// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.EventObject;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.filechooser.FileSystemView;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeModel;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    FileSystemView fileSystemView = FileSystemView.getFileSystemView();
    DefaultMutableTreeNode root = new DefaultMutableTreeNode();
    DefaultTreeModel treeModel = new DefaultTreeModel(root);
    Stream.of(fileSystemView.getRoots()).forEach(fileSystemRoot -> {
      CheckBoxNode check = new CheckBoxNode(fileSystemRoot, Status.DESELECTED);
      DefaultMutableTreeNode node = new DefaultMutableTreeNode(check);
      root.add(node);
      Stream.of(fileSystemView.getFiles(fileSystemRoot, true))
          .filter(File::isDirectory)
          .map(file -> new CheckBoxNode(file, Status.DESELECTED))
          .map(DefaultMutableTreeNode::new)
          .forEach(node::add);
    });
    treeModel.addTreeModelListener(new CheckBoxStatusUpdateListener());

    JTree tree = new JTree(treeModel) {
      @Override public void updateUI() {
        setCellRenderer(null);
        setCellEditor(null);
        super.updateUI();
        // ???#1: JDK 1.6.0 bug??? Nimbus LnF
        setCellRenderer(new FileTreeCellRenderer(fileSystemView));
        setCellEditor(new CheckBoxNodeEditor(fileSystemView));
      }
    };
    tree.setRootVisible(false);
    tree.addTreeSelectionListener(new FolderSelectionListener(fileSystemView));

    tree.setEditable(true);
    tree.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

    tree.expandRow(0);
    // tree.setToggleClickCount(1);

    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    add(new JScrollPane(tree));
    // // TEST
    // add(new JButton(new AbstractAction("test") {
    //   private void searchTreeForCheckedNode(TreePath path) {
    //     Object o = path.getLastPathComponent();
    //     if (!(o instanceof DefaultMutableTreeNode)) {
    //       return;
    //     }
    //     DefaultMutableTreeNode node = (DefaultMutableTreeNode) o;
    //     o = node.getUserObject();
    //     if (!(o instanceof CheckBoxNode)) {
    //       return;
    //     }
    //     CheckBoxNode check = (CheckBoxNode) o;
    //     if (check.getStatus() == Status.SELECTED) {
    //       System.out.println(check.getFile().toString());
    //     } else if (check.getStatus() == Status.INDETERMINATE && !node.isLeaf()) {
    //       // Java 9: Enumeration<TreeNode> e = node.children();
    //       Enumeration<?> e = node.children();
    //       while (e.hasMoreElements()) {
    //         searchTreeForCheckedNode(path.pathByAddingChild(e.nextElement()));
    //       }
    //     }
    //   }
    //
    //   @Override public void actionPerformed(ActionEvent e) {
    //     System.out.println("------------------");
    //     searchTreeForCheckedNode(tree.getPathForRow(0));
    //     // DefaultMutableTreeNode root = (DefaultMutableTreeNode) treeModel.getRoot();
    //     // // Java 9: Enumeration<TreeNode> e = root.breadthFirstEnumeration();
    //     // Enumeration<?> e = root.breadthFirstEnumeration();
    //     // while (e.hasMoreElements()) {
    //     //   DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.nextElement();
    //     //   CheckBoxNode check = (CheckBoxNode) node.getUserObject();
    //     //   if (Objects.nonNull(check) && check.getStatus() == Status.SELECTED) {
    //     //     System.out.println(check.getFile().toString());
    //     //   }
    //     // }
    //   }
    // }), BorderLayout.SOUTH);

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
      ex.printStackTrace();
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

class TriStateCheckBox extends JCheckBox {
  @Override public void updateUI() {
    Icon currentIcon = getIcon();
    setIcon(null);
    super.updateUI();
    if (Objects.nonNull(currentIcon)) {
      setIcon(new IndeterminateIcon());
    }
    setOpaque(false);
  }
}

class IndeterminateIcon implements Icon {
  // TEST: private static final Color FOREGROUND = UIManager.getColor("CheckBox.foreground");
  private static final Color FOREGROUND = new Color(0xC8_32_14_FF, true);
  private static final int MARGIN = 4;
  private static final int HEIGHT = 2;
  private final Icon icon = UIManager.getIcon("CheckBox.icon");

  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.translate(x, y);
    icon.paintIcon(c, g2, 0, 0);
    g2.setPaint(FOREGROUND);
    g2.fillRect(MARGIN, (getIconHeight() - HEIGHT) / 2, getIconWidth() - MARGIN - MARGIN, HEIGHT);
    g2.dispose();
  }

  @Override public int getIconWidth() {
    return icon.getIconWidth();
  }

  @Override public int getIconHeight() {
    return icon.getIconHeight();
  }
}

enum Status {
  SELECTED, DESELECTED, INDETERMINATE
}

class CheckBoxNode {
  private final File file;
  private final Status status;

  protected CheckBoxNode(File file) {
    this.file = file;
    this.status = Status.INDETERMINATE;
  }

  protected CheckBoxNode(File file, Status status) {
    this.file = file;
    this.status = status;
  }

  @Override public String toString() {
    return file.getName();
  }

  public File getFile() {
    return file;
  }

  public Status getStatus() {
    return status;
  }
}

class FileTreeCellRenderer implements TreeCellRenderer {
  private final JPanel panel = new JPanel(new BorderLayout());
  private final DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
  private final TriStateCheckBox checkBox = new TriStateCheckBox();
  private final FileSystemView fileSystemView;

  protected FileTreeCellRenderer(FileSystemView fileSystemView) {
    super();
    this.fileSystemView = fileSystemView;
    panel.setFocusable(false);
    panel.setRequestFocusEnabled(false);
    panel.setOpaque(false);
    panel.add(checkBox, BorderLayout.WEST);
    checkBox.setOpaque(false);
  }

  @Override public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
    Component c = renderer.getTreeCellRendererComponent(
        tree, value, selected, expanded, leaf, row, hasFocus);
    if (value instanceof DefaultMutableTreeNode && c instanceof JLabel) {
      checkBox.setEnabled(tree.isEnabled());
      checkBox.setFont(tree.getFont());
      Object userObject = ((DefaultMutableTreeNode) value).getUserObject();
      if (userObject instanceof CheckBoxNode) {
        CheckBoxNode node = (CheckBoxNode) userObject;
        if (node.getStatus() == Status.INDETERMINATE) {
          checkBox.setIcon(new IndeterminateIcon());
        } else {
          checkBox.setIcon(null);
        }
        File file = node.getFile();
        JLabel l = (JLabel) c;
        l.setIcon(fileSystemView.getSystemIcon(file));
        l.setText(fileSystemView.getSystemDisplayName(file));
        l.setToolTipText(file.getPath());
        checkBox.setSelected(node.getStatus() == Status.SELECTED);
      }
      panel.add(c);
      c = panel;
    }
    c.setFont(tree.getFont());
    return c;
  }
}

class CheckBoxNodeEditor extends AbstractCellEditor implements TreeCellEditor {
  private final JPanel panel = new JPanel(new BorderLayout());
  private final DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
  private final TriStateCheckBox checkBox = new TriStateCheckBox();
  private final transient FileSystemView fileSystemView;
  private File file;

  protected CheckBoxNodeEditor(FileSystemView fileSystemView) {
    super();
    this.fileSystemView = fileSystemView;
    checkBox.setOpaque(false);
    checkBox.setFocusable(false);
    checkBox.addActionListener(e -> stopCellEditing());
    panel.setFocusable(false);
    panel.setRequestFocusEnabled(false);
    panel.setOpaque(false);
    panel.add(checkBox, BorderLayout.WEST);
  }

  @Override public Component getTreeCellEditorComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row) {
    Component c = renderer.getTreeCellRendererComponent(
        tree, value, true, expanded, leaf, row, true);
    if (value instanceof DefaultMutableTreeNode && c instanceof JLabel) {
      checkBox.setEnabled(tree.isEnabled());
      checkBox.setFont(tree.getFont());
      Object userObject = ((DefaultMutableTreeNode) value).getUserObject();
      if (userObject instanceof CheckBoxNode) {
        CheckBoxNode node = (CheckBoxNode) userObject;
        if (node.getStatus() == Status.INDETERMINATE) {
          checkBox.setIcon(new IndeterminateIcon());
        } else {
          checkBox.setIcon(null);
        }
        file = node.getFile();
        JLabel l = (JLabel) c;
        l.setIcon(fileSystemView.getSystemIcon(file));
        l.setText(fileSystemView.getSystemDisplayName(file));
        checkBox.setSelected(node.getStatus() == Status.SELECTED);
      }
      panel.add(c);
      c = panel;
    }
    c.setFont(tree.getFont());
    return c;
  }

  @Override public Object getCellEditorValue() {
    return new CheckBoxNode(file, checkBox.isSelected() ? Status.SELECTED : Status.DESELECTED);
  }

  @Override public boolean isCellEditable(EventObject e) {
    boolean editable = false;
    if (e instanceof MouseEvent) {
      MouseEvent me = (MouseEvent) e;
      editable = Optional.ofNullable(me.getComponent())
          .filter(JTree.class::isInstance)
          .map(JTree.class::cast)
          .map(t -> t.getPathBounds(t.getPathForLocation(me.getX(), me.getY())))
          .map(r -> {
            r.width = checkBox.getPreferredSize().width;
            return r.contains(me.getPoint());
          })
          .orElse(false);
    }
    return editable;
  }

  // // AbstractCellEditor
  // @Override public boolean shouldSelectCell(EventObject anEvent) {
  //   return true;
  // }
  //
  // @Override public boolean stopCellEditing() {
  //   fireEditingStopped();
  //   return true;
  // }
  //
  // @Override public void cancelCellEditing() {
  //   fireEditingCanceled();
  // }
}

class FolderSelectionListener implements TreeSelectionListener {
  private final FileSystemView fileSystemView;

  protected FolderSelectionListener(FileSystemView fileSystemView) {
    this.fileSystemView = fileSystemView;
  }

  @Override public void valueChanged(TreeSelectionEvent e) {
    TreeModel m = ((JTree) e.getSource()).getModel();
    DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.getPath().getLastPathComponent();
    Object uo = node.getUserObject();
    if (node.isLeaf() && uo instanceof CheckBoxNode && m instanceof DefaultTreeModel) {
      DefaultTreeModel model = (DefaultTreeModel) m;
      CheckBoxNode check = (CheckBoxNode) uo;
      File parent = check.getFile();
      // if (!parent.isDirectory()) {
      //   return;
      // }
      Status status = check.getStatus() == Status.SELECTED ? Status.SELECTED : Status.DESELECTED;
      BackgroundTask worker = new BackgroundTask(fileSystemView, parent) {
        @Override protected void process(List<File> chunks) {
          chunks.stream().map(file -> new CheckBoxNode(file, status))
              .map(DefaultMutableTreeNode::new)
              .forEach(child -> model.insertNodeInto(child, node, node.getChildCount()));
          // model.reload(parent); // = model.nodeStructureChanged(parent);
        }
      };
      worker.execute();
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

  @Override public String doInBackground() {
    Stream.of(fileSystemView.getFiles(parent, true))
        .filter(File::isDirectory)
        .forEach(this::publish);
    return "done";
  }
}
