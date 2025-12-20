// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Stream;
import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.filechooser.FileSystemView;
import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout(2, 2));
    FileSystemViewTree tree = new FileSystemViewTree();
    tree.setRootVisible(false);
    tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    tree.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

    DefaultListModel<File> listModel = new DefaultListModel<>();
    tree.addTreeSelectionListener(e -> Optional.ofNullable(e.getNewLeadSelectionPath())
        .map(TreePath::getLastPathComponent).filter(DefaultMutableTreeNode.class::isInstance)
        .map(DefaultMutableTreeNode.class::cast).map(DefaultMutableTreeNode::getUserObject)
        .ifPresent(uo -> updateListFiles(uo, listModel)));

    UIManager.put("PopupMenu.consumeEventOnClose", false);
    JPopupMenu popup = new JPopupMenu();
    popup.addPopupMenuListener(new TreePopupMenuListener(tree));
    popup.add("JMenuItem1");
    popup.add("JMenuItem2");
    popup.add("JMenuItem3");
    tree.setComponentPopupMenu(popup);

    JScrollPane s1 = new JScrollPane(tree);
    JScrollPane s2 = new JScrollPane(new JList<>(listModel));
    JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, s1, s2);
    split.setResizeWeight(.5);
    add(split);
    setPreferredSize(new Dimension(320, 240));
  }

  private static void updateListFiles(Object userObject, DefaultListModel<File> model) {
    if (userObject instanceof File) {
      File[] files = ((File) userObject).listFiles();
      model.clear();
      if (files != null) {
        for (File f : files) {
          model.addElement(f);
        }
      }
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

class FileSystemViewTree extends JTree {
  private static final Color SELECTED_COLOR = new Color(0x00_78_D7);
  private static final Color ROLLOVER_COLOR = new Color(0x64_96_C8);
  private int rollOverRowIndex = -1;
  private transient MouseAdapter rolloverHandler;
  private transient FileSystemView fileSystemView;

  protected FileSystemViewTree() {
    super();
  }

  @Override protected void paintComponent(Graphics g) {
    int[] sr = getSelectionRows();
    if (sr == null) {
      super.paintComponent(g);
    } else {
      g.setColor(getBackground());
      g.fillRect(0, 0, getWidth(), getHeight());
      Graphics2D g2 = (Graphics2D) g.create();
      paintRollover(g2);
      paintRowSelection(g2, sr);
      super.paintComponent(g);
      if (hasFocus()) {
        paintFocus(g2);
      }
      g2.dispose();
    }
  }

  private void paintRollover(Graphics2D g2) {
    if (rollOverRowIndex >= 0) {
      g2.setPaint(ROLLOVER_COLOR);
      Rectangle rect = getRowBounds(rollOverRowIndex);
      g2.fillRect(0, rect.y, getWidth(), rect.height);
    }
  }

  private void paintRowSelection(Graphics2D g2, int[] selectedRows) {
    g2.setPaint(SELECTED_COLOR);
    Arrays.stream(selectedRows).mapToObj(this::getRowBounds)
        .forEach(r -> g2.fillRect(0, r.y, getWidth(), r.height));
  }

  private void paintFocus(Graphics2D g2) {
    Optional.ofNullable(getLeadSelectionPath()).ifPresent(path -> {
      Rectangle r = getRowBounds(getRowForPath(path));
      g2.setPaint(SELECTED_COLOR.darker());
      g2.drawRect(0, r.y, getWidth() - 1, r.height - 1);
    });
  }

  @Override public void updateUI() {
    setCellRenderer(null);
    removeMouseListener(rolloverHandler);
    removeMouseMotionListener(rolloverHandler);
    super.updateUI();
    setUI(new WholeRowSelectTreeUI());
    UIManager.put("Tree.repaintWholeRow", true);
    fileSystemView = FileSystemView.getFileSystemView();
    addTreeSelectionListener(new FolderSelectionListener(fileSystemView));
    expandRow(0);
    DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
    setCellRenderer((tree, value, selected, expanded, leaf, row, hasFocus) -> {
      Component c = renderer.getTreeCellRendererComponent(
          tree, value, selected, expanded, leaf, row, false);
      boolean rollover = row == rollOverRowIndex;
      updateFgc(c, renderer.getTextSelectionColor(), rollover);
      updateBgc(c, tree.getBackground(), selected, rollover);
      updateIcon(c, value, selected);
      return c;
    });
    setOpaque(false);
    rolloverHandler = new RolloverHandler();
    addMouseListener(rolloverHandler);
    addMouseMotionListener(rolloverHandler);
    EventQueue.invokeLater(() -> setModel(makeFileTreeModel(fileSystemView)));
  }

  private void updateIcon(Component c, Object value, boolean selected) {
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
  }

  private void updateFgc(Component c, Color color, boolean rollover) {
    if (rollover) {
      c.setForeground(color);
    }
  }

  private void updateBgc(Component c, Color color, boolean selected, boolean rollover) {
    if (selected) {
      c.setBackground(SELECTED_COLOR);
    } else if (rollover) {
      c.setBackground(ROLLOVER_COLOR);
    } else {
      c.setBackground(color);
    }
    if (c instanceof JComponent) {
      ((JComponent) c).setOpaque(true);
    }
  }

  protected static DefaultTreeModel makeFileTreeModel(FileSystemView fileSystemView) {
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
    return treeModel;
  }

  public void updateRolloverIndex() {
    EventQueue.invokeLater(() -> {
      Point pt = getMousePosition();
      if (pt == null) {
        clearRollover();
      } else {
        updateRolloverIndex(pt);
      }
    });
  }

  public void updateRolloverIndex(Point pt) {
    int row = getRowForLocation(pt.x, pt.y);
    boolean isPopupVisible = getComponentPopupMenu().isVisible();
    if (rollOverRowIndex != row && !isPopupVisible) {
      rollOverRowIndex = row;
      repaint();
    }
  }

  private void clearRollover() {
    rollOverRowIndex = -1;
    repaint();
  }

  protected class RolloverHandler extends MouseAdapter {
    @Override public void mouseMoved(MouseEvent e) {
      updateRolloverIndex(e.getPoint());
    }

    @Override public void mouseEntered(MouseEvent e) {
      updateRolloverIndex(e.getPoint());
    }

    @Override public void mouseExited(MouseEvent e) {
      boolean isPopupVisible = getComponentPopupMenu().isVisible();
      if (!isPopupVisible) {
        clearRollover();
      }
    }
  }
}

class WholeRowSelectTreeUI extends BasicTreeUI {
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
}

class TreePopupMenuListener implements PopupMenuListener {
  private final FileSystemViewTree tree;

  protected TreePopupMenuListener(FileSystemViewTree tree) {
    this.tree = tree;
  }

  @Override public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
    /* not needed */
  }

  @Override public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
    tree.updateRolloverIndex();
  }

  @Override public void popupMenuCanceled(PopupMenuEvent e) {
    tree.updateRolloverIndex();
  }
}

class FolderSelectionListener implements TreeSelectionListener {
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
      DefaultTreeModel m = (DefaultTreeModel) tree.getModel();
      new BackgroundTask(fileSystemView, parent) {
        @Override protected void process(List<File> chunks) {
          if (tree.isDisplayable() && !isCancelled()) {
            chunks.stream().map(DefaultMutableTreeNode::new)
                .forEach(child -> {
                  int count = node.getChildCount();
                  m.insertNodeInto(child, node, count);
                });
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

  @Override public String doInBackground() {
    Stream.of(fileSystemView.getFiles(parent, true))
        .filter(File::isDirectory)
        .forEach(this::publish);
    return "done";
  }
}
