package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private static final String TEXT = "icon.addMouseListener(new MouseAdapter() {\n"
        + "  public void mouseClicked(MouseEvent e) {\n"
        + "    boolean isDoubleClick = e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() >= 2;\n"
        + "    if (isDoubleClick) {\n"
        + "      frame.setVisible(true);\n"
        + "    } else if (frame.isVisible()) {\n"
        + "      frame.setExtendedState(Frame.NORMAL);\n"
        + "      frame.toFront();\n"
        + "    }\n"
        + "  }\n"
        + "});\n";
    private MainPanel() {
        super(new BorderLayout());
        add(new JScrollPane(new JTextArea(TEXT)));
        setPreferredSize(new Dimension(320, 240));
    }
    private static TrayIcon makeTrayIcon(final JFrame frame) {
        MenuItem open = new MenuItem("Option");
        open.addActionListener(e -> frame.setVisible(true));

        MenuItem exit = new MenuItem("Exit");
        exit.addActionListener(e -> {
            SystemTray tray = SystemTray.getSystemTray();
            for (TrayIcon icon: tray.getTrayIcons()) {
                tray.remove(icon);
            }
            // frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            // frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
            frame.dispose();
        });

        PopupMenu popup = new PopupMenu();
        popup.add(open);
        popup.add(exit);

        Image image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Graphics g = image.getGraphics();
        new StarIcon().paintIcon(null, g, 0, 0);
        g.dispose();

        TrayIcon icon = new TrayIcon(image, "Click Test", popup);
        icon.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                boolean isDoubleClick = e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() >= 2;
                if (isDoubleClick) {
                    frame.setVisible(true);
                } else if (frame.isVisible()) {
                    frame.setExtendedState(Frame.NORMAL);
                    frame.toFront();
                }
            }
        });

        return icon;
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
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        if (SystemTray.isSupported()) {
            frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
            try {
                SystemTray.getSystemTray().add(makeTrayIcon(frame));
            } catch (AWTException ex) {
                ex.printStackTrace();
            }
        }
    }
}

class StarIcon implements Icon {
    private final Shape star = makeStar(8, 4, 5);
    private Path2D makeStar(int r1, int r2, int vc) {
        int or = Math.max(r1, r2);
        int ir = Math.min(r1, r2);
        double agl = 0d;
        double add = 2 * Math.PI / (vc * 2);
        Path2D p = new Path2D.Double();
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
    @Override public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.translate(x, y);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setPaint(Color.PINK);
        g2.fill(star);
        g2.dispose();
    }
    @Override public int getIconWidth() {
        return star.getBounds().width;
    }
    @Override public int getIconHeight() {
        return star.getBounds().height;
    }
}
