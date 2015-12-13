package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.io.*;
import java.util.List;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

public class MainPanel extends JPanel {
    private final JTextArea textArea = new JTextArea();

    private final String[] columnNames = {"A", "B"};
    private final Object[][] data = {
        {"aaa", "ccccccc"}, {"bbb", "\u2600\u2601\u2602\u2603"}
    };
    private final JTable table = new JTable(new DefaultTableModel(data, columnNames));

    public MainPanel() {
        super(new BorderLayout());

        table.setAutoCreateRowSorter(true);
        table.getTableHeader().setComponentPopupMenu(new TableHeaderPopupMenu());

        JSplitPane sp = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        sp.setResizeWeight(.5);
        sp.setTopComponent(new JScrollPane(table));
        sp.setBottomComponent(new JScrollPane(textArea));

        JPanel p = new JPanel();
        p.add(new JButton(new AbstractAction("XMLEncoder") {
            @Override public void actionPerformed(ActionEvent e) {
                try {
                    File file = File.createTempFile("output", ".xml");
                    try (XMLEncoder xe = new XMLEncoder(new BufferedOutputStream(new FileOutputStream(file)))) {
                        xe.setPersistenceDelegate(RowSorter.SortKey.class, new DefaultPersistenceDelegate(new String[] {"column", "sortOrder"}));
                        xe.writeObject(table.getRowSorter().getSortKeys());

                        xe.setPersistenceDelegate(DefaultTableModel.class, new DefaultTableModelPersistenceDelegate());
                        xe.writeObject(table.getModel());

                        xe.setPersistenceDelegate(DefaultTableColumnModel.class, new DefaultTableColumnModelPersistenceDelegate());
                        xe.writeObject(table.getColumnModel());
                    }
                    try (Reader r = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"))) {
                        textArea.read(r, "temp");
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }));
        p.add(new JButton(new AbstractAction("XMLDecoder") {
            @Override public void actionPerformed(ActionEvent e) {
                try (XMLDecoder xd = new XMLDecoder(new BufferedInputStream(new ByteArrayInputStream(textArea.getText().getBytes("UTF-8"))))) {
                    @SuppressWarnings("unchecked")
                    List<? extends RowSorter.SortKey> keys = (List<? extends RowSorter.SortKey>) xd.readObject();
                    DefaultTableModel model = (DefaultTableModel) xd.readObject();
                    table.setModel(model);
                    table.setAutoCreateRowSorter(true);
                    table.getRowSorter().setSortKeys(keys);
                    DefaultTableColumnModel cm = (DefaultTableColumnModel) xd.readObject();
                    table.setColumnModel(cm);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }));
        p.add(new JButton(new AbstractAction("clear") {
            @Override public void actionPerformed(ActionEvent e) {
                table.setModel(new DefaultTableModel());
            }
        }));
        add(sp);
        add(p, BorderLayout.SOUTH);
        setPreferredSize(new Dimension(320, 240));
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
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

//http://web.archive.org/web/20090806075316/http://java.sun.com/products/jfc/tsc/articles/persistence4/
//http://www.oracle.com/technetwork/java/persistence4-140124.html
//http://ateraimemo.com/Swing/PersistenceDelegate.html
class DefaultTableModelPersistenceDelegate extends DefaultPersistenceDelegate {
    @Override protected void initialize(Class<?> type, Object oldInstance, Object newInstance, Encoder encoder) {
        super.initialize(type, oldInstance, newInstance, encoder);
        DefaultTableModel m = (DefaultTableModel) oldInstance;
        for (int row = 0; row < m.getRowCount(); row++) {
            for (int col = 0; col < m.getColumnCount(); col++) {
                Object[] o = {m.getValueAt(row, col), row, col};
                encoder.writeStatement(new Statement(oldInstance, "setValueAt", o));
            }
        }
    }
}

class DefaultTableColumnModelPersistenceDelegate extends DefaultPersistenceDelegate {
    @Override protected void initialize(Class<?> type, Object oldInstance, Object newInstance, Encoder encoder) {
        super.initialize(type, oldInstance, newInstance, encoder);
        DefaultTableColumnModel m = (DefaultTableColumnModel) oldInstance;
        for (int col = 0; col < m.getColumnCount(); col++) {
            Object[] o = {m.getColumn(col)};
            encoder.writeStatement(new Statement(oldInstance, "addColumn", o));
        }
    }
}

class TableHeaderPopupMenu extends JPopupMenu {
    private final JTextField textField = new JTextField();
    private final JMenuItem editItem = new JMenuItem(new AbstractAction("Edit: setHeaderValue") {
        @Override public void actionPerformed(ActionEvent e) {
            JTableHeader header = (JTableHeader) getInvoker();
            TableColumn column = header.getColumnModel().getColumn(index);
            String name = column.getHeaderValue().toString();
            textField.setText(name);
            int result = JOptionPane.showConfirmDialog(
                header.getTable(), textField, getValue(Action.NAME).toString(),
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (result == JOptionPane.OK_OPTION) {
                String str = textField.getText().trim();
                if (!str.equals(name)) {
                    column.setHeaderValue(str);
                    header.repaint(header.getHeaderRect(index));
                }
            }
        }
    });
    private int index = -1;
    protected TableHeaderPopupMenu() {
        super();
        textField.addAncestorListener(new AncestorListener() {
            @Override public void ancestorAdded(AncestorEvent e) {
                textField.requestFocusInWindow();
            }
            @Override public void ancestorMoved(AncestorEvent e)   { /* not needed */ }
            @Override public void ancestorRemoved(AncestorEvent e) { /* not needed */ }
        });
        add(editItem);
    }
    @Override public void show(Component c, int x, int y) {
        if (c instanceof JTableHeader) {
            JTableHeader header = (JTableHeader) c;
            header.setDraggedColumn(null);
            header.repaint();
            header.getTable().repaint();
            index = header.columnAtPoint(new Point(x, y));
            super.show(c, x, y);
        }
    }
}
