// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Objects;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    int defaultHeight = 20;
    JTable table = new JTable(makeModel()) {
      @Override public void updateUI() {
        super.updateUI();
        setAutoCreateRowSorter(true);
        setSurrendersFocusOnKeystroke(true);
        setRowHeight(defaultHeight);
        setDefaultRenderer(RowHeader.class, new RowHeaderRenderer());
        setDefaultEditor(RowHeader.class, new RowHeaderEditor());
        TableColumn column = getColumnModel().getColumn(1);
        column.setCellRenderer(new TextAreaCellRenderer());
        column.setPreferredWidth(160);
      }
    };
    table.getModel().addTableModelListener(e -> {
      int mc = e.getColumn();
      int mr = e.getFirstRow();
      int vc = table.convertColumnIndexToView(mc);
      int vr = table.convertRowIndexToView(mr);
      Object o = table.getValueAt(vr, vc);
      if (mc == 0 && o instanceof RowHeader) {
        RowHeader rh = (RowHeader) o;
        int vc1 = table.convertColumnIndexToView(1);
        TableCellRenderer r = table.getColumnModel().getColumn(vc1).getCellRenderer();
        Object v = table.getValueAt(vr, vc1);
        Component c = r.getTableCellRendererComponent(table, v, true, true, vr, vc1);
        int h = rh.isSelected() ? c.getPreferredSize().height : defaultHeight;
        table.setRowHeight(vr, h);
      }
    });
    add(new JScrollPane(table));
    setPreferredSize(new Dimension(320, 240));
  }

  private static TableModel makeModel() {
    String[] columnNames = {"RowHeader", "Description"};
    Object[][] data = {
        {new RowHeader("aaa", true), "0000\n1\n2\n3\n4\n5\n6\n7\n8\n9\n10"},
        {new RowHeader("bbb", false), "1111111"},
        {new RowHeader("ccc", true), "2222222222222\n1\n2\n3"}
    };
    return new DefaultTableModel(data, columnNames) {
      @Override public Class<?> getColumnClass(int column) {
        return getValueAt(0, column).getClass();
      }

      @Override public boolean isCellEditable(int row, int column) {
        return column == 0;
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

class TextAreaCellRenderer implements TableCellRenderer {
  private final JTextArea textArea = new JTextArea();

  protected TextAreaCellRenderer() {
    super();
    textArea.setLineWrap(true);
    textArea.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
  }

  @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    textArea.setFont(table.getFont());
    textArea.setText(Objects.toString(value, ""));
    textArea.setSize(table.getCellRect(row, column, true).width, 0);
    return textArea;
  }
}

class RowHeader {
  private final String title;
  private final boolean expandable;
  private final boolean selected;

  protected RowHeader(String title, boolean expandable) {
    this(title, expandable, false);
  }

  protected RowHeader(String title, boolean expandable, boolean selected) {
    this.title = title;
    this.expandable = expandable;
    this.selected = selected;
  }

  public String getTitle() {
    return title;
  }

  public boolean isExpandable() {
    return expandable;
  }

  public boolean isSelected() {
    return selected;
  }

  @Override public boolean equals(Object o) {
    return this == o || o instanceof RowHeader && equals2((RowHeader) o);
  }

  private boolean equals2(RowHeader rh) {
    return isExpandable() == rh.isExpandable()
        && isSelected() == rh.isSelected()
        && Objects.equals(getTitle(), rh.getTitle());
  }

  @Override public int hashCode() {
    return Objects.hash(getTitle(), isExpandable(), isSelected());
  }
}

class RowHeaderPanel extends JPanel {
  public final JLabel label = new JLabel(" ");
  public final JCheckBox check = new JCheckBox();

  protected RowHeaderPanel() {
    super(new BorderLayout());
    label.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 0));
    check.setOpaque(false);
    check.setFont(check.getFont().deriveFont(8f));
    check.setIcon(new CheckIcon());
    Box box = Box.createHorizontalBox();
    box.add(label);
    box.add(Box.createHorizontalGlue());
    box.add(check);
    add(box, BorderLayout.NORTH);
  }

  @Override public final void add(Component comp, Object constraints) {
    super.add(comp, constraints);
  }

  // @Override public void updateUI() {
  //   super.updateUI();
  //   setOpaque(true);
  // }

  // @Override public boolean isOpaque() {
  //   return true;
  // }
}

class RowHeaderRenderer implements TableCellRenderer {
  private final RowHeaderPanel renderer = new RowHeaderPanel();

  @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    if (value instanceof RowHeader) {
      RowHeader rh = (RowHeader) value;
      renderer.check.setVisible(rh.isExpandable());
      renderer.check.setSelected(rh.isSelected());
      renderer.label.setText(rh.getTitle());
    }
    return renderer;
  }
}

class RowHeaderEditor extends AbstractCellEditor implements TableCellEditor {
  private final RowHeaderPanel renderer = new RowHeaderPanel();
  private transient RowHeader rowHeader;

  protected RowHeaderEditor() {
    super();
    renderer.check.addActionListener(e -> {
      if (rowHeader != null) {
        String title = rowHeader.getTitle();
        boolean expandable = rowHeader.isExpandable();
        boolean selected = renderer.check.isSelected();
        rowHeader = new RowHeader(title, expandable, selected);
      }
      fireEditingStopped();
    });
    renderer.addMouseListener(new MouseAdapter() {
      @Override public void mousePressed(MouseEvent e) {
        fireEditingStopped();
      }
    });
  }

  @Override public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
    if (value instanceof RowHeader) {
      RowHeader rh = (RowHeader) value;
      renderer.check.setVisible(rh.isExpandable());
      renderer.label.setText(rh.getTitle());
      String title = rh.getTitle();
      boolean expandable = rh.isExpandable();
      boolean selected = rh.isExpandable() && renderer.check.isSelected();
      rowHeader = new RowHeader(title, expandable, selected);
    }
    return renderer;
  }

  @Override public Object getCellEditorValue() {
    return rowHeader;
  }
}

class CheckIcon implements Icon {
  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setFont(c.getFont());
    if (c instanceof AbstractButton) {
      ButtonModel m = ((AbstractButton) c).getModel();
      String txt = m.isSelected() ? "∧" : "∨";
      g2.drawString(txt, x, y + 10);
    }
    g2.dispose();
  }

  @Override public int getIconWidth() {
    return 12;
  }

  @Override public int getIconHeight() {
    return 12;
  }
}
