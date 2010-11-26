package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import javax.swing.plaf.*;

class MainPanel extends JPanel {
    private final TestModel model = new TestModel();
    private final TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(model);
    private final JTable table = new JTable(model) {
        private final Color evenColor = new Color(245, 245, 245);
        @Override public Component prepareRenderer(TableCellRenderer tcr, int row, int column) {
            Component c = super.prepareRenderer(tcr, row, column);
            if(isRowSelected(row)) {
                c.setForeground(getSelectionForeground());
                c.setBackground(getSelectionBackground());
            }else{
                c.setForeground(getForeground());
                c.setBackground((row%2==0)?evenColor:getBackground());
            }
            return c;
        }
    };

    private static final Icon emptyIcon = new Icon() {
        @Override public void paintIcon(Component c, Graphics g, int x, int y) {}
        @Override public int getIconWidth()  { return 0; }
        @Override public int getIconHeight() { return 0; }
    };
    private final Icon defaultAscendingSortIcon  = UIManager.getIcon("Table.ascendingSortIcon");
    private final Icon defaultDescendingSortIcon = UIManager.getIcon("Table.descendingSortIcon");
    private final Icon customAscendingSortIcon   = new ImageIcon(getClass().getResource("ascending.png"));
    private final Icon customDescendingSortIcon  = new ImageIcon(getClass().getResource("descending.png"));

    private Box makeRadioPane() {
        final JRadioButton r0 = new JRadioButton("Default");
        final JRadioButton r1 = new JRadioButton("Empty");
        final JRadioButton r2 = new JRadioButton("Cumstom");
        ActionListener al = new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                JRadioButton r = (JRadioButton)e.getSource();
                Icon ascending  = null;
                Icon descending = null;
                if(r==r0) {
                    ascending  = defaultAscendingSortIcon;
                    descending = defaultDescendingSortIcon;
                }else if(r==r1) {
                    ascending  = new IconUIResource(emptyIcon);
                    descending = new IconUIResource(emptyIcon);
                }else{
                    ascending  = new IconUIResource(customAscendingSortIcon);
                    descending = new IconUIResource(customDescendingSortIcon);
                }
                UIManager.put("Table.ascendingSortIcon",  ascending);
                UIManager.put("Table.descendingSortIcon", descending);
                table.getTableHeader().repaint();
            }
        };
        Box box1 = Box.createHorizontalBox();
        box1.setBorder(BorderFactory.createEmptyBorder(0,5,0,5));
        ButtonGroup bg = new ButtonGroup();
        box1.add(new JLabel("Table Sort Icon: "));
        box1.add(r0); bg.add(r0); r0.addActionListener(al);
        box1.add(r1); bg.add(r1); r1.addActionListener(al);
        box1.add(r2); bg.add(r2); r2.addActionListener(al);
        box1.add(Box.createHorizontalGlue());
        r0.setSelected(true);
        return box1;
    }

    public MainPanel() {
        super(new BorderLayout());
        table.setRowSorter(sorter);
        TableColumn col = table.getColumnModel().getColumn(0);
        col.setMinWidth(60);
        col.setMaxWidth(60);
        col.setResizable(false);
        model.addTest(new Test("Name 1", "comment..."));
        model.addTest(new Test("Name 2", "Test"));
        model.addTest(new Test("Name d", ""));
        model.addTest(new Test("Name c", "Test cc"));
        model.addTest(new Test("Name b", "Test bb"));
        model.addTest(new Test("Name a", ""));
        model.addTest(new Test("Name 0", "Test aa"));

//         JCheckBox check = new JCheckBox(new AbstractAction("paintSortIcon") {
//             @Override
//             public void actionPerformed(ActionEvent e) {
//                 JCheckBox cb = (JCheckBox)e.getSource();
//                 if(cb.isSelected()) {
//                     UIManager.put("Table.ascendingSortIcon",  defaultAscendingSortIcon);
//                     UIManager.put("Table.descendingSortIcon", defaultDescendingSortIcon);
//                 }else{
//                     UIManager.put("Table.ascendingSortIcon",  new IconUIResource(emptyIcon));
//                     UIManager.put("Table.descendingSortIcon", new IconUIResource(emptyIcon));
//                 }
//                 table.getTableHeader().repaint();
//             }
//         });
//         check.setSelected(true);
        add(makeRadioPane(), BorderLayout.NORTH);
        add(new JButton(new AbstractAction("clear SortKeys") {
            @Override public void actionPerformed(ActionEvent e) {
                sorter.setSortKeys(null);
            }
        }), BorderLayout.SOUTH);
        add(new JScrollPane(table));
        setPreferredSize(new Dimension(320, 200));
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
