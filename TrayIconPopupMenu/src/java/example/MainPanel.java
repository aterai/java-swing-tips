package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

public final class MainPanel extends JPanel {
    private final JPopupMenu popup = new JPopupMenu();

    private MainPanel() {
        super(new BorderLayout());
        add(new JLabel("SystemTray.isSupported(): " + SystemTray.isSupported()), BorderLayout.NORTH);

        ButtonGroup group = new ButtonGroup();
        Box box = Box.createVerticalBox();
        Stream.of(LookAndFeelEnum.values())
            .map(lnf -> new ChangeLookAndFeelAction(lnf, Arrays.asList(popup)))
            .map(JRadioButton::new)
            .forEach(rb -> {
                group.add(rb);
                box.add(rb);
            });
        // for (LookAndFeelEnum lnf: LookAndFeelEnum.values()) {
        //     JRadioButton rb = new JRadioButton(new ChangeLookAndFeelAction(lnf, Arrays.asList(popup)));
        //     group.add(rb);
        //     box.add(rb);
        // }
        box.add(Box.createVerticalGlue());
        box.setBorder(BorderFactory.createEmptyBorder(5, 25, 5, 25));

        add(box);
        setPreferredSize(new Dimension(320, 240));

        EventQueue.invokeLater(() -> {
            Container c = getTopLevelAncestor();
            if (c instanceof JFrame) {
                initPopupMenu((JFrame) c);
            }
        });
    }

    private void initPopupMenu(JFrame frame) {
        // This code is inspired from:
        // http://weblogs.java.net/blog/alexfromsun/archive/2008/02/jtrayicon_updat.html
        // http://java.net/projects/swinghelper/sources/svn/content/trunk/src/java/org/jdesktop/swinghelper/tray/JXTrayIcon.java

        // JWindow dummy = new JWindow(); // Ubuntu?
        JDialog dummy = new JDialog();
        dummy.setUndecorated(true);
        // dummy.setAlwaysOnTop(true);

        Image image = new ImageIcon(getClass().getResource("16x16.png")).getImage();
        TrayIcon icon = new TrayIcon(image, "TRAY", null);
        icon.addMouseListener(new TrayIconPopupMenuHandler(popup, dummy));
        try {
            SystemTray.getSystemTray().add(icon);
        } catch (AWTException ex) {
            ex.printStackTrace();
        }

        // init JPopupMenu
        popup.addPopupMenuListener(new PopupMenuListener() {
            @Override public void popupMenuWillBecomeVisible(PopupMenuEvent e) { /* not needed */ }
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
        popup.add("Open").addActionListener(e -> {
            frame.setExtendedState(Frame.NORMAL);
            frame.setVisible(true);
        });
        popup.add("Exit").addActionListener(e -> {
            SystemTray tray = SystemTray.getSystemTray();
            for (TrayIcon ti: tray.getTrayIcons()) {
                tray.remove(ti);
            }
            for (Frame f: Frame.getFrames()) {
                f.dispose();
            }
            // tray.remove(icon);
            // frame.dispose();
            // dummy.dispose();
        });
    }

    public static void main(String... args) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                createAndShowGui();
            }
        });
    }
    public static void createAndShowGui() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException
               | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        }
        JFrame frame = new JFrame("@title@");
        if (SystemTray.isSupported()) {
            frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
            frame.addWindowStateListener(e -> {
                if (e.getNewState() == Frame.ICONIFIED) {
                    e.getWindow().dispose();
                }
            });
        } else {
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        }
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

final class TrayIconPopupMenuUtil {
    private TrayIconPopupMenuUtil() { /* Singleton */ }

    // Try to find GraphicsConfiguration, that includes mouse pointer position
    private static GraphicsConfiguration getGraphicsConfiguration(Point p) {
        GraphicsConfiguration gc = null;
        for (GraphicsDevice gd: GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()) {
            if (gd.getType() == GraphicsDevice.TYPE_RASTER_SCREEN) {
                GraphicsConfiguration dgc = gd.getDefaultConfiguration();
                if (dgc.getBounds().contains(p)) {
                    gc = dgc;
                    break;
                }
            }
        }
        return gc;
    }

    // Copied from JPopupMenu.java: JPopupMenu#adjustPopupLocationToFitScreen(...)
    public static Point adjustPopupLocation(JPopupMenu popup, int xposition, int yposition) {
        Point p = new Point(xposition, yposition);
        if (GraphicsEnvironment.isHeadless()) {
            return p;
        }

        Rectangle screenBounds;
        GraphicsConfiguration gc = getGraphicsConfiguration(p);

        // If not found and popup have invoker, ask invoker about his gc
        if (Objects.isNull(gc) && Objects.nonNull(popup.getInvoker())) {
            gc = popup.getInvoker().getGraphicsConfiguration();
        }

        if (Objects.isNull(gc)) {
            // If we don't have GraphicsConfiguration use primary screen
            screenBounds = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
        } else {
            // If we have GraphicsConfiguration use it to get
            // screen bounds
            screenBounds = gc.getBounds();
        }

        Dimension size = popup.getPreferredSize();

        // Use long variables to prevent overflow
        long pw = (long) p.x + (long) size.width;
        long ph = (long) p.y + (long) size.height;

        if (pw > screenBounds.x + screenBounds.width) {
            p.x -= size.width;
        }
        if (ph > screenBounds.y + screenBounds.height) {
            p.y -= size.height;
        }

        // Change is made to the desired (X, Y) values, when the
        // PopupMenu is too tall OR too wide for the screen
        p.x = Math.max(p.x, screenBounds.x);
        p.y = Math.max(p.y, screenBounds.y);
        return p;
    }
}

class TrayIconPopupMenuHandler extends MouseAdapter {
    private final JPopupMenu popup;
    private final Window dummy;
    protected TrayIconPopupMenuHandler(JPopupMenu popup, Window dummy) {
        super();
        this.popup = popup;
        this.dummy = dummy;
    }
    private void showJPopupMenu(MouseEvent e) {
        if (e.isPopupTrigger()) {
            Point p = TrayIconPopupMenuUtil.adjustPopupLocation(popup, e.getX(), e.getY());
            dummy.setLocation(p);
            dummy.setVisible(true);
            // dummy.toFront();
            popup.show(dummy, 0, 0);
        }
    }
    @Override public void mouseReleased(MouseEvent e) {
        showJPopupMenu(e);
    }
    @Override public void mousePressed(MouseEvent e) {
        showJPopupMenu(e);
    }
}

enum LookAndFeelEnum {
    METAL("javax.swing.plaf.metal.MetalLookAndFeel"),
    MAC("com.sun.java.swing.plaf.mac.MacLookAndFeel"),
    MOTIF("com.sun.java.swing.plaf.motif.MotifLookAndFeel"),
    WINDOWS("com.sun.java.swing.plaf.windows.WindowsLookAndFeel"),
    GTK("com.sun.java.swing.plaf.gtk.GTKLookAndFeel"),
    NIMBUS("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
    private final String clazz;
    LookAndFeelEnum(String clazz) {
        this.clazz = clazz;
    }
    public String getClassName() {
        return clazz;
    }
}

class ChangeLookAndFeelAction extends AbstractAction {
    private final String lnf;
    private final List<? extends Component> list;
    protected ChangeLookAndFeelAction(LookAndFeelEnum lnfe, List<? extends Component> list) {
        super(lnfe.toString());
        this.list = list;
        this.lnf = lnfe.getClassName();
        this.setEnabled(isAvailableLookAndFeel(lnf));
    }
    private static boolean isAvailableLookAndFeel(String laf) {
        try {
            Class<?> lnfClass = Class.forName(laf);
            LookAndFeel newLnF = (LookAndFeel) lnfClass.getConstructor().newInstance();
            return newLnF.isSupportedLookAndFeel();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException ex) {
            return false;
        }
    }
    @Override public void actionPerformed(ActionEvent e) {
        try {
            UIManager.setLookAndFeel(lnf);
        } catch (ClassNotFoundException | InstantiationException
               | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
            System.out.println("Failed loading L&F: " + lnf);
        }
        for (Frame f: Frame.getFrames()) {
            SwingUtilities.updateComponentTreeUI(f);
            f.pack();
        }
        list.forEach(SwingUtilities::updateComponentTreeUI);
    }
}
