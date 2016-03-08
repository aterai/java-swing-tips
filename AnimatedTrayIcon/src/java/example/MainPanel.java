package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private final JDialog dialog = new JDialog();
    private final Timer animator;
    private final transient Image[] imglist = new Image[4];
    private final transient TrayIcon icon;

    public MainPanel() {
        super();
        setPreferredSize(new Dimension(320, 240));

        if (!SystemTray.isSupported()) {
            throw new UnsupportedOperationException("SystemTray is not supported");
        }

        imglist[0] = new ImageIcon(getClass().getResource("16x16.png")).getImage();
        imglist[1] = new ImageIcon(getClass().getResource("16x16l.png")).getImage();
        imglist[2] = imglist[0];
        imglist[3] = new ImageIcon(getClass().getResource("16x16r.png")).getImage();

        dialog.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        dialog.setSize(new Dimension(120, 100));
        dialog.setLocationRelativeTo(null);
        dialog.setTitle("TEST: JDialog");

        //TEST: icon = new TrayIcon(new ImageIcon(getClass().getResource("anime.gif")).getImage(), "TRAY", popup);
        icon = new TrayIcon(imglist[0], "TRAY", makeTrayPopupMenu());
        animator = new Timer(100, new ActionListener() {
            private int idx;
            @Override public void actionPerformed(ActionEvent e) {
                icon.setImage(imglist[idx]);
                idx = (idx + 1) % imglist.length;
            }
        });
        try {
            SystemTray.getSystemTray().add(icon);
        } catch (AWTException ex) {
            ex.printStackTrace();
        }
    }
    private PopupMenu makeTrayPopupMenu() {
        MenuItem item1 = new MenuItem("Open:Frame");
        item1.addActionListener(e -> {
            Container c = getTopLevelAncestor();
            if (c instanceof Window) {
                ((Window) c).setVisible(true);
            }
        });

        MenuItem item2 = new MenuItem("Open:Dialog");
        item2.addActionListener(e -> dialog.setVisible(true));

        MenuItem item3 = new MenuItem("Animation:Start");
        item3.addActionListener(e -> animator.start());

        MenuItem item4 = new MenuItem("Animation:Stop");
        item4.addActionListener(e -> {
            animator.stop();
            icon.setImage(imglist[0]);
        });

        MenuItem item5 = new MenuItem("Exit");
        item5.addActionListener(e -> {
            animator.stop();
            SystemTray tray = SystemTray.getSystemTray();
            for (TrayIcon icon: tray.getTrayIcons()) {
                tray.remove(icon);
            }
            for (Frame frame: Frame.getFrames()) {
                frame.dispose();
            }
        });

        PopupMenu popup = new PopupMenu();
        popup.add(item1);
        popup.add(item2);
        popup.addSeparator();
        popup.add(item3);
        popup.add(item4);
        popup.addSeparator();
        popup.add(item5);

        return popup;
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
        //frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
