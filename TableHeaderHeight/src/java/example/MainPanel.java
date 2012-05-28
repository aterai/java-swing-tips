package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.Arrays;
import javax.swing.*;
import javax.swing.table.*;

public class MainPanel extends JPanel {
    private static int HEADER_HEIGHT = 32;
    private JTable makeTable() {
        JTable table = new JTable(new DefaultTableModel(2, 20));
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        return table;
    }
    public MainPanel() {
        super(new BorderLayout());
        JPanel p = new JPanel(new GridLayout(2,1));

        JTable table1 = makeTable();
        //Bad: >>>>
        JTableHeader header = table1.getTableHeader();
        //Dimension d = header.getPreferredSize();
        //d.height = HEADER_HEIGHT;
        //header.setPreferredSize(d); //addColumn case test
        header.setPreferredSize(new Dimension(100, HEADER_HEIGHT));
        p.add(makeTitledPanel("Bad: JTableHeader#setPreferredSize(...)", new JScrollPane(table1)));
        //<<<<

        JTable table2 = makeTable();
        JScrollPane scroll = new JScrollPane(table2);
        scroll.setColumnHeader(new JViewport() {
            @Override public Dimension getPreferredSize() {
                Dimension d = super.getPreferredSize();
                d.height = HEADER_HEIGHT;
                return d;
            }
        });
//         //or
//         table2.setTableHeader(new JTableHeader(table2.getColumnModel()) {
//             @Override public Dimension getPreferredSize() {
//                 Dimension d = super.getPreferredSize();
//                 d.height = HEADER_HEIGHT;
//                 return d;
//             }
//         });
        p.add(makeTitledPanel("Override getPreferredSize()", scroll));

        final List<JTable> list = Arrays.asList(table1, table2);
        add(p);
        add(new JButton(new AbstractAction("addColumn") {
            @Override public void actionPerformed(ActionEvent e) {
                for(JTable t: list) {
                    t.getColumnModel().addColumn(new TableColumn());
                    JTableHeader h = t.getTableHeader();
                    Dimension d = h.getPreferredSize();
                    System.out.println(d);
                }
            }
        }), BorderLayout.SOUTH);
        setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        setPreferredSize(new Dimension(320, 240));
    }
    private static JComponent makeTitledPanel(String title, JComponent c) {
        JPanel p = new JPanel(new BorderLayout());
        p.add(c);
        p.setBorder(BorderFactory.createTitledBorder(title));
        return p;
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
