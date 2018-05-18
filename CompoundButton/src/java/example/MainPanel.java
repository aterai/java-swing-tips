package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.geom.*;
import java.util.Objects;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super();

        Dimension d = new Dimension(64, 64);
        add(new CompoundButton(d, ButtonLocation.NOTH));
        add(new CompoundButton(d, ButtonLocation.SOUTH));
        add(new CompoundButton(d, ButtonLocation.EAST));
        add(new CompoundButton(d, ButtonLocation.WEST));
        add(new CompoundButton(d, ButtonLocation.CENTER));
        add(new CompoundButtonPanel(d));

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

class CompoundButtonPanel extends JComponent {
    private final Dimension dim;
    protected CompoundButtonPanel(Dimension dim) {
        super();
        this.dim = dim;
        setLayout(new OverlayLayout(this));
        add(new CompoundButton(dim, ButtonLocation.CENTER));
        add(new CompoundButton(dim, ButtonLocation.NOTH));
        add(new CompoundButton(dim, ButtonLocation.SOUTH));
        add(new CompoundButton(dim, ButtonLocation.EAST));
        add(new CompoundButton(dim, ButtonLocation.WEST));
    }
    @Override public Dimension getPreferredSize() {
        return dim;
    }
    @Override public boolean isOptimizedDrawingEnabled() {
        return false;
    }
}

enum ButtonLocation {
    CENTER(0f),
    NOTH(45f),
    EAST(135f),
    SOUTH(225f),
    WEST(-45f);
    private final float degree;
    ButtonLocation(float degree) {
        this.degree = degree;
    }
    public float getStartDegree() {
        return degree;
    }
}

class CompoundButton extends JButton {
    protected final Color fc = new Color(100, 150, 255, 200);
    protected final Color ac = new Color(230, 230, 230);
    protected final Color rc = Color.ORANGE;
    protected transient Shape shape;
    protected transient Shape base;
    protected final ButtonLocation bl;
    protected final Dimension dim;
    protected CompoundButton(Dimension d, ButtonLocation bl) {
        super();
        this.dim = d;
        this.bl = bl;
        setIcon(new Icon() {
            @Override public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isArmed()) {
                    g2.setPaint(ac);
                    g2.fill(shape);
                } else if (isRolloverEnabled() && getModel().isRollover()) {
                    paintFocusAndRollover(g2, rc);
                } else if (hasFocus()) {
                    paintFocusAndRollover(g2, fc);
                } else {
                    g2.setPaint(getBackground());
                    g2.fill(shape);
                }
                g2.dispose();
            }
            @Override public int getIconWidth() {
                return dim.width;
            }
            @Override public int getIconHeight() {
                return dim.height;
            }
        });
        setFocusPainted(false);
        setContentAreaFilled(false);
        setBackground(new Color(250, 250, 250));
        initShape();
    }
    @Override public Dimension getPreferredSize() {
        return dim;
    }
    private void initShape() {
        if (!getBounds().equals(base)) {
            base = getBounds();
            double ww = getWidth() * .5;
            double xx = ww * .5;
            Shape inner = new Ellipse2D.Double(xx, xx, ww, ww);
            if (ButtonLocation.CENTER == bl) {
                shape = inner;
            } else {
                // TEST: parent.isOptimizedDrawingEnabled: false
                // shape = new Arc2D.Double(1, 1, getWidth() - 2, getHeight() - 2, bl.getStartDegree(), 90, Arc2D.PIE);
                Shape outer = new Arc2D.Double(1, 1d, getWidth() - 2, getHeight() - 2, bl.getStartDegree(), 90, Arc2D.PIE);
                Area area = new Area(outer);
                area.subtract(new Area(inner));
                shape = area;
            }
        }
    }
    protected void paintFocusAndRollover(Graphics2D g2, Color color) {
        g2.setPaint(new GradientPaint(0, 0, color, getWidth() - 1, getHeight() - 1, color.brighter(), true));
        g2.fill(shape);
        g2.setPaint(getBackground());
    }
    @Override protected void paintComponent(Graphics g) {
        initShape();
        super.paintComponent(g);
    }
    @Override protected void paintBorder(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setPaint(getForeground());
        g2.draw(shape);
        g2.dispose();
    }
    @Override public boolean contains(int x, int y) {
        return Objects.nonNull(shape) && shape.contains(x, y);
    }
}
