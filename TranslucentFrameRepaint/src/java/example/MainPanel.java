package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;
import javax.imageio.*;
import javax.swing.*;
import javax.swing.Timer;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());

        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        JLabel label = new JLabel(df.format(new Date()), SwingConstants.CENTER);
        Timer timer = new Timer(1000, e -> {
            label.setText(df.format(new Date()));
            Container parent = SwingUtilities.getUnwrappedParent(label);
            if (Objects.nonNull(parent) && parent.isOpaque()) {
                repaintWindowAncestor(label);
            }
        });
        TexturePanel tp = TextureUtil.makeTexturePanel(label, getClass().getResource("YournameS7ScientificHalf.ttf"));

        JFrame digitalClock = new JFrame();
        digitalClock.setUndecorated(true);
        // digitalClock.setAlwaysOnTop(true);
        // AWTUtilities.setWindowOpaque(digitalClock, false); // JDK 1.6.0
        digitalClock.setBackground(new Color(0x0, true)); // JDK 1.7.0
        digitalClock.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        digitalClock.getContentPane().add(tp);
        digitalClock.pack();
        digitalClock.setLocationRelativeTo(null);

        JComboBox<Enum> combo = new JComboBox<Enum>(TexturePaints.values()) {
            @Override public Dimension getPreferredSize() {
                Dimension d = super.getPreferredSize();
                d.width = Math.max(150, d.width);
                return d;
            }
        };
        // XXX: combo.setPrototypeDisplayValue(String.join("", Collections.nCopies(16, "M")));
        // combo.setPrototypeDisplayValue(TexturePaints.Checker);
        combo.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                TexturePaints t = (TexturePaints) e.getItem();
                tp.setTexturePaint(t.getTexturePaint());
                repaintWindowAncestor(tp);
            }
        });

        JToggleButton button = new JToggleButton("timer");
        button.addActionListener(e -> {
            if (((AbstractButton) e.getSource()).isSelected()) {
                TexturePaints t = (TexturePaints) combo.getSelectedItem();
                tp.setTexturePaint(t.getTexturePaint());
                timer.start();
                digitalClock.setVisible(true);
            } else {
                timer.stop();
                digitalClock.setVisible(false);
            }
        });
        JPanel p = new JPanel();
        p.add(combo);
        p.add(button);
        add(p, BorderLayout.NORTH);
        add(new JScrollPane(new JTree()));
        setPreferredSize(new Dimension(320, 240));
    }
    protected static void repaintWindowAncestor(JComponent c) {
        Optional.ofNullable(c.getRootPane())
            .ifPresent(rp -> rp.repaint(SwingUtilities.convertRectangle(c, c.getBounds(), rp)));

    }
//     protected void repaintWindowAncestor(Component c) {
//         Window w = SwingUtilities.getWindowAncestor(c);
//         if (w instanceof JFrame) {
//             JFrame f = (JFrame) w;
//             JComponent cp = (JComponent) f.getContentPane();
//             // cp.repaint();
//             Rectangle r = c.getBounds();
//             r = SwingUtilities.convertRectangle(c, r, cp);
//             cp.repaint(r);
//             // r = SwingUtilities.convertRectangle(c, r, f);
//             // f.repaint(r.x, r.y, r.width, r.height);
//         } else {
//             c.repaint();
//         }
//     }
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

class TexturePanel extends JPanel {
    protected transient Paint texture;
    protected TexturePanel() {
        super();
    }
    protected TexturePanel(LayoutManager lm) {
        super(lm);
    }
    public void setTexturePaint(Paint texturePaint) {
        this.texture = texturePaint;
        // setOpaque(false);
        setOpaque(Objects.isNull(texturePaint));
    }
    @Override protected void paintComponent(Graphics g) {
        if (Objects.nonNull(texture)) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setPaint(texture);
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.dispose();
        }
        super.paintComponent(g);
    }
}

enum TexturePaints {
    Null("Color(.5f, .8f, .5f, .5f)"),
    Image("Image TexturePaint"),
    Checker("Checker TexturePaint");
    private final String description;
    TexturePaints(String description) {
        this.description = description;
    }
    public Paint getTexturePaint() {
        switch (this) {
            case Image: return TextureUtil.makeImageTexture();
            case Checker: return TextureUtil.makeCheckerTexture();
            case Null: return null;
            default: throw new AssertionError();
        }
    }
    @Override public String toString() {
        return description;
    }
}

final class TextureUtil {
    private TextureUtil() { /* Singleton */ }
    public static TexturePaint makeImageTexture() {
        BufferedImage bi = null;
        try {
            // http://www.viva-edo.com/komon/edokomon.html
            bi = ImageIO.read(TextureUtil.class.getResource("unkaku_w.png"));
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new IllegalArgumentException(ex);
        }
        return new TexturePaint(bi, new Rectangle(bi.getWidth(), bi.getHeight()));
    }

    public static TexturePaint makeCheckerTexture() {
        int cs = 6;
        int sz = cs * cs;
        BufferedImage bi = new BufferedImage(sz, sz, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = bi.createGraphics();
        g2.setPaint(new Color(200, 150, 100, 50));
        g2.fillRect(0, 0, sz, sz);
        for (int i = 0; i * cs < sz; i++) {
            for (int j = 0; j * cs < sz; j++) {
                if ((i + j) % 2 == 0) {
                    g2.fillRect(i * cs, j * cs, cs, cs);
                }
            }
        }
        g2.dispose();
        return new TexturePaint(bi, new Rectangle(sz, sz));
    }

    public static TexturePanel makeTexturePanel(JLabel label, URL url) {
        // http://www.yourname.jp/soft/digitalfonts-20090306.shtml
        // Digital display font: Copyright (c) Yourname, Inc.
        Font font = makeFont(url);
        label.setFont(font.deriveFont(80f));
        label.setBackground(new Color(0x0, true));
        label.setOpaque(false);
        TexturePanel p = new TexturePanel(new BorderLayout(8, 8));
        p.add(label);
        p.add(new JLabel("Digital display fonts by Yourname, Inc."), BorderLayout.NORTH);
        p.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        p.setBackground(new Color(.5f, .8f, .5f, .5f));
        DragWindowListener dwl = new DragWindowListener();
        p.addMouseListener(dwl);
        p.addMouseMotionListener(dwl);
        return p;
    }

    private static Font makeFont(URL url) {
        Font font = null;
        try (InputStream is = url.openStream()) {
            font = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(12f);
        } catch (IOException | FontFormatException ex) {
            ex.printStackTrace();
        }
        return font;
    }
}

class DragWindowListener extends MouseAdapter {
    private final Point startPt = new Point();
    @Override public void mousePressed(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)) {
            startPt.setLocation(e.getPoint());
        }
    }
    @Override public void mouseDragged(MouseEvent e) {
        Component c = SwingUtilities.getRoot(e.getComponent());
        if (c instanceof Window && SwingUtilities.isLeftMouseButton(e)) {
            Window window = (Window) c;
            Point pt = window.getLocation();
            window.setLocation(pt.x - startPt.x + e.getX(), pt.y - startPt.y + e.getY());
        }
    }
}
