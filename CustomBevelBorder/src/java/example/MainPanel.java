package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@
import java.awt.*;
import java.awt.geom.Path2D;
import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());
        JButton button1 = makeButton("Default BevelBorder", new BevelBorder(BevelBorder.RAISED) {
            @Override public Insets getBorderInsets(Component c, Insets insets) {
                insets.set(10, 10, 10, 10);
                return insets;
            }
        });
        JButton button2 = makeButton("Custom BevelBorder", new CustomBevelBorder(BevelBorder.RAISED));

        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        p.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));
        p.add(button1);
        p.add(button2);

        add(p);
        EventQueue.invokeLater(() -> SwingUtilities.updateComponentTreeUI(p));
        setPreferredSize(new Dimension(320, 240));
    }
    private static JButton makeButton(String text, Border border) {
        return new JButton("<html>JButton<br>+ " + text) {
            @Override public void updateUI() {
                super.updateUI();
                setOpaque(true);
                setForeground(Color.WHITE);
                setBackground(new Color(91, 155, 213));
                setFocusPainted(false);
                setContentAreaFilled(false);
                setBorder(border);
            }
        };
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

class CustomBevelBorder extends BevelBorder {
    private final Insets ins = new Insets(8, 8, 8, 8);
    protected CustomBevelBorder(int bevelType) {
        super(bevelType);
    }
    @Override public Insets getBorderInsets(Component c, Insets insets) {
        insets.set(ins.top + 2, ins.left + 2, ins.bottom + 2, ins.right + 2);
        return insets;
    }
    @Override public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        boolean isPressed = false;
        if (c instanceof AbstractButton) {
            ButtonModel m = ((AbstractButton) c).getModel();
            isPressed = m.isPressed();
        }
        if (bevelType == RAISED && !isPressed) {
            paintRaisedBevel(c, g, x, y, width, height);
        } else { // if (bevelType == LOWERED) {
            paintLoweredBevel(c, g, x, y, width, height);
        }
    }
    @Override protected void paintRaisedBevel(Component c, Graphics g, int x, int y, int width, int height) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.translate(x, y);

        int w = width - 1;
        int h = height - 1;
        g2.setPaint(getHighlightInnerColor(c));
        fillTopLeft(g2, w, h, ins);

        g2.setPaint(getShadowInnerColor(c));
        g2.fill(makeBottomRightShape(w, h, ins));

        g2.setPaint(getShadowOuterColor(c));
        drawRectLine(g2, w, h, ins);

        g2.dispose();
    }
    @Override protected void paintLoweredBevel(Component c, Graphics g, int x, int y, int width, int height) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.translate(x, y);

        int w = width - 1;
        int h = height - 1;
        g2.setPaint(getShadowInnerColor(c));
        fillTopLeft(g2, w, h, ins);

        g2.setPaint(getHighlightInnerColor(c));
        g2.fill(makeBottomRightShape(w, h, ins));

        g2.setPaint(getShadowOuterColor(c));
        drawRectLine(g2, w, h, ins);

        g2.dispose();
    }
    private void fillTopLeft(Graphics2D g2, int w, int h, Insets i) {
        g2.fillRect(0, 0, w, i.top);
        g2.fillRect(0, 0, i.left, h);
    }
    private Shape makeBottomRightShape(int w, int h, Insets i) {
        Path2D p = new Path2D.Double();
        p.moveTo(w, 0);
        p.lineTo(w - i.right, i.top);
        p.lineTo(w - i.right, h - i.bottom);
        p.lineTo(i.left, h - i.bottom);
        p.lineTo(0, h);
        p.lineTo(w, h);
        p.closePath();
        return p;
    }
    private void drawRectLine(Graphics2D g2, int w, int h, Insets i) {
        g2.drawRect(0, 0, w, h);
        g2.drawRect(i.left, i.top, w - i.left - i.right, h - i.top - i.bottom);
        g2.drawLine(0, 0, i.left, i.top);
        g2.drawLine(w, 0, w - i.right, i.top);
        g2.drawLine(0, h, i.left, h - i.bottom);
        g2.drawLine(w, h, w - i.right, h - i.bottom);
    }
}
