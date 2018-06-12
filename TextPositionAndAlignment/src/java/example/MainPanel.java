package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.Arrays;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());
        JLabel label = new JLabel("Test Test", new StarburstIcon(), SwingConstants.CENTER);
        label.setOpaque(true);
        label.setBackground(Color.WHITE);

        JComboBox<Vertical> verticalAlignmentChoices = new JComboBox<>(Vertical.values());
        verticalAlignmentChoices.setSelectedItem(Vertical.CENTER);

        JComboBox<Vertical> verticalTextPositionChoices = new JComboBox<>(Vertical.values());
        verticalTextPositionChoices.setSelectedItem(Vertical.CENTER);

        JComboBox<Horizontal> horizontalAlignmentChoices = new JComboBox<>(Horizontal.values());
        horizontalAlignmentChoices.setSelectedItem(Horizontal.CENTER);

        JComboBox<Horizontal> horizontalTextPositionChoices = new JComboBox<>(Horizontal.values());
        horizontalTextPositionChoices.setSelectedItem(Horizontal.TRAILING);

        ItemListener listener = e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                label.setVerticalAlignment(getSelectedItem(verticalAlignmentChoices).alignment);
                label.setVerticalTextPosition(getSelectedItem(verticalTextPositionChoices).alignment);
                label.setHorizontalAlignment(getSelectedItem(horizontalAlignmentChoices).alignment);
                label.setHorizontalTextPosition(getSelectedItem(horizontalTextPositionChoices).alignment);
                label.repaint();
            }
        };
        Arrays.asList(verticalAlignmentChoices, verticalTextPositionChoices, horizontalAlignmentChoices, horizontalTextPositionChoices)
            .forEach(c -> c.addItemListener(listener));

        JPanel p1 = new JPanel(new BorderLayout());
        p1.setBorder(BorderFactory.createTitledBorder("JLabel Test"));
        p1.add(label);

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.insets = new Insets(5, 5, 5, 0);
        c.anchor = GridBagConstraints.LINE_END;

        JPanel p2 = new JPanel(new GridBagLayout());
        p2.add(new JLabel("setVerticalAlignment:"), c);
        p2.add(new JLabel("setVerticalTextPosition:"), c);
        p2.add(new JLabel("setHorizontalAlignment:"), c);
        p2.add(new JLabel("setHorizontalTextPosition:"), c);

        c.gridx = 1;
        c.weightx = 1d;
        c.fill = GridBagConstraints.HORIZONTAL;
        p2.add(verticalAlignmentChoices, c);
        p2.add(verticalTextPositionChoices, c);
        p2.add(horizontalAlignmentChoices, c);
        p2.add(horizontalTextPositionChoices, c);

        add(p1);
        add(p2, BorderLayout.NORTH);
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        setPreferredSize(new Dimension(320, 240));
    }
    private static <E> E getSelectedItem(JComboBox<E> combo) {
        return combo.getItemAt(combo.getSelectedIndex());
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

enum Vertical {
    TOP(SwingConstants.TOP),
    CENTER(SwingConstants.CENTER),
    BOTTOM(SwingConstants.BOTTOM);
    public final int alignment;
    Vertical(int alignment) {
        this.alignment = alignment;
    }
}

enum Horizontal {
    LEFT(SwingConstants.LEFT),
    CENTER(SwingConstants.CENTER),
    RIGHT(SwingConstants.RIGHT),
    LEADING(SwingConstants.LEADING),
    TRAILING(SwingConstants.TRAILING);
    public final int alignment;
    Horizontal(int alignment) {
        this.alignment = alignment;
    }
}

class StarburstIcon implements Icon {
    private static final int R2 = 24;
    private static final int R1 = 20;
    private static final int VC = 18;
    private final Shape star;
    protected StarburstIcon() {
        double agl = 0d;
        double add = 2 * Math.PI / (VC * 2);
        Path2D p = new Path2D.Double();
        p.moveTo(R2 * 1, R2 * 0);
        for (int i = 0; i < VC * 2 - 1; i++) {
            agl += add;
            if (i % 2 == 0) {
                p.lineTo(R1 * Math.cos(agl), R1 * Math.sin(agl));
            } else {
                p.lineTo(R2 * Math.cos(agl), R2 * Math.sin(agl));
            }
        }
        p.closePath();
        AffineTransform at = AffineTransform.getRotateInstance(-Math.PI / 2, R2, 0);
        star = new Path2D.Double(p, at);
    }
    @Override public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.translate(x, y);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setPaint(Color.YELLOW);
        g2.fill(star);
        g2.setPaint(Color.BLACK);
        g2.draw(star);
        g2.dispose();
    }
    @Override public int getIconWidth() {
        return 2 * R2;
    }
    @Override public int getIconHeight() {
        return 2 * R2;
    }
}
