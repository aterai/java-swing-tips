// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.image.ImageObserver;
import java.util.Objects;
import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    ImageIcon icon = new ImageIcon(getClass().getResource("restore_to_background_color.gif"));

    DefaultMutableTreeNode root = new DefaultMutableTreeNode("root");
    DefaultMutableTreeNode s0 = new DefaultMutableTreeNode(new NodeObject("default", icon));
    DefaultMutableTreeNode s1 = new DefaultMutableTreeNode(new NodeObject("setImageObserver", icon));
    root.add(s0);
    root.add(s1);
    JTree tree = new JTree(new DefaultTreeModel(root));
    tree.setCellRenderer(new DefaultTreeCellRenderer() {
      @Override public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        JLabel l = (JLabel) super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
        if (value instanceof MutableTreeNode && ((DefaultMutableTreeNode) value).getUserObject() instanceof NodeObject) {
          NodeObject uo = (NodeObject) ((DefaultMutableTreeNode) value).getUserObject();
          l.setText(Objects.toString(uo.title, ""));
          l.setIcon(uo.icon);
        } else {
          l.setText(Objects.toString(value, ""));
          l.setIcon(null);
        }
        return l;
      }
    });
    TreePath path = new TreePath(s1.getPath());
    // Wastefulness: icon.setImageObserver((ImageObserver) tree);
    icon.setImageObserver(new ImageObserver() {
      @Override public boolean imageUpdate(Image img, int infoflags, int x, int y, int w, int h) {
        if (!tree.isShowing()) {
          return false;
        }
        Rectangle cellRect = tree.getPathBounds(path);
        if ((infoflags & (FRAMEBITS | ALLBITS)) != 0 && Objects.nonNull(cellRect)) {
          tree.repaint(cellRect);
        }
        return (infoflags & (ALLBITS | ABORT)) == 0;
      }
    });
    tree.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    add(new JScrollPane(tree));
    setPreferredSize(new Dimension(320, 240));
  }

  public static void main(String... args) {
    EventQueue.invokeLater(new Runnable() {
      @Override public void run() {
        createAndShowGui();
      }
    });
  }

  public static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
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

class NodeObject {
  public final Icon icon;
  public final String title;

  protected NodeObject(String title) {
    this(title, null);
  }

  protected NodeObject(String title, Icon icon) {
    this.title = title;
    this.icon = icon;
  }
}
