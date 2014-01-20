package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class MainPanel extends JPanel {
    public MainPanel() {
        super(new BorderLayout());
        JPanel gp = new GridPanel();
        for(int i = 0; i<GridPanel.cols*GridPanel.rows;i++) {
            gp.add(i%2==0 ? new JButton("aa"+i) : new JScrollPane(new JTree()));
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
        add(new JButton(new ScrollAction("right",  scrollPane, new Point( 1,  0))), BorderLayout.EAST);
        add(new JButton(new ScrollAction("left",   scrollPane, new Point(-1,  0))), BorderLayout.WEST);
        add(new JButton(new ScrollAction("bottom", scrollPane, new Point( 0,  1))), BorderLayout.SOUTH);
        add(new JButton(new ScrollAction("top",    scrollPane, new Point( 0, -1))), BorderLayout.NORTH);
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
        }catch(ClassNotFoundException | InstantiationException |
               IllegalAccessException | UnsupportedLookAndFeelException ex) {
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
    public static int cols = 3, rows = 4;
    public static Dimension size = new Dimension(160*cols, 120*rows);
    public GridPanel() {
        super(new GridLayout(rows, cols, 0, 0));
        //putClientProperty("JScrollBar.fastWheelScrolling", Boolean.FALSE);
    }
    @Override public Dimension getPreferredScrollableViewportSize() {
        Dimension d = getPreferredSize();
        return new Dimension(d.width/cols, d.height/rows);
    }
    @Override public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        return orientation==SwingConstants.HORIZONTAL ? visibleRect.width : visibleRect.height;
    }
    @Override public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        return orientation==SwingConstants.HORIZONTAL ? visibleRect.width : visibleRect.height;
    }
    @Override public boolean getScrollableTracksViewportWidth() {
        return false;
    }
    @Override public boolean getScrollableTracksViewportHeight() {
        return false;
    }
    @Override public Dimension getPreferredSize() {
        return size;
    }
}

class ScrollAction extends AbstractAction {
    private static Timer scroller;
    private final Point vec;
    private final JScrollPane scrollPane;
    public ScrollAction(String name, JScrollPane scrollPane, Point vec) {
        super(name);
        this.scrollPane = scrollPane;
        this.vec = vec;
    }
    @Override public void actionPerformed(ActionEvent e) {
        final JViewport vport = scrollPane.getViewport();
        final JComponent v = (JComponent)vport.getView();
        final int w   = vport.getWidth(),
                  h   = vport.getHeight(),
                  sx  = vport.getViewPosition().x,
                  sy  = vport.getViewPosition().y;
        final Rectangle rect = new Rectangle(w, h);
        if(scroller!=null && scroller.isRunning()) {
            return;
        }
        scroller = new Timer(5, new ActionListener() {
            double SIZE = 100d;
            int count = (int)SIZE;
            @Override public void actionPerformed(ActionEvent e) {
                double a = easeInOut(--count/SIZE);
                int dx = (int)(w - a*w + 0.5d);
                int dy = (int)(h - a*h + 0.5d);
                if(count<=0) {
                    dx = w; dy = h;
                    scroller.stop();
                }
                rect.setLocation(sx + vec.x * dx, sy + vec.y * dy);
                v.scrollRectToVisible(rect);
            }
        });
        scroller.start();
    }
    private static double easeInOut(double t) {
        //range: 0.0<=t<=1.0
        if(t<0.5d) {
            return 0.5d*pow3(t*2d);
        }else{
            return 0.5d*(pow3(t*2d-2d) + 2d);
        }
    }
    private static double pow3(double a) {
        //return Math.pow(a, 3d);
        return a * a * a;
    }
}
