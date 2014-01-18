package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.*;

public class MainPanel extends JPanel {
    private final String[] columnNames = {"Year", "String", "Comment"};
    private final DefaultTableModel model = new DefaultTableModel(null, columnNames) {
        @Override public Class<?> getColumnClass(int column) {
            return (column==0)?Integer.class:Object.class;
        }
    };
    private final TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(model);
    private final JTable table = new JTable(model);
    private final JButton first = new JButton(new AbstractAction("|<") {
        @Override public void actionPerformed(ActionEvent e) {
            currentPageIndex = 1;
            initFilterAndButton();
        }
    });
    private final JButton prev  = new JButton(new AbstractAction("<") {
        @Override public void actionPerformed(ActionEvent e) {
            currentPageIndex -= 1;
            initFilterAndButton();
        }
    });
    private final JButton next = new JButton(new AbstractAction(">") {
        @Override public void actionPerformed(ActionEvent e) {
            currentPageIndex += 1;
            initFilterAndButton();
        }
    });
    private final JButton last = new JButton(new AbstractAction(">|") {
        @Override public void actionPerformed(ActionEvent e) {
            currentPageIndex = maxPageIndex;
            initFilterAndButton();
        }
    });
    private final Action enterAction = new AbstractAction() {
        @Override public void actionPerformed(ActionEvent e) {
            try{
                int v = Integer.parseInt(field.getText());
                if(v>0 && v<=maxPageIndex) {
                    currentPageIndex = v;
                }
            }catch(Exception ex) {
                ex.printStackTrace();
            }
            initFilterAndButton();
        }
    };
    private final JTextField field = new JTextField(2);
    private final JLabel label = new JLabel("/ 1");
    public MainPanel() {
        super(new BorderLayout());
        table.setFillsViewportHeight(true);
        table.setRowSorter(sorter);
        table.setEnabled(false);

        JPanel po = new JPanel();
        po.add(field);
        po.add(label);
        JPanel box = new JPanel(new GridLayout(1,4,2,2));
        box.setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
        for(JComponent r:Arrays.asList(first, prev, po, next, last)) {
            box.add(r);
        }

        KeyStroke enter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
        field.getInputMap(JComponent.WHEN_FOCUSED).put(enter, "Enter");
        field.getActionMap().put("Enter", enterAction);

        Task worker = new Task(2013, itemsPerPage) {
            @Override protected void process(List<List<Object[]>> chunks) {
                for(List<Object[]> list: chunks) {
                    for(Object[] o: list) {
                        model.addRow(o);
                    }
                }
                int rowCount = model.getRowCount();
                maxPageIndex = rowCount/itemsPerPage + (rowCount%itemsPerPage==0?0:1);
                initFilterAndButton();
            }
            @Override public void done() {
                String text = null;
                if(isCancelled()) {
                    text = "Cancelled";
                }else{
                    try{
                        text = get();
                    }catch(Exception ex) {
                        ex.printStackTrace();
                        text = "Exception";
                    }
                }
                table.setEnabled(true);
            }
        };
        worker.execute();

        add(box, BorderLayout.NORTH);
        add(new JScrollPane(table));
        setPreferredSize(new Dimension(320, 240));
    }
    private static final int itemsPerPage = 100;
    private int maxPageIndex;
    private int currentPageIndex = 1;
    private void initFilterAndButton() {
        sorter.setRowFilter(new RowFilter<TableModel, Integer>() {
            @Override public boolean include(Entry<? extends TableModel, ? extends Integer> entry) {
                int ti = currentPageIndex - 1;
                int ei = entry.getIdentifier();
                return ti*itemsPerPage<=ei && ei<ti*itemsPerPage+itemsPerPage;
            }
        });
        first.setEnabled(currentPageIndex>1);
        prev.setEnabled(currentPageIndex>1);
        next.setEnabled(currentPageIndex<maxPageIndex);
        last.setEnabled(currentPageIndex<maxPageIndex);
        field.setText(Integer.toString(currentPageIndex));
        label.setText(String.format("/ %d", maxPageIndex));
    }
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                createAndShowGUI();
            }
        });
    }
    public static void createAndShowGUI() {
        try{
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }catch(Exception e) {
            e.printStackTrace();
        }
        JFrame frame = new JFrame("@title@");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

class Task extends SwingWorker<String, List<Object[]>> {
    private final int max;
    private final int itemsPerPage;
    public Task(int max, int itemsPerPage) {
        this.max = max;
        this.itemsPerPage = itemsPerPage;
    }
    @Override public String doInBackground() {
        int current = 1;
        int c = max/itemsPerPage;
        int i = 0;
        while(i<c && !isCancelled()) {
            current = makeRowListAndPublish(current, itemsPerPage);
            i++;
        }
        int m = max%itemsPerPage;
        if(m>0) {
            makeRowListAndPublish(current, m);
        }
        return "Done";
    }
    private int makeRowListAndPublish(int current, int size) {
        try{
            Thread.sleep(500); //dummy
        }catch(Exception ex) {
            ex.printStackTrace();
        }
        List<Object[]> result = new ArrayList<Object[]>(size);
        int j = current;
        while(j<current+size) {
            result.add(new Object[] {j, "Test: "+j, (j%2==0)?"":"comment..."});
            j++;
        }
        publish(result);
        return j;
    }
}
