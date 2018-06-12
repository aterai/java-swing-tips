package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
// import javax.swing.event.*;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());
        JLabel label = new JLabel();
        label.setIcon(new ImageIcon(getClass().getResource("CRW_3857_JFR.jpg"))); // http://sozai-free.com/
        JScrollPane scroll = new JScrollPane(label);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        ViewportDragScrollListener l = new ViewportDragScrollListener();
        JViewport v = scroll.getViewport();
        v.addMouseMotionListener(l);
        v.addMouseListener(l);
        v.addHierarchyListener(l);

        // // TEST:
        // ComponentDragScrollListener l = new ComponentDragScrollListener();
        // label.addMouseMotionListener(l);
        // label.addMouseListener(l);
        // label.addHierarchyListener(l);

        add(scroll);
        scroll.setPreferredSize(new Dimension(320, 240));
    }

    public static void main(String... args) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                createAndShowGui();
            }
        });
    }
    public static void createAndShowGui() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException
               | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        }
        JFrame frame = new JFrame("@title@");
        // frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

class ViewportDragScrollListener extends MouseAdapter implements HierarchyListener {
    private static final int SPEED = 4;
    private static final int DELAY = 10;
    private static final Cursor DC = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
    private static final Cursor HC = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
    private final Point startPt = new Point();
    private final Point move = new Point();
    private final Timer scroller = new Timer(DELAY, null);
    private transient ActionListener listener;

    @Override public void hierarchyChanged(HierarchyEvent e) {
        if ((e.getChangeFlags() & HierarchyEvent.DISPLAYABILITY_CHANGED) != 0 && !e.getComponent().isDisplayable()) {
            scroller.stop();
            scroller.removeActionListener(listener);
        }
    }
    @Override public void mouseDragged(MouseEvent e) {
        JViewport vport = (JViewport) e.getComponent();
        JComponent c = (JComponent) vport.getView();
        Point pt = e.getPoint();
        int dx = startPt.x - pt.x;
        int dy = startPt.y - pt.y;
        Point vp = vport.getViewPosition();
        vp.translate(dx, dy);
        c.scrollRectToVisible(new Rectangle(vp, vport.getSize()));
        move.setLocation(SPEED * dx, SPEED * dy);
        startPt.setLocation(pt);
    }
    @Override public void mousePressed(MouseEvent e) {
        e.getComponent().setCursor(HC);
        startPt.setLocation(e.getPoint());
        move.setLocation(0, 0);
        scroller.stop();
        scroller.removeActionListener(listener);
    }
    @Override public void mouseReleased(MouseEvent e) {
        Component c = e.getComponent();
        c.setCursor(DC);
        if (c instanceof JViewport) {
            JViewport vport = (JViewport) c;
            JComponent label = (JComponent) vport.getView();
            listener = event -> {
                Point vp = vport.getViewPosition(); // = SwingUtilities.convertPoint(vport, 0, 0, label);
                vp.translate(move.x, move.y);
                label.scrollRectToVisible(new Rectangle(vp, vport.getSize())); // vport.setViewPosition(vp);
            };
            scroller.addActionListener(listener);
            scroller.start();
        }
    }
    @Override public void mouseExited(MouseEvent e) {
        e.getComponent().setCursor(DC);
        move.setLocation(0, 0);
        scroller.stop();
        scroller.removeActionListener(listener);
    }
}

class ComponentDragScrollListener extends MouseAdapter implements HierarchyListener {
    private static final int SPEED = 4;
    private static final int DELAY = 10;
    private static final Cursor DC = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
    private static final Cursor HC = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
    private final Point startPt = new Point();
    private final Point move = new Point();
    private final Timer scroller = new Timer(DELAY, null);
    private transient ActionListener listener;

    @Override public void hierarchyChanged(HierarchyEvent e) {
        if ((e.getChangeFlags() & HierarchyEvent.DISPLAYABILITY_CHANGED) != 0 && !e.getComponent().isDisplayable()) {
            scroller.stop();
            scroller.removeActionListener(listener);
        }
    }
    @Override public void mouseDragged(MouseEvent e) {
        scroller.stop();
        scroller.removeActionListener(listener);
        JComponent jc = (JComponent) e.getComponent();
        Container c = SwingUtilities.getAncestorOfClass(JViewport.class, jc);
        if (c instanceof JViewport) {
            JViewport vport = (JViewport) c;
            Point cp = SwingUtilities.convertPoint(jc, e.getPoint(), vport);
            int dx = startPt.x - cp.x;
            int dy = startPt.y - cp.y;
            Point vp = vport.getViewPosition();
            vp.translate(dx, dy);
            jc.scrollRectToVisible(new Rectangle(vp, vport.getSize()));
            move.setLocation(SPEED * dx, SPEED * dy);
            startPt.setLocation(cp);
        }
    }
    @Override public void mousePressed(MouseEvent e) {
        scroller.stop();
        scroller.removeActionListener(listener);
        move.setLocation(0, 0);
        Component c = e.getComponent();
        c.setCursor(HC);
        Container p = SwingUtilities.getUnwrappedParent(c);
        if (p instanceof JViewport) {
            JViewport vport = (JViewport) p;
            startPt.setLocation(SwingUtilities.convertPoint(c, e.getPoint(), vport));
        }
    }
    @Override public void mouseReleased(MouseEvent e) {
        Component c = e.getComponent();
        c.setCursor(DC);
        listener = event -> {
            Container p = SwingUtilities.getUnwrappedParent(c);
            if (p instanceof JViewport) {
                JViewport vport = (JViewport) p;
                Point vp = vport.getViewPosition();
                vp.translate(move.x, move.y);
                ((JComponent) c).scrollRectToVisible(new Rectangle(vp, vport.getSize()));
            }
        };
        scroller.addActionListener(listener);
        scroller.start();
    }
    @Override public void mouseExited(MouseEvent e) {
        scroller.stop();
        scroller.removeActionListener(listener);
        e.getComponent().setCursor(DC);
        move.setLocation(0, 0);
    }
}
