package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private static final boolean HEAVYWEIGHT_LIGHTWEIGHT_MIXING = false;
    private final JScrollPane scroll = new JScrollPane();
    private final JViewport viewport; // = scroll.getViewport();
    private final JRadioButton r1 = new JRadioButton("scrollRectToVisible");
    private final JRadioButton r2 = new JRadioButton("setViewPosition");

    public MainPanel() {
        super(new BorderLayout());
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        //JDK 1.7.0
        viewport = new JViewport() {
            private boolean flag;
            @Override public void revalidate() {
                if (!HEAVYWEIGHT_LIGHTWEIGHT_MIXING && flag) {
                    return;
                }
                super.revalidate();
            }
            @Override public void setViewPosition(Point p) {
                if (HEAVYWEIGHT_LIGHTWEIGHT_MIXING) {
                    super.setViewPosition(p);
                } else {
                    flag = true;
                    super.setViewPosition(p);
                    flag = false;
                }
            }
        };
        scroll.setViewport(viewport);

        JLabel label = new JLabel(new ImageIcon(getClass().getResource("CRW_3857_JFR.jpg"))); //http://sozai-free.com/
        viewport.add(label);
        final KineticScrollingListener1 l1 = new KineticScrollingListener1(label);
        final KineticScrollingListener2 l2 = new KineticScrollingListener2(label);

        ActionListener al = new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                if (e.getSource() == r1) {
                    viewport.removeMouseListener(l2);
                    viewport.removeMouseMotionListener(l2);
                    viewport.removeHierarchyListener(l2);
                    viewport.addMouseMotionListener(l1);
                    viewport.addMouseListener(l1);
                    viewport.addHierarchyListener(l1);
                } else {
                    viewport.removeMouseListener(l1);
                    viewport.removeMouseMotionListener(l1);
                    viewport.removeHierarchyListener(l1);
                    viewport.addMouseMotionListener(l2);
                    viewport.addMouseListener(l2);
                    viewport.addHierarchyListener(l2);
                }
            }
        };
        Box box = Box.createHorizontalBox();
        ButtonGroup bg = new ButtonGroup();
        box.add(r1); bg.add(r1); r1.addActionListener(al);
        box.add(r2); bg.add(r2); r2.addActionListener(al);
        r1.setSelected(true);
        viewport.addMouseMotionListener(l1);
        viewport.addMouseListener(l1);
        viewport.addHierarchyListener(l1);

        add(scroll);
        add(box, BorderLayout.NORTH);
        scroll.setPreferredSize(new Dimension(320, 240));
    }

    public static void main(String... args) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                createAndShowGUI();
            }
        });
    }
    public static void createAndShowGUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException
               | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        }
        JFrame frame = new JFrame("@title@");
        //frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        //frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

class KineticScrollingListener1 extends MouseAdapter implements HierarchyListener {
    private static final int SPEED = 4;
    private static final int DELAY = 10;
    private static final double D = .8;
    private final Cursor dc;
    private final Cursor hc = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
    private final Timer scroller;
    private final JComponent label;
    private final Point startPt = new Point();
    private final Point delta   = new Point();

    public KineticScrollingListener1(JComponent comp) {
        super();
        this.label = comp;
        this.dc = comp.getCursor();
        this.scroller = new Timer(DELAY, new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                JViewport vport = (JViewport) SwingUtilities.getUnwrappedParent(label);
                Point vp = vport.getViewPosition();
                vp.translate(-delta.x, -delta.y);
                label.scrollRectToVisible(new Rectangle(vp, vport.getSize()));
                //System.out.println(delta);
                if (Math.abs(delta.x) > 0 || Math.abs(delta.y) > 0) {
                    delta.setLocation((int) (delta.x * D), (int) (delta.y * D));
                } else {
                    scroller.stop();
                }
            }
        });
    }
    @Override public void mousePressed(MouseEvent e) {
        e.getComponent().setCursor(hc);
        startPt.setLocation(e.getPoint());
        scroller.stop();
    }
    @Override public void mouseDragged(MouseEvent e) {
        Point pt = e.getPoint();
        JViewport vport = (JViewport) e.getComponent(); //label.getParent();
        Point vp = vport.getViewPosition(); //= SwingUtilities.convertPoint(vport, 0, 0, label);
        vp.translate(startPt.x - pt.x, startPt.y - pt.y);
        delta.setLocation(SPEED * (pt.x - startPt.x), SPEED * (pt.y - startPt.y));
        label.scrollRectToVisible(new Rectangle(vp, vport.getSize()));
        startPt.setLocation(pt);
    }
    @Override public void mouseReleased(MouseEvent e) {
        e.getComponent().setCursor(dc);
        scroller.start();
    }
    @Override public void hierarchyChanged(HierarchyEvent e) {
        if ((e.getChangeFlags() & HierarchyEvent.DISPLAYABILITY_CHANGED) != 0 && !e.getComponent().isDisplayable()) {
            scroller.stop();
        }
    }
}

class KineticScrollingListener2 extends MouseAdapter implements HierarchyListener {
    private static final int SPEED = 4;
    private static final int DELAY = 10;
    private static final double D = .8;
    private final JComponent label;
    private final Point startPt = new Point();
    private final Point delta   = new Point();
    private final Cursor dc;
    private final Cursor hc = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
    private final Timer inside = new Timer(DELAY, new ActionListener() {
        @Override public void actionPerformed(ActionEvent e) {
            JViewport vport = (JViewport) SwingUtilities.getUnwrappedParent(label);
            Point vp = vport.getViewPosition();
            //System.out.format("s: %s, %s%n", delta, vp);
            vp.translate(-delta.x, -delta.y);
            vport.setViewPosition(vp);
            if (Math.abs(delta.x) > 0 || Math.abs(delta.y) > 0) {
                delta.setLocation((int) (delta.x * D), (int) (delta.y * D));
                //Outside
                if (vp.x < 0 || vp.x + vport.getWidth() - label.getWidth() > 0) {
                    delta.x = (int) (delta.x * D);
                }
                if (vp.y < 0 || vp.y + vport.getHeight() - label.getHeight() > 0) {
                    delta.y = (int) (delta.y * D);
                }
            } else {
                inside.stop();
                if (!isInside(vport, label)) {
                    outside.start();
                }
            }
        }
    });
    private final Timer outside = new Timer(DELAY, new ActionListener() {
        @Override public void actionPerformed(ActionEvent e) {
            JViewport vport = (JViewport) SwingUtilities.getUnwrappedParent(label);
            Point vp = vport.getViewPosition();
            //System.out.format("r: %s%n", vp);
            if (vp.x < 0) {
                vp.x = (int) (vp.x * D);
            }
            if (vp.y < 0) {
                vp.y = (int) (vp.y * D);
            }
            if (vp.x + vport.getWidth() - label.getWidth() > 0) {
                vp.x = (int) (vp.x - (vp.x + vport.getWidth() - label.getWidth()) * (1d - D));
            }
            if (vp.y + vport.getHeight() > label.getHeight()) {
                vp.y = (int) (vp.y - (vp.y + vport.getHeight() - label.getHeight()) * (1d - D));
            }
            vport.setViewPosition(vp);
            if (isInside(vport, label)) {
                outside.stop();
            }
        }
    });

    private static boolean isInside(JViewport vport, JComponent comp) {
        Point vp = vport.getViewPosition();
        return vp.x >= 0 && vp.x + vport.getWidth()  - comp.getWidth()  <= 0
            && vp.y >= 0 && vp.y + vport.getHeight() - comp.getHeight() <= 0;
    }
    public KineticScrollingListener2(JComponent comp) {
        super();
        this.label = comp;
        this.dc = comp.getCursor();
    }
    @Override public void mousePressed(MouseEvent e) {
        e.getComponent().setCursor(hc);
        startPt.setLocation(e.getPoint());
        inside.stop();
        outside.stop();
    }
    @Override public void mouseDragged(MouseEvent e) {
        Point pt = e.getPoint();
        JViewport vport = (JViewport) SwingUtilities.getUnwrappedParent(label);
        Point vp = vport.getViewPosition();
        vp.translate(startPt.x - pt.x, startPt.y - pt.y);
        vport.setViewPosition(vp);
        delta.setLocation(SPEED * (pt.x - startPt.x), SPEED * (pt.y - startPt.y));
        startPt.setLocation(pt);
    }
    @Override public void mouseReleased(MouseEvent e) {
        e.getComponent().setCursor(dc);
        JViewport vport = (JViewport) SwingUtilities.getUnwrappedParent(label);
        if (isInside(vport, label)) {
            inside.start();
        } else {
            outside.start();
        }
    }
    @Override public void hierarchyChanged(HierarchyEvent e) {
        if ((e.getChangeFlags() & HierarchyEvent.DISPLAYABILITY_CHANGED) != 0 && !e.getComponent().isDisplayable()) {
            inside.stop();
            outside.stop();
        }
    }
}
