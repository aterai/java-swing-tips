package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.util.Objects;
import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.table.*;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());
        String[] columnNames = {"Integer", "String", "Boolean"};
        Object[][] data = {
            {12, "Name 0", true}, {5, "Name 2", false},
            {92, "Name 1", true}, {0, "Name 0", false}
        };
        TableModel model = new DefaultTableModel(data, columnNames) {
            @Override public Class<?> getColumnClass(int column) {
                return getValueAt(0, column).getClass();
            }
        };
        JTable table = new JTable(model) {
            private final Color evenColor = new Color(240, 240, 250);
            @Override public Component prepareRenderer(TableCellRenderer tcr, int row, int column) {
                Component c = super.prepareRenderer(tcr, row, column);
                if (isRowSelected(row)) {
                    c.setForeground(getSelectionForeground());
                    c.setBackground(getSelectionBackground());
                } else {
                    c.setForeground(getForeground());
                    c.setBackground(row % 2 == 0 ? evenColor : getBackground());
                }
                return c;
            }
        };

        TableColumn col = table.getColumnModel().getColumn(0);
        col.setMinWidth(60);
        col.setMaxWidth(60);
        col.setResizable(false);

        UIManager.put("ComboBox.buttonDarkShadow", UIManager.getColor("TextField.foreground"));
        JComboBox<String> combo = makeComboBox(new DefaultComboBoxModel<>(new String[] {"Name 0", "Name 1", "Name 2"}));

        col = table.getColumnModel().getColumn(1);
        col.setCellRenderer(new ComboCellRenderer());
        col.setCellEditor(new DefaultCellEditor(combo));
        // table.setDefaultEditor(JComboBox.class, new DefaultCellEditor(combo));

        table.setAutoCreateRowSorter(true);
        add(new JScrollPane(table));
        setPreferredSize(new Dimension(320, 240));
    }
    private static <E> JComboBox<E> makeComboBox(ComboBoxModel<E> model) {
        JComboBox<E> combo = new JComboBox<E>(model) {
            @Override public void updateUI() {
                super.updateUI();
                setBorder(BorderFactory.createEmptyBorder());
                setUI(new BasicComboBoxUI() {
                    @Override protected JButton createArrowButton() {
                        JButton button = super.createArrowButton();
                        button.setContentAreaFilled(false);
                        button.setBorder(BorderFactory.createEmptyBorder());
                        return button;
                    }
                });
                // JTextField editor = (JTextField) getEditor().getEditorComponent();
                // editor.setBorder(BorderFactory.createEmptyBorder());
                // editor.setOpaque(true);
                // editor.setEditable(false);
            }
        };
        // combo.setBorder(BorderFactory.createEmptyBorder());
        // ((JTextField) combo.getEditor().getEditorComponent()).setBorder(null);
        // ((JTextField) combo.getEditor().getEditorComponent()).setMargin(null);
        // combo.setBackground(Color.WHITE);
        // combo.setOpaque(true);
        // combo.setEditable(true);
        return combo;
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
class ComboCellRenderer extends JComboBox<String> implements TableCellRenderer {
    protected static final Color EVEN_COLOR = new Color(240, 240, 250);
    protected JButton button;
    @Override public void updateUI() {
        super.updateUI();
        setBorder(BorderFactory.createEmptyBorder());
        setUI(new BasicComboBoxUI() {
            @Override protected JButton createArrowButton() {
                button = super.createArrowButton();
                button.setContentAreaFilled(false);
                // button.setBackground(ComboCellRenderer.this.getBackground());
                button.setBorder(BorderFactory.createEmptyBorder());
                return button;
            }
        });
    }
    @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        JTextField editor = (JTextField) getEditor().getEditorComponent();
        editor.setBorder(BorderFactory.createEmptyBorder());
        editor.setOpaque(true);
        // editor.setEditable(false);
        removeAllItems();
        if (button != null) {
            if (isSelected) {
                editor.setForeground(table.getSelectionForeground());
                editor.setBackground(table.getSelectionBackground());
                button.setBackground(table.getSelectionBackground());
            } else {
                editor.setForeground(table.getForeground());
                // setBackground(table.getBackground());
                Color bg = row % 2 == 0 ? EVEN_COLOR : table.getBackground();
                editor.setBackground(bg);
                button.setBackground(bg);
            }
        }
        addItem(Objects.toString(value, ""));
        return this;
    }
    // Overridden for performance reasons. ---->
    @Override public boolean isOpaque() {
        Color back = getBackground();
        Object o = SwingUtilities.getAncestorOfClass(JTable.class, this);
        if (o instanceof JTable) {
            JTable table = (JTable) o;
            boolean colorMatch = Objects.nonNull(back) && back.equals(table.getBackground()) && table.isOpaque();
            return !colorMatch && super.isOpaque();
        } else {
            return super.isOpaque();
        }
    }
    @Override protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        // System.out.println(propertyName);
        // if ((propertyName == "font" || propertyName == "foreground") && oldValue != newValue) {
        //     super.firePropertyChange(propertyName, oldValue, newValue);
        // }
    }
    // @Override public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {}
    @Override public void repaint(long tm, int x, int y, int width, int height) { /* Overridden for performance reasons. */ }
    @Override public void repaint(Rectangle r) { /* Overridden for performance reasons. */ }
    @Override public void repaint() { /* Overridden for performance reasons. */ }
    // @Override public void invalidate() { /* Overridden for performance reasons. */ }
    // @Override public void validate() { /* Overridden for performance reasons. */ }
    @Override public void revalidate() { /* Overridden for performance reasons. */ }
    // <---- Overridden for performance reasons.
}
