package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.*;

public final class MainPanel extends JPanel {
    private static final Color EVEN_COLOR = new Color(250, 250, 250);
    private final BindingMapModel model = new BindingMapModel();
    private final JTable table = new JTable(model) {
        @Override public Component prepareRenderer(TableCellRenderer tcr, int row, int column) {
            Component c = super.prepareRenderer(tcr, row, column);
            if (isRowSelected(row)) {
                c.setForeground(getSelectionForeground());
                c.setBackground(getSelectionBackground());
            } else {
                c.setForeground(getForeground());
                c.setBackground(row % 2 == 0 ? EVEN_COLOR : getBackground());
            }
            return c;
        }
    };
    private final JComponent[] list = {
        new JComboBox(),
        new JFormattedTextField(),
        //new JFileChooser(),
        new JInternalFrame(),
        new JLabel(),
        new JLayeredPane(),
        new JList(),
        new JMenuBar(),
        new JOptionPane(),
        new JPanel(),
        new JPopupMenu(),
        new JProgressBar(),
        new JRootPane(),
        new JScrollBar(),
        new JScrollPane(),
        new JSeparator(),
        new JSlider(),
        new JSpinner(),
        new JSplitPane(),
        new JTabbedPane(),
        new JTable(),
        new JTableHeader(),
        new JToolBar(),
        new JToolTip(),
        new JTree(),
        new JEditorPane(),
        new JTextArea(),
        new JTextField()
    };
    private final JComboBox<JComponent> componentChoices = new JComboBox<>(list);
    private final JButton button = new JButton("show");
    private final List<Integer> focusType = Arrays.asList(
        JComponent.WHEN_FOCUSED, JComponent.WHEN_IN_FOCUSED_WINDOW,
        JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

    public MainPanel() {
        super(new BorderLayout());
        table.setAutoCreateRowSorter(true);
        componentChoices.setRenderer(new ListCellRenderer<JComponent>() {
            private final JLabel l = new JLabel();
            @Override public Component getListCellRendererComponent(JList<? extends JComponent> list, JComponent value, int index, boolean isSelected, boolean cellHasFocus) {
                l.setOpaque(index >= 0);
                l.setText(value.getClass().getName());
                if (isSelected) {
                    l.setBackground(list.getSelectionBackground());
                    l.setForeground(list.getSelectionForeground());
                } else {
                    l.setBackground(list.getBackground());
                    l.setForeground(list.getForeground());
                }
                return l;
            }
        });
        button.addActionListener(e -> {
            model.setRowCount(0);
            JComponent c = componentChoices.getItemAt(componentChoices.getSelectedIndex());
            for (Integer f: focusType) {
                loadBindingMap(f, c.getInputMap(f), c.getActionMap());
            }
        });
        JPanel p = new JPanel(new GridLayout(2, 1, 5, 5));
        p.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        p.add(componentChoices);
        p.add(button);
        add(p, BorderLayout.NORTH);
        add(new JScrollPane(table));
        setPreferredSize(new Dimension(320, 240));
    }
    // -------->
    // original code:
    // ftp://ftp.oreilly.de/pub/examples/english_examples/jswing2/code/goodies/Mapper.java
    // modified by terai
//     private Hashtable<Object, ArrayList<KeyStroke>> buildReverseMap(InputMap im) {
//         Hashtable<Object, ArrayList<KeyStroke>> h = new Hashtable<>();
//         if (Objects.isNull(im.allKeys())) {
//             return h;
//         }
//         for (KeyStroke ks: im.allKeys()) {
//             Object name = im.get(ks);
//             if (h.containsKey(name)) {
//                 h.get(name).add(ks);
//             } else {
//                 ArrayList<KeyStroke> keylist = new ArrayList<>();
//                 keylist.add(ks);
//                 h.put(name, keylist);
//             }
//         }
//         return h;
//     }
    private void loadBindingMap(Integer focusType, InputMap im, ActionMap am) {
        if (Objects.isNull(im.allKeys())) {
            return;
        }
        ActionMap tmpAm = new ActionMap();
        for (Object actionMapKey: am.allKeys()) {
            tmpAm.put(actionMapKey, am.get(actionMapKey));
        }
        for (KeyStroke ks: im.allKeys()) {
            Object actionMapKey = im.get(ks);
            Action action = am.get(actionMapKey);
            if (Objects.isNull(action)) {
                model.addBinding(new Binding(focusType, "____" + actionMapKey.toString(), ks.toString()));
            } else {
                model.addBinding(new Binding(focusType, actionMapKey.toString(), ks.toString()));
            }
            tmpAm.remove(actionMapKey);
        }
        if (Objects.isNull(tmpAm.allKeys())) {
            return;
        }
        for (Object actionMapKey: tmpAm.allKeys()) {
            model.addBinding(new Binding(focusType, actionMapKey.toString(), ""));
        }
    }
    // <--------

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

class BindingMapModel extends DefaultTableModel {
    private static final ColumnContext[] COLUMN_ARRAY = {
        new ColumnContext("Focus", String.class, false),
        new ColumnContext("ActionName", String.class, false),
        new ColumnContext("KeyDescription", String.class, false)
    };
    public void addBinding(Binding t) {
        Integer ft = t.getFocusType();
        String s = (ft == JComponent.WHEN_FOCUSED)           ? "WHEN_FOCUSED"
                 : (ft == JComponent.WHEN_IN_FOCUSED_WINDOW) ? "WHEN_IN_FOCUSED_WINDOW"
                                                             : "WHEN_ANCESTOR_OF_FOCUSED_COMPONENT";
        Object[] obj = {s, t.getActionName(), t.getKeyDescription()};
        super.addRow(obj);
    }
    @Override public boolean isCellEditable(int row, int col) {
        return COLUMN_ARRAY[col].isEditable;
    }
    @Override public Class<?> getColumnClass(int column) {
        return COLUMN_ARRAY[column].columnClass;
    }
    @Override public int getColumnCount() {
        return COLUMN_ARRAY.length;
    }
    @Override public String getColumnName(int column) {
        return COLUMN_ARRAY[column].columnName;
    }
    private static class ColumnContext {
        public final String  columnName;
        public final Class   columnClass;
        public final boolean isEditable;
        protected ColumnContext(String columnName, Class columnClass, boolean isEditable) {
            this.columnName = columnName;
            this.columnClass = columnClass;
            this.isEditable = isEditable;
        }
    }
}

class Binding {
    private Integer focusType;
    private String actionName, keyDescription;
    protected Binding(Integer focusType, String actionName, String keyDescription) {
        this.focusType = focusType;
        this.actionName = actionName;
        this.keyDescription = keyDescription;
    }
    public void setFocusType(Integer focus) {
        focusType = focus;
    }
    public void setActionName(String str) {
        actionName = str;
    }
    public void setKeyDescription(String str) {
        keyDescription = str;
    }
    public Integer getFocusType() {
        return focusType;
    }
    public String getActionName() {
        return actionName;
    }
    public String getKeyDescription() {
        return keyDescription;
    }
}
