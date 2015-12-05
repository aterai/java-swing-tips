package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import javax.imageio.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private Flip mode = Flip.NONE;
    private final transient BufferedImage bufferedImage;
    private final ButtonGroup bg = new ButtonGroup();
    private final JPanel p = new JPanel() {
        @Override protected void paintComponent(Graphics g) {
            g.setColor(getBackground());
            g.fillRect(0, 0, getWidth(), getHeight());
            int w = bufferedImage.getWidth(this);
            int h = bufferedImage.getHeight(this);
            if (mode == Flip.NONE) {
                g.drawImage(bufferedImage, 0, 0, w, h, this);
            } else if (mode == Flip.VERTICAL) {
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
            }
        }
    };
    public MainPanel() {
        super(new BorderLayout());
        BufferedImage bi = null;
        try {
            bi = ImageIO.read(getClass().getResource("test.jpg"));
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        bufferedImage = bi;
        List<AbstractAction> list = Arrays.asList(
            new AbstractAction("NONE") {
                @Override public void actionPerformed(ActionEvent e) {
                    mode = Flip.NONE;
                    repaint();
                }
            },
            new AbstractAction("VERTICAL") {
                @Override public void actionPerformed(ActionEvent e) {
                    mode = Flip.VERTICAL;
                    repaint();
                }
            },
            new AbstractAction("HORIZONTAL") {
                @Override public void actionPerformed(ActionEvent e) {
                    mode = Flip.HORIZONTAL;
                    repaint();
                }
            }
        );
        Box box = Box.createHorizontalBox();
        box.add(Box.createHorizontalGlue());
        box.add(new JLabel("Flip: "));
        for (AbstractAction a: list) {
            JRadioButton rb = new JRadioButton(a);
            if (bg.getButtonCount() == 0) {
                rb.setSelected(true);
            }
            box.add(rb);
            bg.add(rb);
            box.add(Box.createHorizontalStrut(5));
        }
        add(p);
        add(box, BorderLayout.SOUTH);
        setOpaque(false);
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

enum Flip { NONE, VERTICAL, HORIZONTAL; }
