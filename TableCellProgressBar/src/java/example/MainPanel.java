package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;
import javax.swing.*;
import javax.swing.table.*;

public final class MainPanel extends JPanel {
    private final WorkerModel model = new WorkerModel();
    private final JTable table = new JTable(model);
    private final transient TableRowSorter<? extends TableModel> sorter = new TableRowSorter<>(model);
    private final Set<Integer> deleteRowSet = new TreeSet<>();
    //TEST: private final transient ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();
    //TEST: private final Executor executor = Executors.newFixedThreadPool(2);
    public MainPanel() {
        super(new BorderLayout());
        table.setRowSorter(sorter);
        model.addProgressValue("Name 1", 100, null);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(Color.WHITE);
        table.setComponentPopupMenu(new TablePopupMenu());
        table.setFillsViewportHeight(true);
        table.setIntercellSpacing(new Dimension());
        table.setShowGrid(false);
        table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

        TableColumn column = table.getColumnModel().getColumn(0);
        column.setMaxWidth(60);
        column.setMinWidth(60);
        column.setResizable(false);
        column = table.getColumnModel().getColumn(2);
        column.setCellRenderer(new ProgressRenderer());

        JButton button = new JButton("add");
        button.addActionListener(e -> addActionPerformed());
        add(button, BorderLayout.SOUTH);
        add(scrollPane);
        setPreferredSize(new Dimension(320, 240));
    }

    protected void addActionPerformed() {
        final int key = model.getRowCount();
        SwingWorker<Integer, Integer> worker = new Task() {
            @Override protected void process(List<Integer> c) {
                if (isCancelled()) {
                    return;
                }
                if (!isDisplayable()) {
                    System.out.println("process: DISPOSE_ON_CLOSE");
                    cancel(true);
                    //executor.shutdown();
                    return;
                }
                c.forEach(v -> model.setValueAt(v, key, 2));
            }
            @Override protected void done() {
                if (!isDisplayable()) {
                    System.out.println("done: DISPOSE_ON_CLOSE");
                    cancel(true);
                    //executor.shutdown();
                    return;
                }
                String text;
                int i = -1;
                if (isCancelled()) {
                    text = "Cancelled";
                } else {
                    try {
                        i = get();
                        text = i >= 0 ? "Done" : "Disposed";
                    } catch (InterruptedException | ExecutionException ex) {
                        ex.printStackTrace();
                        text = ex.getMessage();
                    }
                }
                System.out.format("%s:%s(%dms)%n", key, text, i);
                //executor.remove(this);
            }
        };
        model.addProgressValue("example", 0, worker);
        //executor.execute(worker);
        worker.execute();
    }

    protected void cancelActionPerformed() {
        int[] selection = table.getSelectedRows();
        for (int i = 0; i < selection.length; i++) {
            int midx = table.convertRowIndexToModel(selection[i]);
            SwingWorker worker = model.getSwingWorker(midx);
            if (Objects.nonNull(worker) && !worker.isDone()) {
                worker.cancel(true);
            }
            worker = null;
        }
        table.repaint();
    }

    protected void deleteActionPerformed() {
        int[] selection = table.getSelectedRows();
        if (selection.length == 0) {
            return;
        }
        for (int i = 0; i < selection.length; i++) {
            int midx = table.convertRowIndexToModel(selection[i]);
            deleteRowSet.add(midx);
            SwingWorker worker = model.getSwingWorker(midx);
            if (Objects.nonNull(worker) && !worker.isDone()) {
                worker.cancel(true);
                //executor.remove(worker);
            }
            worker = null;
        }
        sorter.setRowFilter(new RowFilter<TableModel, Integer>() {
            @Override public boolean include(Entry<? extends TableModel, ? extends Integer> entry) {
                return !deleteRowSet.contains(entry.getIdentifier());
            }
        });
        table.clearSelection();
        table.repaint();
    }

    private class TablePopupMenu extends JPopupMenu {
        private final JMenuItem cancelMenuItem;
        private final JMenuItem deleteMenuItem;
        protected TablePopupMenu() {
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
    }

    public static void main(String... args) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                createAndShowGUI();
            }
        });
    }
    public static void createAndShowGUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException
               | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        }
        JFrame frame = new JFrame("@title@");
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        //frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

class Task extends SwingWorker<Integer, Integer> {
    private final int sleepDummy = new Random().nextInt(100) + 1;
    @Override protected Integer doInBackground() {
        int lengthOfTask = 120;
        int current = 0;
        while (current <= lengthOfTask && !isCancelled()) {
            publish(100 * current / lengthOfTask);
            try {
                Thread.sleep(sleepDummy);
            } catch (InterruptedException ex) {
                break;
            }
            current++;
        }
        return sleepDummy * lengthOfTask;
    }
}

class WorkerModel extends DefaultTableModel {
    private static final ColumnContext[] COLUMN_ARRAY = {
        new ColumnContext("No.",      Integer.class, false),
        new ColumnContext("Name",     String.class,  false),
        new ColumnContext("Progress", Integer.class, false)
    };
    private final Map<Integer, SwingWorker> swmap = new ConcurrentHashMap<>();
    private int number;
    public void addProgressValue(String name, Integer iv, SwingWorker worker) {
        Object[] obj = {number, name, iv};
        super.addRow(obj);
        if (Objects.nonNull(worker)) {
            swmap.put(number, worker);
        }
        number++;
    }
    public synchronized SwingWorker getSwingWorker(int identifier) {
        Integer key = (Integer) getValueAt(identifier, 0);
        return swmap.get(key);
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
    private static class ColumnContext {
        public final String  columnName;
        public final Class   columnClass;
        public final boolean isEditable;
        protected ColumnContext(String columnName, Class columnClass, boolean isEditable) {
            this.columnName = columnName;
            this.columnClass = columnClass;
            this.isEditable = isEditable;
        }
    }
}

class ProgressRenderer extends DefaultTableCellRenderer {
    private final JProgressBar b = new JProgressBar();
    private final JPanel p = new JPanel(new BorderLayout());
    @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Integer i = (Integer) value;
        String text = "Done";
        if (i < 0) {
            text = "Canceled";
        } else if (i < 100) {
            b.setValue(i);
            p.add(b);
            p.setOpaque(false);
            p.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
            return p;
        }
        super.getTableCellRendererComponent(table, text, isSelected, hasFocus, row, column);
        return this;
    }
    @Override public void updateUI() {
        super.updateUI();
        setOpaque(false);
        if (Objects.nonNull(p)) {
            SwingUtilities.updateComponentTreeUI(p);
        }
    }
}
