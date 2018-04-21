package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.basic.BasicGraphicsUtils;
import javax.swing.tree.*;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new GridLayout(1, 3));
        JTree tree1 = new JTree() {
            @Override public void updateUI() {
                setCellRenderer(null);
                super.updateUI();
                setCellRenderer(new MarginTreeCellRenderer());
            }
        };
        // tree1.setCellRenderer(new MarginTreeCellRenderer());

        JTree tree2 = new JTree() {
            @Override public void updateUI() {
                setCellRenderer(null);
                super.updateUI();
                setCellRenderer(new CompoundTreeCellRenderer());
            }
        };
        // tree2.setCellRenderer(new CompoundTreeCellRenderer());

        add(makeTitledPanel("Default", new JScrollPane(new JTree())));
        add(makeTitledPanel("Margin", new JScrollPane(tree1)));
        add(makeTitledPanel("Label", new JScrollPane(tree2)));
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

class MarginTreeCellRenderer extends DefaultTreeCellRenderer {
    private static final int MARGIN = 2; // < 3
    protected boolean drawsFocusBorderAroundIcon;
    protected boolean drawDashedFocusIndicator;
    protected boolean fillBackground;
    protected Color treeBgsColor;
    protected Color focusBgsColor;
    // protected boolean selected;
    // protected boolean hasFocus;

    @Override public void updateUI() {
        super.updateUI();
        drawsFocusBorderAroundIcon = UIManager.getBoolean("Tree.drawsFocusBorderAroundIcon");
        drawDashedFocusIndicator = UIManager.getBoolean("Tree.drawDashedFocusIndicator");
        fillBackground = UIManager.getBoolean("Tree.rendererFillBackground");
        setOpaque(fillBackground);
    }

    @Override public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, false);
        // this.tree = tree;
        this.hasFocus = hasFocus;
        this.selected = selected;
        return this;
    }
    @Override public void paint(Graphics g) {
        if (!getComponentOrientation().isLeftToRight()) {
            super.paint(g);
            return;
        }

        Color bgColor;
        if (selected) {
            bgColor = getBackgroundSelectionColor();
        } else {
            bgColor = Optional.ofNullable(getBackgroundNonSelectionColor()).orElse(getBackground());
        }

        int imageOffset = -1;
        if (Objects.nonNull(bgColor) && fillBackground) {
            imageOffset = getLabelStart();
            g.setColor(bgColor);
            g.fillRect(imageOffset - MARGIN, 0, getWidth() + MARGIN - imageOffset, getHeight());
        }

        // g.translate(MARGIN, 0);
        // boolean flag = selected;
        // selected = false;
        super.paint(g);
        // g.translate(-2, 0);
        // selected = flag;

        if (hasFocus) {
            if (drawsFocusBorderAroundIcon) {
                imageOffset = 0;
            } else if (imageOffset == -1) {
                imageOffset = getLabelStart();
            }
            g.setColor(bgColor);
            g.fillRect(imageOffset - MARGIN, 0, MARGIN + 1, getHeight());
            paintFocus(g, imageOffset - MARGIN, 0, getWidth() + MARGIN - imageOffset, getHeight(), bgColor);
        }
    }
    private void paintFocus(Graphics g, int x, int y, int w, int h, Color notColor) {
        Color bsColor = getBorderSelectionColor();
        boolean b = selected || !drawDashedFocusIndicator;
        if (Objects.nonNull(bsColor) && b) {
            g.setColor(bsColor);
            g.drawRect(x, y, w - 1, h - 1);
        }
        if (drawDashedFocusIndicator && Objects.nonNull(notColor)) {
            if (!notColor.equals(treeBgsColor)) {
                treeBgsColor = notColor;
                focusBgsColor = new Color(~notColor.getRGB());
            }
            g.setColor(focusBgsColor);
            BasicGraphicsUtils.drawDashedRect(g, x, y, w, h);
        }
    }
    private int getLabelStart() {
        return Optional.ofNullable(getIcon())
            .filter(icon -> Objects.nonNull(getText()))
            .map(icon -> icon.getIconWidth() + Math.max(0, getIconTextGap() - 1))
            .orElse(0);
    }
}

class CompoundTreeCellRenderer extends DefaultTreeCellRenderer {
    private final JPanel renderer = new JPanel(new BorderLayout());
    private final JLabel icon = new JLabel();
    private final JLabel text = new JLabel();
    private final Border innerBorder = BorderFactory.createEmptyBorder(1, 2, 1, 2);
    private final Border emptyBorder = BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1), innerBorder);
    private final Border compoundFocusBorder;
    private final boolean isSynth;

    protected CompoundTreeCellRenderer() {
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
        renderer.setOpaque(false);
        renderer.add(icon, BorderLayout.WEST);
        renderer.add(text);
    }
    @Override public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        Color bgColor;
        Color fgColor;
        if (selected) {
            bgColor = getBackgroundSelectionColor();
            fgColor = getTextSelectionColor();
            if (isSynth) {
                text.setOpaque(false);
            } else {
                text.setOpaque(true);
            }
        } else {
            bgColor = Optional.ofNullable(getBackgroundNonSelectionColor()).orElse(getBackground());
            fgColor = Optional.ofNullable(getTextNonSelectionColor()).orElse(getForeground());
            text.setOpaque(false);
        }
        text.setForeground(fgColor);
        text.setBackground(bgColor);
        text.setBorder(hasFocus ? compoundFocusBorder : emptyBorder);

        JLabel l = (JLabel) super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
        text.setText(l.getText());
        icon.setIcon(l.getIcon());

        return renderer;
    }
    // @Override public void paint(Graphics g) {}
}

class DotBorder extends LineBorder {
    private final Color borderSelectionColor;
    protected DotBorder(Color color, Color borderSelectionColor) {
        super(color, 1);
        this.borderSelectionColor = borderSelectionColor;
    }
    @Override public boolean isBorderOpaque() {
        return true;
    }
    @Override public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.translate(x, y);
        g2.setPaint(borderSelectionColor);
        g2.drawRect(0, 0, w - 1, h - 1);
        g2.setPaint(getLineColor());
        BasicGraphicsUtils.drawDashedRect(g2, 0, 0, w, h);
        g2.dispose();
    }
}
