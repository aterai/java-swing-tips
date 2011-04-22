package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.swing.*;
import javax.swing.border.*;

class MainPanel extends JPanel {
    private final JCheckBox check = new JCheckBox("Paint Shadow", true);
    private final JLabel label = new JLabel();
    private JPopupMenu popup0;
    private MyPopupMenu popup1;
    public MainPanel() {
        super(new BorderLayout());
        label.setIcon(new ImageIcon(getClass().getResource("test.png")));
        label.setComponentPopupMenu(popup1);
        check.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                JCheckBox c = (JCheckBox)e.getSource();
                label.setComponentPopupMenu(c.isSelected()?popup1:popup0);
            }
        });
        add(check, BorderLayout.NORTH);
        add(label);
        setPreferredSize(new Dimension(320, 240));
    }
    @Override public void updateUI() {
        super.updateUI();
        popup0 = new JPopupMenu();
        popup1 = new MyPopupMenu();
        initPopupMenu(popup0);
        initPopupMenu(popup1);
    }
    private static void initPopupMenu(JPopupMenu p) {
        p.setOpaque(false);
        p.add(new JMenuItem("Open"));
        p.add(new JMenuItem("Save"));
        p.add(new JMenuItem("Close"));
        //Bug ID: 6595814 Nimbus LAF: Renderers, MenuSeparators, colors rollup bug
        //http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6595814
        //p.addSeparator();
        JSeparator s = new JSeparator();
        s.setOpaque(true);
        p.add(s);
        p.add(new JMenuItem("Exit"));
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

class MyPopupMenu extends JPopupMenu {
    private static final int off = 4;
    private BufferedImage shadow = null;
    private Border inner = null;
    @Override public void paintComponent(Graphics g) {
        //super.paintComponent(g); //??? 1.7.0
        ((Graphics2D)g).drawImage(shadow, 0, 0, this);
    }
    @Override public void show(Component c, int x, int y) {
        if(inner==null) inner = getBorder();
        setBorder(makeShadowBorder(c, new Point(x, y)));

        Dimension d = getPreferredSize();
        int w = d.width, h = d.height;
        if(shadow==null || shadow.getWidth()!=w || shadow.getHeight()!=h) {
            shadow = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = shadow.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.2f));
            g2.setPaint(Color.BLACK);
            for(int i=0;i<off;i++) {
                g2.fillRoundRect(off, off, w-off-off+i, h-off-off+i, 4, 4);
            }
            g2.dispose();
        }
        super.show(c, x, y);
    }
    private Border makeShadowBorder(Component c, Point p) {
        Rectangle r = SwingUtilities.getWindowAncestor(c).getBounds();
        Dimension d = this.getPreferredSize();
        SwingUtilities.convertPointToScreen(p, c);
        //System.out.println(r+" : "+p);
        //pointed out by sawshun
        Border outer;
        if(r.contains(p.x, p.y, d.width+off, d.height+off)) {
            outer = BorderFactory.createEmptyBorder(0,0,off,off);
        }else{
            outer = new ShadowBorder(off,off,this,p);
        }
        return BorderFactory.createCompoundBorder(outer, inner);
    }
}

class ShadowBorder extends AbstractBorder {
    private final int xoff, yoff;
    private final Insets insets;
    private BufferedImage screen = null;
    private BufferedImage shadow = null;

    public ShadowBorder(int x, int y, JComponent c, Point p) {
        this.xoff = x;
        this.yoff = y;
        this.insets = new Insets(0,0,xoff,yoff);
        try{
            Robot robot = new Robot();
            Dimension d = c.getPreferredSize();
            screen = robot.createScreenCapture(new Rectangle(p.x, p.y, d.width+xoff, d.height+yoff));
        }catch (java.awt.AWTException ex) {
            ex.printStackTrace();
        }
    }
    @Override public Insets getBorderInsets(Component c) {
        return insets;
    }
    @Override public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
        if(screen==null) return;
        if(shadow==null || shadow.getWidth()!=w || shadow.getHeight()!=h) {
            shadow = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = shadow.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.2f));
            g2.setPaint(Color.BLACK);
            for(int i=0;i<xoff;i++) {
                g2.fillRoundRect(xoff, xoff, w-xoff-xoff+i, h-xoff-xoff+i, 4, 4);
            }
            g2.dispose();
        }
        Graphics2D g2d = (Graphics2D)g;
        g2d.drawImage(screen, 0, 0, c);
        g2d.drawImage(shadow, 0, 0, c);
    }
}
