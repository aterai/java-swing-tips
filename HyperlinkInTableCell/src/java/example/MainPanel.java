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
    JTable table = new JTable(makeModel()) {
      private final Color evenColor = new Color(0xFA_FA_FA);
      private transient UriRenderer handler;

      @Override public Component prepareRenderer(TableCellRenderer tcr, int row, int column) {
        Component c = super.prepareRenderer(tcr, row, column);
        c.setForeground(getForeground());
        c.setBackground(row % 2 == 0 ? evenColor : getBackground());
        return c;
      }

      @Override public void updateUI() {
        removeMouseListener(handler);
        removeMouseMotionListener(handler);
        super.updateUI();
        setRowSelectionAllowed(true);
        setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        setIntercellSpacing(new Dimension());
        setShowGrid(false);
        putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        setAutoCreateRowSorter(true);

        TableColumnModel cm = getColumnModel();
        TableColumn col = cm.getColumn(0);
        col.setMinWidth(50);
        col.setMaxWidth(50);
        col.setResizable(false);
        cm.getColumn(1).setPreferredWidth(1000);
        cm.getColumn(2).setPreferredWidth(2000);

        handler = new UriRenderer();
        setDefaultRenderer(URI.class, handler);
        addMouseListener(handler);
        addMouseMotionListener(handler);
      }
    };
    JScrollPane scrollPane = new JScrollPane(table);
    scrollPane.getViewport().setBackground(Color.WHITE);
    add(scrollPane);
    setPreferredSize(new Dimension(320, 240));
  }

  private static URI makeUri(String path) {
    Optional<URI> op;
    try {
      op = Optional.of(new URI(path));
    } catch (URISyntaxException ex) {
      op = Optional.empty();
    }
    return op.orElse(null);
  }

  private static TableModel makeModel() {
    String[] columnNames = {"No.", "Name", "URI"};
    Object[][] data = {
        {0, "FrontPage", makeUri("https://ateraimemo.com/")},
        {1, "Java Swing Tips", makeUri("https://ateraimemo.com/Swing.html")},
        {2, "Example", makeUri("http://www.example.com/")},
        {3, "example.jp", makeUri("http://www.example.jp/")}
    };
    return new DefaultTableModel(data, columnNames) {
      @Override public Class<?> getColumnClass(int column) {
        return getValueAt(0, column).getClass();
      }

      @Override public boolean isCellEditable(int row, int col) {
        return false;
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

class UriRenderer extends DefaultTableCellRenderer implements MouseListener, MouseMotionListener {
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
    isRollover = isUriColumn(table, viewColumnIndex);
    boolean isNotRollover = isRollover == prevRollover && !isRollover; // && !prevRollover;

    if (isSameCell && isNotRollover) {
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
    if (isUriColumn(table, col)) {
      int crow = table.rowAtPoint(pt);
      URI uri = (URI) table.getValueAt(crow, col);
      // System.out.println(uri);
      try {
        // Web Start
        // BasicService bs = (BasicService) ServiceManager.lookup("javax.jnlp.BasicService");
        // bs.showDocument(uri);
        if (Desktop.isDesktopSupported()) {
          Desktop.getDesktop().browse(uri);
        }
      } catch (IOException ex) {
        // Logger.getGlobal().severe(ex::getMessage);
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
