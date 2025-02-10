// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTextField field = new JTextField("asd", 10);

    JTree tree = new JTree(makeModel());
    Action findNextAction = new AbstractAction("Find Next") {
      private final List<TreePath> rollOverPathLists = new ArrayList<>();
      @Override public void actionPerformed(ActionEvent e) {
        rollOverPathLists.clear();
        TreePath selectedPath = tree.getSelectionPath();
        tree.clearSelection();
        TreeUtils.searchTree(tree, tree.getPathForRow(0), field.getText(), rollOverPathLists);
        if (!rollOverPathLists.isEmpty()) {
          int nextIndex = 0;
          int size = rollOverPathLists.size();
          for (int i = 0; i < size; i++) {
            if (rollOverPathLists.get(i).equals(selectedPath)) {
              nextIndex = i + 1 < size ? i + 1 : 0;
              break;
            }
          }
          TreePath p = rollOverPathLists.get(nextIndex);
          tree.addSelectionPath(p);
          tree.scrollPathToVisible(p);
        }
      }
    };

    JButton button = new JButton();
    button.setAction(findNextAction);
    button.setFocusable(false);

    String findCmd = "find-next";
    field.getActionMap().put(findCmd, findNextAction);
    KeyStroke enterKey = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
    field.getInputMap(WHEN_FOCUSED).put(enterKey, findCmd);

    JPanel controls = new JPanel();
    ControlPanelLayout layout = new ControlPanelLayout(controls, 5, 5);
    controls.setLayout(layout);

    controls.setBorder(BorderFactory.createTitledBorder("Search down"));
    controls.add(new JLabel("Find what:"), BorderLayout.WEST);
    controls.add(field);
    controls.add(button, BorderLayout.EAST);

    JButton showHideButton = new JButton();
    showHideButton.setAction(layout.showHideAction);
    showHideButton.setFocusable(false);

    String searchCmd = "open-search-box";
    getActionMap().put(searchCmd, layout.showHideAction);
    // Java 10: int modifiers = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
    int modifiers = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
    KeyStroke key = KeyStroke.getKeyStroke(KeyEvent.VK_F, modifiers);
    getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(key, searchCmd);

    add(controls, BorderLayout.NORTH);
    add(new JScrollPane(tree));
    add(showHideButton, BorderLayout.SOUTH);
    setPreferredSize(new Dimension(320, 240));
  }

  private static DefaultTreeModel makeModel() {
    DefaultMutableTreeNode set1 = new DefaultMutableTreeNode("Set 001");
    set1.add(new DefaultMutableTreeNode("111111111"));
    set1.add(new DefaultMutableTreeNode("22222222222"));
    set1.add(new DefaultMutableTreeNode("33333"));

    DefaultMutableTreeNode set2 = new DefaultMutableTreeNode("Set 002");
    set2.add(new DefaultMutableTreeNode("asd fas df as"));
    set2.add(new DefaultMutableTreeNode("as df"));

    DefaultMutableTreeNode set3 = new DefaultMutableTreeNode("Set 003");
    set3.add(new DefaultMutableTreeNode("asd fas dfa sdf"));
    set3.add(new DefaultMutableTreeNode("qwe rqw er"));
    set3.add(new DefaultMutableTreeNode("zvx cvz xcv zxz xcv zx cv"));

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
    // frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

class ControlPanelLayout extends BorderLayout {
  public final Action showHideAction = new AbstractAction("Show/Hide Search Box") {
    @Override public void actionPerformed(ActionEvent e) {
      showHideActionPerformed();
    }
  };
  private boolean isHidden = true;
  private final Container controls;
  private final Timer animator = new Timer(5, null);
  private int controlsHeight;

  protected ControlPanelLayout(Container controls, int hgap, int vgap) {
    super(hgap, vgap);
    this.controls = Objects.requireNonNull(controls, "controls must not be null");
    animator.addActionListener(e -> controls.revalidate());
  }

  protected void showHideActionPerformed() {
    if (!animator.isRunning()) {
      isHidden = controls.getHeight() == 0;
      animator.start();
    }
  }

  @Override public Dimension preferredLayoutSize(Container target) {
    // synchronized (target.getTreeLock()) {
    Dimension ps = super.preferredLayoutSize(target);
    int defaultHeight = ps.height;
    if (animator.isRunning()) {
      if (isHidden) {
        if (controls.getHeight() < defaultHeight) {
          controlsHeight += 5;
        }
      } else {
        if (controls.getHeight() > 0) {
          controlsHeight -= 5;
        }
      }
      if (controlsHeight <= 0) {
        controlsHeight = 0;
        animator.stop();
      } else if (controlsHeight >= defaultHeight) {
        controlsHeight = defaultHeight;
        animator.stop();
      }
    }
    ps.height = controlsHeight;
    return ps;
  }
}

final class TreeUtils {
  private TreeUtils() {
    /* Singleton */
  }

  public static void searchTree(JTree tree, TreePath path, String q, List<TreePath> results) {
    Object o = path.getLastPathComponent();
    if (o instanceof TreeNode) {
      TreeNode node = (TreeNode) o;
      if (node.toString().startsWith(q)) {
        results.add(path);
        tree.expandPath(path.getParentPath());
      }
      if (!node.isLeaf()) {
        // Java 9: Collections.list(node.children())
        Collections.list((Enumeration<?>) node.children())
            .forEach(n -> searchTree(tree, path.pathByAddingChild(n), q, results));
      }
    }
  }
}
