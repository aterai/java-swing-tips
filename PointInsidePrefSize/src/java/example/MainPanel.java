package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.net.*;
import javax.swing.*;
import javax.swing.table.*;

public class MainPanel extends JPanel {
    private static final Color evenColor = new Color(250, 250, 250);
    private final TestModel model = new TestModel();
    public MainPanel() {
        super(new BorderLayout());
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

        URLRenderer renderer = new URLRenderer();
        table.setDefaultRenderer(URL.class, renderer);
        table.addMouseListener(renderer);
        table.addMouseMotionListener(renderer);

        col = table.getColumnModel().getColumn(1);
        col.setPreferredWidth(1000);

        col = table.getColumnModel().getColumn(2);
        //col.setCellRenderer(renderer);
        col.setPreferredWidth(2000);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(Color.WHITE);
        add(scrollPane);
        setPreferredSize(new Dimension(320, 240));
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
    private boolean isRollover = false;
    @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        super.getTableCellRendererComponent(table, value, isSelected, false, row, column);

        int mw = table.getColumnModel().getColumnMargin();
        int rh = table.getRowMargin();
        int w  = table.getColumnModel().getColumn(column).getWidth();
        int h  = table.getRowHeight(row);

        //TEST: this.setBorder(BorderFactory.createMatteBorder(0,16,0,16,Color.RED));
        Insets i = this.getInsets();
        lrect.x = i.left;
        lrect.y = i.top;
        lrect.width  = w - mw - i.right  - lrect.x;
        lrect.height = h - rh - i.bottom - lrect.y;
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
        //String str = value!=null?value.toString():"";

        if(!table.isEditing() && this.row==row && this.col==column && this.isRollover) {
            setText("<html><u><font color='blue'>"+str);
//         }else if(hasFocus) {
//             setText("<html><font color='blue'>"+str);
        }else{
            setText(str);
        }
        return this;
    }
    //@see SwingUtilities2.pointOutsidePrefSize(...)
    private static boolean pointInsidePrefSize(JTable table, Point p) {
        int row = table.rowAtPoint(p);
        int col = table.columnAtPoint(p);
        TableCellRenderer tcr = table.getCellRenderer(row, col);
        Object value = table.getValueAt(row, col);
        Component cell = tcr.getTableCellRendererComponent(table, value, false, false, row, col);
        Dimension itemSize = cell.getPreferredSize();
        Insets i = ((JComponent)cell).getInsets();
        Rectangle cellBounds = table.getCellRect(row, col, false);
        cellBounds.width = itemSize.width-i.right-i.left;
        cellBounds.translate(i.left, i.top);
        return cellBounds.contains(p);
    }
    private static boolean isURLColumn(JTable table, int column) {
        return column>=0 && table.getColumnClass(column).equals(URL.class);
    }
    @Override public void mouseMoved(MouseEvent e) {
        JTable table = (JTable)e.getSource();
        Point pt = e.getPoint();
        int prev_row = row;
        int prev_col = col;
        boolean prev_ro = isRollover;
        row = table.rowAtPoint(pt);
        col = table.columnAtPoint(pt);
        isRollover = isURLColumn(table, col) && pointInsidePrefSize(table, pt);
        if(row==prev_row && col==prev_col && isRollover==prev_ro || !isRollover && !prev_ro) {
            return;
        }

// >>>> HyperlinkCellRenderer.java
// @see http://java.net/projects/swingset3/sources/svn/content/trunk/SwingSet3/src/com/sun/swingset3/demos/table/HyperlinkCellRenderer.java
        Rectangle repaintRect;
        if(isRollover) {
            Rectangle r = table.getCellRect(row, col, false);
            repaintRect = prev_ro ? r.union(table.getCellRect(prev_row, prev_col, false)) : r;
        }else{ //if(prev_ro) {
            repaintRect = table.getCellRect(prev_row, prev_col, false);
        }
        table.repaint(repaintRect);
// <<<<
        //table.repaint();
    }
    @Override public void mouseExited(MouseEvent e)  {
        JTable table = (JTable)e.getSource();
        if(isURLColumn(table, col)) {
            table.repaint(table.getCellRect(row, col, false));
            row = -1;
            col = -1;
            isRollover = false;
        }
    }
    @Override public void mouseClicked(MouseEvent e) {
        JTable table = (JTable)e.getSource();
        Point pt = e.getPoint();
        int ccol = table.columnAtPoint(pt);
        if(isURLColumn(table, ccol) && pointInsidePrefSize(table, pt)) {
            int crow = table.rowAtPoint(pt);
            URL url = (URL)table.getValueAt(crow, ccol);
            System.out.println(url);
            try{
                //Web Start
                //BasicService bs = (BasicService)ServiceManager.lookup("javax.jnlp.BasicService");
                //bs.showDocument(url);
                if(Desktop.isDesktopSupported()) { // JDK 1.6.0
                    Desktop.getDesktop().browse(url.toURI());
                }
            }catch(Exception ex) {
                ex.printStackTrace();
            }
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

class Test {
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
