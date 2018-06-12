package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());
        Box box = Box.createVerticalBox();
        box.add(makeComboBox());
        box.setBorder(BorderFactory.createTitledBorder("ComboBoxSeparator"));
        add(box, BorderLayout.NORTH);
        add(new JScrollPane(new JTextArea("dummy")));
        setPreferredSize(new Dimension(320, 240));
    }
    private static ComboBoxModel<Object> makeModel() {
        DefaultComboBoxModel<Object> model = new DefaultComboBoxModel<Object>() {
            @Override public void setSelectedItem(Object anObject) {
                if (!(anObject instanceof JSeparator)) {
                    super.setSelectedItem(anObject);
                }
            }
        };
        model.addElement("aaaa");
        model.addElement("aaaabbb");
        model.addElement("aaaabbbcc");
        model.addElement("aaaabbbccddddddd");
        model.addElement(new JSeparator());
        model.addElement("bbb1");
        model.addElement("bbb12");
        model.addElement("bbb33333");
        model.addElement(new JSeparator());
        model.addElement("11111");
        model.addElement("2222222");
        return model;
    }
    private static JComboBox<Object> makeComboBox() {
        JComboBox<Object> combo = new JComboBox<>(makeModel());
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                if (value instanceof JSeparator) {
                    return (Component) value;
                } else {
                    return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                }
            }
        });

        ActionMap am = combo.getActionMap();
        am.put("selectPrevious3", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                JComboBox<?> cb = (JComboBox<?>) e.getSource();
                int index = cb.getSelectedIndex();
                if (index == 0) {
                    return;
                }
                Object o = cb.getItemAt(index - 1);
                if (o instanceof JSeparator) {
                    cb.setSelectedIndex(index - 2);
                } else {
                    cb.setSelectedIndex(index - 1);
                }
            }
        });
        am.put("selectNext3", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                JComboBox<?> cb = (JComboBox<?>) e.getSource();
                int index = cb.getSelectedIndex();
                if (index == cb.getItemCount() - 1) {
                    return;
                }
                Object o = cb.getItemAt(index + 1);
                if (o instanceof JSeparator) {
                    cb.setSelectedIndex(index + 2);
                } else {
                    cb.setSelectedIndex(index + 1);
                }
            }
        });

        InputMap im = combo.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "selectPrevious3");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_KP_UP, 0), "selectPrevious3");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "selectNext3");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_KP_DOWN, 0), "selectNext3");

        return combo;
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
