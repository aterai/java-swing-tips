// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Objects;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.ComboPopup;
import javax.swing.plaf.metal.MetalComboBoxUI;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTextField wtf = new JTextField(5);
    wtf.setEditable(false);

    JTextField htf = new JTextField(5);
    htf.setEditable(false);

    TableModel model = makeTableModel();
    JComboBox<PaperSize> combo = new DropdownTableComboBox(PaperSize.values(), model);
    combo.addItemListener(e -> {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        PaperSize item = (PaperSize) e.getItem();
        wtf.setText(Integer.toString(item.getWidth()));
        htf.setText(Integer.toString(item.getHeight()));
      }
    });
    ListCellRenderer<? super PaperSize> renderer = combo.getRenderer();
    combo.setRenderer(new PaperSizeListCellRenderer(renderer));
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

  private static TableModel makeTableModel() {
    String[] columnNames = {"A series", "width", "height"};
    DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
      @Override public Class<?> getColumnClass(int column) {
        return column == 0 ? String.class : Integer.class;
      }

      @Override public boolean isCellEditable(int row, int column) {
        return false;
      }
    };
    for (PaperSize v : PaperSize.values()) {
      Object[] row = {v.getSeries(), v.getWidth(), v.getHeight()};
      model.addRow(row);
    }
    return model;
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

enum PaperSize {
  A1("A1", 594, 841),
  A2("A2", 420, 594),
  A3("A3", 297, 420),
  A4("A4", 210, 297),
  A5("A5", 148, 210),
  A6("A6", 105, 148);
  private final String series;
  private final int width;
  private final int height;

  /* default */ PaperSize(String series, int width, int height) {
    this.series = series;
    this.width = width;
    this.height = height;
  }

  /* default */ String getSeries() {
    return series;
  }

  /* default */ int getWidth() {
    return width;
  }

  /* default */ int getHeight() {
    return height;
  }

  @Override public String toString() {
    return String.format("%s(%dx%d)", series, width, height);
  }
}

class PaperSizeListCellRenderer implements ListCellRenderer<PaperSize> {
  private final ListCellRenderer<? super PaperSize> renderer;

  protected PaperSizeListCellRenderer(ListCellRenderer<? super PaperSize> renderer) {
    this.renderer = renderer;
  }

  @Override public Component getListCellRendererComponent(JList<? extends PaperSize> list, PaperSize value, int index, boolean isSelected, boolean cellHasFocus) {
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
      l.setText(Objects.isNull(value) ? "" : value.getSeries());
    }
    return c;
  }
}

class DropdownTableComboBox extends JComboBox<PaperSize> {
  private final JTable table = new DropdownTable();

  protected DropdownTableComboBox(PaperSize[] paperSizes, TableModel tableModel) {
    super(paperSizes);
    table.setModel(tableModel);
  }

  @Override public void updateUI() {
    super.updateUI();
    EventQueue.invokeLater(() -> {
      setUI(new MetalComboBoxUI() {
        @Override protected ComboPopup createPopup() {
          return new ComboTablePopup(comboBox, table);
        }
      });
    });
  }
}

class DropdownTable extends JTable {
  private transient RowHighlightListener highlighter;

  @Override public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
    Component c = super.prepareRenderer(renderer, row, column);
    if (highlighter != null && highlighter.isHighlightedRow(row)) {
      c.setForeground(getSelectionForeground());
      c.setBackground(getSelectionBackground().brighter());
    } else if (isRowSelected(row)) {
      c.setForeground(getSelectionForeground());
      c.setBackground(getSelectionBackground());
    } else {
      c.setForeground(getForeground());
      c.setBackground(getBackground());
    }
    return c;
  }

  @Override public void updateUI() {
    removeMouseListener(highlighter);
    removeMouseMotionListener(highlighter);
    super.updateUI();
    highlighter = new RowHighlightListener();
    addMouseListener(highlighter);
    addMouseMotionListener(highlighter);
    setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    getTableHeader().setReorderingAllowed(false);
  }
}

class ComboTablePopup extends BasicComboPopup {
  private static final int POPUP_WIDTH = 240;
  private final JTable table;
  private final JScrollPane scroll;
  private final transient ItemListener itemListener;
  private final transient MouseListener mouseListener;

  // Java 8: protected ComboTablePopup(JComboBox<?> combo, JTable table) {
  // Java 9: protected ComboTablePopup(JComboBox<Object> combo, JTable table) {
  @SuppressWarnings("unchecked")
  protected ComboTablePopup(JComboBox combo, JTable table) {
    super(combo);
    this.table = table;
    this.scroll = new JScrollPane(table);

    itemListener = e -> {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        setRowSelection(combo.getSelectedIndex());
      }
    };
    combo.addItemListener(itemListener);

    mouseListener = new MouseAdapter() {
      @Override public void mousePressed(MouseEvent e) {
        int row = table.rowAtPoint(e.getPoint());
        if (row >= 0) {
          combo.setSelectedIndex(row);
          setVisible(false);
        }
      }
    };
    table.addMouseListener(mouseListener);
  }

  @Override public void uninstallingUI() {
    comboBox.removeItemListener(itemListener);
    table.removeMouseListener(mouseListener);
    super.uninstallingUI();
  }

  // JPopupMenu#setVisible(true) is called from both BasicComboPopup#show()
  // (keyboard: Alt+Down, F4, ...) and #togglePopup() (mouse click)
  @Override public void setVisible(boolean visible) {
    if (visible) {
      Insets ins = scroll.getInsets();
      int tableHeight = table.getPreferredSize().height;
      int headerHeight = table.getTableHeader().getPreferredSize().height;
      int scrollHeight = tableHeight + headerHeight + ins.top + ins.bottom;
      scroll.setPreferredSize(new Dimension(POPUP_WIDTH, scrollHeight));
      removeAll();
      add(scroll);
      setBorderPainted(false);
      // setBorder(BorderFactory.createEmptyBorder());
    }
    super.setVisible(visible);
  }

  private void setRowSelection(int index) {
    if (index >= 0) {
      table.setRowSelectionInterval(index, index);
      table.scrollRectToVisible(table.getCellRect(index, 0, true));
    }
  }
}

class RowHighlightListener extends MouseAdapter {
  private int highlightedRow = -1;

  public boolean isHighlightedRow(int row) {
    return highlightedRow == row;
  }

  private void updateHighlightedRow(MouseEvent e) {
    Component c = e.getComponent();
    if (c instanceof JTable) {
      int row = ((JTable) c).rowAtPoint(e.getPoint());
      if (row != highlightedRow) {
        highlightedRow = row;
        c.repaint();
      }
    }
  }

  @Override public void mouseMoved(MouseEvent e) {
    updateHighlightedRow(e);
  }

  @Override public void mouseDragged(MouseEvent e) {
    updateHighlightedRow(e);
  }

  @Override public void mouseExited(MouseEvent e) {
    highlightedRow = -1;
    e.getComponent().repaint();
  }
}
