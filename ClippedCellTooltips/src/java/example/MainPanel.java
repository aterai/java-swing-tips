// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Objects;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    String[] columnNames = {"String-String/String", "Integer", "Boolean"};
    Object[][] data = {
        {"1234567890123456789012345678901234567890", 12, true},
        {"BBB", 2, true}, {"EEE", 3, false},
        {"CCC", 4, true}, {"FFF", 5, false},
        {"DDD", 6, true}, {"GGG", 7, false}
    };
    TableModel model = new DefaultTableModel(data, columnNames) {
      @Override public Class<?> getColumnClass(int column) {
        return getValueAt(0, column).getClass();
      }
    };
    JTable table = new JTable(model) {
      @Override public Component prepareRenderer(TableCellRenderer tcr, int row, int column) {
        Component c = super.prepareRenderer(tcr, row, column);
        if (c instanceof JComponent) {
          JComponent l = (JComponent) c;
          Insets i = l.getInsets();
          Rectangle rect = getCellRect(row, column, false);
          rect.width -= i.left + i.right;
          FontMetrics fm = l.getFontMetrics(l.getFont());
          String str = Objects.toString(getValueAt(row, column), "");
          int cellTextWidth = fm.stringWidth(str);
          l.setToolTipText(cellTextWidth > rect.width ? str : getToolTipText());
        }
        return c;
      }

      @Override public void updateUI() {
        super.updateUI();
        TableCellRenderer r = new ToolTipHeaderRenderer();
        TableColumnModel columnModel = getColumnModel();
        for (int i = 0; i < columnModel.getColumnCount(); i++) {
          columnModel.getColumn(i).setHeaderRenderer(r);
        }
      }
    };
    table.setAutoCreateRowSorter(true);

    add(new JScrollPane(table));
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

class ToolTipHeaderRenderer implements TableCellRenderer {
  // private final Icon icon = UIManager.getIcon("Table.ascendingSortIcon");

  @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    JTableHeader header = table.getTableHeader();
    TableCellRenderer r = header.getDefaultRenderer();
    Component c = r.getTableCellRendererComponent(
        table, value, isSelected, hasFocus, row, column);
    if (c instanceof JLabel) {
      JLabel l = (JLabel) c;
      Insets i = l.getInsets();
      Rectangle rect = header.getHeaderRect(column); // = table.getCellRect(row, column, false);
      rect.width -= i.left + i.right;
      boolean isClipped = isClipped(l, rect);
      // isClipped = l.getFontMetrics(l.getFont()).stringWidth(l.getText()) > rect.width;
      l.setToolTipText(isClipped ? l.getText() : header.getToolTipText());
    }
    return c;
  }

  private static boolean isClipped(JLabel label, Rectangle viewR) {
    Rectangle iconR = new Rectangle();
    Rectangle textR = new Rectangle();
    String str = SwingUtilities.layoutCompoundLabel(
        label,
        label.getFontMetrics(label.getFont()),
        label.getText(),
        label.getIcon(),
        label.getVerticalAlignment(),
        label.getHorizontalAlignment(),
        label.getVerticalTextPosition(),
        label.getHorizontalTextPosition(),
        viewR,
        iconR,
        textR,
        label.getIconTextGap());
    return !Objects.equals(label.getText(), str);
  }
}
