package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

import java.awt.*;
import java.util.Objects;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

public final class MainPanel extends JPanel {
    public static final int BORDERWIDTH1 = 1;
    public static final int BORDERWIDTH2 = 2;
    public static final int CELLSIZE = 18;

    private MainPanel() {
        super(new GridBagLayout());

        String[] columnNames = {"1", "2", "3", "4", "5", "6", "7", "8", "9"};
        Integer[][] data = {
            {5, 3, 0, 0, 7, 0, 0, 0, 0},
            {6, 0, 0, 1, 9, 5, 0, 0, 0},
            {0, 9, 8, 0, 0, 0, 0, 6, 0},
            {8, 0, 0, 0, 6, 0, 0, 0, 3},
            {4, 0, 0, 8, 0, 3, 0, 0, 1},
            {7, 0, 0, 0, 2, 0, 0, 0, 6},
            {0, 6, 0, 0, 0, 0, 2, 8, 0},
            {0, 0, 0, 4, 1, 9, 0, 0, 5},
            {0, 0, 0, 0, 8, 0, 0, 7, 9}
        };

        TableModel model = new DefaultTableModel(data, columnNames) {
            @Override public Class<?> getColumnClass(int column) {
                return Integer.class;
            }
            @Override public boolean isCellEditable(int row, int column) {
                return data[row][column] == 0;
            }
        };
        JTable table = new JTable(model) {
            @Override public Dimension getPreferredScrollableViewportSize() {
                return super.getPreferredSize();
            }
        };
        for (int i = 0; i < table.getRowCount(); i++) {
            int a = (i + 1) % 3 == 0 ? BORDERWIDTH2 : BORDERWIDTH1;
            table.setRowHeight(i, CELLSIZE + a);
        }

        // JTextField editor = new JTextField();
        // editor.setHorizontalAlignment(SwingConstants.CENTER);
        // editor.setBorder(BorderFactory.createLineBorder(Color.RED));
        // table.setDefaultEditor(Integer.class, new DefaultCellEditor(editor));

        table.setCellSelectionEnabled(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.getTableHeader().setReorderingAllowed(false);
        table.setBorder(BorderFactory.createEmptyBorder());

        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(false);

        table.setIntercellSpacing(new Dimension());
        table.setRowMargin(0);
        table.getColumnModel().setColumnMargin(0);

        table.setDefaultRenderer(Integer.class, new SudokuCellRenderer(data, table.getFont()));

        TableColumnModel m = table.getColumnModel();
        m.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        for (int i = 0; i < m.getColumnCount(); i++) {
            TableColumn col = m.getColumn(i);
            int a = (i + 1) % 3 == 0 ? BORDERWIDTH2 : BORDERWIDTH1;
            col.setPreferredWidth(CELLSIZE + a);
            col.setResizable(false);
        }

        JScrollPane scroll = new JScrollPane(table);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setViewportBorder(BorderFactory.createMatteBorder(BORDERWIDTH2, BORDERWIDTH2, 0, 0, Color.BLACK));
        scroll.setColumnHeader(new JViewport());
        scroll.getColumnHeader().setVisible(false);

        add(scroll);
        setPreferredSize(new Dimension(320, 240));
    }
    private static class SudokuCellRenderer extends DefaultTableCellRenderer {
        private final Font font;
        private final Font bold;
        private final Border b0 = BorderFactory.createMatteBorder(0, 0, BORDERWIDTH1, BORDERWIDTH1, Color.GRAY);
        private final Border b1 = BorderFactory.createMatteBorder(0, 0, BORDERWIDTH2, BORDERWIDTH2, Color.BLACK);
        private final Border b2 = BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, BORDERWIDTH2, 0, Color.BLACK),
            BorderFactory.createMatteBorder(0, 0, 0, BORDERWIDTH1, Color.GRAY));
        private final Border b3 = BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 0, BORDERWIDTH2, Color.BLACK),
            BorderFactory.createMatteBorder(0, 0, BORDERWIDTH1, 0, Color.GRAY));
        private final Integer[][] data;
        protected SudokuCellRenderer(Integer[][] src, Font font) {
            super();
            this.font = font;
            this.bold = font.deriveFont(Font.BOLD);
            Integer[][] dest = new Integer[src.length][src[0].length];
            for (int i = 0; i < src.length; i++) {
                System.arraycopy(src[i], 0, dest[i], 0, src[0].length);
            }
            this.data = dest;
        }
        @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            boolean isEditable = data[row][column] == 0;
            super.getTableCellRendererComponent(table, value, isEditable && isSelected, hasFocus, row, column);
            if (isEditable && Objects.equals(value, 0)) {
                this.setText(" ");
            }
            setFont(isEditable ? font : bold);
            setHorizontalAlignment(CENTER);
            boolean rf = (row + 1) % 3 == 0;
            boolean cf = (column + 1) % 3 == 0;
            if (rf && cf) {
                setBorder(b1);
            } else if (rf) {
                setBorder(b2);
            } else if (cf) {
                setBorder(b3);
            } else {
                setBorder(b0);
            }
            return this;
        }
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
