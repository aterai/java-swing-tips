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
        UIManager.put("Tree.closedIcon", new ColorIcon(Color.RED));
        UIManager.put("Tree.openIcon", new ColorIcon(Color.GREEN));

        JTree tree = new JTree() {
            @Override public void updateUI() {
                setCellRenderer(null);
                super.updateUI();
                setCellRenderer(new CompoundTreeCellRenderer());
                setRowHeight(0);
            }
        };
        //tree.setCellRenderer(new CompoundTreeCellRenderer());

        add(makeTitledPanel("Default", new JTree()));
        //add(makeTitledPanel("Margin", tree1));
        add(makeTitledPanel("Label", tree));
        setPreferredSize(new Dimension(320, 240));
    }
    private JComponent makeTitledPanel(String title, JTree tree) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder(title));
        p.add(new JScrollPane(tree));
        tree.setRowHeight(0);
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

class CompoundTreeCellRenderer extends DefaultTreeCellRenderer {
    private final JPanel p = new JPanel(new BorderLayout());
    private final JLabel icon = new JLabel();
    private final JLabel text = new JLabel();
    private final Border innerBorder = BorderFactory.createEmptyBorder(1, 2, 1, 2);
    private final Border emptyBorder = BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1), innerBorder);
    private final Border hasFocusBorder;

    public CompoundTreeCellRenderer() {
        super();
        Color bsColor = getBorderSelectionColor();
        Color focusBGColor = new Color(~getBackgroundSelectionColor().getRGB());
        hasFocusBorder = BorderFactory.createCompoundBorder(new DotBorder(focusBGColor, bsColor), innerBorder);

        icon.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 2));
        text.setBorder(emptyBorder);
        text.setOpaque(true);
        p.setOpaque(false);
        p.add(icon, BorderLayout.WEST);

        JPanel wrap = new JPanel(new GridBagLayout());
        wrap.setOpaque(false);
        wrap.add(text);
        p.add(wrap);
    }
    @Override public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        JLabel l = (JLabel) super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
        Color bColor;
        Color fColor;
        if (selected) {
            bColor = getBackgroundSelectionColor();
            fColor = getTextSelectionColor();
        } else {
            bColor = getBackgroundNonSelectionColor();
            fColor = getTextNonSelectionColor();
            if (bColor == null) {
                bColor = getBackground();
            }
            if (fColor == null) {
                fColor = getForeground();
            }
        }
        text.setForeground(fColor);
        text.setBackground(bColor);
        text.setBorder(hasFocus ? hasFocusBorder : emptyBorder);
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

class ColorIcon implements Icon {
    private final Color color;
    public ColorIcon(Color color) {
        this.color = color;
    }
    @Override public void paintIcon(Component c, Graphics g, int x, int y) {
        g.setColor(color);
        g.fillRoundRect(x + 1, y + 1, 22, 22, 10, 10);
    }
    @Override public int getIconWidth() {
        return 24;
    }
    @Override public int getIconHeight() {
        return 24;
    }
}
