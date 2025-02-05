// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.EventObject;
import java.util.Objects;
import java.util.Optional;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

public final class MainPanel extends JPanel {
  public static final int CELL_SIZE = 24;

  private MainPanel() {
    super(new GridBagLayout());
    JScrollPane scroll = new JScrollPane(makeTable());
    scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
    scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    add(scroll);
    setPreferredSize(new Dimension(320, 240));
  }

  private JTable makeTable() {
    String[] columnNames = {"A", "B", "C", "D", "E", "F", "G", "H", "I"};
    Boolean[][] data = {
        {true, false, true, false, true, true, false, true, true},
        {true, false, true, false, true, true, false, true, true},
        {true, false, true, false, true, true, false, true, true},
        {true, false, true, false, true, true, false, true, true},
        {true, false, true, false, true, true, false, true, true},
        {true, false, true, false, true, true, false, true, true},
        {true, false, true, false, true, true, false, true, true},
        {true, false, true, false, true, true, false, true, true},
        {true, false, true, false, true, true, false, true, true}
    };
    TableModel model = new DefaultTableModel(data, columnNames) {
      @Override public Class<?> getColumnClass(int column) {
        return Boolean.class;
      }
    };
    return new JTable(model) {
      @Override public void updateUI() {
        setDefaultEditor(Boolean.class, null);
        super.updateUI();
        setDefaultEditor(Boolean.class, new BooleanEditor());
        setCellSelectionEnabled(true);
        setAutoResizeMode(AUTO_RESIZE_OFF);
        setAutoCreateRowSorter(true);
        setComponentPopupMenu(new TablePopupMenu());
        TableColumnModel m = getColumnModel();
        for (int i = 0; i < m.getColumnCount(); i++) {
          TableColumn col = m.getColumn(i);
          col.setPreferredWidth(CELL_SIZE);
          col.setResizable(false);
        }
      }

      @Override public Dimension getPreferredScrollableViewportSize() {
        return super.getPreferredSize();
      }
    };
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

final class TablePopupMenu extends JPopupMenu {
  private final JMenuItem select;
  private final JMenuItem clear;
  private final JMenuItem toggle;

  /* default */ TablePopupMenu() {
    super();
    select = add("select");
    select.addActionListener(e -> initAllTableValue((JTable) getInvoker(), true));

    clear = add("clear");
    clear.addActionListener(e -> initAllTableValue((JTable) getInvoker(), false));

    toggle = add("toggle");
    toggle.addActionListener(e -> toggleTableValue((JTable) getInvoker()));
  }

  private static void initAllTableValue(JTable table, boolean value) {
    for (int row : table.getSelectedRows()) {
      for (int col : table.getSelectedColumns()) {
        table.setValueAt(value, row, col);
      }
    }
  }

  private static void toggleTableValue(JTable table) {
    for (int row : table.getSelectedRows()) {
      for (int col : table.getSelectedColumns()) {
        Boolean b = (Boolean) table.getValueAt(row, col);
        table.setValueAt(!b, row, col);
      }
    }
  }

  @Override public void show(Component c, int x, int y) {
    if (c instanceof JTable) {
      boolean isSelected = ((JTable) c).getSelectedRowCount() > 0;
      select.setEnabled(isSelected);
      clear.setEnabled(isSelected);
      toggle.setEnabled(isSelected);
      super.show(c, x, y);
    }
  }
}

// @see JTable.BooleanEditor
// class BooleanEditor extends DefaultCellEditor {
//   protected BooleanEditor() {
//     super(new JCheckBox());
//     JCheckBox check = (JCheckBox) getComponent();
//     check.setHorizontalAlignment(SwingConstants.CENTER);
//   }
//
//   @Override public boolean isCellEditable(EventObject e) {
//     if (e instanceof MouseEvent) {
//       MouseEvent me = (MouseEvent) e;
//       return !(me.isShiftDown() || me.isControlDown());
//     }
//     return super.isCellEditable(e);
//   }
// }

class BooleanEditor extends AbstractCellEditor implements TableCellEditor {
  protected final Container renderer = new JPanel(new GridBagLayout()) {
    private transient MouseListener listener;
    @Override public void updateUI() {
      removeMouseListener(listener);
      super.updateUI();
      listener = new MouseAdapter() {
        @Override public void mousePressed(MouseEvent e) {
          fireEditingStopped();
        }
      };
      addMouseListener(listener);
    }
  };
  protected final JCheckBox checkBox = new JCheckBox() {
    private transient Handler handler;
    @Override public void updateUI() {
      removeActionListener(handler);
      removeMouseListener(handler);
      super.updateUI();
      setBorder(UIManager.getBorder("Table.noFocusBorder"));
      setOpaque(false);
      setFocusable(false);
      setRolloverEnabled(false);
      handler = new Handler();
      addActionListener(handler);
      addMouseListener(handler);
    }
  };

  @Override public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
    checkBox.setSelected(Objects.equals(value, Boolean.TRUE));
    renderer.add(checkBox);
    return renderer;
  }

  @Override public Object getCellEditorValue() {
    return checkBox.isSelected();
  }

  @Override public boolean isCellEditable(EventObject e) {
    return e instanceof MouseEvent ? isEditableKey((MouseEvent) e) : super.isCellEditable(e);
  }

  private static boolean isEditableKey(MouseEvent e) {
    return !(e.isShiftDown() || e.isControlDown());
  }

  private final class Handler extends MouseAdapter implements ActionListener {
    @Override public void actionPerformed(ActionEvent e) {
      fireEditingStopped();
    }

    @Override public void mousePressed(MouseEvent e) {
      Container c = SwingUtilities.getAncestorOfClass(JTable.class, e.getComponent());
      if (c instanceof JTable) {
        JTable t = (JTable) c;
        boolean isPressed = checkBox.getModel().isPressed();
        if (isPressed && t.isRowSelected(t.getEditingRow()) && e.isControlDown()) {
          renderer.setBackground(t.getBackground());
        } else {
          renderer.setBackground(t.getSelectionBackground());
        }
      }
    }

    @Override public void mouseExited(MouseEvent e) {
      Class<JTable> clz = JTable.class;
      Optional.ofNullable(SwingUtilities.getAncestorOfClass(clz, e.getComponent()))
          .filter(clz::isInstance).map(clz::cast)
          .filter(JTable::isEditing)
          .ifPresent(JTable::removeEditor);
    }
  }
}
