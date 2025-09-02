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
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

public final class MainPanel extends JPanel {
  private final JTextArea logger = new JTextArea();
  private final FileModel model = new FileModel();
  private final transient TableRowSorter<TableModel> sorter = new TableRowSorter<>(model);
  private final Set<Integer> deleteRowSet = new TreeSet<>();

  private MainPanel() {
    super(new BorderLayout());
    JTable table = new JTable(model);
    table.setRowSorter(sorter);
    table.setFillsViewportHeight(true);
    table.setComponentPopupMenu(new TablePopupMenu());
    // TableColumn col = table.getColumnModel().getColumn(0);
    // col.setMinWidth(30);
    // col.setMaxWidth(30);
    // col.setResizable(false);

    Toolkit tk = Toolkit.getDefaultToolkit();
    SecondaryLoop loop = tk.getSystemEventQueue().createSecondaryLoop();
    Thread worker = new Thread(new Watcher(loop));
    worker.start();
    if (!loop.enter()) {
      append("Error");
    }
    addHierarchyListener(e -> {
      boolean b = (e.getChangeFlags() & HierarchyEvent.DISPLAYABILITY_CHANGED) != 0;
      if (b && !e.getComponent().isDisplayable()) {
        worker.interrupt();
      }
    });

    JButton button = new JButton("createTempFile");
    button.addActionListener(e -> {
      try {
        Path path = Files.createTempFile("_", ".tmp");
        path.toFile().deleteOnExit();
      } catch (IOException ex) {
        append(ex.getMessage());
      }
    });

    JPanel box = new JPanel();
    box.add(button);

    JPanel p = new JPanel(new GridLayout(2, 1));
    p.add(new JScrollPane(table));
    p.add(new JScrollPane(logger));

    add(box, BorderLayout.NORTH);
    add(p);
    setPreferredSize(new Dimension(320, 240));
  }

  public void append(String str) {
    logger.append(str + "\n");
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

  private final class Watcher implements Runnable {
    private final SecondaryLoop loop;

    private Watcher(SecondaryLoop loop) {
      this.loop = loop;
    }

    @Override public void run() {
      try (WatchService watcher = FileSystems.getDefault().newWatchService()) {
        Path dir = Paths.get(System.getProperty("java.io.tmpdir"));
        dir.register(
            watcher,
            StandardWatchEventKinds.ENTRY_CREATE,
            StandardWatchEventKinds.ENTRY_DELETE);
        append("register: " + dir);
        processEvents(dir, watcher);
        loop.exit();
      } catch (IOException ex) {
        Logger.getGlobal().severe(ex::getMessage);
      }
    }

    // Watching a Directory for Changes (The Java™ Tutorials > Essential Classes > Basic I/O)
    // https://docs.oracle.com/javase/tutorial/essential/io/notification.html
    // Process all events for keys queued to the watcher
    private void processEvents(Path dir, WatchService watcher) {
      for (;;) {
        // wait for key to be signaled
        WatchKey key;
        try {
          key = watcher.take();
        } catch (InterruptedException ex) {
          EventQueue.invokeLater(() -> append("Interrupted"));
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
            append(String.format("%s: %s", kind, child));
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
        for (int i = 0; i < model.getRowCount(); i++) {
          Object value = model.getValueAt(i, 2);
          String path = Objects.toString(value, "");
          if (path.equals(child.toString())) {
            deleteRowSet.add(i);
            // model.removeRow(i);
            break;
          }
        }
        sorter.setRowFilter(new RowFilter<TableModel, Integer>() {
          @Override public boolean include(Entry<? extends TableModel, ? extends Integer> entry) {
            return !isDeleteRow(entry.getIdentifier());
          }
        });
      }
    }

    private boolean isDeleteRow(int row) {
      return deleteRowSet.contains(row);
    }
  }
}

class FileModel extends DefaultTableModel {
  private static final ColumnContext[] COLUMN_ARRAY = {
      new ColumnContext("No.", Integer.class, false),
      new ColumnContext("Name", String.class, false),
      new ColumnContext("Full Path", String.class, false)
  };
  private int number;

  public void addPath(Path path) {
    Object[] obj = {number, path.getFileName(), path.toAbsolutePath()};
    super.addRow(obj);
    number++;
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

final class TablePopupMenu extends JPopupMenu {
  private final JMenuItem delete;

  /* default */ TablePopupMenu() {
    super();
    delete = add("delete");
    delete.addActionListener(this::fileDelete);
  }

  @Override public void show(Component c, int x, int y) {
    if (c instanceof JTable) {
      delete.setEnabled(((JTable) c).getSelectedRowCount() > 0);
      super.show(c, x, y);
    }
  }

  private void fileDelete(ActionEvent e) {
    JTable table = (JTable) getInvoker();
    DefaultTableModel model = (DefaultTableModel) table.getModel();
    int[] selection = table.getSelectedRows();
    for (int i = selection.length - 1; i >= 0; i--) {
      int idx = table.convertRowIndexToModel(selection[i]);
      Path path = Paths.get(Objects.toString(model.getValueAt(idx, 2)));
      try {
        Files.delete(path);
      } catch (IOException ex) {
        UIManager.getLookAndFeel().provideErrorFeedback((Component) e.getSource());
      }
    }
  }
}
