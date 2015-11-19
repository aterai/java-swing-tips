package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.image.*;
import java.util.Objects;
import javax.swing.*;
import javax.swing.tree.*;
//import javax.swing.plaf.nimbus.*; //JDK 1.7.0

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new GridLayout(1, 2, 2, 2));
        JTree tree1 = new TranslucentTree();
        JTree tree2 = new TransparentTree();

//         //NimbusLookAndFeel(SynthLookAndFeel) JDK 1.7.0
//         UIDefaults d = new UIDefaults();
//         d.put("Tree:TreeCell[Enabled+Selected].backgroundPainter", new TransparentTreeCellPainter());
//         tree2.putClientProperty("Nimbus.Overrides", d);
//         tree2.putClientProperty("Nimbus.Overrides.InheritDefaults", false);

        add(makeTranslucentScrollPane(tree1));
        add(makeTranslucentScrollPane(tree2));

        setOpaque(false);
        setPreferredSize(new Dimension(320, 240));
    }
    private static JScrollPane makeTranslucentScrollPane(JComponent c) {
        JScrollPane scroll = new JScrollPane(c);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        return scroll;
    }
    public static void createAndShowGUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException
               | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        }
        JFrame frame = new JFrame("@title@") {
            @Override protected JRootPane createRootPane() {
                return new TransparentRootPane();
            }
        };
        Container contentPane = frame.getContentPane();
        if (contentPane instanceof JComponent) {
            ((JComponent) contentPane).setOpaque(false);
        }
        frame.getContentPane().add(new MainPanel());
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
    public static void main(String... args) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                createAndShowGUI();
            }
        });
    }
}

class TranslucentTree extends JTree {
    @Override public void updateUI() {
        super.updateUI();
        setCellRenderer(new TranslucentTreeCellRenderer());
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    }
}

class TransparentTree extends JTree {
    //http://ateraimemo.com/Swing/TreeRowSelection.html
    private static final Color SELC = new Color(100, 100, 255, 100);
    @Override public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setPaint(SELC);
        for (int i: getSelectionRows()) {
            Rectangle r = getRowBounds(i);
            g2.fillRect(0, r.y, getWidth(), r.height);
        }
        super.paintComponent(g);
        TreePath path = getLeadSelectionPath();
        if (Objects.nonNull(path)) {
            Rectangle r = getRowBounds(getRowForPath(path));
            g2.setPaint(SELC.darker());
            g2.drawRect(0, r.y, getWidth() - 1, r.height - 1);
        }
        g2.dispose();
    }
    @Override public void updateUI() {
        super.updateUI();
        setCellRenderer(new TransparentTreeCellRenderer());
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    }
}

//http://ateraimemo.com/Swing/RootPaneBackground.html
class TransparentRootPane extends JRootPane {
    private static final TexturePaint TEXTURE = makeCheckerTexture();
    private static TexturePaint makeCheckerTexture() {
        int cs = 6;
        int sz = cs * cs;
        BufferedImage img = new BufferedImage(sz, sz, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setPaint(new Color(220, 220, 220));
        g2.fillRect(0, 0, sz, sz);
        g2.setPaint(new Color(200, 200, 200, 200));
        for (int i = 0; i * cs < sz; i++) {
            for (int j = 0; j * cs < sz; j++) {
                if ((i + j) % 2 == 0) {
                    g2.fillRect(i * cs, j * cs, cs, cs);
                }
            }
        }
        g2.dispose();
        return new TexturePaint(img, new Rectangle(sz, sz));
    }
    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setPaint(TEXTURE);
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.dispose();
    }
    @Override public void updateUI() {
        super.updateUI();
        setOpaque(false);
    }
}

// http://ateraimemo.com/Swing/TreeBackgroundSelectionColor.html
class TransparentTreeCellRenderer extends DefaultTreeCellRenderer {
    private static final Color ALPHA_OF_ZERO = new Color(0x0, true);
    @Override public Component getTreeCellRendererComponent(JTree tree, Object value, boolean isSelected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        JComponent c = (JComponent) super.getTreeCellRendererComponent(tree, value, isSelected, expanded, leaf, row, false);
        c.setOpaque(false);
        return c;
    }
    @Override public Color getBackgroundNonSelectionColor() {
        return ALPHA_OF_ZERO;
    }
    @Override public Color getBackgroundSelectionColor() {
        return ALPHA_OF_ZERO;
    }
}

class TranslucentTreeCellRenderer extends TransparentTreeCellRenderer {
    private final Color backgroundSelectionColor = new Color(100, 100, 255, 100);
    @Override public Color getBackgroundSelectionColor() {
        return backgroundSelectionColor;
    }
}

//http://ateraimemo.com/Swing/NimbusColorPalette.html
// // JDK 1.7.0
// class TransparentTreeCellPainter extends AbstractRegionPainter {
//     //private PaintContext ctx = null;
//     @Override protected void doPaint(Graphics2D g, JComponent c, int width, int height, Object[] extendedCacheKeys) {
//         //Do nothing
//     }
//     @Override protected final PaintContext getPaintContext() {
//         return null;
//     }
// }
