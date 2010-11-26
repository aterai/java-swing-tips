package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;

public class TestModel extends DefaultTableModel {
    private static final ColumnContext[] columnArray = {
        new ColumnContext("No.", Integer.class, false),
        new ColumnContext("Name", String.class, false),
        new ColumnContext("Progress", Integer.class, false)
    };
    private final Map<Integer, SwingWorker> swmap = new HashMap<Integer, SwingWorker>();
    private int number = 0;
    public void addTest(Test t, SwingWorker worker) {
        Object[] obj = {number, t.getName(), t.getProgress()};
        super.addRow(obj);
        swmap.put(number, worker);
        number++;
    }
    public synchronized SwingWorker getSwingWorker(int identifier) {
        Integer key = (Integer)getValueAt(identifier, 0);
        return swmap.get(key);
    }
    public Test getTest(int identifier) {
        return new Test((String)getValueAt(identifier,1), (Integer)getValueAt(identifier,2));
    }
    @Override public boolean isCellEditable(int row, int col) {
        return columnArray[col].isEditable;
    }
    @Override public Class<?> getColumnClass(int modelIndex) {
        return columnArray[modelIndex].columnClass;
    }
    @Override public int getColumnCount() {
        return columnArray.length;
    }
    @Override public String getColumnName(int modelIndex) {
        return columnArray[modelIndex].columnName;
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
class Test{
    private String name;
    private Integer progress;
    public Test(String name, Integer progress) {
        this.name = name;
        this.progress = progress;
    }
    public void setName(String str) {
        name = str;
    }
    public void setProgress(Integer str) {
        progress = str;
    }
    public String getName() {
        return name;
    }
    public Integer getProgress() {
        return progress;
    }
}
class ProgressRenderer extends DefaultTableCellRenderer {
    private final JProgressBar b = new JProgressBar(0, 100);
    public ProgressRenderer() {
        super();
        setOpaque(true);
        b.setBorder(BorderFactory.createEmptyBorder(1,1,1,1));
    }
    @Override public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus,
                                                   int row, int column) {
        Integer i = (Integer)value;
        String text = "Done";
        if(i<0) {
            text = "Canceled";
        }else if(i<100) {
            b.setValue(i);
            return b;
        }
        super.getTableCellRendererComponent(table, text, isSelected, hasFocus, row, column);
        return this;
    }
}
