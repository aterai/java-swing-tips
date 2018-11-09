package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Objects;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

public final class MainPanel extends JPanel {
    private final JCheckBox modelCheck = new JCheckBox("edit the cell on single click");

    private final String[] columnNames = {"A", "B", "C"};
    private final Object[][] data = {
        {"aaa", "eeee", "l"}, {"bbb", "ff", "ggg"},
        {"CCC", "kkk", "jj"}, {"DDD", "ii", "hhh"}
    };
    private final TableModel model = new DefaultTableModel(data, columnNames) {
        @Override public Class<?> getColumnClass(int column) {
            return getValueAt(0, column).getClass();
        }
    };
    private final JTable table = new JTable(model);
    public MainPanel() {
        super(new BorderLayout());

        table.setAutoCreateRowSorter(true);
        table.setRowSelectionAllowed(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        // table.setFillsViewportHeight(true);
        table.setIntercellSpacing(new Dimension());
        table.setShowGrid(false);
        // table.setShowHorizontalLines(false);
        // table.setShowVerticalLines(false);
        table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        JTableHeader tableHeader = table.getTableHeader();
        tableHeader.setReorderingAllowed(false);

        // TableColumn col = table.getColumnModel().getColumn(0);
        // col.setMinWidth(50);
        // col.setMaxWidth(50);
        // col.setResizable(false);

        TableCellRenderer defaultRenderer = (DefaultTableCellRenderer) table.getDefaultRenderer(Object.class);
        UnderlineCellRenderer underlineRenderer = new UnderlineCellRenderer();
        DefaultCellEditor ce = (DefaultCellEditor) table.getDefaultEditor(Object.class);
        modelCheck.addActionListener(e -> {
            if (modelCheck.isSelected()) {
                table.setDefaultRenderer(Object.class, underlineRenderer);
                table.addMouseListener(underlineRenderer);
                table.addMouseMotionListener(underlineRenderer);
                ce.setClickCountToStart(1);
            } else {
                table.setDefaultRenderer(Object.class, defaultRenderer);
                table.removeMouseListener(underlineRenderer);
                table.removeMouseMotionListener(underlineRenderer);
                ce.setClickCountToStart(2);
            }
        });
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(Color.WHITE);
        add(modelCheck, BorderLayout.NORTH);
        add(scrollPane);
        setPreferredSize(new Dimension(320, 240));
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
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException
               | IllegalAccessException | UnsupportedLookAndFeelException ex) {
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

class UnderlineCellRenderer extends DefaultTableCellRenderer implements MouseListener, MouseMotionListener {
    private int vrow = -1; // viewRowIndex
    private int vcol = -1; // viewColumnIndex
    @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        String str = Objects.toString(value, "");
        if (!table.isEditing() && this.vrow == row && this.vcol == column) {
            setText("<html><u>" + str);
        } else {
            setText(str);
        }
        // setForeground(table.getForeground());
        return this;
    }
    @Override public void mouseMoved(MouseEvent e) {
        JTable table = (JTable) e.getComponent();
        Point pt = e.getPoint();
        vrow = table.rowAtPoint(pt);
        vcol = table.columnAtPoint(pt);
        if (vrow < 0 || vcol < 0) {
            vrow = -1;
            vcol = -1;
        }
        table.repaint();
    }
    @Override public void mouseExited(MouseEvent e) {
        vrow = -1;
        vcol = -1;
        e.getComponent().repaint();
    }
    @Override public void mouseDragged(MouseEvent e) { /* not needed */ }
    @Override public void mouseClicked(MouseEvent e) { /* not needed */ }
    @Override public void mouseEntered(MouseEvent e) { /* not needed */ }
    @Override public void mousePressed(MouseEvent e) { /* not needed */ }
    @Override public void mouseReleased(MouseEvent e) { /* not needed */ }
}
