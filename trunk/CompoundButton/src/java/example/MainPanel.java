package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.geom.*;
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

class CompoundButtonPanel extends JComponent {
    private final Dimension d;
    public CompoundButtonPanel(final Dimension d) {
        super();
        this.d = d;
        setLayout(new OverlayLayout(this));
        add(new CompoundButton(d, ButtonLocation.CENTER));
        add(new CompoundButton(d, ButtonLocation.NOTH));
        add(new CompoundButton(d, ButtonLocation.SOUTH));
        add(new CompoundButton(d, ButtonLocation.EAST));
        add(new CompoundButton(d, ButtonLocation.WEST));
    }
    @Override public Dimension getPreferredSize() {
        return d;
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
    private ButtonLocation(float degree) {
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
    private final ButtonLocation bl;
    private final Dimension dim;
    public CompoundButton(Dimension d, ButtonLocation bl) {
        super();
        this.dim = d;
        this.bl = bl;
        setIcon(new Icon() {
            @Override public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isArmed()) {
                    g2.setColor(ac);
                    g2.fill(shape);
                } else if (isRolloverEnabled() && getModel().isRollover()) {
                    paintFocusAndRollover(g2, rc);
                } else if (hasFocus()) {
                    paintFocusAndRollover(g2, fc);
                } else {
                    g2.setColor(getBackground());
                    g2.fill(shape);
                }
                g2.dispose();
            }
            @Override public int getIconWidth()  {
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
            float ww = getWidth() * .5f;
            float xx = ww * .5f;
            Shape inner = new Ellipse2D.Float(xx, xx, ww, ww);
            if (ButtonLocation.CENTER == bl) {
                shape = inner;
            } else {
                //TEST: parent.isOptimizedDrawingEnabled: false
                //shape = new Arc2D.Float(1, 1, getWidth() - 2, getHeight() - 2, bl.getStartDegree(), 90f, Arc2D.PIE);
                Shape outer = new Arc2D.Float(1, 1, getWidth() - 2, getHeight() - 2, bl.getStartDegree(), 90f, Arc2D.PIE);
                Area area = new Area(outer);
                area.subtract(new Area(inner));
                shape = area;
            }
        }
    }
    private void paintFocusAndRollover(Graphics2D g2, Color color) {
        g2.setPaint(new GradientPaint(0, 0, color, getWidth() - 1, getHeight() - 1, color.brighter(), true));
        g2.fill(shape);
        g2.setColor(getBackground());
    }
    @Override protected void paintComponent(Graphics g) {
        initShape();
        super.paintComponent(g);
    }
    @Override protected void paintBorder(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(getForeground());
        g2.draw(shape);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_OFF);
        g2.dispose();
    }
    @Override public boolean contains(int x, int y) {
        //initShape();
        return shape == null ? false : shape.contains(x, y);
    }
}
