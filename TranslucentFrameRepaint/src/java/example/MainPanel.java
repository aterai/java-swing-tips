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
    private final JComboBox<? extends Enum> combo = new JComboBox<>(TexturePaints.values());
    private final SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
    private final JLabel label = new JLabel(df.format(new Date()), SwingConstants.CENTER);
    private final Timer timer = new Timer(1000, new ActionListener() {
        @Override public void actionPerformed(ActionEvent e) {
            label.setText(df.format(new Date()));
            Container parent = SwingUtilities.getUnwrappedParent(label);
            if (Objects.nonNull(parent) && parent.isOpaque()) {
                repaintWindowAncestor(label);
            }
        }
    });
    private final TexturePanel tp;

    private void repaintWindowAncestor(JComponent c) {
        JRootPane root = c.getRootPane();
        if (Objects.isNull(root)) {
            return;
        }
        Rectangle r = SwingUtilities.convertRectangle(c, c.getBounds(), root);
        root.repaint(r.x, r.y, r.width, r.height);
    }
//     private void repaintWindowAncestor(Component c) {
//         Window w = SwingUtilities.getWindowAncestor(c);
//         if (w instanceof JFrame) {
//             JFrame f = (JFrame) w;
//             JComponent cp = (JComponent) f.getContentPane();
//             //cp.repaint();
//             Rectangle r = c.getBounds();
//             r = SwingUtilities.convertRectangle(c, r, cp);
//             cp.repaint(r.x, r.y, r.width, r.height);
//             //r = SwingUtilities.convertRectangle(c, r, f);
//             //f.repaint(r.x, r.y, r.width, r.height);
//         } else {
//             c.repaint();
//         }
//     }

    public MainPanel() {
        super(new BorderLayout());
        tp = TextureUtil.makeTexturePanel(label, getClass().getResource("YournameS7ScientificHalf.ttf"));
        combo.addItemListener(new ItemListener() {
            @Override public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    TexturePaints t = (TexturePaints) e.getItem();
                    tp.setTexturePaint(t.getTexturePaint());
                    repaintWindowAncestor(tp);
                }
            }
        });
        JToggleButton button = new JToggleButton(new AbstractAction("timer") {
            private JFrame digitalClock;
            @Override public void actionPerformed(ActionEvent e) {
                if (Objects.isNull(digitalClock)) {
                    digitalClock = new JFrame();
                    digitalClock.setUndecorated(true);
                    //digitalClock.setAlwaysOnTop(true);
                    //com.sun.awt.AWTUtilities.setWindowOpaque(digitalClock, false); //JDK 1.6.0
                    digitalClock.setBackground(new Color(0x0, true)); //JDK 1.7.0
                    digitalClock.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
                    digitalClock.getContentPane().add(tp);
                    digitalClock.pack();
                    digitalClock.setLocationRelativeTo(null);
                }
                if (((AbstractButton) e.getSource()).isSelected()) {
                    TexturePaints t = (TexturePaints) combo.getSelectedItem();
                    tp.setTexturePaint(t.getTexturePaint());
                    timer.start();
                    digitalClock.setVisible(true);
                } else {
                    timer.stop();
                    digitalClock.setVisible(false);
                }
            }
        });
        JPanel p = new JPanel();
        p.add(combo);
        p.add(button);
        add(p, BorderLayout.NORTH);
        add(new JScrollPane(new JTree()));
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
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

class TexturePanel extends JPanel {
    protected transient TexturePaint texture;
    public TexturePanel() {
        super();
    }
    public TexturePanel(LayoutManager lm) {
        super(lm);
    }
    public void setTexturePaint(TexturePaint texture) {
        this.texture = texture;
        //setOpaque(false);
        setOpaque(Objects.isNull(texture));
    }
    @Override public void paintComponent(Graphics g) {
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
    Null    (null, "Color(.5f, .8f, .5f, .5f)"),
    Image   (TextureUtil.makeImageTexture(), "Image TexturePaint"),
    Checker (TextureUtil.makeCheckerTexture(), "Checker TexturePaint");
    private final String description;
    private final TexturePaint texture;
    private TexturePaints(TexturePaint texture, String description) {
        this.texture = texture;
        this.description = description;
    }
    @Override public String toString() {
        return description;
    }
    public TexturePaint getTexturePaint() {
        return this.texture;
    }
}

final class TextureUtil {
    private TextureUtil() { /* Singleton */ }
    public static TexturePaint makeImageTexture() {
        BufferedImage bi = null;
        try {
            //http://www.viva-edo.com/komon/edokomon.html
            bi = ImageIO.read(TextureUtil.class.getResource("unkaku_w.png"));
        } catch (IOException ioe) {
            ioe.printStackTrace();
            throw new IllegalArgumentException(ioe);
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
        return new TexturePaint(bi, new Rectangle(0, 0, sz, sz));
    }

    public static TexturePanel makeTexturePanel(JLabel label, URL url) {
        //http://www.yourname.jp/soft/digitalfonts-20090306.shtml
        //Digital display font: Copyright (c) Yourname, Inc.
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
    @Override public void mousePressed(MouseEvent me) {
        startPt.setLocation(me.getPoint());
    }
    @Override public void mouseDragged(MouseEvent me) {
        Component c = SwingUtilities.getRoot(me.getComponent());
        if (c instanceof Window) {
            Point eventLocationOnScreen = me.getLocationOnScreen();
            ((Window) c).setLocation(eventLocationOnScreen.x - startPt.x,
                                     eventLocationOnScreen.y - startPt.y);
        }
    }
}
