package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@
import java.awt.*;
import java.util.Arrays;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private static final float[] DEFAULT_DASHARRAY = {1f};
    private final JComboBox<JoinStyle> joinCombo = new JComboBox<>(JoinStyle.values());
    private final JComboBox<EndCapStyle> endcapCombo = new JComboBox<>(EndCapStyle.values());
    private final JTextField field = new JTextField("10, 20");
    private final JLabel label = new JLabel();
    private final JButton button = new JButton("Change");
    private float[] getDashArray() {
        // String[] slist = field.getText().split(","); // ErrorProne: StringSplitter
        String[] slist = Arrays.stream(field.getText().split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .toArray(String[]::new);
        if (slist.length == 0) {
            return DEFAULT_DASHARRAY;
        }
        float[] list = new float[slist.length];
        int i = 0;
        try {
            for (String s: slist) {
                String ss = s.trim();
                if (!ss.isEmpty()) {
                    list[i++] = Float.parseFloat(ss);
                }
            }
        } catch (NumberFormatException ex) {
            EventQueue.invokeLater(() -> {
                Toolkit.getDefaultToolkit().beep();
                JOptionPane.showMessageDialog(getRootPane(), "Invalid input.\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            });
            return DEFAULT_DASHARRAY;
        }
        return i == 0 ? DEFAULT_DASHARRAY : list;
    }

    public MainPanel() {
        super(new BorderLayout());
        button.addActionListener(e -> {
            int ecs = endcapCombo.getItemAt(endcapCombo.getSelectedIndex()).style;
            int js = joinCombo.getItemAt(joinCombo.getSelectedIndex()).style;
            BasicStroke dashedStroke = new BasicStroke(5f, ecs, js, 5f, getDashArray(), 0f);
            label.setBorder(BorderFactory.createStrokeBorder(dashedStroke, Color.RED));
        });

        JPanel p = new JPanel(new BorderLayout(2, 2));
        p.add(field);
        p.add(button, BorderLayout.EAST);
        p.setBorder(BorderFactory.createTitledBorder("Comma Separated Values"));

        JPanel p1 = new JPanel(new GridLayout(2, 1));
        p1.add(endcapCombo);
        p1.add(joinCombo);

        p.add(p1, BorderLayout.NORTH);

        JPanel p2 = new JPanel(new BorderLayout());
        p2.add(label);
        p2.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        add(p, BorderLayout.NORTH);
        add(p2);
        setPreferredSize(new Dimension(320, 240));

        button.doClick();
        EventQueue.invokeLater(() -> getRootPane().setDefaultButton(button));
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

enum JoinStyle {
    JOIN_BEVEL(BasicStroke.JOIN_BEVEL),
    JOIN_MITER(BasicStroke.JOIN_MITER),
    JOIN_ROUND(BasicStroke.JOIN_ROUND);
    public final int style;
    JoinStyle(int style) {
        this.style = style;
    }
}

enum EndCapStyle {
    CAP_BUTT(BasicStroke.CAP_BUTT),
    CAP_ROUND(BasicStroke.CAP_ROUND),
    CAP_SQUARE(BasicStroke.CAP_SQUARE);
    public final int style;
    EndCapStyle(int style) {
        this.style = style;
    }
}
