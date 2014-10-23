package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;

public final class MainPanel extends JPanel {
    private final JComboBox<String> comboBox = new JComboBox<>(makeModel());
    private final JSpinner spinner   = new JSpinner(new SpinnerNumberModel(comboBox.getMaximumRowCount(), -1, 1000, 1));
    public MainPanel() {
        super(new BorderLayout());
        spinner.addChangeListener(new ChangeListener() {
            @Override public void stateChanged(ChangeEvent e) {
                JSpinner source = (JSpinner) e.getSource();
                Integer newValue = (Integer) source.getValue();
                comboBox.setMaximumRowCount(newValue);
            }
        });

        Box box = Box.createVerticalBox();
        box.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        box.add(makePanel("JComboBox#setMaximumRowCount:", spinner));
        box.add(Box.createVerticalStrut(10));
        box.add(comboBox);

        add(box, BorderLayout.NORTH);
        setPreferredSize(new Dimension(320, 240));
    }
    private static JPanel makePanel(String title, JComponent c) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder(title));
        p.add(c);
        return p;
    }
    private static DefaultComboBoxModel<String> makeModel() {
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
        for (int i = 0; i < 100; i++) {
            model.addElement("Item" + i);
        }
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
