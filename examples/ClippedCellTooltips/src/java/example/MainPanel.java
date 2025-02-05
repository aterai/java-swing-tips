// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Objects;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTable table = new JTable(makeModel()) {
      @Override public Component prepareRenderer(TableCellRenderer tcr, int row, int column) {
        Component c = super.prepareRenderer(tcr, row, column);
        if (c instanceof JComponent) {
          JComponent label = (JComponent) c;
          Rectangle rect = getCellRect(row, column, false);
          String str = Objects.toString(getValueAt(row, column), "");
          label.setToolTipText(isOmitted(label, rect, str) ? str : getToolTipText());
        }
        return c;
      }

      @Override public void updateUI() {
        super.updateUI();
        TableCellRenderer r = new ToolTipHeaderRenderer();
        for (int i = 0; i < getColumnModel().getColumnCount(); i++) {
          getColumnModel().getColumn(i).setHeaderRenderer(r);
        }
      }
    };
    table.setAutoCreateRowSorter(true);

    add(new JScrollPane(table));
    setPreferredSize(new Dimension(320, 240));
  }

  private static boolean isOmitted(JComponent c, Rectangle rect, String str) {
    Insets i = c.getInsets();
    int rectWidth = rect.width - i.left - i.right;
    FontMetrics fm = c.getFontMetrics(c.getFont());
    int cellTextWidth = fm.stringWidth(str);
    return cellTextWidth > rectWidth;
  }

  private static TableModel makeModel() {
    String[] columnNames = {"String-String/String", "Integer", "Boolean"};
    Object[][] data = {
        {"1234567890123456789012345678901234567890", 12, true},
        {"BBB", 2, true}, {"EEE", 3, false},
        {"CCC", 4, true}, {"FFF", 5, false},
        {"DDD", 6, true}, {"GGG", 7, false}
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
      Logger.getGlobal().severe(ex::getMessage);
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
      Rectangle rect = header.getHeaderRect(column);
      rect.width -= i.left + i.right;
      l.setToolTipText(isClipped(l, rect) ? l.getText() : header.getToolTipText());
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
