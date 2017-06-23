package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());

        RowDataModel model = new RowDataModel();
        model.addRowData(new RowData("Name 1", "comment..."));
        model.addRowData(new RowData("Name 2", "Test"));
        model.addRowData(new RowData("Name d", "ee"));
        model.addRowData(new RowData("Name c", "Test cc"));
        model.addRowData(new RowData("Name b", "Test bb"));
        model.addRowData(new RowData("Name a", "ff"));
        model.addRowData(new RowData("Name 0", "Test aa"));

        JTable table = new JTable(model);
//         //TEST:
//         JTable table = new JTable(model) {
//             protected final Color evenColor = new Color(240, 240, 255);
//             @Override public Component prepareRenderer(TableCellRenderer tcr, int row, int column) {
//                 Component c = super.prepareRenderer(tcr, row, column);
//                 if (isRowSelected(row)) {
//                     c.setForeground(getSelectionForeground());
//                     c.setBackground(getSelectionBackground());
//                 } else {
//                     c.setForeground(getForeground());
//                     c.setBackground(row % 2 == 0 ? evenColor : getBackground());
//                 }
//                 return c;
//             }
//         };

        StripeTableRenderer renderer = new StripeTableRenderer();
        table.setDefaultRenderer(Object.class, renderer);
        table.setDefaultRenderer(Integer.class, renderer);

        table.setShowGrid(false);
        //table.setShowHorizontalLines(false);
        //table.setShowVerticalLines(false);

        TableColumn col = table.getColumnModel().getColumn(0);
        col.setMinWidth(60);
        col.setMaxWidth(60);
        col.setResizable(false);

        table.setAutoCreateRowSorter(true);
        table.setFillsViewportHeight(true);
        table.setComponentPopupMenu(new TablePopupMenu());
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

class StripeTableRenderer extends DefaultTableCellRenderer {
    private static final Color EVEN_COLOR = new Color(240, 240, 255);
    @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        if (isSelected) {
            setForeground(table.getSelectionForeground());
            setBackground(table.getSelectionBackground());
        } else {
            setForeground(table.getForeground());
            setBackground(row % 2 == 0 ? EVEN_COLOR : table.getBackground());
        }
        setHorizontalAlignment(value instanceof Number ? RIGHT : LEFT);
        return this;
    }
}

class RowDataModel extends DefaultTableModel {
    private static final ColumnContext[] COLUMN_ARRAY = {
        new ColumnContext("No.",     Integer.class, false),
        new ColumnContext("Name",    String.class,  true),
        new ColumnContext("Comment", String.class,  true)
    };
    private int number;
    public void addRowData(RowData t) {
        Object[] obj = {number, t.getName(), t.getComment()};
        super.addRow(obj);
        number++;
    }
    @Override public boolean isCellEditable(int row, int col) {
        return COLUMN_ARRAY[col].isEditable;
    }
    @Override public Class<?> getColumnClass(int column) {
        return COLUMN_ARRAY[column].columnClass;
    }
    @Override public int getColumnCount() {
        return COLUMN_ARRAY.length;
    }
    @Override public String getColumnName(int column) {
        return COLUMN_ARRAY[column].columnName;
    }
    private static class ColumnContext {
        public final String  columnName;
        public final Class   columnClass;
        public final boolean isEditable;
        protected ColumnContext(String columnName, Class columnClass, boolean isEditable) {
            this.columnName = columnName;
            this.columnClass = columnClass;
            this.isEditable = isEditable;
        }
    }
}

class RowData {
    private String name;
    private String comment;
    protected RowData(String name, String comment) {
        this.name = name;
        this.comment = comment;
    }
    public void setName(String str) {
        name = str;
    }
    public void setComment(String str) {
        comment = str;
    }
    public String getName() {
        return name;
    }
    public String getComment() {
        return comment;
    }
}

class TablePopupMenu extends JPopupMenu {
    private final JMenuItem delete;
    protected TablePopupMenu() {
        super();
        add("add").addActionListener(e -> {
            JTable table = (JTable) getInvoker();
            RowDataModel model = (RowDataModel) table.getModel();
            model.addRowData(new RowData("New row", ""));
            Rectangle r = table.getCellRect(model.getRowCount() - 1, 0, true);
            table.scrollRectToVisible(r);
        });
        addSeparator();
        delete = add("delete");
        delete.addActionListener(e -> {
            JTable table = (JTable) getInvoker();
            DefaultTableModel model = (DefaultTableModel) table.getModel();
            int[] selection = table.getSelectedRows();
            for (int i = selection.length - 1; i >= 0; i--) {
                model.removeRow(table.convertRowIndexToModel(selection[i]));
            }
        });
    }
    @Override public void show(Component c, int x, int y) {
        if (c instanceof JTable) {
            delete.setEnabled(((JTable) c).getSelectedRowCount() > 0);
            super.show(c, x, y);
        }
    }
}
