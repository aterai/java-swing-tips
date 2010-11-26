package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;

public class MainPanel extends JPanel{
    public MainPanel() {
        super(new BorderLayout());
        TestModel model = new TestModel();
        JTable table = new JTable(model);
        table.setRowSelectionAllowed(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);

        Font font = new Font("Sans-serif", Font.PLAIN, 32);
        JTableHeader header = table.getTableHeader();
        TableCellRenderer renderer = header.getDefaultRenderer();
        TableColumn c = table.getColumnModel().getColumn(0);
        c.setHeaderRenderer(new HeaderRenderer(renderer, font));

        model.addTest(new Test("Name 1", "comment..."));
        model.addTest(new Test("Name 2", "Test"));
        model.addTest(new Test("Name d", ""));
        model.addTest(new Test("Name c", "Test cc"));
        model.addTest(new Test("Name b", "Test bb"));
        model.addTest(new Test("Name a", ""));
        model.addTest(new Test("Name 0", "Test aa"));

        add(new JScrollPane(table));
        setPreferredSize(new Dimension(320, 180));
    }
    private static class HeaderRenderer implements TableCellRenderer {
        private final TableCellRenderer tcr;
        private final Font font;
        public HeaderRenderer(TableCellRenderer tcr, Font font) {
            this.tcr  = tcr;
            this.font = font;
        }
        @Override public Component getTableCellRendererComponent(JTable tbl, Object val, boolean isS,
                                                       boolean hasF, int row, int col) {
            JLabel l = (JLabel)tcr.getTableCellRendererComponent(tbl, val, isS, hasF, row, col);
            l.setFont(font);
            return l;
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
