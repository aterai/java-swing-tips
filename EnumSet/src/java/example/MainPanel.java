package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());

        String[] columnNames = {"user", "rwx"};
        Object[][] data = {
            {"owner", EnumSet.allOf(Permissions.class)}, // EnumSet.of(Permissions.READ, Permissions.WRITE, Permissions.EXECUTE)},
            {"group", EnumSet.of(Permissions.READ)},
            {"other", EnumSet.noneOf(Permissions.class)}
        };
        TableModel model = new DefaultTableModel(data, columnNames) {
            @Override public Class<?> getColumnClass(int column) {
                return getValueAt(0, column).getClass();
            }
        };
        JTable table = new JTable(model);
        table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

//         if (System.getProperty("java.version").startsWith("1.6.0")) {
//             // 1.6.0_xx bug? column header click -> edit cancel?
//             table.getTableHeader().addMouseListener(new MouseAdapter() {
//                 @Override public void mousePressed(MouseEvent e) {
//                     if (table.isEditing()) {
//                         table.getCellEditor().stopCellEditing();
//                     }
//                 }
//             });
//         }

        TableColumn c = table.getColumnModel().getColumn(1);
        c.setCellRenderer(new CheckBoxesRenderer());
        c.setCellEditor(new CheckBoxesEditor());

        EnumMap<Permissions, Integer> map = new EnumMap<>(Permissions.class);
        map.put(Permissions.READ, 1 << 2);
        map.put(Permissions.WRITE, 1 << 1);
        map.put(Permissions.EXECUTE, 1 << 0);

        JLabel label = new JLabel();
        JButton button = new JButton("ls -l (chmod)");
        button.addActionListener(e -> {
            StringBuilder nbuf = new StringBuilder(3);
            StringBuilder buf = new StringBuilder(9);
            for (int i = 0; i < model.getRowCount(); i++) {
                EnumSet<?> v = (EnumSet<?>) model.getValueAt(i, 1);
                int flg = 0;
                if (v.contains(Permissions.READ)) {
                    flg |= map.get(Permissions.READ);
                    buf.append('r');
                } else {
                    buf.append('-');
                }
                if (v.contains(Permissions.WRITE)) {
                    flg |= map.get(Permissions.WRITE);
                    buf.append('w');
                } else {
                    buf.append('-');
                }
                if (v.contains(Permissions.EXECUTE)) {
                    flg |= map.get(Permissions.EXECUTE);
                    buf.append('x');
                } else {
                    buf.append('-');
                }
                nbuf.append(flg);
            }
            label.setText(String.format(" %s -%s", nbuf, buf));
        });

        JPanel p = new JPanel(new BorderLayout());
        p.add(label);
        p.add(button, BorderLayout.EAST);
        add(new JScrollPane(table));
        add(p, BorderLayout.SOUTH);
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

enum Permissions { EXECUTE, WRITE, READ; }

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
        EnumSet<?> f = v instanceof EnumSet ? (EnumSet<?>) v : EnumSet.noneOf(Permissions.class);
        buttons.get(0).setSelected(f.contains(Permissions.READ));
        buttons.get(1).setSelected(f.contains(Permissions.WRITE));
        buttons.get(2).setSelected(f.contains(Permissions.EXECUTE));
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
    // public static class UIResource extends CheckBoxesRenderer implements javax.swing.plaf.UIResource {}
}

class CheckBoxesEditor extends CheckBoxesPanel implements TableCellEditor {
    protected transient ChangeEvent changeEvent;

    protected CheckBoxesEditor() {
        super();
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
    }
    @Override public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        updateButtons(value);
        return this;
    }
    @Override public Object getCellEditorValue() {
        EnumSet<Permissions> f = EnumSet.noneOf(Permissions.class);
        if (buttons.get(0).isSelected()) {
            f.add(Permissions.READ);
        }
        if (buttons.get(1).isSelected()) {
            f.add(Permissions.WRITE);
        }
        if (buttons.get(2).isSelected()) {
            f.add(Permissions.EXECUTE);
        }
        return f;
    }

    // Copied from AbstractCellEditor
    // protected EventListenerList listenerList = new EventListenerList();
    // protected transient ChangeEvent changeEvent;
    @Override public boolean isCellEditable(EventObject e) {
        return true;
    }
    @Override public boolean shouldSelectCell(EventObject anEvent) {
        return true;
    }
    @Override public boolean stopCellEditing() {
        fireEditingStopped();
        return true;
    }
    @Override public void cancelCellEditing() {
        fireEditingCanceled();
    }
    @Override public void addCellEditorListener(CellEditorListener l) {
        listenerList.add(CellEditorListener.class, l);
    }
    @Override public void removeCellEditorListener(CellEditorListener l) {
        listenerList.remove(CellEditorListener.class, l);
    }
    public CellEditorListener[] getCellEditorListeners() {
        return listenerList.getListeners(CellEditorListener.class);
    }
    protected void fireEditingStopped() {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == CellEditorListener.class) {
                // Lazily create the event:
                if (Objects.isNull(changeEvent)) {
                    changeEvent = new ChangeEvent(this);
                }
                ((CellEditorListener) listeners[i + 1]).editingStopped(changeEvent);
            }
        }
    }
    protected void fireEditingCanceled() {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == CellEditorListener.class) {
                // Lazily create the event:
                if (Objects.isNull(changeEvent)) {
                    changeEvent = new ChangeEvent(this);
                }
                ((CellEditorListener) listeners[i + 1]).editingCanceled(changeEvent);
            }
        }
    }
}
