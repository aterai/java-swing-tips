package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private static final float[] DEFAULT_DASH_ARRAY = {1f};
    private final JTextField field = new JTextField("1f, 1f, 5f, 1f");
    private final JLabel label;
    private transient BasicStroke dashedStroke;

    private float[] getDashArray() {
        String[] slist = field.getText().split(",");
        if (slist.length == 0) {
            return DEFAULT_DASH_ARRAY;
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
            return DEFAULT_DASH_ARRAY;
        }
        return i == 0 ? DEFAULT_DASH_ARRAY : list;
    }
    public MainPanel() {
        super(new BorderLayout());
        JButton button = new JButton(new AbstractAction("Change") {
            @Override public void actionPerformed(ActionEvent e) {
                dashedStroke = null;
                label.repaint();
            }
        });
        label = new JLabel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (Objects.isNull(dashedStroke)) {
                    dashedStroke = new BasicStroke(5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, getDashArray(), 0f);
                }
                Insets i = getInsets();
                int w = getWidth();
                int h = getHeight() / 2;
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setStroke(dashedStroke);
                g2.drawLine(i.left, h, w - i.right, h);
                g2.dispose();
            }
        };
        label.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));

        JPanel p = new JPanel(new BorderLayout(5, 5));
        p.add(field);
        p.add(button, BorderLayout.EAST);
        p.setBorder(BorderFactory.createTitledBorder("Comma Separated Values"));

        add(p, BorderLayout.NORTH);
        add(label);
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
