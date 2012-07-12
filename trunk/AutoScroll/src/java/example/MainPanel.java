package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
//import javax.swing.event.*;

class MainPanel extends JPanel {
    public MainPanel() {
        super(new BorderLayout());
        JLabel label = new JLabel();
        label.setIcon(new ImageIcon(getClass().getResource("CRW_3857_JFR.jpg"))); //http://sozai-free.com/
        JScrollPane scroll = new JScrollPane(label,
                                             JScrollPane.VERTICAL_SCROLLBAR_NEVER,
                                             JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

//*
        ViewportDragScrollListener l = new ViewportDragScrollListener(label);
        JViewport v = scroll.getViewport();
        v.addMouseMotionListener(l);
        v.addMouseListener(l);
        v.addHierarchyListener(l);
/*/
        ComponentDragScrollListener l = new ComponentDragScrollListener(label);
        label.addMouseMotionListener(l);
        label.addMouseListener(l);
        label.addHierarchyListener(l);
//*/
        add(scroll);
        scroll.setPreferredSize(new Dimension(320, 240));
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

class ViewportDragScrollListener extends MouseAdapter implements HierarchyListener{
    private static final int SPEED = 4;
    private static final int DELAY = 10;
    private final Cursor dc;
    private final Cursor hc = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
    private final Timer scroller;
    private final JComponent label;
    private Point startPt = new Point();
    private Point move    = new Point();

    public ViewportDragScrollListener(JComponent comp) {
        this.label = comp;
        this.dc = comp.getCursor();
        this.scroller = new Timer(DELAY, new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                JViewport vport = (JViewport)label.getParent();
                Point vp = vport.getViewPosition(); //= SwingUtilities.convertPoint(vport,0,0,label);
                vp.translate(move.x, move.y);
                label.scrollRectToVisible(new Rectangle(vp, vport.getSize())); //vport.setViewPosition(vp);
            }
        });
    }
    @Override public void hierarchyChanged(HierarchyEvent e) {
        JComponent c = (JComponent)e.getSource();
        if((e.getChangeFlags() & HierarchyEvent.DISPLAYABILITY_CHANGED)!=0 && !c.isDisplayable()) {
            scroller.stop();
        }
    }
    @Override public void mouseDragged(MouseEvent e) {
        JViewport vport = (JViewport)e.getSource();
        Point pt = e.getPoint();
        int dx = startPt.x - pt.x;
        int dy = startPt.y - pt.y;
        Point vp = vport.getViewPosition();
        vp.translate(dx, dy);
        label.scrollRectToVisible(new Rectangle(vp, vport.getSize()));
        move.setLocation(SPEED*dx, SPEED*dy);
        startPt.setLocation(pt);
    }
    @Override public void mousePressed(MouseEvent e) {
        ((JComponent)e.getSource()).setCursor(hc); //label.setCursor(hc);
        startPt.setLocation(e.getPoint());
        move.setLocation(0, 0);
        scroller.stop();
    }
    @Override public void mouseReleased(MouseEvent e) {
        ((JComponent)e.getSource()).setCursor(dc); //label.setCursor(dc);
        scroller.start();
    }
    @Override public void mouseExited(MouseEvent e) {
        ((JComponent)e.getSource()).setCursor(dc); //label.setCursor(dc);
        move.setLocation(0, 0);
        scroller.stop();
    }
}

class ComponentDragScrollListener extends MouseAdapter implements HierarchyListener{
    private static final int SPEED = 4;
    private static final int DELAY = 10;
    private final Cursor dc;
    private final Cursor hc = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
    private final Timer scroller;
    private final JComponent label;
    private Point startPt = new Point();
    private Point move    = new Point();

    public ComponentDragScrollListener(JComponent comp) {
        this.label = comp;
        this.dc = comp.getCursor();
        this.scroller = new Timer(DELAY, new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                Container c = label.getParent();
                if(c instanceof JViewport) {
                    JViewport vport = (JViewport)c;
                    Point vp = vport.getViewPosition();
                    vp.translate(move.x, move.y);
                    label.scrollRectToVisible(new Rectangle(vp, vport.getSize()));
                }
            }
        });
    }
    @Override public void hierarchyChanged(HierarchyEvent e) {
        JComponent jc = (JComponent)e.getSource();
        if((e.getChangeFlags() & HierarchyEvent.DISPLAYABILITY_CHANGED)!=0 && !jc.isDisplayable()) {
            scroller.stop();
        }
    }
    @Override public void mouseDragged(MouseEvent e) {
        scroller.stop();
        JComponent jc = (JComponent)e.getSource();
        Container c = jc.getParent();
        if(c instanceof JViewport) {
            JViewport vport = (JViewport)jc.getParent();
            Point cp = SwingUtilities.convertPoint(jc,e.getPoint(),vport);
            int dx = startPt.x - cp.x;
            int dy = startPt.y - cp.y;
            Point vp = vport.getViewPosition();
            vp.translate(dx, dy);
            jc.scrollRectToVisible(new Rectangle(vp, vport.getSize()));
            move.setLocation(SPEED*dx, SPEED*dy);
            startPt.setLocation(cp);
        }
    }
    @Override public void mousePressed(MouseEvent e) {
        scroller.stop();
        move.setLocation(0, 0);
        JComponent jc = (JComponent)e.getSource();
        jc.setCursor(hc);
        Container c = jc.getParent();
        if(c instanceof JViewport) {
            JViewport vport = (JViewport)c;
            Point cp = SwingUtilities.convertPoint(jc,e.getPoint(),vport);
            startPt.setLocation(cp);
        }
    }
    @Override public void mouseReleased(MouseEvent e) {
        ((JComponent)e.getSource()).setCursor(dc);
        scroller.start();
    }
    @Override public void mouseExited(MouseEvent e) {
        ((JComponent)e.getSource()).setCursor(dc);
        move.setLocation(0, 0);
        scroller.stop();
    }
}
