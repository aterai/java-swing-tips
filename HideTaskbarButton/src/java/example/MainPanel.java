package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.util.Arrays;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private final JCheckBox check = new JCheckBox("Hide the taskbar button when JFrame is minimized");
    private MainPanel(final JFrame frame) {
        super();
        add(check);
        setPreferredSize(new Dimension(320, 240));

        if (!SystemTray.isSupported()) {
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            return;
        }
        frame.addWindowListener(new WindowAdapter() {
            @Override public void windowIconified(WindowEvent e) {
                if (check.isSelected()) {
                    e.getWindow().dispose();
                }
            }
        });
        //or
        //frame.addWindowStateListener(new WindowStateListener() {
        //    @Override public void windowStateChanged(WindowEvent e) {
        //        if (check.isSelected() && e.getNewState() == Frame.ICONIFIED) {
        //            e.getWindow().dispose();
        //        }
        //    }
        //});

        final SystemTray tray = SystemTray.getSystemTray();
        Dimension d = tray.getTrayIconSize();
        BufferedImage image = makeBufferedImage(new StarIcon(), d.width, d.height);
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
                frame.dispose();
                //frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
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
    private static BufferedImage makeBufferedImage(Icon icon, int w, int h) {
        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        icon.paintIcon(null, g, (w - icon.getIconWidth()) / 2, (h - icon.getIconWidth()) / 2);
        g.dispose();
        return image;
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
        frame.setIconImages(Arrays.asList(
            makeBufferedImage(new StarIcon(), 16, 16),
            makeBufferedImage(new StarIcon(16, 8, 5), 40, 40)));
        //frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        //frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.getContentPane().add(new MainPanel(frame));
        frame.setResizable(false);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

class StarIcon implements Icon {
    private final Shape star;
    public StarIcon() {
        star = makeStar(8, 4, 5);
    }
    public StarIcon(int r1, int r2, int vc) {
        star = makeStar(r1, r2, vc);
    }
    private Path2D.Double makeStar(int r1, int r2, int vc) {
        int or = Math.max(r1, r2);
        int ir = Math.min(r1, r2);
        double agl = 0d;
        double add = 2 * Math.PI / (vc * 2);
        Path2D.Double p = new Path2D.Double();
        p.moveTo(or * 1, or * 0);
        for (int i = 0; i < vc * 2 - 1; i++) {
            agl += add;
            int r = i % 2 == 0 ? ir : or;
            p.lineTo(r * Math.cos(agl), r * Math.sin(agl));
        }
        p.closePath();
        AffineTransform at = AffineTransform.getRotateInstance(-Math.PI / 2, or, 0);
        return new Path2D.Double(p, at);
    }
    @Override public int getIconWidth() {
        return star.getBounds().width;
    }
    @Override public int getIconHeight() {
        return star.getBounds().height;
    }
    @Override public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.translate(x, y);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setPaint(Color.ORANGE);
        g2.fill(star);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g2.translate(-x, -y);
        g2.dispose();
    }
}
