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
        new ColumnContext("No.",     Integer.class, false),
        new ColumnContext("Name",    String.class,  true),
        new ColumnContext("Comment", String.class,  true)
    };
    private int number = 0;
    public void addTest(Test t) {
        Object[] obj = {number, t.getName(), t.getComment()};
        super.addRow(obj);
        number++;
        list.add(t);
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
    private final Vector<Test> list = new Vector<Test>();
    public void filterRows(boolean flg) {
        //dataVector.clear();
        setRowCount(0);
        for(int i=0;i<list.size();i++) {
            if(flg && i%2==0) continue;
            Test t = list.elementAt(i);
            addRow(convertToVector(new Object[] {i, t.getName(), t.getComment()}));
            //dataVector.add(convertToVector(new Object[] {i, t.getName(), t.getComment()}));
        }
        //setDataVector(v, columnIdentifiers);
        fireTableDataChanged();
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
