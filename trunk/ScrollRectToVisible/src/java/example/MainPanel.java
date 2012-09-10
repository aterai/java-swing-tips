package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.Date;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.tree.*;

public class MainPanel extends JPanel{
    private final String[] columnNames = {"String", "Integer", "Boolean"};
    private final Object[][] data = {
        {"aaa", 12, true}, {"bbb", 5, false},
        {"CCC", 92, true}, {"DDD", 0, false}
    };
    private final DefaultTableModel model = new DefaultTableModel(data, columnNames) {
        @Override public Class<?> getColumnClass(int column) {
            return getValueAt(0, column).getClass();
        }
    };
    private final JTable table = new JTable(model);

    private final DefaultListModel listModel = new DefaultListModel();
    @SuppressWarnings("unchecked")
    private final JList list = new JList(listModel);

    private final JTree tree = new JTree();

    public MainPanel() {
        super(new BorderLayout());
        table.setAutoCreateRowSorter(true);
        table.setFillsViewportHeight(true);
        //table.setComponentPopupMenu(new TablePopupMenu());

        JTabbedPane t = new JTabbedPane();
        t.addTab("JTable", new JScrollPane(table));
        t.addTab("JList",  new JScrollPane(list));
        t.addTab("JTree",  new JScrollPane(tree));

        timer = new Timer(1000, new ActionListener() {
            @SuppressWarnings("unchecked")
            @Override public void actionPerformed(ActionEvent e) {
                Date date = new Date();

                //JTable
                model.addRow(new Object[] {date.toString(), model.getRowCount(), false});
                int i = table.convertRowIndexToView(model.getRowCount()-1);
                Rectangle r = table.getCellRect(i, 0, true);
                table.scrollRectToVisible(r);

                //JList
                listModel.addElement(date);
                int index = listModel.getSize()-1;
                list.ensureIndexIsVisible(index);
                //Rectangle cellBounds = list.getCellBounds(index, index);
                //if(cellBounds != null) {
                //    list.scrollRectToVisible(cellBounds);
                //}

                //JTree
                DefaultTreeModel treeModel = (DefaultTreeModel)tree.getModel();
                DefaultMutableTreeNode parent   = (DefaultMutableTreeNode)treeModel.getRoot();
                DefaultMutableTreeNode newChild = new DefaultMutableTreeNode(date);
                treeModel.insertNodeInto(newChild, parent, parent.getChildCount());
                /* //tree.scrollRowToVisible(row) == tree.scrollPathToVisible(tree.getPathForRow(row))
                tree.scrollRowToVisible(tree.getRowCount()-1);
                /*/
                tree.scrollPathToVisible(new TreePath(newChild.getPath()));
                //*/
            }
        });
        timer.start();
        add(t);
        setPreferredSize(new Dimension(320, 240));
    }
    private transient Timer timer;
    private transient HierarchyListener hierarchyListener;
    @Override public void updateUI() {
        if(hierarchyListener!=null) removeHierarchyListener(hierarchyListener);
        super.updateUI();
        addHierarchyListener(hierarchyListener = new HierarchyListener() {
            @Override public void hierarchyChanged(HierarchyEvent e) {
                JComponent c = (JComponent)e.getSource();
                if(timer!=null && (e.getChangeFlags() & HierarchyEvent.DISPLAYABILITY_CHANGED)!=0 && !c.isDisplayable()) {
                    System.out.println("case DISPOSE_ON_CLOSE: hierarchyChanged");
                    timer.stop();
                }
            }
        });
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
        //frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
