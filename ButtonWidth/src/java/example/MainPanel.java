package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.util.Arrays;
import java.util.List;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());

        JComponent box1 = Box.createHorizontalBox();
        box1.add(Box.createHorizontalGlue());
        box1.add(new JButton("default"));
        box1.add(Box.createHorizontalStrut(5));
        box1.add(new JButton("a"));
        box1.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 5));

        JComponent box2 = createRightAlignButtonBox2(Arrays.asList(new JButton("getPreferredSize"), new JButton("xxx")),    120, 5);
        JComponent box3 = createRightAlignButtonBox3(Arrays.asList(new JButton("Spring+Box"),       new JButton("Layout")), 100, 5);
        JComponent box4 = createRightAlignButtonBox4(Arrays.asList(new JButton("SpringLayout"),     new JButton("gap:2")),  120, 2);
        JComponent box5 = createRightAlignButtonBox5(Arrays.asList(new JButton("GridLayout+Box"),   new JButton("gap:2")),  2);
        JComponent box6 = createRightAlignButtonBox6(Arrays.asList(new JButton("GridBugLayout"),    new JButton("gap:2")),  120, 2);

        Box box = Box.createVerticalBox();
        for (Component c: Arrays.asList(box6, box5, box4, box3, box2, box1)) {
            box.add(new JSeparator());
            box.add(c);
        }
        add(box, BorderLayout.SOUTH);
        setPreferredSize(new Dimension(320, 240));
    }

    private static JComponent createRightAlignButtonBox6(List<JButton> list, int buttonWidth, int gap) {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(0, gap, 0, 0);
        for (JButton b: list) {
            c.ipadx = buttonWidth - b.getPreferredSize().width;
            p.add(b, c);
        }
        p.setBorder(BorderFactory.createEmptyBorder(gap, gap, gap, gap));
        JPanel pp = new JPanel(new BorderLayout());
        pp.add(p, BorderLayout.EAST);
        return pp;
    }

    private static JComponent createRightAlignButtonBox5(List<JButton> list, int gap) {
        JPanel p = new JPanel(new GridLayout(1, list.size(), gap, gap)) {
            @Override public Dimension getMaximumSize() {
                return super.getPreferredSize();
            }
        };
        for (JButton b: list) {
            p.add(b);
        }
        Box box = Box.createHorizontalBox();
        box.add(Box.createHorizontalGlue());
        box.add(p);
        box.setBorder(BorderFactory.createEmptyBorder(gap, gap, gap, gap));
        return box;
    }

    private static JComponent createRightAlignButtonBox4(final List<JButton> list, final int buttonWidth, final int gap) {
        SpringLayout layout = new SpringLayout();
        JPanel p = new JPanel(layout) {
            @Override public Dimension getPreferredSize() {
                int maxHeight = 0;
                for (JButton b: list) {
                    maxHeight = Math.max(maxHeight, b.getPreferredSize().height);
                }
                return new Dimension(buttonWidth * list.size() + gap + gap, maxHeight + gap + gap);
            }
        };
        Spring x     = layout.getConstraint(SpringLayout.WIDTH, p);
        Spring y     = Spring.constant(gap);
        Spring g     = Spring.minus(Spring.constant(gap));
        Spring width = Spring.constant(buttonWidth);
        for (JButton b: list) {
            SpringLayout.Constraints constraints = layout.getConstraints(b);
            x = Spring.sum(x, g);
            constraints.setConstraint(SpringLayout.EAST, x);
            constraints.setY(y);
            constraints.setWidth(width);
            p.add(b);
            x = Spring.sum(x, Spring.minus(width));
        }
        return p;
    }

    private static JComponent createRightAlignButtonBox3(final List<JButton> list, final int buttonWidth, final int gap) {
        SpringLayout layout = new SpringLayout();
        JPanel p = new JPanel(layout) {
            @Override public Dimension getPreferredSize() {
                int maxHeight = 0;
                for (JButton b: list) {
                    maxHeight = Math.max(maxHeight, b.getPreferredSize().height);
                }
                return new Dimension(buttonWidth * list.size() + gap + gap, maxHeight + gap + gap);
            }
        };
        SpringLayout.Constraints pCons = layout.getConstraints(p);
        //pCons.setConstraint(SpringLayout.SOUTH, Spring.constant(p.getPreferredSize().height));
        pCons.setConstraint(SpringLayout.EAST, Spring.constant((buttonWidth + gap) * list.size()));

        Spring x     = Spring.constant(0);
        Spring y     = Spring.constant(gap);
        Spring g     = Spring.constant(gap);
        Spring width = Spring.constant(buttonWidth);
        for (JButton b: list) {
            SpringLayout.Constraints constraints = layout.getConstraints(b);
            constraints.setX(x);
            constraints.setY(y);
            constraints.setWidth(width);
            p.add(b);
            x = Spring.sum(x, width);
            x = Spring.sum(x, g);
        }

        Box box = Box.createHorizontalBox();
        box.add(Box.createHorizontalGlue());
        box.add(p);
        return box;
    }

    private static JComponent createRightAlignButtonBox2(final List<JButton> list, final int buttonWidth, int gap) {
        JComponent box = new JPanel() {
            @Override public void updateUI() {
                for (JButton b: list) {
                    b.setPreferredSize(null);
                }
                super.updateUI();
                EventQueue.invokeLater(() -> {
                    int maxHeight = 0;
                    for (JButton b: list) {
                        maxHeight = Math.max(maxHeight, b.getPreferredSize().height);
                    }
                    Dimension d = new Dimension(buttonWidth, maxHeight);
                    for (JButton b: list) {
                        b.setPreferredSize(d);
                    }
                    revalidate();
                });
            }
        };
        box.setLayout(new BoxLayout(box, BoxLayout.X_AXIS));
        box.add(Box.createHorizontalGlue());
        for (JButton b: list) {
            box.add(b);
            box.add(Box.createHorizontalStrut(gap));
        }
        box.setBorder(BorderFactory.createEmptyBorder(gap, 0, gap, 0));
        return box;
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
