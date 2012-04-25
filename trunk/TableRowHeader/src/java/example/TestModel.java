package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;

public class TestModel extends DefaultTableModel {
    private static final ColumnContext[] columnArray = {
        //new ColumnContext("No.",     Integer.class, false),
        new ColumnContext("Name",    String.class,  false),
        new ColumnContext("Comment", String.class,  false)
    };
    private int number = 0;
    private final DefaultListModel rowListModel;
    public TestModel(DefaultListModel lm) {
        super();
        rowListModel = lm;
    }
    @SuppressWarnings("unchecked")
    public void addTest(Test t) {
        Object[] obj = {t.getName(), t.getComment()};
        super.addRow(obj);
        rowListModel.addElement("row"+number);
        number++;
    }
    public void removeRow(int index) {
        super.removeRow(index);
        rowListModel.remove(index);
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
