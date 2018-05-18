package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());
        JComboBox<String> combo01 = new AlternateRowColorComboBox<>(makeModel());
        // // MetalLookAndFeel
        // combo01.setUI(new MetalComboBoxUI() {
        //     @Override public PropertyChangeListener createPropertyChangeListener() {
        //         return new MetalPropertyChangeListener() {
        //             @Override public void propertyChange(PropertyChangeEvent e) {
        //                 String propertyName = e.getPropertyName();
        //                 if (propertyName == "background") {
        //                     Color color = (Color) e.getNewValue();
        //                     // arrowButton.setBackground(color);
        //                     listBox.setBackground(color);
        //                 } else {
        //                     super.propertyChange(e);
        //                 }
        //             }
        //         };
        //     }
        // });

        JComboBox<String> combo02 = new AlternateRowColorComboBox<>(makeModel());
        combo02.setEditable(true);

        Box box = Box.createVerticalBox();
        box.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        box.add(makeTitledPanel("setEditable(false)", combo01));
        box.add(Box.createVerticalStrut(5));
        box.add(makeTitledPanel("setEditable(true)", combo02));

        add(box, BorderLayout.NORTH);
        setPreferredSize(new Dimension(320, 240));
    }
    private static Component makeTitledPanel(String title, Component c) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder(title));
        p.add(c);
        return p;
    }
    private static ComboBoxModel<String> makeModel() {
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
        model.addElement("aaaa");
        model.addElement("aaaabbb");
        model.addElement("aaaabbbcc");
        model.addElement("1234123512351234");
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

class AlternateRowColorComboBox<E> extends JComboBox<E> {
    private static final Color EVEN_BGCOLOR = new Color(225, 255, 225);
    private static final Color ODD_BGCOLOR = new Color(255, 255, 255);
    private transient ItemListener itemColorListener;

    protected AlternateRowColorComboBox() {
        super();
    }
    protected AlternateRowColorComboBox(ComboBoxModel<E> model) {
        super(model);
    }
    // protected AlternateRowColorComboBox(E[] items) {
    //     super(items);
    // }
    @Override public void setEditable(boolean flag) {
        super.setEditable(flag);
        if (flag) {
            JTextField field = (JTextField) getEditor().getEditorComponent();
            field.setOpaque(true);
            field.setBackground(getAlternateRowColor(getSelectedIndex()));
        }
    }
    @Override public void updateUI() {
        removeItemListener(itemColorListener);
        super.updateUI();
        setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel c = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                c.setOpaque(true);
                if (!isSelected) {
                    c.setBackground(getAlternateRowColor(index));
                }
                return c;
            }
        });
        itemColorListener = e -> {
            if (e.getStateChange() != ItemEvent.SELECTED) {
                return;
            }
            JComboBox<?> cb = (JComboBox<?>) e.getItemSelectable();
            Color rc = getAlternateRowColor(cb.getSelectedIndex());
            if (cb.isEditable()) {
                JTextField field = (JTextField) cb.getEditor().getEditorComponent();
                field.setBackground(rc);
            } else {
                cb.setBackground(rc);
            }
        };
        addItemListener(itemColorListener);
        EventQueue.invokeLater(() -> {
            Component c = getEditor().getEditorComponent();
            if (c instanceof JTextField) {
                JTextField field = (JTextField) c;
                field.setOpaque(true);
                field.setBackground(getAlternateRowColor(getSelectedIndex()));
            }
        });
    }
    protected static Color getAlternateRowColor(int index) {
        return index % 2 == 0 ? EVEN_BGCOLOR : ODD_BGCOLOR;
    }
}
