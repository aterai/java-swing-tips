package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;

public class MainPanel extends JPanel {
    private final TestModel model = new TestModel();
    private final JTable table = new JTable(model) {
        private final Color evenColor = new Color(240, 240, 250);
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

    private final JComboBox cb = new JComboBox(new String[] {"Name 0", "Name 1", "Name 2"});
    public MainPanel() {
        super(new BorderLayout());

        cb.setBorder(BorderFactory.createEmptyBorder());
        //((JTextField)cb.getEditor().getEditorComponent()).setBorder(null);
        //((JTextField)cb.getEditor().getEditorComponent()).setMargin(null);
        //cb.setEditable(true);

        TableColumn col = table.getColumnModel().getColumn(0);
        col.setMinWidth(60);
        col.setMaxWidth(60);
        col.setResizable(false);

        col = table.getColumnModel().getColumn(1);
        col.setCellRenderer(new ComboCellRenderer());
        col.setCellEditor(new DefaultCellEditor(cb));
        //table.setDefaultEditor(JComboBox.class, new DefaultCellEditor(tf));

        model.addTest(new Test("Name 1", "comment..."));
        model.addTest(new Test("Name 2", "Test"));
        model.addTest(new Test("Name d", ""));
        model.addTest(new Test("Name c", "Test cc"));
        model.addTest(new Test("Name b", "Test bb"));
        model.addTest(new Test("Name a", ""));
        model.addTest(new Test("Name 0", "Test aa"));

        table.setAutoCreateRowSorter(true);
        add(new JScrollPane(table));
        setPreferredSize(new Dimension(320, 180));
    }

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
class ComboCellRenderer extends JComboBox implements TableCellRenderer {
    private static final Color evenColor = new Color(240, 240, 250);
    private final JTextField editor;
    public ComboCellRenderer() {
        super();
        setEditable(true);
        setBorder(BorderFactory.createEmptyBorder());

        editor = (JTextField) getEditor().getEditorComponent();
        editor.setBorder(BorderFactory.createEmptyBorder());
        editor.setOpaque(true);
    }
    @Override public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus,
                                                   int row, int column) {
        removeAllItems();
        if(isSelected) {
            editor.setForeground(table.getSelectionForeground());
            editor.setBackground(table.getSelectionBackground());
        }else{
            editor.setForeground(table.getForeground());
            //setBackground(table.getBackground());
            editor.setBackground((row%2==0)?evenColor:table.getBackground());
        }
        addItem((value==null)?"":value.toString());
        return this;
    }
    //Overridden for performance reasons. ---->
    @Override public boolean isOpaque() {
        Color back = getBackground();
        Component p = getParent();
        if(p != null) {
            p = p.getParent();
        } // p should now be the JTable.
        boolean colorMatch = (back != null) && (p != null) && back.equals(p.getBackground()) && p.isOpaque();
        return !colorMatch && super.isOpaque();
    }
    @Override protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
//         System.out.println(propertyName);
//         if((propertyName == "font" || propertyName == "foreground") && oldValue != newValue) {
//             super.firePropertyChange(propertyName, oldValue, newValue);
//         }
    }
//     @Override public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {}
    @Override public void repaint(long tm, int x, int y, int width, int height) {}
    @Override public void repaint(Rectangle r) {}
    @Override public void repaint() {}
//     @Override public void invalidate() {}
//     @Override public void validate() {}
    @Override public void revalidate() {}
    //<---- Overridden for performance reasons.
}
