package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@
import java.awt.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.nimbus.*;
import javax.swing.table.*;

public final class MainPanel extends JPanel {
    private final String[] columnNames = {"A", "B", "C"};
    private final Object[][] data = {
        {"A0, Line1\nA0, Line2\nA0, Line3", "B0, Line1\nB0, Line2", "C0, Line1"},
        {"A1, Line1", "B1, Line1\nB1, Line2", "C1, Line1"},
        {"A2, Line1", "B2, Line1", "C2, Line1"}
    };
    private final TableModel model = new DefaultTableModel(data, columnNames) {
        @Override public Class<?> getColumnClass(int column) {
            return getValueAt(0, column).getClass();
        }
        @Override public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table1 = new JTable(model);

    public MainPanel() {
        super(new GridLayout(2, 0));

        table1.setAutoCreateRowSorter(true);
        table1.setDefaultRenderer(String.class, new MultiLineTableCellRenderer());

        JTable table2 = new JTable(model);
        table2.setAutoCreateRowSorter(true);

        // https://docs.oracle.com/javase/tutorial/uiswing/lookandfeel/_nimbusDefaults.html
        UIDefaults d = new UIDefaults();
        d.put("TextArea.borderPainter", new Painter<JComponent>() {
            @Override public void paint(Graphics2D g, JComponent c, int w, int h) { /* Empty painter */ }
        });
        MultiLineTableCellRenderer r = new MultiLineTableCellRenderer();
        r.putClientProperty("Nimbus.Overrides", d);
        r.putClientProperty("Nimbus.Overrides.InheritDefaults", false);

        // // or
        // d.put("TextArea.NotInScrollPane", new State("NotInScrollPane") {
        //     @Override protected boolean isInState(JComponent c) {
        //         // @see javax.swing.plaf.nimbus.TextAreaNotInScrollPaneState
        //         // return !(c.getParent() instanceof JViewport);
        //         return false;
        //     }
        // });
        // r.putClientProperty("Nimbus.Overrides", d);

        table2.setDefaultRenderer(String.class, r);

        add(new JScrollPane(table1));
        add(new JScrollPane(table2));
        setPreferredSize(new Dimension(320, 240));
    }
    private JCheckBoxMenuItem makeJCheckBoxMenuItem(String title, UIDefaults d) {
        JCheckBoxMenuItem cbmi = new JCheckBoxMenuItem(title);
        cbmi.putClientProperty("Nimbus.Overrides", d);
        cbmi.putClientProperty("Nimbus.Overrides.InheritDefaults", false);
        return cbmi;
    }
    public JMenuBar createMenuBar() {
        UIDefaults d = new UIDefaults();
        d.put("CheckBoxMenuItem[Enabled].checkIconPainter",
              new MyCheckBoxMenuItemPainter(CheckIcon.ENABLED));
        d.put("CheckBoxMenuItem[MouseOver].checkIconPainter",
              new MyCheckBoxMenuItemPainter(CheckIcon.MOUSEOVER));
        d.put("CheckBoxMenuItem[Enabled+Selected].checkIconPainter",
              new MyCheckBoxMenuItemPainter(CheckIcon.ENABLED_SELECTED));
        d.put("CheckBoxMenuItem[MouseOver+Selected].checkIconPainter",
              new MyCheckBoxMenuItemPainter(CheckIcon.SELECTED_MOUSEOVER));
        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("Menu");
        menuBar.add(menu);
        menu.add(new JCheckBoxMenuItem("Default"));
        menu.add(makeJCheckBoxMenuItem("Test1", d));
        menu.add(makeJCheckBoxMenuItem("Test2", d));
        menu.add(makeJCheckBoxMenuItem("Test3", d));
        JCheckBoxMenuItem cbmi1 = makeJCheckBoxMenuItem("Test4", d);
        cbmi1.setSelected(true);
        cbmi1.setEnabled(false);
        menu.add(cbmi1);
        JCheckBoxMenuItem cbmi2 = makeJCheckBoxMenuItem("Test5", d);
        cbmi2.setSelected(false);
        cbmi2.setEnabled(false);
        menu.add(cbmi2);
        menuBar.add(menu);
        return menuBar;
    }
    public static void main(String... args) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                createAndShowGui();
            }
        });
    }
    public static void createAndShowGui() {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (ClassNotFoundException | InstantiationException
               | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        }
        MainPanel p = new MainPanel();
        JFrame frame = new JFrame("@title@");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(p);
        frame.setJMenuBar(p.createMenuBar());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

enum CheckIcon {
    ENABLED_SELECTED,
    SELECTED_MOUSEOVER,
    ENABLED,
    MOUSEOVER;
}

// @see CheckBoxMenuItemPainter.java
class MyCheckBoxMenuItemPainter extends AbstractRegionPainter {
    // public static final int CHECKICON_ENABLED_SELECTED = 6;
    // public static final int CHECKICON_SELECTED_MOUSEOVER = 7;
    // public static final int CHECKICON_ENABLED = 8;
    // public static final int CHECKICON_MOUSEOVER = 9;
    private final CheckIcon state;
    private final PaintContext ctx;
    protected MyCheckBoxMenuItemPainter(CheckIcon state) {
        super();
        this.state = state;
        this.ctx = new AbstractRegionPainter.PaintContext(new Insets(5, 5, 5, 5), new Dimension(9, 10), false, null, 1d, 1d);
    }
    @Override protected void doPaint(Graphics2D g, JComponent c, int width, int height, Object[] eckey) {
        switch (state) {
            case ENABLED:
                paintcheckIconEnabled(g);
                break;
            case MOUSEOVER:
                paintcheckIconMouseOver(g);
                break;
            case ENABLED_SELECTED:
                paintcheckIconEnabledAndSelected(g);
                break;
            case SELECTED_MOUSEOVER:
                paintcheckIconSelectedAndMouseOver(g);
                break;
            default:
                break;
        }
    }
    @Override protected final PaintContext getPaintContext() {
        return ctx;
    }
    private void paintcheckIconEnabled(Graphics2D g) {
        g.setPaint(Color.GREEN);
        g.drawOval(0, 0, 10, 10);
    }
    private void paintcheckIconMouseOver(Graphics2D g) {
        g.setPaint(Color.PINK);
        g.drawOval(0, 0, 10, 10);
    }
    private void paintcheckIconEnabledAndSelected(Graphics2D g) {
        g.setPaint(Color.ORANGE);
        g.fillOval(0, 0, 10, 10);
    }
    private void paintcheckIconSelectedAndMouseOver(Graphics2D g) {
        g.setPaint(Color.CYAN);
        g.fillOval(0, 0, 10, 10);
    }
}

class MultiLineTableCellRenderer extends JTextArea implements TableCellRenderer {
    private final List<List<Integer>> rowColHeight = new ArrayList<>();
    private Border fhb; // = UIManager.getBorder("Table.focusCellHighlightBorder");
    private final Border epb = BorderFactory.createEmptyBorder(2, 5, 2, 5);
    @Override public void updateUI() {
        setBorder(null);
        super.updateUI();
        setLineWrap(true);
        setWrapStyleWord(true);
        setOpaque(true);

        // System.out.println(UIManager.get("nimbusFocus"));
        Border b = BorderFactory.createLineBorder(new Color(115, 164, 209));
        fhb = BorderFactory.createCompoundBorder(b, BorderFactory.createEmptyBorder(1, 4, 1, 4));
        setBorder(epb);
        // setMargin(new Insets(0, 0, 0, 0));
    }
    @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        setFont(table.getFont());
        setText(Objects.toString(value, ""));
        setBorder(hasFocus ? fhb : epb);
        if (isSelected) {
            setForeground(table.getSelectionForeground());
            setBackground(table.getSelectionBackground());
        } else {
            setForeground(table.getForeground());
            setBackground(table.getBackground());
        }
        setBounds(table.getCellRect(row, column, false));
        int maxH = getAdjustedRowHeight(row, column);
        if (table.getRowHeight(row) != maxH) {
            table.setRowHeight(row, maxH);
        }
        return this;
    }

    /**
     * Calculate the new preferred height for a given row, and sets the height on the table.
     * http://blog.botunge.dk/post/2009/10/09/JTable-multiline-cell-renderer.aspx
     */
    private int getAdjustedRowHeight(int row, int column) {
        // The trick to get this to work properly is to set the width of the column to the
        // textarea. The reason for this is that getPreferredSize(), without a width tries
        // to place all the text in one line. By setting the size with the with of the column,
        // getPreferredSize() returnes the proper height which the row should have in
        // order to make room for the text.
        // int cWidth = table.getTableHeader().getColumnModel().getColumn(column).getWidth();
        // int cWidth = table.getCellRect(row, column, false).width; // Ignore IntercellSpacing
        // setSize(new Dimension(cWidth, 1000));

        int prefH = getPreferredSize().height;
        while (rowColHeight.size() <= row) {
            rowColHeight.add(new ArrayList<>(column));
        }
        List<Integer> colHeights = rowColHeight.get(row);
        while (colHeights.size() <= column) {
            colHeights.add(0);
        }
        colHeights.set(column, prefH);
        int maxH = prefH;
        for (Integer colHeight: colHeights) {
            if (colHeight > maxH) {
                maxH = colHeight;
            }
        }
        return maxH;
    }
}
