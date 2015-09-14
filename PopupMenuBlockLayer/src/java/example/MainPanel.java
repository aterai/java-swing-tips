package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import javax.swing.*;
import javax.swing.plaf.LayerUI;
import javax.swing.table.*;

public final class MainPanel extends JPanel {
    private final JCheckBox check = new JCheckBox("Lock all(JScrollPane, JTable, JPopupMenu)");
    private final String[] columnNames = {"String", "Integer", "Boolean"};
    private final Object[][] data = {
        {"aaa", 12, true}, {"bbb", 5, false},
        {"CCC", 92, true}, {"DDD", 0, false},
    };
    private final DefaultTableModel model = new DefaultTableModel(data, columnNames) {
        @Override public Class<?> getColumnClass(int column) {
            // ArrayIndexOutOfBoundsException: 0 >= 0
            // Bug ID: JDK-6967479 JTable sorter fires even if the model is empty
            // http://bugs.java.com/view_bug.do?bug_id=6967479
            //return getValueAt(0, column).getClass();
            switch (column) {
              case 0:
                return String.class;
              case 1:
                return Number.class;
              case 2:
                return Boolean.class;
              default:
                return super.getColumnClass(column);
            }
        }
    };
    private final JTable table = new JTable(model) {
        @Override public String getToolTipText(MouseEvent e) {
            int row = convertRowIndexToModel(rowAtPoint(e.getPoint()));
            TableModel m = getModel();
            return String.format("%s, %s", m.getValueAt(row, 0), m.getValueAt(row, 2));
        }
    };

    public MainPanel() {
        super(new BorderLayout());
        for (int i = 0; i < 100; i++) {
            model.addRow(new Object[] {"Name " + i, i, Boolean.FALSE});
        }

        JScrollPane scroll = new JScrollPane(table);
//         {
//             @Override public JPopupMenu getComponentPopupMenu() {
//                 System.out.println("JScrollPane#getComponentPopupMenu");
//                 return check.isSelected()? null: super.getComponentPopupMenu();
//             }
//         };
        scroll.setComponentPopupMenu(new TablePopupMenu());
        table.setInheritsPopupMenu(true);
        table.setAutoCreateRowSorter(true);

        final DisableInputLayerUI layerUI = new DisableInputLayerUI();
        check.addItemListener(new ItemListener() {
            @Override public void itemStateChanged(ItemEvent ie) {
                layerUI.setLocked(((JCheckBox) ie.getItemSelectable()).isSelected());
            }
        });

        JLayer<JComponent> layer = new JLayer<>(scroll, layerUI);
        add(layer);
        add(check, BorderLayout.NORTH);
        setPreferredSize(new Dimension(320, 240));
    }

    class TestCreateAction extends AbstractAction {
        public TestCreateAction(String label, Icon icon) {
            super(label, icon);
        }
        @Override public void actionPerformed(ActionEvent e) {
            model.addRow(new Object[] {"New Name", 0, Boolean.FALSE});
            Rectangle rect = table.getCellRect(model.getRowCount() - 1, 0, true);
            table.scrollRectToVisible(rect);
        }
    }

    class DeleteAction extends AbstractAction {
        public DeleteAction(String label, Icon icon) {
            super(label, icon);
        }
        @Override public void actionPerformed(ActionEvent e) {
            int[] selection = table.getSelectedRows();
            for (int i = selection.length - 1; i >= 0; i--) {
                model.removeRow(table.convertRowIndexToModel(selection[i]));
            }
        }
    }

    private class TablePopupMenu extends JPopupMenu {
        private final Action createAction = new TestCreateAction("add", null);
        private final Action deleteAction = new DeleteAction("delete", null);
        public TablePopupMenu() {
            super();
            add(createAction);
            addSeparator();
            add(deleteAction);
        }
        @Override public void show(Component c, int x, int y) {
            createAction.setEnabled(!check.isSelected());
            deleteAction.setEnabled(table.getSelectedRowCount() > 0);
            super.show(c, x, y);
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
class DisableInputLayerUI extends LayerUI<JComponent> {
    private static final String CMD_REPAINT = "lock";
    private final transient MouseAdapter dummyMouseListener = new MouseAdapter() { /* Dummy listener */ };
    private boolean isBlocking;
    public void setLocked(boolean flag) {
        firePropertyChange(CMD_REPAINT, isBlocking, flag);
        isBlocking = flag;
    }
    @Override public void installUI(JComponent c) {
        super.installUI(c);
        if (c instanceof JLayer) {
            JLayer jlayer = (JLayer) c;
            jlayer.getGlassPane().addMouseListener(dummyMouseListener);
            jlayer.setLayerEventMask(AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK
                                   | AWTEvent.MOUSE_WHEEL_EVENT_MASK | AWTEvent.KEY_EVENT_MASK);
        }
    }
    @Override public void uninstallUI(JComponent c) {
        if (c instanceof JLayer) {
            JLayer jlayer = (JLayer) c;
            jlayer.setLayerEventMask(0);
            jlayer.getGlassPane().removeMouseListener(dummyMouseListener);
        }
        super.uninstallUI(c);
    }
    @Override public void eventDispatched(AWTEvent e, JLayer l) {
        if (isBlocking && e instanceof InputEvent) {
            ((InputEvent) e).consume();
        }
    }
    @Override public void applyPropertyChange(PropertyChangeEvent pce, JLayer l) {
        String cmd = pce.getPropertyName();
        if (CMD_REPAINT.equals(cmd)) {
            l.getGlassPane().setVisible((Boolean) pce.getNewValue());
        }
    }
}
