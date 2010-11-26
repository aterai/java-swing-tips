package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.event.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.plaf.basic.*;

public class MainPanel extends JPanel {
    private final JDesktopPane desktop          = new JDesktopPane();
    private final JInternalFrame magneticFrame1 = createFrame("Frame");
    private final JInternalFrame magneticFrame2 = createFrame("Frame");
    public MainPanel() {
        super(new BorderLayout());
        desktop.setBackground(Color.GRAY.brighter());
        desktop.setDragMode(JDesktopPane.OUTLINE_DRAG_MODE);
        desktop.setDesktopManager(new DefaultDesktopManager() {
            //@override
            @Override public void dragFrame(JComponent frame, int x, int y) {
                int e = x; int n = y;
                int w = desktop.getSize().width -frame.getSize().width -e;
                int s = desktop.getSize().height-frame.getSize().height-n;
                if(isNear(e) || isNear(n) || isNear(w) || isNear(s)) {
                    x = (e<w)?(isNear(e)?0:e):(isNear(w)?w+e:e);
                    y = (n<s)?(isNear(n)?0:n):(isNear(s)?s+n:n);
                }
                super.dragFrame(frame, x, y);
            }
            private boolean isNear(int c) {
                return (Math.abs(c)<10);
            }
//             public void _dragFrame(JComponent frame, int x, int y) {
//                 int e = x; int n = y;
//                 int w = desktop.getSize().width -frame.getSize().width -e;
//                 int s = desktop.getSize().height-frame.getSize().height-n;
//                 x = (e<w)?((e<0)?0:e):((w<0)?w+e:e);
//                 y = (n<s)?((n<0)?0:n):((s<0)?s+n:n);
//                 super.dragFrame(frame, x, y);
//             }
        });
//         BasicInternalFrameUI ui = (BasicInternalFrameUI)magneticFrame.getUI();
//         Component north = ui.getNorthPane();
//         MouseInputAdapter mml = new MagneticListener(magneticFrame);
//         north.addMouseMotionListener(mml);
//         north.addMouseListener(mml);

        desktop.add(magneticFrame1);
        magneticFrame1.setLocation(30, 10);
        magneticFrame1.setVisible(true);
        desktop.add(magneticFrame2);
        magneticFrame2.setLocation(50, 30);
        magneticFrame2.setVisible(true);

        add(desktop);
        setPreferredSize(new Dimension(320, 240));
    }
    private JInternalFrame createFrame(String title) {
        //title, resizable, closable, maximizable, iconifiable
        JInternalFrame frame = new JInternalFrame(title, false, false, true, true);
        frame.setSize(200, 100);
        return frame;
    }

//     private class MagneticListener extends MouseInputAdapter {
//         private final JInternalFrame frame;
//         private final Point loc = new Point();
//         public MagneticListener(JInternalFrame frame) {
//             this.frame = frame;
//         }
//         public void mousePressed(MouseEvent e) {
//             Point p1 = frame.getLocation();
//             Point p2 = SwingUtilities.convertPoint(frame, e.getPoint(), frame.getDesktopPane());
//             loc.setLocation(p2.x-p1.x, p2.y-p1.y);
//         }
//         public void mouseDragged(MouseEvent me) {
//             JDesktopPane desktop = frame.getDesktopPane();
//             Point ep = me.getPoint();
//             Point pt = SwingUtilities.convertPoint(frame, ep.x-loc.x, ep.y-loc.y, desktop);
//             int e = pt.x;
//             int n = pt.y;
//             int w = desktop.getSize().width -frame.getSize().width -e;
//             int s = desktop.getSize().height-frame.getSize().height-n;
//             if(isNear(e) || isNear(n) || isNear(w) || isNear(s)) {
//                 int x = (e<w)?(isNear(e)?0:e):(isNear(w)?w+e:e);
//                 int y = (n<s)?(isNear(n)?0:n):(isNear(s)?s+n:n);
//                 desktop.getDesktopManager().dragFrame(frame,x,y);
//             }
//         }
//         private boolean isNear(int c) {
//             return (Math.abs(c)<10);
//         }
//     }

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
        frame.setResizable(false);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
