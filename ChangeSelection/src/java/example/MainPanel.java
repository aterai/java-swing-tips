package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

public class MainPanel extends JPanel {
    protected final String[] columnNames = {"A", "B", "C"};
    protected final Object[][] data = {
        {"0, 0", "0, 1", "0, 2"},
        {"1, 0", "1, 1", "1, 2"},
        {"2, 0", "2, 1", "2, 2"},
        {"3, 0", "3, 1", "3, 2"},
        {"4, 0", "4, 1", "4, 2"},
        {"5, 0", "5, 1", "5, 2"},
        {"6, 0", "6, 1", "6, 2"},
        {"7, 0", "7, 1", "7, 2"},
        {"8, 0", "8, 1", "8, 2"},
        {"9, 0", "9, 1", "9, 2"}
    };
    protected final TableModel model = new DefaultTableModel(data, columnNames) {
        @Override public Class<?> getColumnClass(int column) {
            return getValueAt(0, column).getClass();
        }
    };
    protected final JTable table = new JTable(model) {
        private final Color evenColor = new Color(250, 250, 250);
        @Override public Component prepareRenderer(TableCellRenderer tcr, int row, int column) {
            Component c = super.prepareRenderer(tcr, row, column);
            if (isCellSelected(row, column)) {
                c.setForeground(getSelectionForeground());
                c.setBackground(getSelectionBackground());
            } else {
                c.setForeground(getForeground());
                c.setBackground(row % 2 == 0 ? evenColor : getBackground());
            }
            return c;
        }
    };
    protected final JSpinner rowField;
    protected final JSpinner colField;
    protected final JCheckBox toggle = new JCheckBox("toggle", false);
    protected final JCheckBox extend = new JCheckBox("extend", false);

    public MainPanel() {
        super(new BorderLayout());
        table.setCellSelectionEnabled(true);
        // table.setAutoCreateRowSorter(true);

        rowField = new JSpinner(new SpinnerNumberModel(1, 0, model.getRowCount() - 1, 1));
        colField = new JSpinner(new SpinnerNumberModel(2, 0, model.getColumnCount() - 1, 1));

        table.getActionMap().put("clear-selection", new AbstractAction("clear-selection") {
            @Override public void actionPerformed(ActionEvent e) {
                table.clearSelection();
                requestFocusInWindow();
            }
        });
        InputMap imap = table.getInputMap(JComponent.WHEN_FOCUSED);
        imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "clear-selection");

        Box box = Box.createHorizontalBox();
        box.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        box.add(new JLabel("row:"));
        box.add(rowField);
        box.add(new JLabel(" col:"));
        box.add(colField);
        box.add(toggle);
        box.add(extend);

        JButton changeSelection = new JButton("changeSelection");
        changeSelection.addActionListener(e -> {
            int row = Integer.parseInt(rowField.getValue().toString());
            int col = Integer.parseInt(colField.getValue().toString());

            table.changeSelection(row, col, toggle.isSelected(), extend.isSelected());
            // table.changeSelection(row, table.convertColumnIndexToModel(col), toggle.isSelected(), extend.isSelected());
            table.requestFocusInWindow();
            table.repaint();
        });

        JButton clear = new JButton("clear(Esc)");
        clear.addActionListener(e -> {
            table.clearSelection();
            requestFocusInWindow();
        });

        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));
        p.add(changeSelection);
        p.add(clear);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("JTable#changeSelection(int, int, boolean, boolean)"));
        panel.add(box, BorderLayout.NORTH);
        panel.add(p, BorderLayout.SOUTH);

        add(panel, BorderLayout.NORTH);
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
