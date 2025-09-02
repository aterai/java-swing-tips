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
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

public final class MainPanel extends JPanel {
  private final DefaultTableModel model = makeModel();
  private final JTable table = new ProgressTable(model);
  private final Set<Integer> deletedRowSet = new TreeSet<>();
  private int number;
  private final Random rnd = new Random();

  private MainPanel() {
    super(new BorderLayout());
    table.setRowSorter(new TableRowSorter<>(model));

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

  private static DefaultTableModel makeModel() {
    String[] columnNames = {"No.", "Name", "Progress", ""};
    return new DefaultTableModel(columnNames, 0);
  }

  public void addActionPerformed() {
    int lengthOfTask = rnd.nextInt(100) + 100;
    int key = model.getRowCount();
    SwingWorker<?, ?> worker = new ProgressWorker(lengthOfTask, key);
    String name = "example(max: " + lengthOfTask + ")";
    ProgressValue pv = new ProgressValue(lengthOfTask, 0);
    Object[] obj = {number, name, pv, worker};
    model.addRow(obj);
    number++;
    worker.execute();
  }

  private final class ProgressWorker extends BackgroundTask {
    private final int key;

    private ProgressWorker(int lengthOfTask, int key) {
      super(lengthOfTask);
      this.key = key;
    }

    @Override protected void process(List<ProgressValue> c) {
      if (isDisplayable() && !isCancelled()) {
        c.forEach(v -> model.setValueAt(v, key, 2));
      } else {
        cancel(true);
      }
    }

    @Override protected void done() {
      String text;
      int i = -1;
      if (isCancelled()) {
        text = "Cancelled";
      } else {
        try {
          i = get();
          text = i >= 0 ? "Done" : "Disposed";
        } catch (InterruptedException | ExecutionException ex) {
          text = ex.getMessage();
          Thread.currentThread().interrupt();
        }
      }
      model.setValueAt(String.format("%s(%dms)%n", text, i), key, 2);
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

class ProgressTable extends JTable {
  protected ProgressTable(TableModel model) {
    super(model);
  }

  @Override public void updateUI() {
    super.updateUI();
    removeColumn(getColumnModel().getColumn(3));
    TableColumn tc = getColumnModel().getColumn(2);
    tc.setCellRenderer(new ProgressCellRenderer());
  }
}

class ProgressCellRenderer implements TableCellRenderer {
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

  protected BackgroundTask(int lengthOfTask) {
    super();
    this.lengthOfTask = lengthOfTask;
  }

  @Override protected Integer doInBackground() throws InterruptedException {
    int current = 0;
    int total = 0;
    while (current <= lengthOfTask && !isCancelled()) {
      total += doSomething(current);
      current++;
    }
    return total;
  }

  protected int doSomething(int current) throws InterruptedException {
    publish(new ProgressValue(lengthOfTask, current));
    int iv = rnd.nextInt(50) + 1;
    Thread.sleep(iv);
    return iv;
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
