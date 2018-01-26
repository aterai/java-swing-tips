package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.table.*;

public final class MainPanel extends JPanel {
    private final String[] columnNames = {"Integer", "String", "Boolean"};
    private final Object[][] data = {
        {12, "Name 0", true}, {5, "Name 2", false},
        {92, "Name 1", true}, {0, "Name 0", false}
    };
    private final TableModel model = new DefaultTableModel(data, columnNames) {
        @Override public Class<?> getColumnClass(int column) {
            return getValueAt(0, column).getClass();
        }
    };
    private final JTable table = new JTable(model);
    private MainPanel() {
        super(new BorderLayout());

        UIManager.put("ComboBox.buttonDarkShadow", UIManager.getColor("TextField.foreground"));
        JComboBox<String> combo = makeComboBox();

        TableColumn col = table.getColumnModel().getColumn(0);
        col.setMinWidth(60);
        col.setMaxWidth(60);
        col.setResizable(false);

        col = table.getColumnModel().getColumn(1);
        col.setCellEditor(new DefaultCellEditor(combo));
        // table.setDefaultEditor(JComboBox.class, new DefaultCellEditor(combo));

        table.setAutoCreateRowSorter(true);
        add(new JScrollPane(table));
        setPreferredSize(new Dimension(320, 240));
    }
    private static JComboBox<String> makeComboBox() {
        JComboBox<String> combo = new JComboBox<String>(new String[] {"Name 0", "Name 1", "Name 2"}) {
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
//                 JTextField editor = (JTextField) getEditor().getEditorComponent();
//                 editor.setBorder(BorderFactory.createEmptyBorder());
//                 editor.setOpaque(true);
//                 editor.setEditable(false);
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
