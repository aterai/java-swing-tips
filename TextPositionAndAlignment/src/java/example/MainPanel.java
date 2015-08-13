package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private final JComboBox<? extends Enum> verticalAlignmentChoices      = new JComboBox<>(Vertical.values());
    private final JComboBox<? extends Enum> verticalTextPositionChoices   = new JComboBox<>(Vertical.values());
    private final JComboBox<? extends Enum> horizontalAlignmentChoices    = new JComboBox<>(Horizontal.values());
    private final JComboBox<? extends Enum> horizontalTextPositionChoices = new JComboBox<>(Horizontal.values());
    private final JLabel label = new JLabel("Test Test", new StarburstIcon(), SwingConstants.CENTER);

    public MainPanel() {
        super(new BorderLayout());
        label.setOpaque(true);
        label.setBackground(Color.WHITE);

        //default
        verticalAlignmentChoices.setSelectedItem(Vertical.CENTER);
        verticalTextPositionChoices.setSelectedItem(Vertical.CENTER);
        horizontalAlignmentChoices.setSelectedItem(Horizontal.CENTER);
        horizontalTextPositionChoices.setSelectedItem(Horizontal.TRAILING);

        ItemListener il = new ItemListener() {
            @Override public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    Vertical v1 = (Vertical) verticalAlignmentChoices.getSelectedItem();
                    label.setVerticalAlignment(v1.alignment);
                    Vertical v2 = (Vertical) verticalTextPositionChoices.getSelectedItem();
                    label.setVerticalTextPosition(v2.alignment);
                    Horizontal h1 = (Horizontal) horizontalAlignmentChoices.getSelectedItem();
                    label.setHorizontalAlignment(h1.alignment);
                    Horizontal h2 = (Horizontal) horizontalTextPositionChoices.getSelectedItem();
                    label.setHorizontalTextPosition(h2.alignment);
                    label.repaint();
                }
            }
        };
        verticalAlignmentChoices.addItemListener(il);
        verticalTextPositionChoices.addItemListener(il);
        horizontalAlignmentChoices.addItemListener(il);
        horizontalTextPositionChoices.addItemListener(il);

        JPanel p1 = new JPanel(new BorderLayout());
        p1.setBorder(BorderFactory.createTitledBorder("JLabel Test"));
        p1.add(label);

        JPanel p2 = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridheight = 1;
        c.gridx   = 0;
        c.insets  = new Insets(5, 5, 5, 0);
        c.anchor  = GridBagConstraints.WEST;
        c.gridy   = 0; p2.add(new JLabel("setVerticalAlignment:"), c);
        c.gridy   = 1; p2.add(new JLabel("setVerticalTextPosition:"), c);
        c.gridy   = 2; p2.add(new JLabel("setHorizontalAlignment:"), c);
        c.gridy   = 3; p2.add(new JLabel("setHorizontalTextPosition:"), c);
        c.gridx   = 1;
        c.weightx = 1d;
        c.fill    = GridBagConstraints.HORIZONTAL;
        c.gridy   = 0; p2.add(verticalAlignmentChoices, c);
        c.gridy   = 1; p2.add(verticalTextPositionChoices, c);
        c.gridy   = 2; p2.add(horizontalAlignmentChoices, c);
        c.gridy   = 3; p2.add(horizontalTextPositionChoices, c);

        add(p1);
        add(p2, BorderLayout.NORTH);
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
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

enum Vertical {
    TOP(SwingConstants.TOP),
    CENTER(SwingConstants.CENTER),
    BOTTOM(SwingConstants.BOTTOM);
    public final int alignment;
    private Vertical(int alignment) {
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
    private Horizontal(int alignment) {
        this.alignment = alignment;
    }
}

class StarburstIcon implements Icon {
    private static final int R2 = 24;
    private static final int R1 = 20;
    private static final int VC = 18;
    private final Shape star;
    public StarburstIcon() {
        double agl = 0d;
        double add = 2 * Math.PI / (VC * 2);
        Path2D.Double p = new Path2D.Double();
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
    @Override public int getIconWidth() {
        return 2 * R2;
    }
    @Override public int getIconHeight() {
        return 2 * R2;
    }
    @Override public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.translate(x, y);
        g2.setPaint(Color.YELLOW);
        g2.fill(star);
        g2.setPaint(Color.BLACK);
        g2.draw(star);
        g2.dispose();
    }
}
