package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import javax.swing.*;
import javax.swing.plaf.basic.*;
import javax.swing.tree.*;

public final class MainPanel extends JPanel {
    public MainPanel() {
        super(new GridLayout(1, 3));

        UIManager.put("Tree.paintLines", Boolean.TRUE);
        UIManager.put("Tree.lineTypeDashed", Boolean.TRUE);
        UIManager.put("Tree.line", Color.GREEN);
        UIManager.put("Tree.hash", Color.RED);

        JTree tree0 = new JTree();

        JTree tree1 = new JTree();
        // tree1.putClientProperty("JTree.lineStyle", "Angled");
        tree1.putClientProperty("JTree.lineStyle", "Horizontal");
        // tree1.putClientProperty("JTree.lineStyle", "None");

        JTree tree2 = new JTree() {
            @Override public void updateUI() {
                super.updateUI();
                UIManager.put("Tree.lineTypeDashed", Boolean.FALSE);
                setUI(new BasicTreeUI() {
                    private final Stroke horizontalLine = new BasicStroke(2f);
                    private final Stroke verticalLine = new BasicStroke(5f);
                    @Override public Color getHashColor() {
                        return Color.BLUE;
                    }
                    @Override protected void paintHorizontalPartOfLeg(Graphics g, Rectangle clipBounds, Insets insets, Rectangle bounds, TreePath path, int row, boolean isExpanded, boolean hasBeenExpanded, boolean isLeaf) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setStroke(horizontalLine);
                        super.paintHorizontalPartOfLeg(g2, clipBounds, insets, bounds, path, row, isExpanded, hasBeenExpanded, isLeaf);
                        g2.dispose();
                    }
                    @Override protected void paintVerticalPartOfLeg(Graphics g, Rectangle clipBounds, Insets insets, TreePath path) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setStroke(verticalLine);
                        super.paintVerticalPartOfLeg(g2, clipBounds, insets, path);
                        g2.dispose();
                    }
                });
            }
        };

        add(makeTitledPanel("lineTypeDashed", tree0));
        add(makeTitledPanel("lineStyle",      tree1));
        add(makeTitledPanel("BasicTreeUI",    tree2));
        setPreferredSize(new Dimension(320, 240));
    }
    private JComponent makeTitledPanel(String title, JTree tree) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder(title));
        p.add(new JScrollPane(tree));
        return p;
    }
    public static void main(String... args) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                createAndShowGUI();
            }
        });
    }
    public static void createAndShowGUI() {
        JFrame frame = new JFrame("@title@");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
