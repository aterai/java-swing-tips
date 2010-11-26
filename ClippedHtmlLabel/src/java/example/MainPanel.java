package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.net.*;
import javax.swing.*;
import javax.swing.table.*;

public class MainPanel extends JPanel {
    public MainPanel() {
        super(new BorderLayout());
        JSplitPane sp = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        sp.setTopComponent(new JScrollPane(makeTable(new URLRenderer1())));
        sp.setBottomComponent(new JScrollPane(makeTable(new URLRenderer())));
        sp.setResizeWeight(0.5);
        add(sp);
        setPreferredSize(new Dimension(320, 240));
    }
    private static final Color evenColor = new Color(250, 250, 250);
    private static JTable makeTable(URLRenderer renderer) {
        TestModel model = new TestModel();
        try{
            model.addTest(new Test("FrontPage", new URL("http://terai.xrea.jp/")));
            model.addTest(new Test("Java Swing Tips", new URL("http://terai.xrea.jp/Swing.html")));
            model.addTest(new Test("Example", new URL("http://www.example.com/")));
            model.addTest(new Test("Example.jp", new URL("http://www.example.jp/")));
        }catch(Exception ex) {
            ex.printStackTrace();
        }
        JTable table = new JTable(model) {
            @Override public Component prepareRenderer(TableCellRenderer tcr, int row, int column) {
                Component c = super.prepareRenderer(tcr, row, column);
                c.setForeground(getForeground());
                c.setBackground((row%2==0)?evenColor:getBackground());
                return c;
            }
        };
        table.setRowSelectionAllowed(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        table.setIntercellSpacing(new Dimension());
        table.setShowGrid(false);
        table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        table.setAutoCreateRowSorter(true);

        TableColumn col = table.getColumnModel().getColumn(0);
        col.setMinWidth(50);
        col.setMaxWidth(50);
        col.setResizable(false);

        table.setDefaultRenderer(URL.class, renderer);
        table.addMouseListener(renderer);
        table.addMouseMotionListener(renderer);

        return table;
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

class URLRenderer extends DefaultTableCellRenderer implements MouseListener, MouseMotionListener {
    private static Rectangle lrect = new Rectangle();
    private static Rectangle irect = new Rectangle();
    private static Rectangle trect = new Rectangle();
    private int row = -1;
    private int col = -1;
    @Override public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus,
                                                   int row, int column) {
        super.getTableCellRendererComponent(table, value, isSelected, false, row, column);

        int mw = table.getColumnModel().getColumnMargin();
        int rh = table.getRowMargin();
        int w  = table.getColumnModel().getColumn(column).getWidth();
        int h  = table.getRowHeight(row);

        Insets i = this.getInsets();
        lrect.x = i.left;
        lrect.y = i.top;
        lrect.width  = w - (mw + i.right  + lrect.x);
        lrect.height = h - (rh + i.bottom + lrect.y);
        irect.x = irect.y = irect.width = irect.height = 0;
        trect.x = trect.y = trect.width = trect.height = 0;

        String str = SwingUtilities.layoutCompoundLabel(
            this,
            this.getFontMetrics(this.getFont()),
            value.toString(), //this.getText(),
            this.getIcon(),
            this.getVerticalAlignment(),
            this.getHorizontalAlignment(),
            this.getVerticalTextPosition(),
            this.getHorizontalTextPosition(),
            lrect,
            irect, //icon
            trect, //text
            this.getIconTextGap());

        if(!table.isEditing() && this.row==row && this.col==column) {
            setText("<html><u><font color='blue'>"+str);
        }else if(hasFocus) {
            setText("<html><font color='blue'>"+str);
        }else{
            setText(str);
            //setText(value.toString());
        }
        return this;
    }
    @Override public void mouseMoved(MouseEvent e) {
        JTable table = (JTable)e.getSource();
        Point pt = e.getPoint();
        row = table.rowAtPoint(pt);
        col = table.columnAtPoint(pt);
        if(row<0 || col<0) {
            row = -1;
            col = -1;
        }
        table.repaint();
    }
    @Override public void mouseExited(MouseEvent e)  {
        JTable table = (JTable)e.getSource();
        row = -1;
        col = -1;
        table.repaint();
    }
    @Override public void mouseClicked(MouseEvent e) {
        JTable table = (JTable)e.getSource();
        Point pt = e.getPoint();
        int crow = table.rowAtPoint(pt);
        int ccol = table.columnAtPoint(pt);
        if(table.getColumnClass(ccol).equals(URL.class)) {
            URL url = (URL)table.getValueAt(crow, ccol);
            System.out.println(url);
            //try{
            //  Desktop.getDesktop().browse(url.toURI());
            //}catch(Exception ex) {
            //  ex.printStackTrace();
            //}
        }
    }
    @Override public void mouseDragged(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mousePressed(MouseEvent e) {}
    @Override public void mouseReleased(MouseEvent e) {}
}

class URLRenderer1 extends URLRenderer { //DefaultTableCellRenderer implements MouseListener, MouseMotionListener {
    private int row = -1;
    private int col = -1;
    @Override public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus,
                                                   int row, int column) {
        super.getTableCellRendererComponent(table, value, isSelected, false, row, column);
        if(!table.isEditing() && this.row==row && this.col==column) {
            setText("<html><u><font color='blue'>"+value.toString());
        }else if(hasFocus) {
            setText("<html><font color='blue'>"+value.toString());
        }else{
            setText(value.toString());
        }
        return this;
    }
    @Override public void mouseMoved(MouseEvent e) {
        JTable table = (JTable)e.getSource();
        Point pt = e.getPoint();
        row = table.rowAtPoint(pt);
        col = table.columnAtPoint(pt);
        if(row<0 || col<0) {
            row = -1;
            col = -1;
        }
        table.repaint();
    }
    @Override public void mouseExited(MouseEvent e)  {
        JTable table = (JTable)e.getSource();
        row = -1;
        col = -1;
        table.repaint();
    }
    @Override public void mouseClicked(MouseEvent e) {
        JTable table = (JTable)e.getSource();
        Point pt = e.getPoint();
        int crow = table.rowAtPoint(pt);
        int ccol = table.columnAtPoint(pt);
        if(table.getColumnClass(ccol).equals(URL.class)) {
            URL url = (URL)table.getValueAt(crow, ccol);
            System.out.println(url);
            //try{
            //  Desktop.getDesktop().browse(url.toURI());
            //}catch(Exception ex) {
            //  ex.printStackTrace();
            //}
        }
    }
    @Override public void mouseDragged(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mousePressed(MouseEvent e) {}
    @Override public void mouseReleased(MouseEvent e) {}
}

class TestModel extends DefaultTableModel {
    private static final ColumnContext[] columnArray = {
        new ColumnContext("No.",  Integer.class, false),
        new ColumnContext("Name", String.class, false),
        new ColumnContext("URL",  URL.class, false)
    };
    private int number = 0;
    public void addTest(Test t) {
        Object[] obj = {number, t.getName(), t.getURL()};
        super.addRow(obj);
        number++;
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

class Test{
    private String name;
    private URL url;
    public Test(String name, URL url) {
        this.name = name;
        this.url = url;
    }
    public void setName(String str) {
        this.name = str;
    }
    public void setURL(URL url) {
        this.url = url;
    }
    public String getName() {
        return name;
    }
    public URL getURL() {
        return url;
    }
}
