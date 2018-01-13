package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());
        add(new PaintPanel());
        setPreferredSize(new Dimension(320, 240));
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
        frame.setResizable(false);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

class PaintPanel extends JPanel implements MouseMotionListener, MouseListener {
    private Point startPoint = new Point();
    private final transient BufferedImage backImage;
    private static final TexturePaint TEXTURE = TextureFactory.createCheckerTexture(6, new Color(200, 150, 100, 50));
    private final Rectangle rect = new Rectangle(320, 240);
    private final int[] pixels = new int[rect.width * rect.height];
    private final transient MemoryImageSource source = new MemoryImageSource(rect.width, rect.height, pixels, 0, rect.width);
    private int penColor;

    protected PaintPanel() {
        super();
        addMouseMotionListener(this);
        addMouseListener(this);
        backImage = new BufferedImage(rect.width, rect.height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = backImage.createGraphics();
        g2.setPaint(TEXTURE);
        g2.fill(rect);
        g2.dispose();
    }
    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.drawImage(backImage, 0, 0, this);
        g2.drawImage(createImage(source), 0, 0, this);
        g2.dispose();
    }
    @SuppressWarnings("LocalVariableName")
    @Override public void mouseDragged(MouseEvent e) {
        double xDelta = e.getX() - startPoint.getX();
        double yDelta = e.getY() - startPoint.getY();
        double delta = Math.max(Math.abs(xDelta), Math.abs(yDelta));

        double xIncrement = xDelta / delta;
        double yIncrement = yDelta / delta;
        double xStart = startPoint.x;
        double yStart = startPoint.y;
        Point pt = new Point();
        for (int i = 0; i < delta; i++) {
            pt.setLocation((int) xStart, (int) yStart);
            // if (pt.x < 0 || pt.y < 0 || pt.x >= rect.width || pt.y >= rect.height) {
            if (!rect.contains(pt)) {
                break;
            }
            paintStamp(pt, penColor);
            // source.newPixels(pt.x - 2, pt.y - 2, 4, 4);
            xStart += xIncrement;
            yStart += yIncrement;
        }
        startPoint = e.getPoint();
    }
    private void paintStamp(Point p, int penc) {
        // 1 x 1:
        // pixels[p.x + p.y * 320] = penc;
        // 3 x 3 square:
        for (int n = -1; n <= 1; n++) {
            for (int m = -1; m <= 1; m++) {
                int t = p.x + n + (p.y + m) * rect.width;
                if (t >= 0 && t < rect.width * rect.height) {
                    pixels[t] = penc;
                }
            }
        }
        repaint(p.x - 2, p.y - 2, 4, 4);
    }
    @Override public void mousePressed(MouseEvent e) {
        startPoint = e.getPoint();
        penColor = e.getButton() == MouseEvent.BUTTON1 ? 0xFF000000 : 0x0;
    }
    @Override public void mouseMoved(MouseEvent e) { /* not needed */ }
    @Override public void mouseExited(MouseEvent e) { /* not needed */ }
    @Override public void mouseEntered(MouseEvent e) { /* not needed */ }
    @Override public void mouseReleased(MouseEvent e) { /* not needed */ }
    @Override public void mouseClicked(MouseEvent e) { /* not needed */ }
}

final class TextureFactory {
    private static final Color DEFAULT_COLOR = new Color(100, 100, 100, 100);
    private TextureFactory() { /* Singleton */ }
    public static TexturePaint createCheckerTexture(int cs, Color color) {
        int size = cs * cs;
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setPaint(color);
        g2.fillRect(0, 0, size, size);
        for (int i = 0; i * cs < size; i++) {
            for (int j = 0; j * cs < size; j++) {
                if ((i + j) % 2 == 0) {
                    g2.fillRect(i * cs, j * cs, cs, cs);
                }
            }
        }
        g2.dispose();
        return new TexturePaint(img, new Rectangle(size, size));
    }
    public static TexturePaint createCheckerTexture(int cs) {
        return createCheckerTexture(cs, DEFAULT_COLOR);
    }
}

// class PaintPanel extends JPanel implements MouseMotionListener, MouseListener {
//     private static final Color ERASER = new Color(0x0, true);
//     private boolean isPen = true;
//     private Point startPoint = new Point();
//     private BufferedImage currentImage = null;
//     private BufferedImage backImage = null;
//     private TexturePaint texture = makeTexturePaint();
//     protected PaintPanel() {
//         super();
//         addMouseMotionListener(this);
//         addMouseListener(this);
//         currentImage = new BufferedImage(320, 240, BufferedImage.TYPE_INT_ARGB);
//         backImage = new BufferedImage(320, 240, BufferedImage.TYPE_INT_ARGB);
//         Graphics2D g2 = backImage.createGraphics();
//         g2.setPaint(texture);
//         g2.fillRect(0, 0, 320, 240);
//         g2.dispose();
//     }
//     private static BufferedImage makeBGImage() {
//         Color color = new Color(200, 150, 100, 50);
//         int cs = 6;
//         int sz = cs * cs;
//         BufferedImage img = new BufferedImage(sz, sz, BufferedImage.TYPE_INT_ARGB);
//         Graphics2D g2 = img.createGraphics();
//         g2.setPaint(color);
//         g2.fillRect(0, 0, sz, sz);
//         for (int i = 0; i * cs < sz; i++) {
//             for (int j = 0; j * cs < sz; j++) {
//                 if ((i + j) % 2 == 0) {
//                     g2.fillRect(i * cs, j * cs, cs, cs);
//                 }
//             }
//         }
//         g2.dispose();
//         return img;
//     }
//     private static TexturePaint makeTexturePaint() {
//         BufferedImage img = makeBGImage();
//         return new TexturePaint(img, new Rectangle(img.getWidth(), img.getHeight()));
//     }
//     @Override protected void paintComponent(Graphics g) {
//         super.paintComponent(g);
//         // if (Objects.nonNull(backImage)) {
//         g.drawImage(backImage, 0, 0, this);
//         // if (Objects.nonNull(currentImage)) {
//         g.drawImage(currentImage, 0, 0, this);
//     }
//     @Override public void mouseDragged(MouseEvent e) {
//         Point pt = e.getPoint();
//         Graphics2D g2 = currentImage.createGraphics();
//         g2.setStroke(new BasicStroke(3f));
//         if (isPen) {
//             g2.setPaint(Color.BLACK);
//         } else {
//             g2.setComposite(AlphaComposite.Clear);
//             g2.setPaint(ERASER);
//         }
//         g2.drawLine(startPoint.x, startPoint.y, pt.x, pt.y);
//         g2.dispose();
//         startPoint = pt;
//         repaint();
//     }
//     @Override public void mousePressed(MouseEvent e) {
//         startPoint = e.getPoint();
//         isPen = e.getButton() == MouseEvent.BUTTON1;
//     }
//     @Override public void mouseMoved(MouseEvent e) {}
//     @Override public void mouseExited(MouseEvent e) {}
//     @Override public void mouseEntered(MouseEvent e) {}
//     @Override public void mouseReleased(MouseEvent e) {}
//     @Override public void mouseClicked(MouseEvent e) {}
// }
