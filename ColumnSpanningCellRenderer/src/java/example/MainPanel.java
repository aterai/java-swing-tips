// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Locale;
import java.util.Objects;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

public final class MainPanel extends JPanel {
  private static final String SEE = "See Also: Constan Field Values";
  private final String[] columnNames = {"AAA", "BBB"};
  private final Object[][] data = {
    {makeOptionPaneDescription("error"), SEE},
    {makeOptionPaneDescription("information"), SEE},
    {makeOptionPaneDescription("question"), SEE},
    {makeOptionPaneDescription("warning"), SEE},
  };
  private final TableModel model = new DefaultTableModel(data, columnNames) {
    @Override public boolean isCellEditable(int row, int column) {
      return false;
    }
  };
  private final JTable table = new JTable(model);

  private MainPanel() {
    super(new BorderLayout());
    table.setAutoCreateRowSorter(true);
    table.getTableHeader().setReorderingAllowed(false);
    table.setRowSelectionAllowed(true);
    // table.setFocusable(false);
    table.setFillsViewportHeight(true);
    table.setShowVerticalLines(false);
    table.setIntercellSpacing(new Dimension(0, 1));
    table.setRowHeight(56);
    TableCellRenderer renderer = new ColumnSpanningCellRenderer();
    for (int i = 0; i < table.getColumnModel().getColumnCount(); i++) {
      TableColumn c = table.getColumnModel().getColumn(i);
      c.setCellRenderer(renderer);
      c.setMinWidth(50);
    }
    add(new JScrollPane(table));
    setPreferredSize(new Dimension(320, 240));
  }

  private static OptionPaneDescription makeOptionPaneDescription(String type) {
    String key = type + "Icon";
    Icon icon = UIManager.getIcon("OptionPane." + key);
    String fmt = "public static final int %s_MESSAGE%nUsed for %s messages.";
    String msg = String.format(fmt, type.toUpperCase(Locale.ENGLISH), type);
    return new OptionPaneDescription(key, icon, msg);
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
      ex.printStackTrace();
      Toolkit.getDefaultToolkit().beep();
    }
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

class ColumnSpanningCellRenderer extends JPanel implements TableCellRenderer {
  private static final int TARGET_COLIDX = 0;
  private final JTextArea textArea = new JTextArea(2, 999_999);
  private final JLabel label = new JLabel();
  private final JLabel iconLabel = new JLabel();
  private final JScrollPane scroll = new JScrollPane(textArea);

  protected ColumnSpanningCellRenderer() {
    super(new BorderLayout());

    scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
    scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    scroll.setBorder(BorderFactory.createEmptyBorder());
    scroll.setViewportBorder(BorderFactory.createEmptyBorder());
    scroll.setOpaque(false);
    scroll.getViewport().setOpaque(false);

    textArea.setBorder(BorderFactory.createEmptyBorder());
    textArea.setMargin(new Insets(0, 0, 0, 0));
    textArea.setForeground(Color.RED);
    textArea.setEditable(false);
    textArea.setFocusable(false);
    textArea.setOpaque(false);

    iconLabel.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
    iconLabel.setOpaque(false);

    Border b1 = BorderFactory.createEmptyBorder(2, 2, 2, 2);
    Border b2 = BorderFactory.createMatteBorder(0, 0, 1, 1, Color.GRAY);
    label.setBorder(BorderFactory.createCompoundBorder(b2, b1));

    setBackground(textArea.getBackground());
    setOpaque(true);
    add(label, BorderLayout.NORTH);
    add(scroll);
  }

  @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    OptionPaneDescription d;
    if (value instanceof OptionPaneDescription) {
      d = (OptionPaneDescription) value;
      add(iconLabel, BorderLayout.WEST);
    } else {
      String title = Objects.toString(value, "");
      int mrow = table.convertRowIndexToModel(row);
      Object o = table.getModel().getValueAt(mrow, 0);
      if (o instanceof OptionPaneDescription) {
        OptionPaneDescription t = (OptionPaneDescription) o;
        d = new OptionPaneDescription(title, t.icon, t.text);
      } else {
        d = new OptionPaneDescription(title, null, "");
      }
      remove(iconLabel);
    }
    label.setText(d.title);
    textArea.setText(d.text);
    iconLabel.setIcon(d.icon);

    Rectangle cr = table.getCellRect(row, column, false);
    // // Flickering on first visible row ?
    // if (column == TARGET_COLIDX) {
    //   cr.x = 0;
    //   cr.width -= iconLabel.getPreferredSize().width;
    // } else {
    //   cr.x -= iconLabel.getPreferredSize().width;
    // }
    // textArea.scrollRectToVisible(cr);
    if (column != TARGET_COLIDX) {
      cr.x -= iconLabel.getPreferredSize().width;
    }
    scroll.getViewport().setViewPosition(cr.getLocation());
    if (isSelected) {
      setBackground(Color.ORANGE);
    } else {
      setBackground(Color.WHITE);
    }
    return this;
  }
}

class OptionPaneDescription {
  public final String title;
  public final Icon icon;
  public final String text;

  protected OptionPaneDescription(String title, Icon icon, String text) {
    this.title = title;
    this.icon = icon;
    this.text = text;
  }
}
