package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.swing.*;

public class MainPanel extends JPanel{
    private final JButton label1 = new JButton("?");
    private final JButton label2 = new JButton("Oval");
    private final JButton label3 = new JButton("Rect");
    private final Point centerpt = new Point(16, 16);
    public MainPanel() {
        super(new BorderLayout());
        BufferedImage bi1 = new BufferedImage(32,32,BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D g2d1 = bi1.createGraphics();
        g2d1.setPaint(Color.BLACK);
        g2d1.drawString("?",16,28);
        g2d1.dispose();
        label1.setCursor(getToolkit().createCustomCursor(bi1, centerpt, "?"));

        BufferedImage bi2 = new BufferedImage(32,32,BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D g2d2 = bi2.createGraphics();
        g2d2.setPaint(Color.RED);
        g2d2.drawOval(8,8,16,16);
        g2d2.dispose();
        label2.setCursor(getToolkit().createCustomCursor(bi2, centerpt, "oval"));
        label2.setIcon(new ImageIcon(bi2));

        Icon icon = new GreenBlueIcon();
        BufferedImage bi3 = new BufferedImage(icon.getIconWidth(),icon.getIconHeight(),
                                              BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D g2d3 = bi3.createGraphics();
        icon.paintIcon(null, g2d3, 0, 0);
        g2d3.dispose();
        label3.setCursor(getToolkit().createCustomCursor(bi3, centerpt, "rect"));
        label3.setIcon(icon);

        JPanel p = new JPanel(new GridLayout(3,1,5,5));
        p.add(makePanel("String", label1));
        p.add(makePanel("drawOval", label2));
        p.add(makePanel("paintIcon", label3));
        p.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        add(p);
        setPreferredSize(new Dimension(320, 180));
    }
    private static JPanel makePanel(String title, JComponent c) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder(title));
        p.add(c);
        return p;
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
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
class GreenBlueIcon implements Icon {
    @Override public int getIconWidth()  { return 32; }
    @Override public int getIconHeight() { return 32; }
    @Override public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2d = (Graphics2D)g;
        g2d.translate(x, y);
        g2d.setPaint(Color.GREEN);
        g2d.fillRect(8,8,8,8);
        g2d.setPaint(Color.BLUE);
        g2d.fillRect(16,16,8,8);
        g2d.translate(-x, -y);
    }
}
