package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.tree.*;
import javax.swing.plaf.basic.*;

public class MainPanel extends JPanel {
    public MainPanel() {
        super(new GridLayout(1,3));
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
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                createAndShowGUI();
            }
        });
    }
    public static void createAndShowGUI() {
        try{
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }catch(Exception e) {
            e.printStackTrace();
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
    private final Border border;

    public MyTreeCellRenderer() {
        super();
        Border outside = getBorder();
        Border inside  = BorderFactory.createEmptyBorder(0,20,0,2);
        border = BorderFactory.createCompoundBorder(outside, inside);
        setOpaque(false);
    }
    //protected boolean selected;
    //protected boolean hasFocus;

    private boolean drawsFocusBorderAroundIcon;
    private boolean drawDashedFocusIndicator = true;
    private boolean isDropCell;
    private boolean fillBackground = true;

    private Color treeBGColor;
    private Color focusBGColor;

    int w = 2; // < 3

    @Override public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel,
                                                  boolean expanded, boolean leaf, int row, boolean hasFocus) {
        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, false);
        //this.tree = tree;
        this.hasFocus = hasFocus;
        this.selected = sel;
        return this;
    }
    @Override public void paint(Graphics g) {
        if(!getComponentOrientation().isLeftToRight()) {
            super.paint(g);
            return;
        }

        Color bColor;
        if(selected) {
            bColor = getBackgroundSelectionColor();
        }else{
            bColor = getBackgroundNonSelectionColor();
            if(bColor == null) {
                bColor = getBackground();
            }
        }

        int imageOffset = -1;
        if(bColor != null && fillBackground) {
            imageOffset = getLabelStart();
            g.setColor(bColor);
            if(getComponentOrientation().isLeftToRight()) {
                g.fillRect(imageOffset - w, 0, getWidth() + w - imageOffset, getHeight());
//             }else{
//                 //XXX
//                 g.fillRect(0, 0, getWidth() - imageOffset, getHeight());
            }
        }

        //g.translate(w, 0);
        //boolean flag = selected;
        //selected = false;
        super.paint(g);
        //g.translate(-2,0);
        //selected = flag;

        if(hasFocus) {
            if(drawsFocusBorderAroundIcon) {
                imageOffset = 0;
            }else if(imageOffset == -1) {
                imageOffset = getLabelStart();
            }
            if(getComponentOrientation().isLeftToRight()) {
                g.setColor(bColor);
                g.fillRect(imageOffset - w, 0, w + 1, getHeight());
                paintFocus(g, imageOffset - w, 0, getWidth() + w - imageOffset, getHeight(), bColor);
//             }else{
//                 //XXX
//                 paintFocus(g, 0, 0, getWidth() - imageOffset, getHeight(), bColor);
            }
        }
    }
    private void paintFocus(Graphics g, int x, int y, int w, int h, Color notColor) {
        Color bsColor = getBorderSelectionColor();
        boolean b = selected || !drawDashedFocusIndicator;
        if(bsColor != null && b) {
            g.setColor(bsColor);
            g.drawRect(x, y, w - 1, h - 1);
        }
        if(drawDashedFocusIndicator && notColor != null) {
            if(treeBGColor != notColor) {
                treeBGColor = notColor;
                focusBGColor = new Color(~notColor.getRGB());
            }
            g.setColor(focusBGColor);
            BasicGraphicsUtils.drawDashedRect(g, x, y, w, h);
        }
    }
    private int getLabelStart() {
        Icon currentI = getIcon();
        if(currentI != null && getText() != null) {
            return currentI.getIconWidth() + Math.max(0, getIconTextGap() - 1);
        }
        return 0;
    }
}

class CompoundTreeCellRenderer extends DefaultTreeCellRenderer {
    private final JPanel p = new JPanel(new BorderLayout());
    private final JLabel icon = new JLabel();
    private final JLabel text = new JLabel();
    private final Border innerBorder = BorderFactory.createEmptyBorder(1,2,1,2);
    private final Border emptyBorder = BorderFactory.createCompoundBorder(
                                            BorderFactory.createEmptyBorder(1,1,1,1), innerBorder);
    private final Border hasFocusBorder;

    public CompoundTreeCellRenderer() {
        super();
        Color bsColor = getBorderSelectionColor();
        Color focusBGColor = new Color(~getBackgroundSelectionColor().getRGB());
        hasFocusBorder = BorderFactory.createCompoundBorder(new DotBorder(focusBGColor, bsColor), innerBorder);

        icon.setBorder(BorderFactory.createEmptyBorder(0,0,0,2));
        text.setBorder(emptyBorder);
        text.setOpaque(true);
        p.setOpaque(false);
        p.add(icon, BorderLayout.WEST);
        p.add(text);
    }
    @Override public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        JLabel l = (JLabel)super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
        Color bColor, fColor;
        if(selected) {
            bColor = getBackgroundSelectionColor();
            fColor = getTextSelectionColor();
        }else{
            bColor = getBackgroundNonSelectionColor();
            fColor = getTextNonSelectionColor();
            if(bColor == null) bColor = getBackground();
            if(fColor == null) fColor = getForeground();
        }
        text.setForeground(fColor);
        text.setBackground(bColor);
        text.setBorder(hasFocus?hasFocusBorder:emptyBorder);
        text.setText(l.getText());
        icon.setIcon(l.getIcon());

        return p;
    }
    @Override public void paint(Graphics g) {}
}
class DotBorder extends LineBorder {
    private final Color borderSelectionColor;
    public DotBorder(Color color, Color borderSelectionColor) {
        super(color, 1);
        this.borderSelectionColor = borderSelectionColor;
    }
    @Override public boolean isBorderOpaque() { return true; }
    @Override public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
        Graphics2D g2 = (Graphics2D)g;
        //g2.translate(x,y);
        g2.setPaint(borderSelectionColor);
        g2.drawRect(x, y, w-1, h-1);
        g2.setPaint(getLineColor());
        javax.swing.plaf.basic.BasicGraphicsUtils.drawDashedRect(g2, x, y, w, h);
        //g2.translate(-x,-y);
    }
}
