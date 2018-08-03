package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@
import java.awt.*;
import java.awt.geom.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.TitledBorder;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());
        Box box = Box.createVerticalBox();
        box.add(new TitledSeparator("TitledBorder", 2, TitledBorder.DEFAULT_POSITION));
        box.add(new JCheckBox("JCheckBox 0"));
        box.add(new JCheckBox("JCheckBox 1"));
        box.add(Box.createVerticalStrut(10));

        box.add(new TitledSeparator("TitledBorder ABOVE TOP", new Color(100, 180, 200), 2, TitledBorder.ABOVE_TOP));
        box.add(new JCheckBox("JCheckBox 2"));
        box.add(new JCheckBox("JCheckBox 3"));
        box.add(Box.createVerticalStrut(10));

        box.add(new JSeparator());
        box.add(new JCheckBox("JCheckBox 4"));
        box.add(new JCheckBox("JCheckBox 5"));
        // box.add(Box.createVerticalStrut(8));

        add(box, BorderLayout.NORTH);
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
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

class TitledSeparator extends JLabel {
    protected final String title;
    protected final Color target;
    protected final int height;
    protected final int titlePosition;
    protected TitledSeparator(String title, int height, int titlePosition) {
        this(title, null, height, titlePosition);
    }
    protected TitledSeparator(String title, Color target, int height, int titlePosition) {
        super();
        this.title = title;
        this.target = target;
        this.height = height;
        this.titlePosition = titlePosition;
        updateBorder();
    }
    private void updateBorder() {
        Icon icon = new TitledSeparatorIcon();
        setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createMatteBorder(height, 0, 0, 0, icon), title,
            TitledBorder.DEFAULT_JUSTIFICATION, titlePosition));
    }
    @Override public Dimension getMaximumSize() {
        return new Dimension(Short.MAX_VALUE, super.getPreferredSize().height);
    }
    @Override public void updateUI() {
        super.updateUI();
        updateBorder();
    }
    private class TitledSeparatorIcon implements Icon {
        private int width = -1;
        private Paint painter1;
        private Paint painter2;
        @Override public void paintIcon(Component c, Graphics g, int x, int y) {
            int w = c.getWidth();
            if (w != width || Objects.isNull(painter1) || Objects.isNull(painter2)) {
                width = w;
                Point2D start = new Point2D.Float();
                Point2D end = new Point2D.Float(width, 0);
                float[] dist = {0f, 1f};
                Color ec = Optional.ofNullable(getBackground()).orElse(UIManager.getColor("Panel.background"));
                Color sc = Optional.ofNullable(target).orElse(ec);
                painter1 = new LinearGradientPaint(start, end, dist, new Color[] {sc.darker(), ec});
                painter2 = new LinearGradientPaint(start, end, dist, new Color[] {sc.brighter(), ec});
            }
            int h = getIconHeight() / 2;
            Graphics2D g2 = (Graphics2D) g.create();
            // XXX: g2.translate(x, y);
            g2.setPaint(painter1);
            g2.fillRect(x, y, width, getIconHeight());
            g2.setPaint(painter2);
            g2.fillRect(x, y + h, width, getIconHeight() - h);
            g2.dispose();
        }
        @Override public int getIconWidth() {
            return 200; // dummy width
        }
        @Override public int getIconHeight() {
            return height;
        }
    }
}
