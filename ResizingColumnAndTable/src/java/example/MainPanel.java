package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;

public final class MainPanel extends JPanel {
    private final JTable table = new JTable(100, 3) {
        //http://stackoverflow.com/questions/16368343/jtable-resize-only-selected-column-when-container-size-changes
        //http://stackoverflow.com/questions/23201818/jtable-columns-doesnt-resize-probably-when-jframe-resize
        @Override public void doLayout() {
            Optional.ofNullable(getTableHeader()).ifPresent(header -> {
                if (Objects.isNull(header.getResizingColumn()) && getAutoResizeMode() == JTable.AUTO_RESIZE_LAST_COLUMN) {
                    TableColumnModel tcm = getColumnModel();
                    header.setResizingColumn(tcm.getColumn(tcm.getColumnCount() - 1));
                }
            });
            super.doLayout();
        }
    };
    public MainPanel() {
        super(new GridLayout(0, 1));
        //table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        add(makePanel(new JTable(100, 3), "Normal JTable.AUTO_RESIZE_LAST_COLUMN"));
        add(makePanel(table, "Resize only last column when JTable resized"));

//         //TEST:
//         JTable table1 = new JTable(100, 3) {
//             private transient ComponentListener resizeHandler;
//             @Override public void updateUI() {
//                 removeComponentListener(resizeHandler);
//                 super.updateUI();
//                 resizeHandler = new ComponentAdapter() {
//                     @Override public void componentResized(ComponentEvent e) {
//                         Optional.ofNullable(getTableHeader()).ifPresent(header -> {
//                             if (Objects.isNull(header.getResizingColumn()) && getAutoResizeMode() == JTable.AUTO_RESIZE_LAST_COLUMN) {
//                                 TableColumnModel tcm = getColumnModel();
//                                 header.setResizingColumn(tcm.getColumn(tcm.getColumnCount() - 1));
//                             }
//                         });
//                     }
//                 };
//                 addComponentListener(resizeHandler);
//             }
//         };
//         add(makePanel(table1, "JTable#addComponentListener(...)"));
//
//         JTable table2 = new JTable(100, 3);
//         table2.getTableHeader().addComponentListener(new ComponentAdapter() {
//             @Override public void componentResized(ComponentEvent e) {
//                 Optional.ofNullable(table2.getTableHeader()).ifPresent(header -> {
//                     if (Objects.isNull(header.getResizingColumn()) && table2.getAutoResizeMode() == JTable.AUTO_RESIZE_LAST_COLUMN) {
//                         TableColumnModel tcm = table2.getColumnModel();
//                         header.setResizingColumn(tcm.getColumn(tcm.getColumnCount() - 1));
//                     }
//                 });
//             }
//         });
//         add(makePanel(table2, "JTableHeader#addComponentListener(...)"));
        setPreferredSize(new Dimension(320, 240));
    }
    private static JPanel makePanel(JTable table, String title) {
        table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder(title));
        p.add(new JScrollPane(table));
        return p;
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
        } catch (ClassNotFoundException | InstantiationException
               | IllegalAccessException | UnsupportedLookAndFeelException ex) {
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
