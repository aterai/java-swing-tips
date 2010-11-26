package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;

public class MainPanel extends JPanel {
    private final JCheckBox modelCheck    = new JCheckBox("isCellEditable return false");
    private final JCheckBox objectCheck   = new JCheckBox("setDefaultEditor(Object.class, null)");
    private final JCheckBox editableCheck = new JCheckBox("setEnabled(false)");
    private final JTable table;

    public MainPanel() {
        super(new BorderLayout());
        TestModel model = new TestModel() {
            @Override public boolean isCellEditable(int row, int col) {
                return (col==0)?false:!modelCheck.isSelected();
            }
        };
        model.addTest(new Test("Name 1", "Comment"));
        model.addTest(new Test("Name 2", "Test"));
        model.addTest(new Test("Name d", ""));
        model.addTest(new Test("Name c", "Test cc"));
        model.addTest(new Test("Name b", "Test bb"));
        model.addTest(new Test("Name a", ""));
        model.addTest(new Test("Name 0", "Test aa"));
        model.addTest(new Test("Name 0", ""));

        table = new JTable(model) {
            private final Color evenColor = new Color(250, 250, 250);
            @Override public Component prepareRenderer(TableCellRenderer tcr, int row, int column) {
                Component c = super.prepareRenderer(tcr, row, column);
                if(isRowSelected(row)) {
                    c.setForeground(getSelectionForeground());
                    c.setBackground(getSelectionBackground());
                }else{
                    c.setForeground(getForeground());
                    c.setBackground((row%2==0)?evenColor:table.getBackground());
                }
                return c;
            }
        };
        table.setRowSelectionAllowed(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);

        JTableHeader tableHeader = table.getTableHeader();
        tableHeader.setReorderingAllowed(false);

        TableColumn col = table.getColumnModel().getColumn(0);
        col.setMinWidth(50);
        col.setMaxWidth(50);
        col.setResizable(false);

        ActionListener al = new ActionListener() {
            private final DefaultCellEditor dce = new DefaultCellEditor(new JTextField());
            @Override public void actionPerformed(ActionEvent e) {
                table.clearSelection();
                if(table.isEditing()) table.getCellEditor().stopCellEditing();
                table.setDefaultEditor(Object.class, (objectCheck.isSelected())?null:dce);
                table.setEnabled(!editableCheck.isSelected());
            }
        };
        modelCheck.addActionListener(al);
        objectCheck.addActionListener(al);
        editableCheck.addActionListener(al);

        JPanel p = new JPanel(new GridLayout(3,1));
        p.add(modelCheck);
        p.add(objectCheck);
        p.add(editableCheck);

        add(p, BorderLayout.NORTH);
        add(new JScrollPane(table));
        setPreferredSize(new Dimension(320, 240));
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
