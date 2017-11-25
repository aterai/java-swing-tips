package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import javax.accessibility.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.plaf.basic.*;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());

        // CheckableItem displayValue = new CheckableItem("MMMMMMMMMM", false);
        CheckableItem[] m = {
            new CheckableItem("aaa",     false),
            new CheckableItem("bbbbb",   true),
            new CheckableItem("111",     false),
            new CheckableItem("33333",   true),
            new CheckableItem("2222",    true),
            new CheckableItem("ccccccc", false)
        };

        JComboBox<CheckableItem> combo0 = new CheckedComboBox<>(new DefaultComboBoxModel<>(m));
        JComboBox<CheckableItem> combo1 = new CheckedComboBox1<>(new DefaultComboBoxModel<>(m));
        JComboBox<CheckableItem> combo2 = new CheckedComboBox2<>(new DefaultComboBoxModel<>(m));
        JComboBox<CheckableItem> combo3 = new CheckedComboBox3<>(new DefaultComboBoxModel<>(m));
        JComboBox<CheckableItem> combo4 = new CheckedComboBox4<>(new CheckableComboBoxModel<>(m));

        GridBagConstraints c = new GridBagConstraints();
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        c.insets = new Insets(10, 5, 5, 0);
        c.gridheight = 1;
        c.gridwidth = 1;
        c.gridy = 0;

        c.gridx = 0;
        c.weightx = 0.0;
        c.anchor = GridBagConstraints.WEST;
        for (String s: Arrays.asList("setSelectedIndex(-1/idx):", "contentsChanged(...):", "repaint():", "(remove/insert)ItemAt(...):", "fireContentsChanged(...):")) {
            p.add(new JLabel(s), c);
            c.gridy += 1;
        }

        c.gridy = 0;
        c.gridx = 1;
        c.weightx = 1.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        for (JComboBox<CheckableItem> combo: Arrays.asList(combo0, combo1, combo2, combo3, combo4)) {
            // combo.setPrototypeDisplayValue(displayValue);
            p.add(combo, c);
            c.gridy += 1;
        }

        add(p, BorderLayout.NORTH);
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
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

class CheckableItem {
    public final String text;
    public boolean selected;
    protected CheckableItem(String text, boolean selected) {
        this.text = text;
        this.selected = selected;
    }
    @Override public String toString() {
        return text;
    }
}

class CheckBoxCellRenderer<E extends CheckableItem> implements ListCellRenderer<E> {
    private final JLabel label = new JLabel(" ");
    private final JCheckBox check = new JCheckBox(" ");
    @Override public Component getListCellRendererComponent(JList list, CheckableItem value, int index, boolean isSelected, boolean cellHasFocus) {
        if (index < 0) {
            label.setText(getCheckedItemString(list.getModel()));
            return label;
        } else {
            check.setText(Objects.toString(value, ""));
            check.setSelected(value.selected);
            if (isSelected) {
                check.setBackground(list.getSelectionBackground());
                check.setForeground(list.getSelectionForeground());
            } else {
                check.setBackground(list.getBackground());
                check.setForeground(list.getForeground());
            }
            return check;
        }
    }
    private static String getCheckedItemString(ListModel model) {
        List<String> sl = new ArrayList<>();
        for (int i = 0; i < model.getSize(); i++) {
            Object o = model.getElementAt(i);
            if (o instanceof CheckableItem && ((CheckableItem) o).selected) {
                sl.add(o.toString());
            }
        }
        if (sl.isEmpty()) {
            return " "; // When returning the empty string, the height of JComboBox may become 0 in some cases.
        } else {
            return sl.stream().sorted().collect(Collectors.joining(", "));
        }
    }
}

class CheckedComboBox<E extends CheckableItem> extends JComboBox<E> {
    private boolean keepOpen;
    private transient ActionListener listener;

    protected CheckedComboBox() {
        super();
    }
    protected CheckedComboBox(ComboBoxModel<E> model) {
        super(model);
    }
//     protected CheckedComboBox(E[] m) {
//         super(m);
//     }
    @Override public Dimension getPreferredSize() {
        return new Dimension(200, 20);
    }
    @Override public void updateUI() {
        setRenderer(null);
        removeActionListener(listener);
        super.updateUI();
        listener = e -> {
            if ((e.getModifiers() & InputEvent.MOUSE_EVENT_MASK) != 0) {
                updateItem(getSelectedIndex());
                keepOpen = true;
            }
        };
        setRenderer(new CheckBoxCellRenderer<>());
        addActionListener(listener);
        getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "checkbox-select");
        getActionMap().put("checkbox-select", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                Accessible a = getAccessibleContext().getAccessibleChild(0);
                if (a instanceof ComboPopup) {
                    updateItem(((ComboPopup) a).getList().getSelectedIndex());
                }
            }
        });
    }
    protected void updateItem(int index) {
        if (isPopupVisible()) {
            E item = getItemAt(index);
            item.selected ^= true;
            setSelectedIndex(-1);
            setSelectedItem(item);
        }
    }
    @Override public void setPopupVisible(boolean v) {
        if (keepOpen) {
            keepOpen = false;
        } else {
            super.setPopupVisible(v);
        }
    }
}

class CheckedComboBox1<E extends CheckableItem> extends CheckedComboBox<E> {
    protected CheckedComboBox1(ComboBoxModel<E> model) {
        super(model);
    }
    @Override protected void updateItem(int index) {
        if (isPopupVisible()) {
            E item = getItemAt(index);
            item.selected ^= true;
            contentsChanged(new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, index, index));
        }
    }
}

class CheckedComboBox2<E extends CheckableItem> extends CheckedComboBox<E> {
    protected CheckedComboBox2(ComboBoxModel<E> model) {
        super(model);
    }
    @Override protected void updateItem(int index) {
        if (isPopupVisible()) {
            E item = getItemAt(index);
            item.selected ^= true;
            repaint();
            Accessible a = getAccessibleContext().getAccessibleChild(0);
            if (a instanceof ComboPopup) {
                ((ComboPopup) a).getList().repaint();
            }
        }
    }
}

class CheckedComboBox3<E extends CheckableItem> extends CheckedComboBox<E> {
    protected CheckedComboBox3(ComboBoxModel<E> model) {
        super(model);
    }
    @Override protected void updateItem(int index) {
        if (isPopupVisible()) {
            E item = getItemAt(index);
            item.selected ^= true;
            removeItemAt(index);
            insertItemAt(item, index);
            setSelectedItem(item);
        }
    }
}

class CheckableComboBoxModel<E> extends DefaultComboBoxModel<E> {
    @SuppressWarnings("PMD.UseVarargs")
    protected CheckableComboBoxModel(E[] items) {
        super(items);
    }
    public void fireContentsChanged(int index) {
        super.fireContentsChanged(this, index, index);
    }
}

class CheckedComboBox4<E extends CheckableItem> extends CheckedComboBox<E> {
    protected CheckedComboBox4(ComboBoxModel<E> model) {
        super(model);
    }
    @Override protected void updateItem(int index) {
        if (isPopupVisible()) {
            E item = getItemAt(index);
            item.selected ^= true;
            ComboBoxModel m = getModel();
            if (m instanceof CheckableComboBoxModel) {
                ((CheckableComboBoxModel) m).fireContentsChanged(index);
            }
        }
    }
}
