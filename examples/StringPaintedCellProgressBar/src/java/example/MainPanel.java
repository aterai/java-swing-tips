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
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;

public final class MainPanel extends JPanel {
  private static final int PROGRESS_COLUMN = 2;
  private static final int WORKER_COLUMN = 3;
  private final DefaultTableModel model = createModel();
  private final JTable table = new JTable(model) {
    @Override public void updateUI() {
      super.updateUI();
      removeColumn(getColumnModel().getColumn(WORKER_COLUMN));
      TableColumn progressColumn = getColumnModel().getColumn(PROGRESS_COLUMN);
      progressColumn.setCellRenderer(new ProgressRenderer());
    }
  };
  private int rowNumber;

  private MainPanel() {
    super(new BorderLayout());
    table.setRowSorter(new TableRowSorter<>(model));
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

    JButton button = new JButton("add");
    button.addActionListener(e -> addActionPerformed());
    add(button, BorderLayout.SOUTH);
    add(scrollPane);
    setPreferredSize(new Dimension(320, 240));
  }

  private void addProgressRow(String name, Integer progress, SwingWorker<?, ?> worker) {
    Object[] rowData = {rowNumber, name, progress, worker};
    model.addRow(rowData);
    rowNumber++;
  }

  private void addActionPerformed() {
    SwingWorker<?, ?> worker = new ProgressWorker();
    addProgressRow("example", 0, worker);
    worker.execute();
  }

  private int rowIndexOf(SwingWorker<?, ?> worker) {
    return IntStream.range(0, model.getRowCount())
        .filter(i -> Objects.equals(model.getValueAt(i, WORKER_COLUMN), worker))
        .findFirst()
        .orElse(-1);
  }

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
    @Override protected void process(List<ProgressValue> chunks) {
      if (isDisplayable() && !isCancelled()) {
        chunks.forEach(v -> setProgressValue(this, v));
      } else {
        cancel(true);
      }
    }

    @Override protected void done() {
      String text = isCancelled() ? "Cancelled" : getResultMessage();
      setProgressValue(this, text);
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
      SwingWorker<?, ?> worker = (SwingWorker<?, ?>) model.getValueAt(modelRow, WORKER_COLUMN);
      if (Objects.nonNull(worker) && !worker.isDone()) {
        worker.cancel(true);
      }
    }

    private void deleteActionPerformed() {
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

class ProgressRenderer implements TableCellRenderer {
  private final JProgressBar progress = new JProgressBar();
  private final TableCellRenderer renderer = new DefaultTableCellRenderer();

  @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    Component c = renderer.getTableCellRendererComponent(
        table, value, isSelected, hasFocus, row, column);
    if (value instanceof ProgressValue) {
      ProgressValue pv = (ProgressValue) value;
      Integer current = pv.getProgress();
      Integer lengthOfTask = pv.getLengthOfTask();
      if (current < 0) {
        c = renderer.getTableCellRendererComponent(
            table, "Canceled", isSelected, hasFocus, row, column);
      } else if (current < lengthOfTask) {
        progress.setValue(current * 100 / lengthOfTask);
        progress.setStringPainted(true);
        progress.setString(String.format("%d/%d", current, lengthOfTask));
        c = progress;
      }
    }
    return c;
  }
}

class BackgroundTask extends SwingWorker<Integer, ProgressValue> {
  private final int lengthOfTask;
  private final Random rnd = new Random();

  protected BackgroundTask() {
    super();
    lengthOfTask = rnd.nextInt(100) + 100;
  }

  @Override protected Integer doInBackground() throws InterruptedException {
    int current = 0;
    int total = 0;
    while (current <= lengthOfTask && !isCancelled()) {
      publish(createProgressValue(current));
      total += sleepRandomly();
      current++;
    }
    return total;
  }

  private ProgressValue createProgressValue(int current) {
    return new ProgressValue(lengthOfTask, current);
  }

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

  private int sleepRandomly() throws InterruptedException {
    int sleepTime = rnd.nextInt(50) + 1;
    Thread.sleep(sleepTime);
    return sleepTime;
  }
}

class ProgressValue {
  private final Integer progress;
  private final Integer lengthOfTask;

  protected ProgressValue(Integer lengthOfTask, Integer progress) {
    this.progress = progress;
    this.lengthOfTask = lengthOfTask;
  }

  public Integer getProgress() {
    return progress;
  }

  public Integer getLengthOfTask() {
    return lengthOfTask;
  }
}
