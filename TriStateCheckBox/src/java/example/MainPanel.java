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
    private final JCheckBox checkBox = new JCheckBox("TriState JCheckBox") {
        protected TriStateActionListener listener = null;
        class TriStateActionListener implements ActionListener{
            protected Icon icon;
            public void setIcon(Icon icon) {
                this.icon = icon;
            }
            @Override public void actionPerformed(ActionEvent e) {
                JCheckBox cb = (JCheckBox)e.getSource();
                if(!cb.isSelected()) {
                    cb.setIcon(icon);
                }else if(cb.getIcon()!=null){
                    cb.setIcon(null);
                    cb.setSelected(false);
                }
            }
        }
        @Override public void updateUI() {
            final Icon oi = getIcon();
            removeActionListener(listener);
            setIcon(null);
            super.updateUI();
            EventQueue.invokeLater(new Runnable() {
                @Override public void run() {
                    if(listener==null) listener = new TriStateActionListener();
                    Icon icon = new IndeterminateIcon();
                    listener.setIcon(icon);
                    addActionListener(listener);
                    if(oi!=null) {
                        setIcon(icon);
                    }
                }
            });
        }
    };
    private static JTable makeTable() {
        Object[] columnNames = {Status.INDETERMINATE, "Integer", "String"};
        Object[][] data = {{true, 1, "BBB"}, {false, 12, "AAA"},
            {true, 2, "DDD"}, {false, 5, "CCC"},
            {true, 3, "EEE"}, {false, 6, "GGG"},
            {true, 4, "FFF"}, {false, 7, "HHH"}};
        DefaultTableModel model = new DefaultTableModel(data, columnNames) {
            @Override public Class<?> getColumnClass(int column) {
                return getValueAt(0, column).getClass();
            }
        };
        final JTable table = new JTable(model) {
            @Override public void updateUI() {
                super.updateUI();
                //XXX: Nimbus
                TableCellRenderer r = getDefaultRenderer(Boolean.class);
                if(r instanceof JComponent) {
                    ((JComponent)r).updateUI();
                }
            }
            @Override public Component prepareEditor(TableCellEditor editor, int row, int column) {
                Component c = super.prepareEditor(editor, row, column);
                if(c instanceof JCheckBox) {
                    JCheckBox b = (JCheckBox)c;
                    b.setBackground(getSelectionBackground());
                    b.setBorderPainted(true);
                }
                return c;
            }
        };
        TableColumn column = table.getColumnModel().getColumn(0);
        column.setHeaderRenderer(new HeaderRenderer(table.getTableHeader(), 0));
        column.setHeaderValue(Status.INDETERMINATE);
        column.setResizable(false);
        column.setMaxWidth(32);

        model.addTableModelListener(new TableModelListener() {
            @Override public void tableChanged(TableModelEvent e) {
                if(e.getType()==TableModelEvent.UPDATE && e.getColumn()==0) {
                    int mci = 0;
                    int vci = table.convertColumnIndexToView(mci);
                    TableColumn column = table.getColumnModel().getColumn(vci);
                    Object title = column.getHeaderValue();
                    if(!Status.INDETERMINATE.equals(title)) {
                        column.setHeaderValue(Status.INDETERMINATE);
                    }else{
                        int selected = 0, deselected = 0;
                        TableModel m = table.getModel();
                        for(int i=0; i<m.getRowCount(); i++) {
                            if(Boolean.TRUE.equals(m.getValueAt(i, mci))) {
                                selected++;
                            }else{
                                deselected++;
                            }
                        }
                        if(selected==0) {
                            column.setHeaderValue(Status.DESELECTED);
                        }else if(deselected==0) {
                            column.setHeaderValue(Status.SELECTED);
                        }else{
                            return;
                        }
                    }
                    table.getTableHeader().repaint();
                }
            }
        });
        return table;
    }

    public MainPanel() {
        super(new BorderLayout());
        JPanel p = new JPanel();
        p.add(checkBox);

        JTabbedPane tp = new JTabbedPane();
        tp.addTab("JCheckBox", p);
        tp.addTab("JTableHeader", new JScrollPane(makeTable()));

        JMenuBar mb = new JMenuBar();
        mb.add(createLookAndFeelMenu());

        add(mb, BorderLayout.NORTH);
        add(tp);
        setPreferredSize(new Dimension(320, 240));
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
class HeaderRenderer extends JCheckBox implements TableCellRenderer {
    public HeaderRenderer(JTableHeader header, final int targetColumnIndex) {
        super();
        setOpaque(false);
        setFont(header.getFont());
        header.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                JTableHeader header = (JTableHeader)e.getSource();
                JTable table = header.getTable();
                TableColumnModel columnModel = table.getColumnModel();
                int vci = columnModel.getColumnIndexAtX(e.getX());
                int mci = table.convertColumnIndexToModel(vci);
                if(mci == targetColumnIndex) {
                    TableColumn column = columnModel.getColumn(vci);
                    Object v = column.getHeaderValue();
                    boolean b = Status.DESELECTED.equals(v)?true:false;
                    TableModel m = table.getModel();
                    for(int i=0; i<m.getRowCount(); i++) m.setValueAt(b, i, mci);
                    column.setHeaderValue(b?Status.SELECTED:Status.DESELECTED);
                    header.repaint();
                }
            }
        });
    }
    @Override public Component getTableCellRendererComponent(JTable tbl, Object val, boolean isS, boolean hasF, int row, int col) {
        TableCellRenderer r = tbl.getTableHeader().getDefaultRenderer();
        JLabel l =(JLabel)r.getTableCellRendererComponent(tbl, "", isS, hasF, row, col);
        l.setHorizontalAlignment(SwingConstants.CENTER);
        if(val instanceof Status) {
            switch((Status)val) {
              case SELECTED:      setSelected(true);  setIcon(null); break;
              case DESELECTED:    setSelected(false); setIcon(null); break;
              case INDETERMINATE: setSelected(false); setIcon(icon); break;
            }
        }else{
            setSelected(true);
        }
        l.setIcon(new CheckBoxIcon(this));
//         if(l.getPreferredSize().height>1000) { //XXX: Nimbus
//             System.out.println(l.getPreferredSize().height);
//             Rectangle rect = tbl.getTableHeader().getHeaderRect(col);
//             l.setPreferredSize(new Dimension(0, rect.height));
//         }
        return l;
    }
    private Icon icon;
    @Override public void updateUI() {
        final Icon oi = getIcon();
        setText(null); //XXX: Nimbus?
        //setIcon(null);
        super.updateUI();
        icon = new IndeterminateIcon();
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                if(oi!=null) {
                    setIcon(icon);
                }
            }
        });
    }
}

enum Status { SELECTED, DESELECTED, INDETERMINATE }

class CheckBoxIcon implements Icon{
    private final JCheckBox check;
    public CheckBoxIcon(JCheckBox check) {
        this.check = check;
    }
    @Override public int getIconWidth() {
        return check.getPreferredSize().width;
    }
    @Override public int getIconHeight() {
        return check.getPreferredSize().height;
    }
    @Override public void paintIcon(Component c, Graphics g, int x, int y) {
        SwingUtilities.paintComponent(g, check, (Container)c, x, y, getIconWidth(), getIconHeight());
    }
}

class IndeterminateIcon implements Icon {
    private final Color FOREGROUND = Color.BLACK; //TEST: UIManager.getColor("CheckBox.foreground");
    private final Icon icon = UIManager.getIcon("CheckBox.icon");
    @Override public void paintIcon(Component c, Graphics g, int x, int y) {
        icon.paintIcon(c, g, x, y);
        int w = getIconWidth(), h = getIconHeight();
        int a = 4, b = 2;
        Graphics2D g2 = (Graphics2D)g;
        g2.setPaint(FOREGROUND);
        g2.translate(x, y);
        g2.fillRect(a, (h-b)/2, w-a-a, b);
        g2.translate(-x, -y);
    }
    @Override public int getIconWidth()  { return icon.getIconWidth();  }
    @Override public int getIconHeight() { return icon.getIconHeight(); }
}
