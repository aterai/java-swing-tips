// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.Collections;
import java.util.EventObject;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.swing.*;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;

public final class MainPanel extends JPanel {
  private final List<String> model1 = Arrays.asList("Disabled", "Enabled", "Debug mode");
  private final List<String> model2 = Arrays.asList("Disabled", "Enabled");

  private MainPanel() {
    super(new BorderLayout());

    DefaultMutableTreeNode root = new DefaultMutableTreeNode(new PluginNode("Plugins"));
    root.add(new DefaultMutableTreeNode(new PluginNode("Plugin 1", model1)));
    root.add(new DefaultMutableTreeNode(new PluginNode("Plugin 2", model1)));
    DefaultMutableTreeNode leaf = new DefaultMutableTreeNode(new PluginNode("Plugin 3"));
    root.add(leaf);
    leaf.add(new DefaultMutableTreeNode(new PluginNode("Plugin 3A", model2)));
    leaf.add(new DefaultMutableTreeNode(new PluginNode("Plugin 3B", model2)));

    JTree tree = new JTree(root);
    tree.setRowHeight(0);
    tree.setEditable(true);
    tree.setCellRenderer(new PluginCellRenderer(new JComboBox<>()));
    tree.setCellEditor(new PluginCellEditor(new JComboBox<>()));

    JTextArea textArea = new JTextArea(5, 1);

    tree.getModel().addTreeModelListener(new TreeModelListener() {
      @Override public void treeNodesChanged(TreeModelEvent e) {
        Optional.ofNullable(e.getChildren())
          .filter(children -> children.length == 1)
          .map(children -> children[0])
          .filter(DefaultMutableTreeNode.class::isInstance)
          .map(DefaultMutableTreeNode.class::cast)
          .map(DefaultMutableTreeNode::getUserObject)
          .filter(PluginNode.class::isInstance)
          .map(PluginNode.class::cast)
          .ifPresent(uo -> textArea.append(String.format("%s %s%n", uo, uo.plugins.get(uo.getSelectedIndex()))));

        // Object[] children = e.getChildren();
        // boolean isNotRootAndOnlyOneNodeChanged = Objects.nonNull(children)
        //   && children.length == 1 && children[0] instanceof DefaultMutableTreeNode;
        // if (isNotRootAndOnlyOneNodeChanged) {
        //   DefaultMutableTreeNode node = (DefaultMutableTreeNode) children[0];
        //   Object userObject = node.getUserObject();
        //   if (userObject instanceof PluginNode) {
        //     PluginNode uo = (PluginNode) userObject;
        //     textArea.append(String.format("%s %s%n", uo, uo.plugins.get(uo.getSelectedIndex())));
        //   }
        // }
      }

      @Override public void treeNodesInserted(TreeModelEvent e) {
        /* not needed */
      }

      @Override public void treeNodesRemoved(TreeModelEvent e) {
        /* not needed */
      }

      @Override public void treeStructureChanged(TreeModelEvent e) {
        /* not needed */
      }
    });
    add(new JScrollPane(tree));
    add(new JScrollPane(textArea), BorderLayout.SOUTH);
    setPreferredSize(new Dimension(320, 240));
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
      ex.printStackTrace();
      Toolkit.getDefaultToolkit().beep();
    }
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

class PluginNode {
  protected final String name;
  protected final List<String> plugins;
  private int selectedIndex;

  protected PluginNode(String name, List<String> plugins) {
    this.name = name;
    this.plugins = plugins;
  }

  protected PluginNode(String name) {
    this(name, Collections.emptyList());
  }

  public int getSelectedIndex() {
    return selectedIndex;
  }

  public void setSelectedIndex(int selectedIndex) {
    this.selectedIndex = selectedIndex;
  }

  @Override public String toString() {
    return name;
  }
}

class PluginPanel extends JPanel {
  protected final JLabel pluginName = new JLabel();
  protected final JComboBox<String> comboBox;

  protected PluginPanel(JComboBox<String> comboBox) {
    super();
    this.comboBox = comboBox;
    comboBox.setPrototypeDisplayValue("Debug mode x");
    setOpaque(false);
    add(pluginName);
    add(comboBox);
  }

  protected PluginNode extractNode(Object value) {
    if (value instanceof DefaultMutableTreeNode) {
      Object userObject = ((DefaultMutableTreeNode) value).getUserObject();
      if (userObject instanceof PluginNode) {
        PluginNode node = (PluginNode) userObject;
        pluginName.setText(node.toString());
        DefaultComboBoxModel<String> model = (DefaultComboBoxModel<String>) comboBox.getModel();
        model.removeAllElements();
        if (node.plugins.isEmpty()) {
          remove(comboBox);
        } else {
          add(comboBox);
          for (String s: node.plugins) {
            model.addElement(s);
          }
          comboBox.setSelectedIndex(node.getSelectedIndex());
        }
        return node;
      }
    }
    return null;
  }
}

class PluginCellRenderer implements TreeCellRenderer {
  private final PluginPanel panel;

  protected PluginCellRenderer(JComboBox<String> comboBox) {
    super();
    panel = new PluginPanel(comboBox);
  }

  @Override public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
    panel.extractNode(value);
    return panel;
  }
}

class PluginCellEditor extends DefaultCellEditor {
  private final PluginPanel panel;
  private transient PluginNode node;

  protected PluginCellEditor(JComboBox<String> comboBox) {
    super(comboBox);
    panel = new PluginPanel(comboBox);
  }

  @Override public Component getTreeCellEditorComponent(JTree tree, Object value, boolean isSelected, boolean expanded, boolean leaf, int row) {
    this.node = panel.extractNode(value);
    return panel;
  }

  @Override public Object getCellEditorValue() {
    Object o = super.getCellEditorValue();
    return Optional.ofNullable(node).map(n -> {
      DefaultComboBoxModel<String> m = (DefaultComboBoxModel<String>) panel.comboBox.getModel();
      PluginNode pn = new PluginNode(panel.pluginName.getText(), n.plugins);
      pn.setSelectedIndex(m.getIndexOf(o));
      return (Object) pn;
    }).orElse(o);
  }

  @Override public boolean isCellEditable(EventObject e) {
    Object source = e.getSource();
    if (!(source instanceof JTree) || !(e instanceof MouseEvent)) {
      return false;
    }
    JTree tree = (JTree) source;
    Point p = ((MouseEvent) e).getPoint();
    TreePath path = tree.getPathForLocation(p.x, p.y);
    if (Objects.isNull(path)) {
      return false;
    }
    Object n = path.getLastPathComponent();
    if (!(n instanceof DefaultMutableTreeNode)) {
      return false;
    }
    Rectangle r = tree.getPathBounds(path);
    if (Objects.isNull(r)) {
      return false;
    }
    Dimension d = panel.getPreferredSize();
    r.width = d.width;
    if (r.contains(p)) {
      showComboPopup(tree, p);
      return true;
    }
    return delegate.isCellEditable(e);
  }

  private void showComboPopup(JTree tree, Point p) {
    EventQueue.invokeLater(() -> {
      Point pt = SwingUtilities.convertPoint(tree, p, panel);
      Component o = SwingUtilities.getDeepestComponentAt(panel, pt.x, pt.y);
      if (o instanceof JComboBox) {
        panel.comboBox.showPopup();
      } else if (Objects.nonNull(o)) {
        Container c = SwingUtilities.getAncestorOfClass(JComboBox.class, (Component) o);
        if (c instanceof JComboBox) {
          panel.comboBox.showPopup();
        }
      }
    });
  }
}
