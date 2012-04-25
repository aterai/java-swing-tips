package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

public class MainPanel extends JPanel {
    private final DefaultListModel listModel = new DefaultListModel();
    private final TestModel model = new TestModel(listModel);
    private final JTable table = new JTable(model);
    public MainPanel() {
        super(new BorderLayout());
        model.addTest(new Test("Name 1", "comment"));
        model.addTest(new Test("Name 2", "test"));
        model.addTest(new Test("Name d", ""));
        model.addTest(new Test("Name c", "test cc"));
        model.addTest(new Test("Name b", "test bb"));
        model.addTest(new Test("Name a", ""));
        model.addTest(new Test("Name 0", "test aa"));
        model.addTest(new Test("Name 0", ""));

        table.setCellSelectionEnabled(true);

        final JTableHeader header = table.getTableHeader();
        header.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                if(table.isEditing()) {
                    table.getCellEditor().stopCellEditing();
                }
                int col = header.columnAtPoint(e.getPoint());
                table.changeSelection(0, col, false, false);
                table.changeSelection(table.getRowCount()-1, col, false, true);
            }
        });

        RowHeaderList rowHeader = new RowHeaderList(listModel, table);
        rowHeader.setFixedCellWidth(50);

        final JScrollPane scroll = new JScrollPane(table);
        scroll.setRowHeaderView(rowHeader);
        scroll.getRowHeader().addChangeListener(new ChangeListener() {
            @Override public void stateChanged(ChangeEvent e) {
                JViewport viewport = (JViewport) e.getSource();
                scroll.getVerticalScrollBar().setValue(viewport.getViewPosition().y);
            }
        });
        scroll.setComponentPopupMenu(new TablePopupMenu());
        table.setInheritsPopupMenu(true);

        rowHeader.setBackground(Color.BLUE);
        scroll.setBackground(Color.RED);
        scroll.getViewport().setBackground(Color.GREEN);

        add(scroll);
        setPreferredSize(new Dimension(320, 200));
    }

    class TestCreateAction extends AbstractAction{
        public TestCreateAction(String label, Icon icon) {
            super(label,icon);
        }
        @Override public void actionPerformed(ActionEvent evt) {
            testCreateActionPerformed(evt);
        }
    }
    private void testCreateActionPerformed(ActionEvent e) {
        model.addTest(new Test("add row", ""));
        Rectangle rect = table.getCellRect(model.getRowCount()-1, 0, true);
        table.scrollRectToVisible(rect);
    }

    class DeleteAction extends AbstractAction{
        public DeleteAction(String label, Icon icon) {
            super(label,icon);
        }
        @Override public void actionPerformed(ActionEvent evt) {
            deleteActionPerformed(evt);
        }
    }
    public void deleteActionPerformed(ActionEvent evt) {
        int[] selection = table.getSelectedRows();
        if(selection==null || selection.length<=0) return;
        for(int i=selection.length-1;i>=0;i--) {
            model.removeRow(table.convertRowIndexToModel(selection[i]));
        }
    }

    private class TablePopupMenu extends JPopupMenu {
        private final Action deleteAction = new DeleteAction("delete", null);
        public TablePopupMenu() {
            super();
            add(new TestCreateAction("add", null));
            //add(new ClearAction("clearSelection", null));
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

class RowHeaderList extends JList {
    private final JTable table;
    private final ListSelectionModel tableSelection;
    private final ListSelectionModel rListSelection;
    @SuppressWarnings("unchecked")
    public RowHeaderList(ListModel model, JTable table) {
        super(model);
        this.table = table;
        setFixedCellHeight(table.getRowHeight());
        setCellRenderer(new RowHeaderRenderer(table.getTableHeader()));
        //setSelectionModel(table.getSelectionModel());
        RollOverListener rol = new RollOverListener();
        addMouseListener(rol);
        addMouseMotionListener(rol);
        //setBorder(BorderFactory.createMatteBorder(1,0,0,0,Color.GRAY.brighter()));

        tableSelection = table.getSelectionModel();
        rListSelection = getSelectionModel();
    }
    class RowHeaderRenderer extends JLabel implements ListCellRenderer {
        private final JTableHeader header; // = table.getTableHeader();
        public RowHeaderRenderer(JTableHeader header) {
            this.header = header;
            this.setOpaque(true);
            //this.setBorder(UIManager.getBorder("TableHeader.cellBorder"));
            this.setBorder(BorderFactory.createMatteBorder(0,0,1,2,Color.GRAY.brighter()));
            this.setHorizontalAlignment(CENTER);
            this.setForeground(header.getForeground());
            this.setBackground(header.getBackground());
            this.setFont(header.getFont());
        }
        @Override public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            if(index==pressedRowIndex) {
                setBackground(Color.GRAY);
            }else if(index==rollOverRowIndex) {
                setBackground(Color.WHITE);
            }else if(isSelected) {
                setBackground(Color.GRAY.brighter());
            }else{
                setForeground(header.getForeground());
                setBackground(header.getBackground());
            }
            setText((value == null) ? "" : value.toString());
            return this;
        }
    }
    private int rollOverRowIndex = -1;
    private int pressedRowIndex  = -1;
    class RollOverListener extends MouseAdapter{
        @Override public void mouseExited(MouseEvent e) {
            if(pressedRowIndex<0) {
                //pressedRowIndex  = -1;
                rollOverRowIndex = -1;
                repaint();
            }
        }
        @Override public void mouseMoved(MouseEvent e) {
            int row = locationToIndex(e.getPoint());
            if( row != rollOverRowIndex ) {
                rollOverRowIndex = row;
                repaint();
            }
        }
        @Override public void mouseDragged(MouseEvent e) {
            if(pressedRowIndex>=0) {
                int row   = locationToIndex(e.getPoint());
                int start = Math.min(row,pressedRowIndex);
                int end   = Math.max(row,pressedRowIndex);
                tableSelection.clearSelection();
                rListSelection.clearSelection();
                tableSelection.addSelectionInterval(start, end);
                rListSelection.addSelectionInterval(start, end);
                repaint();
            }
        }
        @Override public void mousePressed(MouseEvent e) {
            int row = locationToIndex(e.getPoint());
            if(row==pressedRowIndex) {
                return;
            }
            rListSelection.clearSelection();
            table.changeSelection(row, 0, false, false);
            table.changeSelection(row, table.getColumnModel().getColumnCount()-1, false, true);
            pressedRowIndex = row;
//             table.setRowSelectionInterval(row, row);
//             table.getSelectionModel().setSelectionInterval(row, row);
//             tableSelection.clearSelection();
//             table.getSelectionModel().setAnchorSelectionIndex(row);
//             table.getSelectionModel().setLeadSelectionIndex(row);
//             tableSelection.addSelectionInterval(row,row);
//             rListSelection.addSelectionInterval(row,row);
//             table.getColumnModel().getSelectionModel().setAnchorSelectionIndex(0);
//             table.getColumnModel().getSelectionModel().setLeadSelectionIndex(0);
//             table.changeSelection(pressedRowIndex, table.getColumnModel().getColumnCount()-1, false, true);
        }
        @Override public void mouseReleased(MouseEvent e) {
            rListSelection.clearSelection();
            pressedRowIndex  = -1;
            rollOverRowIndex = -1;
            repaint();
        }
    }
}
