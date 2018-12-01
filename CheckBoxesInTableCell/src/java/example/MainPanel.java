package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());

        String[] columnNames = {"user", "rwx"};
        Object[][] data = {
            {"owner", 7}, {"group", 6}, {"other", 5}
        };
        TableModel model = new DefaultTableModel(data, columnNames) {
            @Override public Class<?> getColumnClass(int column) {
                return getValueAt(0, column).getClass();
            }
        };
        JTable table = new JTable(model) {
            @Override public void updateUI() {
                super.updateUI();
                getColumnModel().getColumn(1).setCellRenderer(new CheckBoxesRenderer());
                getColumnModel().getColumn(1).setCellEditor(new CheckBoxesEditor());
            }
        };
        table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

        // if (System.getProperty("java.version").startsWith("1.6.0")) {
        //     // 1.6.0_xx bug? column header click -> edit cancel?
        //     table.getTableHeader().addMouseListener(new MouseAdapter() {
        //         @Override public void mousePressed(MouseEvent e) {
        //             if (table.isEditing()) {
        //                 table.getCellEditor().stopCellEditing();
        //             }
        //         }
        //     });
        // }

        // // https://ateraimemo.com/Swing/TerminateEdit.html
        // // table.getTableHeader().setReorderingAllowed(false);
        // // frame.setResizeable(false);
        // // or
        // table.addMouseListener(new MouseAdapter() {
        //     @Override public void mouseReleased(MouseEvent e) {
        //         JTable t = (JTable) e.getComponent();
        //         Point p = e.getPoint();
        //         int row = t.rowAtPoint(p);
        //         int col = t.columnAtPoint(p);
        //         if (t.convertColumnIndexToModel(col) == 1) {
        //             t.getCellEditor(row, col).stopCellEditing();
        //         }
        //     }
        // });

        add(new JScrollPane(table));
        setPreferredSize(new Dimension(320, 240));
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

class CheckBoxesPanel extends JPanel {
    private static final Color BGC = new Color(0x0, true);
    protected final String[] titles = {"r", "w", "x"};
    protected final List<JCheckBox> buttons = new ArrayList<>(titles.length);
    @Override public void updateUI() {
        super.updateUI();
        setOpaque(false);
        setBackground(BGC);
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        EventQueue.invokeLater(() -> initButtons());
    }
    private void initButtons() {
        removeAll();
        buttons.clear();
        for (String t: titles) {
            JCheckBox b = makeCheckBox(t);
            buttons.add(b);
            add(b);
            add(Box.createHorizontalStrut(5));
        }
    }
    private static JCheckBox makeCheckBox(String title) {
        JCheckBox b = new JCheckBox(title);
        b.setOpaque(false);
        b.setFocusable(false);
        b.setRolloverEnabled(false);
        b.setBackground(BGC);
        return b;
    }
    protected void updateButtons(Object v) {
        initButtons();
        Integer i = v instanceof Integer ? (Integer) v : 0;
        buttons.get(0).setSelected((i & (1 << 2)) != 0);
        buttons.get(1).setSelected((i & (1 << 1)) != 0);
        buttons.get(2).setSelected((i & (1 << 0)) != 0);
    }
}

class CheckBoxesRenderer extends CheckBoxesPanel implements TableCellRenderer {
    @Override public void updateUI() {
        super.updateUI();
        setName("Table.cellRenderer");
    }
    @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        updateButtons(value);
        return this;
    }
    // public static class UIResource extends CheckBoxesRenderer implements UIResource {}
}

// // TEST:
// class CheckBoxesEditor extends CheckBoxesPanel implements TableCellEditor {
//     protected transient ChangeEvent changeEvent;
//
//     @Override public void updateUI() {
//         super.updateUI();
//         EventQueue.invokeLater(() -> {
//             ActionMap am = getActionMap();
//             for (int i = 0; i < buttons.length; i++) {
//                 String t = titles[i];
//                 am.put(t, new AbstractAction(t) {
//                     @Override public void actionPerformed(ActionEvent e) {
//                         for (JCheckBox b: buttons) {
//                             if (b.getText().equals(t)) {
//                                 b.doClick();
//                                 break;
//                             }
//                         }
//                         fireEditingStopped();
//                     }
//                 });
//             }
//             InputMap im = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
//             im.put(KeyStroke.getKeyStroke(KeyEvent.VK_R, 0), titles[0]);
//             im.put(KeyStroke.getKeyStroke(KeyEvent.VK_W, 0), titles[1]);
//             im.put(KeyStroke.getKeyStroke(KeyEvent.VK_X, 0), titles[2]);
//         });
//     }
//     @Override public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
//         updateButtons(value);
//         return this;
//     }
//     @Override public Object getCellEditorValue() {
//         int i = 0;
//         i = buttons.get(0).isSelected() ? 1 << 2 | i : i;
//         i = buttons.get(1).isSelected() ? 1 << 1 | i : i;
//         i = buttons.get(2).isSelected() ? 1 << 0 | i : i;
//         // if (buttons.get(0).isSelected()) { i |= 1 << 2; }
//         // if (buttons.get(1).isSelected()) { i |= 1 << 1; }
//         // if (buttons.get(2).isSelected()) { i |= 1 << 0; }
//         return i;
//     }
//
//     // Copied from AbstractCellEditor
//     // protected EventListenerList listenerList = new EventListenerList();
//     // protected transient ChangeEvent changeEvent;
//     @Override public boolean isCellEditable(EventObject e) {
//         return true;
//     }
//     @Override public boolean shouldSelectCell(EventObject anEvent) {
//         return true;
//     }
//     @Override public boolean stopCellEditing() {
//         fireEditingStopped();
//         return true;
//     }
//     @Override public void cancelCellEditing() {
//         fireEditingCanceled();
//     }
//     @Override public void addCellEditorListener(CellEditorListener l) {
//         listenerList.add(CellEditorListener.class, l);
//     }
//     @Override public void removeCellEditorListener(CellEditorListener l) {
//         listenerList.remove(CellEditorListener.class, l);
//     }
//     public CellEditorListener[] getCellEditorListeners() {
//         return listenerList.getListeners(CellEditorListener.class);
//     }
//     protected void fireEditingStopped() {
//         // Guaranteed to return a non-null array
//         Object[] listeners = listenerList.getListenerList();
//         // Process the listeners last to first, notifying
//         // those that are interested in this event
//         for (int i = listeners.length - 2; i >= 0; i -= 2) {
//             if (listeners[i] == CellEditorListener.class) {
//                 // Lazily create the event:
//                 if (Objects.isNull(changeEvent)) {
//                     changeEvent = new ChangeEvent(this);
//                 }
//                 ((CellEditorListener) listeners[i + 1]).editingStopped(changeEvent);
//             }
//         }
//     }
//     protected void fireEditingCanceled() {
//         // Guaranteed to return a non-null array
//         Object[] listeners = listenerList.getListenerList();
//         // Process the listeners last to first, notifying
//         // those that are interested in this event
//         for (int i = listeners.length - 2; i >= 0; i -= 2) {
//             if (listeners[i] == CellEditorListener.class) {
//                 // Lazily create the event:
//                 if (Objects.isNull(changeEvent)) {
//                     changeEvent = new ChangeEvent(this);
//                 }
//                 ((CellEditorListener) listeners[i + 1]).editingCanceled(changeEvent);
//             }
//         }
//     }
// }

class CheckBoxesEditor extends AbstractCellEditor implements TableCellEditor {
    private final CheckBoxesPanel panel = new CheckBoxesPanel() {
        @Override public void updateUI() {
            super.updateUI();
            EventQueue.invokeLater(() -> {
                ActionMap am = getActionMap();
                for (int i = 0; i < buttons.size(); i++) {
                    String t = titles[i];
                    am.put(t, new AbstractAction(t) {
                        @Override public void actionPerformed(ActionEvent e) {
                            buttons.stream()
                                .filter(b -> b.getText().equals(t))
                                .findFirst()
                                .ifPresent(JCheckBox::doClick);
                            fireEditingStopped();
                        }
                    });
                }
                InputMap im = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
                im.put(KeyStroke.getKeyStroke(KeyEvent.VK_R, 0), titles[0]);
                im.put(KeyStroke.getKeyStroke(KeyEvent.VK_W, 0), titles[1]);
                im.put(KeyStroke.getKeyStroke(KeyEvent.VK_X, 0), titles[2]);
            });
        }
    };
    @Override public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        panel.updateButtons(value);
        return panel;
    }
    @Override public Object getCellEditorValue() {
        int i = 0;
        i = panel.buttons.get(0).isSelected() ? 1 << 2 | i : i;
        i = panel.buttons.get(1).isSelected() ? 1 << 1 | i : i;
        i = panel.buttons.get(2).isSelected() ? 1 << 0 | i : i;
        return i;
    }
}
