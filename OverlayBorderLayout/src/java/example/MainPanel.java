// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JPanel searchBox = new JPanel(new BorderLayout());
    LayoutAnimator handler = new LayoutAnimator(searchBox);

    JPanel p = new JPanel(handler) {
      @Override public boolean isOptimizedDrawingEnabled() {
        return false;
      }
    };
    p.add(searchBox, BorderLayout.NORTH);

    JTree tree = new JTree();
    tree.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

    JScrollPane scroll = new JScrollPane(tree);
    scroll.setBorder(BorderFactory.createTitledBorder("Find... Ctrl+F"));
    p.add(scroll);

    JTextField field = new JTextField("b", 10) {
      private transient AncestorListener listener;

      @Override public void updateUI() {
        removeAncestorListener(listener);
        super.updateUI();
        listener = new FocusAncestorListener();
        addAncestorListener(listener);
      }
    };
    Action findNextAction = new FindNextAction(tree, field);
    JButton button = new JButton(findNextAction);
    button.setFocusable(false);
    button.setToolTipText("Find next");
    button.setText("v");

    searchBox.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    searchBox.add(field);
    searchBox.add(button, BorderLayout.EAST);
    searchBox.setVisible(false);

    Timer animator = new Timer(5, handler);
    int modifiers = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
    // Java 10: int modifiers = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
    InputMap im = p.getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F, modifiers), "open-searchbox");
    im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "close-searchbox");
    p.getActionMap().put("open-searchbox", new AbstractAction("Show/Hide Search Box") {
      @Override public void actionPerformed(ActionEvent e) {
        if (!animator.isRunning()) {
          handler.isDirectionOfHiding = !searchBox.isVisible();
          searchBox.setVisible(true);
          animator.start();
        }
      }
    });
    p.getActionMap().put("close-searchbox", new AbstractAction("Hide Search Box") {
      @Override public void actionPerformed(ActionEvent e) {
        if (!animator.isRunning()) {
          handler.isDirectionOfHiding = false;
          animator.start();
        }
      }
    });

    field.getActionMap().put("find-next", findNextAction);
    field.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "find-next");

    add(p);
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
    // frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

class LayoutAnimator extends BorderLayout implements ActionListener {
  protected boolean isDirectionOfHiding = true;
  private final JComponent component;
  private int yy;
  private int counter;

  protected LayoutAnimator(JComponent c) {
    super();
    this.component = c;
  }

  @Override public void actionPerformed(ActionEvent e) {
    Timer animator = (Timer) e.getSource();
    int height = component.getPreferredSize().height;
    if (isDirectionOfHiding) {
      yy = (int) (.5 + AnimationUtil.easeInOut(++counter / (double) height) * height);
      if (yy >= height) {
        yy = height;
        animator.stop();
      }
    } else {
      yy = (int) (.5 + AnimationUtil.easeInOut(--counter / (double) height) * height);
      if (yy <= 0) {
        yy = 0;
        animator.stop();
        component.setVisible(false);
      }
    }
    component.revalidate();
  }

  @Override public void layoutContainer(Container parent) {
    synchronized (parent.getTreeLock()) {
      Insets insets = parent.getInsets();
      int width = parent.getWidth();
      int height = parent.getHeight();
      int top = insets.top;
      int bottom = height - insets.bottom;
      int left = insets.left;
      int right = width - insets.right;
      Component nc = getLayoutComponent(parent, BorderLayout.NORTH);
      if (Objects.nonNull(nc)) {
        Dimension d = nc.getPreferredSize();
        int vsw = UIManager.getInt("ScrollBar.width");
        nc.setBounds(right - d.width - vsw, yy - d.height, d.width, d.height);
      }
      Component cc = getLayoutComponent(parent, BorderLayout.CENTER);
      if (Objects.nonNull(cc)) {
        cc.setBounds(left, top, right - left, bottom - top);
      }
    }
  }
}

class FocusAncestorListener implements AncestorListener {
  @Override public void ancestorAdded(AncestorEvent e) {
    e.getComponent().requestFocusInWindow();
  }

  @Override public void ancestorMoved(AncestorEvent e) {
    /* not needed */
  }

  @Override public void ancestorRemoved(AncestorEvent e) {
    /* not needed */
  }
}

class FindNextAction extends AbstractAction {
  private final JTree tree;
  private final JTextField field;
  private final List<TreePath> rollOverPathLists = new ArrayList<>();

  protected FindNextAction(JTree tree, JTextField field) {
    super();
    this.tree = tree;
    this.field = field;
  }

  @Override public void actionPerformed(ActionEvent e) {
    TreePath selectedPath = tree.getSelectionPath();
    tree.clearSelection();
    rollOverPathLists.clear();
    searchTree(tree, tree.getPathForRow(0), field.getText(), rollOverPathLists);
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

  private static void searchTree(JTree tree, TreePath path, String q, List<TreePath> rollOverPathLists) {
    Object o = path.getLastPathComponent();
    if (o instanceof TreeNode) {
      TreeNode node = (TreeNode) o;
      if (node.toString().startsWith(q)) {
        rollOverPathLists.add(path);
        tree.expandPath(path.getParentPath());
      }
      if (!node.isLeaf()) {
        // Java 9: Collections.list(node.children())
        Collections.list((Enumeration<?>) node.children())
            .forEach(n -> searchTree(tree, path.pathByAddingChild(n), q, rollOverPathLists));
      }
    }
  }
}

final class AnimationUtil {
  private static final int N = 3;

  private AnimationUtil() {
    /* Singleton */
  }

  // http://www.anima-entertainment.de/math-easein-easeout-easeinout-and-bezier-curves
  // Math: EaseIn EaseOut, EaseInOut and Bezier Curves | Anima Entertainment GmbH
  // public static double easeIn(double t) {
  //   // range: 0.0 <= t <= 1.0
  //   return Math.pow(t, N);
  // }

  // public static double easeOut(double t) {
  //   return Math.pow(t - 1d, N) + 1d;
  // }

  public static double easeInOut(double t) {
    double ret;
    boolean isFirstHalf = t < .5;
    if (isFirstHalf) {
      ret = .5 * intPow(t * 2d, N);
    } else {
      ret = .5 * (intPow(t * 2d - 2d, N) + 2d);
    }
    return ret;
  }

  // http://d.hatena.ne.jp/pcl/20120617/p1
  // http://d.hatena.ne.jp/rexpit/20110328/1301305266
  // http://c2.com/cgi/wiki?IntegerPowerAlgorithm
  // http://www.osix.net/modules/article/?id=696
  public static double intPow(double da, int ib) {
    int b = ib;
    if (b < 0) {
      throw new IllegalArgumentException("B must be a positive integer or zero");
    }
    double a = da;
    double d = 1d;
    for (; b > 0; a *= a, b >>>= 1) {
      if ((b & 1) != 0) {
        d *= a;
      }
    }
    return d;
  }
}
