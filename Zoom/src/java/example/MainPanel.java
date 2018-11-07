package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@
import java.awt.*;
import java.awt.event.MouseWheelListener;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());
        ImageIcon icon = new ImageIcon(getClass().getResource("test.png"));
        ZoomImage zoom = new ZoomImage(icon.getImage());

        JButton button1 = new JButton("Zoom In");
        button1.addActionListener(e -> zoom.changeScale(-5));

        JButton button2 = new JButton("Zoom Out");
        button2.addActionListener(e -> zoom.changeScale(5));

        JButton button3 = new JButton("Original size");
        button3.addActionListener(e -> zoom.initScale());

        Box box = Box.createHorizontalBox();
        box.add(Box.createHorizontalGlue());
        box.add(button1);
        box.add(button2);
        box.add(button3);

        add(zoom);
        add(box, BorderLayout.SOUTH);
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

class ZoomImage extends JPanel {
    private transient MouseWheelListener handler;
    private final transient Image image;
    private final int iw;
    private final int ih;
    private double scale = 1d;
    protected ZoomImage(Image image) {
        super();
        this.image = image;
        iw = image.getWidth(this);
        ih = image.getHeight(this);
    }
    @Override public void updateUI() {
        removeMouseWheelListener(handler);
        super.updateUI();
        // handler = new MouseWheelListener() {
        //     @Override public void mouseWheelMoved(MouseWheelEvent e) {
        //         changeScale(e.getWheelRotation());
        //     }
        // };
        handler = e -> changeScale(e.getWheelRotation());
        addMouseWheelListener(handler);
    }
    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.scale(scale, scale);
        g2.drawImage(image, 0, 0, iw, ih, this);
        g2.dispose();
    }
    public void initScale() {
        scale = 1d;
        repaint();
    }
    public void changeScale(int iv) {
        scale = Math.max(.05, Math.min(5d, scale - iv * .05));
        repaint();
        // double v = scale - iv * .1;
        // if (v - 1d > -1.0e-2) {
        //     scale = Math.min(10d, v);
        // } else {
        //     scale = Math.max(.01, scale - iv * .01);
        // }
    }
}
