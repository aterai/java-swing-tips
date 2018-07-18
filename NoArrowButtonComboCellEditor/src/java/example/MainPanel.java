package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.*;
import javax.swing.*;
import javax.swing.plaf.basic.*;
import javax.swing.table.*;

public final class MainPanel extends JPanel {
    private final String[] columnNames = {"LocalDateTime", "String", "Boolean"};
    private final Object[][] data = {
        {LocalDateTime.now(), "aaa", true}, {LocalDateTime.now(), "bbb", false},
        {LocalDateTime.now(), "CCC", true}, {LocalDateTime.now(), "DDD", false}
    };
    private final TableModel model = new DefaultTableModel(data, columnNames) {
        @Override public Class<?> getColumnClass(int column) {
            return getValueAt(0, column).getClass();
        }
    };
    private final JTable table = new JTable(model);
    public MainPanel() {
        super(new BorderLayout());
        table.setAutoCreateRowSorter(true);
        table.getColumnModel().getColumn(0).setCellRenderer(new LocalDateTimeTableCellRenderer());
        table.getColumnModel().getColumn(0).setCellEditor(new LocalDateTimeTableCellEditor());
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

class LocalDateTimeTableCellRenderer extends DefaultTableCellRenderer {
    private static final String DATE_FORMAT_PATTERN = "yyyy/MM/dd";
    private final transient DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(DATE_FORMAT_PATTERN);
    @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        if (value instanceof TemporalAccessor) {
            setText(dateTimeFormatter.format((TemporalAccessor) value));
        }
        return this;
    }
}

class ZeroSizeButtonUI extends BasicComboBoxUI {
    @Override protected JButton createArrowButton() {
        return new JButton() {
            @Override public Dimension getPreferredSize() {
                return new Dimension();
            }
            @Override public boolean isVisible() {
                return false;
            }
            @Override public void updateUI() {
                super.updateUI();
                setBorder(BorderFactory.createEmptyBorder());
                // setVisible(false);
            }
        };
    }
}

class LocalDateTimeTableCellEditor extends AbstractCellEditor implements TableCellEditor {
    protected final JComboBox<LocalDateTime> comboBox = new JComboBox<LocalDateTime>() {
        @Override public void updateUI() {
            super.updateUI();
            UIManager.put("ComboBox.squareButton", Boolean.FALSE);
            putClientProperty("JComboBox.isTableCellEditor", Boolean.TRUE);
            setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
            setOpaque(false);
            setRenderer(new LocalDateTimeCellRenderer());
            setUI(new ZeroSizeButtonUI());
        }
    };
    protected LocalDateTime selectedDate;

    @Override public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        if (value instanceof LocalDateTime) {
            comboBox.setModel(new DefaultComboBoxModel<LocalDateTime>() {
                @Override public LocalDateTime getElementAt(int index) {
                    // if (index >= 0 && index < getSize()) {
                    return LocalDateTime.now().plusDays(index);
                }
                @Override public int getSize() {
                    return 7; // in a week
                }
                @Override public Object getSelectedItem() {
                    return selectedDate;
                }
                @Override public void setSelectedItem(Object anItem) {
                    selectedDate = (LocalDateTime) anItem;
                }
            });
            selectedDate = (LocalDateTime) value;
        }
        comboBox.setBorder(UIManager.getBorder("Table.focusCellHighlightBorder"));
        return comboBox;
    }
    @Override public Object getCellEditorValue() {
        return comboBox.getSelectedItem();
    }
    @Override public boolean shouldSelectCell(EventObject anEvent) {
        if (anEvent instanceof MouseEvent) {
            MouseEvent e = (MouseEvent) anEvent;
            return e.getID() != MouseEvent.MOUSE_DRAGGED;
        }
        return true;
    }
    @Override public boolean stopCellEditing() {
        if (comboBox.isEditable()) {
            // Commit edited value.
            comboBox.actionPerformed(new ActionEvent(this, 0, ""));
        }
        return super.stopCellEditing();
    }
    @Override public boolean isCellEditable(EventObject e) {
        return true;
    }
}

class LocalDateTimeCellRenderer extends JLabel implements ListCellRenderer<LocalDateTime> {
    private static final String DATE_FORMAT_PATTERN = "yyyy/MM/dd";
    private final transient DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(DATE_FORMAT_PATTERN);
    @Override public Component getListCellRendererComponent(JList<? extends LocalDateTime> list, LocalDateTime value, int index, boolean isSelected, boolean cellHasFocus) {
        if (Objects.nonNull(value)) {
            setText(dateTimeFormatter.format((TemporalAccessor) value));
        }
        setOpaque(true);
        if (isSelected) {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
        } else {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        }
        return this;
    }
}
