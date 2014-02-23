package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.util.List;
import java.util.Arrays;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private final List<JButton> list = Arrays.asList(
        new JButton("JButton"),
        new JButton("+getPreferredSize") {
            @Override public Dimension getPreferredSize() {
                Dimension d = super.getPreferredSize();
                d.width += 3 + 3;
                return d;
            }
        },
        new BlurJButton("Blurred JButton1"),
        new BlurJButton("+getPreferredSize") {
            @Override public Dimension getPreferredSize() {
                Dimension d = super.getPreferredSize();
                d.width += 9;
                return d;
            }
        },
        new BlurButton("Blurred JButton2"),
        new BlurButton("+getPreferredSize") {
            @Override public Dimension getPreferredSize() {
                Dimension d = super.getPreferredSize();
                d.width += 9;
                return d;
            }
        });

    public MainPanel() {
        super(new BorderLayout());

        Box box = Box.createVerticalBox();
        JPanel p0 = new JPanel();
        p0.setBorder(BorderFactory.createTitledBorder("Default JButton"));
        p0.add(list.get(0));
        p0.add(list.get(1));
        box.add(p0);
        box.add(Box.createVerticalStrut(10));

        JPanel p1 = new JPanel();
        p1.setBorder(BorderFactory.createTitledBorder("Blurred JButton1"));
        p1.add(list.get(2));
        p1.add(list.get(3));
        box.add(p1);
        box.add(Box.createVerticalStrut(10));

        JPanel p2 = new JPanel();
        p2.setBorder(BorderFactory.createTitledBorder("Blurred JButton(ConvolveOp.EDGE_NO_OP)"));
        p2.add(list.get(4));
        p2.add(list.get(5));
        box.add(p2);
        box.add(Box.createVerticalStrut(10));

        add(box, BorderLayout.NORTH);
        add(new JToggleButton(new AbstractAction("setEnabled(false)") {
            @Override public void actionPerformed(ActionEvent e) {
                boolean f = ((AbstractButton) e.getSource()).isSelected();
                f ^= true;
                for (JButton b: list) {
                    b.setEnabled(f);
                }
            }
        }), BorderLayout.SOUTH);
        setPreferredSize(new Dimension(320, 240));
    }
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                createAndShowGUI();
            }
        });
    }
    public static void createAndShowGUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException |
                 IllegalAccessException | UnsupportedLookAndFeelException ex) {
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

//http://shop.oreilly.com/product/9780596009076.do
//9. Blur Disabled Components
//http://code.google.com/p/filthy-rich-clients/source/browse/trunk/swing-hacks-examples-20060109/Ch01-JComponents/09/swinghacks/ch01/JComponents/hack09/BlurJButton.java?r=11
class BlurJButton extends JButton {
    private static final ConvolveOp CONVOLVE_OP = new ConvolveOp(new Kernel(3, 3, new float[] {
        .05f, .05f, .05f,
        .05f, .60f, .05f,
        .05f, .05f, .05f
    }));
    private int iw = -1;
    private int ih = -1;
    private transient BufferedImage buf;
    public BlurJButton(String label) {
        super(label);
        //System.out.println(op.getEdgeCondition());
    }
    @Override protected void paintComponent(Graphics g) {
        if (isEnabled()) {
            super.paintComponent(g);
        } else {
            if (buf == null || iw != getWidth() || ih != getHeight()) {
                iw = getWidth();
                ih = getHeight();
                buf = new BufferedImage(iw, ih, BufferedImage.TYPE_INT_ARGB);
            }
            Graphics2D g2 = (Graphics2D) buf.getGraphics();
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
    private int iw = -1;
    private int ih = -1;
    private transient BufferedImage buf;
    public BlurButton(String label) {
        super(label);
        //System.out.println(op.getEdgeCondition());
    }
    @Override protected void paintComponent(Graphics g) {
        if (isEnabled()) {
            super.paintComponent(g);
        } else {
            if (buf == null || iw != getWidth() || ih != getHeight()) {
                iw = getWidth();
                ih = getHeight();
                buf = new BufferedImage(iw, ih, BufferedImage.TYPE_INT_ARGB);
            }
            Graphics2D g2 = (Graphics2D) buf.getGraphics();
            super.paintComponent(g2);
            g2.dispose();
            g.drawImage(CONVOLVE_OP.filter(buf, null), 0, 0, null);
        }
    }
//     @Override public Dimension getPreferredSize() {
//         Dimension d = super.getPreferredSize();
//         d.width += 3 * 3;
//         return d;
//     }
}
