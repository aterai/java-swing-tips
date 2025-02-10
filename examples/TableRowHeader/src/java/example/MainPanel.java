// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

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
    // table.setAutoCreateRowSorter(true);
    table.setCellSelectionEnabled(true);
    table.setRowHeight(20);

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
    rowHeader.setFixedCellHeight(table.getRowHeight());

    JScrollPane scroll = new JScrollPane(table);
    scroll.setRowHeaderView(rowHeader);
    scroll.getRowHeader().addChangeListener(e ->
        scroll.getVerticalScrollBar().setValue(((JViewport) e.getSource()).getViewPosition().y));
    scroll.setComponentPopupMenu(new TablePopupMenu());
    table.setInheritsPopupMenu(true);

    rowHeader.setBackground(Color.BLUE);
    scroll.setBackground(Color.RED);
    scroll.getViewport().setBackground(Color.GREEN);

    add(scroll);
    setPreferredSize(new Dimension(320, 240));
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (UnsupportedLookAndFeelException ignored) {
      Toolkit.getDefaultToolkit().beep();
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
      ex.printStackTrace();
      return;
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
  protected final transient ListSelectionModel tableSelection;
  protected final transient ListSelectionModel listSelection = getSelectionModel();
  protected int rollOverRowIndex = -1;
  protected int pressedRowIndex = -1;
  private transient MouseAdapter handler;

  protected RowHeaderList(ListModel<E> model, JTable table) {
    super(model);
    this.table = table;
    // setSelectionModel(table.getSelectionModel());
    tableSelection = table.getSelectionModel();
  }

  @Override public void updateUI() {
    removeMouseListener(handler);
    removeMouseMotionListener(handler);
    super.updateUI();
    handler = new RollOverListener();
    addMouseListener(handler);
    addMouseMotionListener(handler);
    setCellRenderer(new RowHeaderRenderer<>());
    // setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.GRAY.brighter()));
  }

  public class RowHeaderRenderer<F> implements ListCellRenderer<F> {
    private final JLabel renderer = new JLabel(); // new DefaultListCellRenderer();

    protected RowHeaderRenderer() {
      super();
      renderer.setOpaque(true);
      // renderer.setBorder(UIManager.getBorder("TableHeader.cellBorder"));
      renderer.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, Color.GRAY.brighter()));
      renderer.setHorizontalAlignment(SwingConstants.CENTER);
      // renderer.setForeground(header.getForeground());
      // renderer.setBackground(header.getBackground());
      // renderer.setFont(header.getFont());
    }

    @Override public Component getListCellRendererComponent(JList<? extends F> list, F value, int index, boolean isSelected, boolean cellHasFocus) {
      // Component c = renderer.getListCellRendererComponent(
      //     list, value, index, isSelected, cellHasFocus);
      // Component c = renderer;
      JTableHeader header = table.getTableHeader();
      renderer.setFont(header.getFont());
      if (index == pressedRowIndex) {
        renderer.setBackground(Color.GRAY);
      } else if (index == rollOverRowIndex) {
        renderer.setBackground(Color.WHITE);
      } else if (isSelected) {
        renderer.setBackground(Color.GRAY.brighter());
      } else {
        renderer.setForeground(header.getForeground());
        renderer.setBackground(header.getBackground());
      }
      renderer.setText(Objects.toString(value, ""));
      return renderer;
    }
  }

  private final class RollOverListener extends MouseAdapter {
    @Override public void mouseExited(MouseEvent e) {
      if (pressedRowIndex < 0) {
        // pressedRowIndex = -1;
        rollOverRowIndex = -1;
        e.getComponent().repaint();
      }
    }

    @Override public void mouseMoved(MouseEvent e) {
      int row = locationToIndex(e.getPoint());
      if (row != rollOverRowIndex) {
        rollOverRowIndex = row;
        e.getComponent().repaint();
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
        e.getComponent().repaint();
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
      // ColumnModel cm = table.getColumnModel()
      // cm.getSelectionModel().setAnchorSelectionIndex(0);
      // cm.getSelectionModel().setLeadSelectionIndex(0);
      // table.changeSelection(pressedRowIndex, cm.getColumnCount() - 1, false, true);
    }

    @Override public void mouseReleased(MouseEvent e) {
      listSelection.clearSelection();
      pressedRowIndex = -1;
      rollOverRowIndex = -1;
      e.getComponent().repaint();
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
  private final String name;
  private final String comment;

  protected RowData(String name, String comment) {
    this.name = name;
    this.comment = comment;
  }

  public String getName() {
    return name;
  }

  public String getComment() {
    return comment;
  }
}

final class TablePopupMenu extends JPopupMenu {
  private final JMenuItem delete;

  /* default */ TablePopupMenu() {
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
