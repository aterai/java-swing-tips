// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;
import java.util.stream.IntStream;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;

public final class MainPanel extends JPanel {
  // Column index of the cell that displays the progress of a task.
  private static final int PROGRESS_COLUMN = 2;
  // Column index of the hidden column that holds the SwingWorker of a row.
  private static final int WORKER_COLUMN = 3;
  private final DefaultTableModel model = createModel();
  private final JTable table = new JTable(model) {
    @Override public void updateUI() {
      super.updateUI();
      // Remove the last column from the TableColumnModel only: its value
      // (a SwingWorker) is still available from the TableModel.
      removeColumn(getColumnModel().getColumn(WORKER_COLUMN));
      TableColumn progressColumn = getColumnModel().getColumn(PROGRESS_COLUMN);
      progressColumn.setCellRenderer(new ProgressRenderer());
    }
  };
  // TEST: ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();
  // TEST: Executor executor = Executors.newFixedThreadPool(2);
  private int rowNumber;

  private MainPanel() {
    super(new BorderLayout());
    table.setRowSorter(new TableRowSorter<>(model));
    // Sample row without a task: its progress value is already the maximum,
    // so the renderer displays a text instead of a JProgressBar.
    addProgressRow("Name 1", 100, null);

    JScrollPane scrollPane = new JScrollPane(table);
    scrollPane.getViewport().setBackground(Color.WHITE);
    table.setComponentPopupMenu(new TablePopupMenu());
    table.setFillsViewportHeight(true);
    table.setIntercellSpacing(new Dimension());
    table.setShowGrid(false);
    table.putClientProperty("terminateEditOnFocusLost", true);

    TableColumn column = table.getColumnModel().getColumn(0);
    column.setMaxWidth(60);
    column.setMinWidth(60);
    column.setResizable(false);

    // addHierarchyListener(e -> {
    //   if ((e.getChangeFlags() & HierarchyEvent.DISPLAYABILITY_CHANGED) != 0
    //       && !e.getComponent().isDisplayable()) {
    //     executor.shutdownNow();
    //   }
    // });

    JButton button = new JButton("add");
    button.addActionListener(e -> addActionPerformed());
    add(button, BorderLayout.SOUTH);
    add(scrollPane);
    setPreferredSize(new Dimension(320, 240));
  }

  // Append a row that holds its own SwingWorker in the hidden WORKER_COLUMN.
  // The SwingWorker acts as the identity of this row: the popup menu uses it
  // to cancel the task of the selected row, and the task itself uses it to
  // look up the cell it has to update.
  private void addProgressRow(String name, Integer progress, SwingWorker<?, ?> worker) {
    Object[] rowData = {rowNumber, name, progress, worker};
    model.addRow(rowData);
    rowNumber++;
  }

  private void addActionPerformed() {
    SwingWorker<?, ?> worker = new ProgressWorker();
    addProgressRow("example", 0, worker);
    worker.execute(); // executor.execute(worker);
  }

  // Search the model for the row that holds the specified task, or return -1
  // if that row no longer exists. Because the row index is looked up on every
  // update instead of being fixed when the task starts, rows can be removed
  // from the model while other tasks are still running.
  private int rowIndexOf(SwingWorker<?, ?> worker) {
    return IntStream.range(0, model.getRowCount())
        .filter(i -> Objects.equals(model.getValueAt(i, WORKER_COLUMN), worker))
        .findFirst()
        .orElse(-1);
  }

  // Update the progress cell that belongs to the specified task. Nothing
  // happens if its row has already been removed from the model.
  private void setProgressValue(SwingWorker<?, ?> worker, Object value) {
    int modelRow = rowIndexOf(worker);
    if (modelRow >= 0) {
      model.setValueAt(value, modelRow, PROGRESS_COLUMN);
    }
  }

  private static DefaultTableModel createModel() {
    String[] columnNames = {"No.", "Name", "Progress", ""};
    return new DefaultTableModel(columnNames, 0);
  }

  private final class ProgressWorker extends BackgroundTask {
    @Override protected void process(List<Integer> chunks) {
      if (isDisplayable() && !isCancelled()) {
        chunks.forEach(v -> setProgressValue(this, v));
      } else {
        // The frame has been disposed, so there is no cell to update.
        cancel(true); // executor.shutdown();
      }
    }

    @Override protected void done() {
      // if (!isDisplayable()) {
      //   cancel(true);
      //   // executor.shutdown();
      //   return;
      // }
      // Replace the Integer progress value with a String status text.
      String text = isCancelled() ? "Cancelled" : getResultMessage();
      setProgressValue(this, text); // executor.remove(this);
    }
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
        boolean hasSelection = ((JTable) c).getSelectedRowCount() > 0;
        cancelMenuItem.setEnabled(hasSelection);
        deleteMenuItem.setEnabled(hasSelection);
        super.show(c, x, y);
      }
    }

    private void cancelWorker(int modelRow) {
      // Integer key = (Integer) model.getValueAt(modelRow, 0);
      // SwingWorker<?, ?> worker = workerMap.get(key);
      SwingWorker<?, ?> worker = (SwingWorker<?, ?>) model.getValueAt(modelRow, WORKER_COLUMN);
      if (Objects.nonNull(worker) && !worker.isDone()) {
        worker.cancel(true);
        // executor.remove(worker);
      }
      // worker = null;
    }

    // Cancel the tasks of the selected rows and remove these rows from the
    // TableModel. A running task looks its own row up by identity, so the
    // model indices shifted by this removal do not break the other tasks.
    private void deleteActionPerformed() {
      // Remove the rows in descending order, because removing a row shifts
      // the model index of every row that follows it.
      int[] modelRows = Arrays.stream(table.getSelectedRows())
          .map(table::convertRowIndexToModel)
          .sorted()
          .toArray();
      for (int i = modelRows.length - 1; i >= 0; i--) {
        cancelWorker(modelRows[i]);
        model.removeRow(modelRows[i]);
      }
      table.clearSelection();
    }

    private void cancelActionPerformed() {
      int[] selection = table.getSelectedRows();
      for (int viewRow : selection) {
        cancelWorker(table.convertRowIndexToModel(viewRow));
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
  private final JProgressBar progressBar = new JProgressBar();

  protected ProgressRenderer() {
    super();
    progressBar.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
  }

  @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    Component c;
    if (value instanceof Integer) {
      int progress = (int) value;
      if (0 <= progress && progress < progressBar.getMaximum()) { // < 100
        // The task is still running: use the JProgressBar as the renderer.
        progressBar.setValue(progress);
        c = progressBar;
      } else {
        // The task has never published a progress value: fall back to this
        // DefaultTableCellRenderer(JLabel) and paint a status text.
        String text = progress < 0 ? "Canceled" : "Done(0ms)";
        c = super.getTableCellRendererComponent(
            table, text, isSelected, hasFocus, row, column);
      }
    } else {
      // SwingWorker#done() has replaced the progress value with a message.
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
      // Publish the progress in percent, because the maximum value of the
      // JProgressBar in the ProgressRenderer is the default 100.
      publish(100 * current / lengthOfTask);
      total += sleepRandomly();
      current++;
    }
    return total;
  }

  // Make the text that replaces the progress value when this task is done.
  protected String getResultMessage() {
    String text;
    try {
      int total = get();
      text = String.format("%s(%dms)%n", total >= 0 ? "Done" : "Disposed", total);
    } catch (InterruptedException | ExecutionException ex) {
      text = ex.getMessage();
      Thread.currentThread().interrupt();
    }
    return text;
  }

  // Simulate a unit of work and return the time it took in milliseconds.
  private int sleepRandomly() throws InterruptedException {
    int sleepTime = rnd.nextInt(50) + 1;
    Thread.sleep(sleepTime);
    return sleepTime;
  }
}

// class ProgressRenderer extends DefaultTableCellRenderer {
//   private JProgressBar progress;
//
//   @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
//     Integer i = (Integer) value;
//     String text = "Done";
//      if (i < 0) {
//       text = "Canceled";
//     } else if (i < progress.getMaximum()) { // < 100
//       progress.setValue(i);
//       return progress;
//     }
//     super.getTableCellRendererComponent(table, text, isSelected, hasFocus, row, column);
//     return this;
//   }
//
//   @Override public void updateUI() {
//     super.updateUI();
//     setOpaque(false);
//     progress = new JProgressBar();
//   }
// }
