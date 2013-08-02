package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.plaf.LayerUI;

class MainPanel extends JPanel {
    public MainPanel() {
        super(new GridLayout(1,2,5,5));

        Box box1 = makeTestBox();
        box1.setBorder(BorderFactory.createTitledBorder("DragScrollListener"));
        MouseAdapter l = new DragScrollListener();
        box1.addMouseListener(l);
        box1.addMouseMotionListener(l);
        add(new JScrollPane(box1));

        Box box2 = makeTestBox();
        box2.setBorder(BorderFactory.createTitledBorder("DragScrollLayerUI"));
        add(new JLayer<JScrollPane>(new JScrollPane(box2), new DragScrollLayerUI()));

        setPreferredSize(new Dimension(320, 240));
    }

    private static Box makeTestBox() {
        JTabbedPane tab1 = new JTabbedPane();
        tab1.addTab("aaa", new JLabel("11111111111"));
        tab1.addTab("bbb", new JCheckBox("2222222222"));

        JTabbedPane tab2 = new JTabbedPane();
        tab2.addTab("ccccc", new JLabel("3333"));
        tab2.addTab("ddddd", new JLabel("444444444444"));

        JTree tree = new JTree();
        tree.setVisibleRowCount(5);

        Box box = Box.createVerticalBox();
        box.add(new JLabel("aaaaaaaaaaaaaaaaaaaaaa"));
        box.add(Box.createVerticalStrut(5));
        box.add(tab1);
        box.add(Box.createVerticalStrut(5));
        box.add(new JCheckBox("bbbbbbbbbbbb"));
        box.add(Box.createVerticalStrut(5));
        box.add(tab2);
        box.add(Box.createVerticalStrut(5));
        box.add(new JButton("ccccc"));
        box.add(Box.createVerticalStrut(5));
        box.add(new JScrollPane(tree));
        box.add(Box.createVerticalStrut(5));
        box.add(new JLabel("ddddddddddddddd"));
        box.add(Box.createVerticalGlue());

        return box;
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
class DragScrollListener extends MouseAdapter {
    private final Cursor defCursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
    private final Cursor hndCursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
    private final Point pp = new Point();
    @Override public void mouseDragged(MouseEvent e) {
        final JComponent jc = (JComponent)e.getSource();
        Container c = jc.getParent();
        if(c instanceof JViewport) {
            JViewport vport = (JViewport)c;
            Point cp = SwingUtilities.convertPoint(jc,e.getPoint(),vport);
            Point vp = vport.getViewPosition();
            vp.translate(pp.x-cp.x, pp.y-cp.y);
            jc.scrollRectToVisible(new Rectangle(vp, vport.getSize()));
            pp.setLocation(cp);
        }
    }
    @Override public void mousePressed(MouseEvent e) {
        JComponent jc = (JComponent)e.getSource();
        Container c = jc.getParent();
        if(c instanceof JViewport) {
            jc.setCursor(hndCursor);
            JViewport vport = (JViewport)c;
            Point cp = SwingUtilities.convertPoint(jc,e.getPoint(),vport);
            pp.setLocation(cp);
        }
    }
    @Override public void mouseReleased(MouseEvent e) {
        ((JComponent)e.getSource()).setCursor(defCursor);
    }
}
class DragScrollLayerUI extends LayerUI<JScrollPane> {
    private final Point pp = new Point();
    @Override public void installUI(JComponent c) {
        super.installUI(c);
        JLayer jlayer = (JLayer)c;
        jlayer.setLayerEventMask(
            AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK |
            AWTEvent.MOUSE_WHEEL_EVENT_MASK | AWTEvent.KEY_EVENT_MASK);
    }
    @Override public void uninstallUI(JComponent c) {
        JLayer jlayer = (JLayer)c;
        jlayer.setLayerEventMask(0);
        super.uninstallUI(c);
    }
    @Override protected void processMouseEvent(MouseEvent e, JLayer<? extends JScrollPane> l) {
        if(e.getComponent() instanceof JScrollBar) {
            return;
        }
        if(e.getID()==MouseEvent.MOUSE_PRESSED) {
            JViewport vport = l.getView().getViewport();
            Point cp = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), vport);
            pp.setLocation(cp);
        }
    }
    @Override protected void processMouseMotionEvent(MouseEvent e, JLayer<? extends JScrollPane> l) {
        if(e.getComponent() instanceof JScrollBar) {
            return;
        }
        if(e.getID()==MouseEvent.MOUSE_DRAGGED) {
            JViewport vport = l.getView().getViewport();
            JComponent cmp = (JComponent)vport.getView();
            Point cp = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), vport);
            Point vp = vport.getViewPosition();
            vp.translate(pp.x-cp.x, pp.y-cp.y);
            cmp.scrollRectToVisible(new Rectangle(vp, vport.getSize()));
            pp.setLocation(cp);
        }
    }
}
