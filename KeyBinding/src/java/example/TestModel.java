package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;

public class TestModel extends DefaultTableModel {
    private static final ColumnContext[] columnArray = {
        new ColumnContext("Focus", String.class, false),
        new ColumnContext("ActionName", String.class, false),
        new ColumnContext("KeyDescription", String.class, false)
    };
    private int number = 0;
    public void addTest(Test t) {
        Integer ft = t.getFocusType();
        String s = (ft==JComponent.WHEN_FOCUSED)?"WHEN_FOCUSED"
          :(ft==JComponent.WHEN_IN_FOCUSED_WINDOW)?"WHEN_IN_FOCUSED_WINDOW"
            :"WHEN_ANCESTOR_OF_FOCUSED_COMPONENT";
        Object[] obj = {s, t.getActionName(), t.getKeyDescription()};
        super.addRow(obj);
        number++;
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
    private Integer focusType;
    private String actionName, keyDescription;
    public Test(Integer focusType, String actionName, String keyDescription) {
        this.focusType = focusType;
        this.actionName = actionName;
        this.keyDescription = keyDescription;
    }
    public void setFocusType(Integer focus) {
        focusType = focus;
    }
    public void setActionName(String str) {
        actionName = str;
    }
    public void setKeyDescription(String str) {
        keyDescription = str;
    }
    public Integer getFocusType() {
        return focusType;
    }
    public String getActionName() {
        return actionName;
    }
    public String getKeyDescription() {
        return keyDescription;
    }
}
