package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.EnumSet;
import java.util.Set;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.ColorUIResource;
import javax.swing.table.*;

public final class MainPanel extends JPanel {
    private final String[] columnNames = {"String", "Integer", "Boolean"};
    private final Object[][] data = {
        {"aaa", 12, true}, {"bbb", 5, false},
        {"CCC", 92, true}, {"DDD", 0, false}
    };
    private final DefaultTableModel model = new DefaultTableModel(data, columnNames) {
        @Override public Class<?> getColumnClass(int column) {
            // ArrayIndexOutOfBoundsException: 0 >= 0
            // [JDK-6967479] JTable sorter fires even if the model is empty - Java Bug System
            // https://bugs.openjdk.java.net/browse/JDK-6967479
            // return getValueAt(0, column).getClass();
            switch (column) {
                case 0: return String.class;
                case 1: return Number.class;
                case 2: return Boolean.class;
                default: return super.getColumnClass(column);
            }
        }
    };
    private final JTable table = new LineFocusTable(model);

    public MainPanel() {
        super(new BorderLayout());
        UIManager.put("Table.focusCellHighlightBorder", new DotBorder(2, 2, 2, 2));

        // TableColumnModel columns = table.getColumnModel();
        // for (int i = 0; i < columns.getColumnCount(); i++) {
        //     columns.getColumn(i).setCellRenderer(new TestRenderer());
        // }

        table.setRowSelectionAllowed(true);
        table.setAutoCreateRowSorter(true);
        table.setFillsViewportHeight(true);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension());
        table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

        table.setComponentPopupMenu(new TablePopupMenu());
        add(new JScrollPane(table));
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

// class TestRenderer extends DefaultTableCellRenderer {
//     private static final DotBorder dotBorder = new DotBorder(2, 2, 2, 2);
//     private static final Border emptyBorder = BorderFactory.createEmptyBorder(2, 2, 2, 2);
//     @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
//         Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
//         if (c instanceof JComponent) {
//             int lsi = table.getSelectionModel().getLeadSelectionIndex();
//             ((JComponent) c).setBorder(row == lsi ? dotBorder : emptyBorder);
//             dotBorder.setLastCellFlag(row == lsi && column == table.getColumnCount() - 1);
//         }
//         return c;
//     }
// }

class LineFocusTable extends JTable {
    private final DotBorder dotBorder = new DotBorder(2, 2, 2, 2);
    private final Border emptyBorder = BorderFactory.createEmptyBorder(2, 2, 2, 2);
    protected LineFocusTable(TableModel model) {
        super(model);
    }
    @Override public void updateUI() {
        // [JDK-6788475] Changing to Nimbus LAF and back doesn't reset look and feel of JTable completely - Java Bug System
        // https://bugs.openjdk.java.net/browse/JDK-6788475
        // XXX: set dummy ColorUIResource
        setSelectionForeground(new ColorUIResource(Color.RED));
        setSelectionBackground(new ColorUIResource(Color.RED));
        super.updateUI();
        updateRenderer();
        remakeBooleanEditor();
    }
    private void updateRenderer() {
        TableModel m = getModel();
        for (int i = 0; i < m.getColumnCount(); i++) {
            TableCellRenderer r = getDefaultRenderer(m.getColumnClass(i));
            if (r instanceof Component) {
                SwingUtilities.updateComponentTreeUI((Component) r);
            }
        }
    }
    private void remakeBooleanEditor() {
        JCheckBox checkBox = new JCheckBox();
        checkBox.setHorizontalAlignment(SwingConstants.CENTER);
        checkBox.setBorderPainted(true);
        checkBox.setOpaque(true);
        checkBox.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                JCheckBox cb = (JCheckBox) e.getComponent();
                ButtonModel m = cb.getModel();
                if (m.isPressed() && isRowSelected(getEditingRow()) && e.isControlDown()) {
                    if (getEditingRow() % 2 == 0) {
                        cb.setOpaque(false);
                        // cb.setBackground(getBackground());
                    } else {
                        cb.setOpaque(true);
                        cb.setBackground(UIManager.getColor("Table.alternateRowColor"));
                    }
                } else {
                    cb.setBackground(getSelectionBackground());
                    cb.setOpaque(true);
                }
            }
            @Override public void mouseExited(MouseEvent e) {
                // in order to drag table row selection
                if (isEditing() && !getCellEditor().stopCellEditing()) {
                    getCellEditor().cancelCellEditing();
                }
            }
        });
        setDefaultEditor(Boolean.class, new DefaultCellEditor(checkBox));
    }
    private void updateBorderType(DotBorder border, int column) {
        border.type.clear(); // = EnumSet.noneOf(Type.class);
        if (column == 0) {
            border.type.add(Type.START);
        }
        if (column == getColumnCount() - 1) {
            border.type.add(Type.END);
        }
    }
    @Override public Component prepareRenderer(TableCellRenderer tcr, int row, int column) {
        Component c = super.prepareRenderer(tcr, row, column);
        if (c instanceof JCheckBox) {
            JCheckBox b = (JCheckBox) c;
            b.setBorderPainted(true);
        }
        if (row == getSelectionModel().getLeadSelectionIndex()) { // isRowSelected(row)) {
            ((JComponent) c).setBorder(dotBorder);
            updateBorderType(dotBorder, column);
        } else {
            ((JComponent) c).setBorder(emptyBorder);
        }
        return c;
    }
    @Override public Component prepareEditor(TableCellEditor editor, int row, int column) {
        Component c = super.prepareEditor(editor, row, column);
        if (c instanceof JCheckBox) {
            JCheckBox b = (JCheckBox) c;
            // System.out.println(b.getBorder());
            b.setBorder(dotBorder);
            updateBorderType(dotBorder, column);
            // updateBorderType((DotBorder) b.getBorder(), column);
            // b.setBorderPainted(true);
            // b.setBackground(getSelectionBackground());
        }
        return c;
    }
}

enum Type { START, END; }

class DotBorder extends EmptyBorder {
    private static final BasicStroke DASHED = new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, new float[] {1f}, 0f);
    private static final Color DOT_COLOR = new Color(200, 150, 150);
    public final Set<Type> type = EnumSet.noneOf(Type.class);

    protected DotBorder(int top, int left, int bottom, int right) {
        super(top, left, bottom, right);
    }
    @Override public boolean isBorderOpaque() {
        return true;
    }
    @Override public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.translate(x, y);
        g2.setPaint(DOT_COLOR);
        g2.setStroke(DASHED);
        if (type.contains(Type.START)) {
            g2.drawLine(0, 0, 0, h);
        }
        if (type.contains(Type.END)) {
            g2.drawLine(w - 1, 0, w - 1, h);
        }
        if (c.getBounds().x % 2 == 0) {
            g2.drawLine(0, 0, w, 0);
            g2.drawLine(0, h - 1, w, h - 1);
        } else {
            g2.drawLine(1, 0, w, 0);
            g2.drawLine(1, h - 1, w, h - 1);
        }
        g2.dispose();
    }
}

// // Another example test:
// import java.awt.*;
// import java.util.*;
// import javax.swing.*;
// import javax.swing.border.Border;
// import javax.swing.table.*;
// class FocusCellHighlightBorderTest {
//   String[] columnNames = {"String", "Integer", "Boolean"};
//   Object[][] data = {
//     {"aaa", 12, true}, {"bbb", 5, false}, {"CCC", 92, true}
//   };
//   DefaultTableModel model = new DefaultTableModel(data, columnNames) {
//     @Override public Class<?> getColumnClass(int column) {
//       return getValueAt(0, column).getClass();
//     }
//   };
//   public Component makeUI() {
//     UIManager.put("Table.focusCellHighlightBorder", new DotBorder(2, 2, 2, 2));
//     JTable table = new JTable(model) {
//       private final DotBorder dotBorder = new DotBorder(2, 2, 2, 2);
//       private void updateBorderType(DotBorder border, boolean isLeadRow, int column) {
//         border.type = EnumSet.noneOf(DotBorder.Type.class);
//         if (isLeadRow) {
//           border.type.add(DotBorder.Type.LEAD);
//           if (column == 0) {
//             border.type.add(DotBorder.Type.WEST);
//           }
//           if (column == getColumnCount() - 1) {
//             border.type.add(DotBorder.Type.EAST);
//           }
//         }
//       }
//       @Override public Component prepareRenderer(TableCellRenderer tcr, int row, int column) {
//         JComponent c = (JComponent) super.prepareRenderer(tcr, row, column);
//         c.setBorder(dotBorder);
//         updateBorderType(dotBorder, row == getSelectionModel().getLeadSelectionIndex(), column);
//         return c;
//       }
//       @Override public Component prepareEditor(TableCellEditor editor, int row, int column) {
//         Component c = super.prepareEditor(editor, row, column);
//         if (c instanceof JCheckBox) {
//           JCheckBox b = (JCheckBox) c;
//           updateBorderType((DotBorder) b.getBorder(), true, column);
//           b.setBorderPainted(true);
//           b.setBackground(getSelectionBackground());
//         }
//         return c;
//       }
//     };
//     table.setShowGrid(false);
//     table.setIntercellSpacing(new Dimension());
//     JPanel p = new JPanel(new BorderLayout());
//     p.add(new JScrollPane(table));
//     return p;
//   }
//   public static void main(String... args) {
//     EventQueue.invokeLater(new Runnable() {
//       @Override public void run() {
//         createAndShowGui();
//       }
//     });
//   }
//   public static void createAndShowGui() {
//     JFrame frame = new JFrame();
//     frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
//     frame.getContentPane().add(new FocusCellHighlightBorderTest().makeUI());
//     frame.setSize(320, 240);
//     frame.setLocationRelativeTo(null);
//     frame.setVisible(true);
//   }
// }
// class DotBorder extends EmptyBorder {
//   public enum Type { LEAD, WEST, EAST; }
//   public EnumSet<Type> type = EnumSet.noneOf(Type.class);
//   private static final BasicStroke dashed = new BasicStroke(
//     1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
//     10f, (new float[] { 1f }), 0f);
//   private static final Color DOT_COLOR = new Color(200, 150, 150);
//   public DotBorder(int top, int left, int bottom, int right) {
//     super(top, left, bottom, right);
//   }
//   @Override public boolean isBorderOpaque() {
//     return true;
//   }
//   @Override public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
//     Graphics2D g2 = (Graphics2D) g.create();
//     g2.translate(x, y);
//     g2.setPaint(DOT_COLOR);
//     g2.setStroke(dashed);
//     if (type.contains(Type.WEST)) {
//       g2.drawLine(0, 0, 0, h);
//     }
//     if (type.contains(Type.EAST)) {
//       g2.drawLine(w - 1, 0, w - 1, h);
//     }
//     if (type.contains(Type.LEAD)) {
//       if (c.getBounds().x % 2 == 0) {
//         g2.drawLine(0, 0, w, 0);
//         g2.drawLine(0, h - 1, w, h - 1);
//       } else {
//         g2.drawLine(1, 0, w, 0);
//         g2.drawLine(1, h - 1, w, h - 1);
//       }
//     }
//     g2.dispose();
//   }
// }

class TablePopupMenu extends JPopupMenu {
    private final JMenuItem delete;
    protected TablePopupMenu() {
        super();
        add("add").addActionListener(e -> {
            JTable table = (JTable) getInvoker();
            DefaultTableModel model = (DefaultTableModel) table.getModel();
            model.addRow(new Object[] {"New row", model.getRowCount(), false});
            Rectangle r = table.getCellRect(model.getRowCount() - 1, 0, true);
            table.scrollRectToVisible(r);
        });
        add("clearSelection").addActionListener(e -> ((JTable) getInvoker()).clearSelection());
        addSeparator();
        delete = add("delete");
        delete.addActionListener(e -> {
            JTable table = (JTable) getInvoker();
            DefaultTableModel model = (DefaultTableModel) table.getModel();
            int[] selection = table.getSelectedRows();
            for (int i = selection.length - 1; i >= 0; i--) {
                model.removeRow(table.convertRowIndexToModel(selection[i]));
            }
        });
    }
    @Override public void show(Component c, int x, int y) {
        if (c instanceof JTable) {
            delete.setEnabled(((JTable) c).getSelectedRowCount() > 0);
            super.show(c, x, y);
        }
    }
}
