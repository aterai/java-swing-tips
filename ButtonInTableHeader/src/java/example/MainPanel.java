package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

public final class MainPanel extends JPanel {
    private final Object[] columnNames = {"Boolean", "Integer", "String"};
    private final Object[][] data = {
        {true, 1, "BBB"}, {false, 12, "AAA"},
        {true, 2, "DDD"}, {false,  5, "CCC"},
        {true, 3, "EEE"}, {false,  6, "GGG"},
        {true, 4, "FFF"}, {false,  7, "HHH"}
    };
    private final DefaultTableModel model = new DefaultTableModel(data, columnNames) {
        @Override public Class<?> getColumnClass(int column) {
            return getValueAt(0, column).getClass();
        }
    };
    private final JTable table = new JTable(model) {
        @Override public void updateUI() {
            super.updateUI();
            //XXX: Nimbus
            TableCellRenderer r = getDefaultRenderer(Boolean.class);
            if (r instanceof JComponent) {
                ((JComponent) r).updateUI();
            }
        }
        @Override public Component prepareEditor(TableCellEditor editor, int row, int column) {
            Component c = super.prepareEditor(editor, row, column);
            if (c instanceof JCheckBox) {
                JCheckBox b = (JCheckBox) c;
                b.setBackground(getSelectionBackground());
                b.setBorderPainted(true);
            }
            return c;
        }
    };
    public MainPanel() {
        super(new BorderLayout());
        HeaderRenderer r = new HeaderRenderer(table.getTableHeader());
        table.getColumnModel().getColumn(0).setHeaderRenderer(r);
        table.getColumnModel().getColumn(1).setHeaderRenderer(r);
        table.getColumnModel().getColumn(2).setHeaderRenderer(r);

        //table.setAutoCreateRowSorter(true);
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

class HeaderRenderer extends JButton implements TableCellRenderer {
    private static final int BUTTON_WIDTH = 16;
    private static final Color BUTTONBGC = new Color(200, 200, 200, 100);
    private JPopupMenu pop;
    private int rolloverIndex = -1;
    private final transient MouseAdapter ma = new MouseAdapter() {
        @Override public void mouseClicked(MouseEvent e) {
            JTableHeader header = (JTableHeader) e.getComponent();
            JTable table = header.getTable();
            TableColumnModel columnModel = table.getColumnModel();
            int vci = columnModel.getColumnIndexAtX(e.getX());
            //int mci = table.convertColumnIndexToModel(vci);
            //TableColumn column = table.getColumnModel().getColumn(mci);
            //int w = column.getWidth(); //Nimbus???
            //int h = header.getHeight();
            Rectangle r = header.getHeaderRect(vci);
            Container c = (Container) getTableCellRendererComponent(table, "", true, true, -1, vci);
            //if (!isNimbus) {
            //  Insets i = c.getInsets();
            //  r.translate(r.width - i.right, 0);
            //} else {
            r.translate(r.width - BUTTON_WIDTH, 0);
            r.setSize(BUTTON_WIDTH, r.height);
            Point pt = e.getPoint();
            if (c.getComponentCount() > 0 && r.contains(pt) && pop != null) {
                pop.show(header, r.x, r.height);
                JButton b = (JButton) c.getComponent(0);
                b.doClick();
                e.consume();
            }
        }
        @Override public void mouseExited(MouseEvent e) {
            rolloverIndex = -1;
        }
        @Override public void mouseMoved(MouseEvent e) {
            JTableHeader header = (JTableHeader) e.getComponent();
            JTable table = header.getTable();
            TableColumnModel columnModel = table.getColumnModel();
            int vci = columnModel.getColumnIndexAtX(e.getX());
            int mci = table.convertColumnIndexToModel(vci);
            rolloverIndex = mci;
        }
    };

    public HeaderRenderer(JTableHeader header) {
        super();
        //setOpaque(false);
        //setFont(header.getFont());
        setBorder(BorderFactory.createEmptyBorder());
        setContentAreaFilled(false);
        pop.add("000");
        pop.add("11111");
        pop.add("2222222");
        header.addMouseListener(ma);
        header.addMouseMotionListener(ma);
    }

    @Override public void updateUI() {
        super.updateUI();
        if (pop == null) {
            pop = new JPopupMenu();
        } else {
            SwingUtilities.updateComponentTreeUI(pop);
        }
    }

//     JButton button = new JButton(new AbstractAction() {
//         @Override public void actionPerformed(ActionEvent e) {
//             System.out.println("clicked");
//         }
//     });
    @Override public Component getTableCellRendererComponent(JTable tbl, Object val, boolean isS, boolean hasF, int row, int col) {
        TableCellRenderer r = tbl.getTableHeader().getDefaultRenderer();
        JLabel l = (JLabel) r.getTableCellRendererComponent(tbl, val, isS, hasF, row, col);
        setIcon(new MenuArrowIcon());
        l.removeAll();
        int mci = tbl.convertColumnIndexToModel(col);

        if (rolloverIndex == mci) {
            TableColumn column = tbl.getColumnModel().getColumn(mci);
            int w = column.getWidth();
            int h = tbl.getTableHeader().getHeight();
            //Icon icon = new MenuArrowIcon();
            Border outside = l.getBorder();
            Border inside  = BorderFactory.createEmptyBorder(0, 0, 0, BUTTON_WIDTH);
            Border b = BorderFactory.createCompoundBorder(outside, inside);
            l.setBorder(b);
            l.add(this);
            //Insets i = b.getBorderInsets(l);
            //setBounds(w - i.right, 0, BUTTON_WIDTH, h - 2);
            setBounds(w - BUTTON_WIDTH, 0, BUTTON_WIDTH, h - 2);
            setBackground(BUTTONBGC);
            setOpaque(true);
            setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, Color.GRAY));
        }
//         if (l.getPreferredSize().height > 1000) { //XXX: Nimbus
//             System.out.println(l.getPreferredSize().height);
//             l.setPreferredSize(new Dimension(0, h));
//         }
        return l;
    }
}

class MenuArrowIcon implements Icon {
    @Override public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setPaint(Color.BLACK);
        g2.translate(x, y);
        g2.drawLine(2, 3, 6, 3);
        g2.drawLine(3, 4, 5, 4);
        g2.drawLine(4, 5, 4, 5);
        //g2.translate(-x, -y);
        g2.dispose();
    }
    @Override public int getIconWidth()  { return 10; }
    @Override public int getIconHeight() { return 10; }
}
