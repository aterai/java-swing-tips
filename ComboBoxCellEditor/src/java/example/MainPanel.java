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
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellRenderer;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    DefaultMutableTreeNode root = new DefaultMutableTreeNode(new PluginNode("Plugins"));
    List<String> model1 = Arrays.asList("Disabled", "Enabled", "Debug mode");
    root.add(new DefaultMutableTreeNode(new PluginNode("Plugin 1", model1)));
    root.add(new DefaultMutableTreeNode(new PluginNode("Plugin 2", model1)));
    DefaultMutableTreeNode leaf = new DefaultMutableTreeNode(new PluginNode("Plugin 3"));
    root.add(leaf);
    List<String> model2 = Arrays.asList("Disabled", "Enabled");
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
            .map(uo -> String.format("%s %s%n", uo, uo.getPlugins().get(uo.getSelectedIndex())))
            .ifPresent(textArea::append);
        // Object[] children = e.getChildren();
        // boolean isNotRootAndOnlyOneNodeChanged = Objects.nonNull(children)
        //     && children.length == 1 && children[0] instanceof DefaultMutableTreeNode;
        // if (isNotRootAndOnlyOneNodeChanged) {
        //   DefaultMutableTreeNode node = (DefaultMutableTreeNode) children[0];
        //   Object userObject = node.getUserObject();
        //   if (userObject instanceof PluginNode) {
        //     PluginNode uo = (PluginNode) userObject;
        //     String v = uo.getPlugins().get(uo.getSelectedIndex());
        //     textArea.append(String.format("%s %s%n", uo, v));
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

class PluginNode {
  private final String name;
  private final List<String> plugins;
  private int selectedIndex;

  protected PluginNode(String name, List<String> plugins) {
    this.name = name;
    this.plugins = plugins;
  }

  protected PluginNode(String name) {
    this(name, Collections.emptyList());
  }

  // public String getName() {
  //   return name;
  // }

  public List<String> getPlugins() {
    return plugins;
  }

  public int getSelectedIndex() {
    return selectedIndex;
  }

  public void setSelectedIndex(int index) {
    selectedIndex = index;
  }

  @Override public String toString() {
    return name;
  }
}

class PluginPanel extends JPanel {
  protected final JLabel pluginName = new JLabel();
  protected final JComboBox<String> combo;

  protected PluginPanel(JComboBox<String> combo) {
    super();
    this.combo = combo;
    combo.setPrototypeDisplayValue("Debug mode x");
    add(pluginName);
    add(combo);
  }

  @Override public final Component add(Component comp) {
    return super.add(comp);
  }

  @Override public boolean isOpaque() {
    return false;
  }

  protected PluginNode extractNode(Object value) {
    return Optional.ofNullable(value)
        .filter(DefaultMutableTreeNode.class::isInstance)
        .map(DefaultMutableTreeNode.class::cast)
        .map(DefaultMutableTreeNode::getUserObject)
        .filter(PluginNode.class::isInstance)
        .map(PluginNode.class::cast)
        .map(this::updatePluginNode)
        .orElse(null);
  }

  private PluginNode updatePluginNode(PluginNode node) {
    pluginName.setText(node.toString());
    ComboBoxModel<String> model = combo.getModel();
    if (model instanceof DefaultComboBoxModel) {
      DefaultComboBoxModel<String> m = (DefaultComboBoxModel<String>) model;
      m.removeAllElements();
      List<String> plugins = node.getPlugins();
      if (plugins.isEmpty()) {
        remove(combo);
      } else {
        add(combo);
        for (String s : plugins) {
          m.addElement(s);
        }
        // Java 11: m1.addAll(plugins);
        combo.setSelectedIndex(node.getSelectedIndex());
      }
    }
    return node;
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
    return Optional.ofNullable(node).<Object>map(n -> {
      DefaultComboBoxModel<String> m = (DefaultComboBoxModel<String>) panel.combo.getModel();
      PluginNode pn = new PluginNode(panel.pluginName.getText(), n.getPlugins());
      pn.setSelectedIndex(m.getIndexOf(o));
      return pn;
    }).orElse(o);
  }

  // @Override public boolean isCellEditable(EventObject e) {
  //   Object source = e.getSource();
  //   if (!(source instanceof JTree) || !(e instanceof MouseEvent)) {
  //     return false;
  //   }
  //   JTree tree = (JTree) source;
  //   Point p = ((MouseEvent) e).getPoint();
  //   TreePath path = tree.getPathForLocation(p.x, p.y);
  //   if (Objects.isNull(path)) {
  //     return false;
  //   }
  //   Object n = path.getLastPathComponent();
  //   if (!(n instanceof DefaultMutableTreeNode)) {
  //     return false;
  //   }
  //   Rectangle r = tree.getPathBounds(path);
  //   if (Objects.isNull(r)) {
  //     return false;
  //   }
  //   Dimension d = panel.getPreferredSize();
  //   r.width = d.width;
  //   if (r.contains(p)) {
  //     showComboPopup(tree, p);
  //     return true;
  //   }
  //   return delegate.isCellEditable(e);
  // }

  @Override public boolean isCellEditable(EventObject e) {
    if (e instanceof MouseEvent) {
      MouseEvent me = (MouseEvent) e;
      showComboPopup(me.getComponent(), me.getPoint());
    }
    return delegate.isCellEditable(e);
  }

  private void showComboPopup(Component cmp, Point p) {
    EventQueue.invokeLater(() -> {
      Point pt = SwingUtilities.convertPoint(cmp, p, panel);
      Component o = SwingUtilities.getDeepestComponentAt(panel, pt.x, pt.y);
      if (o instanceof JComboBox) {
        panel.combo.showPopup();
      } else if (Objects.nonNull(o)) { // maybe ArrowButton in JComboBox
        Container c = SwingUtilities.getAncestorOfClass(JComboBox.class, o);
        if (c instanceof JComboBox) {
          panel.combo.showPopup();
        }
      }
    });
  }
}
