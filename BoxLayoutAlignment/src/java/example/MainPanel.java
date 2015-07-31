package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

public class MainPanel extends JPanel {
    private final JPanel panel = new JPanel(new GridLayout(1, 2, 5, 5));
    private final JSpinner spinner = new JSpinner(new SpinnerNumberModel(9, 0, 100, 1));
    private Integer value = 9;

    public MainPanel() {
        super(new BorderLayout(5, 5));

        final JPanel p1 = new TestPanel();
        p1.setLayout(new BoxLayout(p1, BoxLayout.X_AXIS));
        p1.add(Box.createHorizontalGlue());
        p1.add(makeLabel());
        p1.add(Box.createHorizontalGlue());

        final JPanel p2 = new TestPanel();
        p2.setLayout(new BoxLayout(p2, BoxLayout.Y_AXIS));
        p2.add(Box.createVerticalGlue());
        p2.add(makeLabel());
        p2.add(Box.createVerticalGlue());

        for (JPanel c: Arrays.asList(p1, p2)) {
            c.setBackground(Color.WHITE);
            panel.add(c);
        }

        spinner.addChangeListener(new ChangeListener() {
            @Override public void stateChanged(ChangeEvent e) {
                JSpinner s = (JSpinner) e.getSource();
                value = (Integer) s.getValue();
                p1.revalidate();
                p2.revalidate();
            }
        });

        JPanel np = new JPanel(new GridLayout(1, 2));
        np.add(new JLabel("BoxLayout.X_AXIS", SwingConstants.CENTER));
        np.add(new JLabel("BoxLayout.Y_AXIS", SwingConstants.CENTER));

        JPanel sp = new JPanel(new BorderLayout());
        sp.add(new JLabel("MinimumSize: "), BorderLayout.WEST);
        sp.add(spinner);

        add(np, BorderLayout.NORTH);
        add(panel);
        add(sp, BorderLayout.SOUTH);
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        setPreferredSize(new Dimension(320, 240));
    }

    private JLabel makeLabel() {
        JLabel l = new JLabel("abc") {
            @Override public Dimension getPreferredSize() {
                return new Dimension(50, 50);
            }
            @Override public Dimension getMinimumSize() {
                Dimension d = super.getMinimumSize();
                if (Objects.nonNull(d)) {
                    d.width = value;
                    d.height = value;
                }
                return d;
            }
        };
        l.setOpaque(true);
        l.setBackground(Color.ORANGE);
        l.setFont(l.getFont().deriveFont(Font.PLAIN));
        l.setAlignmentX(Component.CENTER_ALIGNMENT);
        l.setAlignmentY(Component.CENTER_ALIGNMENT);
        l.setVerticalAlignment(SwingConstants.CENTER);
        l.setVerticalTextPosition(SwingConstants.CENTER);
        l.setHorizontalAlignment(SwingConstants.CENTER);
        l.setHorizontalTextPosition(SwingConstants.CENTER);
        return l;
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

class TestPanel extends JPanel {
    @Override public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawLine(0, getHeight() / 2, getWidth(), getHeight() / 2);
        g.drawLine(getWidth() / 2, 0, getWidth() / 2, getHeight());
    }
}
