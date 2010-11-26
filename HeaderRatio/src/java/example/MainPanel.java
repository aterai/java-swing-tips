package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;

public class MainPanel extends JPanel {
    private final JTextField field = new JTextField("1:2:5");
    private final JTable table;
    public MainPanel() {
        super(new BorderLayout(5,5));
        TestModel model = new TestModel();
        model.addTest(new Test("Name 1", "comment..."));
        model.addTest(new Test("Name 2", "Test"));
        model.addTest(new Test("Name d", ""));
        model.addTest(new Test("Name c", "Test cc"));
        model.addTest(new Test("Name b", "Test bb"));
        model.addTest(new Test("Name a", ""));
        model.addTest(new Test("Name 0", "Test aa"));

        table = new JTable(model);

        JPanel p = new JPanel(new BorderLayout(5,5));
        p.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        p.add(new JLabel("Header Ratio:"), BorderLayout.WEST);
        p.add(field);
        p.add(new JButton(new AbstractAction("init") {
            @Override public void actionPerformed(ActionEvent ae) {
                setRaito();
            }
        }), BorderLayout.EAST);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) {
                setRaito();
            }
        });
        add(p, BorderLayout.NORTH);
        add(scrollPane);
        setPreferredSize(new Dimension(320, 200));
        setRaito();
    }
    private void setRaito() {
        int[] list = getWidthRaitoArray();
        int total = getTotalColumnWidth(table);
        int raito = total/getRaitoTotal(table, list);
        for(int i=0;i<table.getColumnModel().getColumnCount()-1;i++) {
            TableColumn col = table.getColumnModel().getColumn(i);
            int colwidth = list[i]*raito;
            col.setMaxWidth(colwidth);
            total = total - colwidth;
        }
        table.getColumnModel().getColumn(table.getColumnModel().getColumnCount()-1).setMaxWidth(total);
        table.revalidate();
    }
    private int getRaitoTotal(JTable table, int[] list) {
        int wr = 0;
        for(int i=0;i<list.length;i++) {
            wr = wr + list[i];
        }
        return wr;
    }
    private int getTotalColumnWidth(JTable table) {
        int tablewidth = table.getBounds(null).width;
        if(tablewidth==0) tablewidth = 512;
        return tablewidth;
    }

    private int[] getWidthRaitoArray() {
        StringTokenizer st = new StringTokenizer(field.getText(), ":");
        int[] list = {1,1,1};
        try{
            int i = 0;
            while(st.hasMoreTokens() && i<list.length) {
                list[i] = (new Integer(st.nextToken().trim())).intValue();
                i++;
            }
        }catch(final NumberFormatException nfe) {
            java.awt.Toolkit.getDefaultToolkit().beep();
            JOptionPane.showMessageDialog(field, "invalid value.\n"+nfe.getMessage(),
                                          "Error", JOptionPane.ERROR_MESSAGE);
        }
        return list;
    }

//     private void setRaito_xxx() {
//         int[] list = getWidthRaitoArray();
//         int total = table.getColumnModel().getTotalColumnWidth();;
//         int raito = total/getRaitoTotal(table, list);
// //                 System.out.println("---------------------------");
// //                 for(int i=0;i<table.getColumnModel().getColumnCount();i++) {
// //                     TableColumn col = table.getColumnModel().getColumn(i);
// //                     int colwidth = col.getWidth();
// //                     System.out.println(""+i+": "+colwidth);
// //                 }
// //                 System.out.println("---------");
//         for(int i=0;i<table.getColumnModel().getColumnCount()-1;i++) {
//             TableColumn col = table.getColumnModel().getColumn(i);
//             int colwidth = list[i]*raito; // - table.getColumnModel().getColumnMargin();
//             col.setMinWidth(0);
//             col.setMaxWidth(Integer.MAX_VALUE);
//             col.setPreferredWidth(colwidth);
//             total = total - colwidth;
//             //    System.out.println(""+i+": "+colwidth);
//         }
//         System.out.println("2: "+(int)(list[2]*raito));
//         System.out.println("l: "+total);
//
//         table.getColumnModel().getColumn(table.getColumnModel().getColumnCount()-1).setPreferredWidth(total);
//         table.getColumnModel().getColumn(table.getColumnModel().getColumnCount()-1).setMinWidth(0);
//         table.getColumnModel().getColumn(table.getColumnModel().getColumnCount()-1).setMaxWidth(Integer.MAX_VALUE);
//
//         table.revalidate();
//     }

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
