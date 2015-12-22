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
    public MainPanel() {
        super(new BorderLayout());

        JPanel p = new JPanel(new BorderLayout());
        p.add(new JScrollPane(new JTree()));
        p.add(new JButton(new AbstractAction("close") {
            @Override public void actionPerformed(ActionEvent e) {
                Container c = getTopLevelAncestor();
                if (c instanceof Window) {
                    Window w = (Window) c;
                    w.dispatchEvent(new WindowEvent(w, WindowEvent.WINDOW_CLOSING));
                }
            }
        }), BorderLayout.SOUTH);

        JMenuBar mb = new JMenuBar();
        mb.add(LookAndFeelUtil.createLookAndFeelMenu());

        JInternalFrame f = new DraggableInternalFrame("@title@");
        f.getContentPane().add(p);
        f.setJMenuBar(mb);
        f.setVisible(true);

        add(f);
        setPreferredSize(new Dimension(320, 240));
    }
    @Override public void updateUI() {
        super.updateUI();
        // Translucent resize area for mouse cursor >>>
        setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        setBackground(new Color(1f, 1f, 1f, .01f));
        //<<<
    }
//     private ButtonGroup lookAndFeelRadioGroup;
//     private String lookAndFeel;
//     protected JMenu createLookAndFeelMenu() {
//         JMenu menu = new JMenu("LookAndFeel");
//         lookAndFeel = UIManager.getLookAndFeel().getClass().getName();
//         lookAndFeelRadioGroup = new ButtonGroup();
//         for (UIManager.LookAndFeelInfo lafInfo: UIManager.getInstalledLookAndFeels()) {
//             menu.add(createLookAndFeelItem(lafInfo.getName(), lafInfo.getClassName()));
//         }
//         return menu;
//     }
//     protected JRadioButtonMenuItem createLookAndFeelItem(String lafName, String lafClassName) {
//         JRadioButtonMenuItem lafItem = new JRadioButtonMenuItem();
//         lafItem.setSelected(lafClassName.equals(lookAndFeel));
//         lafItem.setHideActionText(true);
//         lafItem.setAction(new AbstractAction() {
//             @Override public void actionPerformed(ActionEvent e) {
//                 ButtonModel m = lookAndFeelRadioGroup.getSelection();
//                 try {
//                     setLookAndFeel(m.getActionCommand());
//                 } catch (ClassNotFoundException | InstantiationException
//                        | IllegalAccessException | UnsupportedLookAndFeelException ex) {
//                     ex.printStackTrace();
//                 }
//             }
//         });
//         lafItem.setText(lafName);
//         lafItem.setActionCommand(lafClassName);
//         lookAndFeelRadioGroup.add(lafItem);
//         return lafItem;
//     }
//     public void setLookAndFeel(String lookAndFeel) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
//         String oldLookAndFeel = this.lookAndFeel;
//         if (!oldLookAndFeel.equals(lookAndFeel)) {
//             UIManager.setLookAndFeel(lookAndFeel);
//             this.lookAndFeel = lookAndFeel;
//             updateLookAndFeel();
//             firePropertyChange("lookAndFeel", oldLookAndFeel, lookAndFeel);
//         }
//     }
//     private void updateLookAndFeel() {
//         for (Window window: Frame.getWindows()) {
//             if (window instanceof RootPaneContainer) {
//                 RootPaneContainer rpc = (RootPaneContainer) window;
//                 SwingUtilities.updateComponentTreeUI(rpc.getContentPane());
//             }
//         }
//     }
    public static void main(String... args) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                createAndShowGUI();
            }
        });
    }
    public static void createAndShowGUI() {
        JFrame frame = new JFrame();
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException
               | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        }
        //XXX: JFrame frame = new JFrame();
        frame.setUndecorated(true);

        JRootPane root = frame.getRootPane();
        root.setWindowDecorationStyle(JRootPane.PLAIN_DIALOG);
        JLayeredPane layeredPane = root.getLayeredPane();
        Component c = layeredPane.getComponent(1);
        if (c instanceof JComponent) {
            JComponent orgTitlePane = (JComponent) c;
            orgTitlePane.setVisible(false);
            //layeredPane.remove(orgTitlePane);
        }
        //JComponent dummyTitlePane = new JLabel();
        //layeredPane.add(dummyTitlePane, JLayeredPane.FRAME_CONTENT_LAYER);
        //dummyTitlePane.setVisible(true);

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
    @Override public void mousePressed(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)) {
            startPt.setLocation(e.getPoint());
        }
    }
    @Override public void mouseDragged(MouseEvent e) {
        Component c = SwingUtilities.getRoot(e.getComponent());
        if (c instanceof Window && SwingUtilities.isLeftMouseButton(e)) {
            Window window = (Window) c;
            Point pt = window.getLocation();
            window.setLocation(pt.x - startPt.x + e.getX(), pt.y - startPt.y + e.getY());
        }
    }
}

class DraggableInternalFrame extends JInternalFrame {
    protected DraggableInternalFrame(String title) {
        super(title);
        KeyboardFocusManager focusManager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        focusManager.addPropertyChangeListener(new PropertyChangeListener() {
            @Override public void propertyChange(PropertyChangeEvent e) {
                String prop = e.getPropertyName();
                if ("activeWindow".equals(prop)) {
                    try {
                        setSelected(Objects.nonNull(e.getNewValue()));
                    } catch (PropertyVetoException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
    }
    @Override public void updateUI() {
        super.updateUI();
        BasicInternalFrameUI ui = (BasicInternalFrameUI) getUI();
        Component titleBar = ui.getNorthPane();
        for (MouseMotionListener l: titleBar.getListeners(MouseMotionListener.class)) {
            titleBar.removeMouseMotionListener(l);
        }
        DragWindowListener dwl = new DragWindowListener();
        titleBar.addMouseListener(dwl);
        titleBar.addMouseMotionListener(dwl);
    }
}

//http://java.net/projects/swingset3/sources/svn/content/trunk/SwingSet3/src/com/sun/swingset3/SwingSet3.java
final class LookAndFeelUtil {
    private static String lookAndFeel = UIManager.getLookAndFeel().getClass().getName();
    private LookAndFeelUtil() { /* Singleton */ }
    public static JMenu createLookAndFeelMenu() {
        JMenu menu = new JMenu("LookAndFeel");
        ButtonGroup lookAndFeelRadioGroup = new ButtonGroup();
        for (UIManager.LookAndFeelInfo lafInfo: UIManager.getInstalledLookAndFeels()) {
            menu.add(createLookAndFeelItem(lafInfo.getName(), lafInfo.getClassName(), lookAndFeelRadioGroup));
        }
        return menu;
    }
    private static JRadioButtonMenuItem createLookAndFeelItem(String lafName, String lafClassName, final ButtonGroup lookAndFeelRadioGroup) {
        JRadioButtonMenuItem lafItem = new JRadioButtonMenuItem();
        lafItem.setSelected(lafClassName.equals(lookAndFeel));
        lafItem.setHideActionText(true);
        lafItem.setAction(new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                ButtonModel m = lookAndFeelRadioGroup.getSelection();
                try {
                    setLookAndFeel(m.getActionCommand());
                } catch (ClassNotFoundException | InstantiationException
                       | IllegalAccessException | UnsupportedLookAndFeelException ex) {
                    ex.printStackTrace();
                }
            }
        });
        lafItem.setText(lafName);
        lafItem.setActionCommand(lafClassName);
        lookAndFeelRadioGroup.add(lafItem);
        return lafItem;
    }
    private static void setLookAndFeel(String lookAndFeel) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
        String oldLookAndFeel = LookAndFeelUtil.lookAndFeel;
        if (!oldLookAndFeel.equals(lookAndFeel)) {
            UIManager.setLookAndFeel(lookAndFeel);
            LookAndFeelUtil.lookAndFeel = lookAndFeel;
            updateLookAndFeel();
            //firePropertyChange("lookAndFeel", oldLookAndFeel, lookAndFeel);
        }
    }
    private static void updateLookAndFeel() {
        for (Window window: Frame.getWindows()) {
            if (window instanceof JFrame) {
                SwingUtilities.updateComponentTreeUI(((JFrame) window).getContentPane());
            }
        }
    }
}
