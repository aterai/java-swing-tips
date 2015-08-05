package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

public final class MainPanel extends JPanel {
    private static final String PLACEHOLDER = "<html>No data! <a href='dummy'>Input hint(beep)</a></html>";
    private final JEditorPane editor = new JEditorPane("text/html", PLACEHOLDER);
    private final String[] columnNames = {"Integer", "String", "Boolean"};
    private final DefaultTableModel model = new DefaultTableModel(null, columnNames) {
        @Override public Class<?> getColumnClass(int column) {
            switch (column) {
              case 0:  return Integer.class;
              case 2:  return Boolean.class;
              default: return String.class;
            }
        }
    };
    private final JTable table = new JTable(model);

    public MainPanel() {
        super(new BorderLayout());
        editor.setOpaque(false);
        editor.setEditable(false);
        editor.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        editor.addHyperlinkListener(new HyperlinkListener() {
            @Override public void hyperlinkUpdate(HyperlinkEvent e) {
                if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                    Toolkit.getDefaultToolkit().beep();
                }
            }
        });

        model.addTableModelListener(new TableModelListener() {
            @Override public void tableChanged(TableModelEvent e) {
                DefaultTableModel model = (DefaultTableModel) e.getSource();
                editor.setVisible(model.getRowCount() == 0);
            }
        });

        table.setAutoCreateRowSorter(true);
        table.setFillsViewportHeight(true);
        table.setComponentPopupMenu(new TablePopupMenu());
        table.setLayout(new GridBagLayout());
        table.add(editor);
        add(new JScrollPane(table));
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

class TablePopupMenu extends JPopupMenu {
    private final Action addAction = new AbstractAction("add") {
        @Override public void actionPerformed(ActionEvent e) {
            JTable table = (JTable) getInvoker();
            DefaultTableModel model = (DefaultTableModel) table.getModel();
            model.addRow(new Object[] {0, "aaa", Boolean.FALSE});
            Rectangle rect = table.getCellRect(model.getRowCount() - 1, 0, true);
            table.scrollRectToVisible(rect);
        }
    };
    private final Action deleteAction = new AbstractAction("delete") {
        @Override public void actionPerformed(ActionEvent e) {
            JTable table = (JTable) getInvoker();
            int[] selection = table.getSelectedRows();
            DefaultTableModel model = (DefaultTableModel) table.getModel();
            for (int i = selection.length - 1; i >= 0; i--) {
                model.removeRow(table.convertRowIndexToModel(selection[i]));
            }
        }
    };
    public TablePopupMenu() {
        super();
        add(addAction);
        addSeparator();
        add(deleteAction);
    }
    @Override public void show(Component c, int x, int y) {
        if (c instanceof JTable) {
            JTable table = (JTable) c;
            deleteAction.setEnabled(table.getSelectedRowCount() > 0);
            super.show(c, x, y);
        }
    }
}
