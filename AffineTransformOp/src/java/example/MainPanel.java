package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.stream.Stream;
import javax.imageio.ImageIO;
import javax.swing.*;

public class MainPanel extends JPanel {
    protected Flip mode;
    protected final transient BufferedImage bufferedImage;
    protected final ButtonGroup bg = new ButtonGroup();
    protected final JPanel panel = new JPanel() {
        @Override protected void paintComponent(Graphics g) {
            g.setColor(getBackground());
            g.fillRect(0, 0, getWidth(), getHeight());
            int w = bufferedImage.getWidth(this);
            int h = bufferedImage.getHeight(this);
            if (mode == Flip.VERTICAL) {
                AffineTransform at = AffineTransform.getScaleInstance(1d, -1d);
                at.translate(0, -h);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.drawImage(bufferedImage, at, this);
                g2.dispose();
            } else if (mode == Flip.HORIZONTAL) {
                AffineTransform at = AffineTransform.getScaleInstance(-1d, 1d);
                at.translate(-w, 0);
                AffineTransformOp atOp = new AffineTransformOp(at, null);
                g.drawImage(atOp.filter(bufferedImage, null), 0, 0, w, h, this);
            } else { // if (mode == Flip.NONE) {
                g.drawImage(bufferedImage, 0, 0, w, h, this);
            }
        }
    };
    public MainPanel() {
        super(new BorderLayout());
        BufferedImage bi = null;
        try {
            bi = ImageIO.read(MainPanel.class.getResource("test.jpg"));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        bufferedImage = bi;
        Box box = Box.createHorizontalBox();
        box.add(Box.createHorizontalGlue());
        box.add(new JLabel("Flip: "));
        Stream.of(Flip.values()).map(this::makeRadioButton).forEach(rb -> {
            box.add(rb);
            bg.add(rb);
            box.add(Box.createHorizontalStrut(5));
        });
        add(panel);
        add(box, BorderLayout.SOUTH);
        setOpaque(false);
        setPreferredSize(new Dimension(320, 240));
    }
    private JRadioButton makeRadioButton(Flip f) {
        JRadioButton rb = new JRadioButton(f.toString(), f == Flip.NONE);
        rb.addActionListener(e -> {
            mode = f;
            panel.repaint();
        });
        return rb;
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

enum Flip { NONE, VERTICAL, HORIZONTAL; }
