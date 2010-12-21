package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

class MainPanel extends JPanel {
    private final JLabel label       = new JLabel();
    private final JScrollPane scroll = new JScrollPane(label);
    private final JRadioButton r1    = new JRadioButton("scrollRectToVisible");
    private final JRadioButton r2    = new JRadioButton("setViewPosition");
    public MainPanel() {
        super(new BorderLayout());
        Box box = Box.createHorizontalBox();
        ButtonGroup bg = new ButtonGroup();
        box.add(r1); bg.add(r1);
        box.add(r2); bg.add(r2);
        r1.setSelected(true);

        label.setIcon(new ImageIcon(getClass().getResource("CRW_3857_JFR.jpg")));
        HandScrollListener hsl = new HandScrollListener();
        JViewport vport = scroll.getViewport();
        vport.addMouseMotionListener(hsl);
        vport.addMouseListener(hsl);
        add(scroll);
        add(box, BorderLayout.NORTH);
        scroll.setPreferredSize(new Dimension(320, 240));
    }
    class HandScrollListener extends MouseAdapter {
        private final Cursor defCursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
        private final Cursor hndCursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
        private final Point pp = new Point();
        @Override public void mouseDragged(MouseEvent e) {
            JViewport vport = (JViewport)e.getSource();
            Point cp = e.getPoint();
            Point vp = vport.getViewPosition(); //= SwingUtilities.convertPoint(vport,0,0,label);
            vp.translate(pp.x-cp.x, pp.y-cp.y);
            if(r1.isSelected()) {
                label.scrollRectToVisible(new Rectangle(vp, vport.getSize()));
            }else{
                vport.setViewPosition(vp);
            }
            pp.setLocation(cp);
        }
        @Override public void mousePressed(MouseEvent e) {
            ((JComponent)e.getSource()).setCursor(hndCursor);
            pp.setLocation(e.getPoint());
        }
        @Override public void mouseReleased(MouseEvent e) {
            ((JComponent)e.getSource()).setCursor(defCursor);
        }
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
