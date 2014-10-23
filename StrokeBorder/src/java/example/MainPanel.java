package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private final JComboBox<? extends Enum> joinCombo   = new JComboBox<>(JoinStyle.values());
    private final JComboBox<? extends Enum> endcapCombo = new JComboBox<>(EndCapStyle.values());
    private final JTextField field = new JTextField("10, 20");
    private final JLabel label = new JLabel();
    public  final JButton button;
    private float[] getDashArray() {
        StringTokenizer st = new StringTokenizer(field.getText(), ",");
        float[] list = new float[st.countTokens()];
        int i = 0;
        try {
            while (st.hasMoreTokens()) {
                list[i] = Float.valueOf(st.nextToken());
                i++;
            }
        } catch (NumberFormatException nfe) {
            Toolkit.getDefaultToolkit().beep();
            JOptionPane.showMessageDialog(label, "Invalid input.\n" + nfe.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        if (i == 0) {
            list = new float[] {1f};
        }
        return list;
    }

    public MainPanel() {
        super(new BorderLayout());
        button = new JButton(new AbstractAction("Change") {
            @Override public void actionPerformed(ActionEvent ae) {
                BasicStroke dashedStroke = new BasicStroke(5f,
                    ((EndCapStyle) endcapCombo.getSelectedItem()).style,
                    ((JoinStyle) joinCombo.getSelectedItem()).style,
                    5f, getDashArray(), 0f);
                label.setBorder(BorderFactory.createStrokeBorder(dashedStroke, Color.RED));
            }
        });

        JPanel p = new JPanel(new BorderLayout(2, 2));
        p.add(field); p.add(button, BorderLayout.EAST);
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
        } catch (ClassNotFoundException | InstantiationException |
                 IllegalAccessException | UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        }
        MainPanel p = new MainPanel();
        JFrame frame = new JFrame("@title@");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(p);
        frame.getRootPane().setDefaultButton(p.button);
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
    private JoinStyle(int style) {
        this.style = style;
    }
}

enum EndCapStyle {
    CAP_BUTT(BasicStroke.CAP_BUTT),
    CAP_ROUND(BasicStroke.CAP_ROUND),
    CAP_SQUARE(BasicStroke.CAP_SQUARE);
    public final int style;
    private EndCapStyle(int style) {
        this.style = style;
    }
}
