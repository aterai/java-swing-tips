package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
//import java.util.ArrayList;
import java.util.Arrays;
//import java.util.Hashtable;
import java.util.List;
import javax.swing.*;
import javax.swing.table.*;

public class MainPanel extends JPanel {
    private final BindingMapModel model = new BindingMapModel();
    private final JTable table = new JTable(model) {
        private final Color evenColor = new Color(250, 250, 250);
        @Override public Component prepareRenderer(TableCellRenderer tcr, int row, int column) {
            Component c = super.prepareRenderer(tcr, row, column);
            if(isRowSelected(row)) {
                c.setForeground(getSelectionForeground());
                c.setBackground(getSelectionBackground());
            }else{
                c.setForeground(getForeground());
                c.setBackground((row%2==0)?evenColor:getBackground());
            }
            return c;
        }
    };

    private static enum JComponentType {
        JComboBox(new JComboBox()),
        JFormattedTextField(new JFormattedTextField()),
        //JFileChooser(new JFileChooser()),
        JInternalFrame(new JInternalFrame()),
        JLabel(new JLabel()),
        JLayeredPane(new JLayeredPane()),
        JList(new JList()),
        JMenuBar(new JMenuBar()),
        JOptionPane(new JOptionPane()),
        JPanel(new JPanel()),
        JPopupMenu(new JPopupMenu()),
        JProgressBar(new JProgressBar()),
        JRootPane(new JRootPane()),
        JScrollBar(new JScrollBar()),
        JScrollPane(new JScrollPane()),
        JSeparator(new JSeparator()),
        JSlider(new JSlider()),
        JSpinner(new JSpinner()),
        JSplitPane(new JSplitPane()),
        JTabbedPane(new JTabbedPane()),
        JTable(new JTable()),
        JTableHeader(new JTableHeader()),
        JToolBar(new JToolBar()),
        JToolTip(new JToolTip()),
        JTree(new JTree()),
        JEditorPane(new JEditorPane()),
        JTextArea(new JTextArea()),
        JTextField(new JTextField());
        public final JComponent component;
        private JComponentType(JComponent component) {
            this.component = component;
        }
    }
    private final JComboBox componentChoices = makeComboBox(JComponentType.values());
    @SuppressWarnings("unchecked")
    private static JComboBox makeComboBox(Object[] model) {
        return new JComboBox(model);
    }

    private final List<Integer> focusType = Arrays.asList(
        JComponent.WHEN_FOCUSED, JComponent.WHEN_IN_FOCUSED_WINDOW,
        JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

    public MainPanel() {
        super(new BorderLayout());
        table.setAutoCreateRowSorter(true);
        JPanel p = new JPanel(new GridLayout(2,1,5,5));
        p.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        p.add(componentChoices);
        p.add(new JButton(new AbstractAction("show") {
            @Override public void actionPerformed(ActionEvent e) {
                model.setRowCount(0);
                JComponent c = ((JComponentType)componentChoices.getSelectedItem()).component;
                for(Integer f:focusType) {
                    loadBindingMap(f, c.getInputMap(f), c.getActionMap());
                }
            }
        }));
        add(p, BorderLayout.NORTH);
        add(new JScrollPane(table));
        setPreferredSize(new Dimension(320, 240));
    }
    // -------->
    // original code:
    // ftp://ftp.oreilly.de/pub/examples/english_examples/jswing2/code/goodies/Mapper.java
    // modified by terai
//     private Hashtable<Object, ArrayList<KeyStroke>> buildReverseMap(InputMap im) {
//         Hashtable<Object, ArrayList<KeyStroke>> h = new Hashtable<Object, ArrayList<KeyStroke>>();
//         if(im.allKeys()==null) { return h; }
//         for(KeyStroke ks:im.allKeys()) {
//             Object name = im.get(ks);
//             if(h.containsKey(name)) {
//                 h.get(name).add(ks);
//             }else{
//                 ArrayList<KeyStroke> keylist = new ArrayList<KeyStroke>();
//                 keylist.add(ks);
//                 h.put(name, keylist);
//             }
//         }
//         return h;
//     }
    private void loadBindingMap(Integer focusType, InputMap im, ActionMap am) {
        if(im.allKeys()==null) { return; }
        ActionMap tmpAm = new ActionMap();
        for(Object actionMapKey:am.allKeys()) {
            tmpAm.put(actionMapKey, am.get(actionMapKey));
        }
        for(KeyStroke ks:im.allKeys()) {
            Object actionMapKey = im.get(ks);
            Action action = am.get(actionMapKey);
            if(action!=null) {
                model.addBinding(new Binding(focusType, actionMapKey.toString(), ks.toString()));
            }else{
                model.addBinding(new Binding(focusType, "____"+actionMapKey.toString(), ks.toString()));
            }
            tmpAm.remove(actionMapKey);
        }
        if(tmpAm.allKeys()==null) { return; }
        for(Object actionMapKey:tmpAm.allKeys()) {
            model.addBinding(new Binding(focusType, actionMapKey.toString(), ""));
        }
    }
    // <--------

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                createAndShowGUI();
            }
        });
    }
    public static void createAndShowGUI() {
        try{
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }catch(Exception e) {
            e.printStackTrace();
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
    private static final ColumnContext[] columnArray = {
        new ColumnContext("Focus", String.class, false),
        new ColumnContext("ActionName", String.class, false),
        new ColumnContext("KeyDescription", String.class, false)
    };
    public void addBinding(Binding t) {
        Integer ft = t.getFocusType();
        String s = (ft==JComponent.WHEN_FOCUSED)?"WHEN_FOCUSED"
          :(ft==JComponent.WHEN_IN_FOCUSED_WINDOW)?"WHEN_IN_FOCUSED_WINDOW"
            :"WHEN_ANCESTOR_OF_FOCUSED_COMPONENT";
        Object[] obj = {s, t.getActionName(), t.getKeyDescription()};
        super.addRow(obj);
    }
    @Override public boolean isCellEditable(int row, int col) {
        return columnArray[col].isEditable;
    }
    @Override public Class<?> getColumnClass(int modelIndex) {
        return columnArray[modelIndex].columnClass;
    }
    @Override public int getColumnCount() {
        return columnArray.length;
    }
    @Override public String getColumnName(int modelIndex) {
        return columnArray[modelIndex].columnName;
    }
    private static class ColumnContext {
        public final String  columnName;
        public final Class   columnClass;
        public final boolean isEditable;
        public ColumnContext(String columnName, Class columnClass, boolean isEditable) {
            this.columnName = columnName;
            this.columnClass = columnClass;
            this.isEditable = isEditable;
        }
    }
}
class Binding {
    private Integer focusType;
    private String actionName, keyDescription;
    public Binding(Integer focusType, String actionName, String keyDescription) {
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
