package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());

        JTextField field = new JTextField("1, 2, 5");

        DisableItemComboBox<String> combo = new DisableItemComboBox<>(makeModel());
        combo.setDisableIndex(getDisableIndexFromTextField(field));

        JButton button = new JButton("init");
        button.addActionListener(e -> combo.setDisableIndex(getDisableIndexFromTextField(field)));

        Box box = Box.createHorizontalBox();
        box.add(new JLabel("Disabled Item Index:"));
        box.add(field);
        box.add(Box.createHorizontalStrut(2));
        box.add(button);
        add(box, BorderLayout.SOUTH);
        add(combo, BorderLayout.NORTH);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setPreferredSize(new Dimension(320, 240));
    }

    private static ComboBoxModel<String> makeModel() {
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
        model.addElement("0000000000000");
        model.addElement("111111");
        model.addElement("222222222222");
        model.addElement("33");
        model.addElement("4444444444444444");
        model.addElement("555555555555555555555555");
        model.addElement("6666666666");
        return model;
    }

    private static Set<Integer> getDisableIndexFromTextField(JTextField field) {
        try {
            return Stream.of(field.getText().split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Integer::valueOf)
                .collect(Collectors.toSet());
        } catch (NumberFormatException ex) {
            Toolkit.getDefaultToolkit().beep();
            JOptionPane.showMessageDialog(field, "invalid value.\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return Collections.<Integer>emptySet();
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

class DisableItemComboBox<E> extends JComboBox<E> {
    protected final Set<Integer> disableIndexSet = new HashSet<>();
    protected boolean isDisableIndex;
    protected final Action up = new AbstractAction() {
        @Override public void actionPerformed(ActionEvent e) {
            int si = getSelectedIndex();
            for (int i = si - 1; i >= 0; i--) {
                if (!disableIndexSet.contains(i)) {
                    setSelectedIndex(i);
                    break;
                }
            }
        }
    };
    protected final Action down = new AbstractAction() {
        @Override public void actionPerformed(ActionEvent e) {
            int si = getSelectedIndex();
            for (int i = si + 1; i < getModel().getSize(); i++) {
                if (!disableIndexSet.contains(i)) {
                    setSelectedIndex(i);
                    break;
                }
            }
        }
    };
    protected DisableItemComboBox() {
        super();
    }
    protected DisableItemComboBox(ComboBoxModel<E> model) {
        super(model);
    }
    // protected DisableItemComboBox(E[] items) {
    //     super(items);
    // }
    @Override public void updateUI() {
        super.updateUI();
        setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component c;
                if (disableIndexSet.contains(index)) {
                    c = super.getListCellRendererComponent(list, value, index, false, false);
                    c.setEnabled(false);
                } else {
                    c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    c.setEnabled(true);
                }
                return c;
            }
        });
        EventQueue.invokeLater(() -> {
            ActionMap am = getActionMap();
            am.put("selectPrevious3", up);
            am.put("selectNext3", down);
            InputMap im = getInputMap();
            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "selectPrevious3");
            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_KP_UP, 0), "selectPrevious3");
            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "selectNext3");
            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_KP_DOWN, 0), "selectNext3");
        });
    }
    public void setDisableIndex(Set<Integer> set) {
        disableIndexSet.clear();
        for (Integer i: set) {
            disableIndexSet.add(i);
        }
    }
    @Override public void setPopupVisible(boolean v) {
        if (!v && isDisableIndex) {
            isDisableIndex = false;
        } else {
            super.setPopupVisible(v);
        }
    }
    @Override public void setSelectedIndex(int index) {
        if (disableIndexSet.contains(index)) {
            isDisableIndex = true;
        } else {
            // isDisableIndex = false;
            super.setSelectedIndex(index);
        }
    }
}
