package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.dnd.*;
import java.awt.event.*;
import java.util.Arrays;
import java.net.*;
import javax.swing.*;

public class MainPanel extends JPanel {
    private static final String path = "/toolbarButtonGraphics/general/";
    private final JToolBar toolbar = new JToolBar("ToolBarButton");
    public MainPanel() {
        super(new BorderLayout());

        toolbar.setFloatable(false);
        DragHandler dh = new DragHandler();
        toolbar.addMouseListener(dh);
        toolbar.addMouseMotionListener(dh);
        toolbar.setBorder(BorderFactory.createEmptyBorder(2,2,2,0));

        for(String str: Arrays.asList("Copy24.gif", "Cut24.gif", "Paste24.gif",
                                      "Delete24.gif", "Undo24.gif", "Redo24.gif",
                                      "Help24.gif", "Open24.gif", "Save24.gif")) {
            URL url = getClass().getResource(path+str);
            toolbar.add(createToolbarButton(url));
        }
        add(toolbar, BorderLayout.NORTH);
        add(new JScrollPane(new JTree()));
        setPreferredSize(new Dimension(320, 240));
    }
    private static JComponent createToolbarButton(URL url) {
        JComponent b = new JLabel(new ImageIcon(url));
        b.setOpaque(false);
        return b;
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
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

class DragHandler extends MouseAdapter {
    private final JWindow window = new JWindow();
    private Component draggingComonent = null;
    private int index = -1;
    private Component gap = Box.createHorizontalStrut(24);
    private Point startPt = null;
    private int gestureMotionThreshold = DragSource.getDragThreshold();
    public DragHandler() {
        window.setBackground(new Color(0, true));
    }
    @Override public void mousePressed(MouseEvent e) {
        JComponent parent = (JComponent)e.getComponent();
        if(parent.getComponentCount()<=1) {
            startPt = null;
            return;
        }
        startPt = e.getPoint();
    }
    @Override public void mouseDragged(MouseEvent e) {
        Point pt = e.getPoint();
        JComponent parent = (JComponent)e.getComponent();

        if(startPt != null && Math.sqrt(Math.pow(pt.x-startPt.x, 2)+Math.pow(pt.y-startPt.y, 2))>gestureMotionThreshold) {
            startPt = null;
            Component c = parent.getComponentAt(pt);
            index = parent.getComponentZOrder(c);
            if(c == parent || index < 0) {
                return;
            }
            draggingComonent = c;

            parent.remove(draggingComonent);
            parent.add(gap, index);
            parent.revalidate();
            parent.repaint();

            window.add(draggingComonent);
            window.pack();

            Dimension d = draggingComonent.getPreferredSize();
            Point p = new Point(pt.x - d.width/2, pt.y - d.height/2);
            SwingUtilities.convertPointToScreen(p, parent);
            window.setLocation(p);
            window.setVisible(true);

            return;
        }
        if(!window.isVisible() || draggingComonent==null) {
            return;
        }

        Dimension d = draggingComonent.getPreferredSize();
        Point p = new Point(pt.x - d.width/2, pt.y - d.height/2);
        SwingUtilities.convertPointToScreen(p, parent);
        window.setLocation(p);

        for(int i=0; i<parent.getComponentCount(); i++) {
            Component c = parent.getComponent(i);
            Rectangle r = c.getBounds();
            int wd2 = r.width/2;
            Rectangle r1 = new Rectangle(r.x, r.y, wd2, r.height);
            Rectangle r2 = new Rectangle(r.x+wd2, r.y, wd2, r.height);
            if(r1.contains(pt)) {
                if(c==gap) {
                    return;
                }
                parent.remove(gap);
                parent.add(gap, i-1>0 ? i : 0);
                parent.revalidate();
                parent.repaint();
                return;
            }else if(r2.contains(pt)) {
                if(c==gap) {
                    return;
                }
                parent.remove(gap);
                parent.add(gap, i);
                parent.revalidate();
                parent.repaint();
                return;
            }
        }
        parent.remove(gap);
        parent.revalidate();
        parent.repaint();
    }

    @Override public void mouseReleased(MouseEvent e) {
        startPt = null;
        if(!window.isVisible() || draggingComonent==null) {
            return;
        }
        Point pt = e.getPoint();
        JComponent parent = (JComponent)e.getComponent();

        Component cmp = draggingComonent;
        draggingComonent = null;
        window.setVisible(false);

        for(int i=0; i<parent.getComponentCount(); i++) {
            Component c = parent.getComponent(i);
            Rectangle r = c.getBounds();
            int wd2 = r.width/2;
            Rectangle r1 = new Rectangle(r.x, r.y, wd2, r.height);
            Rectangle r2 = new Rectangle(r.x+wd2, r.y, wd2, r.height);
            if(r1.contains(pt)) {
                parent.remove(gap);
                parent.add(cmp, i-1>0 ? i : 0);
                parent.revalidate();
                parent.repaint();
                return;
            }else if(r2.contains(pt)) {
                parent.remove(gap);
                parent.add(cmp, i);
                parent.revalidate();
                parent.repaint();
                return;
            }
        }
        if(parent.getBounds().contains(pt)) {
            parent.remove(gap);
            parent.add(cmp);
        }else{
            parent.remove(gap);
            parent.add(cmp, index);
        }
        parent.revalidate();
        parent.repaint();
    }
}
