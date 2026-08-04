// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.HierarchyEvent;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.stream.IntStream;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

public final class MainPanel extends JPanel {
  // Column index of the hidden column that holds the full path of a row.
  private static final int FULL_PATH_COLUMN = 2;
  private final JTextArea logArea = new JTextArea();
  private final FileTableModel model = new FileTableModel();

  private MainPanel() {
    super(new BorderLayout());
    JTable table = new JTable(model);
    table.setRowSorter(new TableRowSorter<>(model));
    table.setFillsViewportHeight(true);
    table.setComponentPopupMenu(new TablePopupMenu());
    // TableColumn col = table.getColumnModel().getColumn(0);
    // col.setMinWidth(30);
    // col.setMaxWidth(30);
    // col.setResizable(false);

    Toolkit tk = Toolkit.getDefaultToolkit();
    SecondaryLoop loop = tk.getSystemEventQueue().createSecondaryLoop();
    Thread worker = new Thread(new DirectoryWatcher(loop));
    worker.start();
    if (!loop.enter()) {
      appendLog("Error");
    }
    addHierarchyListener(e -> {
      long flags = e.getChangeFlags() & HierarchyEvent.DISPLAYABILITY_CHANGED;
      if (flags != 0 && !e.getComponent().isDisplayable()) {
        worker.interrupt();
      }
    });

    JButton button = new JButton("createTempFile");
    button.addActionListener(e -> {
      try {
        Path path = Files.createTempFile("_", ".tmp");
        path.toFile().deleteOnExit();
      } catch (IOException ex) {
        appendLog(ex.getMessage());
      }
    });

    JPanel buttonPanel = new JPanel();
    buttonPanel.add(button);

    JPanel centerPanel = new JPanel(new GridLayout(2, 1));
    centerPanel.add(new JScrollPane(table));
    centerPanel.add(new JScrollPane(logArea));

    add(buttonPanel, BorderLayout.NORTH);
    add(centerPanel);
    setPreferredSize(new Dimension(320, 240));
  }

  public void appendLog(String str) {
    logArea.append(str + "\n");
  }

  // Search the model for the row whose full path matches the given path, or
  // return -1 if no such row exists.
  private int rowIndexOf(Path path) {
    String fullPath = path.toString();
    return IntStream.range(0, model.getRowCount())
        .filter(i -> {
          Object obj = model.getValueAt(i, FULL_PATH_COLUMN);
          return fullPath.equals(Objects.toString(obj, ""));
        })
        .findFirst()
        .orElse(-1);
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

  private final class DirectoryWatcher implements Runnable {
    private final SecondaryLoop loop;

    private DirectoryWatcher(SecondaryLoop loop) {
      this.loop = loop;
    }

    @Override public void run() {
      try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
        Path dir = Paths.get(System.getProperty("java.io.tmpdir"));
        dir.register(
            watchService,
            StandardWatchEventKinds.ENTRY_CREATE,
            StandardWatchEventKinds.ENTRY_DELETE);
        appendLog("register: " + dir);
        processEvents(dir, watchService);
        loop.exit();
      } catch (IOException ex) {
        Logger.getGlobal().severe(ex::getMessage);
      }
    }

    // Watching a Directory for Changes
    // (The Java™ Tutorials > Essential Classes > Basic I/O)
    // https://docs.oracle.com/javase/tutorial/essential/io/notification.html
    // Process all events for keys queued to the watcher
    @SuppressWarnings("ReturnCount")
    private void processEvents(Path dir, WatchService watchService) {
      for (;;) {
        // wait for key to be signaled
        WatchKey key;
        try {
          key = watchService.take();
        } catch (InterruptedException ex) {
          EventQueue.invokeLater(() -> appendLog("Interrupted"));
          Thread.currentThread().interrupt();
          return;
        }

        for (WatchEvent<?> event : key.pollEvents()) {
          WatchEvent.Kind<?> kind = event.kind();
          // This key is registered only for ENTRY_CREATE events,
          // but an OVERFLOW event can occur regardless if events
          // are lost or discarded.
          if (kind == StandardWatchEventKinds.OVERFLOW) {
            continue;
          }

          // @SuppressWarnings("unchecked") WatchEvent<Path> ev = (WatchEvent<Path>) event;
          // The filename is the context of the event.
          Path filename = (Path) event.context();
          Path child = dir.resolve(filename);
          EventQueue.invokeLater(() -> {
            appendLog(String.format("%s: %s", kind, child));
            updateTable(kind, child);
          });
        }

        // Reset the key -- this step is critical if you want to
        // receive further watch events.  If the key is no longer valid,
        // the directory is inaccessible so exit the loop.
        boolean valid = key.reset();
        if (!valid) {
          break;
        }
      }
    }

    private void updateTable(WatchEvent.Kind<?> kind, Path child) {
      if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
        model.addPath(child);
      } else if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
        int modelRow = rowIndexOf(child);
        if (modelRow >= 0) {
          model.removeRow(modelRow);
        }
      }
    }
  }

  private static final class TablePopupMenu extends JPopupMenu {
    private final JMenuItem deleteMenuItem;

    private TablePopupMenu() {
      super();
      deleteMenuItem = add("delete");
      deleteMenuItem.addActionListener(this::deleteActionPerformed);
    }

    @Override public void show(Component c, int x, int y) {
      if (c instanceof JTable) {
        deleteMenuItem.setEnabled(((JTable) c).getSelectedRowCount() > 0);
        super.show(c, x, y);
      }
    }

    private void deleteActionPerformed(ActionEvent e) {
      JTable table = (JTable) getInvoker();
      DefaultTableModel tableModel = (DefaultTableModel) table.getModel();
      int[] selection = table.getSelectedRows();
      for (int i = selection.length - 1; i >= 0; i--) {
        int modelRow = table.convertRowIndexToModel(selection[i]);
        Object obj = tableModel.getValueAt(modelRow, FULL_PATH_COLUMN);
        Path path = Paths.get(Objects.toString(obj));
        try {
          Files.delete(path);
        } catch (IOException ex) {
          UIManager.getLookAndFeel().provideErrorFeedback((Component) e.getSource());
        }
      }
    }
  }
}

class FileTableModel extends DefaultTableModel {
  private static final ColumnContext[] COLUMN_ARRAY = {
      new ColumnContext("No.", Integer.class, false),
      new ColumnContext("Name", String.class, false),
      new ColumnContext("Full Path", String.class, false),
  };
  private int rowNumber;

  public void addPath(Path path) {
    Object[] rowData = {rowNumber, path.getFileName(), path.toAbsolutePath()};
    super.addRow(rowData);
    rowNumber++;
  }

  @Override public boolean isCellEditable(int row, int col) {
    return COLUMN_ARRAY[col].isEditable;
  }

  @Override public Class<?> getColumnClass(int column) {
    return COLUMN_ARRAY[column].columnClass;
  }

  @Override public int getColumnCount() {
    return COLUMN_ARRAY.length;
  }

  @Override public String getColumnName(int column) {
    return COLUMN_ARRAY[column].columnName;
  }

  private static final class ColumnContext {
    private final String columnName;
    private final Class<?> columnClass;
    private final boolean isEditable;

    private ColumnContext(String columnName, Class<?> columnClass, boolean isEditable) {
      this.columnName = columnName;
      this.columnClass = columnClass;
      this.isEditable = isEditable;
    }
  }
}
