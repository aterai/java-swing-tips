// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;

public final class MainPanel extends JPanel {
  private static final String MARK = "§"; // U+00A7

  private MainPanel() {
    super(new BorderLayout());
    JTree toc = new JTree(makeModel()) {
      @Override public void updateUI() {
        setCellRenderer(null);
        super.updateUI();
        // setCellRenderer(new ChapterNumberingTreeCellRenderer());
        TreeCellRenderer renderer = getCellRenderer();
        setCellRenderer((tree, value, selected, expanded, leaf, row, hasFocus) -> {
          Component c = renderer.getTreeCellRendererComponent(
              tree, value, selected, expanded, leaf, row, hasFocus);
          if (value instanceof DefaultMutableTreeNode && c instanceof JLabel) {
            String s = makeNumberString(((DefaultMutableTreeNode) value).getPath());
            ((JLabel) c).setText(String.format("%s%s %s", MARK, s, value));
          }
          return c;
        });
        setRootVisible(false);
      }
    };
    add(new JScrollPane(toc));
    setPreferredSize(new Dimension(320, 240));
  }

  private static String makeNumberString(TreeNode... treeNodes) {
    return IntStream.range(1, treeNodes.length) // ignore the root node by skipping index 0
        .map(i -> 1 + treeNodes[i - 1].getIndex(treeNodes[i]))
        .mapToObj(Objects::toString)
        .collect(Collectors.joining("."));
  }

  private static TreeModel makeModel() {
    DefaultMutableTreeNode root = new DefaultMutableTreeNode("root");
    root.add(new DefaultMutableTreeNode("Introduction"));
    root.add(makePart());
    root.add(makePart());
    return new DefaultTreeModel(root);
  }

  private static MutableTreeNode makePart() {
    DefaultMutableTreeNode c1 = new DefaultMutableTreeNode("Chapter");
    c1.add(new DefaultMutableTreeNode("Section A"));
    c1.add(new DefaultMutableTreeNode("Section B"));
    c1.add(new DefaultMutableTreeNode("Section C"));

    DefaultMutableTreeNode c2 = new DefaultMutableTreeNode("Chapter");
    c2.add(new DefaultMutableTreeNode("aaa aaa aaa"));
    c2.add(new DefaultMutableTreeNode("bb bb"));
    c2.add(new DefaultMutableTreeNode("cc"));

    DefaultMutableTreeNode p1 = new DefaultMutableTreeNode("Part");
    p1.add(c1);
    p1.add(c2);
    return p1;
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

// class ChapterNumberingTreeCellRenderer extends DefaultTreeCellRenderer {
//   private static final String MARK = "§";
//
//   @Override public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
//     Component c = super.getTreeCellRendererComponent(
//         tree, value, selected, expanded, leaf, row, hasFocus);
//     if (value instanceof DefaultMutableTreeNode && c instanceof JLabel) {
//       JLabel l = (JLabel) c;
//       TreeNode[] tn = ((DefaultMutableTreeNode) value).getPath();
//       String s = IntStream.range(1, tn.length) // ignore the root node by skipping index 0
//           .map(i -> 1 + tn[i - 1].getIndex(tn[i]))
//           .mapToObj(Objects::toString)
//           .collect(Collectors.joining("."));
//       l.setText(String.format("%s%s %s", MARK, s, value));
//     }
//     return c;
//   }
// }
