package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.swing.*;

public class MainPanel extends JPanel{
    private final JPanel panel;
    private BufferedImage offImage = null;
    private Point startPoint;
    public MainPanel() {
        super(new BorderLayout());
        panel = new JPanel() {
            @Override public void paintComponent(Graphics g) {
                super.paintComponent(g);
                if(offImage!=null) ((Graphics2D)g).drawImage(offImage, 0, 0, this);
            }
        };
        panel.setBorder(BorderFactory.createEmptyBorder());
        panel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override public void mouseDragged(MouseEvent e) {
                Point p = e.getPoint();
                if(offImage==null) {
                    offImage = (BufferedImage)createImage(getWidth(), getHeight());
                }
                Graphics2D g2d = (Graphics2D)offImage.createGraphics();
            //    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setStroke(new BasicStroke(3.0F));
                g2d.setPaint(Color.BLACK);
                g2d.drawLine(startPoint.x, startPoint.y, p.x, p.y);
                g2d.dispose();
                repaint();
                startPoint = e.getPoint();
            }
        });
        panel.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                startPoint = e.getPoint();
            }
        });

        add(panel);
        setPreferredSize(new Dimension(320, 240));
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
        frame.setResizable(false);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
