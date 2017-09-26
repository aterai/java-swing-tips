package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.plaf.*;
import javax.swing.table.*;

public final class MainPanel extends JPanel {
    private final String[] columnNames = {"user", "rwx"};
    private final Object[][] data = {
        {"owner", 7}, {"group", 6}, {"other", 5}
    };
    private final TableModel model = new DefaultTableModel(data, columnNames) {
        @Override public Class<?> getColumnClass(int column) {
            return getValueAt(0, column).getClass();
        }
    };
    private final JTable table = new JTable(model) {
        @Override public void updateUI() {
            super.updateUI();
            getColumnModel().getColumn(1).setCellRenderer(new CheckBoxesRenderer());
            getColumnModel().getColumn(1).setCellEditor(new CheckBoxesEditor());
        }
    };

    public MainPanel() {
        super(new BorderLayout());
        table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

//         if (System.getProperty("java.version").startsWith("1.6.0")) {
//             //1.6.0_xx bug? column header click -> edit cancel?
//             table.getTableHeader().addMouseListener(new MouseAdapter() {
//                 @Override public void mousePressed(MouseEvent e) {
//                     if (table.isEditing()) {
//                         table.getCellEditor().stopCellEditing();
//                     }
//                 }
//             });
//         }

        //http://ateraimemo.com/Swing/TerminateEdit.html
        //table.getTableHeader().setReorderingAllowed(false);
        //frame.setResizeable(false);
        //or
//         table.addMouseListener(new MouseAdapter() {
//             @Override public void mouseReleased(MouseEvent e) {
//                 JTable t = (JTable) e.getComponent();
//                 Point p  = e.getPoint();
//                 int row  = t.rowAtPoint(p);
//                 int col  = t.columnAtPoint(p);
//                 if (t.convertColumnIndexToModel(col) == 1) {
//                     t.getCellEditor(row, col).stopCellEditing();
//                 }
//             }
//         });

        add(new JScrollPane(table));
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

class CheckBoxesPanel extends JPanel {
    protected static final String[] TITLES = {"r", "w", "x"};
    public JCheckBox[] buttons;
    @Override public void updateUI() {
        super.updateUI();
        setOpaque(false);
        setBackground(new Color(0x0, true));
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        initButtons();
    }
    private void initButtons() {
        Color bgc = new Color(0x0, true);
        buttons = new JCheckBox[TITLES.length];
        for (int i = 0; i < buttons.length; i++) {
            JCheckBox b = new JCheckBox(TITLES[i]);
            b.setOpaque(false);
            b.setFocusable(false);
            b.setRolloverEnabled(false);
            b.setBackground(bgc);
            buttons[i] = b;
            add(b);
            add(Box.createHorizontalStrut(5));
        }
    }
    protected void updateButtons(Object v) {
        removeAll();
        initButtons();
        Integer i = v instanceof Integer ? (Integer) v : 0;
        buttons[0].setSelected((i & (1 << 2)) != 0);
        buttons[1].setSelected((i & (1 << 1)) != 0);
        buttons[2].setSelected((i & (1 << 0)) != 0);
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
    //public static class UIResource extends CheckBoxesRenderer implements javax.swing.plaf.UIResource {}
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
//                 final String t = TITLES[i];
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
//             im.put(KeyStroke.getKeyStroke(KeyEvent.VK_R, 0), TITLES[0]);
//             im.put(KeyStroke.getKeyStroke(KeyEvent.VK_W, 0), TITLES[1]);
//             im.put(KeyStroke.getKeyStroke(KeyEvent.VK_X, 0), TITLES[2]);
//         });
//     }
//     @Override public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
//         updateButtons(value);
//         return this;
//     }
//     @Override public Object getCellEditorValue() {
//         int i = 0;
//         i = buttons[0].isSelected() ? 1 << 2 | i : i;
//         i = buttons[1].isSelected() ? 1 << 1 | i : i;
//         i = buttons[2].isSelected() ? 1 << 0 | i : i;
//         //if (buttons[0].isSelected()) { i |= 1 << 2; }
//         //if (buttons[1].isSelected()) { i |= 1 << 1; }
//         //if (buttons[2].isSelected()) { i |= 1 << 0; }
//         return i;
//     }
//
//     //Copied from AbstractCellEditor
//     //protected EventListenerList listenerList = new EventListenerList();
//     //protected transient ChangeEvent changeEvent;
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
                for (int i = 0; i < buttons.length; i++) {
                    String title = TITLES[i];
                    am.put(title, new AbstractAction(title) {
                        @Override public void actionPerformed(ActionEvent e) {
                            Arrays.stream(buttons)
                                .filter(b -> b.getText().equals(title))
                                .findFirst()
                                .ifPresent(JCheckBox::doClick);
                            fireEditingStopped();
                        }
                    });
                }
                InputMap im = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
                im.put(KeyStroke.getKeyStroke(KeyEvent.VK_R, 0), TITLES[0]);
                im.put(KeyStroke.getKeyStroke(KeyEvent.VK_W, 0), TITLES[1]);
                im.put(KeyStroke.getKeyStroke(KeyEvent.VK_X, 0), TITLES[2]);
            });
        }
    };
    @Override public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        panel.updateButtons(value);
        return panel;
    }
    @Override public Object getCellEditorValue() {
        int i = 0;
        i = panel.buttons[0].isSelected() ? 1 << 2 | i : i;
        i = panel.buttons[1].isSelected() ? 1 << 1 | i : i;
        i = panel.buttons[2].isSelected() ? 1 << 0 | i : i;
        return i;
    }
}
