package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import javax.swing.*;
import javax.swing.plaf.basic.*;

public class RoundedCornerButtonUI extends BasicButtonUI {
    private static final float ARC_WIDTH  = 16f;
    private static final float ARC_HEIGHT = 16f;
    protected static final int FOCUS_STROKE = 2;
    protected final Color fc = new Color(100, 150, 255);
    protected final Color ac = new Color(220, 225, 230);
    protected final Color rc = Color.ORANGE;
    protected Shape shape;
    protected Shape border;
    protected Shape base;

    @Override protected void installDefaults(AbstractButton b) {
        super.installDefaults(b);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setOpaque(false);
        b.setBackground(new Color(245, 250, 255));
        b.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));
        initShape(b);
    }
    @Override protected void installListeners(AbstractButton button) {
        BasicButtonListener listener = new BasicButtonListener(button) {
            @Override public void mousePressed(MouseEvent e) {
                AbstractButton b = (AbstractButton) e.getComponent();
                initShape(b);
                if (shape.contains(e.getX(), e.getY())) {
                    super.mousePressed(e);
                }
            }
            @Override public void mouseEntered(MouseEvent e) {
                if (shape.contains(e.getX(), e.getY())) {
                    super.mouseEntered(e);
                }
            }
            @Override public void mouseMoved(MouseEvent e) {
                if (shape.contains(e.getX(), e.getY())) {
                    super.mouseEntered(e);
                } else {
                    super.mouseExited(e);
                }
            }
        };
        //if (listener != null)
        button.addMouseListener(listener);
        button.addMouseMotionListener(listener);
        button.addFocusListener(listener);
        button.addPropertyChangeListener(listener);
        button.addChangeListener(listener);
    }
    @Override public void paint(Graphics g, JComponent c) {
        initShape(c);

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        //ContentArea
        if (c instanceof AbstractButton) {
            AbstractButton b = (AbstractButton) c;
            ButtonModel model = b.getModel();
            if (model.isArmed()) {
                g2.setColor(ac);
                g2.fill(shape);
            } else if (b.isRolloverEnabled() && model.isRollover()) {
                paintFocusAndRollover(g2, c, rc);
            } else if (b.hasFocus()) {
                paintFocusAndRollover(g2, c, fc);
            } else {
                g2.setColor(c.getBackground());
                g2.fill(shape);
            }
        }

        //Border
        g2.setPaint(c.getForeground());
        g2.draw(shape);
        g2.dispose();
        super.paint(g, c);
    }
    private void initShape(JComponent c) {
        if (!c.getBounds().equals(base)) {
            base = c.getBounds();
            shape = new RoundRectangle2D.Float(0, 0, c.getWidth() - 1, c.getHeight() - 1, ARC_WIDTH, ARC_HEIGHT);
            border = new RoundRectangle2D.Float(FOCUS_STROKE, FOCUS_STROKE,
                                                c.getWidth() - 1 - FOCUS_STROKE * 2,
                                                c.getHeight() - 1 - FOCUS_STROKE * 2,
                                                ARC_WIDTH, ARC_HEIGHT);
        }
    }
    private void paintFocusAndRollover(Graphics2D g2, JComponent c, Color color) {
        g2.setPaint(new GradientPaint(0, 0, color, c.getWidth() - 1, c.getHeight() - 1, color.brighter(), true));
        g2.fill(shape);
        g2.setColor(c.getBackground());
        g2.fill(border);
    }
}
