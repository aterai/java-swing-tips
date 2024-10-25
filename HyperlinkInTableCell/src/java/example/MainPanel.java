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
import java.util.Optional;
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
      private transient UrlRenderer handler;

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

        handler = new UrlRenderer();
        setDefaultRenderer(URL.class, handler);
        addMouseListener(handler);
        addMouseMotionListener(handler);
      }
    };
    JScrollPane scrollPane = new JScrollPane(table);
    scrollPane.getViewport().setBackground(Color.WHITE);
    add(scrollPane);
    setPreferredSize(new Dimension(320, 240));
  }

  private static URL makeUrl(String path) {
    Optional<URL> op;
    try {
      op = Optional.of(new URL(path));
    } catch (MalformedURLException ex) {
      op = Optional.empty();
    }
    return op.orElse(null);
  }

  private static TableModel makeModel() {
    String[] columnNames = {"No.", "Name", "URL"};
    Object[][] data = {
        {0, "FrontPage", makeUrl("https://ateraimemo.com/")},
        {1, "Java Swing Tips", makeUrl("https://ateraimemo.com/Swing.html")},
        {2, "Example", makeUrl("http://www.example.com/")},
        {3, "example.jp", makeUrl("http://www.example.jp/")}
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

class UrlRenderer extends DefaultTableCellRenderer implements MouseListener, MouseMotionListener {
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

  private static boolean isUrlColumn(JTable table, int column) {
    return column >= 0 && table.getColumnClass(column).equals(URL.class);
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
    isRollover = isUrlColumn(table, viewColumnIndex);
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
    if (isUrlColumn(table, col)) {
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
