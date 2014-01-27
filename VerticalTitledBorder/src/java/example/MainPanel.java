package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;

public class MainPanel extends JPanel {
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
        }catch(ClassNotFoundException | InstantiationException |
               IllegalAccessException | UnsupportedLookAndFeelException ex) {
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

class VerticalTitledBorder extends TitledBorder {
    private final JLabel label;
    public VerticalTitledBorder(String title) {
        super(title);
        this.label = new JLabel(title);
        this.label.setOpaque(true);
        //this.label.putClientProperty(BasicHTML.propertyKey, null);
    }
    @Override public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        Border border = getBorder();
        String title = getTitle();
        if(title == null || title.isEmpty() || border == null) {
            super.paintBorder(c, g, x, y, width, height);
        }else{
            int edge = border instanceof TitledBorder ? 0 : EDGE_SPACING;
            JLabel label = getLabel(c);
            Dimension size = label.getPreferredSize();
            Insets insets = getBorderInsets(border, c, new Insets(0, 0, 0, 0));

            int borderX = x + edge;
            int borderY = y + edge;
            int borderW = width - edge - edge;
            int borderH = height - edge - edge;

            int labelH = size.height;
            int labelW = height - insets.top - insets.bottom; //TEST: - (edge * 8);
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
            //or: g2.transform(AffineTransform.getQuadrantRotateInstance(-1));
            label.setSize(labelW, labelH);
            label.paint(g2);
            g2.dispose();
        }
    }
    @Override public Insets getBorderInsets(Component c, Insets insets) {
        Border border = getBorder();
        Insets ins = getBorderInsets(border, c, insets);
        String title = getTitle();
        if(title != null && !title.isEmpty()) {
            int edge = border instanceof TitledBorder ? 0 : EDGE_SPACING;
            JLabel label = getLabel(c);
            Dimension size = label.getPreferredSize();
            if(ins.left < size.height) {
                ins.left = size.height - edge;
            }
            ins.top += edge + TEXT_SPACING;
            ins.left += edge + TEXT_SPACING;
            ins.right += edge + TEXT_SPACING;
            ins.bottom += edge + TEXT_SPACING;
        }
        return ins;
    }

    //Copied from TitledBorder
    private Color getColor(Component c) {
        Color color = getTitleColor();
        if(color != null) {
            return color;
        }
        color = UIManager.getColor("TitledBorder.titleColor");
        if(color != null) {
            return color;
        }
        return c == null ? null : c.getForeground();
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
    private static Insets getBorderInsets(Border border, Component c, Insets i) {
        Insets ins = new Insets(i.top, i.left, i.bottom, i.right);
        if(border == null) {
            ins.set(0, 0, 0, 0);
        }else if(border instanceof AbstractBorder) {
            AbstractBorder ab = (AbstractBorder)border;
            ins = ab.getBorderInsets(c, i);
        }else{
            ins = border.getBorderInsets(c);
        }
        return ins;
    }
}
