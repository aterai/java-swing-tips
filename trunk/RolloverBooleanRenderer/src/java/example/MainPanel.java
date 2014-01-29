package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.*;
import javax.swing.table.*;

public class MainPanel extends JPanel {
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
    private final JTable table = new JTable(model) {
        @Override public Component prepareEditor(TableCellEditor editor, int row, int column) {
            Component c = super.prepareEditor(editor, row, column);
            if(c instanceof JCheckBox) {
                ((JCheckBox)c).setBackground(getSelectionBackground());
            }
            return c;
        }
        private transient HighlightListener highlighter;
        @Override public void updateUI() {
            if(highlighter!=null) {
                addMouseListener(highlighter);
                addMouseMotionListener(highlighter);
                setDefaultRenderer(Object.class,  null);
                setDefaultRenderer(Number.class,  null);
                setDefaultRenderer(Boolean.class, null);
            }
            super.updateUI();
            highlighter = new HighlightListener(this);
            addMouseListener(highlighter);
            addMouseMotionListener(highlighter);
            setDefaultRenderer(Object.class,  new RolloverDefaultTableCellRenderer(highlighter));
            setDefaultRenderer(Number.class,  new RolloverNumberRenderer(highlighter));
            setDefaultRenderer(Boolean.class, new RolloverBooleanRenderer(highlighter));
        }
    };

    public MainPanel() {
        super(new BorderLayout());

        table.setAutoCreateRowSorter(true);

        JSplitPane sp = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                                       new JScrollPane(new JTable(model)),
                                       new JScrollPane(table));
        sp.setResizeWeight(0.5);
        add(sp);
        setPreferredSize(new Dimension(320, 240));
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

class HighlightListener extends MouseAdapter {
    private int row = -1;
    private int col = -1;
    private final JTable table;
    public HighlightListener(JTable table) {
        super();
        this.table = table;
    }
    public boolean isHighlightableCell(int row, int column) {
        return this.row==row && this.col==column;
    }
    @Override public void mouseMoved(MouseEvent e) {
        Point pt = e.getPoint();
        int prev_row = row;
        int prev_col = col;
        row = table.rowAtPoint(pt);
        col = table.columnAtPoint(pt);
        if(row<0 || col<0) { row = col = -1; }
// >>>> HyperlinkCellRenderer.java
// @see http://java.net/projects/swingset3/sources/svn/content/trunk/SwingSet3/src/com/sun/swingset3/demos/table/HyperlinkCellRenderer.java
        if(row == prev_row && col == prev_col) { return; }
        Rectangle repaintRect;
        if(row >= 0 && col >= 0) {
            Rectangle r = table.getCellRect(row, col, false);
            if(prev_row >= 0 && prev_col >= 0) {
                repaintRect = r.union(table.getCellRect(prev_row, prev_col, false));
            }else{
                repaintRect = r;
            }
        }else{
            repaintRect = table.getCellRect(prev_row, prev_col, false);
        }
        table.repaint(repaintRect);
// <<<<
        //table.repaint();
    }
    @Override public void mouseExited(MouseEvent e) {
        if(row >= 0 && col >= 0) {
            table.repaint(table.getCellRect(row, col, false));
        }
        row = col = -1;
    }
}

class RolloverDefaultTableCellRenderer extends DefaultTableCellRenderer {
    private static final Color highlight = new Color(255, 150, 50);
    private final HighlightListener highlighter;
    public RolloverDefaultTableCellRenderer(HighlightListener highlighter) {
        super();
        this.highlighter = highlighter;
    }
    @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        if(highlighter.isHighlightableCell(row, column)) {
            setText("<html><u>"+value.toString());
            setForeground(isSelected?table.getSelectionForeground():highlight);
            setBackground(isSelected?table.getSelectionBackground().darker():table.getBackground());
        }else{
            setText(value.toString());
            setForeground(isSelected?table.getSelectionForeground():table.getForeground());
            setBackground(isSelected?table.getSelectionBackground():table.getBackground());
        }
        return this;
    }
}

class RolloverNumberRenderer extends RolloverDefaultTableCellRenderer {
    public RolloverNumberRenderer(HighlightListener highlighter) {
        super(highlighter);
        setHorizontalAlignment(JLabel.RIGHT);
    }
}

class RolloverBooleanRenderer extends JCheckBox implements TableCellRenderer, UIResource {
    private static final Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);
    private final HighlightListener highlighter;
    public RolloverBooleanRenderer(HighlightListener highlighter) {
        super();
        this.highlighter = highlighter;
        setHorizontalAlignment(JLabel.CENTER);
        setBorderPainted(true);
        setRolloverEnabled(true);
        setOpaque(true);
    }
    @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        getModel().setRollover(highlighter.isHighlightableCell(row, column));

        if(isSelected) {
            setForeground(table.getSelectionForeground());
            super.setBackground(table.getSelectionBackground());
        }else{
            setForeground(table.getForeground());
            setBackground(table.getBackground());
            //setBackground(row%2==0?table.getBackground():Color.WHITE); //Nimbus
        }
        setSelected(value != null && ((Boolean)value).booleanValue());

        if(hasFocus) {
            setBorder(UIManager.getBorder("Table.focusCellHighlightBorder"));
        }else{
            setBorder(noFocusBorder);
        }
        return this;
    }
    //Overridden for performance reasons. ---->
    @Override public boolean isOpaque() {
        Color back = getBackground();
        Component p = getParent();
        if(p != null) {
            p = p.getParent();
        } // p should now be the JTable.
        boolean colorMatch = back != null && p != null && back.equals(p.getBackground()) && p.isOpaque();
        return !colorMatch && super.isOpaque();
    }
    @Override protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
//         System.out.println(propertyName);
//         if(propertyName=="border" || ((propertyName == "font" || propertyName == "foreground") && oldValue != newValue)) {
//             super.firePropertyChange(propertyName, oldValue, newValue);
//         }
    }
    @Override public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) { /* Overridden for performance reasons. */ }
    @Override public void repaint(long tm, int x, int y, int width, int height) { /* Overridden for performance reasons. */ }
    @Override public void repaint(Rectangle r) { /* Overridden for performance reasons. */ }
    @Override public void repaint()    { /* Overridden for performance reasons. */ }
    @Override public void invalidate() { /* Overridden for performance reasons. */ }
    @Override public void validate()   { /* Overridden for performance reasons. */ }
    @Override public void revalidate() { /* Overridden for performance reasons. */ }
    //<---- Overridden for performance reasons.
}
