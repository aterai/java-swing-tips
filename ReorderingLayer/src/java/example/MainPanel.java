package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.dnd.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.plaf.*;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());

        Box box = Box.createVerticalBox();
        box.setBorder(BorderFactory.createMatteBorder(10, 8, 5, 1, Color.RED));
        int idx = 0;
        for (Component c: Arrays.asList(
            new JLabel("<html>111<br>11<br>11"),
            new JButton("2"), new JCheckBox("3"), new JTextField(14))) {
            box.add(createToolBarButton(idx++, c));
        }
        add(new JLayer<>(box, new ReorderingLayerUI<>()), BorderLayout.NORTH);
        setPreferredSize(new Dimension(320, 240));
    }

    private static JComponent createToolBarButton(int i, Component c) {
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

class ReorderingLayerUI<V extends JComponent> extends LayerUI<V> {
    private static final Rectangle R1 = new Rectangle();
    private static final Rectangle R2 = new Rectangle();
    private static final Rectangle R3 = new Rectangle();
    private final Rectangle prevRect = new Rectangle();
    private final Rectangle draggingRect = new Rectangle();
    private final Point startPt = new Point();
    private final Point dragOffset = new Point();
    private final JComponent rubberStamp = new JPanel();
    private final int gestureMotionThreshold = DragSource.getDragThreshold();

    private Component draggingComonent;
    private Component gap;
    private int index = -1;

    @Override public void paint(Graphics g, JComponent c) {
        super.paint(g, c);
        if (c instanceof JLayer && Objects.nonNull(draggingComonent)) {
            SwingUtilities.paintComponent(g, draggingComonent, rubberStamp, draggingRect);
        }
    }

    @Override public void installUI(JComponent c) {
        super.installUI(c);
        if (c instanceof JLayer) {
            ((JLayer) c).setLayerEventMask(AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK);
        }
    }

    @Override public void uninstallUI(JComponent c) {
        if (c instanceof JLayer) {
            ((JLayer) c).setLayerEventMask(0);
        }
        super.uninstallUI(c);
    }

    @Override protected void processMouseEvent(MouseEvent e, JLayer<? extends V> l) {
        JComponent parent = l.getView();
        switch (e.getID()) {
          case MouseEvent.MOUSE_PRESSED:
            if (parent.getComponentCount() > 0) {
                startPt.setLocation(e.getPoint());
                l.repaint();
            }
            break;
          case MouseEvent.MOUSE_RELEASED:
            if (Objects.isNull(draggingComonent)) {
                return;
            }
            Point pt = e.getPoint();

            Component cmp = draggingComonent;
            draggingComonent = null;

            //swap the dragging panel and the dummy filler
            for (int i = 0; i < parent.getComponentCount(); i++) {
                Component c = parent.getComponent(i);
                if (Objects.equals(c, gap)) {
                    replaceComponent(parent, gap, cmp, i);
                    return;
                }
                int tgt = getTargetIndex(c.getBounds(), pt, i);
                if (tgt >= 0) {
                    replaceComponent(parent, gap, cmp, tgt);
                    return;
                }
            }
            if (parent.getParent().getBounds().contains(pt)) {
                replaceComponent(parent, gap, cmp, parent.getComponentCount());
            } else {
                replaceComponent(parent, gap, cmp, index);
            }
            l.repaint();
            break;
          default:
            break;
        }
    }

    @Override protected void processMouseMotionEvent(MouseEvent e, JLayer<? extends V> l) {
        if (e.getID() == MouseEvent.MOUSE_DRAGGED) {
            Point pt = e.getPoint();
            JComponent parent = l.getView();

            if (Objects.isNull(draggingComonent)) {
                //MotionThreshold
                if (startPt.distance(pt) > gestureMotionThreshold) {
                    startDragging(parent, pt);
                }
                return;
            }

            //update the cursor window location
            updateWindowLocation(pt, parent);
            l.repaint();

            if (prevRect.contains(pt)) {
                return;
            }

            //change the dummy filler location
            for (int i = 0; i < parent.getComponentCount(); i++) {
                Component c = parent.getComponent(i);
                Rectangle r = c.getBounds();
                if (Objects.equals(c, gap) && r.contains(pt)) {
                    return;
                }
                int tgt = getTargetIndex(r, pt, i);
                if (tgt >= 0) {
                    replaceComponent(parent, gap, gap, tgt);
                    return;
                }
            }
            parent.revalidate();
            l.repaint();
        }
    }

    private void startDragging(JComponent parent, Point pt) {
        Component c = parent.getComponentAt(pt);
        index = parent.getComponentZOrder(c);
        if (Objects.equals(c, parent) || index < 0) {
            return;
        }
        draggingComonent = c;

        Rectangle r = draggingComonent.getBounds();
        draggingRect.setBounds(r); //save draggingComonent size
        dragOffset.setLocation(pt.x - r.x, pt.y - r.y);

        gap = Box.createRigidArea(r.getSize());
        replaceComponent(parent, c, gap, index);

        updateWindowLocation(pt, parent);
    }

    private void updateWindowLocation(Point pt, JComponent parent) {
        Insets i = parent.getInsets();
        Rectangle r = SwingUtilities.calculateInnerArea(parent, R3);
        int x = r.x;
        int y = pt.y - dragOffset.y;
        int h = draggingRect.height;
        int yy = y < i.top ? i.top : r.contains(x, y + h) ? y : r.height + i.top - h;
        draggingRect.setLocation(x, yy);
    }

    private int getTargetIndex(Rectangle r, Point pt, int i) {
        int ht2 = (int) (.5 + r.height * .5);
        R1.setBounds(r.x, r.y,       r.width, ht2);
        R2.setBounds(r.x, r.y + ht2, r.width, ht2);
        if (R1.contains(pt)) {
            prevRect.setBounds(R1);
            return i > 1 ? i : 0;
        } else if (R2.contains(pt)) {
            prevRect.setBounds(R2);
            return i;
        }
        return -1;
    }

    private static void replaceComponent(Container parent, Component remove, Component insert, int idx) {
        parent.remove(remove);
        parent.add(insert, idx);
        parent.revalidate();
        parent.repaint();
    }
}
