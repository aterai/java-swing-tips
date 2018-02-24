package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.util.stream.*;
import javax.swing.*;
import javax.swing.table.*;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout(5, 5));

        String[] columnNames = {"String", "Integer", "Boolean"};
        Object[][] data = {
            {"aaa", 12, true}, {"bbb", 5, false},
            {"CCC", 92, true}, {"DDD", 0, false}
        };
        TableModel model = new DefaultTableModel(data, columnNames) {
            @Override public Class<?> getColumnClass(int column) {
                return getValueAt(0, column).getClass();
            }
        };
        JTable table = new JTable(model);
        table.setAutoCreateRowSorter(true);

        JTextField field = new JTextField("5 : 3 : 2");
        JCheckBox check = new JCheckBox("ComponentListener#componentResized(...)", true);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) {
                if (check.isSelected()) {
                    setTableHeaderColumnRaito(table, field.getText().trim());
                }
            }
        });

        JButton button = new JButton("revalidate");
        button.addActionListener(e -> setTableHeaderColumnRaito(table, field.getText().trim()));

        JPanel p = new JPanel(new BorderLayout(5, 5));
        p.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        p.add(new JLabel("Ratio:"), BorderLayout.WEST);
        p.add(field);
        p.add(button, BorderLayout.EAST);
        JPanel panel = new JPanel(new GridLayout(2, 1));
        panel.setBorder(BorderFactory.createTitledBorder("JTableHeader column width ratio"));
        panel.add(p);
        panel.add(check);

        add(panel, BorderLayout.NORTH);
        add(scrollPane);
        setPreferredSize(new Dimension(320, 240));
    }

    protected static void setTableHeaderColumnRaito(JTable table, String text) {
        TableColumnModel m = table.getColumnModel();
        List<Integer> list = getWidthRaitoArray(text, m.getColumnCount());
        // System.out.println("a: " + m.getTotalColumnWidth());
        // System.out.println("b: " + table.getSize().width);
        int total = table.getSize().width; // m.getTotalColumnWidth();
        double raito = total / (double) list.stream().mapToInt(Integer::intValue).sum();
        for (int i = 0; i < m.getColumnCount() - 1; i++) {
            TableColumn col = m.getColumn(i);
            int colwidth = (int) (.5 + list.get(i) * raito);
            // col.setMaxWidth(colwidth);
            col.setPreferredWidth(colwidth);
            total -= colwidth;
        }
        // m.getColumn(m.getColumnCount() - 1).setMaxWidth(total);
        m.getColumn(m.getColumnCount() - 1).setPreferredWidth(total);
        table.revalidate();
    }

    protected static List<Integer> getWidthRaitoArray(String text, int length) {
        try {
            Stream<Integer> a = Arrays.stream(text.split(":")).map(String::trim).filter(s -> !s.isEmpty()).map(Integer::valueOf);
            Stream<Integer> b = Stream.generate(() -> 1).limit(length);
            return Stream.concat(a, b).limit(length).collect(Collectors.toList());
        } catch (NumberFormatException ex) {
            Toolkit.getDefaultToolkit().beep();
            JOptionPane.showMessageDialog(null, "invalid value.\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return Stream.generate(() -> 1).limit(length).collect(Collectors.toList());
        }
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
