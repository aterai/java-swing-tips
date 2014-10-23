package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private static final double A2 = 4.0;
    private MainPanel() {
        super(new BorderLayout());

        JPanel panel1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JPanel panel2 = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Insets i = getInsets();
                g.translate(i.left, i.top);
                g.setColor(Color.RED);
                int w = getWidth() - i.left - i.right;
                int px = 0;
                int py = 0;
                for (int x = 0; x < w; x++) {
                    int y = (int) Math.pow(x / A2, 2.0);
                    g.drawLine(px, py, x, y);
                    px = x;
                    py = y;
                }
                g.translate(-i.left, -i.top);
            }
        };
        panel2.setLayout(new FlowLayout() {
            @Override public void layoutContainer(Container target) {
                synchronized (target.getTreeLock()) {
                    int nmembers = target.getComponentCount();
                    if (nmembers <= 0) {
                        return;
                    }
                    Insets insets = target.getInsets();
                    int vgap = getVgap();
                    int hgap = getHgap();
                    int rowh = (target.getHeight() - insets.top - insets.bottom - vgap * 2) / nmembers;
                    int x = insets.left + hgap;
                    int y = insets.top  + vgap;
                    for (int i = 0; i < nmembers; i++) {
                        Component m = target.getComponent(i);
                        if (m.isVisible()) {
                            Dimension d = m.getPreferredSize();
                            m.setSize(d.width, d.height);
                            m.setLocation(x, y);
                            y += vgap + Math.min(rowh, d.height);
                            x = (int) (A2 * Math.sqrt(y));
                        }
                    }
                }
            }
        });

        JPanel p = new JPanel(new GridLayout(1, 2));
        p.add(initPanel("FlowLayout(LEFT)", panel1));
        p.add(initPanel("y=Math.pow(x/4.0,2.0)", panel2));
        add(p);
        setPreferredSize(new Dimension(320, 240));
    }
    private static JComponent initPanel(String title, JComponent p) {
        p.setBorder(BorderFactory.createTitledBorder(title));
        p.add(new JCheckBox("aaaaaaaaaaaaaaa"));
        p.add(new JCheckBox("bbbbbbbb"));
        p.add(new JCheckBox("ccccccccccc"));
        p.add(new JCheckBox("ddddddd"));
        p.add(new JCheckBox("eeeeeeeeeee"));
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
        } catch (ClassNotFoundException | InstantiationException |
                 IllegalAccessException | UnsupportedLookAndFeelException ex) {
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
