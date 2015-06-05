package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.util.Objects;
import javax.swing.*;
import javax.swing.plaf.basic.*;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());
        add(makeUI());
        setOpaque(false);
        setPreferredSize(new Dimension(320, 240));
    }
    private static JComponent makeUI() {
        final JInternalFrame internal = new JInternalFrame("@title@");
        BasicInternalFrameUI ui = (BasicInternalFrameUI) internal.getUI();
        Component title = ui.getNorthPane();
        for (MouseMotionListener l: title.getListeners(MouseMotionListener.class)) {
            title.removeMouseMotionListener(l);
        }
        DragWindowListener dwl = new DragWindowListener();
        title.addMouseListener(dwl);
        title.addMouseMotionListener(dwl);
        JPanel p = new JPanel(new BorderLayout());
        p.add(new JScrollPane(new JTree()));
        p.add(new JButton(new AbstractAction("close") {
            @Override public void actionPerformed(ActionEvent e) {
                Window w = SwingUtilities.getWindowAncestor((Component) e.getSource());
                //w.dispose();
                w.dispatchEvent(new WindowEvent(w, WindowEvent.WINDOW_CLOSING));
            }
        }), BorderLayout.SOUTH);
        internal.getContentPane().add(p);
        internal.setVisible(true);

        KeyboardFocusManager focusManager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        focusManager.addPropertyChangeListener(new PropertyChangeListener() {
            @Override public void propertyChange(PropertyChangeEvent e) {
                String prop = e.getPropertyName();
                //System.out.println(prop);
                if ("activeWindow".equals(prop)) {
                    try {
                        internal.setSelected(Objects.nonNull(e.getNewValue()));
                    } catch (PropertyVetoException ex) {
                        ex.printStackTrace();
                    }
                    //System.out.println("---------------------");
                }
            }
        });

//         frame.addWindowListener(new WindowAdapter() {
//             @Override public void windowLostFocus(FocusEvent e) {
//                 System.out.println("bbbbbbbbb");
//                 try {
//                     internal.setSelected(false);
//                 } catch (PropertyVetoException ex) { ex.printStackTrace(); }
//             }
//             @Override public void windowGainedFocus(FocusEvent e) {
//                 System.out.println("aaaaaaaa");
//                 try {
//                     internal.setSelected(true);
//                 } catch (PropertyVetoException ex) { ex.printStackTrace(); }
//             }
//         });
//         EventQueue.invokeLater(new Runnable() {
//             @Override public void run() {
//                 try {
//                     internal.setSelected(true);
//                 } catch (java.beans.PropertyVetoException ex) {
//                     ex.printStackTrace();
//                 }
//                 //internal.requestFocusInWindow();
//             }
//         });
        return internal;
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
            //UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            for (UIManager.LookAndFeelInfo laf: UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(laf.getName())) {
                    UIManager.setLookAndFeel(laf.getClassName());
                }
            }
        } catch (ClassNotFoundException | InstantiationException
               | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        }
        JFrame frame = new JFrame();
        frame.setUndecorated(true);
        frame.setMinimumSize(new Dimension(300, 120));
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.setBackground(new Color(0x0, true)); //JDK 1.7
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

class DragWindowListener extends MouseAdapter {
    private final Point startPt = new Point();
    @Override public void mousePressed(MouseEvent me) {
        startPt.setLocation(me.getPoint());
    }
    @Override public void mouseDragged(MouseEvent me) {
        Component c = SwingUtilities.getRoot(me.getComponent());
        if (c instanceof Window) {
            Point eventLocationOnScreen = me.getLocationOnScreen();
            ((Window) c).setLocation(eventLocationOnScreen.x - startPt.x,
                                     eventLocationOnScreen.y - startPt.y);
        }
    }
}
