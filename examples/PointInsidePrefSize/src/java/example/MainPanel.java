// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTable table = new JTable(makeModel());
    table.setRowSelectionAllowed(true);
    table.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
    table.setIntercellSpacing(new Dimension());
    table.setShowGrid(false);
    table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
    table.setAutoCreateRowSorter(true);

    TableColumnModel cm = table.getColumnModel();
    TableColumn col = cm.getColumn(0);
    col.setMinWidth(50);
    col.setMaxWidth(50);
    col.setResizable(false);

    cm.getColumn(1).setPreferredWidth(1000);
    cm.getColumn(2).setPreferredWidth(2000);

    UriRenderer renderer = new UriRenderer();
    table.setDefaultRenderer(URI.class, renderer);
    table.addMouseListener(renderer);
    table.addMouseMotionListener(renderer);

    JScrollPane scrollPane = new JScrollPane(table);
    scrollPane.getViewport().setBackground(Color.WHITE);
    add(scrollPane);
    setPreferredSize(new Dimension(320, 240));
  }

  private static TableModel makeModel() {
    String[] columnNames = {"No.", "Name", "URI"};
    DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
      @SuppressWarnings("PMD.OnlyOneReturn")
      @Override public Class<?> getColumnClass(int column) {
        switch (column) {
          case 0: return Integer.class;
          case 1: return String.class;
          case 2: return URI.class;
          default: return super.getColumnClass(column);
        }
      }

      @Override public boolean isCellEditable(int row, int col) {
        return false;
      }
    };
    // // Java 12:
    // DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
    //   @Override public Class<?> getColumnClass(int column) {
    //     return switch (column) {
    //       case 0 -> Integer.class;
    //       case 1 -> String.class;
    //       case 2 -> URI.class;
    //       default -> super.getColumnClass(column);
    //     };
    //   }
    //
    //   @Override public boolean isCellEditable(int row, int col) {
    //     return false;
    //   }
    // };
    model.addRow(new Object[] {0, "FrontPage", toUri("https://ateraimemo.com/")});
    model.addRow(new Object[] {1, "Java Swing Tips", toUri("https://ateraimemo.com/Swing.html")});
    model.addRow(new Object[] {2, "Example", toUri("https://www.example.com/")});
    model.addRow(new Object[] {3, "Example.jp", toUri("https://www.example.jp/")});
    return model;
  }

  private static URI toUri(String path) {
    Optional<URI> op;
    try {
      op = Optional.of(new URI(path));
    } catch (URISyntaxException ex) {
      op = Optional.empty();
    }
    return op.orElse(null);
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

class UriRenderer extends DefaultTableCellRenderer implements MouseListener, MouseMotionListener {
  private static final Rectangle CELL_RECT = new Rectangle();
  private static final Rectangle ICON_RECT = new Rectangle();
  private static final Rectangle TEXT_RECT = new Rectangle();
  private int viewRowIndex = -1;
  private int viewColumnIndex = -1;
  private boolean isRollover;

  @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    Component c = super.getTableCellRendererComponent(
        table, value, isSelected, false, row, column);
    if (c instanceof JLabel) {
      JLabel l = (JLabel) c;
      TableColumnModel cm = table.getColumnModel();
      TableColumn col = cm.getColumn(column);
      Insets i = l.getInsets();
      CELL_RECT.x = i.left;
      CELL_RECT.y = i.top;
      CELL_RECT.width = col.getWidth() - cm.getColumnMargin() - i.right - CELL_RECT.x;
      CELL_RECT.height = table.getRowHeight(row) - table.getRowMargin() - i.bottom - CELL_RECT.y;
      ICON_RECT.setBounds(0, 0, 0, 0);
      TEXT_RECT.setBounds(0, 0, 0, 0);
      String str = SwingUtilities.layoutCompoundLabel(
          l,
          l.getFontMetrics(l.getFont()),
          Objects.toString(value, ""),
          l.getIcon(),
          l.getVerticalAlignment(),
          l.getHorizontalAlignment(),
          l.getVerticalTextPosition(),
          l.getHorizontalTextPosition(),
          CELL_RECT,
          ICON_RECT,
          TEXT_RECT,
          l.getIconTextGap());

      if (isRolloverCell(table, row, column)) {
        l.setText("<html><u><font color='blue'>" + str);
      } else {
        l.setText(str);
      }
    }
    return c;
  }

  protected boolean isRolloverCell(JTable table, int row, int column) {
    return !table.isEditing() && viewRowIndex == row && viewColumnIndex == column && isRollover;
  }

  // @see SwingUtilities2.pointOutsidePrefSize(...)
  private static boolean pointInsidePrefSize(JTable table, Point p) {
    int row = table.rowAtPoint(p);
    int col = table.columnAtPoint(p);
    TableCellRenderer tcr = table.getCellRenderer(row, col);
    Object value = table.getValueAt(row, col);
    Component cell = tcr.getTableCellRendererComponent(table, value, false, false, row, col);
    Dimension itemSize = cell.getPreferredSize();
    Insets i = ((JComponent) cell).getInsets();
    Rectangle cellBounds = table.getCellRect(row, col, false);
    cellBounds.width = itemSize.width - i.right - i.left;
    cellBounds.translate(i.left, i.top);
    return cellBounds.contains(p);
  }

  private static boolean isUriColumn(JTable table, int column) {
    return column >= 0 && table.getColumnClass(column).equals(URI.class);
  }

  @Override public void mouseMoved(MouseEvent e) {
    JTable table = (JTable) e.getComponent();
    Point pt = e.getPoint();
    int prevRow = viewRowIndex;
    int prevCol = viewColumnIndex;
    viewRowIndex = table.rowAtPoint(pt);
    viewColumnIndex = table.columnAtPoint(pt);
    boolean isSameCell = viewRowIndex == prevRow && viewColumnIndex == prevCol;

    boolean prevRollover = isRollover;
    isRollover = isUriColumn(table, viewColumnIndex) && pointInsidePrefSize(table, pt);
    boolean isNotRollover = isRollover == prevRollover && !isRollover; // && !prevRollover;

    if (isSameCell && isNotRollover) {
      return;
    }

    // if (viewRowIndex == prevRow && viewColumnIndex == prevCol && isRollover == prevRollover) {
    //   return;
    // }
    // if (!isRollover && !prevRollover) {
    //   return;
    // }

    // >>>> HyperlinkCellRenderer.java
    // @see https://github.com/sjas/swingset3/blob/master/trunk/SwingSet3/src/com/sun/swingset3/demos/table/HyperlinkCellRenderer.java
    Rectangle repaintRect;
    if (isRollover) {
      Rectangle r = table.getCellRect(viewRowIndex, viewColumnIndex, false);
      repaintRect = prevRollover ? r.union(table.getCellRect(prevRow, prevCol, false)) : r;
    } else { // if (prevRollover) {
      repaintRect = table.getCellRect(prevRow, prevCol, false);
    }
    table.repaint(repaintRect);
    // <<<<
    // table.repaint();
  }

  @Override public void mouseExited(MouseEvent e) {
    JTable table = (JTable) e.getComponent();
    if (isUriColumn(table, viewColumnIndex)) {
      table.repaint(table.getCellRect(viewRowIndex, viewColumnIndex, false));
      viewRowIndex = -1;
      viewColumnIndex = -1;
      isRollover = false;
    }
  }

  @Override public void mouseClicked(MouseEvent e) {
    JTable table = (JTable) e.getComponent();
    Point pt = e.getPoint();
    int col = table.columnAtPoint(pt);
    if (isUriColumn(table, col) && pointInsidePrefSize(table, pt)) {
      int crow = table.rowAtPoint(pt);
      URI uri = (URI) table.getValueAt(crow, col);
      if (Desktop.isDesktopSupported()) {
        try {
          Desktop.getDesktop().browse(uri);
        } catch (IOException ex) {
          UIManager.getLookAndFeel().provideErrorFeedback(e.getComponent());
        }
      }
    }
  }

  @Override public void mouseDragged(MouseEvent e) {
    /* not needed */
  }

  @Override public void mouseEntered(MouseEvent e) {
    /* not needed */
  }

  @Override public void mousePressed(MouseEvent e) {
    /* not needed */
  }

  @Override public void mouseReleased(MouseEvent e) {
    /* not needed */
  }
}
