package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.io.*;
import java.net.*;
import java.util.Objects;
import java.util.stream.IntStream;
import javax.imageio.*;
import javax.swing.*;
import javax.swing.border.*;

public final class MainPanel extends JPanel {
    public static final TexturePaint TEXTURE = ImageUtil.makeCheckerTexture();
    private final JDesktopPane desktop = new JDesktopPane() {
        @Override public void updateUI() {
            super.updateUI();
            setOpaque(false);
        }
        // @Override protected void paintComponent(Graphics g) {
        //     super.paintComponent(g);
        //     Graphics2D g2 = (Graphics2D) g.create();
        //     g2.setPaint(new Color(100, 100, 100, 100));
        //     g2.fillRect(0, 0, getWidth(), getHeight());
        //     g2.dispose();
        // }
    };
    public MainPanel() {
        super(new BorderLayout());

        JPanel p = new TranslucentTexturePanel(TEXTURE);
        p.add(new JButton("button"));
        JInternalFrame iframe = new JInternalFrame("InternalFrame", true, true, true, true);
        iframe.setContentPane(p);
        iframe.setSize(160, 80);
        iframe.setLocation(10, 10);
        iframe.setOpaque(false);
        iframe.setVisible(true);
        desktop.add(iframe);

        add(desktop);
        setOpaque(false);
        setPreferredSize(new Dimension(320, 240));
    }

    public static void createAndShowGUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException
               | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        }
        PopupFactory.setSharedInstance(new TranslucentPopupFactory());
        JFrame frame = new JFrame("@title@") {
            @Override protected JRootPane createRootPane() {
                return new JRootPane() {
                    // private final TexturePaint texture = makeCheckerTexture();
                    @Override protected void paintComponent(Graphics g) {
                        super.paintComponent(g);
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setPaint(TEXTURE);
                        g2.fillRect(0, 0, getWidth(), getHeight());
                        g2.dispose();
                    }
                    @Override public void updateUI() {
                        super.updateUI();
                        URL url = MainPanel.class.getResource("test.jpg");
                        BufferedImage bi = ImageUtil.getFilteredImage(url);
                        setBorder(new CentredBackgroundBorder(bi));
                        setOpaque(false);
                    }
                };
            }
        };
        // frame.getRootPane().setBackground(Color.BLUE);
        // frame.getLayeredPane().setBackground(Color.GREEN);
        // frame.getContentPane().setBackground(Color.RED);
        Container contentPane = frame.getContentPane();
        if (contentPane instanceof JComponent) {
            ((JComponent) contentPane).setOpaque(false);
        }
        frame.setJMenuBar(ImageUtil.createMenuBar());
        frame.getContentPane().add(new MainPanel());
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
    public static void main(String... args) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                createAndShowGUI();
            }
        });
    }
}

final class ImageUtil {
    private ImageUtil() { /* Singleton */ }
    public static JMenuBar createMenuBar() {
        UIManager.put("Menu.background", new Color(200, 0, 0, 0));
        UIManager.put("Menu.selectionBackground", new Color(100, 100, 255, 100));
        UIManager.put("Menu.selectionForeground", new Color(200, 200, 200));
        UIManager.put("Menu.useMenuBarBackgroundForTopLevel", Boolean.TRUE);
        JMenuBar mb = new JMenuBar() {
            @Override protected void paintComponent(Graphics g) {
                // super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setPaint(new Color(100, 100, 100, 100));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        mb.setOpaque(false);
        for (String key: new String[] {"File", "Edit", "Help"}) {
            JMenu m = createMenu(key);
            // if (m != null)
            mb.add(m);
        }
        return mb;
    }
    public static JMenu createMenu(String key) {
        JMenu menu = new TransparentMenu(key);
        menu.setForeground(new Color(200, 200, 200));
        menu.setOpaque(false); // Motif lnf
        JMenu sub = new TransparentMenu("Submenu");
        sub.add("JMenuItem");
        sub.add("Looooooooooooooooooooong");
        menu.add(sub);
        menu.add("dummy1");
        menu.add("dummy2");
        return menu;
    }
    public static BufferedImage getFilteredImage(URL url) {
        BufferedImage image;
        try {
            image = ImageIO.read(url);
        } catch (IOException ex) {
            ex.printStackTrace();
            return new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        }
        BufferedImage dest = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
        byte[] b = new byte[256];
        IntStream.range(0, b.length).forEach(i -> b[i] = (byte) (i * .5));
        BufferedImageOp op = new LookupOp(new ByteLookupTable(0, b), null);
        op.filter(image, dest);
        return dest;
    }
    public static TexturePaint makeCheckerTexture() {
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
}

class TranslucentTexturePanel extends JPanel {
    private final transient TexturePaint texture;
    protected TranslucentTexturePanel(TexturePaint texture) {
        super();
        this.texture = texture;
    }
    @Override protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setPaint(texture);
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .6f));
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.dispose();
    }
}

// https://community.oracle.com/thread/1395763 How can I use TextArea with Background Picture ?
// https://ateraimemo.com/Swing/CentredBackgroundBorder.html
class CentredBackgroundBorder implements Border {
    private final BufferedImage image;
    protected CentredBackgroundBorder(BufferedImage image) {
        this.image = image;
    }
    @Override public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        int cx = (width - image.getWidth()) / 2;
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

// https://ateraimemo.com/Swing/TranslucentPopupMenu.html
class TranslucentPopupMenu extends JPopupMenu {
    private static final Color POPUP_BACK = new Color(250, 250, 250, 100);
    private static final Color POPUP_LEFT = new Color(230, 230, 230, 100);
    private static final int LEFT_WIDTH = 24;
    @Override public boolean isOpaque() {
        return false;
    }
    @Override public Component add(Component c) {
        if (c instanceof JComponent) {
            ((JComponent) c).setOpaque(false);
        }
        return c;
    }
    @Override public JMenuItem add(JMenuItem menuItem) {
        menuItem.setOpaque(false);
        return super.add(menuItem);
    }
    // private static final Color ALPHA_ZERO = new Color(0x0, true);
    // @Override public void show(Component c, int x, int y) {
    //     EventQueue.invokeLater(() -> {
    //         Container p = getTopLevelAncestor();
    //         if (p instanceof JWindow) {
    //             System.out.println("Heavy weight");
    //             ((JWindow) p).setBackground(ALPHA_ZERO);
    //         } else {
    //             System.out.println("Light weight");
    //         }
    //     });
    //     super.show(c, x, y);
    // }
    @Override protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setPaint(POPUP_LEFT);
        g2.fillRect(0, 0, LEFT_WIDTH, getHeight());
        g2.setPaint(POPUP_BACK);
        g2.fillRect(LEFT_WIDTH, 0, getWidth(), getHeight());
        g2.dispose();
    }
}

class TransparentMenu extends JMenu {
    private JPopupMenu popupMenu;

    protected TransparentMenu(String title) {
        super(title);
    }
    // [JDK-4688783] JPopupMenu hardcoded i JMenu - Java Bug System
    // https://bugs.openjdk.java.net/browse/JDK-4688783
    private void ensurePopupMenuCreated() {
        if (Objects.isNull(popupMenu)) {
            this.popupMenu = new TranslucentPopupMenu();
            popupMenu.setInvoker(this);
            popupListener = createWinListener(popupMenu);
        }
    }
    @Override public JPopupMenu getPopupMenu() {
        ensurePopupMenuCreated();
        return popupMenu;
    }
    @Override public JMenuItem add(JMenuItem menuItem) {
        ensurePopupMenuCreated();
        menuItem.setOpaque(false);
        return popupMenu.add(menuItem);
    }
    @Override public Component add(Component c) {
        ensurePopupMenuCreated();
        if (c instanceof JComponent) {
            ((JComponent) c).setOpaque(false);
        }
        popupMenu.add(c);
        return c;
    }
    @Override public void addSeparator() {
        ensurePopupMenuCreated();
        popupMenu.addSeparator();
    }
    @Override public void insert(String s, int pos) {
        if (pos < 0) {
            throw new IllegalArgumentException("index less than zero.");
        }
        ensurePopupMenuCreated();
        popupMenu.insert(new JMenuItem(s), pos);
    }
    @Override public JMenuItem insert(JMenuItem mi, int pos) {
        if (pos < 0) {
            throw new IllegalArgumentException("index less than zero.");
        }
        ensurePopupMenuCreated();
        popupMenu.insert(mi, pos);
        return mi;
    }
    @Override public void insertSeparator(int index) {
        if (index < 0) {
            throw new IllegalArgumentException("index less than zero.");
        }
        ensurePopupMenuCreated();
        popupMenu.insert(new JPopupMenu.Separator(), index);
    }
    @Override public boolean isPopupMenuVisible() {
        ensurePopupMenuCreated();
        return popupMenu.isVisible();
    }
}

/*
<a href="http://today.java.net/pub/a/today/2008/03/18/translucent-and-shaped-swing-windows.html">
Translucent and Shaped Swing Windows | Java.net
</a>
*/
class TranslucentPopupFactory extends PopupFactory {
    @Override public Popup getPopup(Component owner, Component contents, int x, int y) { // throws IllegalArgumentException {
        return new TranslucentPopup(owner, contents, x, y);
    }
}

class TranslucentPopup extends Popup {
    private final JWindow popupWindow;
    protected TranslucentPopup(Component owner, Component contents, int ownerX, int ownerY) {
        super(owner, contents, ownerX, ownerY);
        // create a new heavyweight window
        this.popupWindow = new JWindow();
        // mark the popup with partial opacity
        // AWTUtilities.setWindowOpacity(popupWindow, (contents instanceof JToolTip) ? .8f : .95f);
        // popupWindow.setOpacity(.5f);
        // AWTUtilities.setWindowOpaque(popupWindow, false); // Java 1.6.0_10
        popupWindow.setBackground(new Color(0x0, true)); // Java 1.7.0
        // determine the popup location
        popupWindow.setLocation(ownerX, ownerY);
        // add the contents to the popup
        popupWindow.getContentPane().add(contents);
        contents.invalidate();
        // JComponent parent = (JComponent) contents.getParent();
        // set the shadow border
        // parent.setBorder(new ShadowPopupBorder());
    }
    @Override public void show() {
        // System.out.println("Always Heavy weight!");
        this.popupWindow.setVisible(true);
        this.popupWindow.pack();
        // mark the window as non-opaque, so that the
        // shadow border pixels take on the per-pixel
        // translucency
        // AWTUtilities.setWindowOpaque(this.popupWindow, false);
    }
    @Override public void hide() {
        this.popupWindow.setVisible(false);
        this.popupWindow.removeAll();
        this.popupWindow.dispose();
    }
}
