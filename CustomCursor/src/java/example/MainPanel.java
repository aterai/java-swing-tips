package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.image.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private final JButton label1 = new JButton("?");
    private final JButton label2 = new JButton("Oval");
    private final JButton label3 = new JButton("Rect");
    private final Point centerpt = new Point(16, 16);

    public MainPanel() {
        super(new BorderLayout());
        BufferedImage bi1 = makeStringBufferedImage("?");
        label1.setCursor(getToolkit().createCustomCursor(bi1, centerpt, "?"));

        BufferedImage bi2 = makeOvalBufferedImage();
        label2.setCursor(getToolkit().createCustomCursor(bi2, centerpt, "oval"));
        label2.setIcon(new ImageIcon(bi2));

        Icon icon = new GreenBlueIcon();
        BufferedImage bi3 = makeIconBufferedImage(icon);
        label3.setCursor(getToolkit().createCustomCursor(bi3, centerpt, "rect"));
        label3.setIcon(icon);

        JPanel p = new JPanel(new GridLayout(3, 1, 5, 5));
        p.add(makePanel("String", label1));
        p.add(makePanel("drawOval", label2));
        p.add(makePanel("paintIcon", label3));
        p.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        add(p);
        setPreferredSize(new Dimension(320, 240));
    }
    private static BufferedImage makeStringBufferedImage(String str) {
        BufferedImage bi = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = bi.createGraphics();
        g2.setPaint(Color.BLACK);
        g2.drawString(str, 16, 28);
        g2.dispose();
        return bi;
    }
    private static BufferedImage makeOvalBufferedImage() {
        BufferedImage bi = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = bi.createGraphics();
        g2.setPaint(Color.RED);
        g2.drawOval(8, 8, 16, 16);
        g2.dispose();
        return bi;
    }
    private static BufferedImage makeIconBufferedImage(Icon icon) {
        BufferedImage bi = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = bi.createGraphics();
        icon.paintIcon(null, g2, 0, 0);
        g2.dispose();
        return bi;
    }
    private static JPanel makePanel(String title, JComponent c) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder(title));
        p.add(c);
        return p;
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

class GreenBlueIcon implements Icon {
    @Override public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.translate(x, y);
        g2.setPaint(Color.GREEN);
        g2.fillRect(8, 8, 8, 8);
        g2.setPaint(Color.BLUE);
        g2.fillRect(16, 16, 8, 8);
        g2.dispose();
    }
    @Override public int getIconWidth() {
        return 32;
    }
    @Override public int getIconHeight() {
        return 32;
    }
}
