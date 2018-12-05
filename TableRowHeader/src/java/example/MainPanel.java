package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Objects;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());

        DefaultListModel<String> listModel = new DefaultListModel<>();
        RowDataModel model = new RowDataModel(listModel);
        model.addRowData(new RowData("Name 1", "comment"));
        model.addRowData(new RowData("Name 2", "test"));
        model.addRowData(new RowData("Name d", "ee"));
        model.addRowData(new RowData("Name c", "test cc"));
        model.addRowData(new RowData("Name b", "test bb"));
        model.addRowData(new RowData("Name a", "ff"));
        model.addRowData(new RowData("Name 0", "test aa"));
        model.addRowData(new RowData("Name 0", "gg"));

        JTable table = new JTable(model);
        table.setCellSelectionEnabled(true);

        JTableHeader header = table.getTableHeader();
        header.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                if (table.isEditing()) {
                    table.getCellEditor().stopCellEditing();
                }
                int col = header.columnAtPoint(e.getPoint());
                table.changeSelection(0, col, false, false);
                table.changeSelection(table.getRowCount() - 1, col, false, true);
            }
        });

        RowHeaderList<String> rowHeader = new RowHeaderList<>(listModel, table);
        rowHeader.setFixedCellWidth(50);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setRowHeaderView(rowHeader);
        scroll.getRowHeader().addChangeListener(e -> scroll.getVerticalScrollBar().setValue(((JViewport) e.getSource()).getViewPosition().y));
        scroll.setComponentPopupMenu(new TablePopupMenu());
        table.setInheritsPopupMenu(true);

        rowHeader.setBackground(Color.BLUE);
        scroll.setBackground(Color.RED);
        scroll.getViewport().setBackground(Color.GREEN);

        add(scroll);
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

class RowHeaderList<E> extends JList<E> {
    protected final JTable table;
    protected final ListSelectionModel tableSelection;
    protected final ListSelectionModel listSelection;
    protected int rollOverRowIndex = -1;
    protected int pressedRowIndex = -1;

    protected RowHeaderList(ListModel<E> model, JTable table) {
        super(model);
        this.table = table;
        setFixedCellHeight(table.getRowHeight());
        setCellRenderer(new RowHeaderRenderer<>(table.getTableHeader()));
        // setSelectionModel(table.getSelectionModel());
        RollOverListener rol = new RollOverListener();
        addMouseListener(rol);
        addMouseMotionListener(rol);
        // setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.GRAY.brighter()));

        tableSelection = table.getSelectionModel();
        listSelection = getSelectionModel();
    }
    protected class RowHeaderRenderer<E2> extends JLabel implements ListCellRenderer<E2> {
        private final JTableHeader header; // = table.getTableHeader();
        protected RowHeaderRenderer(JTableHeader header) {
            super();
            this.header = header;
            this.setOpaque(true);
            // this.setBorder(UIManager.getBorder("TableHeader.cellBorder"));
            this.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 2, Color.GRAY.brighter()));
            this.setHorizontalAlignment(CENTER);
            this.setForeground(header.getForeground());
            this.setBackground(header.getBackground());
            this.setFont(header.getFont());
        }
        @Override public Component getListCellRendererComponent(JList<? extends E2> list, E2 value, int index, boolean isSelected, boolean cellHasFocus) {
            if (index == pressedRowIndex) {
                setBackground(Color.GRAY);
            } else if (index == rollOverRowIndex) {
                setBackground(Color.WHITE);
            } else if (isSelected) {
                setBackground(Color.GRAY.brighter());
            } else {
                setForeground(header.getForeground());
                setBackground(header.getBackground());
            }
            setText(Objects.toString(value, ""));
            return this;
        }
    }

    private class RollOverListener extends MouseAdapter {
        @Override public void mouseExited(MouseEvent e) {
            if (pressedRowIndex < 0) {
                // pressedRowIndex = -1;
                rollOverRowIndex = -1;
                repaint();
            }
        }
        @Override public void mouseMoved(MouseEvent e) {
            int row = locationToIndex(e.getPoint());
            if (row != rollOverRowIndex) {
                rollOverRowIndex = row;
                repaint();
            }
        }
        @Override public void mouseDragged(MouseEvent e) {
            if (pressedRowIndex >= 0) {
                int row = locationToIndex(e.getPoint());
                int start = Math.min(row, pressedRowIndex);
                int end = Math.max(row, pressedRowIndex);
                tableSelection.clearSelection();
                listSelection.clearSelection();
                tableSelection.addSelectionInterval(start, end);
                listSelection.addSelectionInterval(start, end);
                repaint();
            }
        }
        @Override public void mousePressed(MouseEvent e) {
            int row = locationToIndex(e.getPoint());
            if (row == pressedRowIndex) {
                return;
            }
            listSelection.clearSelection();
            table.changeSelection(row, 0, false, false);
            table.changeSelection(row, table.getColumnModel().getColumnCount() - 1, false, true);
            pressedRowIndex = row;
            // table.setRowSelectionInterval(row, row);
            // table.getSelectionModel().setSelectionInterval(row, row);
            // tableSelection.clearSelection();
            // table.getSelectionModel().setAnchorSelectionIndex(row);
            // table.getSelectionModel().setLeadSelectionIndex(row);
            // tableSelection.addSelectionInterval(row, row);
            // listSelection.addSelectionInterval(row, row);
            // table.getColumnModel().getSelectionModel().setAnchorSelectionIndex(0);
            // table.getColumnModel().getSelectionModel().setLeadSelectionIndex(0);
            // table.changeSelection(pressedRowIndex, table.getColumnModel().getColumnCount() - 1, false, true);
        }
        @Override public void mouseReleased(MouseEvent e) {
            listSelection.clearSelection();
            pressedRowIndex = -1;
            rollOverRowIndex = -1;
            repaint();
        }
    }
}

class RowDataModel extends DefaultTableModel {
    private static final ColumnContext[] COLUMN_ARRAY = {
        // new ColumnContext("No.", Integer.class, false),
        new ColumnContext("Name", String.class, false),
        new ColumnContext("Comment", String.class, false)
    };
    private int number;
    private final DefaultListModel<String> rowListModel;
    protected RowDataModel(DefaultListModel<String> lm) {
        super();
        rowListModel = lm;
    }
    public void addRowData(RowData t) {
        Object[] obj = {t.getName(), t.getComment()};
        super.addRow(obj);
        rowListModel.addElement("row" + number);
        number++;
    }
    @Override public void removeRow(int index) {
        super.removeRow(index);
        rowListModel.remove(index);
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
        public final String columnName;
        public final Class<?> columnClass;
        public final boolean isEditable;
        protected ColumnContext(String columnName, Class<?> columnClass, boolean isEditable) {
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
