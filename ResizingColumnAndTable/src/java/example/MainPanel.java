package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;

public final class MainPanel extends JPanel {
    private final JTable table = new JTable(100, 3) {
        private transient ComponentListener resizeHandler;
        @Override public void updateUI() {
            removeComponentListener(resizeHandler);
            super.updateUI();
            resizeHandler = new ComponentAdapter() {
                @Override public void componentResized(ComponentEvent e) {
                    JTable table = (JTable) e.getComponent();
                    JTableHeader tableHeader = table.getTableHeader();
                    if (tableHeader != null) {
                        tableHeader.setResizingColumn(null);
                    }
                }
            };
            addComponentListener(resizeHandler);
        }
        //http://stackoverflow.com/questions/16368343/jtable-resize-only-selected-column-when-container-size-changes
        //http://stackoverflow.com/questions/23201818/jtable-columns-doesnt-resize-probably-when-jframe-resize
        @Override public void doLayout() {
            if (tableHeader != null && autoResizeMode != AUTO_RESIZE_OFF && check.isSelected()) {
                TableColumn resizingColumn = tableHeader.getResizingColumn();
                if (resizingColumn == null) {
                    TableColumnModel tcm = getColumnModel();
                    int lastColumn = tcm.getColumnCount() - 1;
                    tableHeader.setResizingColumn(tcm.getColumn(lastColumn));
                }
            }
            super.doLayout();
        }
    };
    private final JCheckBox check = new JCheckBox("Resize only last column when JTable resized");

    public MainPanel() {
        super(new BorderLayout());
        table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
//         JTable table2 = new JTable(100, 3);
//         table2.getTableHeader().addComponentListener(new ComponentAdapter() {
//             @Override public void componentResized(ComponentEvent e) {
//                 JTableHeader tableHeader = (JTableHeader) e.getComponent();
//                 if (tableHeader == null) {
//                     return;
//                 }
//                 if (check.isSelected()) {
//                     TableColumnModel tcm = tableHeader.getTable().getColumnModel();
//                     int lastColumn = tcm.getColumnCount() - 1;
//                     tableHeader.setResizingColumn(tcm.getColumn(lastColumn));
//                 } else {
//                     tableHeader.setResizingColumn(null);
//                 }
//             }
//         });
//
//         JPanel p = new JPanel(new GridLayout(2, 1));
//         p.add(new JScrollPane(table));
//         p.add(new JScrollPane(table2));

        add(check, BorderLayout.NORTH);
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
