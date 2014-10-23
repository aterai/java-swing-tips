package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private final JComboBox<String> combo01 = new JComboBox<>(makeModel());
    private final JComboBox<String> combo02 = new JComboBox<>(makeModel());

    public MainPanel() {
        super(new BorderLayout());

        Action up = new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                int index = combo02.getSelectedIndex();
                combo02.setSelectedIndex((index == 0) ? combo02.getItemCount() - 1 : index - 1);
            }
        };
        Action down = new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                int index = combo02.getSelectedIndex();
                combo02.setSelectedIndex((index == combo02.getItemCount() - 1) ? 0 : index + 1);
            }
        };
        ActionMap amc = combo02.getActionMap();
        amc.put("myUp",   up);
        amc.put("myDown", down);

        InputMap imc = combo02.getInputMap();
        imc.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0),   "myUp");
        imc.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "myDown");

        Box box = Box.createVerticalBox();
        box.add(createPanel(combo01, "default:"));
        box.add(Box.createVerticalStrut(5));
        box.add(createPanel(combo02, "loop:"));
        box.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        add(box, BorderLayout.NORTH);
        setPreferredSize(new Dimension(320, 200));
    }
    private static JComponent createPanel(JComponent cmp, String str) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(str));
        panel.add(cmp);
        return panel;
    }
    private static ComboBoxModel<String> makeModel() {
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
        model.addElement("aaaa");
        model.addElement("aaaabbb");
        model.addElement("aaaabbbcc");
        model.addElement("1354123451234513512");
        model.addElement("bbb1");
        model.addElement("bbb12");
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
