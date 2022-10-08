// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Objects;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    String[] columnNames = {"No.", "Name", "URL"};
    DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
      @Override public Class<?> getColumnClass(int column) {
        switch (column) {
          case 0: return Integer.class;
          case 1: return String.class;
          case 2: return URL.class;
          default: return super.getColumnClass(column);
        }
      }

      @Override public boolean isCellEditable(int row, int col) {
        return false;
      }
    };
    model.addRow(new Object[] {0, "FrontPage", mkUrl("https://ateraimemo.com/")});
    model.addRow(new Object[] {1, "Java Swing Tips", mkUrl("https://ateraimemo.com/Swing.html")});
    model.addRow(new Object[] {2, "Example", mkUrl("https://www.example.com/")});
    model.addRow(new Object[] {3, "Example.jp", mkUrl("https://www.example.jp/")});

    JTable table = new JTable(model) {
      private final Color evenColor = new Color(0xFA_FA_FA);
      @Override public Component prepareRenderer(TableCellRenderer tcr, int row, int column) {
        Component c = super.prepareRenderer(tcr, row, column);
        c.setForeground(getForeground());
        c.setBackground(row % 2 == 0 ? evenColor : getBackground());
        return c;
      }
    };
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

    UrlRenderer renderer = new UrlRenderer();
    table.setDefaultRenderer(URL.class, renderer);
    table.addMouseListener(renderer);
    table.addMouseMotionListener(renderer);

    JScrollPane scrollPane = new JScrollPane(table);
    scrollPane.getViewport().setBackground(Color.WHITE);
    add(scrollPane);
    setPreferredSize(new Dimension(320, 240));
  }

  private static URL mkUrl(String path) {
    try {
      return new URL(path);
    } catch (MalformedURLException ex) {
      ex.printStackTrace();
      return null;
    }
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

class UrlRenderer extends DefaultTableCellRenderer implements MouseListener, MouseMotionListener {
  // private static final Rectangle CELL_RECT = new Rectangle();
  // private static final Rectangle ICON_RECT = new Rectangle();
  // private static final Rectangle TEXT_RECT = new Rectangle();
  private int viewRowIndex = -1;
  private int viewColumnIndex = -1;
  private boolean isRollover;

  @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    Component c = super.getTableCellRendererComponent(
        table, value, isSelected, false, row, column);
    if (c instanceof JLabel) {
      JLabel l = (JLabel) c;
      // @see https://ateraimemo.com/Swing/ClippedHtmlLabel.html
      // String str = SwingUtilities.layoutCompoundLabel(...);
      String str = Objects.toString(value, "");
      if (isRolloverCell(table, row, column)) {
        l.setText("<html><u><font color='blue'>" + str);
      } else if (hasFocus) {
        l.setText("<html><font color='blue'>" + str);
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
  // private static boolean pointInsidePrefSize(JTable table, Point p) {
  //   int row = table.rowAtPoint(p);
  //   int col = table.columnAtPoint(p);
  //   TableCellRenderer tcr = table.getCellRenderer(row, col);
  //   Object value = table.getValueAt(row, col);
  //   Component cell = tcr.getTableCellRendererComponent(table, value, false, false, row, col);
  //   Dimension itemSize = cell.getPreferredSize();
  //   Insets i = ((JComponent) cell).getInsets();
  //   Rectangle cellBounds = table.getCellRect(row, col, false);
  //   cellBounds.width = itemSize.width - i.right - i.left;
  //   cellBounds.translate(i.left, i.top);
  //   return cellBounds.contains(p);
  // }

  private static boolean isUrlColumn(JTable table, int column) {
    return column >= 0 && table.getColumnClass(column).equals(URL.class);
  }

  @Override public void mouseMoved(MouseEvent e) {
    JTable table = (JTable) e.getComponent();
    Point pt = e.getPoint();
    final int prevRow = viewRowIndex;
    final int prevCol = viewColumnIndex;
    final boolean prevRollover = isRollover;
    viewRowIndex = table.rowAtPoint(pt);
    viewColumnIndex = table.columnAtPoint(pt);
    isRollover = isUrlColumn(table, viewColumnIndex); // && pointInsidePrefSize(table, pt);
    if (viewRowIndex == prevRow && viewColumnIndex == prevCol && isRollover == prevRollover) {
      return;
    }
    if (!isRollover && !prevRollover) {
      return;
    }
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
    if (isUrlColumn(table, viewColumnIndex)) {
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
    if (isUrlColumn(table, col)) { // && pointInsidePrefSize(table, pt)) {
      int crow = table.rowAtPoint(pt);
      URL url = (URL) table.getValueAt(crow, col);
      // System.out.println(url);
      try {
        // Web Start
        // BasicService bs = (BasicService) ServiceManager.lookup("javax.jnlp.BasicService");
        // bs.showDocument(url);
        if (Desktop.isDesktopSupported()) {
          Desktop.getDesktop().browse(url.toURI());
        }
      } catch (URISyntaxException | IOException ex) {
        ex.printStackTrace();
        UIManager.getLookAndFeel().provideErrorFeedback(e.getComponent());
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
