package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.dnd.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
    public MainPanel() {
        super(new BorderLayout());

        Box box = Box.createVerticalBox();
        MouseAdapter dh = new RearrangingHandler();
        box.addMouseListener(dh);
        box.addMouseMotionListener(dh);

        int idx = 0;
        for (JComponent c: Arrays.asList(
            new JLabel("<html>1<br>11<br>111"),
            new JButton("22"),
            new JCheckBox("333"),
            new JScrollPane(new JTextArea(4, 12)))) {
            box.add(createSortablePanel(idx++, c));
        }
        add(box, BorderLayout.NORTH);
        setPreferredSize(new Dimension(320, 240));
    }

    private JComponent createSortablePanel(int i, JComponent c) {
        JLabel l = new JLabel(String.format(" %04d ", i));
        l.setOpaque(true);
        l.setBackground(Color.RED);
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(5, 5, 5, 5),
            BorderFactory.createLineBorder(Color.BLUE, 2)));
        p.add(l, BorderLayout.WEST);
        p.add(c);
        p.setOpaque(false);
        return p;
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
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

class RearrangingHandler extends MouseAdapter {
    private static final Rectangle R1 = new Rectangle();
    private static final Rectangle R2 = new Rectangle();
    private static Rectangle prevRect;
    private final int gestureMotionThreshold = DragSource.getDragThreshold();
    private final JWindow window = new JWindow();
    private int index = -1;
    private Component draggingComonent;
    private Component gap;
    private Point startPt;
    private Point dragOffset;

    public RearrangingHandler() {
        super();
        window.setBackground(new Color(0x0, true));
    }
    @Override public void mousePressed(MouseEvent e) {
        if (((JComponent) e.getComponent()).getComponentCount() <= 1) {
            startPt = null;
        } else {
            startPt = e.getPoint();
        }
    }
    private void startDragging(JComponent parent, Point pt) {
        Component c = parent.getComponentAt(pt);
        index = parent.getComponentZOrder(c);
        if (Objects.equals(c, parent) || index < 0) {
            return;
        }
        draggingComonent = c;
        Dimension d = draggingComonent.getSize();

        Point dp = draggingComonent.getLocation();
        dragOffset = new Point(pt.x - dp.x, pt.y - dp.y);

        gap = Box.createRigidArea(d);
        swapComponentLocation(parent, c, gap, index);

        window.add(draggingComonent);
        //window.setSize(d);
        window.pack();

        updateWindowLocation(pt, parent);
        window.setVisible(true);
    }
    private void updateWindowLocation(Point pt, JComponent parent) {
        Point p = new Point(pt.x - dragOffset.x, pt.y - dragOffset.y);
        SwingUtilities.convertPointToScreen(p, parent);
        window.setLocation(p);
    }
    private static int getTargetIndex(Rectangle r, Point pt, int i) {
        int ht2 = (int) (.5 + r.height * .5);
        R1.setBounds(r.x, r.y,       r.width, ht2);
        R2.setBounds(r.x, r.y + ht2, r.width, ht2);
        if (R1.contains(pt)) {
            prevRect = R1;
            return i - 1 > 0 ? i : 0;
        } else if (R2.contains(pt)) {
            prevRect = R2;
            return i;
        }
        return -1;
    }
    private static void swapComponentLocation(Container parent, Component remove, Component add, int idx) {
        parent.remove(remove);
        parent.add(add, idx);
        parent.revalidate();
        parent.repaint();
    }
    @Override public void mouseDragged(MouseEvent e) {
        Point pt = e.getPoint();
        JComponent parent = (JComponent) e.getComponent();
        if (draggingComonent == null && Math.sqrt(Math.pow(pt.x - startPt.x, 2) + Math.pow(pt.y - startPt.y, 2)) > gestureMotionThreshold) {
            startDragging(parent, pt);
            return;
        }
        if (!window.isVisible() || draggingComonent == null) {
            return;
        }
        updateWindowLocation(pt, parent);
        if (prevRect != null && prevRect.contains(pt)) {
            return;
        }

        for (int i = 0; i < parent.getComponentCount(); i++) {
            Component c = parent.getComponent(i);
            Rectangle r = c.getBounds();
            if (Objects.equals(c, gap) && r.contains(pt)) {
                return;
            }
            int tgt = getTargetIndex(r, pt, i);
            if (tgt >= 0) {
                swapComponentLocation(parent, gap, gap, tgt);
                return;
            }
        }
        //System.out.println("outer");
        parent.remove(gap);
        parent.revalidate();
    }

    @Override public void mouseReleased(MouseEvent e) {
        startPt = null;
        if (!window.isVisible() || draggingComonent == null) {
            return;
        }
        Point pt = e.getPoint();
        JComponent parent = (JComponent) e.getComponent();

        Component cmp = draggingComonent;
        draggingComonent = null;
        prevRect = null;
        startPt = null;
        dragOffset = null;
        window.setVisible(false);

        for (int i = 0; i < parent.getComponentCount(); i++) {
            Component c = parent.getComponent(i);
            if (Objects.equals(c, gap)) {
                //System.out.println("000");
                swapComponentLocation(parent, gap, cmp, i);
                return;
            }
            int tgt = getTargetIndex(c.getBounds(), pt, i);
            if (tgt >= 0) {
                swapComponentLocation(parent, gap, cmp, tgt);
                return;
            }
        }
        if (parent.getParent().getBounds().contains(pt)) {
            //System.out.println("333");
            swapComponentLocation(parent, gap, cmp, parent.getComponentCount());
        } else {
            //System.out.println("444");
            swapComponentLocation(parent, gap, cmp, index);
        }
    }
}
