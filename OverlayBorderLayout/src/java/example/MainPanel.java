package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

public class MainPanel extends JPanel {
    protected final JTree tree = new JTree();
    protected final JTextField field = new JTextField("b", 10) {
        protected transient AncestorListener listener;
        @Override public void updateUI() {
            removeAncestorListener(listener);
            super.updateUI();
            listener = new AncestorListener() {
                @Override public void ancestorAdded(AncestorEvent e) {
                    e.getComponent().requestFocusInWindow();
                }
                @Override public void ancestorMoved(AncestorEvent e) { /* not needed */ }
                @Override public void ancestorRemoved(AncestorEvent e) { /* not needed */ }
            };
            addAncestorListener(listener);
        }
    };
    protected final JPanel searchBox = new JPanel(new BorderLayout());
    protected boolean isHidden = true;
    protected int yy;
    protected int counter;
    protected final Timer animator = new Timer(5, null);
    protected final Action showHideAction = new AbstractAction("Show/Hide Search Box") {
        @Override public void actionPerformed(ActionEvent e) {
            if (!animator.isRunning()) {
                // isHidden = !searchBox.isVisible();
                isHidden = searchBox.isVisible() ^ true;
                searchBox.setVisible(true);
                animator.start();
            }
        }
    };
    protected final Action hideAction = new AbstractAction("Hide Search Box") {
        @Override public void actionPerformed(ActionEvent e) {
            if (!animator.isRunning()) {
                isHidden = false;
                animator.start();
            }
        }
    };
    public MainPanel() {
        super(new BorderLayout());

        animator.addActionListener(e -> {
            int height = searchBox.getPreferredSize().height;
            double h = (double) height;
            if (isHidden) {
                yy = (int) (.5 + AnimationUtil.easeInOut(++counter / h) * h);
                if (yy >= height) {
                    yy = height;
                    animator.stop();
                }
            } else {
                yy = (int) (.5 + AnimationUtil.easeInOut(--counter / h) * h);
                if (yy <= 0) {
                    yy = 0;
                    animator.stop();
                    searchBox.setVisible(false);
                }
            }
            searchBox.revalidate();
        });

        JPanel p = new JPanel() {
            @Override public boolean isOptimizedDrawingEnabled() {
                return false;
            }
        };
        p.setLayout(new BorderLayout() {
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
        });
        p.add(searchBox, BorderLayout.NORTH);
        p.add(new JScrollPane(tree));

        tree.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        Action findNextAction = new FindNextAction(tree, field);
        JButton button = new JButton(findNextAction);
        button.setFocusable(false);
        button.setToolTipText("Find next");
        button.setText("v");

        searchBox.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        searchBox.add(field);
        searchBox.add(button, BorderLayout.EAST);
        searchBox.setVisible(false);

        InputMap imap = p.getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), "open-searchbox");
        imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "close-searchbox");
        p.getActionMap().put("open-searchbox", showHideAction);
        p.getActionMap().put("close-searchbox", hideAction);

        field.getActionMap().put("find-next", findNextAction);
        field.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "find-next");

        add(p);
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
        } catch (ClassNotFoundException | InstantiationException
               | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
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
            if (!node.isLeaf() && node.getChildCount() >= 0) {
                // Java 9: Enumeration<TreeNode> e = node.children();
                Enumeration<?> e = node.children();
                while (e.hasMoreElements()) {
                    searchTree(tree, path.pathByAddingChild(e.nextElement()), q, rollOverPathLists);
                }
            }
        }
    }
}

final class AnimationUtil {
    private static final int N = 3;
    private AnimationUtil() { /* Singleton */ }
    // http://www.anima-entertainment.de/math-easein-easeout-easeinout-and-bezier-curves
    // Math: EaseIn EaseOut, EaseInOut and Bezier Curves | Anima Entertainment GmbH
    public static double easeIn(double t) {
        // range: 0.0 <= t <= 1.0
        return Math.pow(t, N);
    }
    public static double easeOut(double t) {
        return Math.pow(t - 1d, N) + 1d;
    }
    public static double easeInOut(double t) {
        double ret;
        boolean isFirstHalf = t < .5;
        if (isFirstHalf) {
            ret = .5 * intpow(t * 2d, N);
        } else {
            ret = .5 * (intpow(t * 2d - 2d, N) + 2d);
        }
        return ret;
    }
    // http://d.hatena.ne.jp/pcl/20120617/p1
    // http://d.hatena.ne.jp/rexpit/20110328/1301305266
    // http://c2.com/cgi/wiki?IntegerPowerAlgorithm
    // http://www.osix.net/modules/article/?id=696
    public static double intpow(double da, int ib) {
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
