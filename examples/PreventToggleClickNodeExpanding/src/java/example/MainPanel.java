// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.plaf.metal.MetalTreeUI;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreePath;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new GridLayout(1, 2, 4, 4));
    File dir = new File(".");
    DefaultMutableTreeNode root = new DefaultMutableTreeNode(dir);
    DefaultTreeModel treeModel = new DefaultTreeModel(root);
    createChildren(dir, root);

    JTree tree1 = new JTree(treeModel);
    tree1.addTreeWillExpandListener(new FileExpandVetoListener());

    JTree tree2 = new JTree(treeModel) {
      @Override public void updateUI() {
        super.updateUI();
        setUI(new MetalTreeUI() {
          @Override protected boolean isToggleEvent(MouseEvent e) {
            File file = getFileFromTreePath(tree.getSelectionPath());
            return file == null && super.isToggleEvent(e);
          }
        });
      }
    };

    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    add(new JScrollPane(initTree(tree1)));
    add(new JScrollPane(initTree(tree2)));
    setPreferredSize(new Dimension(320, 240));
  }

  private static JTree initTree(JTree tree) {
    tree.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
    tree.setCellRenderer(new FileTreeCellRenderer());
    // tree.addMouseListener(new MouseAdapter() {
    //   @Override public void mouseClicked(MouseEvent e) {
    //     boolean isDoubleClick = e.getClickCount() == 2;
    //     if (isDoubleClick) {
    //       File file = getFileFromTreePath(tree.getSelectionPath());
    //       System.out.println(file);
    //     }
    //   }
    // });
    // tree.setToggleClickCount(0);
    tree.expandRow(0);
    return tree;
  }

  private static void createChildren(File parent, DefaultMutableTreeNode node) {
    File[] list = parent.listFiles();
    if (list == null) {
      return;
    }
    Arrays.asList(list).forEach(file -> {
      DefaultMutableTreeNode child = new DefaultMutableTreeNode(file);
      node.add(child);
      if (file.isDirectory()) {
        createChildren(file, child);
      } else if (Objects.equals("MainPanel.java", file.getName())) {
        child.add(makeNode("MainPanel()"));
        child.add(makeNode("createAndShowGui():void"));
        child.add(makeNode("createChildren(File, DefaultMutableTreeNode):void"));
        child.add(makeNode("main(String[]):void"));
      }
    });
  }

  private static DefaultMutableTreeNode makeNode(String txt) {
    return new DefaultMutableTreeNode(txt);
  }

  public static File getFileFromTreePath(TreePath path) {
    File file = null;
    Object o = Objects.nonNull(path) ? path.getLastPathComponent() : null;
    if (o instanceof DefaultMutableTreeNode) {
      DefaultMutableTreeNode node = (DefaultMutableTreeNode) o;
      Object uo = node.getUserObject();
      if (uo instanceof File && ((File) uo).isFile()) {
        file = (File) uo;
      }
    }
    return file;
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
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

class FileExpandVetoListener implements TreeWillExpandListener {
  @Override public void treeWillExpand(TreeExpansionEvent e) throws ExpandVetoException {
    TreePath path = e.getPath();
    Object o = path.getLastPathComponent();
    if (o instanceof DefaultMutableTreeNode) {
      DefaultMutableTreeNode node = (DefaultMutableTreeNode) o;
      File file = (File) node.getUserObject();
      if (file.isFile()) {
        throw new ExpandVetoException(e, "Tree expansion cancelled");
      }
    }
  }

  @Override public void treeWillCollapse(TreeExpansionEvent e) { // throws ExpandVetoException {
    // throw new ExpandVetoException(e, "Tree collapse cancelled");
  }
}

class FileTreeCellRenderer extends DefaultTreeCellRenderer {
  @Override public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
    Component c = super.getTreeCellRendererComponent(
        tree, value, selected, expanded, leaf, row, hasFocus);
    if (selected) {
      c.setForeground(getTextSelectionColor());
    } else {
      c.setForeground(getTextNonSelectionColor());
      c.setBackground(getBackgroundNonSelectionColor());
    }
    if (value instanceof DefaultMutableTreeNode && c instanceof JLabel) {
      Object o = ((DefaultMutableTreeNode) value).getUserObject();
      if (o instanceof File) {
        String txt;
        try {
          txt = ((File) o).getCanonicalFile().getName();
        } catch (IOException ex) {
          txt = ex.getMessage();
        }
        ((JLabel) c).setText(txt);
      }
    }
    return c;
  }
}
