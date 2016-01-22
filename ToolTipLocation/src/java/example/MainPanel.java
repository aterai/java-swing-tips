package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super();
        MouseAdapter ma = new MouseAdapter() {
            private JWindow window = new JWindow();
            private JToolTip tip = new JToolTip();
            private PopupFactory factory = PopupFactory.getSharedInstance();
            private Popup popup;
            private Point getToolTipLocation(MouseEvent e) {
                Point p = e.getPoint();
                SwingUtilities.convertPointToScreen(p, e.getComponent());
                p.translate(0, -16);
                return p;
            }
            @Override public void mousePressed(MouseEvent e) {
                Point p = getToolTipLocation(e);
                if (SwingUtilities.isLeftMouseButton(e)) {
                    tip.setTipText(String.format("Window(x, y)=(%4d,%4d)", p.x, p.y));
                    window.getContentPane().removeAll();
                    window.add(tip);
                    window.pack();
                    window.setLocation(p);
                    //window.setAlwaysOnTop(true);
                    window.setVisible(true);
                }
            }
            @Override public void mouseDragged(MouseEvent e) {
                Point p = e.getPoint();
                if (SwingUtilities.isLeftMouseButton(e)) {
                    tip.setTipText(String.format("Window(x, y)=(%4d,%4d)", p.x, p.y));
                    //tip.revalidate();
                    tip.repaint();
                    //window.pack();
                    window.setLocation(getToolTipLocation(e));
                } else {
                    if (popup != null) {
                        popup.hide();
                    }
                    tip.setTipText(String.format("Popup(x, y)=(%d,%d)", p.x, p.y));
                    p = getToolTipLocation(e);
                    popup = factory.getPopup(e.getComponent(), tip, p.x, p.y);
                    popup.show();
                }
            }
            @Override public void mouseReleased(MouseEvent e) {
                if (popup != null) {
                    popup.hide();
                }
                window.setVisible(false);
            }
        };
        addMouseMotionListener(ma);
        addMouseListener(ma);
        add(new JLabel("mouseDragged: Show JToolTip"));
        setPreferredSize(new Dimension(320, 240));
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
