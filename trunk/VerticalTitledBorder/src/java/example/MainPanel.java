package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.geom.*;
// import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
// import javax.swing.tree.*;
// import javax.swing.plaf.basic.*;

public class MainPanel extends JPanel{
    public MainPanel() {
        super(new GridLayout(1,3,5,5));

        JPanel p1 = new JPanel(new BorderLayout());
        p1.add(new JScrollPane(new JTree()));
        p1.setBorder(new TitledBorder("TitledBorder 1234567890"));

        JPanel p2 = new JPanel(new BorderLayout());
        p2.add(new JScrollPane(new JTree()));
        p2.setBorder(new VerticalTitledBorder("VerticalTitledBorder 1234567890"));

        JPanel p3 = new JPanel(new BorderLayout());
        p3.add(new JScrollPane(new JTree()));
        p3.setBorder(new TitledBorder(new VerticalTitledBorder("VerticalTitledBorder"), "TitledBorder"));

        add(p1);
        add(p2);
        add(p3);
        setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        setPreferredSize(new Dimension(320, 240));
    }
    private JComponent makeTitledPanel(String title, JTree tree) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder(title));
        p.add(new JScrollPane(tree));
        tree.setRowHeight(0);
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

class VerticalTitledBorder extends TitledBorder{
    private final JLabel label;
    public VerticalTitledBorder(String title) {
        super(title);
        this.label = new JLabel(title);
        this.label.setOpaque(true);
        //this.label.putClientProperty(BasicHTML.propertyKey, null);
    }
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        Border border = getBorder();
        String title = getTitle();
        if((title != null) && !title.isEmpty() && border != null) {
            int edge = (border instanceof TitledBorder) ? 0 : EDGE_SPACING;
            JLabel label = getLabel(c);
            Dimension size = label.getPreferredSize();
            Insets insets = getBorderInsets(border, c, new Insets(0, 0, 0, 0));

            int borderX = x + edge;
            int borderY = y + edge;
            int borderW = width - edge - edge;
            int borderH = height - edge - edge;

            int labelH = size.height;
            int labelW = height - insets.top - insets.bottom;
            if(labelW > size.width) {
                labelW = size.width;
            }

            int ileft = edge + insets.left/2 - labelH/2;
            if(ileft < edge) {
                borderX -= ileft;
                borderW += ileft;
            }
            border.paintBorder(c, g, borderX, borderY, borderW, borderH);

            Graphics2D g2 = (Graphics2D)g.create();
            g2.translate(0, (height+labelW)/2);
            g2.rotate(Math.toRadians(-90));
            //g2.transform(AffineTransform.getQuadrantRotateInstance(-1));
            label.setSize(labelW, labelH);
            label.paint(g2);
            g2.dispose();
        } else {
            super.paintBorder(c, g, x, y, width, height);
        }
    }
    public Insets getBorderInsets(Component c, Insets insets) {
        Border border = getBorder();
        insets = getBorderInsets(border, c, insets);
        String title = getTitle();
        if((title != null) && !title.isEmpty()) {
            int edge = (border instanceof TitledBorder) ? 0 : EDGE_SPACING;
            JLabel label = getLabel(c);
            Dimension size = label.getPreferredSize();
            if(insets.left < size.height) {
                insets.left = size.height - edge;
            }
            insets.top += edge + TEXT_SPACING;
            insets.left += edge + TEXT_SPACING;
            insets.right += edge + TEXT_SPACING;
            insets.bottom += edge + TEXT_SPACING;
        }
        return insets;
    }

    //Copy from TitledBorder
    private Color getColor(Component c) {
        Color color = getTitleColor();
        if(color != null) {
            return color;
        }
        color = UIManager.getColor("TitledBorder.titleColor");
        if(color != null) {
            return color;
        }
        return (c != null) ? c.getForeground() : null;
    }
    private JLabel getLabel(Component c) {
        this.label.setText(getTitle());
        this.label.setFont(getFont(c));
        this.label.setForeground(getColor(c));
        this.label.setComponentOrientation(c.getComponentOrientation());
        this.label.setEnabled(c.isEnabled());
        this.label.setBackground(c.getBackground()); //???
        return this.label;
    }
    private static Insets getBorderInsets(Border border, Component c, Insets insets) {
        if(border == null) {
            insets.set(0, 0, 0, 0);
        } else if(border instanceof AbstractBorder) {
            AbstractBorder ab = (AbstractBorder) border;
            insets = ab.getBorderInsets(c, insets);
        } else {
            Insets i = border.getBorderInsets(c);
            insets.set(i.top, i.left, i.bottom, i.right);
        }
        return insets;
    }
}
