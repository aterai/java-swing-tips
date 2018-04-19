package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());

        SpringLayout layout = new SpringLayout();
        JPanel panel = new JPanel(layout);
        JLabel l1 = new JLabel("label: 05%, 05%, 90%, 55%", SwingConstants.CENTER);
        JButton l2 = new JButton("button: 50%, 65%, 40%, 30%");
        // JLabel l2 = new JLabel("label: 50%, 65%, 40%, 30%", SwingConstants.CENTER);

        panel.setBorder(BorderFactory.createLineBorder(Color.GREEN, 10));
        l1.setOpaque(true);
        l1.setBackground(Color.ORANGE);
        l1.setBorder(BorderFactory.createLineBorder(Color.RED, 1));
        // l2.setBorder(BorderFactory.createLineBorder(Color.GREEN, 1));

        setScaleAndAdd(panel, layout, l1, .05f, .05f, .90f, .55f);
        setScaleAndAdd(panel, layout, l2, .50f, .65f, .40f, .30f);

        // addComponentListener(new ComponentAdapter() {
        //     @Override public void componentResized(ComponentEvent e) {
        //         initLayout();
        //     }
        // });
        add(panel);
        setPreferredSize(new Dimension(320, 240));
    }

    private static void setScaleAndAdd(Container parent, SpringLayout layout, Component child, float sx, float sy, float sw, float sh) {
        Spring panelw = layout.getConstraint(SpringLayout.WIDTH, parent);
        Spring panelh = layout.getConstraint(SpringLayout.HEIGHT, parent);

        SpringLayout.Constraints c = layout.getConstraints(child);
        c.setX(Spring.scale(panelw, sx));
        c.setY(Spring.scale(panelh, sy));
        c.setWidth(Spring.scale(panelw, sw));
        c.setHeight(Spring.scale(panelh, sh));

        parent.add(child);
    }

//     public void initLayout() {
//         SpringLayout layout = new SpringLayout();
//         Insets i = panel.getInsets();
//         int w = panel.getWidth()  - i.left - i.right;
//         int h = panel.getHeight() - i.top  - i.bottom;
//
//         l1.setPreferredSize(new Dimension(w * 90 / 100, h * 55 / 100));
//         l2.setPreferredSize(new Dimension(w * 40 / 100, h * 30 / 100));
//
//         layout.putConstraint(SpringLayout.WEST,  l1,  w *  5 / 100, SpringLayout.WEST,  panel);
//         layout.putConstraint(SpringLayout.NORTH, l1,  h *  5 / 100, SpringLayout.NORTH, panel);
//         layout.putConstraint(SpringLayout.WEST,  l2,  w * 50 / 100, SpringLayout.WEST,  panel);
//         layout.putConstraint(SpringLayout.SOUTH, l2, -h *  5 / 100, SpringLayout.SOUTH, panel);
//
//         panel.setLayout(layout);
//         panel.revalidate();
//     }

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
