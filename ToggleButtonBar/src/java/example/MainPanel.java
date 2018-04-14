package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.geom.*;
import java.util.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super();

        add(makeToggleButtonBar(0xFF7400, true));
        add(makeToggleButtonBar(0x555555, false));
        add(makeToggleButtonBar(0x006400, true));
        add(makeToggleButtonBar(0x8B0000, false));
        add(makeToggleButtonBar(0x001E43, true));

        setPreferredSize(new Dimension(320, 240));
    }
    private static AbstractButton makeButton(String title) {
        AbstractButton b = new JRadioButton(title);
        // b.setVerticalAlignment(SwingConstants.CENTER);
        // b.setVerticalTextPosition(SwingConstants.CENTER);
        // b.setHorizontalAlignment(SwingConstants.CENTER);
        b.setHorizontalTextPosition(SwingConstants.CENTER);
        b.setBorder(BorderFactory.createEmptyBorder());
        b.setContentAreaFilled(false);
        b.setFocusPainted(false);
        // b.setBackground(new Color(cc));
        b.setForeground(Color.WHITE);
        return b;
    }
    private static Component makeToggleButtonBar(int cc, boolean round) {
        ButtonGroup bg = new ButtonGroup();
        JPanel p = new JPanel(new GridLayout(1, 0, 0, 0));
        p.setBorder(BorderFactory.createTitledBorder(String.format("Color: #%06X", cc)));
        Color color = new Color(cc);
        for (AbstractButton b: Arrays.asList(makeButton("left"), makeButton("center"), makeButton("right"))) {
            b.setBackground(color);
            if (round) {
                b.setIcon(new ToggleButtonBarCellIcon());
            } else {
                b.setIcon(new CellIcon());
            }
            bg.add(b);
            p.add(b);
        }
        return p;
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

class CellIcon implements Icon {
    // http://weboook.blog22.fc2.com/blog-entry-342.html
    // Webpark 2012.11.15
    private static final Color TL = new Color(1f, 1f, 1f, .2f);
    private static final Color BR = new Color(0f, 0f, 0f, .2f);
    private static final Color ST = new Color(1f, 1f, 1f, .4f);
    private static final Color SB = new Color(1f, 1f, 1f, .1f);

    @Override public void paintIcon(Component c, Graphics g, int x, int y) {
        int w = c.getWidth();
        int h = c.getHeight();
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.translate(x, y);

        Color ssc = TL;
        Color bgc = BR;
        if (c instanceof AbstractButton) {
            ButtonModel m = ((AbstractButton) c).getModel();
            if (m.isSelected() || m.isRollover()) {
                ssc = ST;
                bgc = SB;
            }
        }

        g2.setPaint(c.getBackground());
        g2.fillRect(0, 0, w, h);

        g2.setPaint(new GradientPaint(0, 0, ssc, 0, h, bgc, true));
        g2.fillRect(0, 0, w, h);

        g2.setPaint(TL);
        g2.fillRect(0, 0, 1, h);
        g2.setPaint(BR);
        g2.fillRect(w, 0, 1, h);

        g2.dispose();
    }
    @Override public int getIconWidth() {
        return 80;
    }
    @Override public int getIconHeight() {
        return 20;
    }
}

class ToggleButtonBarCellIcon implements Icon {
    private static final Color TL = new Color(1f, 1f, 1f, .2f);
    private static final Color BR = new Color(0f, 0f, 0f, .2f);
    private static final Color ST = new Color(1f, 1f, 1f, .4f);
    private static final Color SB = new Color(1f, 1f, 1f, .1f);

    @Override public void paintIcon(Component c, Graphics g, int x, int y) {
        Container parent = c.getParent();
        if (Objects.isNull(parent)) {
            return;
        }
        int r = 8;
        int w = c.getWidth();
        int h = c.getHeight() - 1;

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Path2D p = new Path2D.Double();
        if (c == parent.getComponent(0)) {
            // :first-child
            p.moveTo(x, y + r);
            p.quadTo(x, y, x + r, y);
            p.lineTo(x + w, y);
            p.lineTo(x + w, y + h);
            p.lineTo(x + r, y + h);
            p.quadTo(x, y + h, x, y + h - r);
        } else if (c == parent.getComponent(parent.getComponentCount() - 1)) {
            // :last-child
            w--;
            p.moveTo(x, y);
            p.lineTo(x + w - r, y);
            p.quadTo(x + w, y, x + w, y + r);
            p.lineTo(x + w, y + h - r);
            p.quadTo(x + w, y + h, x + w - r, y + h);
            p.lineTo(x, y + h);
        } else {
            p.moveTo(x, y);
            p.lineTo(x + w, y);
            p.lineTo(x + w, y + h);
            p.lineTo(x, y + h);
        }
        p.closePath();

        Color ssc = TL;
        Color bgc = BR;
        if (c instanceof AbstractButton) {
            ButtonModel m = ((AbstractButton) c).getModel();
            if (m.isSelected() || m.isRollover()) {
                ssc = ST;
                bgc = SB;
            }
        }

        Area area = new Area(p);
        g2.setPaint(c.getBackground());
        g2.fill(area);
        g2.setPaint(new GradientPaint(x, y, ssc, x, y + h, bgc, true));
        g2.fill(area);
        g2.setPaint(BR);
        g2.draw(area);
        g2.dispose();
    }
    @Override public int getIconWidth() {
        return 80;
    }
    @Override public int getIconHeight() {
        return 20;
    }
}
