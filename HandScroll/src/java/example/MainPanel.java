package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.stream.Stream;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());

        JLabel label = new JLabel(new ImageIcon(getClass().getResource("CRW_3857_JFR.jpg"))); // http://sozai-free.com/
        // JViewport vport = scroll.getViewport();
        // JDK 1.7.0
        JViewport vport = new JViewport() {
            private static final boolean WEIGHT_MIXING = false;
            private boolean isAjusting;
            @Override public void revalidate() {
                if (!WEIGHT_MIXING && isAjusting) {
                    return;
                }
                super.revalidate();
            }
            @Override public void setViewPosition(Point p) {
                if (WEIGHT_MIXING) {
                    super.setViewPosition(p);
                } else {
                    isAjusting = true;
                    super.setViewPosition(p);
                    isAjusting = false;
                }
            }
        };
        vport.add(label);

        JScrollPane scroll = new JScrollPane(); // new JScrollPane(label);
        scroll.setViewport(vport);

        HandScrollListener hsl1 = new HandScrollListener();
        vport.addMouseMotionListener(hsl1);
        vport.addMouseListener(hsl1);

        JRadioButton radio = new JRadioButton("scrollRectToVisible", true);
        radio.addItemListener(e -> hsl1.withinRangeMode = e.getStateChange() == ItemEvent.SELECTED);

        Box box = Box.createHorizontalBox();
        ButtonGroup bg = new ButtonGroup();
        Stream.of(radio, new JRadioButton("setViewPosition")).forEach(r -> {
            box.add(r);
            bg.add(r);
        });

        // // TEST:
        // MouseAdapter hsl2 = new DragScrollListener();
        // label.addMouseMotionListener(hsl2);
        // label.addMouseListener(hsl2);
        add(scroll);
        add(box, BorderLayout.NORTH);
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
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

class HandScrollListener extends MouseAdapter {
    private final Cursor defCursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
    private final Cursor hndCursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
    private final Point pp = new Point();
    protected boolean withinRangeMode = true;

    @Override public void mouseDragged(MouseEvent e) {
        JViewport vport = (JViewport) e.getComponent();
        Point cp = e.getPoint();
        Point vp = vport.getViewPosition(); // = SwingUtilities.convertPoint(vport, 0, 0, label);
        vp.translate(pp.x - cp.x, pp.y - cp.y);
        if (withinRangeMode) {
            ((JComponent) SwingUtilities.getUnwrappedView(vport)).scrollRectToVisible(new Rectangle(vp, vport.getSize()));
        } else {
            vport.setViewPosition(vp);
        }
        pp.setLocation(cp);
    }
    @Override public void mousePressed(MouseEvent e) {
        e.getComponent().setCursor(hndCursor);
        pp.setLocation(e.getPoint());
    }
    @Override public void mouseReleased(MouseEvent e) {
        e.getComponent().setCursor(defCursor);
    }
}

// // TEST:
// class DragScrollListener extends MouseAdapter {
//     private final Cursor defCursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
//     private final Cursor hndCursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
//     private final Point pp = new Point();
//     @Override public void mouseDragged(MouseEvent e) {
//         Component c = e.getComponent();
//         Container p = SwingUtilities.getUnwrappedParent(c);
//         if (p instanceof JViewport) {
//             JViewport vport = (JViewport) p;
//             Point cp = SwingUtilities.convertPoint(c, e.getPoint(), vport);
//             Point vp = vport.getViewPosition();
//             vp.translate(pp.x - cp.x, pp.y - cp.y);
//             ((JComponent) c).scrollRectToVisible(new Rectangle(vp, vport.getSize()));
//             pp.setLocation(cp);
//         }
//     }
//     @Override public void mousePressed(MouseEvent e) {
//         Component c = e.getComponent();
//         c.setCursor(hndCursor);
//         Container p = SwingUtilities.getUnwrappedParent(c);
//         if (p instanceof JViewport) {
//             JViewport vport = (JViewport) p;
//             Point cp = SwingUtilities.convertPoint(c, e.getPoint(), vport);
//             pp.setLocation(cp);
//         }
//     }
//     @Override public void mouseReleased(MouseEvent e) {
//         e.getComponent().setCursor(defCursor);
//     }
// }
