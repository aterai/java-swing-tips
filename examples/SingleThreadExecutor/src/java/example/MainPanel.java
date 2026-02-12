// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.HierarchyEvent;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

public final class MainPanel extends JPanel {
  private final DefaultTableModel model = makeModel();
  private final JTable table = new JTable(model) {
    @Override public void updateUI() {
      super.updateUI();
      removeColumn(getColumnModel().getColumn(3));
      TableColumn tc = getColumnModel().getColumn(2);
      tc.setCellRenderer(new ProgressRenderer());
    }
  };
  // TEST: ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();
  // TEST: Executor executor = Executors.newFixedThreadPool(2);
  private final Set<Integer> deletedRowSet = new TreeSet<>();
  // Map<Integer, SwingWorker<Integer, Integer>> workerMap = new ConcurrentHashMap<>();
  private int number;

  @SuppressWarnings("PMD.CloseResource")
  private MainPanel() {
    super(new BorderLayout());
    ExecutorService executor = Executors.newSingleThreadExecutor();
    table.setComponentPopupMenu(new TablePopupMenu(executor));
    table.setRowSorter(new TableRowSorter<>(model));
    table.setFillsViewportHeight(true);
    table.setIntercellSpacing(new Dimension());
    table.setShowGrid(false);
    table.putClientProperty("terminateEditOnFocusLost", true);
    table.addHierarchyListener(e -> {
      boolean b = (e.getChangeFlags() & HierarchyEvent.DISPLAYABILITY_CHANGED) != 0;
      if (b && !e.getComponent().isDisplayable()) {
        executor.shutdownNow();
      }
    });

    TableColumn column = table.getColumnModel().getColumn(0);
    column.setMaxWidth(60);
    column.setMinWidth(60);
    column.setResizable(false);

    addProgressValue("Name 1", 100, null);

    JButton button = new JButton("add");
    button.addActionListener(e -> addActionPerformed(executor));

    JScrollPane scrollPane = new JScrollPane(table);
    scrollPane.getViewport().setBackground(Color.WHITE);

    add(button, BorderLayout.SOUTH);
    add(scrollPane);
    setPreferredSize(new Dimension(320, 240));
  }

  public void addProgressValue(String name, Integer iv, SwingWorker<?, ?> worker) {
    Object[] obj = {number, name, iv, worker};
    model.addRow(obj);
    number++;
  }

  public void addActionPerformed(ExecutorService executor) {
    int key = model.getRowCount();
    SwingWorker<Integer, Integer> worker = new BackgroundTask() {
      @Override protected void process(List<Integer> c) {
        if (isDisplayable() && !isCancelled()) {
          c.forEach(v -> model.setValueAt(v, key, 2));
        } else {
          cancel(true);
          executor.shutdown();
        }
      }

      @Override protected void done() {
        // if (!isDisplayable()) {
        //   cancel(true);
        //   executor.shutdown();
        //   return;
        // }
        String text = isCancelled() ? "Cancelled" : getMessage();
        model.setValueAt(text, key, 2); // executor.remove(this);
      }
    };
    addProgressValue("example", 0, worker);
    executor.execute(worker);
  }

  private static DefaultTableModel makeModel() {
    String[] columnNames = {"No.", "Name", "Progress", ""};
    return new DefaultTableModel(columnNames, 0);
  }

  private final class TablePopupMenu extends JPopupMenu {
    private final JMenuItem cancelMenuItem;
    private final JMenuItem deleteMenuItem;

    /* default */ TablePopupMenu(ExecutorService executor) {
      super();
      add("add").addActionListener(e -> addActionPerformed(executor));
      addSeparator();
      cancelMenuItem = add("cancel");
      cancelMenuItem.addActionListener(e -> cancelActionPerformed());
      deleteMenuItem = add("delete");
      deleteMenuItem.addActionListener(e -> {
        int[] selection = table.getSelectedRows();
        if (selection.length != 0) {
          deleteActionPerformed(selection);
        }
      });
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
      // Integer key = (Integer) model.getValueAt(identifier, 0);
      // return workerMap.get(key);
      return (SwingWorker<?, ?>) model.getValueAt(identifier, 3);
    }

    private void deleteActionPerformed(int... selection) {
      for (int i : selection) {
        int mi = table.convertRowIndexToModel(i);
        deletedRowSet.add(mi);
        SwingWorker<?, ?> worker = getSwingWorker(mi);
        if (Objects.nonNull(worker) && !worker.isDone()) {
          worker.cancel(true);
          // executor.remove(worker);
        }
        // worker = null;
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
        // worker = null;
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

class ProgressRenderer extends DefaultTableCellRenderer {
  private final JProgressBar progress = new JProgressBar();

  @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    Component c;
    if (value instanceof Integer) {
      int i = (int) value;
      if (0 <= i && i < progress.getMaximum()) { // < 100
        progress.setValue(i);
        progress.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        c = progress;
      } else {
        String text = i < 0 ? "Canceled" : "Done(0ms)";
        c = super.getTableCellRendererComponent(
            table, text, isSelected, hasFocus, row, column);
      }
    } else {
      c = super.getTableCellRendererComponent(
          table, Objects.toString(value), isSelected, hasFocus, row, column);
    }
    return c;
  }
}

class BackgroundTask extends SwingWorker<Integer, Integer> {
  private final Random rnd = new Random();

  @Override protected Integer doInBackground() throws InterruptedException {
    int lengthOfTask = 120;
    int current = 0;
    int total = 0;
    while (current <= lengthOfTask && !isCancelled()) {
      publish(100 * current / lengthOfTask);
      total += doSomething();
      current++;
    }
    return total;
  }

  protected String getMessage() {
    String text;
    try {
      int i = get();
      text = String.format("%s(%dms)%n", i >= 0 ? "Done" : "Disposed", i);
    } catch (InterruptedException | ExecutionException ex) {
      text = ex.getMessage();
      Thread.currentThread().interrupt();
    }
    return text;
  }

  private int doSomething() throws InterruptedException {
    int iv = rnd.nextInt(50) + 1;
    Thread.sleep(iv);
    return iv;
  }
}
