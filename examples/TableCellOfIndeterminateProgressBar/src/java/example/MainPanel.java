// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.plaf.basic.BasicProgressBarUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

public final class MainPanel extends JPanel {
  private final DefaultTableModel model = makeModel();
  private final JTable table = new JTable(model) {
    @Override public void updateUI() {
      super.updateUI();
      removeColumn(getColumnModel().getColumn(3));
      JProgressBar progress = new JProgressBar();
      TableColumn tc = getColumnModel().getColumn(2);
      tc.setCellRenderer(new ProgressTableCellRenderer(progress));
    }
  };
  private final Set<Integer> deletedRowSet = new TreeSet<>();
  private int number;

  private MainPanel() {
    super(new BorderLayout());
    JScrollPane scrollPane = new JScrollPane(table);
    scrollPane.getViewport().setBackground(Color.WHITE);

    table.setRowSorter(new TableRowSorter<>(model));
    table.setComponentPopupMenu(new TablePopupMenu());
    table.setFillsViewportHeight(true);
    table.setIntercellSpacing(new Dimension());
    table.setShowGrid(false);
    table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

    TableColumn column = table.getColumnModel().getColumn(0);
    column.setMaxWidth(60);
    column.setMinWidth(60);
    column.setResizable(false);

    JButton button = new JButton("add");
    button.addActionListener(e -> addActionPerformed());
    add(button, BorderLayout.SOUTH);
    add(scrollPane);
    setPreferredSize(new Dimension(320, 240));
  }

  public void addProgressValue(String name, Integer iv, SwingWorker<?, ?> worker) {
    Object[] obj = {number, name, iv, worker};
    model.addRow(obj);
    number++;
  }

  public void addActionPerformed() {
    int key = model.getRowCount();
    SwingWorker<Integer, Object> worker = new BackgroundTask() {
      @Override protected void process(List<Object> c) {
        if (isDisplayable() && !isCancelled()) {
          c.forEach(v -> model.setValueAt(v, key, 2));
        } else {
          cancel(true);
        }
      }

      @Override protected void done() {
        if (isDisplayable()) {
          model.setValueAt(getDoneMessage(), key, 2);
        } else {
          cancel(true);
        }
      }
    };
    addProgressValue("example", 0, worker);
    worker.execute();
  }

  private static DefaultTableModel makeModel() {
    String[] columnNames = {"No.", "Name", "Progress", ""};
    return new DefaultTableModel(columnNames, 0) {
      @Override public boolean isCellEditable(int row, int column) {
        return false;
      }
    };
  }

  private final class TablePopupMenu extends JPopupMenu {
    private final JMenuItem cancelMenuItem;
    private final JMenuItem deleteMenuItem;

    /* default */ TablePopupMenu() {
      super();
      add("add").addActionListener(e -> addActionPerformed());
      addSeparator();
      cancelMenuItem = add("cancel");
      cancelMenuItem.addActionListener(e -> cancelActionPerformed());
      deleteMenuItem = add("delete");
      deleteMenuItem.addActionListener(e -> deleteActionPerformed());
    }

    @Override public void show(Component c, int x, int y) {
      if (c instanceof JTable) {
        boolean flag = ((JTable) c).getSelectedRowCount() > 0;
        cancelMenuItem.setEnabled(flag);
        deleteMenuItem.setEnabled(flag);
        super.show(c, x, y);
      }
    }

    private SwingWorker<?, ?> getSwingWorker(int identifier) {
      return (SwingWorker<?, ?>) model.getValueAt(identifier, 3);
    }

    private void deleteActionPerformed() {
      int[] selection = table.getSelectedRows();
      if (selection.length == 0) {
        return;
      }
      for (int i : selection) {
        int mi = table.convertRowIndexToModel(i);
        deletedRowSet.add(mi);
        SwingWorker<?, ?> worker = getSwingWorker(mi);
        if (Objects.nonNull(worker) && !worker.isDone()) {
          worker.cancel(true);
        }
      }
      RowSorter<? extends TableModel> sorter = table.getRowSorter();
      if (sorter instanceof TableRowSorter) {
        RowFilter<TableModel, Integer> filter = new RowFilter<TableModel, Integer>() {
          @Override public boolean include(Entry<? extends TableModel, ? extends Integer> entry) {
            return !deletedRowSet.contains(entry.getIdentifier());
          }
        };
        ((TableRowSorter<? extends TableModel>) sorter).setRowFilter(filter);
      }
      table.clearSelection();
      table.repaint();
    }

    private void cancelActionPerformed() {
      int[] selection = table.getSelectedRows();
      for (int i : selection) {
        int mi = table.convertRowIndexToModel(i);
        SwingWorker<?, ?> worker = getSwingWorker(mi);
        if (Objects.nonNull(worker) && !worker.isDone()) {
          worker.cancel(true);
        }
      }
      table.repaint();
    }
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
    frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    // frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

class ProgressTableCellRenderer implements TableCellRenderer {
  private final TableCellRenderer renderer = new DefaultTableCellRenderer();
  private final JProgressBar progress;
  // private final JLabel label = new JLabel(null, null, SwingConstants.CENTER);
  // private final URL url;

  public ProgressTableCellRenderer(JProgressBar progress) {
    this.progress = progress;
    // ClassLoader cl = Thread.currentThread().getContextClassLoader();
    // url = cl.getResource("example/restore_to_background_color.gif");
  }

  @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    Component c;
    // label.setIcon(makeImageIcon(url, table, row, column));
    // c = label;
    if (value instanceof JProgressBar) {
      c = (JProgressBar) value;
    } else if (value instanceof Integer) {
      progress.setValue((int) value);
      c = progress;
    } else {
      c = renderer.getTableCellRendererComponent(
          table, Objects.toString(value), isSelected, hasFocus, row, column);
    }
    return c;
  }

  // public static Icon makeImageIcon(URL url, JTable table, int row, int col) {
  //   if (Objects.nonNull(url)) {
  //     ImageIcon icon = new ImageIcon(url);
  //     // Wastefulness: icon.setImageObserver((ImageObserver) table);
  //     icon.setImageObserver((img, flags, x, y, w, h) -> {
  //       // @see http://www2.gol.com/users/tame/swing/examples/SwingExamples.html
  //       if (!table.isShowing()) {
  //         return false; // @see javax.swing.JLabel#imageUpdate(...)
  //       }
  //       // @see java.awt.Component#imageUpdate(...)
  //       if ((flags & (FRAMEBITS | ALLBITS)) != 0) {
  //         int vr = table.convertRowIndexToView(row); // JDK 1.6.0
  //         int vc = table.convertColumnIndexToView(col);
  //         table.repaint(table.getCellRect(vr, vc, false));
  //       }
  //       return (flags & (ALLBITS | ABORT)) == 0;
  //     });
  //     return icon;
  //   } else {
  //     return UIManager.getIcon("html.missingImage");
  //   }
  // }
}

class IndeterminateProgressBarUI extends BasicProgressBarUI {
  @Override public void incrementAnimationIndex() {
    super.incrementAnimationIndex();
  }
}

class BackgroundTask extends SwingWorker<Integer, Object> {
  private final Random rnd = new Random();

  @Override protected Integer doInBackground() throws InterruptedException {
    int lengthOfTask = calculateTaskSize();
    int current = 0;
    int total = 0;
    while (current <= lengthOfTask && !isCancelled()) {
      publish(100 * current / lengthOfTask);
      total += doSomething();
      current++;
    }
    return total;
  }

  private int calculateTaskSize() throws InterruptedException {
    int total = 0;
    JProgressBar indeterminate = new JProgressBar() {
      @Override public void updateUI() {
        super.updateUI();
        setUI(new IndeterminateProgressBarUI());
      }
    };
    indeterminate.setIndeterminate(true);
    // Indeterminate loop:
    for (int i = 0; i < 200; i++) {
      int iv = rnd.nextInt(50) + 1;
      Thread.sleep(iv);
      ((IndeterminateProgressBarUI) indeterminate.getUI()).incrementAnimationIndex();
      publish(indeterminate);
      total += iv;
    }
    return 1 + total / 100;
  }

  private int doSomething() throws InterruptedException {
    int iv = rnd.nextInt(50) + 1;
    Thread.sleep(iv);
    return iv;
  }

  protected String getDoneMessage() {
    String msg;
    int i = -1;
    if (isCancelled()) {
      msg = "Cancelled";
    } else {
      try {
        i = get();
        msg = i >= 0 ? "Done" : "Disposed";
      } catch (InterruptedException ex) {
        msg = "Interrupted";
        Thread.currentThread().interrupt();
      } catch (ExecutionException ex) {
        msg = "ExecutionException: " + ex.getMessage();
      }
    }
    return String.format("%s(%dms)%n", msg, i);
  }
}
