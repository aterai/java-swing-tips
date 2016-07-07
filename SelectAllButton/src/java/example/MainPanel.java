package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;

public final class MainPanel extends JPanel {
    private final String[] columnNames = {"String", "Integer", "Boolean"};
    private final Object[][] data = {
        {"aaa", 12, true}, {"bbb", 5, false},
        {"CCC", 92, true}, {"DDD", 0, false}
    };
    private final TableModel model = new DefaultTableModel(data, columnNames) {
        @Override public Class<?> getColumnClass(int column) {
            return getValueAt(0, column).getClass();
        }
    };
    private final JTable table = new JTable(model);
    private final Action selectAllAction = new AbstractAction("selectAll") {
        @Override public void actionPerformed(ActionEvent e) {
            e.setSource(table);
            table.getActionMap().get("selectAll").actionPerformed(e);
        }
    };
    private final Action copyAction = new AbstractAction("copy") {
        @Override public void actionPerformed(ActionEvent e) {
            e.setSource(table);
            table.getActionMap().get("copy").actionPerformed(e);
        }
    };
    public MainPanel() {
        super(new BorderLayout());

        JPanel p = new JPanel();
        p.add(new JButton(selectAllAction));
        p.add(new JButton(copyAction));

        JSplitPane sp = new JSplitPane(JSplitPane.VERTICAL_SPLIT); //Panel(new GridLayout(2, 1));
        sp.setTopComponent(new JScrollPane(table));
        sp.setBottomComponent(new JScrollPane(new JTextArea()));
        sp.setResizeWeight(.5);

        add(p, BorderLayout.NORTH);
        add(sp);
        setPreferredSize(new Dimension(320, 240));
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                getRootPane().setJMenuBar(createMenuBar());
            }
        });
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("Edit");
        menu.setMnemonic(KeyEvent.VK_E);
        menuBar.add(menu);
        menu.add(new JMenuItem(selectAllAction));
        menu.add(new JMenuItem(copyAction));
        return menuBar;
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
