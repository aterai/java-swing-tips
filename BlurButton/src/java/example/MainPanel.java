package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@
import java.awt.*;
import java.awt.image.*;
import java.util.Optional;
import java.util.stream.Stream;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());

        JPanel p0 = new JPanel();
        p0.setBorder(BorderFactory.createTitledBorder("Default JButton"));
        JButton b0 = new JButton("Default JButton");
        p0.add(b0);

        JPanel p1 = new JPanel();
        p1.setBorder(BorderFactory.createTitledBorder("Blurred JButton1"));
        JButton b1 = new BlurJButton("Blurred JButton1");
        p1.add(b1);

        JPanel p2 = new JPanel();
        p2.setBorder(BorderFactory.createTitledBorder("Blurred JButton(ConvolveOp.EDGE_NO_OP)"));
        JButton b2 = new BlurButton("Blurred JButton2");
        p2.add(b2);

        Box box = Box.createVerticalBox();
        Stream.of(p0, p1, p2).forEach(p -> {
            box.add(p);
            box.add(Box.createVerticalStrut(10));
        });

        JToggleButton button = new JToggleButton("setEnabled(false)");
        button.addActionListener(e -> {
            boolean f = !((AbstractButton) e.getSource()).isSelected();
            Stream.of(b0, b1, b2).forEach(b -> b.setEnabled(f));
        });

        add(box, BorderLayout.NORTH);
        add(button, BorderLayout.SOUTH);
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

// http://shop.oreilly.com/product/9780596009076.do
// 9. Blur Disabled Components
// http://code.google.com/p/filthy-rich-clients/source/browse/trunk/swing-hacks-examples-20060109/Ch01-JComponents/09/swinghacks/ch01/JComponents/hack09/BlurJButton.java?r=11
class BlurJButton extends JButton {
    private static final ConvolveOp CONVOLVE_OP = new ConvolveOp(new Kernel(3, 3, new float[] {
        .05f, .05f, .05f,
        .05f, .60f, .05f,
        .05f, .05f, .05f
    }));
    private transient BufferedImage buf;
    protected BlurJButton(String label) {
        super(label);
        // System.out.println(op.getEdgeCondition());
    }
    @Override protected void paintComponent(Graphics g) {
        if (isEnabled()) {
            super.paintComponent(g);
        } else {
            // if (Objects.isNull(buf) || iw != getWidth() || ih != getHeight()) {
            //     iw = getWidth();
            //     ih = getHeight();
            //     buf = new BufferedImage(iw, ih, BufferedImage.TYPE_INT_ARGB);
            // }
            buf = Optional.ofNullable(buf)
                .filter(bi -> bi.getWidth() == getWidth() && bi.getHeight() == getHeight())
                .orElseGet(() -> new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB));
            Graphics2D g2 = buf.createGraphics();
            g2.setFont(g.getFont()); // pointed out by 八ツ玉舘
            super.paintComponent(g2);
            g2.dispose();
            g.drawImage(CONVOLVE_OP.filter(buf, null), 0, 0, null);
        }
    }
}

class BlurButton extends JButton {
    private static final ConvolveOp CONVOLVE_OP = new ConvolveOp(new Kernel(3, 3, new float[] {
        .05f, .05f, .05f,
        .05f, .60f, .05f,
        .05f, .05f, .05f
    }), ConvolveOp.EDGE_NO_OP, null);
    private transient BufferedImage buf;
    protected BlurButton(String label) {
        super(label);
        // System.out.println(op.getEdgeCondition());
    }
    @Override protected void paintComponent(Graphics g) {
        if (isEnabled()) {
            super.paintComponent(g);
        } else {
            buf = Optional.ofNullable(buf)
                .filter(bi -> bi.getWidth() == getWidth() && bi.getHeight() == getHeight())
                .orElseGet(() -> new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB));
            Graphics2D g2 = buf.createGraphics();
            g2.setFont(g.getFont()); // pointed out by 八ツ玉舘
            super.paintComponent(g2);
            g2.dispose();
            g.drawImage(CONVOLVE_OP.filter(buf, null), 0, 0, null);
        }
    }
    // @Override public Dimension getPreferredSize() {
    //     Dimension d = super.getPreferredSize();
    //     d.width += 3 * 3;
    //     return d;
    // }
}
