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
    public MainPanel() {
        super(new BorderLayout());
        ImageIcon icon = new ImageIcon(getClass().getResource("test.png"));
        check.setFocusPainted(false);

        final MyPopupMenu pop = new MyPopupMenu();
        pop.add(new JMenuItem("Open"));
        pop.add(new JMenuItem("Save"));
        pop.add(new JMenuItem("Close"));
        //pop.addSeparator();
        JSeparator s = new JSeparator();
        s.setOpaque(true);
        pop.add(s);
        pop.add(new JMenuItem("Exit"));

        JLabel label = new JLabel(icon);
        //Swing - Can popup menu events be consumed by other (e.g. background) components?
        //http://forums.sun.com/thread.jspa?threadID=590867
        label.addMouseListener(new MouseAdapter() {}); // JDK 1.5 JPanel, JLabel
        label.setComponentPopupMenu(pop);
//         addMouseListener(new MouseAdapter() {
//             public void mouseReleased(MouseEvent e) {
//                 if(e.isPopupTrigger()) {
//                     Point pt = e.getPoint();
//                     pop.show(e.getComponent(), pt.x, pt.y);
//                 }
//                 repaint();
//             }
//         });
        add(check, BorderLayout.NORTH);
        add(label);
        setPreferredSize(new Dimension(320, 240));
    }
    class MyPopupMenu extends JPopupMenu {
        private static final int off = 6;
        @Override public void paintComponent(Graphics g) {
            int w = getWidth();
            int h = getHeight();
            BufferedImage shadow = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = shadow.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,0.2f));
            g2.setPaint(Color.BLACK);
            for(int i=0;i<off;i++) {
                g2.fillRoundRect(off, off, w-off-off+i, h-off-off+i, 10, 10);
            }
//             g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,1.0f));
//             g2.setPaint(getBackground());
//             Insets i = getInsets();
//             g2.fillRect(i.left, i.top, w-i.right, h-i.bottom);
            g2.dispose();
            ((Graphics2D)g).drawImage(shadow, 0, 0, this);
        }
        private Border inner = null;
        @Override public void show(Component c, int x, int y) {
            //updateUI();
            if(inner==null) inner = getBorder();
            setBorder(check.isSelected()?makeShadowBorder(c, new Point(x, y)):inner);
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
//         private boolean isInRootPanel(Component root, Point p) {
//             Rectangle r = root.getBounds();
//             Dimension d = this.getPreferredSize();
//             //pointed out by sawshun
//             return r.contains(p.x, p.y, d.width+off, d.height+off);
//             //    Point pt = new Point(p.x+d.width+off, p.y+d.height+off);
//             //    SwingUtilities.convertPointToScreen(pt, root);
//             //    return r.contains(pt);
//         }
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

class ShadowBorder extends AbstractBorder {
    private final int xoff, yoff;
    private final Insets insets;
    private BufferedImage screenShot = null;

    public ShadowBorder(int x, int y, JComponent c, Point p) { //JComponent c, int sx, int sy) {
        this.xoff = x;
        this.yoff = y;
        this.insets = new Insets(0,0,xoff,yoff);
        try{
            Robot robot = new Robot();
            //Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            //Rectangle rect = new Rectangle(0, 0, screenSize.width, screenSize.height);
            Dimension dim = c.getPreferredSize();
            Rectangle rect = new Rectangle(p.x, p.y, dim.width+xoff, dim.height+yoff);
            screenShot = robot.createScreenCapture(rect);
            //System.out.println(rect.toString());
        }catch (java.awt.AWTException ex) {
            ex.printStackTrace();
        }
    }
    @Override public Insets getBorderInsets(Component c) {
        return insets;
    }
    @Override public void paintBorder(Component comp, Graphics g, int x, int y, int w, int h) {
        if(screenShot==null) return;
        BufferedImage shadow = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = shadow.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,0.2f));
        g2.setPaint(Color.BLACK);
        for(int i=0;i<xoff;i++) {
            g2.fillRoundRect(xoff, xoff, w-xoff-xoff+i, h-xoff-xoff+i, 10,10);
        }
        g2.dispose();
        Graphics2D gx = (Graphics2D) g;
        //Point p = new Point(x, y);
        //SwingUtilities.convertPointToScreen(p, comp);
        //BufferedImage bi = screenShot; //.getSubimage(p.x, p.y, w, h);
        //System.out.println(new Rectangle(p.x, p.y, w, h).toString());
        gx.drawImage(screenShot, 0, 0, comp);
        gx.drawImage(shadow, 0, 0, comp);
    }
}
