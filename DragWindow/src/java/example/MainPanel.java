package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
//import javax.swing.event.*;

class MainPanel{
    private JFrame frame;
    private JWindow splashScreen;
    private JLabel  splashLabel;

    public void start(JFrame frame) {
        this.frame = frame;
        createSplashScreen("splash.png");
        showSplashScreen();
        (new Thread() {
            public void run() {
                try{
                    //dummy long task
                    Thread.sleep(6000);
                    EventQueue.invokeAndWait(new Runnable() {
                        @Override public void run() {
                            showFrame();
                            hideSplash();
                        }
                    });
                }catch(Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
    private JPanel makeUI() {
        JLabel label = new JLabel("Draggable Label (@title@)");
        DragWindowListener dwl = new DragWindowListener();
        label.addMouseListener(dwl);
        label.addMouseMotionListener(dwl);
        label.setOpaque(true);
        label.setForeground(Color.WHITE);
        label.setBackground(Color.BLUE);
        label.setBorder(BorderFactory.createEmptyBorder(5,16+5,5,2));
        Box box = Box.createHorizontalBox();
        box.add(Box.createHorizontalGlue());
        box.add(new JButton(new AbstractAction("Exit") {
            @Override public void actionPerformed(ActionEvent e) {
                //frame.dispose();
                //System.exit(0);
                frame.getToolkit().getSystemEventQueue().postEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
            }
        }));
        JPanel p = new JPanel(new BorderLayout());
        p.add(label, BorderLayout.NORTH);
        p.add(box, BorderLayout.SOUTH);
        p.add(new JLabel("Alt+Space => System Menu"));
        return p;
    }
    public void createSplashScreen(String path) {
        ImageIcon img = new ImageIcon(getClass().getResource(path));
        DragWindowListener dwl = new DragWindowListener();

        splashLabel = new JLabel(img);
        splashLabel.addMouseListener(dwl);
        splashLabel.addMouseMotionListener(dwl);
        splashScreen = new JWindow(getFrame());
        splashScreen.getContentPane().add(splashLabel);
        splashScreen.pack();
        splashScreen.setLocationRelativeTo(null);
    }
    public void showSplashScreen() {
        splashScreen.setVisible(true);
    }
    public void hideSplash() {
        splashScreen.setVisible(false);
        splashScreen.dispose();
    }
    public JFrame getFrame() {
        return frame;
    }
    public void showFrame() {
        frame.getContentPane().add(makeUI());
        frame.setMinimumSize(new Dimension(100, 100));
        frame.setSize(320, 240);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

//     protected JMenuBar createMenuBar() {
//         JMenuBar menuBar = new JMenuBar();
//         JMenu menu = new JMenu("FFFFFF");
//         menu.setMnemonic(KeyEvent.VK_F);
//         menuBar.add(menu);
//
//         JMenuItem menuItem = new JMenuItem("NNNNNNNNN");
//         menuItem.setMnemonic(KeyEvent.VK_N);
//         menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.ALT_MASK));
//         menu.add(menuItem);
//
//         menuItem = new JMenuItem("MMMMMMMM");
//         menuItem.setMnemonic(KeyEvent.VK_M);
//         menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, ActionEvent.ALT_MASK));
//         menu.add(menuItem);
//
//         menuItem = new JMenuItem("UUUUUU");
//         menuItem.setMnemonic(KeyEvent.VK_U);
//         menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_U, ActionEvent.ALT_MASK));
//         menu.add(menuItem);
//
//         menuItem = new JMenuItem("IIIIIIIIII");
//         menuItem.setMnemonic(KeyEvent.VK_I);
//         menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, ActionEvent.ALT_MASK));
//         menu.add(menuItem);
//
//         return menuBar;
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
        JFrame frame = new JFrame();
        frame.setUndecorated(true);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        //frame.getContentPane().add(new MainPanel(frame));
        //frame.setMinimumSize(new Dimension(100, 100));
        //frame.setSize(320, 240);
        //frame.setLocationRelativeTo(null);
        //frame.setVisible(true);
        new MainPanel().start(frame);
    }
}

class DragWindowListener extends MouseAdapter {
    private MouseEvent start;
    //private Point  loc;
    private Window window;
    @Override public void mousePressed(MouseEvent me) {
        start = me;
    }
    @Override public void mouseDragged(MouseEvent me) {
        if(window==null) {
            window = SwingUtilities.windowForComponent(me.getComponent());
        }
        Point eventLocationOnScreen = me.getLocationOnScreen();
        window.setLocation(eventLocationOnScreen.x - start.getX(),
                           eventLocationOnScreen.y - start.getY());
        //loc = window.getLocation(loc);
        //int x = loc.x - start.getX() + me.getX();
        //int y = loc.y - start.getY() + me.getY();
        //window.setLocation(x, y);
    }
}
