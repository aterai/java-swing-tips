package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

class MainPanel extends JPanel {
    private final JDialog dialog = new JDialog();
    private final Image[] imglist = new Image[4];
    public MainPanel(final JFrame frame) {
        super();
        setPreferredSize(new Dimension(320, 240));

        imglist[0] = imglist[2] = new ImageIcon(getClass().getResource("16x16.png")).getImage();
        imglist[1] = new ImageIcon(getClass().getResource("16x16l.png")).getImage();
        imglist[3] = new ImageIcon(getClass().getResource("16x16r.png")).getImage();

        dialog.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        dialog.setSize(new Dimension(100, 100));
        dialog.setLocationRelativeTo(null);
        dialog.setTitle("xxxxxxxxx");

        if(!SystemTray.isSupported()) {
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            return;
        }
        final SystemTray tray = SystemTray.getSystemTray();
        final PopupMenu popup = new PopupMenu();
        //Image img = new ImageIcon(getClass().getResource("anime.gif")).getImage();
        //final TrayIcon icon   = new TrayIcon(img, "TRAY", popup);
        final TrayIcon icon   = new TrayIcon(imglist[0], "TRAY", popup);
        final Timer animator = new Timer(100, new ActionListener() {
            private int idx = 0;
            @Override public void actionPerformed(ActionEvent e) {
                icon.setImage(imglist[idx]);
                idx = idx<imglist.length-1 ? idx+1 : 0;
            }
        });

        MenuItem item1 = new MenuItem("Open:Frame");
        item1.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                frame.setVisible(true);
            }
        });
        MenuItem item2 = new MenuItem("Open:Dialog");
        item2.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                dialog.setVisible(true);
            }
        });

        MenuItem item3 = new MenuItem("Animation:Start");
        item3.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                animator.start();
            }
        });
        MenuItem item4 = new MenuItem("Animation:Stop");
        item4.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                animator.stop();
                icon.setImage(imglist[0]);
            }
        });

        MenuItem item5 = new MenuItem("Exit");
        item5.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                tray.remove(icon);
                animator.stop();
                dialog.dispose();
                //frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
                frame.dispose();
            }
        });
        popup.add(item1);
        popup.add(item2);
        popup.addSeparator();
        popup.add(item3);
        popup.add(item4);
        popup.addSeparator();
        popup.add(item5);

        try{
            tray.add(icon);
        }catch(AWTException e) {
            e.printStackTrace();
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
        //frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        frame.getContentPane().add(new MainPanel(frame));
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
