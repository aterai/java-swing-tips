// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Objects;
import java.util.regex.Pattern;
import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTree tree = new JTree(makeModel()) {
      @Override public void updateUI() {
        setCellRenderer(null);
        setCellEditor(null);
        super.updateUI();
        setCellRenderer(new SelectionColorTreeCellRenderer());
      }
    };
    add(new JScrollPane(tree));
    setPreferredSize(new Dimension(320, 240));
  }

  private static DefaultTreeModel makeModel() {
    DefaultMutableTreeNode set1 = new DefaultMutableTreeNode(Color.ORANGE);
    set1.add(new DefaultMutableTreeNode(Color.RED));
    set1.add(new DefaultMutableTreeNode(Color.GREEN));
    set1.add(new DefaultMutableTreeNode(Color.BLUE));

    DefaultMutableTreeNode set2 = new DefaultMutableTreeNode("Set 002");
    set2.add(new DefaultMutableTreeNode("aaa 111111111"));
    set2.add(new DefaultMutableTreeNode("aa 2222"));

    DefaultMutableTreeNode set3 = new DefaultMutableTreeNode("Set 003");
    set3.add(new DefaultMutableTreeNode("Abc 3333333333333"));
    set3.add(new DefaultMutableTreeNode("44444444"));
    set3.add(new DefaultMutableTreeNode("55555555555555555"));

    DefaultMutableTreeNode root = new DefaultMutableTreeNode("Root");
    root.add(set1);
    root.add(set2);
    set2.add(set3);
    return new DefaultTreeModel(root);
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

class SelectionColorTreeCellRenderer extends DefaultTreeCellRenderer {
  private final Pattern pattern = Pattern.compile("^a.*", Pattern.CASE_INSENSITIVE);

  @Override public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
    Component c = super.getTreeCellRendererComponent(
        tree, value, selected, expanded, leaf, row, hasFocus);
    if (selected && c instanceof JComponent) {
      JComponent jc = (JComponent) c;
      jc.setOpaque(false);
      jc.setForeground(getTextSelectionColor());
      jc.setBackground(getBackgroundSelectionColor());
      setUserObjectColor(value, jc);
      setPatternColor(value, jc, leaf);
    } else {
      c.setForeground(getTextNonSelectionColor());
      c.setBackground(getBackgroundNonSelectionColor());
    }
    return c;
  }

  private void setUserObjectColor(Object value, JComponent c) {
    if (value instanceof DefaultMutableTreeNode) {
      Object uo = ((DefaultMutableTreeNode) value).getUserObject();
      if (uo instanceof Color) {
        c.setOpaque(true);
        c.setBackground((Color) uo);
      }
    }
  }

  private void setPatternColor(Object value, JComponent c, boolean leaf) {
    String str = Objects.toString(value, "");
    if (leaf && pattern.matcher(str).matches()) {
      c.setOpaque(true);
      c.setBackground(Color.RED);
    }
  }
}
