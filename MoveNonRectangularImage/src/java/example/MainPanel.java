package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
    public MainPanel() {
        super((LayoutManager) null);
        add(makeLabelIcon());
        setPreferredSize(new Dimension(320, 240));
    }
    private JLabel makeLabelIcon() {
        ImageIcon i = new ImageIcon(getClass().getResource("duke.gif"));
        Dimension d = new Dimension(i.getIconWidth(), i.getIconHeight());
        final BufferedImage image = new BufferedImage(d.width, d.height, BufferedImage.TYPE_INT_ARGB);
        Graphics g = image.createGraphics();
        i.paintIcon(null, g, 0, 0);
        g.dispose();
        final JLabel icon = new JLabel(i) {
            @Override public boolean contains(int x, int y) {
                return super.contains(x, y) && ((image.getRGB(x, y) >> 24) & 0xff) != 0;
            }
        };
        icon.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
        MouseAdapter l = new MouseAdapter() {
            private final transient Point start = new Point();
            private Point loc;
            @Override public void mousePressed(MouseEvent me) {
                start.setLocation(me.getPoint());
            }
            @Override public void mouseDragged(MouseEvent me) {
                loc = icon.getLocation(loc);
                int x = loc.x - start.x + me.getX();
                int y = loc.y - start.y + me.getY();
                icon.setLocation(x, y);
            }
        };
        icon.addMouseListener(l);
        icon.addMouseMotionListener(l);
        icon.setBounds(new Rectangle(22, 22, d.width, d.height));
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
    }
}
