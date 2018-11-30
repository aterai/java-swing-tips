package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@
import java.awt.*;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout(5, 5));

        JSlider slider1 = makeSlider();
        setCurrentLabelListener(slider1);

        @SuppressWarnings("JdkObsolete")
        Dictionary<Integer, Component> labelTable = new Hashtable<>();
        // @SuppressWarnings("PMD.ReplaceHashtableWithMap")
        // Hashtable labelTable = slider2.createStandardLabels(1);
        List<Component> list = Stream.of("A", "B", "C", "D", "E")
            .map(JLabel::new).collect(Collectors.toList());
        IntStream.range(0, list.size()).boxed()
            .forEach(i -> labelTable.put(i, list.get(i)));

        JSlider slider2 = new JSlider(0, 4, 0);
        setCurrentLabelListener(slider2);
        slider2.setLabelTable(labelTable);
        slider2.setSnapToTicks(true);
        slider2.setPaintTicks(true);
        slider2.setPaintLabels(true);
        // slider2.setMajorTickSpacing(1);
        ((Component) labelTable.get(0)).setForeground(Color.RED);

        Box box = Box.createVerticalBox();
        box.add(Box.createVerticalStrut(5));
        box.add(makeTitledPanel("Default", makeSlider()));
        box.add(Box.createVerticalStrut(5));
        box.add(makeTitledPanel("setMajorTickSpacing(10)", slider1));
        box.add(Box.createVerticalStrut(5));
        box.add(makeTitledPanel("setMajorTickSpacing(0)", slider2));
        box.add(Box.createVerticalGlue());
        add(box);
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        setPreferredSize(new Dimension(320, 240));
        EventQueue.invokeLater(() -> {
            slider1.getModel().setValue(40);
            slider1.repaint();
        });
    }
    private static JSlider makeSlider() {
        JSlider slider = new JSlider(0, 100);
        slider.setMajorTickSpacing(10);
        // slider.setMinorTickSpacing(5);
        slider.setPaintLabels(true);
        slider.setSnapToTicks(true);
        slider.setPaintTicks(true);
        return slider;
    }
    private static void setCurrentLabelListener(JSlider slider) {
        slider.getModel().addChangeListener(new ChangeListener() {
            private int prev = -1;
            private void resetForeground(Object o, Color c) {
                if (o instanceof Component) {
                    ((Component) o).setForeground(c);
                }
            }
            @Override public void stateChanged(ChangeEvent e) {
                BoundedRangeModel m = (BoundedRangeModel) e.getSource();
                int i = m.getValue();
                if ((slider.getMajorTickSpacing() == 0 || i % slider.getMajorTickSpacing() == 0) && i != prev) {
                    Dictionary<?, ?> dictionary = slider.getLabelTable();
                    resetForeground(dictionary.get(i), Color.RED);
                    resetForeground(dictionary.get(prev), Color.BLACK);
                    slider.repaint();
                    prev = i;
                }
            }
        });
    }
    private static Component makeTitledPanel(String title, Component c) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder(title));
        p.add(c);
        return p;
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
