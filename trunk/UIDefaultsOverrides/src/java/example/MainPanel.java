package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
// // JDK 1.6
// import com.sun.java.swing.*;
// import com.sun.java.swing.plaf.nimbus.*;
// JDK 1.7
import javax.swing.plaf.nimbus.*;

public class MainPanel extends JPanel {
    public MainPanel() {
        super(new GridLayout(2, 0));

        String[] columnNames = {"A", "B", "C"};
        Object[][] data = {
            { "A0, Line1\nA0, Line2\nA0, Line3", "B0, Line1\nB0, Line2", "C0, Line1" },
            { "A1, Line1", "B1, Line1\nB1, Line2", "C1, Line1" },
            { "A2, Line1", "B2, Line1", "C2, Line1" }
        };
        DefaultTableModel model = new DefaultTableModel(data, columnNames) {
            @Override public Class<?> getColumnClass(int column) {
                return getValueAt(0, column).getClass();
            }
            @Override public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table1 = new JTable(model);
        table1.setAutoCreateRowSorter(true);
        table1.setDefaultRenderer(String.class, new MultiLineTableCellRenderer());

        JTable table2 = new JTable(model);
        table2.setAutoCreateRowSorter(true);

        //http://docs.oracle.com/javase/tutorial/uiswing/lookandfeel/_nimbusDefaults.html
        UIDefaults d = new UIDefaults();
        d.put("TextArea.borderPainter", new Painter() {
            @Override public void paint(Graphics2D g, Object o, int w, int h) {}
        });
        MultiLineTableCellRenderer r = new MultiLineTableCellRenderer();
        r.putClientProperty("Nimbus.Overrides", d);
        r.putClientProperty("Nimbus.Overrides.InheritDefaults", false);

//         //or
//         d.put("TextArea.NotInScrollPane", new State("NotInScrollPane") {
//             @Override protected boolean isInState(JComponent c) {
//                 //@see javax.swing.plaf.nimbus.TextAreaNotInScrollPaneState
//                 //return !(c.getParent() instanceof JViewport);
//                 return false;
//             }
//         });
//         r.putClientProperty("Nimbus.Overrides", d);

        table2.setDefaultRenderer(String.class, r);

        add(new JScrollPane(table1));
        add(new JScrollPane(table2));
        setPreferredSize(new Dimension(320, 240));
    }
    private JCheckBoxMenuItem makeJCheckBoxMenuItem(String title, UIDefaults d) {
        JCheckBoxMenuItem cbmi = new JCheckBoxMenuItem(title);
        cbmi.putClientProperty("Nimbus.Overrides", d);
        cbmi.putClientProperty("Nimbus.Overrides.InheritDefaults", false);
        return cbmi;
    }
    public JMenuBar createMenuBar() {
        UIDefaults d = new UIDefaults();
        d.put("CheckBoxMenuItem[Enabled].checkIconPainter",
              new MyCheckBoxMenuItemPainter(
                  MyCheckBoxMenuItemPainter.CHECKICON_ENABLED));
        d.put("CheckBoxMenuItem[MouseOver].checkIconPainter",
              new MyCheckBoxMenuItemPainter(
                  MyCheckBoxMenuItemPainter.CHECKICON_MOUSEOVER));
        d.put("CheckBoxMenuItem[Enabled+Selected].checkIconPainter",
              new MyCheckBoxMenuItemPainter(
                  MyCheckBoxMenuItemPainter.CHECKICON_ENABLED_SELECTED));
        d.put("CheckBoxMenuItem[MouseOver+Selected].checkIconPainter",
              new MyCheckBoxMenuItemPainter(
                  MyCheckBoxMenuItemPainter.CHECKICON_SELECTED_MOUSEOVER));
        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("Menu");
        menuBar.add(menu);
        menu.add(new JCheckBoxMenuItem("Default"));
        menu.add(makeJCheckBoxMenuItem("Test1", d));
        menu.add(makeJCheckBoxMenuItem("Test2", d));
        menu.add(makeJCheckBoxMenuItem("Test3", d));
        JCheckBoxMenuItem cbmi;
        menu.add(cbmi = makeJCheckBoxMenuItem("Test4", d));
        cbmi.setSelected(true);
        cbmi.setEnabled(false);
        menu.add(cbmi = makeJCheckBoxMenuItem("Test5", d));
        cbmi.setSelected(false);
        cbmi.setEnabled(false);
        menuBar.add(menu);
        return menuBar;
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
            for(UIManager.LookAndFeelInfo laf: UIManager.getInstalledLookAndFeels()) {
                if("Nimbus".equals(laf.getName())) {
                    UIManager.setLookAndFeel(laf.getClassName());
                }
            }
        }catch(ClassNotFoundException | InstantiationException |
               IllegalAccessException | UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        }
        MainPanel p;
        JFrame frame = new JFrame("@title@");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(p = new MainPanel());
        frame.setJMenuBar(p.createMenuBar());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

//@see CheckBoxMenuItemPainter.java
class MyCheckBoxMenuItemPainter extends AbstractRegionPainter {
    static final int CHECKICON_ENABLED_SELECTED   = 6;
    static final int CHECKICON_SELECTED_MOUSEOVER = 7;
    static final int CHECKICON_ENABLED            = 8;
    static final int CHECKICON_MOUSEOVER          = 9;
    private int state;
    private PaintContext ctx;
    public MyCheckBoxMenuItemPainter(int state) {
        super();
        this.state = state;
        this.ctx = new AbstractRegionPainter.PaintContext(
            new Insets(5, 5, 5, 5), new Dimension(9, 10), false, null, 1.0, 1.0);
    }
    @Override protected void doPaint(Graphics2D g, JComponent c,
                                     int width, int height, Object[] eckey) {
        switch(state) {
          case CHECKICON_ENABLED:
            paintcheckIconEnabled(g);              break;
          case CHECKICON_MOUSEOVER:
            paintcheckIconMouseOver(g);            break;
          case CHECKICON_ENABLED_SELECTED:
            paintcheckIconEnabledAndSelected(g);   break;
          case CHECKICON_SELECTED_MOUSEOVER:
            paintcheckIconSelectedAndMouseOver(g); break;
          default:
            break;
        }
    }
    @Override protected final PaintContext getPaintContext() {
        return ctx;
    }
    private void paintcheckIconEnabled(Graphics2D g) {
        g.setPaint(Color.GREEN);
        g.drawOval( 0, 0, 10, 10 );
    }
    private void paintcheckIconMouseOver(Graphics2D g) {
        g.setPaint(Color.PINK);
        g.drawOval( 0, 0, 10, 10 );
    }
    private void paintcheckIconEnabledAndSelected(Graphics2D g) {
        g.setPaint(Color.ORANGE);
        g.fillOval( 0, 0, 10, 10 );
    }
    private void paintcheckIconSelectedAndMouseOver(Graphics2D g) {
        g.setPaint(Color.CYAN);
        g.fillOval( 0, 0, 10, 10 );
    }
}

class MultiLineTableCellRenderer extends JTextArea implements TableCellRenderer {
    private List<List<Integer>> rowColHeight = new ArrayList<List<Integer>>();
    private Border fhb; // = UIManager.getBorder("Table.focusCellHighlightBorder");
    private Border epb = BorderFactory.createEmptyBorder(2,5,2,5);
    public MultiLineTableCellRenderer() {
        super();

        //System.out.println(UIManager.get("nimbusFocus"));
        Border b = BorderFactory.createLineBorder(new Color(115,164,209));
        fhb = BorderFactory.createCompoundBorder(b, BorderFactory.createEmptyBorder(1,4,1,4));

        setLineWrap(true);
        setWrapStyleWord(true);
        setOpaque(true);
        setBorder(epb);
        //setMargin(new Insets(0,0,0,0));
    }
    @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        setFont(table.getFont());
        setText(Objects.toString(value, ""));
        setBorder(hasFocus ? fhb : epb);
        if(isSelected) {
            setForeground(table.getSelectionForeground());
            setBackground(table.getSelectionBackground());
        }else{
            setForeground(table.getForeground());
            setBackground(table.getBackground());
        }
        setBounds(table.getCellRect(row, column, false));
        int maxH = getAdjustedRowHeight(row, column);
        if(table.getRowHeight(row) != maxH) {
            table.setRowHeight(row, maxH);
        }
        return this;
    }

    /**
     * Calculate the new preferred height for a given row, and sets the height on the table.
     * http://blog.botunge.dk/post/2009/10/09/JTable-multiline-cell-renderer.aspx
     */
    private int getAdjustedRowHeight(int row, int column) {
        //The trick to get this to work properly is to set the width of the column to the
        //textarea. The reason for this is that getPreferredSize(), without a width tries
        //to place all the text in one line. By setting the size with the with of the column,
        //getPreferredSize() returnes the proper height which the row should have in
        //order to make room for the text.
        //int cWidth = table.getTableHeader().getColumnModel().getColumn(column).getWidth();
        //int cWidth = table.getCellRect(row, column, false).width; //Ignore IntercellSpacing
        //setSize(new Dimension(cWidth, 1000));

        int prefH = getPreferredSize().height;
        while(rowColHeight.size() <= row) {
            rowColHeight.add(new ArrayList<Integer>(column));
        }
        List<Integer> colHeights = rowColHeight.get(row);
        while(colHeights.size() <= column) {
            colHeights.add(0);
        }
        colHeights.set(column, prefH);
        int maxH = prefH;
        for(Integer colHeight : colHeights) {
            if(colHeight > maxH) {
                maxH = colHeight;
            }
        }
        return maxH;
    }
}
