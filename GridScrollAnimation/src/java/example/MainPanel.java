package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());
        JPanel gp = new GridPanel();
        for (int i = 0; i < GridPanel.cols * GridPanel.rows; i++) {
            gp.add(i % 2 == 0 ? new JButton("aa" + i) : new JScrollPane(new JTree()));
        }
        final JScrollPane scrollPane = new JScrollPane(gp);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
//         scrollPane.getVerticalScrollBar().setEnabled(false);
//         scrollPane.getHorizontalScrollBar().setEnabled(false);
//         scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension());
//         scrollPane.getHorizontalScrollBar().setPreferredSize(new Dimension());
        JPanel p = new JPanel();
        p.add(scrollPane);
        add(p);
        add(new JButton(new ScrollAction("right",  scrollPane, new Point(1,  0))), BorderLayout.EAST);
        add(new JButton(new ScrollAction("left",   scrollPane, new Point(-1, 0))), BorderLayout.WEST);
        add(new JButton(new ScrollAction("bottom", scrollPane, new Point(0,  1))), BorderLayout.SOUTH);
        add(new JButton(new ScrollAction("top",    scrollPane, new Point(0, -1))), BorderLayout.NORTH);
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
        frame.setSize(320, 240);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

class GridPanel extends JPanel implements Scrollable {
    public static int cols = 3;
    public static int rows = 4;
    public static Dimension size = new Dimension(160 * cols, 120 * rows);
    protected GridPanel() {
        super(new GridLayout(rows, cols, 0, 0));
        //putClientProperty("JScrollBar.fastWheelScrolling", Boolean.FALSE);
    }
    @Override public Dimension getPreferredScrollableViewportSize() {
        Dimension d = getPreferredSize();
        return new Dimension(d.width / cols, d.height / rows);
    }
    @Override public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        return orientation == SwingConstants.HORIZONTAL ? visibleRect.width : visibleRect.height;
    }
    @Override public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        return orientation == SwingConstants.HORIZONTAL ? visibleRect.width : visibleRect.height;
    }
    @Override public boolean getScrollableTracksViewportWidth() { //NOPMD A getX() method which returns a boolean should be named isX()
        return false;
    }
    @Override public boolean getScrollableTracksViewportHeight() { //NOPMD A getX() method which returns a boolean should be named isX()
        return false;
    }
    @Override public Dimension getPreferredSize() {
        return size;
    }
}

class ScrollAction extends AbstractAction {
    private static final double SIZE = 100d;
    private final Point vec;
    private final JScrollPane scrollPane;
    private final Timer scroller = new Timer(5, null);
    private transient ActionListener listener;
    protected ScrollAction(String name, JScrollPane scrollPane, Point vec) {
        super(name);
        this.scrollPane = scrollPane;
        this.vec = vec;
    }
    @Override public void actionPerformed(ActionEvent e) {
        if (scroller.isRunning()) {
            return;
        }
        final JViewport vport = scrollPane.getViewport();
        final JComponent v = (JComponent) vport.getView();
        final int w  = vport.getWidth();
        final int h  = vport.getHeight();
        final int sx = vport.getViewPosition().x;
        final int sy = vport.getViewPosition().y;
        final Rectangle rect = new Rectangle(w, h);
        scroller.removeActionListener(listener);
        listener = new ActionListener() {
            int count = (int) SIZE;
            @Override public void actionPerformed(ActionEvent e) {
                double a = easeInOut(--count / SIZE);
                int dx = (int) (w - a * w + .5);
                int dy = (int) (h - a * h + .5);
                if (count <= 0) {
                    dx = w;
                    dy = h;
                    scroller.stop();
                }
                rect.setLocation(sx + vec.x * dx, sy + vec.y * dy);
                v.scrollRectToVisible(rect);
            }
        };
        scroller.addActionListener(listener);
        scroller.start();
    }
    private static double easeInOut(double t) {
        //range: 0.0 <= t <= 1.0
        if (t < .5) {
            return .5 * pow3(t * 2d);
        } else {
            return .5 * (pow3(t * 2d - 2d) + 2d);
        }
    }
    private static double pow3(double a) {
        //return Math.pow(a, 3d);
        return a * a * a;
    }
}
