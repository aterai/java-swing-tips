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
    private final TestModel model = new TestModel();
    private final JTable table = new JTable(model);
//     {
//         @Override
//         public Component prepareRenderer(TableCellRenderer tcr, int row, int column) {
//             Component c = super.prepareRenderer(tcr, row, column);
//             if(1==convertColumnIndexToModel(column)) {
//                 initLabel((JLabel)c, row);
//             }else{
//                 ((JLabel)c).setHorizontalAlignment(JLabel.RIGHT);
//             }
//             return c;
//         }
//     };

    private final JRadioButton leftRadio   = new JRadioButton("left", true);
    private final JRadioButton centerRadio = new JRadioButton("center");
    private final JRadioButton rightRadio  = new JRadioButton("right");
    private final JRadioButton customRadio = new JRadioButton("custom");

    public MainPanel() {
        super(new BorderLayout());

        TableColumn col = table.getColumnModel().getColumn(0);
        col.setMinWidth(60);
        col.setMaxWidth(60);
        col.setResizable(false);

        col = table.getColumnModel().getColumn(1);
        col.setCellRenderer(new HorizontalAlignmentTableRenderer(new DefaultTableCellRenderer()));
//         TableCellRenderer cr = table.getDefaultRenderer(model.getColumnClass(1));
//         col.setCellRenderer(new HorizontalAlignmentTableRenderer(cr));

        col = table.getColumnModel().getColumn(2);
        TableCellRenderer hr = table.getTableHeader().getDefaultRenderer();
        col.setHeaderRenderer(new HeaderRenderer(hr));
        //col.setHeaderRenderer(new HeaderRenderer(new DefaultTableCellRenderer()));

        model.addTest(new Test("Name 1", "comment..."));
        model.addTest(new Test("Name 2", "Test"));
        model.addTest(new Test("Name d", ""));
        model.addTest(new Test("Name c", "Test cc"));
        model.addTest(new Test("Name b", "Test bb"));
        model.addTest(new Test("Name a", ""));
        model.addTest(new Test("Name 0", "Test aa"));

        ActionListener al = new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                table.repaint();
            }
        };
        ButtonGroup bg = new ButtonGroup();
        JPanel p = new JPanel();
        for(JRadioButton r:java.util.Arrays.asList(
              leftRadio,centerRadio,rightRadio,customRadio)) {
            bg.add(r); p.add(r); r.addActionListener(al);
        }

        add(p, BorderLayout.NORTH);
        add(new JScrollPane(table));
        setPreferredSize(new Dimension(320, 240));
    }
    static class HeaderRenderer implements TableCellRenderer {
        private final TableCellRenderer renderer;
        private final Font font = new Font("Sans-serif", Font.BOLD, 14);
        public HeaderRenderer(TableCellRenderer renderer) {
            this.renderer  = renderer;
        }
        @Override public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel l = (JLabel)renderer.getTableCellRendererComponent(table, value,
                                                                 isSelected, hasFocus, row, column);
            l.setHorizontalAlignment(JLabel.CENTER);
            l.setFont(font);
            return l;
        }
    }
    class HorizontalAlignmentTableRenderer implements TableCellRenderer {
        private final TableCellRenderer renderer;
        public HorizontalAlignmentTableRenderer(TableCellRenderer renderer) {
            this.renderer = renderer;
        }
        @Override public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = renderer.getTableCellRendererComponent(table, value,
                                                                 isSelected, hasFocus, row, column);
            if(c instanceof JLabel) {
                initLabel((JLabel)c, row);
            }
            return c;
        }
        private void initLabel(JLabel l, int row) {
            if(leftRadio.isSelected()) {
                l.setHorizontalAlignment(JLabel.LEFT);
            }else if(centerRadio.isSelected()) {
                l.setHorizontalAlignment(JLabel.CENTER);
            }else if(rightRadio.isSelected()) {
                l.setHorizontalAlignment(JLabel.RIGHT);
            }else if(customRadio.isSelected()) {
                l.setHorizontalAlignment(row%3==0?JLabel.LEFT:
                                         row%3==1?JLabel.CENTER:
                                         JLabel.RIGHT);
            }
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
//             for(UIManager.LookAndFeelInfo laf: UIManager.getInstalledLookAndFeels()) {
//                 //if("Metal".equals(laf.getName()))
//                 //if("Motif".equals(laf.getName()))
//                 if("Nimbus".equals(laf.getName())) {
//                     UIManager.setLookAndFeel(laf.getClassName());
//                 }
//             }
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
