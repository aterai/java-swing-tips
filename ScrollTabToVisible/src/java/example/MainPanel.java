package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@
import java.awt.*;
import java.util.stream.IntStream;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private final JCheckBox check = new JCheckBox("setSelectedIndex");
    public MainPanel() {
        super(new BorderLayout());

        JTabbedPane jtp = new JTabbedPane();
        jtp.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        IntStream.range(0, 100).forEach(i -> jtp.addTab("title" + i, new JLabel("label" + i)));

        JSlider slider = new JSlider(0, jtp.getTabCount() - 1, 50);
        slider.setMajorTickSpacing(10);
        slider.setMinorTickSpacing(5);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        slider.addChangeListener(e -> {
            int i = ((JSlider) e.getSource()).getValue();
            if (check.isSelected()) {
                jtp.setSelectedIndex(i);
            }
            scrollTabAt(jtp, i);
        });
        check.setHorizontalAlignment(SwingConstants.RIGHT);
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder("Scroll Slider"));
        p.add(check, BorderLayout.SOUTH);
        p.add(slider, BorderLayout.NORTH);
        add(p, BorderLayout.NORTH);
        add(jtp);
        setPreferredSize(new Dimension(320, 240));
    }
    private static void scrollTabAt(JTabbedPane tp, int index) {
        Component cmp = null;
        for (Component c: tp.getComponents()) {
            if ("TabbedPane.scrollableViewport".equals(c.getName())) {
                cmp = c;
                break;
            }
        }
        if (cmp instanceof JViewport) {
            JViewport viewport = (JViewport) cmp;
            for (int i = 0; i < tp.getTabCount(); i++) {
                tp.setForegroundAt(i, i == index ? Color.RED : Color.BLACK);
            }
            Dimension d = tp.getSize();
            Rectangle r = tp.getBoundsAt(index);
            int gw = (d.width - r.width) / 2;
            r.grow(gw, 0);
            viewport.scrollRectToVisible(r);
        }
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
