package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

public class MainPanel extends JPanel {
    private final String[] model = {"Item 0", "Item 1", "Item 2"};
    public MainPanel() {
        super(new BorderLayout());

        String[] columnNames = {"Default", "setEnabled", "String"};
        Object[][] data = {
            {model[0], model[0], "aaa"}, {model[1], model[2], "bbb"}
        };
        JTable table = new JTable(new DefaultTableModel(data, columnNames));
        table.setRowHeight(20);

        table.getColumnModel().getColumn(0).setCellEditor(new DefaultCellEditor(new JComboBox<String>(model)));

        JComboBox<String> comboBox = new JComboBox<>(model);
        //comboBox.setEnabled(false);
        comboBox.addAncestorListener(new AncestorListener() {
            @Override public void ancestorAdded(final AncestorEvent e) {
                System.out.println("ancestorAdded");
                e.getComponent().setEnabled(false);
                EventQueue.invokeLater(new Runnable() {
                    @Override public void run() {
                        System.out.println("invokeLater");
                        e.getComponent().setEnabled(true);
                    }
                });
            }
            @Override public void ancestorRemoved(AncestorEvent e) {
                //OR:
                //System.out.println("ancestorRemoved");
                //e.getComponent().setEnabled(false);
            }
            @Override public void ancestorMoved(AncestorEvent e) { /* not needed */ }
        });
        table.getColumnModel().getColumn(1).setCellEditor(new DefaultCellEditor(comboBox));

        add(new JScrollPane(table));
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
