package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.geom.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());

        Box box1 = Box.createHorizontalBox();
        box1.add(new JLabel("asasdfadfsdf"));
        box1.add(new WavyLineSeparator(SwingConstants.VERTICAL));
        box1.add(new JLabel("123a2adfasdfa1sdf"));
        box1.add(Box.createHorizontalGlue());
        box1.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        Box box2 = Box.createVerticalBox();
        box2.add(new JLabel("asdfasdfas"));
        box2.add(new JLabel("----adfa-------"));
        box2.add(new JLabel("1235436873434325"));
        box2.add(new WavyLineSeparator());
        box2.add(new JLabel("asdfasdfas"));
        box2.add(new JLabel("1235436873434325"));
        box2.add(Box.createVerticalGlue());
        box2.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        add(box1, BorderLayout.NORTH);
        add(box2);
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

class WavyLineSeparator extends JSeparator {
    private static final int ICONWIDTH = 3;
    private static final Icon WAVY_HLINE = new WavyLineIcon();
    private static final Icon WAVY_VLINE = new WavyLineIcon(SwingConstants.VERTICAL);
    protected WavyLineSeparator() {
        this(SwingConstants.HORIZONTAL);
    }
    protected WavyLineSeparator(int orientation) {
        super(orientation);
        if (orientation == SwingConstants.HORIZONTAL) {
            setBorder(BorderFactory.createEmptyBorder(2, 1, 2, 1));
        } else {
            setBorder(BorderFactory.createEmptyBorder(1, 2, 1, 2));
        }
    }
    @Override protected void paintComponent(Graphics g) {
        //super.paintComponent(g);
        //g.setClip(0, 0, getWidth(), getHeight());
        int pos;
        Insets i = getInsets();
        if (getOrientation() == SwingConstants.HORIZONTAL) {
            for (pos = i.left; getWidth() - pos > 0; pos += WAVY_HLINE.getIconWidth()) {
                WAVY_HLINE.paintIcon(this, g, pos, i.top);
            }
        } else {
            for (pos = i.top; getHeight() - pos > 0; pos += WAVY_VLINE.getIconHeight()) {
                WAVY_VLINE.paintIcon(this, g, i.left, pos);
            }
        }
    }
    @Override public Dimension getPreferredSize() {
        Insets i = getInsets();
        if (getOrientation() == SwingConstants.HORIZONTAL) {
            return new Dimension(30, ICONWIDTH + i.top + i.bottom);
        } else {
            return new Dimension(ICONWIDTH + i.left + i.right, 30);
        }
    }
    private static class WavyLineIcon implements Icon {
        private final Color sfc = UIManager.getColor("Separator.foreground");
        private final int orientation;
        protected WavyLineIcon() {
            this.orientation = SwingConstants.HORIZONTAL;
        }
        protected WavyLineIcon(int orientation) {
            this.orientation = orientation;
        }
        @Override public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            AffineTransform oldTransform = g2.getTransform();
            g2.setPaint(sfc);
            if (orientation == SwingConstants.VERTICAL) {
                g2.translate(x + getIconWidth(), y);
                g2.rotate(Math.PI / 2);
            } else {
                g2.translate(x, y);
            }
            g2.drawLine(0, 2, 0, 2);
            g2.drawLine(1, 1, 1, 1);
            g2.drawLine(2, 0, 3, 0);
            g2.drawLine(4, 1, 4, 1);
            g2.drawLine(5, 2, 5, 2);
            g2.setTransform(oldTransform);
            g2.dispose();
        }
        @Override public int getIconWidth() {
            return (orientation == SwingConstants.HORIZONTAL) ? ICONWIDTH * 2 : ICONWIDTH;
        }
        @Override public int getIconHeight() {
            return (orientation == SwingConstants.HORIZONTAL) ? ICONWIDTH : ICONWIDTH * 2;
        }
    }
}
