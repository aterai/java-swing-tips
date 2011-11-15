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
        label.setIcon(new ImageIcon(getClass().getResource("CRW_3857_JFR.jpg")));
        JScrollPane scroll = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_NEVER,
                                             JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        DragMoverListener l = new DragMoverListener(label);
        JViewport v = scroll.getViewport();
        v.add(label);
        v.addMouseMotionListener(l);
        v.addMouseListener(l);
        v.addHierarchyListener(l);
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

class DragMoverListener extends MouseAdapter implements HierarchyListener{
    private static final int SPEED = 4;
    private static final int DELAY = 10;
    private final Cursor dc;
    private final Cursor hc = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
    private final javax.swing.Timer scroller;
    private final JComponent label;
    private Point startPt = new Point();
    private Point move    = new Point();

    public DragMoverListener(JComponent comp) {
        this.label = comp;
        this.dc = comp.getCursor();
        this.scroller = new javax.swing.Timer(DELAY, new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                JViewport vport = (JViewport)label.getParent();
                Point vp = vport.getViewPosition(); //= SwingUtilities.convertPoint(vport,0,0,label);
                vp.translate(-move.x, -move.y);
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
        scroller.stop();
        Point pt = e.getPoint();
        move.setLocation(SPEED*(pt.x-startPt.x), SPEED*(pt.y-startPt.y));
        startPt.setLocation(pt);
        scroller.start();
    }
    @Override public void mousePressed(MouseEvent e) {
        ((JComponent)e.getSource()).setCursor(hc); //label.setCursor(hc);
        startPt.setLocation(e.getPoint());
        scroller.stop();
    }
    @Override public void mouseReleased(MouseEvent e) {
        ((JComponent)e.getSource()).setCursor(dc); //label.setCursor(dc);
    }
    @Override public void mouseExited(MouseEvent e) {
        ((JComponent)e.getSource()).setCursor(dc); //label.setCursor(dc);
        move.setLocation(0, 0);
        scroller.stop();
    }
}
