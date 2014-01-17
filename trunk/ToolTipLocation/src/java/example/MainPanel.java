package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class MainPanel extends JPanel {
    public MainPanel() {
        super();
        MouseAdapter ma = new MouseAdapter() {
            private JWindow window = new JWindow();
            private JToolTip tip = new JToolTip();
            private PopupFactory factory = PopupFactory.getSharedInstance();
            private Popup popup;
            private Point getToolTipLocation(MouseEvent me) {
                JComponent c = (JComponent)me.getSource();
                Point p = me.getPoint();
                SwingUtilities.convertPointToScreen(p, c);
                p.translate(0, -16);
                return p;
            }
            @Override public void mousePressed(MouseEvent me) {
                Point p = getToolTipLocation(me);
                if(SwingUtilities.isLeftMouseButton(me)) {
                    tip.setTipText(String.format("Window(x,y)=(%4d,%4d)", p.x, p.y));
                    window.getContentPane().removeAll();
                    window.add(tip);
                    window.pack();
                    window.setLocation(p);
                    //window.setAlwaysOnTop(true);
                    window.setVisible(true);
                }
            }
            @Override public void mouseDragged(MouseEvent me) {
                JComponent c = (JComponent)me.getSource();
                Point p = me.getPoint();
                if(SwingUtilities.isLeftMouseButton(me)) {
                    tip.setTipText(String.format("Window(x,y)=(%4d,%4d)", p.x, p.y));
                    //tip.revalidate();
                    tip.repaint();
                    //window.pack();
                    window.setLocation(getToolTipLocation(me));
                }else{
                    if(popup!=null) { popup.hide(); }
                    tip.setTipText(String.format("Popup(x,y)=(%d,%d)", p.x, p.y));
                    p = getToolTipLocation(me);
                    popup = factory.getPopup(c, tip, p.x, p.y);
                    popup.show();
                }
            }
            @Override public void mouseReleased(MouseEvent me) {
                if(popup!=null) { popup.hide(); }
                window.setVisible(false);
            }
        };
        addMouseMotionListener(ma);
        addMouseListener(ma);
        add(new JLabel("mouseDragged: Show JToolTip"));
        setPreferredSize(new Dimension(320, 240));
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
