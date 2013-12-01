package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

public class MainPanel extends JPanel {
    private final JPanel breadcrumb = makePanel(10);
    private final JTree tree = new JTree();
    public MainPanel() {
        super(new BorderLayout());
        tree.setSelectionRow(0);
        tree.addTreeSelectionListener(new TreeSelectionListener() {
            @Override public void valueChanged(TreeSelectionEvent e) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode)tree.getLastSelectedPathComponent();
                if(node == null || node.isLeaf()) {
                    return;
                }else{
                    initBreadcrumbList(breadcrumb, tree);
                    breadcrumb.revalidate();
                    breadcrumb.repaint();
                }
            }
        });

        initBreadcrumbList(breadcrumb, tree);
        add(breadcrumb, BorderLayout.NORTH);

        JComponent c = makeBreadcrumbList(tree, Arrays.asList("aaa", "bb", "c"));
        add(c, BorderLayout.SOUTH);
        add(new JScrollPane(tree));
        setPreferredSize(new Dimension(320, 240));
    }
    private static JPanel makePanel(int overlap) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEADING, -overlap, 0));
        p.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        p.setOpaque(false);
        return p;
    }
    private static void initBreadcrumbList(JPanel p, JTree tree) {
        p.removeAll();
        ButtonGroup bg = new ButtonGroup();
        TreePath tp = tree.getSelectionPath();
        Object[] list = tp.getPath();
        ArrayList<Object> al = new ArrayList<>();
        for(int i=0;i<list.length;i++) {
            al.add(list[i]);
            TreePath cur = new TreePath(al.toArray());
            AbstractButton b = makeButton(tree, cur, Color.ORANGE);
            p.add(b);
            bg.add(b);
        }
    }
    private static JComponent makeBreadcrumbList(JTree tree, List<String> list) {
        JPanel p = makePanel(5);
        ButtonGroup bg = new ButtonGroup();
        for(String title: list) {
            AbstractButton b = makeButton(null, new TreePath(title), Color.PINK);
            p.add(b);
            bg.add(b);
        }
        return p;
    }
    private static AbstractButton makeButton(final JTree tree, final TreePath path, Color color) {
        final ToggleButtonBarCellIcon icon = new ToggleButtonBarCellIcon();
        AbstractButton b = new JRadioButton(path.getLastPathComponent().toString()) {
            @Override public boolean contains(int x, int y) {
                if(icon==null || icon.area==null) {
                    return super.contains(x, y);
                }else{
                    return icon.area.contains(x, y);
                }
            }
        };
        if(tree!=null) {
            b.addActionListener(new ActionListener() {
                @Override public void actionPerformed(ActionEvent e) {
                    JRadioButton r = (JRadioButton)e.getSource();
                    tree.setSelectionPath(path);
                    r.setSelected(true);
                }
            });
        }
        b.setIcon(icon);
        b.setVerticalAlignment(SwingConstants.CENTER);
        b.setVerticalTextPosition(SwingConstants.CENTER);
        b.setHorizontalAlignment(SwingConstants.CENTER);
        b.setHorizontalTextPosition(SwingConstants.CENTER);
        b.setBorder(BorderFactory.createEmptyBorder());
        b.setContentAreaFilled(false);
        b.setFocusPainted(false);
        b.setOpaque(false);
        b.setBorder(BorderFactory.createEmptyBorder());
        b.setBackground(color);
        return b;
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

//http://terai.xrea.jp/Swing/ToggleButtonBar.html
class ToggleButtonBarCellIcon implements Icon {
    public Shape area;
    private static final Color TL = new Color(1f,1f,1f,.2f);
    private static final Color BR = new Color(0f,0f,0f,.2f);
    private static final Color ST = new Color(1f,1f,1f,.4f);
    private static final Color SB = new Color(1f,1f,1f,.1f);

    private Color ssc;
    private Color bgc;

    @Override public void paintIcon(Component c, Graphics g, int x, int y) {
        Container parent = c.getParent();
        if(parent==null) {
            return;
        }
        int r = 2;
        int h = c.getHeight()-1;
        int h2 = h/2;
        int w = c.getWidth()-1-h2;
        x += h2;

        Graphics2D g2 = (Graphics2D)g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Path2D.Float p = new Path2D.Float();
        if(c==parent.getComponent(0)) {
            //:first-child
            p.moveTo(x, y);
            p.lineTo(x + w - h2, y);
            p.lineTo(x + w,      y + h2);
            p.lineTo(x + w - h2, y + h);
            p.lineTo(x,          y + h);
        }else{
            p.moveTo(x - h2,     y);
            p.lineTo(x + w - h2, y);
            p.lineTo(x + w,      y + h2);
            p.lineTo(x + w - h2, y + h);
            p.lineTo(x - h2,     y + h);
            p.lineTo(x,          y + h2);
        }
        p.closePath();
        area = p;

        g2.setPaint(c.getBackground());
        g2.fill(area);

        ssc = TL;
        bgc = BR;
        Color borderColor = Color.GRAY.brighter();
        if(c instanceof AbstractButton) {
            ButtonModel m = ((AbstractButton)c).getModel();
            if(m.isSelected() || m.isRollover()) {
                ssc = ST;
                bgc = SB;
                borderColor = Color.GRAY;
            }
        }
        g2.setPaint(new GradientPaint(x, y, ssc, x, y+h, bgc, true));
        g2.fill(area);
        g2.setPaint(BR);
        g2.draw(area);
        g2.setPaint(borderColor);
        g2.draw(area);
        g2.dispose();
    }
    @Override public int getIconWidth()  {
        return 100;
    }
    @Override public int getIconHeight() {
        return 20;
    }
}
