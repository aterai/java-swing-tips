package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.*;
import javax.swing.table.*;

public class MainPanel extends JPanel {
    private final String[] columnNames = {"String", "Integer", "Boolean"};
    private final Object[][] data = {
        {"aaa", 12, true}, {"bbb", 5, false},
        {"CCC", 92, true}, {"DDD", 0, false}
    };
    private final DefaultTableModel model = new DefaultTableModel(data, columnNames) {
        @Override public Class<?> getColumnClass(int column) {
            //ArrayIndexOutOfBoundsException:  0 >= 0
            //Bug ID: JDK-6967479 JTable sorter fires even if the model is empty
            //http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6967479
            //return getValueAt(0, column).getClass();
            switch(column) {
              case 0:
                return String.class;
              case 1:
                return Number.class;
              case 2:
                return Boolean.class;
              default:
                return super.getColumnClass(column);
            }
        }
    };
    private final JTable table;
    public MainPanel() {
        super(new BorderLayout());
        UIManager.put("Table.focusCellHighlightBorder", new DotBorder(2,2,2,2));
        table = new JTable(model) {
            @Override public void updateUI() {
                //Bug ID: 6788475 Changing to Nimbus LAF and back doesn't reset look and feel of JTable completely
                //http://bugs.sun.com/view_bug.do?bug_id=6788475
                //XXX: set dummy ColorUIResource
                setSelectionForeground(new ColorUIResource(Color.RED));
                setSelectionBackground(new ColorUIResource(Color.RED));
                super.updateUI();
                updateRenderer();
                remakeBooleanEditor();
            }
            private void updateRenderer() {
                TableModel m = getModel();
                for(int i=0;i<m.getColumnCount();i++) {
                    TableCellRenderer r = getDefaultRenderer(m.getColumnClass(i));
                    if(r instanceof JComponent) {
                        ((JComponent)r).updateUI();
                    }
                }
            }
            private void remakeBooleanEditor() {
                JCheckBox checkBox = new JCheckBox();
                checkBox.setHorizontalAlignment(JCheckBox.CENTER);
                checkBox.setBorderPainted(true);
                checkBox.setOpaque(true);
                checkBox.addMouseListener(new MouseAdapter() {
                    @Override public void mousePressed(MouseEvent e) {
                        JCheckBox cb = (JCheckBox)e.getSource();
                        ButtonModel m = cb.getModel();
                        if(m.isPressed() && isRowSelected(getEditingRow()) && e.isControlDown()) {
                            if(getEditingRow()%2==0) {
                                cb.setOpaque(false);
                                //cb.setBackground(getBackground());
                            }else{
                                cb.setOpaque(true);
                                cb.setBackground(UIManager.getColor("Table.alternateRowColor"));
                            }
                        }else{
                            cb.setBackground(getSelectionBackground());
                            cb.setOpaque(true);
                        }
                    }
                    @Override public void mouseExited(MouseEvent e) {
                        //in order to drag table row selection
                        if(isEditing() && !getCellEditor().stopCellEditing()) {
                            getCellEditor().cancelCellEditing();
                        }
                    }
                });
                setDefaultEditor(Boolean.class, new DefaultCellEditor(checkBox));
            }
            private final DotBorder dotBorder = new DotBorder(2,2,2,2);
            private final Border emptyBorder  = BorderFactory.createEmptyBorder(2,2,2,2);
            private void updateBorderType(DotBorder border, int column) {
                border.type = EnumSet.noneOf(DotBorder.Type.class);
                if(column==0) border.type.add(DotBorder.Type.START);
                if(column==getColumnCount()-1) border.type.add(DotBorder.Type.END);
            }
            @Override public Component prepareRenderer(TableCellRenderer tcr, int row, int column) {
                Component c = super.prepareRenderer(tcr, row, column);
                if(c instanceof JCheckBox) {
                    JCheckBox b = (JCheckBox)c;
                    b.setBorderPainted(true);
                }
                if(row==getSelectionModel().getLeadSelectionIndex()) { //isRowSelected(row)) {
                    ((JComponent)c).setBorder(dotBorder);
                    updateBorderType(dotBorder, column);
                }else{
                    ((JComponent)c).setBorder(emptyBorder);
                }
                return c;
            }
            @Override public Component prepareEditor(TableCellEditor editor, int row, int column) {
                Component c = super.prepareEditor(editor, row, column);
                if(c instanceof JCheckBox) {
                    JCheckBox b = (JCheckBox)c;
                    //System.out.println(b.getBorder());
                    b.setBorder(dotBorder);
                    updateBorderType(dotBorder, column);
                    //updateBorderType((DotBorder)b.getBorder(), column);
                    //b.setBorderPainted(true);
                    //b.setBackground(getSelectionBackground());
                }
                return c;
            }
        };

        //TableColumnModel columns = table.getColumnModel();
        //for(int i=0;i<columns.getColumnCount();i++) {
        //    columns.getColumn(i).setCellRenderer(new TestRenderer());
        //}

        table.setRowSelectionAllowed(true);
        table.setAutoCreateRowSorter(true);
        table.setFillsViewportHeight(true);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension());
        table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

        table.setComponentPopupMenu(new TablePopupMenu());
        add(new JScrollPane(table));
        setPreferredSize(new Dimension(320, 240));
    }
    class TestCreateAction extends AbstractAction{
        public TestCreateAction(String label, Icon icon) {
            super(label,icon);
        }
        @Override public void actionPerformed(ActionEvent e) {
            model.addRow(new Object[] {"New row", 0, true});
            Rectangle r = table.getCellRect(model.getRowCount()-1, 0, true);
            table.scrollRectToVisible(r);
        }
    }
    class DeleteAction extends AbstractAction{
        public DeleteAction(String label, Icon icon) {
            super(label,icon);
        }
        @Override public void actionPerformed(ActionEvent e) {
            int[] selection = table.getSelectedRows();
            if(selection==null || selection.length<=0) return;
            for(int i=selection.length-1;i>=0;i--) {
                model.removeRow(table.convertRowIndexToModel(selection[i]));
            }
        }
    }
    class ClearAction extends AbstractAction{
        public ClearAction(String label, Icon icon) {
            super(label,icon);
        }
        @Override public void actionPerformed(ActionEvent e) {
            table.clearSelection();
        }
    }
    private class TablePopupMenu extends JPopupMenu {
        private final Action deleteAction = new DeleteAction("delete", null);
        public TablePopupMenu() {
            super();
            add(new TestCreateAction("add", null));
            add(new ClearAction("clearSelection", null));
            addSeparator();
            add(deleteAction);
        }
        @Override public void show(Component c, int x, int y) {
            int[] l = table.getSelectedRows();
            deleteAction.setEnabled(l!=null && l.length>0);
            super.show(c, x, y);
        }
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
// class TestRenderer extends DefaultTableCellRenderer {
//     private static final DotBorder dotBorder = new DotBorder(2,2,2,2);
//     private static final Border emptyBorder  = BorderFactory.createEmptyBorder(2,2,2,2);
//     @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
//         Component c = super.getTableCellRendererComponent(table,value,isSelected,hasFocus,row,column);
//         if(c instanceof JComponent) {
//             int lsi = table.getSelectionModel().getLeadSelectionIndex();
//             ((JComponent)c).setBorder(row==lsi?dotBorder:emptyBorder);
//             dotBorder.setLastCellFlag(row==lsi&&column==table.getColumnCount()-1);
//         }
//         return c;
//     }
// }
class DotBorder extends EmptyBorder {
    public enum Type { START, END; }
    private static final BasicStroke dashed = new BasicStroke(
        1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
        10.0f, new float[] {1.0f}, 0.0f);
    private static final Color dotColor = new Color(200,150,150);
    public DotBorder(int top, int left, int bottom, int right) {
        super(top, left, bottom, right);
    }
    public EnumSet<Type> type = EnumSet.noneOf(Type.class);
    @Override public boolean isBorderOpaque() {
        return true;
    }
    @Override public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
        Graphics2D g2 = (Graphics2D)g;
        g2.translate(x,y);
        g2.setPaint(dotColor);
        g2.setStroke(dashed);
        if(type.contains(Type.START)) {
            g2.drawLine(0,0,0,h);
        }
        if(type.contains(Type.END)) {
            g2.drawLine(w-1,0,w-1,h);
        }
        if(c.getBounds().x%2==0) {
            g2.drawLine(0,0,w,0);
            g2.drawLine(0,h-1,w,h-1);
        }else{
            g2.drawLine(1,0,w,0);
            g2.drawLine(1,h-1,w,h-1);
        }
        g2.translate(-x,-y);
    }
}

// //Another example test:
// import java.awt.*;
// import java.util.*;
// import javax.swing.*;
// import javax.swing.border.*;
// import javax.swing.table.*;
// class FocusCellHighlightBorderTest {
//   String[] columnNames = {"String", "Integer", "Boolean"};
//   Object[][] data = {
//     {"aaa", 12, true}, {"bbb", 5, false}, {"CCC", 92, true}
//   };
//   DefaultTableModel model = new DefaultTableModel(data, columnNames) {
//     @Override public Class<?> getColumnClass(int column) {
//       return getValueAt(0, column).getClass();
//     }
//   };
//   public JComponent makeUI() {
//     UIManager.put("Table.focusCellHighlightBorder", new DotBorder(2,2,2,2));
//     JTable table = new JTable(model) {
//       private final DotBorder dotBorder = new DotBorder(2,2,2,2);
//       private void updateBorderType(DotBorder border, boolean isLeadRow, int column) {
//         border.type = EnumSet.noneOf(DotBorder.Type.class);
//         if(isLeadRow) {
//           border.type.add(DotBorder.Type.LEAD);
//           if(column==0) border.type.add(DotBorder.Type.WEST);
//           if(column==getColumnCount()-1) border.type.add(DotBorder.Type.EAST);
//         }
//       }
//       @Override public Component prepareRenderer(
//       TableCellRenderer tcr, int row, int column) {
//         JComponent c = (JComponent)super.prepareRenderer(tcr, row, column);
//         c.setBorder(dotBorder);
//         updateBorderType(dotBorder, row==getSelectionModel().getLeadSelectionIndex(), column);
//         return c;
//       }
//       @Override public Component prepareEditor(
//       TableCellEditor editor, int row, int column) {
//         Component c = super.prepareEditor(editor, row, column);
//         if(c instanceof JCheckBox) {
//           JCheckBox b = (JCheckBox)c;
//           updateBorderType((DotBorder)b.getBorder(), true, column);
//           b.setBorderPainted(true);
//           b.setBackground(getSelectionBackground());
//         }
//         return c;
//       }
//     };
//     table.setShowGrid(false);
//     table.setIntercellSpacing(new Dimension());
//     JPanel p = new JPanel(new BorderLayout());
//     p.add(new JScrollPane(table));
//     return p;
//   }
//   public static void main(String[] args) {
//     EventQueue.invokeLater(new Runnable() {
//       @Override public void run() {
//         createAndShowGUI();
//       }
//     });
//   }
//   public static void createAndShowGUI() {
//     JFrame frame = new JFrame();
//     frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
//     frame.getContentPane().add(new FocusCellHighlightBorderTest().makeUI());
//     frame.setSize(320, 240);
//     frame.setLocationRelativeTo(null);
//     frame.setVisible(true);
//   }
// }
// class DotBorder extends EmptyBorder {
//   public enum Type { LEAD, WEST, EAST; }
//   public EnumSet<Type> type = EnumSet.noneOf(Type.class);
//   private static final BasicStroke dashed = new BasicStroke(
//     1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
//     10.0f, (new float[] {1.0f}), 0.0f);
//   private static final Color dotColor = new Color(200,150,150);
//   public DotBorder(int top, int left, int bottom, int right) {
//     super(top, left, bottom, right);
//   }
//   @Override public boolean isBorderOpaque() {
//     return true;
//   }
//   @Override public void paintBorder(
//     Component c, Graphics g, int x, int y, int w, int h) {
//     Graphics2D g2 = (Graphics2D)g;
//     g2.translate(x,y);
//     g2.setPaint(dotColor);
//     g2.setStroke(dashed);
//     if(type.contains(Type.WEST)) {
//       g2.drawLine(0,0,0,h);
//     }
//     if(type.contains(Type.EAST)) {
//       g2.drawLine(w-1,0,w-1,h);
//     }
//     if(type.contains(Type.LEAD)) {
//       if(c.getBounds().x%2==0) {
//         g2.drawLine(0,0,w,0);
//         g2.drawLine(0,h-1,w,h-1);
//       }else{
//         g2.drawLine(1,0,w,0);
//         g2.drawLine(1,h-1,w,h-1);
//       }
//     }
//     g2.translate(-x,-y);
//   }
// }
