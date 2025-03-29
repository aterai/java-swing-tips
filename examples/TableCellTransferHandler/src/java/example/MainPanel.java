// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.Objects;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

public final class MainPanel extends JPanel {
  // DataFlavor FLAVOR = new ActivationDataFlavor(
  //     JTable.class, DataFlavor.javaJVMLocalObjectMimeType, "JTable");
  private MainPanel() {
    super(new BorderLayout());
    JTable table = new JTable(makeModel());
    CellIconTransferHandler handler = new CellIconTransferHandler();
    table.setTransferHandler(handler);
    table.setCellSelectionEnabled(true);
    table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    table.setDragEnabled(true);
    table.setFillsViewportHeight(true);
    TableRowSorter<TableModel> sorter = new TableRowSorter<>(table.getModel());
    table.setRowSorter(sorter);

    // // Disable JTable rows Cut, Copy, Paste
    // ActionMap am = table.getActionMap();
    // Action empty = new AbstractAction() {
    //   @Override public void actionPerformed(ActionEvent e) {
    //     /* do nothing */
    //   }
    // };
    // am.put(TransferHandler.getCutAction().getValue(Action.NAME), empty);
    // am.put(TransferHandler.getCopyAction().getValue(Action.NAME), empty);
    // am.put(TransferHandler.getPasteAction().getValue(Action.NAME), empty);

    DefaultListModel<Icon> model = new DefaultListModel<>();
    JList<Icon> list = new JList<>(model);
    list.setLayoutOrientation(JList.HORIZONTAL_WRAP);
    list.setVisibleRowCount(0);
    list.setFixedCellWidth(16);
    list.setFixedCellHeight(16);
    list.setCellRenderer(new IconListCellRenderer<>());
    list.setTransferHandler(handler);

    JButton clearButton = new JButton("clear");
    clearButton.addActionListener(e -> {
      model.clear();
      sorter.setRowFilter(null);
    });

    RowFilter<TableModel, Integer> filter = new RowFilter<TableModel, Integer>() {
      @Override public boolean include(Entry<? extends TableModel, ? extends Integer> entry) {
        Object o = entry.getModel().getValueAt(entry.getIdentifier(), 1);
        return model.isEmpty() || model.contains(o);
      }
    };
    JButton filterButton = new JButton("filter");
    filterButton.addActionListener(e -> sorter.setRowFilter(filter));

    // // PMD IllegalArgumentException ???
    // // <? super javax.swing.table.TableModel> cannot be a wildcard bound
    // JButton button = new JButton("filter");
    // button.addActionListener(e -> sorter.setRowFilter(new RowFilter<TableModel, Integer>() {
    //   @Override public boolean include(Entry<? extends TableModel, ? extends Integer> entry) {
    //     Object o = entry.getModel().getValueAt(entry.getIdentifier(), 1);
    //     return model.isEmpty() || model.contains(o);
    //   }
    // }));

    Box box = Box.createHorizontalBox();
    box.add(clearButton);
    box.add(filterButton);

    JPanel p = new JPanel(new BorderLayout(5, 5));
    p.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    p.add(new JScrollPane(list));
    p.add(box, BorderLayout.EAST);

    add(p, BorderLayout.SOUTH);
    add(new JScrollPane(table));
    setPreferredSize(new Dimension(320, 240));
  }

  private static DefaultTableModel makeModel() {
    String[] columnNames = {"String", "Icon", "Boolean"};
    Object[][] data = {
        {"aaa", new ColorIcon(Color.RED), true},
        {"bbb", new ColorIcon(Color.GREEN), false},
        {"ccc", new ColorIcon(Color.BLUE), true},
        {"ddd", new ColorIcon(Color.ORANGE), true},
        {"eee", new ColorIcon(Color.PINK), false},
        {"fff", new ColorIcon(Color.CYAN), true},
    };
    return new DefaultTableModel(data, columnNames) {
      @SuppressWarnings("PMD.OnlyOneReturn")
      @Override public Class<?> getColumnClass(int column) {
        switch (column) {
          case 0: return String.class;
          case 1: return Icon.class;
          case 2: return Boolean.class;
          default: return super.getColumnClass(column);
        }
        // Java 12
        // return switch (column) {
        //   case 0 -> String.class;
        //   case 1 -> Icon.class;
        //   case 2 -> Boolean.class;
        //   default -> super.getColumnClass(column);
        // };
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

class CellIconTransferHandler extends TransferHandler {
  public static final DataFlavor ICON_FLAVOR = new DataFlavor(Icon.class, "Icon");

  @Override protected Transferable createTransferable(JComponent c) {
    Object o = null;
    if (c instanceof JTable) {
      JTable table = (JTable) c;
      int row = table.getSelectedRow();
      int col = table.getSelectedColumn();
      if (Icon.class.isAssignableFrom(table.getColumnClass(col))) {
        o = table.getValueAt(row, col);
      }
    }
    Object transferData = o;
    return new Transferable() {
      @Override public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[]{ICON_FLAVOR};
      }

      @Override public boolean isDataFlavorSupported(DataFlavor flavor) {
        return Objects.equals(ICON_FLAVOR, flavor);
      }

      @Override public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
        if (isDataFlavorSupported(flavor) && transferData != null) {
          return transferData;
        } else {
          throw new UnsupportedFlavorException(flavor);
        }
      }
    };
  }

  @Override public boolean canImport(TransferSupport info) {
    Component c = info.getComponent();
    return c instanceof JList && info.isDataFlavorSupported(ICON_FLAVOR);
  }

  @Override public int getSourceActions(JComponent c) {
    return COPY;
  }

  @SuppressWarnings("unchecked")
  @Override public boolean importData(TransferSupport info) {
    boolean inserted;
    Component c = info.getComponent();
    try {
      Object o = info.getTransferable().getTransferData(ICON_FLAVOR);
      if (c instanceof JList && o instanceof Icon) {
        ((DefaultListModel<Object>) ((JList<?>) c).getModel()).addElement(o);
      }
      inserted = true;
    } catch (UnsupportedFlavorException | IOException ex) {
      inserted = false;
    }
    return inserted;
  }
}

class IconListCellRenderer<E extends Icon> implements ListCellRenderer<E> {
  private final JLabel renderer = new JLabel();

  @Override public Component getListCellRendererComponent(JList<? extends E> list, E value, int index, boolean isSelected, boolean cellHasFocus) {
    renderer.setIcon(value);
    return renderer;
  }
}

class ColorIcon implements Icon {
  private final Color color;

  protected ColorIcon(Color color) {
    this.color = color;
  }

  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.translate(x, y);
    g2.setPaint(color);
    g2.fillRect(1, 1, getIconWidth() - 2, getIconHeight() - 2);
    g2.dispose();
  }

  @Override public int getIconWidth() {
    return 12;
  }

  @Override public int getIconHeight() {
    return 12;
  }
}
