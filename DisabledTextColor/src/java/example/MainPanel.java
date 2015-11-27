package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;
import java.util.List;
import javax.swing.*;

public final class MainPanel extends JPanel {
// RadioButton.disabledText
// ComboBox.disabledForeground
// ComboBox.disabledBackground
// Label.disabledForeground
// CheckBoxMenuItem.disabledForeground
// CheckBox.disabledText
// Label.disabledShadow
// ToggleButton.disabledText
// RadioButtonMenuItem.disabledForeground
// Button.disabledToolBarBorderBackground
// Menu.disabledForeground
// MenuItem.disabledForeground
// Button.disabledText
    private final JCheckBox cbx1   = new JCheckBox("default", true);
    private final JCheckBox cbx2   = new JCheckBox("<html>html tag</html>", true);
    private final JLabel label     = new JLabel("label disabledForeground");
    private final JButton button   = new JButton("button disabledText");
    private final JComboBox<String> combo1 = new JComboBox<>(new String[] {"disabledForeground", "bb"});
    private final JComboBox<String> combo2 = new JComboBox<>(new String[] {"<html>html</html>", "renderer"});
    private final JComboBox<String> combo3 = new JComboBox<>(new String[] {"setEditable(true)", "setDisabledTextColor"});
    private final List<? extends JComponent> list = Arrays.asList(cbx1, cbx2, combo1, combo2, combo3, label, button);

    public MainPanel() {
        super(new BorderLayout());
        UIManager.put("CheckBox.disabledText", Color.RED);
        UIManager.put("ComboBox.disabledForeground", Color.GREEN);
        UIManager.put("Button.disabledText", Color.YELLOW);
        UIManager.put("Label.disabledForeground", Color.ORANGE);
        final JCheckBox cbx = new JCheckBox(new AbstractAction("setEnabled") {
            @Override public void actionPerformed(ActionEvent e) {
                JCheckBox source = (JCheckBox) e.getSource();
                boolean flg = source.isSelected();
                for (JComponent c: list) {
                    c.setEnabled(flg);
                }
            }
        });
        combo2.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (index < 0 && !combo2.isEnabled()) {
                    JLabel l = (JLabel) c;
                    l.setText("<html><font color='red'>" + l.getText());
                    l.setOpaque(false);
                    //l.setForeground(Color.RED);
                }
                return c;
            }
        });
        //combo2.setEditable(true);

        combo3.setEditable(true);
        JTextField editor = (JTextField) combo3.getEditor().getEditorComponent();
        editor.setDisabledTextColor(Color.PINK);

        Box box = Box.createVerticalBox();
        box.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 5));
        for (JComponent c: list) {
            c.setEnabled(false);
            c.setAlignmentX(Component.LEFT_ALIGNMENT);
            int h = c.getPreferredSize().height;
            c.setMaximumSize(new Dimension(Integer.MAX_VALUE, h));
            box.add(c);
            box.add(Box.createVerticalStrut(5));
        }
        box.add(Box.createVerticalGlue());
        add(cbx, BorderLayout.NORTH);
        add(box);
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
        frame.setMinimumSize(new Dimension(256, 100));
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
