package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.Optional;
import javax.swing.*;
import javax.swing.table.*;

public class MainPanel extends JPanel {
    private final String[] columnNames = {"String", "Integer", "Boolean"};
    private final Object[][] data = {
        {"aaa", 12, true}, {"bbb", 5, false},
        {"CCC", 92, true}, {"DDD", 0, false}
    };
    private final DefaultTableModel model = new DefaultTableModel(data, columnNames) {
        @Override public Class<?> getColumnClass(int column) {
            return getValueAt(0, column).getClass();
        }
    };
    private final JTable table = new JTable(model) {
        private int prevHeight = -1;
        private int prevCount = -1;
        private void updateRowsHeight(JViewport vport) {
            int height = vport.getExtentSize().height;
            int rowCount = getModel().getRowCount();
            int defautlRowHeight = height / rowCount;
            if ((height != prevHeight || rowCount != prevCount) && defautlRowHeight > 0) {
                int over = height - rowCount * defautlRowHeight;
                for (int i = 0; i < rowCount; i++) {
                    int a = over > 0 ? i == rowCount - 1 ? over : 1 : 0;
                    setRowHeight(i, defautlRowHeight + a);
                    over--;
                }
            }
            prevHeight = height;
            prevCount = rowCount;
        }
        @Override public void doLayout() {
            super.doLayout();
            Class<JViewport> clz = JViewport.class;
            Optional.ofNullable(SwingUtilities.getAncestorOfClass(clz, this))
                .filter(clz::isInstance).map(clz::cast)
                .ifPresent(this::updateRowsHeight);
        }
    };

    public MainPanel() {
        super(new BorderLayout());

        JScrollPane scroll = new JScrollPane(table);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) {
                Component c = e.getComponent();
                if (c instanceof JScrollPane) {
                    ((JScrollPane) c).getViewport().getView().revalidate();
                }
            }
        });

        JButton button = new JButton("add");
        button.addActionListener(e -> model.addRow(new Object[] {"", 0, false}));

        add(scroll);
        add(button, BorderLayout.SOUTH);
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
