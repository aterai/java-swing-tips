package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.Objects;
import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new GridLayout(2, 1));
        JTree tree1 = new JTree() {
            @Override public String getToolTipText(MouseEvent e) {
                Object o = null;
                TreePath path = getPathForLocation(e.getX(), e.getY());
                if (Objects.nonNull(path)) {
                    o = path.getLastPathComponent();
                }
                return Objects.toString(o, "getToolTipText");
            }
        };
        ToolTipManager.sharedInstance().registerComponent(tree1);

        JTree tree2 = new JTree();
        tree2.setCellRenderer(new DefaultTreeCellRenderer() {
            // private void init() {
            //     setLeafIcon(sun.swing.DefaultLookup.getIcon(this, ui, "Tree.leafIcon"));
            //     setClosedIcon(sun.swing.DefaultLookup.getIcon(this, ui, "Tree.closedIcon"));
            //     setOpenIcon(sun.swing.DefaultLookup.getIcon(this, ui, "Tree.openIcon"));
            //     setTextSelectionColor(sun.swing.DefaultLookup.getColor(this, ui, "Tree.selectionForeground"));
            //     setTextNonSelectionColor(sun.swing.DefaultLookup.getColor(this, ui, "Tree.textForeground"));
            //     setBackgroundSelectionColor(sun.swing.DefaultLookup.getColor(this, ui, "Tree.selectionBackground"));
            //     setBackgroundNonSelectionColor(sun.swing.DefaultLookup.getColor(this, ui, "Tree.textBackground"));
            //     setBorderSelectionColor(sun.swing.DefaultLookup.getColor(this, ui, "Tree.selectionBorderColor"));
            //     // drawsFocusBorderAroundIcon = sun.swing.DefaultLookup.getBoolean(this, ui, "Tree.drawsFocusBorderAroundIcon", false);
            //     // drawDashedFocusIndicator = sun.swing.DefaultLookup.getBoolean(this, ui, "Tree.drawDashedFocusIndicator", false);
            //     // fillBackground = sun.swing.DefaultLookup.getBoolean(this, ui, "Tree.rendererFillBackground", true);
            //     Insets margins = sun.swing.DefaultLookup.getInsets(this, ui, "Tree.rendererMargins");
            //     if (margins != null) {
            //         setBorder(BorderFactory.createEmptyBorder(margins.top, margins.left, margins.bottom, margins.right));
            //     }
            // }
            // @Override public void updateUI() {
            //     super.updateUI();
            //     init();
            // }
            @Override public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                JComponent c = (JComponent) super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
                c.setToolTipText(Objects.nonNull(value) ? "TreeCellRenderer: " + value.toString() : null);
                return c;
            }
        });
        // tree2.setToolTipText("dummy");
        ToolTipManager.sharedInstance().registerComponent(tree2);

        add(makeTitledPanel("Override getToolTipText", new JScrollPane(tree1)));
        add(makeTitledPanel("Use TreeCellRenderer", new JScrollPane(tree2)));
        setPreferredSize(new Dimension(320, 240));
    }
    private static Component makeTitledPanel(String title, Component c) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder(title));
        p.add(c);
        return p;
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
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
