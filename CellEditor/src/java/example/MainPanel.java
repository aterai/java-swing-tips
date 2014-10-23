package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;
import javax.swing.*;
import javax.swing.table.*;

public final class MainPanel extends JPanel {
    private final JCheckBox modelCheck    = new JCheckBox("isCellEditable return false");
    private final JCheckBox objectCheck   = new JCheckBox("setDefaultEditor(Object.class, null)");
    private final JCheckBox editableCheck = new JCheckBox("setEnabled(false)");
    private final TestModel model = new TestModel() {
        @Override public boolean isCellEditable(int row, int col) {
            return !modelCheck.isSelected();
        }
    };
    private final JTable table = new JTable(model);

    public MainPanel() {
        super(new BorderLayout());
        model.addTest(new Test("Name 1", "Comment"));
        model.addTest(new Test("Name 2", "Test"));
        model.addTest(new Test("Name d", "ee"));
        model.addTest(new Test("Name c", "Test cc"));
        model.addTest(new Test("Name b", "Test bb"));
        model.addTest(new Test("Name a", "ff"));
        model.addTest(new Test("Name 0", "Test aa"));
        model.addTest(new Test("Name 0", "gg"));

        TableColumn col = table.getColumnModel().getColumn(0);
        col.setMinWidth(50);
        col.setMaxWidth(50);
        col.setResizable(false);

        final ActionListener al = new ActionListener() {
            private final DefaultCellEditor dce = new DefaultCellEditor(new JTextField());
            @Override public void actionPerformed(ActionEvent e) {
                table.clearSelection();
                if (table.isEditing()) {
                    table.getCellEditor().stopCellEditing();
                }
                table.setDefaultEditor(Object.class, objectCheck.isSelected() ? null : dce);
                table.setEnabled(!editableCheck.isSelected());
            }
        };
        JPanel p = new JPanel(new GridLayout(3, 1));
        for (JCheckBox cb: Arrays.asList(modelCheck, objectCheck, editableCheck)) {
            cb.addActionListener(al);
            p.add(cb);
        }
        add(p, BorderLayout.NORTH);
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
        } catch (ClassNotFoundException | InstantiationException |
                 IllegalAccessException | UnsupportedLookAndFeelException ex) {
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

class TestModel extends DefaultTableModel {
    private static final ColumnContext[] COLUMN_ARRAY = {
        new ColumnContext("No.",     Integer.class, false),
        new ColumnContext("Name",    String.class,  true),
        new ColumnContext("Comment", String.class,  true)
    };
    private int number;
    public void addTest(Test t) {
        Object[] obj = {number, t.getName(), t.getComment()};
        super.addRow(obj);
        number++;
    }
    @Override public boolean isCellEditable(int row, int col) {
        return COLUMN_ARRAY[col].isEditable;
    }
    @Override public Class<?> getColumnClass(int modelIndex) {
        return COLUMN_ARRAY[modelIndex].columnClass;
    }
    @Override public int getColumnCount() {
        return COLUMN_ARRAY.length;
    }
    @Override public String getColumnName(int modelIndex) {
        return COLUMN_ARRAY[modelIndex].columnName;
    }
    private static class ColumnContext {
        public final String  columnName;
        public final Class   columnClass;
        public final boolean isEditable;
        public ColumnContext(String columnName, Class columnClass, boolean isEditable) {
            this.columnName = columnName;
            this.columnClass = columnClass;
            this.isEditable = isEditable;
        }
    }
}

class Test {
    private String name, comment;
    public Test(String name, String comment) {
        this.name = name;
        this.comment = comment;
    }
    public void setName(String str) {
        name = str;
    }
    public void setComment(String str) {
        comment = str;
    }
    public String getName() {
        return name;
    }
    public String getComment() {
        return comment;
    }
}
