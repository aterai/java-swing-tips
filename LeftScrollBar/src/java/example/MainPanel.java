package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

public final class MainPanel extends JPanel {
    private final JLabel label       = new JLabel();
    private final JScrollPane scroll = new JScrollPane(label);
    private final JViewport vport    = scroll.getViewport();

    public MainPanel() {
        super(new BorderLayout());
        label.setIcon(new ImageIcon(getClass().getResource("CRW_3857_JFR.jpg"))); //http://sozai-free.com/

        HandScrollListener hsl = new HandScrollListener();
        vport.addMouseMotionListener(hsl);
        vport.addMouseListener(hsl);

        scroll.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(Box.createHorizontalStrut(scroll.getVerticalScrollBar().getPreferredSize().width), BorderLayout.WEST);
        panel.add(scroll.getHorizontalScrollBar());
        add(panel,  BorderLayout.NORTH);
        add(scroll);
        scroll.setPreferredSize(new Dimension(320, 240));
    }
    class HandScrollListener extends MouseInputAdapter {
        private final Rectangle rect = new Rectangle();
        private final Cursor defCursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
        private final Cursor hndCursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
        private int startX, startY;
        @Override public void mouseDragged(final MouseEvent e) {
            Rectangle vr = vport.getViewRect();
            int w = vr.width;
            int h = vr.height;
            int x = e.getX();
            int y = e.getY();
            Point pt = SwingUtilities.convertPoint(vport, 0, 0, label);
            rect.setRect(pt.x - x + startX, pt.y - y + startY, w, h);
            label.scrollRectToVisible(rect);
            startX = x; startY = y;
        }
        @Override public void mousePressed(MouseEvent e) {
            startX = e.getX(); startY = e.getY();
            label.setCursor(hndCursor);
        }
        @Override public void mouseReleased(MouseEvent e) {
            label.setCursor(defCursor);
            label.repaint();
        }
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
        } catch (ClassNotFoundException | InstantiationException |
                 IllegalAccessException | UnsupportedLookAndFeelException ex) {
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
