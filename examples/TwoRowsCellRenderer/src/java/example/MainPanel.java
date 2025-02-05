// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Objects;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTable table = new JTable(makeModel()) {
      @Override public void updateUI() {
        setSelectionForeground(null); // Nimbus
        setSelectionBackground(null); // Nimbus
        super.updateUI();
        setDefaultRenderer(String.class, new TwoRowsCellRenderer());
      }
    };
    table.setAutoCreateRowSorter(true);
    table.setRowHeight(table.getRowHeight() * 2);
    add(new JScrollPane(table));
    setPreferredSize(new Dimension(320, 240));
  }

  private static TableModel makeModel() {
    String[] columnNames = {"A", "B"};
    Object[][] data = {
        {"123456789012345678901234567890123456789012345678901234567890", "12345"},
        {"bbb", "abcdefghijklmnopqrstuvwxyz----abcdefghijklmnopqrstuvwxyz"},
    };
    return new DefaultTableModel(data, columnNames) {
      @Override public boolean isCellEditable(int row, int column) {
        return false;
      }

      @Override public Class<?> getColumnClass(int column) {
        return String.class;
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

class TwoRowsCellRenderer implements TableCellRenderer {
  private final JLabel top = new JLabel();
  private final JLabel bottom = new JLabel();
  private final JPanel renderer = new JPanel(new GridLayout(2, 1, 0, 0));

  protected TwoRowsCellRenderer() {
    renderer.add(top);
    renderer.add(bottom);
  }

  @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    if (isSelected) {
      top.setForeground(table.getSelectionForeground());
      bottom.setForeground(table.getSelectionForeground());
      renderer.setBackground(table.getSelectionBackground());
    } else {
      top.setForeground(table.getForeground());
      bottom.setForeground(table.getForeground());
      renderer.setBackground(table.getBackground());
    }
    top.setFont(table.getFont());
    bottom.setFont(table.getFont());
    FontMetrics fm = top.getFontMetrics(top.getFont());
    String text = Objects.toString(value, "");
    String first = text;
    String second = "";
    int columnWidth = table.getCellRect(0, column, false).width;
    int textWidth = 0;
    // for (int i = 0; i < text.length(); i++) {
    //   textWidth += fm.charWidth(text.charAt(i));
    //   if (textWidth > columnWidth) {
    //     first = text.substring(0, i - 1);
    //     second = text.substring(i - 1);
    //     break;
    //   }
    // }

    // // @see Unicode surrogate programming with the Java language
    // // https://www.ibm.com/developerworks/library/j-unicode/index.html
    // // https://www.ibm.com/developerworks/jp/ysl/library/java/j-unicode_surrogate/index.html
    // char[] ach = text.toCharArray();
    // int len = ach.length;
    // int[] acp = new int[Character.codePointCount(ach, 0, len)];
    // int j = 0;
    // int cp;
    // for (int i = 0; i < len; i += Character.charCount(cp)) {
    //   cp = Character.codePointAt(ach, i);
    //   acp[j++] = cp;
    // }
    // for (int i = 0; i < acp.length; i++) {
    //   textWidth += fm.charWidth(acp[i]);
    //   if (textWidth > columnWidth) {
    //     first = new String(acp, 0, i);
    //     second = new String(acp, i, acp.length - i);
    //     break;
    //   }
    // }

    int i = 0;
    while (i < text.length()) {
      int cp = text.codePointAt(i);
      textWidth += fm.charWidth(cp);
      if (textWidth > columnWidth) {
        first = text.substring(0, i);
        second = text.substring(i);
        break;
      }
      i += Character.charCount(cp);
    }
    top.setText(first);
    bottom.setText(second);
    return renderer;
  }
}
