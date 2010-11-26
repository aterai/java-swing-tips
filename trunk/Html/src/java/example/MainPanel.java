package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;

public class MainPanel extends JPanel{
    private final JTable table;
    private final TestModel model = new TestModel();
    private final JTabbedPane tab = new JTabbedPane();

    public MainPanel() {
        super(new BorderLayout());
        tab.addTab("<html>Test<p>Test</p></html>", new JLabel("Test1"));
        tab.addTab("<html>Test<p>test", new JLabel("Test2"));

        table = new JTable(model);
        table.setRowSelectionAllowed(true);
        table.setRowHeight(32);
        table.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);

        //JTableHeader tableHeader = table.getTableHeader();
        //tableHeader.setReorderingAllowed(false);

        TableColumn col = table.getColumnModel().getColumn(0);
        col.setMinWidth(50);
        col.setMaxWidth(50);
        col.setResizable(false);

        model.addTest(new Test("Name-1", "<html>Comment<p>etc."));
        model.addTest(new Test("Name-2", "Test1"));
        model.addTest(new Test("Name-3", "Test2"));

        add(tab, BorderLayout.NORTH);
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
