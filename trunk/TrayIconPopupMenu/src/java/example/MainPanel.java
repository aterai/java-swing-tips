package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

class MainPanel extends JPanel {
    private final SystemTray tray  = SystemTray.getSystemTray();
    private final Image image      = new ImageIcon(getClass().getResource("16x16.png")).getImage();
    private final TrayIcon icon    = new TrayIcon(image, "TRAY", null);
    private final JPopupMenu popup = new JPopupMenu();
    //private final JWindow dummy    = new JWindow(); //Ubuntu?
    private final JDialog dummy    = new JDialog();
    private final JFrame frame;
    public MainPanel(JFrame f) {
        super(new BorderLayout());
        this.frame = f;
        add(new JLabel("SystemTray.isSupported(): "+SystemTray.isSupported()), BorderLayout.NORTH);
        add(createBox(popup));
        setPreferredSize(new Dimension(320, 200));
        if(!SystemTray.isSupported()) {
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            return;
        }
        frame.addWindowStateListener(new WindowAdapter() {
            @Override public void windowIconified(WindowEvent e) {
                frame.dispose();
            }
        });
        dummy.setUndecorated(true);
        //dummy.setAlwaysOnTop(true);

        icon.addMouseListener(new MouseAdapter() {
            private void showJPopupMenu(MouseEvent e) {
                if(e.isPopupTrigger()) {
                    Point p = adjustPopupLocation(popup, e.getX(), e.getY());
                    dummy.setLocation(p);
                    dummy.setVisible(true);
                    //dummy.toFront();
                    popup.show(dummy, 0, 0);
                }
            }
            @Override public void mouseReleased(MouseEvent e) {
                showJPopupMenu(e);
            }
            @Override public void mousePressed(MouseEvent e) {
                showJPopupMenu(e);
            }
        });

        initPopupMenu();
        try{
            tray.add(icon);
        }catch(AWTException e) {
            e.printStackTrace();
        }
    }

    private void initPopupMenu() {
        // This code is inspired from:
        // http://weblogs.java.net/blog/alexfromsun/archive/2008/02/jtrayicon_updat.html
        // http://java.net/projects/swinghelper/sources/svn/content/trunk/src/java/org/jdesktop/swinghelper/tray/JXTrayIcon.java
        popup.addPopupMenuListener(new PopupMenuListener() {
            @Override public void popupMenuWillBecomeVisible(PopupMenuEvent e) {}
            @Override public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                dummy.setVisible(false);
            }
            @Override public void popupMenuCanceled(PopupMenuEvent e) {
                dummy.setVisible(false);
            }
        });
        popup.add(new JCheckBoxMenuItem("JCheckBoxMenuItem"));
        popup.add(new JRadioButtonMenuItem("JRadioButtonMenuItem"));
        popup.add(new JRadioButtonMenuItem("JRadioButtonMenuItem aaaaaaaaaaa"));
        popup.add(new AbstractAction("OPEN") {
            @Override public void actionPerformed(ActionEvent e) {
                frame.setVisible(true);
            }
        });
        popup.add(new AbstractAction("EXIT") {
            @Override public void actionPerformed(ActionEvent e) {
                frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
                frame.dispose();
                dummy.dispose();
                tray.remove(icon);
                //System.exit(0);
            }
        });
    }

    // Try to find GraphicsConfiguration, that includes mouse pointer position
    private static GraphicsConfiguration getGraphicsConfiguration(Point p) {
        GraphicsConfiguration gc = null;
        for(GraphicsDevice gd: GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()) {
            if(gd.getType() == GraphicsDevice.TYPE_RASTER_SCREEN) {
                GraphicsConfiguration dgc = gd.getDefaultConfiguration();
                if(dgc.getBounds().contains(p)) {
                    gc = dgc;
                    break;
                }
            }
        }
        return gc;
    }

    //Copied from JPopupMenu.java: JPopupMenu#adjustPopupLocationToFitScreen(...)
    private static Point adjustPopupLocation(JPopupMenu popup, int xposition, int yposition) {
        Point p = new Point(xposition, yposition);
        if(GraphicsEnvironment.isHeadless()) {
            return p;
        }

        Rectangle screenBounds;
        GraphicsConfiguration gc = getGraphicsConfiguration(p);

        // If not found and popup have invoker, ask invoker about his gc
        if(gc == null && popup.getInvoker() != null) {
            gc = popup.getInvoker().getGraphicsConfiguration();
        }

        if(gc != null) {
            // If we have GraphicsConfiguration use it to get
            // screen bounds
            screenBounds = gc.getBounds();
        }else{
            // If we don't have GraphicsConfiguration use primary screen
            screenBounds = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
        }

        Dimension size = popup.getPreferredSize();

        // Use long variables to prevent overflow
        long pw = (long) p.x + (long) size.width;
        long ph = (long) p.y + (long) size.height;

        if(pw > screenBounds.x + screenBounds.width)  { p.x -= size.width;  }
        if(ph > screenBounds.y + screenBounds.height) { p.y -= size.height; }

        // Change is made to the desired (X,Y) values, when the
        // PopupMenu is too tall OR too wide for the screen
        if(p.x < screenBounds.x) { p.x = screenBounds.x; }
        if(p.y < screenBounds.y) { p.y = screenBounds.y; }

        return p;
    }
    public static Box createBox(JComponent... list) {
        ButtonGroup group = new ButtonGroup();
        Box box = Box.createVerticalBox();
        for(LookAndFeelEnum lnf : LookAndFeelEnum.values()) {
            JRadioButton rb = new JRadioButton(new ChangeLookAndFeelAction(lnf, list));
            group.add(rb); box.add(rb);
        }
        box.add(Box.createVerticalGlue());
        box.setBorder(BorderFactory.createEmptyBorder(5,25,5,25));
        return box;
    }
    private static class ChangeLookAndFeelAction extends AbstractAction {
        private final String lnf;
        private final JComponent[] list;
        protected ChangeLookAndFeelAction(LookAndFeelEnum lnfe, JComponent... list) {
            super(lnfe.toString());
            this.list = list;
            this.lnf = lnfe.getClassName();
            this.setEnabled(isAvailableLookAndFeel(lnf));
        }
        private static boolean isAvailableLookAndFeel(String lnf) {
            try{
                Class lnfClass = Class.forName(lnf);
                LookAndFeel newLnF = (LookAndFeel)(lnfClass.newInstance());
                return newLnF.isSupportedLookAndFeel();
            }catch(Exception e) {
                return false;
            }
        }
        @Override public void actionPerformed(ActionEvent e) {
            try{
                UIManager.setLookAndFeel(lnf);
            }catch(Exception ex) {
                ex.printStackTrace();
                System.out.println("Failed loading L&F: " + lnf);
            }
            for(Frame f:Frame.getFrames()) {
                SwingUtilities.updateComponentTreeUI(f);
                f.pack();
            }
            for(JComponent c:list) {
                SwingUtilities.updateComponentTreeUI(c);
            }
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
//             for(UIManager.LookAndFeelInfo laf : UIManager.getInstalledLookAndFeels())
//               if("Nimbus".equals(laf.getName()))
//                 UIManager.setLookAndFeel(laf.getClassName());
        }catch(ClassNotFoundException | InstantiationException |
               IllegalAccessException | UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        }
        JFrame frame = new JFrame("@title@");
        //frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        frame.getContentPane().add(new MainPanel(frame));
        frame.setResizable(false);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

enum LookAndFeelEnum {
    Metal  ("javax.swing.plaf.metal.MetalLookAndFeel"),
    Mac    ("com.sun.java.swing.plaf.mac.MacLookAndFeel"),
    Motif  ("com.sun.java.swing.plaf.motif.MotifLookAndFeel"),
    Windows("com.sun.java.swing.plaf.windows.WindowsLookAndFeel"),
    GTK    ("com.sun.java.swing.plaf.gtk.GTKLookAndFeel"),
    Nimbus ("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
    private final String clazz;
    private LookAndFeelEnum(String clazz) {
        this.clazz = clazz;
    }
    public String getClassName() {
        return clazz;
    }
}
