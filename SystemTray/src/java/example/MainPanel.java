package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private MainPanel(final JFrame frame) {
        super();
        setPreferredSize(new Dimension(320, 240));
        if (!SystemTray.isSupported()) {
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            return;
        }
        frame.addWindowStateListener(new WindowAdapter() {
            @Override public void windowIconified(WindowEvent e) {
                frame.dispose();
            }
        });
        final SystemTray tray = SystemTray.getSystemTray();
//         final Dimension d = tray.getTrayIconSize();
//         BufferedImage image = new BufferedImage(d.width, d.height, BufferedImage.TYPE_INT_ARGB);
//         ImageIcon i = new ImageIcon(getClass().getResource("16x16.png"));
//         Graphics2D g = image.createGraphics();
//         g.setBackground(new Color(0, 0, 0, 0));
//         g.clearRect(0, 0, d.width, d.height);
//         i.paintIcon(null, g, (d.width - i.getIconWidth()) / 2, (d.height - i.getIconWidth()) / 2);
//         g.dispose();
        final Image image     = new ImageIcon(getClass().getResource("16x16.png")).getImage();
        final PopupMenu popup = new PopupMenu();
        final TrayIcon icon   = new TrayIcon(image, "TRAY", popup);

        MenuItem item1 = new MenuItem("OPEN");
        item1.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                frame.setVisible(true);
            }
        });
        MenuItem item2 = new MenuItem("EXIT");
        item2.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                tray.remove(icon);
                frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
                frame.dispose();
                //System.exit(0);
            }
        });
        popup.add(item1);
        popup.add(item2);

        try {
            tray.add(icon);
        } catch (AWTException e) {
            e.printStackTrace();
        }
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
        frame.getContentPane().add(new MainPanel(frame));
        frame.setResizable(false);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
