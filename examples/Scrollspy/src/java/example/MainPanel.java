// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

public final class MainPanel extends JPanel {
  private final JEditorPane editor = new JEditorPane();

  private MainPanel() {
    super(new BorderLayout(2, 2));
    Icon emptyIcon = new EmptyIcon();
    UIManager.put("Tree.openIcon", emptyIcon);
    UIManager.put("Tree.closedIcon", emptyIcon);
    UIManager.put("Tree.leafIcon", emptyIcon);
    UIManager.put("Tree.expandedIcon", emptyIcon);
    UIManager.put("Tree.collapsedIcon", emptyIcon);
    UIManager.put("Tree.leftChildIndent", 10);
    UIManager.put("Tree.rightChildIndent", 0);
    UIManager.put("Tree.paintLines", false);

    HTMLEditorKit htmlEditorKit = new HTMLEditorKit();
    editor.setEditable(false);
    editor.setEditorKit(htmlEditorKit);
    editor.setText("<html><body><h1>Scrollspy</h1><p id='main'></p><p id='bottom'>id=bottom</p>");

    HTMLDocument doc = (HTMLDocument) editor.getDocument();
    JTree tree = makeTocSideTree(doc);

    // scroll to top of page
    EventQueue.invokeLater(() -> editor.scrollRectToVisible(editor.getBounds()));

    JScrollPane scroll = new JScrollPane(editor);
    scroll.getVerticalScrollBar().getModel().addChangeListener(e -> {
      // Document d = editor.getDocument();
      // if (d instanceof HTMLDocument) {
      //   HTMLDocument htmlDocument = (HTMLDocument) d;
      HTMLDocument.Iterator itr = doc.getIterator(HTML.Tag.A);
      for (; itr.isValid(); itr.next()) {
        try {
          // Java 9: Rectangle r = editor.modelToView2D(itr.getStartOffset()).getBounds();
          Rectangle r = editor.modelToView(itr.getStartOffset());
          if (r != null && editor.getVisibleRect().contains(r.getLocation())) {
            searchTreeNode(tree, itr.getAttributes().getAttribute(HTML.Attribute.NAME));
            break;
          }
        } catch (BadLocationException ex) {
          UIManager.getLookAndFeel().provideErrorFeedback(editor);
        }
      }
    });

    JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(tree), scroll);
    split.setResizeWeight(.5);
    add(split);
    setPreferredSize(new Dimension(320, 240));
  }

  private JTree makeTocSideTree(HTMLDocument doc) {
    Element element = doc.getElement("main");
    TreeModel model = makeModel();
    DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();
    // Java 9: Collections.list(root.preorderEnumeration()).stream()
    Collections.list((Enumeration<?>) root.preorderEnumeration()).stream()
        .filter(DefaultMutableTreeNode.class::isInstance)
        .map(DefaultMutableTreeNode.class::cast)
        .filter(node -> !node.isRoot())
        .map(node -> Objects.toString(node.getUserObject()))
        .forEach(ref -> {
          String br = String.join("", Collections.nCopies(12, "<br />"));
          String tag = "<a name='%s' href='#'>%s</a>%s";
          try {
            doc.insertBeforeEnd(element, String.format(tag, ref, ref, br));
          } catch (BadLocationException | IOException ex) {
            // Logger.getGlobal().severe(ex::getMessage);
            UIManager.getLookAndFeel().provideErrorFeedback(editor);
          }
        });

    JTree tree = new RowSelectionTree();
    tree.setModel(model);
    tree.setRowHeight(32);
    tree.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
    tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    tree.addTreeSelectionListener(e -> {
      Object o = e.getNewLeadSelectionPath().getLastPathComponent();
      if (o instanceof DefaultMutableTreeNode && tree.isEnabled()) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) o;
        String ref = Objects.toString(node.getUserObject());
        editor.scrollToReference(ref);
      }
    });
    expandAllNodes(tree);
    return tree;
  }

  private static void searchTreeNode(JTree tree, Object name) {
    TreeModel model = tree.getModel();
    DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();
    // Java 9: Collections.list(root.preorderEnumeration()).stream()
    Collections.list((Enumeration<?>) root.preorderEnumeration()).stream()
        .filter(DefaultMutableTreeNode.class::isInstance)
        .map(DefaultMutableTreeNode.class::cast)
        .filter(node -> Objects.equals(name, Objects.toString(node.getUserObject())))
        .findFirst()
        .ifPresent(node -> {
          tree.setEnabled(false);
          TreePath path = new TreePath(node.getPath());
          tree.setSelectionPath(path);
          tree.scrollPathToVisible(path);
          tree.setEnabled(true);
        });
    // // Java 9: Enumeration<TreeNode> e = root.preorderEnumeration();
    // Enumeration<?> e = root.preorderEnumeration();
    // while (e.hasMoreElements()) {
    //   DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.nextElement();
    //   if (node != null && Objects.equals(name, node.toString())) {
    //     tree.setEnabled(false);
    //     TreePath path = new TreePath(node.getPath());
    //     tree.setSelectionPath(path);
    //     tree.scrollPathToVisible(path);
    //     tree.setEnabled(true);
    //     return;
    //   }
    // }
  }

  private static DefaultTreeModel makeModel() {
    DefaultMutableTreeNode root = new DefaultMutableTreeNode("root");
    DefaultMutableTreeNode c1 = new DefaultMutableTreeNode("1. Introduction");
    root.add(c1);

    DefaultMutableTreeNode c2 = new DefaultMutableTreeNode("2. Chapter");
    c2.add(new DefaultMutableTreeNode("2.1. Section"));
    c2.add(new DefaultMutableTreeNode("2.2. Section"));
    c2.add(new DefaultMutableTreeNode("2.3. Section"));
    root.add(c2);

    DefaultMutableTreeNode c3 = new DefaultMutableTreeNode("3. Chapter");
    c3.add(new DefaultMutableTreeNode("3.1. Section"));
    c3.add(new DefaultMutableTreeNode("3.2. Section"));
    c3.add(new DefaultMutableTreeNode("3.3. Section"));
    c3.add(new DefaultMutableTreeNode("3.4. Section"));
    root.add(c3);

    return new DefaultTreeModel(root);
  }

  // https://ateraimemo.com/Swing/ExpandAllNodes.html
  private static void expandAllNodes(JTree tree) {
    int row = 0;
    while (row < tree.getRowCount()) {
      tree.expandRow(row++);
    }
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

// https://ateraimemo.com/Swing/TreeRowSelection.html
class RowSelectionTree extends JTree {
  private static final Color SELECTED_COLOR = new Color(0x64_96_C8);
  private transient TreeWillExpandListener listener;

  @Override protected void paintComponent(Graphics g) {
    int[] sr = getSelectionRows();
    if (sr == null) {
      super.paintComponent(g);
      return;
    }
    g.setColor(getBackground());
    g.fillRect(0, 0, getWidth(), getHeight());
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setPaint(SELECTED_COLOR);
    Arrays.stream(sr).mapToObj(this::getRowBounds)
        .forEach(r -> g2.fillRect(0, r.y, getWidth(), r.height));
    super.paintComponent(g);
    if (hasFocus()) {
      Optional.ofNullable(getLeadSelectionPath()).ifPresent(path -> {
        Rectangle r = getRowBounds(getRowForPath(path));
        g2.setPaint(SELECTED_COLOR.darker());
        g2.drawRect(0, r.y, getWidth() - 1, r.height - 1);
      });
    }
    g2.dispose();
  }

  @Override public void updateUI() {
    setCellRenderer(null);
    removeTreeWillExpandListener(listener);
    super.updateUI();
    setUI(new BasicTreeUI() {
      @Override public Rectangle getPathBounds(JTree tree, TreePath path) {
        Rectangle r = null;
        if (Objects.nonNull(tree) && Objects.nonNull(treeState)) {
          r = getTreePathBounds(path, tree.getInsets(), new Rectangle());
        }
        return r;
      }

      private Rectangle getTreePathBounds(TreePath path, Insets insets, Rectangle bounds) {
        Rectangle rect = treeState.getBounds(path, bounds);
        if (Objects.nonNull(rect)) {
          rect.width = tree.getWidth();
          rect.y += insets.top;
        }
        return rect;
      }
    });
    UIManager.put("Tree.repaintWholeRow", Boolean.TRUE);
    TreeCellRenderer r = getCellRenderer();
    setCellRenderer((tree, value, selected, expanded, leaf, row, hasFocus) -> {
      Component c = r.getTreeCellRendererComponent(
          tree, value, selected, expanded, leaf, row, hasFocus);
      c.setBackground(selected ? SELECTED_COLOR : tree.getBackground());
      if (c instanceof JComponent) {
        ((JComponent) c).setOpaque(true);
      }
      return c;
    });
    setOpaque(false);
    setRootVisible(false);
    // https://ateraimemo.com/Swing/TreeNodeCollapseVeto.html
    listener = new TreeWillExpandListener() {
      @Override public void treeWillExpand(TreeExpansionEvent e) { // throws ExpandVetoException {
        // throw new ExpandVetoException(e, "Tree expansion cancelled");
      }

      @Override public void treeWillCollapse(TreeExpansionEvent e) throws ExpandVetoException {
        throw new ExpandVetoException(e, "Tree collapse cancelled");
      }
    };
    addTreeWillExpandListener(listener);
  }
}

class EmptyIcon implements Icon {
  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    /* Empty icon */
  }

  @Override public int getIconWidth() {
    return 0;
  }

  @Override public int getIconHeight() {
    return 0;
  }
}
