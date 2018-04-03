package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.plaf.basic.*;
import javax.swing.plaf.metal.MetalComboBoxUI;
import javax.swing.table.*;

public final class MainPanel extends JPanel {
    @SuppressWarnings("JdkObsolete")
    private MainPanel() {
        super(new BorderLayout());

        List<Vector<Object>> aseries = new ArrayList<>();
        aseries.add(new Vector<>(Arrays.asList("A1", 594, 841)));
        aseries.add(new Vector<>(Arrays.asList("A2", 420, 594)));
        aseries.add(new Vector<>(Arrays.asList("A3", 297, 420)));
        aseries.add(new Vector<>(Arrays.asList("A4", 210, 297)));
        aseries.add(new Vector<>(Arrays.asList("A5", 148, 210)));
        aseries.add(new Vector<>(Arrays.asList("A6", 105, 148)));

        String[] columns = {"A series", "width", "height"};

        JTextField wtf = new JTextField(5);
        wtf.setEditable(false);

        JTextField htf = new JTextField(5);
        htf.setEditable(false);

        DefaultTableModel model = new DefaultTableModel(null, columns) {
            @Override public Class<?> getColumnClass(int column) {
                return column == 1 || column == 2 ? Integer.class : String.class;
            }
            @Override public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        DropdownTableComboBox<Vector<Object>> combo = new DropdownTableComboBox<>(aseries, model);
        combo.addActionListener(e -> {
            List<Object> rowData = combo.getSelectedRow();
            wtf.setText(Objects.toString(rowData.get(1)));
            htf.setText(Objects.toString(rowData.get(2)));
        });
        ListCellRenderer<? super Vector<Object>> renderer = combo.getRenderer();
        combo.setRenderer(new ListCellRenderer<Vector<Object>>() {
            @SuppressWarnings("PMD.ReplaceVectorWithList")
            @Override public Component getListCellRendererComponent(JList<? extends Vector<Object>> list, Vector<Object> value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel c = (JLabel) renderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                c.setOpaque(true);
                if (isSelected) {
                    c.setBackground(list.getSelectionBackground());
                    c.setForeground(list.getSelectionForeground());
                } else {
                    c.setBackground(list.getBackground());
                    c.setForeground(list.getForeground());
                }
                c.setText(Objects.toString(value.get(0), ""));
                return c;
            }
        });

        EventQueue.invokeLater(() -> combo.setSelectedIndex(3));

        Box box = Box.createHorizontalBox();
        box.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        box.add(combo);
        box.add(Box.createHorizontalStrut(15));
        box.add(new JLabel("width: "));
        box.add(wtf);
        box.add(Box.createHorizontalStrut(5));
        box.add(new JLabel("height: "));
        box.add(htf);
        box.add(Box.createHorizontalGlue());

        add(box, BorderLayout.NORTH);
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
        JFrame frame = new JFrame("@title@");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

class DropdownTableComboBox<E extends Vector<Object>> extends JComboBox<E> {
    protected final transient HighlightListener highlighter = new HighlightListener();
    protected final JTable table = new JTable() {
        @Override public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
            Component c = super.prepareRenderer(renderer, row, column);
            c.setForeground(Color.BLACK);
            if (highlighter.isHighlightableRow(row)) {
                c.setBackground(new Color(255, 200, 200));
            } else if (isRowSelected(row)) {
                c.setBackground(Color.CYAN);
            } else {
                c.setBackground(Color.WHITE);
            }
            return c;
        }
        @Override public void updateUI() {
            removeMouseListener(highlighter);
            removeMouseMotionListener(highlighter);
            super.updateUI();
            addMouseListener(highlighter);
            addMouseMotionListener(highlighter);
            getTableHeader().setReorderingAllowed(false);
        }
    };
    protected final List<E> list;

    protected DropdownTableComboBox(List<E> list, DefaultTableModel model) {
        super();
        this.list = list;
        table.setModel(model);
        list.forEach(this::addItem);
        list.forEach(model::addRow);
    }

    @Override public void updateUI() {
        super.updateUI();
        EventQueue.invokeLater(() -> {
            setUI(new MetalComboBoxUI() {
                @Override protected ComboPopup createPopup() {
                    return new ComboTablePopup(comboBox, table);
                }
            });
            setEditable(false);
        });
    }
    public List<Object> getSelectedRow() {
        return list.get(getSelectedIndex());
    }
}

class ComboTablePopup extends BasicComboPopup {
    private final JTable table;
    private final JScrollPane scroll;
    protected ComboTablePopup(JComboBox<?> combo, JTable table) {
        super(combo);
        this.table = table;

        ListSelectionModel sm = table.getSelectionModel();
        sm.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        sm.addListSelectionListener(e -> {
            combo.setSelectedIndex(table.getSelectedRow());
        });

        combo.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                setRowSelection(combo.getSelectedIndex());
            }
        });

        table.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                combo.setSelectedIndex(table.rowAtPoint(e.getPoint()));
                setVisible(false);
            }
        });

        scroll = new JScrollPane(table);
        setBorder(BorderFactory.createEmptyBorder());
    }
    @Override public void show() {
        if (isEnabled()) {
            Insets ins = scroll.getInsets();
            int tableh = table.getPreferredSize().height;
            int headerh = table.getTableHeader().getPreferredSize().height;
            scroll.setPreferredSize(new Dimension(240, tableh + headerh + ins.top + ins.bottom));
            super.removeAll();
            super.add(scroll);
            setRowSelection(comboBox.getSelectedIndex());
            super.show(comboBox, 0, comboBox.getBounds().height);
        }
    }
    private void setRowSelection(int index) {
        if (index != -1) {
            table.setRowSelectionInterval(index, index);
            table.scrollRectToVisible(table.getCellRect(index, 0, true));
        }
    }
}

class HighlightListener extends MouseAdapter {
    private int vrow = -1;
    public boolean isHighlightableRow(int row) {
        return this.vrow == row;
    }
    private void setHighlighTableCell(MouseEvent e) {
        Point pt = e.getPoint();
        Component c = e.getComponent();
        if (c instanceof JTable) {
            vrow = ((JTable) c).rowAtPoint(pt);
            c.repaint();
        }
    }
    @Override public void mouseMoved(MouseEvent e) {
        setHighlighTableCell(e);
    }
    @Override public void mouseDragged(MouseEvent e) {
        setHighlighTableCell(e);
    }
    @Override public void mouseExited(MouseEvent e) {
        vrow = -1;
        e.getComponent().repaint();
    }
}
