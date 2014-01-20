package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.plaf.*;

class MainPanel extends JPanel {
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
    private final TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(model);

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
        for(JRadioButton rb: Arrays.asList(r0, r1, r2)) {
            box1.add(rb);
            box1.add(Box.createHorizontalStrut(5));
            bg.add(rb);
            rb.addActionListener(al);
        }
        box1.add(Box.createHorizontalGlue());
        r0.setSelected(true);
        return box1;
    }

    public MainPanel() {
        super(new BorderLayout());
        table.setRowSorter(sorter);
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
        }catch(ClassNotFoundException | InstantiationException |
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
