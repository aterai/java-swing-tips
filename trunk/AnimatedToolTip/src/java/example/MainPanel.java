package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.*;

class MainPanel extends JPanel {
    private final URL url = getClass().getResource("anime.gif");
    private final ImageIcon icon = new ImageIcon(url);
    private MainPanel() {
        super(new BorderLayout());
        JLabel l1 = new JLabel("Timer Animated ToolTip") {
            @Override public JToolTip createToolTip() {
                JToolTip tip = new AnimatedToolTip(new AnimatedLabel(""));
                tip.setComponent(this);
                return tip;
            }
        };
        l1.setToolTipText("Test1");

        JLabel l2 = new JLabel("Gif Animated ToolTip") {
            @Override public JToolTip createToolTip() {
                JToolTip tip = new AnimatedToolTip(new JLabel("", icon, SwingConstants.LEFT));
                tip.setComponent(this);
                return tip;
            }
        };
        l2.setToolTipText("Test2");

        JLabel l3 = new JLabel("Gif Animated ToolTip(html)");
        l3.setToolTipText("<html><img src='"+url+"'>Test3</html>");

        JPanel p1 = new JPanel(new BorderLayout());
        p1.setBorder(BorderFactory.createTitledBorder("javax.swing.Timer"));
        p1.add(l1);
        JPanel p2 = new JPanel(new BorderLayout());
        p2.setBorder(BorderFactory.createTitledBorder("Animated Gif"));
        p2.add(l2, BorderLayout.NORTH);
        p2.add(l3, BorderLayout.SOUTH);

        Box box = Box.createVerticalBox();
        box.add(p1);
        box.add(Box.createVerticalStrut(20));
        box.add(p2);
        box.add(Box.createVerticalGlue());
        add(box);
        setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        setPreferredSize(new Dimension(320, 200));
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
        //frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

class AnimatedToolTip extends JToolTip {
    private final JLabel iconlabel;
    public AnimatedToolTip(JLabel label) {
        this.iconlabel = label;
        LookAndFeel.installColorsAndFont(iconlabel, "ToolTip.background", "ToolTip.foreground", "ToolTip.font");
        iconlabel.setOpaque(true);
        setLayout(new BorderLayout());
        add(iconlabel);
    }
    @Override public Dimension getPreferredSize() {
        return getLayout().preferredLayoutSize(this);
    }
//     @Override public Dimension getPreferredSize() {
//         Insets i = getInsets();
//         Dimension d = iconlabel.getPreferredSize();
//         d.width  += i.left+i.right;
//         d.height += i.top+i.bottom;
//         return d;
//     }
    @Override public void setTipText(final String tipText) {
        String oldValue = iconlabel.getText();
        iconlabel.setText(tipText);
        firePropertyChange("tiptext", oldValue, tipText);
    }
    @Override public String getTipText() {
        return iconlabel == null ? "" : iconlabel.getText();
    }
}

class AnimatedLabel extends JLabel implements ActionListener {
    private final Timer animator;
    private final AnimeIcon icon = new AnimeIcon();
    public AnimatedLabel(String title) {
        super(title);
        setOpaque(true);
        animator = new Timer(100, this);
        setIcon(icon);
        addHierarchyListener(new HierarchyListener() {
            @Override public void hierarchyChanged(HierarchyEvent e) {
                if((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED)!=0) {
                    if(isShowing()) {
                        startAnimation();
                    }else{
                        stopAnimation();
                    }
                }
            }
        });
    }
    @Override public void actionPerformed(ActionEvent e) {
        icon.next();
        repaint();
    }
    public void startAnimation() {
        icon.setRunning(true);
        animator.start();
    }
    public void stopAnimation() {
        icon.setRunning(false);
        animator.stop();
    }
}

class AnimeIcon implements Icon {
    private static final Color cColor = new Color(0.5f,0.5f,0.5f);
    private static final double r  = 2.0d;
    private static final double sx = 1.0d;
    private static final double sy = 1.0d;
    private static final Dimension dim = new Dimension((int)(r*8+sx*2), (int)(r*8+sy*2));
    private final List<Shape> list = new ArrayList<Shape>(Arrays.asList(
        new Ellipse2D.Double(sx+3*r, sy+0*r, 2*r, 2*r),
        new Ellipse2D.Double(sx+5*r, sy+1*r, 2*r, 2*r),
        new Ellipse2D.Double(sx+6*r, sy+3*r, 2*r, 2*r),
        new Ellipse2D.Double(sx+5*r, sy+5*r, 2*r, 2*r),
        new Ellipse2D.Double(sx+3*r, sy+6*r, 2*r, 2*r),
        new Ellipse2D.Double(sx+1*r, sy+5*r, 2*r, 2*r),
        new Ellipse2D.Double(sx+0*r, sy+3*r, 2*r, 2*r),
        new Ellipse2D.Double(sx+1*r, sy+1*r, 2*r, 2*r)));

    private boolean isRunning = false;
    public void next() {
        if(isRunning) list.add(list.remove(0));
    }
    public void setRunning(boolean isRunning) {
        this.isRunning = isRunning;
    }
    @Override public int getIconWidth()  { return dim.width;  }
    @Override public int getIconHeight() { return dim.height; }
    @Override public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setPaint((c!=null)?c.getBackground():Color.WHITE);
        g2d.fillRect(x, y, getIconWidth(), getIconHeight());
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(cColor);
        float alpha = 0.0f;
        g2d.translate(x, y);
        for(Shape s: list) {
            alpha = isRunning?alpha+0.1f:0.5f;
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            g2d.fill(s);
        }
        g2d.translate(-x, -y);
    }
}
