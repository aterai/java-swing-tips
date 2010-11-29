package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;

public class MainPanel extends JPanel {
    public MainPanel() {
        super(new BorderLayout());

        JTable table0 = makeTable();
        ((JLabel)table0.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(SwingConstants.CENTER);

        JTable table1 = makeTable();
        table1.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table,value,isSelected,hasFocus,row,column);
                setHorizontalAlignment(SwingConstants.CENTER);
                return this;
            }
        });

        JTable table2 = makeTable();
        table2.getColumnModel().getColumn(0).setHeaderRenderer(new HorizontalAlignmentHeaderRenderer(SwingConstants.LEFT));
        table2.getColumnModel().getColumn(1).setHeaderRenderer(new HorizontalAlignmentHeaderRenderer(SwingConstants.CENTER));
        table2.getColumnModel().getColumn(2).setHeaderRenderer(new HorizontalAlignmentHeaderRenderer(SwingConstants.RIGHT));

//         final TableCellRenderer r = table.getTableHeader().getDefaultRenderer();
//         table.getTableHeader().setDefaultRenderer(new TableCellRenderer() {
//             public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
//                 JLabel l = (JLabel)r.getTableCellRendererComponent(table,value,isSelected,hasFocus,row,column);
//                 l.setHorizontalAlignment(SwingConstants.CENTER);
//                 return l;
//             }
//         });

        JTabbedPane tabs = new JTabbedPane();
        tabs.add("Default", new JScrollPane(makeTable()));
        tabs.add("Test1", new JScrollPane(table0));
        tabs.add("Test2", new JScrollPane(table1));
        tabs.add("Test3", new JScrollPane(table2));

        JMenuBar mb = new JMenuBar();
        mb.add(createLookAndFeelMenu());
        add(mb, BorderLayout.NORTH);
        add(tabs);
        setPreferredSize(new Dimension(320, 240));
    }
    public static JTable makeTable() {
        String[] columnNames = {"String", "Integer", "Boolean"};
        Object[][] data = {
            {"aaa", 12, true}, {"bbb", 5, false},
            {"CCC", 92, true}, {"DDD", 0, false}
        };
        DefaultTableModel model = new DefaultTableModel(data, columnNames) {
            @Override public Class<?> getColumnClass(int column) {
                return getValueAt(0, column).getClass();
            }
        };
        JTable table = new JTable(model);
        table.setAutoCreateRowSorter(true);
        return table;
    }

    //<blockquote cite="https://swingset3.dev.java.net/svn/swingset3/trunk/SwingSet3/src/com/sun/swingset3/SwingSet3.java">
    private ButtonGroup lookAndFeelRadioGroup;
    private String lookAndFeel;
    protected JMenu createLookAndFeelMenu() {
        JMenu menu = new JMenu("LookAndFeel");
        lookAndFeel = UIManager.getLookAndFeel().getClass().getName();
        lookAndFeelRadioGroup = new ButtonGroup();
        for(UIManager.LookAndFeelInfo lafInfo: UIManager.getInstalledLookAndFeels()) {
            menu.add(createLookAndFeelItem(lafInfo.getName(), lafInfo.getClassName()));
        }
        return menu;
    }
    protected JRadioButtonMenuItem createLookAndFeelItem(String lafName, String lafClassName) {
        JRadioButtonMenuItem lafItem = new JRadioButtonMenuItem();
        lafItem.setSelected(lafClassName.equals(lookAndFeel));
        lafItem.setHideActionText(true);
        lafItem.setAction(new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                ButtonModel m = lookAndFeelRadioGroup.getSelection();
                try{
                    setLookAndFeel(m.getActionCommand());
                }catch(Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        lafItem.setText(lafName);
        lafItem.setActionCommand(lafClassName);
        lookAndFeelRadioGroup.add(lafItem);
        return lafItem;
    }
    public void setLookAndFeel(String lookAndFeel) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
        String oldLookAndFeel = this.lookAndFeel;
        if(!oldLookAndFeel.equals(lookAndFeel)) {
            UIManager.setLookAndFeel(lookAndFeel);
            this.lookAndFeel = lookAndFeel;
            updateLookAndFeel();
            firePropertyChange("lookAndFeel", oldLookAndFeel, lookAndFeel);
        }
    }
    private void updateLookAndFeel() {
        for(Window window: Frame.getWindows()) {
            SwingUtilities.updateComponentTreeUI(window);
        }
    }
    //</blockquote>

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

class HorizontalAlignmentHeaderRenderer implements TableCellRenderer{
    private int horizontalAlignment = SwingConstants.LEFT;
    public HorizontalAlignmentHeaderRenderer(int horizontalAlignment) {
        this.horizontalAlignment = horizontalAlignment;
    }
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        TableCellRenderer r = table.getTableHeader().getDefaultRenderer();
        JLabel l = (JLabel)r.getTableCellRendererComponent(table,value,isSelected,hasFocus,row,column);
        l.setHorizontalAlignment(horizontalAlignment);
        return l;
    }
}
