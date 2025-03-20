// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Objects;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.JTextComponent;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new GridLayout(2, 1));
    JTable table = new JTable(makeModel());
    table.setAutoCreateRowSorter(true);
    table.setFillsViewportHeight(true);
    // table.setFocusable(false);
    // table.setCellSelectionEnabled(false);
    table.setSelectionForeground(Color.BLACK);
    table.setSelectionBackground(new Color(0xEE_EE_EE));

    JTextField field = new JTextField();
    field.setEditable(false);
    // field.setSelectedTextColor(Color.BLACK);
    // field.setSelectionColor(Color.GREEN);
    // field.setForeground(table.getSelectionForeground());
    field.setBackground(table.getSelectionBackground());
    field.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
    field.setComponentPopupMenu(new TextComponentPopupMenu());

    // DefaultCellEditor cellEditor = new DefaultCellEditor(field) {
    //   @Override public boolean isCellEditable(EventObject e) {
    //     if (e instanceof MouseEvent && e.getSource() instanceof JTable) {
    //       Point pt = ((MouseEvent) e).getPoint();
    //       JTable t = (JTable) e.getSource();
    //       int row = t.rowAtPoint(pt);
    //       int col = t.columnAtPoint(pt);
    //       TableCellRenderer tcr = t.getCellRenderer(row, col);
    //       Object value = t.getValueAt(row, col);
    //       Component cell = tcr.getTableCellRendererComponent(t, value, false, false, row, col);
    //       Rectangle cellRect = t.getCellRect(row, col, false);
    //       cellRect.width = cell.getPreferredSize().width;
    //       return cellRect.contains(pt);
    //     }
    //     return super.isCellEditable(e);
    //   }
    // };
    DefaultCellEditor cellEditor = new DefaultCellEditor(field);
    cellEditor.setClickCountToStart(1);
    table.setDefaultEditor(Object.class, cellEditor);

    TableCellRenderer r = new DefaultTableCellRenderer();
    table.setDefaultRenderer(Object.class, (tbl, value, isSelected, hasFocus, row, column) ->
        r.getTableCellRendererComponent(tbl, value, isSelected, false, row, column));

    add(new JScrollPane(table));
    add(new JScrollPane(new JTextArea()));
    setPreferredSize(new Dimension(320, 240));
  }

  private static TableModel makeModel() {
    String[] columnNames = {"A", "B", "C"};
    Object[][] data = {
        {"aaa", "bb bb bb bb", "ccc ccc"}, {"bbb", "ff", "ggg oo pp"},
        {"CCC", "kkk", "jj"}, {"DDD", "ii mm nn", "hhh hhh lll"}
    };
    return new DefaultTableModel(data, columnNames) {
      @Override public Class<?> getColumnClass(int column) {
        return getValueAt(0, column).getClass();
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

final class TextComponentPopupMenu extends JPopupMenu {
  private final Action copyAction = new DefaultEditorKit.CopyAction();

  /* default */ TextComponentPopupMenu() {
    super();
    add(copyAction);
  }

  @Override public void show(Component c, int x, int y) {
    if (c instanceof JTextComponent) {
      JTextComponent tc = (JTextComponent) c;
      boolean hasSelectedText = Objects.nonNull(tc.getSelectedText());
      copyAction.setEnabled(hasSelectedText);
      super.show(c, x, y);
    }
  }
}
