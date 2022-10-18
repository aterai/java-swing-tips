// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import javax.swing.*;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.ComboPopup;
import javax.swing.plaf.metal.MetalComboBoxUI;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    List<List<Object>> aseries = new ArrayList<>();
    aseries.add(Arrays.asList("A1", 594, 841));
    aseries.add(Arrays.asList("A2", 420, 594));
    aseries.add(Arrays.asList("A3", 297, 420));
    aseries.add(Arrays.asList("A4", 210, 297));
    aseries.add(Arrays.asList("A5", 148, 210));
    aseries.add(Arrays.asList("A6", 105, 148));

    String[] columns = {"A series", "width", "height"};

    JTextField wtf = new JTextField(5);
    wtf.setEditable(false);

    JTextField htf = new JTextField(5);
    htf.setEditable(false);

    DefaultTableModel model = new DefaultTableModel(null, columns) {
      @Override public Class<?> getColumnClass(int column) {
        return column == 1 || column == 2 ? Integer.class : String.class;
      }

      @Override public boolean isCellEditable(int row, int column) {
        return false;
      }
    };

    DropdownTableComboBox<List<Object>> combo = new DropdownTableComboBox<>(aseries, model);
    // combo.addActionListener(e -> {
    //   List<Object> rowData = combo.getSelectedRow();
    //   wtf.setText(Objects.toString(rowData.get(1)));
    //   htf.setText(Objects.toString(rowData.get(2)));
    // });
    combo.addItemListener(e -> {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        List<Object> rowData = combo.getSelectedRow();
        wtf.setText(Objects.toString(rowData.get(1)));
        htf.setText(Objects.toString(rowData.get(2)));
      }
    });
    ListCellRenderer<? super List<Object>> renderer = combo.getRenderer();
    combo.setRenderer((list, value, index, isSelected, cellHasFocus) -> {
      Component c = renderer.getListCellRendererComponent(
          list, value, index, isSelected, cellHasFocus);
      if (isSelected) {
        c.setBackground(list.getSelectionBackground());
        c.setForeground(list.getSelectionForeground());
      } else {
        c.setBackground(list.getBackground());
        c.setForeground(list.getForeground());
      }
      if (c instanceof JLabel) {
        JLabel l = (JLabel) c;
        l.setOpaque(true);
        l.setText(Objects.toString(value.get(0), ""));
      }
      return c;
    });

    EventQueue.invokeLater(() -> combo.setSelectedIndex(3));

    Box box = Box.createHorizontalBox();
    box.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    box.add(combo);
    box.add(Box.createHorizontalStrut(15));
    box.add(new JLabel("width: "));
    box.add(wtf);
    box.add(Box.createHorizontalStrut(5));
    box.add(new JLabel("height: "));
    box.add(htf);
    box.add(Box.createHorizontalGlue());

    add(box, BorderLayout.NORTH);
    setPreferredSize(new Dimension(320, 240));
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

class DropdownTableComboBox<E extends List<Object>> extends JComboBox<E> {
  private final JTable table = new JTable() {
    private transient HighlightListener mouseHandler;
    @Override public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
      Component c = super.prepareRenderer(renderer, row, column);
      c.setForeground(Color.BLACK);
      if (mouseHandler != null && mouseHandler.isHighlightTableRow(row)) {
        c.setBackground(new Color(0xFF_C8_C8));
      } else if (isRowSelected(row)) {
        c.setBackground(Color.CYAN);
      } else {
        c.setBackground(Color.WHITE);
      }
      return c;
    }

    @Override public void updateUI() {
      removeMouseListener(mouseHandler);
      removeMouseMotionListener(mouseHandler);
      super.updateUI();
      mouseHandler = new HighlightListener();
      addMouseListener(mouseHandler);
      addMouseMotionListener(mouseHandler);
      getTableHeader().setReorderingAllowed(false);
    }
  };
  private final List<E> list = new ArrayList<>();

  protected DropdownTableComboBox(List<E> list, DefaultTableModel model) {
    super();
    this.list.addAll(list);
    table.setModel(model);
    Object[] a = new Object[0];
    for (E v : list) {
      addItem(v);
      model.addRow(v.toArray(a));
    }
    // list.forEach(this::addItem);
    // list.forEach(v -> model.addRow(v.toArray(a)));
  }

  @Override public void updateUI() {
    super.updateUI();
    EventQueue.invokeLater(() -> {
      setUI(new MetalComboBoxUI() {
        @Override protected ComboPopup createPopup() {
          return new ComboTablePopup(comboBox, table);
        }
      });
      setEditable(false);
    });
  }

  public List<Object> getSelectedRow() {
    return list.get(getSelectedIndex());
  }
}

class ComboTablePopup extends BasicComboPopup {
  private final JTable table;
  private final JScrollPane scroll;

  // Java 8: protected ComboTablePopup(JComboBox<?> combo, JTable table) {
  // Java 9: protected ComboTablePopup(JComboBox<Object> combo, JTable table) {
  @SuppressWarnings("unchecked")
  protected ComboTablePopup(JComboBox combo, JTable table) {
    super(combo);
    this.table = table;

    ListSelectionModel sm = table.getSelectionModel();
    sm.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    sm.addListSelectionListener(e -> combo.setSelectedIndex(table.getSelectedRow()));

    combo.addItemListener(e -> {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        setRowSelection(combo.getSelectedIndex());
      }
    });

    table.addMouseListener(new MouseAdapter() {
      @Override public void mousePressed(MouseEvent e) {
        combo.setSelectedIndex(table.rowAtPoint(e.getPoint()));
        setVisible(false);
      }
    });

    scroll = new JScrollPane(table);
    setBorder(BorderFactory.createEmptyBorder());
  }

  // Java 9: @SuppressWarnings("deprecation")
  // @Override public void show() {
  //   if (isEnabled()) {
  //     Insets ins = scroll.getInsets();
  //     int tableHeight = table.getPreferredSize().height;
  //     int headerHeight = table.getTableHeader().getPreferredSize().height;
  //     int scrollHeight = tableHeight + headerHeight + ins.top + ins.bottom;
  //     scroll.setPreferredSize(new Dimension(240, scrollHeight));
  //     super.removeAll();
  //     super.add(scroll);
  //     setRowSelection(comboBox.getSelectedIndex());
  //     super.show(comboBox, 0, comboBox.getBounds().height);
  //   }
  // }

  @Override protected void togglePopup() {
    if (!isVisible()) {
      Insets ins = scroll.getInsets();
      int tableHeight = table.getPreferredSize().height;
      int headerHeight = table.getTableHeader().getPreferredSize().height;
      int scrollHeight = tableHeight + headerHeight + ins.top + ins.bottom;
      scroll.setPreferredSize(new Dimension(240, scrollHeight));
      super.removeAll();
      super.add(scroll);
    }
    super.togglePopup();
  }

  private void setRowSelection(int index) {
    if (index != -1) {
      table.setRowSelectionInterval(index, index);
      table.scrollRectToVisible(table.getCellRect(index, 0, true));
    }
  }
}

class HighlightListener extends MouseAdapter {
  private int viewRowIdx = -1;

  public boolean isHighlightTableRow(int row) {
    return this.viewRowIdx == row;
  }

  private void setHighlightTableCell(MouseEvent e) {
    Point pt = e.getPoint();
    Component c = e.getComponent();
    if (c instanceof JTable) {
      viewRowIdx = ((JTable) c).rowAtPoint(pt);
      c.repaint();
    }
  }

  @Override public void mouseMoved(MouseEvent e) {
    setHighlightTableCell(e);
  }

  @Override public void mouseDragged(MouseEvent e) {
    setHighlightTableCell(e);
  }

  @Override public void mouseExited(MouseEvent e) {
    viewRowIdx = -1;
    e.getComponent().repaint();
  }
}
