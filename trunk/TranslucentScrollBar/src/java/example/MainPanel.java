package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.Date;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.plaf.basic.BasicScrollBarUI;

class MainPanel extends JPanel {
    public MainPanel() {
        super(new GridLayout(1, 2));
//         ImageIcon icon = new ImageIcon(getClass().getResource("CRW_3857_JFR.jpg")); //http://sozai-free.com/
//         MouseAdapter dsl = new DragScrollListener();
//         JLabel l1 = new JLabel(icon);
//         l1.addMouseMotionListener(dsl);
//         l1.addMouseListener(dsl);
//         JLabel l2 = new JLabel(icon);
//         l2.addMouseMotionListener(dsl);
//         l2.addMouseListener(dsl);
//         add(makeScrollPane(l1));
//         add(makeTranslucentScrollBar(makeScrollPane(l2)));

        add(makeScrollPane(makeList()));
        add(makeTranslucentScrollBar(makeScrollPane(makeList())));
        setPreferredSize(new Dimension(320, 240));
    }
    private static JComponent makeList() {
        DefaultListModel<String> model = new DefaultListModel<>();
        for(int i=0; i<50; i++) {
            Date d = new Date();
            model.addElement(String.format("%d: %s", i, d.toString()));
        }
        JList<String> list = new JList<>(model);
        RepaintHandler handler = new RepaintHandler();
        list.addListSelectionListener(handler);
        list.addFocusListener(handler);
        return list;
    }
    private static JScrollPane makeScrollPane(JComponent c) {
        return new JScrollPane(
            c, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
               ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    }
    public JComponent makeTranslucentScrollBar(JScrollPane scrollPane) {
        scrollPane.getVerticalScrollBar().setUI(new TranslucentScrollBarUI());

        scrollPane.setComponentZOrder(scrollPane.getVerticalScrollBar(), 0);
        scrollPane.setComponentZOrder(scrollPane.getViewport(), 1);
        scrollPane.getVerticalScrollBar().setOpaque(false);

        scrollPane.setLayout(new ScrollPaneLayout() {
            @Override public void layoutContainer(Container parent) {
                if(parent instanceof JScrollPane) {
                    JScrollPane scrollPane = (JScrollPane)parent;

                    Rectangle availR = scrollPane.getBounds();
                    availR.setLocation(0, 0); //availR.x = availR.y = 0;

                    Insets insets = parent.getInsets();
                    availR.x = insets.left;
                    availR.y = insets.top;
                    availR.width  -= insets.left + insets.right;
                    availR.height -= insets.top  + insets.bottom;

                    Rectangle vsbR = new Rectangle();
                    vsbR.width  = 12;
                    vsbR.height = availR.height;
                    vsbR.x = availR.x + availR.width - vsbR.width;
                    vsbR.y = availR.y;

                    if(viewport != null) {
                        viewport.setBounds(availR);
                    }
                    if(vsb != null) {
                        vsb.setVisible(true);
                        vsb.setBounds(vsbR);
                    }
                }
            }
        });
        return scrollPane;
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
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

class RepaintHandler extends FocusAdapter implements ListSelectionListener {
    private static void repaintEvent(Component c) {
        Container p = SwingUtilities.getAncestorOfClass(JScrollPane.class, c);
        if(p!=null) {
            p.repaint();
        }
    }
    @Override public void valueChanged(ListSelectionEvent e) {
        repaintEvent((Component)e.getSource());
    }
    @Override public void focusLost(FocusEvent e) {
        repaintEvent(e.getComponent());
    }
    @Override public void focusGained(FocusEvent e) {
        repaintEvent(e.getComponent());
    }
}

class TranslucentScrollBarUI extends BasicScrollBarUI {
    private static final Color DEFAULT_COLOR  = new Color(220, 100, 100, 100);
    private static final Color DRAGGING_COLOR = new Color(200, 100, 100, 100);
    private static final Color ROLLOVER_COLOR = new Color(255, 120, 100, 100);
    private static final Dimension ZERO_SIZE = new Dimension();
    private static class ZeroSizeButton extends JButton {
        @Override public Dimension getPreferredSize() {
            return ZERO_SIZE;
        }
    }
    @Override protected JButton createDecreaseButton(int orientation) {
        return new ZeroSizeButton();
    }
    @Override protected JButton createIncreaseButton(int orientation) {
        return new ZeroSizeButton();
    }
    @Override protected void paintTrack(Graphics g, JComponent c, Rectangle r) {
        //Graphics2D g2 = (Graphics2D)g.create();
        //g2.setPaint(new Color(100,100,100,100));
        //g2.fillRect(r.x,r.y,r.width-1,r.height-1);
        //g2.dispose();
    }
    @Override protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
        Graphics2D g2 = (Graphics2D)g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Color color = null;
        JScrollBar sb = (JScrollBar)c;
        if(!sb.isEnabled() || r.width>r.height) {
            return;
        }else if(isDragging) {
            color = DRAGGING_COLOR;
        }else if(isThumbRollover()) {
            color = ROLLOVER_COLOR;
        }else{
            color = DEFAULT_COLOR;
        }
        g2.setPaint(color);
        g2.fillRect(r.x,r.y,r.width-1,r.height-1);
        g2.setPaint(Color.WHITE);
        g2.drawRect(r.x,r.y,r.width-1,r.height-1);
        g2.dispose();
    }
    @Override protected void setThumbBounds(int x, int y, int width, int height) {
        super.setThumbBounds(x, y, width, height);
        //scrollbar.repaint(x, 0, width, scrollbar.getHeight());
        scrollbar.repaint();
    }
}

// class DragScrollListener extends MouseAdapter {
//     private final Cursor defCursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
//     private final Cursor hndCursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
//     private final Point pp = new Point();
//     @Override public void mouseDragged(MouseEvent e) {
//         Component c = e.getComponent();
//         Container p = SwingUtilities.getUnwrappedParent(c);
//         if(p instanceof JViewport) {
//             JViewport vport = (JViewport)p;
//             Point cp = SwingUtilities.convertPoint(c, e.getPoint(), vport);
//             Point vp = vport.getViewPosition();
//             vp.translate(pp.x-cp.x, pp.y-cp.y);
//             ((JComponent)c).scrollRectToVisible(new Rectangle(vp, vport.getSize()));
//             pp.setLocation(cp);
//         }
//     }
//     @Override public void mousePressed(MouseEvent e) {
//         Component c = e.getComponent();
//         Container p = SwingUtilities.getUnwrappedParent(c);
//         if(p instanceof JViewport) {
//             c.setCursor(hndCursor);
//             JViewport vport = (JViewport)p;
//             Point cp = SwingUtilities.convertPoint(c, e.getPoint(), vport);
//             pp.setLocation(cp);
//         }
//     }
//     @Override public void mouseReleased(MouseEvent e) {
//         e.getComponent().setCursor(defCursor);
//     }
// }
