package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.basic.BasicGraphicsUtils;
import javax.swing.tree.*;

public final class MainPanel extends JPanel {
    public MainPanel() {
        super(new GridLayout(1, 3));
        JTree tree1 = new JTree() {
            @Override public void updateUI() {
                setCellRenderer(null);
                super.updateUI();
                setCellRenderer(new MyTreeCellRenderer());
            }
        };
        //tree1.setCellRenderer(new MyTreeCellRenderer());

        JTree tree2 = new JTree() {
            @Override public void updateUI() {
                setCellRenderer(null);
                super.updateUI();
                setCellRenderer(new CompoundTreeCellRenderer());
            }
        };
        //tree2.setCellRenderer(new CompoundTreeCellRenderer());

        add(makeTitledPanel("Default", new JTree()));
        add(makeTitledPanel("Margin", tree1));
        add(makeTitledPanel("Label",  tree2));
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

class MyTreeCellRenderer extends DefaultTreeCellRenderer {
    private static final int MARGIN = 2; // < 3
    protected final boolean drawsFocusBorderAroundIcon;
    protected final boolean drawDashedFocusIndicator;
    protected final boolean fillBackground;
    protected Color treeBGColor;
    protected Color focusBGColor;
    //protected boolean selected;
    //protected boolean hasFocus;

    public MyTreeCellRenderer() {
        super();
        drawsFocusBorderAroundIcon = UIManager.getBoolean("Tree.drawsFocusBorderAroundIcon");
        drawDashedFocusIndicator   = UIManager.getBoolean("Tree.drawDashedFocusIndicator");
        fillBackground             = UIManager.getBoolean("Tree.rendererFillBackground");
        setOpaque(fillBackground);
    }

    @Override public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, false);
        //this.tree = tree;
        this.hasFocus = hasFocus;
        this.selected = selected;
        return this;
    }
    @Override public void paint(Graphics g) {
        if (!getComponentOrientation().isLeftToRight()) {
            super.paint(g);
            return;
        }

        Color bColor;
        if (selected) {
            bColor = getBackgroundSelectionColor();
        } else {
            bColor = getBackgroundNonSelectionColor();
            if (bColor == null) {
                bColor = getBackground();
            }
        }

        int imageOffset = -1;
        if (bColor != null && fillBackground) {
            imageOffset = getLabelStart();
            g.setColor(bColor);
            g.fillRect(imageOffset - MARGIN, 0, getWidth() + MARGIN - imageOffset, getHeight());
        }

        //g.translate(MARGIN, 0);
        //boolean flag = selected;
        //selected = false;
        super.paint(g);
        //g.translate(-2, 0);
        //selected = flag;

        if (hasFocus) {
            if (drawsFocusBorderAroundIcon) {
                imageOffset = 0;
            } else if (imageOffset == -1) {
                imageOffset = getLabelStart();
            }
            g.setColor(bColor);
            g.fillRect(imageOffset - MARGIN, 0, MARGIN + 1, getHeight());
            paintFocus(g, imageOffset - MARGIN, 0, getWidth() + MARGIN - imageOffset, getHeight(), bColor);
        }
    }
    private void paintFocus(Graphics g, int x, int y, int w, int h, Color notColor) {
        Color bsColor = getBorderSelectionColor();
        boolean b = selected || !drawDashedFocusIndicator;
        if (bsColor != null && b) {
            g.setColor(bsColor);
            g.drawRect(x, y, w - 1, h - 1);
        }
        if (drawDashedFocusIndicator && notColor != null) {
            if (!notColor.equals(treeBGColor)) {
                treeBGColor = notColor;
                focusBGColor = new Color(~notColor.getRGB());
            }
            g.setColor(focusBGColor);
            BasicGraphicsUtils.drawDashedRect(g, x, y, w, h);
        }
    }
    private int getLabelStart() {
        Icon currentI = getIcon();
        if (currentI != null && getText() != null) {
            return currentI.getIconWidth() + Math.max(0, getIconTextGap() - 1);
        }
        return 0;
    }
}

class CompoundTreeCellRenderer extends DefaultTreeCellRenderer {
    private final JPanel p = new JPanel(new BorderLayout());
    private final JLabel icon = new JLabel();
    private final JLabel text = new JLabel();
    private final Border innerBorder = BorderFactory.createEmptyBorder(1, 2, 1, 2);
    private final Border emptyBorder = BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1), innerBorder);
    private final Border compoundFocusBorder;
    private final boolean isSynth;

    public CompoundTreeCellRenderer() {
        super();

        isSynth = getUI().getClass().getName().contains("Synth");
        if (isSynth) {
            compoundFocusBorder = emptyBorder;
        } else {
            Color bsColor = getBorderSelectionColor();

            boolean drawDashedFocusIndicator = UIManager.getBoolean("Tree.drawDashedFocusIndicator");
            Border b;
            if (drawDashedFocusIndicator) {
                b = new DotBorder(new Color(~getBackgroundSelectionColor().getRGB()), bsColor);
            } else {
                b = BorderFactory.createLineBorder(bsColor);
            }
            compoundFocusBorder = BorderFactory.createCompoundBorder(b, innerBorder);
        }

        icon.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 2));
        text.setBorder(emptyBorder);
        text.setOpaque(true);
        p.setOpaque(false);
        p.add(icon, BorderLayout.WEST);
        p.add(text);
    }
    @Override public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        JLabel l = (JLabel) super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
        Color bColor;
        Color fColor;
        if (selected) {
            bColor = getBackgroundSelectionColor();
            fColor = getTextSelectionColor();
            if (isSynth) {
                text.setOpaque(false);
            } else {
                text.setOpaque(true);
            }
        } else {
            bColor = getBackgroundNonSelectionColor();
            fColor = getTextNonSelectionColor();
            if (bColor == null) {
                bColor = getBackground();
            }
            if (fColor == null) {
                fColor = getForeground();
            }
            text.setOpaque(false);
        }
        text.setForeground(fColor);
        text.setBackground(bColor);
        text.setBorder(hasFocus ? compoundFocusBorder : emptyBorder);
        text.setText(l.getText());
        icon.setIcon(l.getIcon());

        return p;
    }
    //@Override public void paint(Graphics g) {}
}

class DotBorder extends LineBorder {
    private final Color borderSelectionColor;
    public DotBorder(Color color, Color borderSelectionColor) {
        super(color, 1);
        this.borderSelectionColor = borderSelectionColor;
    }
    @Override public boolean isBorderOpaque() {
        return true;
    }
    @Override public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
        Graphics2D g2 = (Graphics2D) g.create();
        //g2.translate(x, y);
        g2.setPaint(borderSelectionColor);
        g2.drawRect(x, y, w - 1, h - 1);
        g2.setPaint(getLineColor());
        BasicGraphicsUtils.drawDashedRect(g2, x, y, w, h);
        //g2.translate(-x, -y);
        g2.dispose();
    }
}
