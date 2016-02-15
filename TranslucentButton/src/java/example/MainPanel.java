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
// import javax.swing.plaf.basic.*;

public final class MainPanel extends JPanel {
    private static final TexturePaint TEXTURE = makeCheckerTexture();

    public MainPanel() {
        super();
        //Icon: refer to http://chrfb.deviantart.com/art/quot-ecqlipse-2-quot-PNG-59941546
        URL url = getClass().getResource("RECYCLE BIN - EMPTY_16x16-32.png");
        Icon icon = new ImageIcon(url);

        AbstractButton b = makeButton(makeTitleWithIcon(url, "align=top", "top"));
        add(b);
        b = makeButton(makeTitleWithIcon(url, "align=middle", "middle"));
        add(b);
        b = makeButton(makeTitleWithIcon(url, "align=bottom", "bottom"));
        add(b);

        JLabel label = new JLabel("JLabel", icon, SwingConstants.CENTER);
        label.setForeground(Color.WHITE);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        b = makeButton("");
        b.setAlignmentX(Component.CENTER_ALIGNMENT);
        JPanel p = new JPanel();
        p.setLayout(new OverlayLayout(p));
        p.setOpaque(false);
        p.add(label);
        p.add(b);
        add(p);

        add(makeButton("\u260f text"));

        b = new TranslucentButton("TranslucentButton", icon);
        add(b);

        add(makeButton("a"));
        add(makeButton("bbbbbbbb"));
        add(makeButton("cccccccccccccccccccc"));
        add(makeButton("dddddddddddddddddddddddddddddddd"));

        BufferedImage bi = getFilteredImage(getClass().getResource("test.jpg"));
        setBorder(new CentredBackgroundBorder(bi));
        //setBackground(new Color(50, 50, 50));
        setOpaque(false);
        setPreferredSize(new Dimension(320, 240));
    }
    private static String makeTitleWithIcon(URL url, String title, String align) {
        return String.format("<html><p align='%s'><img src='%s' align='%s' />&nbsp;%s</p></html>", align, url, align, title);
    }
    private static AbstractButton makeButton(String title) {
        return new JButton(title) {
            @Override public void updateUI() {
                super.updateUI();
                setVerticalAlignment(SwingConstants.CENTER);
                setVerticalTextPosition(SwingConstants.CENTER);
                setHorizontalAlignment(SwingConstants.CENTER);
                setHorizontalTextPosition(SwingConstants.CENTER);
                setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));
                setMargin(new Insets(2, 8, 2, 8));
                setBorderPainted(false);
                setContentAreaFilled(false);
                setFocusPainted(false);
                setOpaque(false);
                setForeground(Color.WHITE);
                setIcon(new TranslucentButtonIcon(this));
            }
        };
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

    private static BufferedImage getFilteredImage(URL url) {
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
            b[i] = (byte) (i * .5);
        }
        BufferedImageOp op = new LookupOp(new ByteLookupTable(0, b), null);
        op.filter(image, dest);
        return dest;
    }

    private static TexturePaint makeCheckerTexture() {
        int cs = 6;
        int sz = cs * cs;
        BufferedImage img = new BufferedImage(sz, sz, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setPaint(new Color(120, 120, 120));
        g2.fillRect(0, 0, sz, sz);
        g2.setPaint(new Color(200, 200, 200, 20));
        for (int i = 0; i * cs < sz; i++) {
            for (int j = 0; j * cs < sz; j++) {
                if ((i + j) % 2 == 0) {
                    g2.fillRect(i * cs, j * cs, cs, cs);
                }
            }
        }
        g2.dispose();
        return new TexturePaint(img, new Rectangle(sz, sz));
    }
    @Override protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setPaint(TEXTURE);
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.dispose();
        super.paintComponent(g);
    }
}

class TranslucentButton extends JButton {
    private static final Color TL = new Color(1f, 1f, 1f, .2f);
    private static final Color BR = new Color(0f, 0f, 0f, .4f);
    private static final Color ST = new Color(1f, 1f, 1f, .2f);
    private static final Color SB = new Color(1f, 1f, 1f, .1f);
    private static final int R = 8;
    protected TranslucentButton(String text) {
        super(text);
    }
    protected TranslucentButton(String text, Icon icon) {
        super(text, icon);
    }
    @Override public void updateUI() {
        super.updateUI();
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setOpaque(false);
        setForeground(Color.WHITE);
    }
    @Override protected void paintComponent(Graphics g) {
        int x = 0;
        int y = 0;
        int w = getWidth();
        int h = getHeight();
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Shape area = new RoundRectangle2D.Double(x, y, w - 1, h - 1, R, R);
        Color ssc = TL;
        Color bgc = BR;
        ButtonModel m = getModel();
        if (m.isPressed()) {
            ssc = SB;
            bgc = ST;
        } else if (m.isRollover()) {
            ssc = ST;
            bgc = SB;
        }
        g2.setPaint(new GradientPaint(x, y, ssc, x, y + h, bgc, true));
        g2.fill(area);
        g2.setPaint(BR);
        g2.draw(area);
        g2.dispose();
        super.paintComponent(g);
    }
}

class TranslucentButtonIcon implements Icon {
    private static final Color TL = new Color(1f, 1f, 1f, .2f);
    private static final Color BR = new Color(0f, 0f, 0f, .4f);
    private static final Color ST = new Color(1f, 1f, 1f, .2f);
    private static final Color SB = new Color(1f, 1f, 1f, .1f);
    private static final int R = 8;
    private int width;
    private int height;
    protected TranslucentButtonIcon(JComponent c) {
        Insets i = c.getBorder().getBorderInsets(c);
        Dimension d = c.getPreferredSize();
        width  = d.width - i.left - i.right;
        height = d.height - i.top - i.bottom;
    }
    @Override public void paintIcon(Component c, Graphics g, int x, int y) {
        if (c instanceof AbstractButton) {
            AbstractButton b = (AbstractButton) c;
            //XXX: Insets i = b.getMargin();
            Insets i = b.getBorder().getBorderInsets(b);
            int w = c.getWidth();
            int h = c.getHeight();
            width  = w - i.left - i.right;
            height = h - i.top - i.bottom;
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Shape area = new RoundRectangle2D.Double(x - i.left, y - i.top, w - 1, h - 1, R, R);
            Color ssc = TL;
            Color bgc = BR;
            ButtonModel m = b.getModel();
            if (m.isPressed()) {
                ssc = SB;
                bgc = ST;
            } else if (m.isRollover()) {
                ssc = ST;
                bgc = SB;
            }
            g2.setPaint(new GradientPaint(0, 0, ssc, 0, h, bgc, true));
            g2.fill(area);
            g2.setPaint(BR);
            g2.draw(area);
            g2.dispose();
        }
    }
    @Override public int getIconWidth() {
        return Math.max(width, 100);
    }
    @Override public int getIconHeight() {
        return Math.max(height, 20);
    }
}

// https://community.oracle.com/thread/1395763 How can I use TextArea with Background Picture ?
// http://ateraimemo.com/Swing/CentredBackgroundBorder.html
class CentredBackgroundBorder implements Border {
    private final Insets insets = new Insets(0, 0, 0, 0);
    private final BufferedImage image;
    protected CentredBackgroundBorder(BufferedImage image) {
        this.image = image;
    }
    @Override public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        int cx = x + (width - image.getWidth()) / 2;
        int cy = y + (height - image.getHeight()) / 2;
        Graphics2D g2 = (Graphics2D) g.create();
        g2.drawRenderedImage(image, AffineTransform.getTranslateInstance(cx, cy));
        g2.dispose();
    }
    @Override public Insets getBorderInsets(Component c) {
        return insets;
    }
    @Override public boolean isBorderOpaque() {
        return true;
    }
}
