package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.io.*;
import java.net.*;
import javax.imageio.*;
import javax.swing.*;
import javax.swing.border.*;

public final class MainPanel extends JPanel {
    public MainPanel() {
        super(new BorderLayout());
        JTextArea area = new JTextArea();
        //area.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 32));
        area.setForeground(Color.WHITE);
        area.setBackground(new Color(0x0, true)); //Nimbus
        area.setLineWrap(true);
        area.setOpaque(false);
        area.setText("public static void createAndShowGUI() {\n"
                   + "  final JFrame frame = new JFrame();\n"
                   + "  frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);\n"
                   + "  frame.getContentPane().add(new MainPanel());\n"
                   + "  frame.pack();\n"
                   + "  frame.setLocationRelativeTo(null);\n"
                   + "  frame.setVisible(true);\n"
                   + "}\n");
        URL url = getClass().getResource("tokeidai.jpg");
        BufferedImage bi = getFilteredImage(url);
        JScrollPane scroll = new JScrollPane(area);
        scroll.getViewport().setOpaque(false);
        scroll.setViewportBorder(new CentredBackgroundBorder(bi));
        scroll.getVerticalScrollBar().setUnitIncrement(25);
        add(scroll);
        setPreferredSize(new Dimension(320, 240));
    }

    private BufferedImage getFilteredImage(URL url) {
        BufferedImage image;
        try {
            image = ImageIO.read(url);
        } catch (IOException ioe) {
            ioe.printStackTrace();
            return new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        }
        BufferedImage dest = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
        byte[] b = new byte[256];
        for (int i = 0; i < b.length; i++) {
            b[i] = (byte) (i * .2f);
        }
        BufferedImageOp op = new LookupOp(new ByteLookupTable(0, b), null);
        op.filter(image, dest);
        return dest;
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

// https://community.oracle.com/thread/1395763 How can I use TextArea with Background Picture ?
class CentredBackgroundBorder implements Border {
    private final BufferedImage image;
    protected CentredBackgroundBorder(BufferedImage image) {
        this.image = image;
    }
    @Override public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        int cx = (width  - image.getWidth())  / 2;
        int cy = (height - image.getHeight()) / 2;
        Graphics2D g2 = (Graphics2D) g.create();
        g2.translate(x, y);
        g2.drawRenderedImage(image, AffineTransform.getTranslateInstance(cx, cy));
        g2.dispose();
    }
    @Override public Insets getBorderInsets(Component c) {
        return new Insets(0, 0, 0, 0);
    }
    @Override public boolean isBorderOpaque() {
        return true;
    }
}
