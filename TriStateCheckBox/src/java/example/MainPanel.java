package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

public final class MainPanel extends JPanel {
    private final JCheckBox checkBox = new TriStateCheckBox("TriState JCheckBox");
    private final Object[] columnNames = {Status.INDETERMINATE, "Integer", "String"};
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
            if (r instanceof Component) {
                SwingUtilities.updateComponentTreeUI((Component) r);
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

        int modelColmunIndex = 0;
        TableCellRenderer renderer = new HeaderRenderer(table.getTableHeader(), modelColmunIndex);
        TableColumn column = table.getColumnModel().getColumn(modelColmunIndex);
        column.setHeaderRenderer(renderer);
        column.setHeaderValue(Status.INDETERMINATE);
        //column.setResizable(false);
        //column.setMaxWidth(32);
        model.addTableModelListener(new HeaderCheckBoxHandler(table, modelColmunIndex));

        JPanel p = new JPanel();
        p.add(checkBox);

        JTabbedPane tp = new JTabbedPane();
        tp.addTab("JCheckBox", p);
        tp.addTab("JTableHeader", new JScrollPane(table));

        add(tp);
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
        } catch (ClassNotFoundException | InstantiationException
               | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        }
        JMenuBar mb = new JMenuBar();
        mb.add(LookAndFeelUtil.createLookAndFeelMenu());

        JFrame frame = new JFrame("@title@");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.setJMenuBar(mb);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

class TriStateActionListener implements ActionListener {
    protected Icon icon;
    public void setIcon(Icon icon) {
        this.icon = icon;
    }
    @Override public void actionPerformed(ActionEvent e) {
        JCheckBox cb = (JCheckBox) e.getSource();
        if (cb.isSelected()) {
            if (cb.getIcon() != null) {
                cb.setIcon(null);
                cb.setSelected(false);
            }
        } else {
            cb.setIcon(icon);
        }
    }
}

class TriStateCheckBox extends JCheckBox {
    protected transient TriStateActionListener listener;
    public TriStateCheckBox(String title) {
        super(title);
    }
    @Override public void updateUI() {
        final Icon oi = getIcon();
        removeActionListener(listener);
        setIcon(null);
        super.updateUI();
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                if (listener == null) {
                    listener = new TriStateActionListener();
                }
                Icon icon = new IndeterminateIcon();
                listener.setIcon(icon);
                addActionListener(listener);
                if (oi != null) {
                    setIcon(icon);
                }
            }
        });
    }
}

class HeaderRenderer extends JCheckBox implements TableCellRenderer {
    private final JLabel label = new JLabel("Check All");
    private final int targetColumnIndex;
    private transient Icon icon;
    public HeaderRenderer(JTableHeader header, int index) {
        super((String) null);
        this.targetColumnIndex = index;
        setOpaque(false);
        setFont(header.getFont());
        header.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                JTableHeader header = (JTableHeader) e.getComponent();
                JTable table = header.getTable();
                TableColumnModel columnModel = table.getColumnModel();
                int vci = columnModel.getColumnIndexAtX(e.getX());
                int mci = table.convertColumnIndexToModel(vci);
                if (mci == targetColumnIndex) {
                    TableColumn column = columnModel.getColumn(vci);
                    Object v = column.getHeaderValue();
                    boolean b = Status.DESELECTED.equals(v);
                    TableModel m = table.getModel();
                    for (int i = 0; i < m.getRowCount(); i++) {
                        m.setValueAt(b, i, mci);
                    }
                    column.setHeaderValue(b ? Status.SELECTED : Status.DESELECTED);
                    //header.repaint();
                }
            }
        });
    }
    @Override public Component getTableCellRendererComponent(JTable tbl, Object val, boolean isS, boolean hasF, int row, int col) {
        TableCellRenderer r = tbl.getTableHeader().getDefaultRenderer();
        JLabel l = (JLabel) r.getTableCellRendererComponent(tbl, val, isS, hasF, row, col);
        if (targetColumnIndex == tbl.convertColumnIndexToModel(col)) {
            if (val instanceof Status) {
                switch ((Status) val) {
                  case SELECTED:      setSelected(true);  setIcon(null); break;
                  case DESELECTED:    setSelected(false); setIcon(null); break;
                  case INDETERMINATE: setSelected(false); setIcon(icon); break;
                  default:            throw new AssertionError("Unknown Status");
                }
            } else {
                setSelected(true);
            }
            label.setIcon(new ComponentIcon(this));
            l.setIcon(new ComponentIcon(label));
            l.setText(null); //XXX: Nimbus???
            l.setHorizontalAlignment(SwingConstants.CENTER);
        //} else {
        //    l.setHorizontalAlignment(SwingConstants.LEFT);
        }

//         System.out.println("getHeaderRect: " + tbl.getTableHeader().getHeaderRect(col));
//         System.out.println("getPreferredSize: " + l.getPreferredSize());
//         System.out.println("getMaximunSize: " + l.getMaximumSize());
//         System.out.println("----");
//         if (l.getPreferredSize().height > 1000) { //XXX: Nimbus???
//             System.out.println(l.getPreferredSize().height);
//             Rectangle rect = tbl.getTableHeader().getHeaderRect(col);
//             l.setPreferredSize(new Dimension(0, rect.height));
//         }
        return l;
    }
    @Override public void updateUI() {
        final Icon oi = getIcon();
        super.updateUI();
        icon = new IndeterminateIcon();
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                if (oi != null) {
                    setIcon(icon);
                }
            }
        });
    }
}

class IndeterminateIcon implements Icon {
    private static final Color FOREGROUND = Color.BLACK; //TEST: UIManager.getColor("CheckBox.foreground");
    private static final int SIDE_MARGIN = 4;
    private static final int HEIGHT = 2;
    private final Icon icon = UIManager.getIcon("CheckBox.icon");
    @Override public void paintIcon(Component c, Graphics g, int x, int y) {
        icon.paintIcon(c, g, x, y);
        int w = getIconWidth();
        int h = getIconHeight();
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setPaint(FOREGROUND);
        g2.translate(x, y);
        g2.fillRect(SIDE_MARGIN, (h - HEIGHT) / 2, w - SIDE_MARGIN - SIDE_MARGIN, HEIGHT);
        g2.translate(-x, -y);
        g2.dispose();
    }
    @Override public int getIconWidth() {
        return icon.getIconWidth();
    }
    @Override public int getIconHeight() {
        return icon.getIconHeight();
    }
}

class HeaderCheckBoxHandler implements TableModelListener {
    private final JTable table;
    private final int targetColumnIndex;
    public HeaderCheckBoxHandler(JTable table, int index) {
        this.table = table;
        this.targetColumnIndex = index;
    }
    @Override public void tableChanged(TableModelEvent e) {
        if (e.getType() == TableModelEvent.UPDATE && e.getColumn() == targetColumnIndex) {
            int vci = table.convertColumnIndexToView(targetColumnIndex);
            TableColumn column = table.getColumnModel().getColumn(vci);
            if (Status.INDETERMINATE.equals(column.getHeaderValue())) {
                boolean selected = true;
                boolean deselected = true;
                TableModel m = table.getModel();
                for (int i = 0; i < m.getRowCount(); i++) {
                    Boolean b = (Boolean) m.getValueAt(i, targetColumnIndex);
                    selected &= b;
                    deselected &= !b;
                    if (selected == deselected) {
                        return;
                    }
                }
                if (selected) {
                    column.setHeaderValue(Status.SELECTED);
                } else if (deselected) {
                    column.setHeaderValue(Status.DESELECTED);
                } else {
                    return;
                }
            } else {
                column.setHeaderValue(Status.INDETERMINATE);
            }
            JTableHeader h = table.getTableHeader();
            h.repaint(h.getHeaderRect(vci));
        }
    }
}

class ComponentIcon implements Icon {
    private final JComponent cmp;
    public ComponentIcon(JComponent cmp) {
        this.cmp = cmp;
    }
    @Override public int getIconWidth() {
        return cmp.getPreferredSize().width;
    }
    @Override public int getIconHeight() {
        return cmp.getPreferredSize().height;
    }
    @Override public void paintIcon(Component c, Graphics g, int x, int y) {
        SwingUtilities.paintComponent(g, cmp, c.getParent(), x, y, getIconWidth(), getIconHeight());
    }
}

enum Status { SELECTED, DESELECTED, INDETERMINATE }

//http://java.net/projects/swingset3/sources/svn/content/trunk/SwingSet3/src/com/sun/swingset3/SwingSet3.java
final class LookAndFeelUtil {
    private static String lookAndFeel = UIManager.getLookAndFeel().getClass().getName();
    private LookAndFeelUtil() { /* Singleton */ }
    public static JMenu createLookAndFeelMenu() {
        JMenu menu = new JMenu("LookAndFeel");
        ButtonGroup lookAndFeelRadioGroup = new ButtonGroup();
        for (UIManager.LookAndFeelInfo lafInfo: UIManager.getInstalledLookAndFeels()) {
            menu.add(createLookAndFeelItem(lafInfo.getName(), lafInfo.getClassName(), lookAndFeelRadioGroup));
        }
        return menu;
    }
    private static JRadioButtonMenuItem createLookAndFeelItem(String lafName, String lafClassName, final ButtonGroup lookAndFeelRadioGroup) {
        JRadioButtonMenuItem lafItem = new JRadioButtonMenuItem();
        lafItem.setSelected(lafClassName.equals(lookAndFeel));
        lafItem.setHideActionText(true);
        lafItem.setAction(new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                ButtonModel m = lookAndFeelRadioGroup.getSelection();
                try {
                    setLookAndFeel(m.getActionCommand());
                } catch (ClassNotFoundException | InstantiationException
                       | IllegalAccessException | UnsupportedLookAndFeelException ex) {
                    ex.printStackTrace();
                }
            }
        });
        lafItem.setText(lafName);
        lafItem.setActionCommand(lafClassName);
        lookAndFeelRadioGroup.add(lafItem);
        return lafItem;
    }
    private static void setLookAndFeel(String lookAndFeel) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
        String oldLookAndFeel = LookAndFeelUtil.lookAndFeel;
        if (!oldLookAndFeel.equals(lookAndFeel)) {
            UIManager.setLookAndFeel(lookAndFeel);
            LookAndFeelUtil.lookAndFeel = lookAndFeel;
            updateLookAndFeel();
            //firePropertyChange("lookAndFeel", oldLookAndFeel, lookAndFeel);
        }
    }
    private static void updateLookAndFeel() {
        for (Window window: Frame.getWindows()) {
            SwingUtilities.updateComponentTreeUI(window);
        }
    }
}
