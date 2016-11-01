package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.stream.IntStream;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private final JComboBox<String> combo01 = new JComboBox<>(makeModel());
    private final JComboBox<String> combo02 = new JComboBox<>(makeModel());

    public MainPanel() {
        super(new BorderLayout());

        Action up = new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                JComboBox c = (JComboBox) e.getSource();
                int i = c.getSelectedIndex();
                c.setSelectedIndex(i == 0 ? c.getItemCount() - 1 : i - 1);
            }
        };
        Action down = new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                JComboBox c = (JComboBox) e.getSource();
                int i = c.getSelectedIndex();
                c.setSelectedIndex(i == c.getItemCount() - 1 ? 0 : i + 1);
            }
        };
        ActionMap am = combo02.getActionMap();
        am.put("myUp",   up);
        am.put("myDown", down);

        InputMap im = combo02.getInputMap();
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0),   "myUp");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "myDown");

        Box box = Box.createVerticalBox();
        box.add(createPanel(combo01, "default:"));
        box.add(Box.createVerticalStrut(5));
        box.add(createPanel(combo02, "loop:"));
        box.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        add(box, BorderLayout.NORTH);
        setPreferredSize(new Dimension(320, 240));
    }
    private static JComponent createPanel(JComponent cmp, String str) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(str));
        panel.add(cmp);
        return panel;
    }
    private static ComboBoxModel<String> makeModel() {
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
        IntStream.range(0, 10).forEach(i -> model.addElement("item: " + i));
        return model;
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
