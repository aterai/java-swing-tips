package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());
        try {
            Image img = ImageIO.read(getClass().getResource("CRW_3857_JFR.jpg"));
            add(new JScrollPane(new ZoomAndPanePanel(img)));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        setPreferredSize(new Dimension(320, 240));
    }
    public static void main(String... args) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                createAndShowGui();
            }
        });
    }
    public static void createAndShowGui() {
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

class ZoomAndPanePanel extends JPanel {
    protected final AffineTransform zoomTransform = new AffineTransform();
    protected final transient Image img;
    protected final Rectangle imgrect;
    protected transient ZoomHandler handler;
    protected transient DragScrollListener listener;

    protected ZoomAndPanePanel(Image img) {
        super();
        this.img = img;
        this.imgrect = new Rectangle(img.getWidth(this), img.getHeight(this));
    }
    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setPaint(new Color(0x55FF0000, true));
        Rectangle r = new Rectangle(500, 140, 150, 150);

        // use: AffineTransform#concatenate(...) and Graphics2D#setTransform(...)
        // https://docs.oracle.com/javase/8/docs/api/java/awt/geom/AffineTransform.html#concatenate-java.awt.geom.AffineTransform-
        // AffineTransform at = g2.getTransform();
        // at.concatenate(zoomTransform);
        // g2.setTransform(at);
        // g2.drawImage(img, 0, 0, this);
        // g2.fill(r);

        // or use: Graphics2D#drawImage(Image, AffineTransform, ImageObserver)
        // https://docs.oracle.com/javase/8/docs/api/java/awt/Graphics2D.html#drawImage-java.awt.Image-java.awt.geom.AffineTransform-java.awt.image.ImageObserver-
        g2.drawImage(img, zoomTransform, this); // or: g2.drawRenderedImage((RenderedImage) img, zoomTransform);
        g2.fill(zoomTransform.createTransformedShape(r));

        // BAD EXAMPLE
        // g2.setTransform(zoomTransform);
        // g2.drawImage(img, 0, 0, this);

        g2.dispose();
    }
    @Override public Dimension getPreferredSize() {
        Rectangle r = zoomTransform.createTransformedShape(imgrect).getBounds();
        return new Dimension(r.width, r.height);
    }
    @Override public void updateUI() {
        removeMouseListener(listener);
        removeMouseMotionListener(listener);
        removeMouseWheelListener(handler);
        super.updateUI();
        listener = new DragScrollListener();
        addMouseListener(listener);
        addMouseMotionListener(listener);
        handler = new ZoomHandler();
        addMouseWheelListener(handler);
    }

    protected class ZoomHandler extends MouseAdapter {
        private static final double ZOOM_MULTIPLICATION_FACTOR = 1.2;
        private static final int MIN_ZOOM = -10;
        private static final int MAX_ZOOM = 10;
        private static final int EXTENT = 1;
        private final BoundedRangeModel zoomRange = new DefaultBoundedRangeModel(0, EXTENT, MIN_ZOOM, MAX_ZOOM + EXTENT);
        @Override public void mouseWheelMoved(MouseWheelEvent e) {
            int dir = e.getWheelRotation();
            int z = zoomRange.getValue();
            zoomRange.setValue(z + EXTENT * (dir > 0 ? -1 : 1));
            if (z != zoomRange.getValue()) {
                Component c = e.getComponent();
                Container p = SwingUtilities.getAncestorOfClass(JViewport.class, c);
                if (p instanceof JViewport) {
                    JViewport vport = (JViewport) p;
                    Rectangle ovr = vport.getViewRect();
                    double s = dir > 0 ? 1d / ZOOM_MULTIPLICATION_FACTOR : ZOOM_MULTIPLICATION_FACTOR;
                    zoomTransform.scale(s, s);
                    // double s = 1d + zoomRange.getValue() * .1;
                    // zoomTransform.setToScale(s, s);
                    Rectangle nvr = AffineTransform.getScaleInstance(s, s).createTransformedShape(ovr).getBounds();
                    Point vp = nvr.getLocation();
                    vp.translate((nvr.width - ovr.width) / 2, (nvr.height - ovr.height) / 2);
                    vport.setViewPosition(vp);
                    c.revalidate();
                    c.repaint();
                }
            }
        }
    }
}

class DragScrollListener extends MouseAdapter {
    private final Cursor defCursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
    private final Cursor hndCursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
    private final Point pp = new Point();
    @Override public void mouseDragged(MouseEvent e) {
        Component c = e.getComponent();
        Container p = SwingUtilities.getUnwrappedParent(c);
        if (p instanceof JViewport) {
            JViewport vport = (JViewport) p;
            Point cp = SwingUtilities.convertPoint(c, e.getPoint(), vport);
            Point vp = vport.getViewPosition();
            vp.translate(pp.x - cp.x, pp.y - cp.y);
            ((JComponent) c).scrollRectToVisible(new Rectangle(vp, vport.getSize()));
            pp.setLocation(cp);
        }
    }
    @Override public void mousePressed(MouseEvent e) {
        Component c = e.getComponent();
        c.setCursor(hndCursor);
        Container p = SwingUtilities.getUnwrappedParent(c);
        if (p instanceof JViewport) {
            JViewport vport = (JViewport) p;
            Point cp = SwingUtilities.convertPoint(c, e.getPoint(), vport);
            pp.setLocation(cp);
        }
    }
    @Override public void mouseReleased(MouseEvent e) {
        e.getComponent().setCursor(defCursor);
    }
}
