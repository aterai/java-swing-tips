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
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

public final class MainPanel extends JPanel {
  private static final String SEE = "See Also: Constant Field Values";

  private MainPanel() {
    super(new BorderLayout());
    JTable table = new JTable(makeModel()) {
      @Override public void updateUI() {
        setColumnCellRenderer(null);
        super.updateUI();
        TableCellRenderer r = new ColumnSpanningCellRenderer();
        setColumnCellRenderer(r);
      }

      private void setColumnCellRenderer(TableCellRenderer renderer) {
        TableColumnModel cm = getColumnModel();
        for (int i = 0; i < cm.getColumnCount(); i++) {
          TableColumn c = cm.getColumn(i);
          c.setCellRenderer(renderer);
          c.setMinWidth(50);
        }
      }
    };
    table.setAutoCreateRowSorter(true);
    table.getTableHeader().setReorderingAllowed(false);
    table.setRowSelectionAllowed(true);
    // table.setFocusable(false);
    table.setFillsViewportHeight(true);
    table.setShowVerticalLines(false);
    table.setIntercellSpacing(new Dimension(0, 1));
    table.setRowHeight(56);
    add(new JScrollPane(table));
    setPreferredSize(new Dimension(320, 240));
  }

  private static TableModel makeModel() {
    String[] columnNames = {"A", "B"};
    Object[][] data = {
        {makeOptionPaneDescription("error"), SEE},
        {makeOptionPaneDescription("information"), SEE},
        {makeOptionPaneDescription("question"), SEE},
        {makeOptionPaneDescription("warning"), SEE},
    };
    return new DefaultTableModel(data, columnNames) {
      @Override public boolean isCellEditable(int row, int column) {
        return false;
      }
    };
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

class ColumnSpanningCellRenderer implements TableCellRenderer {
  private static final int TARGET_IDX = 0;
  private final JTextArea textArea = new JTextArea(2, 999_999);
  private final JLabel label = new JLabel();
  private final JLabel iconLabel = new JLabel();
  private final JScrollPane scroll = new JScrollPane(textArea);
  private final JPanel renderer = new JPanel(new BorderLayout());

  protected ColumnSpanningCellRenderer() {
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

    renderer.setOpaque(true);
    renderer.add(label, BorderLayout.NORTH);
    renderer.add(scroll);
  }

  @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    OptionPaneDescription d;
    if (value instanceof OptionPaneDescription) {
      d = (OptionPaneDescription) value;
      renderer.add(iconLabel, BorderLayout.WEST);
    } else {
      String title = Objects.toString(value, "");
      int mri = table.convertRowIndexToModel(row);
      Object o = table.getModel().getValueAt(mri, 0);
      if (o instanceof OptionPaneDescription) {
        OptionPaneDescription t = (OptionPaneDescription) o;
        d = new OptionPaneDescription(title, t.getIcon(), t.getText());
      } else {
        d = new OptionPaneDescription(title, null, "");
      }
      renderer.remove(iconLabel);
    }
    label.setText(d.getTitle());
    textArea.setText(d.getText());
    iconLabel.setIcon(d.getIcon());

    Rectangle cr = table.getCellRect(row, column, false);
    // // Flickering on first visible row ?
    // if (column == TARGET_IDX) {
    //   cr.x = 0;
    //   cr.width -= iconLabel.getPreferredSize().width;
    // } else {
    //   cr.x -= iconLabel.getPreferredSize().width;
    // }
    // textArea.scrollRectToVisible(cr);
    if (column != TARGET_IDX) {
      cr.x -= iconLabel.getPreferredSize().width;
    }
    scroll.getViewport().setViewPosition(cr.getLocation());
    Color bgc = isSelected ? Color.ORANGE : Color.WHITE;
    renderer.setBackground(bgc);
    textArea.setBackground(bgc); // Nimbus???
    return renderer;
  }
}

class OptionPaneDescription {
  private final String title;
  private final Icon icon;
  private final String text;

  protected OptionPaneDescription(String title, Icon icon, String text) {
    this.title = title;
    this.icon = icon;
    this.text = text;
  }

  public String getTitle() {
    return title;
  }

  public Icon getIcon() {
    return icon;
  }

  public String getText() {
    return text;
  }
}
