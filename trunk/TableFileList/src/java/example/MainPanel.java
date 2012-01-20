package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.plaf.basic.*;
import javax.swing.table.*;

public class MainPanel extends JPanel {
    String[] columnNames = {"Name", "Comment"};
    Object[][] data = {
        {"test1.jpg", "adfasd"},
        {"test1234.jpg", "  "},
        {"test15354.gif", "fasdf"},
        {"t.png", "comment"},
        {"tfasdfasd.jpg", "123"},
        {"afsdfasdfffffffffffasdfasdf.mpg", "test"},
        {"fffffffffffasdfasdf", ""},
        {"test1.jpg", ""}
    };
    DefaultTableModel model = new DefaultTableModel(data, columnNames) {
        @Override public Class<?> getColumnClass(int column) {
            return getValueAt(0, column).getClass();
        }
        @Override public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table  = new MyTable(model);
    public MainPanel() {
        super(new BorderLayout());
        table.putClientProperty("Table.isFileList", Boolean.TRUE);
//         MouseInputAdapter ma = new MouseInputAdapter() {
//             @Override public void mousePressed(MouseEvent e) {
//                 Point pt = e.getPoint();
//                 int row  = table.rowAtPoint(pt);
//                 int col  = table.columnAtPoint(pt);
//                 int mcol = table.convertColumnIndexToModel(col);
//                 if(mcol!=1 || row<0 || row>table.getRowCount()) return;
//                 if(!isOnLabel(table, pt, row, col)) {
//                     table.changeSelection(row, 0, false, false);
//                     table.clearSelection();
//                 }
//             }
//         };
//         table.addMouseListener(ma);
//         table.addMouseMotionListener(ma);
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                return super.getTableCellRendererComponent(table, value, false, false, row, column);
            }
        });
        //table.setRowSelectionAllowed(true);
        table.setCellSelectionEnabled(true);
        table.setIntercellSpacing(new Dimension());
        //table.setRowMargin(0);
        table.setShowGrid(false);
        //table.setShowHorizontalLines(false);
        //table.setShowVerticalLines(false);

        TableColumn col = table.getColumnModel().getColumn(0);
        col.setCellRenderer(new TestRenderer(table));
        col.setPreferredWidth(200);
        col = table.getColumnModel().getColumn(1);
        col.setPreferredWidth(300);

        final Color orgColor = table.getSelectionBackground();
        final Color tflColor = this.getBackground();
        table.addFocusListener(new FocusListener() {
            @Override public void focusGained(FocusEvent e) {
                table.setSelectionForeground(Color.WHITE);
                table.setSelectionBackground(orgColor);
            }
            @Override public void focusLost(FocusEvent e) {
                table.setSelectionForeground(Color.BLACK);
                table.setSelectionBackground(tflColor);
            }
        });

        table.setAutoCreateRowSorter(true);
        table.setFillsViewportHeight(true);
        //table.setComponentPopupMenu(new TablePopupMenu());
        add(new JScrollPane(table));
        setPreferredSize(new Dimension(320, 240));
    }
//     private static int getStringWidth(JTable table, int row, int column) {
//         FontMetrics fm = table.getFontMetrics(table.getFont());
//         Object o = table.getValueAt(row, column);
//         return fm.stringWidth(o.toString()) + ICON_SIZE + 2 + 2;
//     }
//     private static boolean isOnLabel(JTable table, Point pt, int row, int col) {
//         Rectangle rect = table.getCellRect(row, col, true);
//         rect.setSize(getStringWidth(table, row, col), rect.height);
//         return(rect.contains(pt));
//     }

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

class TestRenderer extends JPanel implements TableCellRenderer {
    private static class SelectedImageFilter extends RGBImageFilter {
        //public SelectedImageFilter() {
        //    canFilterIndexColorModel = true;
        //}
        @Override public int filterRGB(int x, int y, int argb) {
            int r = (argb >> 16) & 0xff;
            int g = (argb >>  8) & 0xff;
            return (argb & 0xff0000ff) | ((r >> 1) << 16) | ((g >> 1) << 8);
            //return (argb & 0xffffff00) | ((argb & 0xff) >> 1);
        }
    }
    private final ImageIcon nicon;
    private final ImageIcon sicon;
    private final MyLabel textLabel;
    private final JLabel iconLabel;
    public TestRenderer(JTable table) {
        super(new BorderLayout());
        setOpaque(false);
        //http://www.icongalore.com/ XP Style Icons - Windows Application Icon, Software XP Icons
        nicon = new ImageIcon(getClass().getResource("wi0063-16.png"));
        sicon = new ImageIcon(createImage(new FilteredImageSource(nicon.getImage().getSource(), new SelectedImageFilter())));
        textLabel = new MyLabel(new Color(~table.getSelectionBackground().getRGB()));
        iconLabel = new JLabel(nicon) {
            //Overridden for performance reasons. ---->
            @Override public boolean isOpaque() {
                Color back = getBackground();
                Component p = getParent();
                if(p != null) {
                    p = p.getParent();
                } // p should now be the JTable.
                boolean colorMatch = (back != null) && (p != null) && back.equals(p.getBackground()) && p.isOpaque();
                return false; //!colorMatch && super.isOpaque();
            }
            @Override protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
                //String literal pool
                //if(propertyName == "labelFor" || ((propertyName=="icon" || propertyName == "foreground") && oldValue != newValue)) {
                if("labelFor".equals(propertyName) || (("icon".equals(propertyName) || "foreground".equals(propertyName)) && oldValue != newValue)) {
                    //System.out.println(propertyName);
                    super.firePropertyChange(propertyName, oldValue, newValue);
                }
            }
            @Override public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {}
            @Override public void repaint(long tm, int x, int y, int width, int height) {}
            @Override public void repaint(Rectangle r) {}
            @Override public void repaint() {}
            @Override public void invalidate() {}
            @Override public void validate() {}
            @Override public void revalidate() {}
            //<---- Overridden for performance reasons.
        };
        iconLabel.setBorder(BorderFactory.createEmptyBorder());
        table.setRowHeight(textLabel.getPreferredSize().height);
        removeAll();
        add(iconLabel, BorderLayout.WEST);
        add(textLabel);
    }
    @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        textLabel.setText((value ==null)?"":value.toString());
        FontMetrics fm = table.getFontMetrics(table.getFont());
        int swidth = fm.stringWidth(textLabel.getText()) + textLabel.getInsets().left + textLabel.getInsets().right;
        int cwidth = table.getColumnModel().getColumn(column).getWidth()-iconLabel.getPreferredSize().width;
        textLabel.setPreferredSize(new Dimension((swidth>cwidth)?cwidth:swidth, 10000)); //height:10000 is dummy
        if(isSelected) {
            textLabel.setForeground(table.getSelectionForeground());
            textLabel.setBackground(table.getSelectionBackground());
        }else{
            textLabel.setForeground(table.getForeground());
            textLabel.setBackground(table.getBackground());
        }
        textLabel.setFocusedBorder(hasFocus);
        textLabel.setFont(table.getFont());
        iconLabel.setIcon((isSelected)?sicon:nicon);
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
    @Override protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {}
    @Override public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {}
    @Override public void repaint(long tm, int x, int y, int width, int height) {}
    @Override public void repaint(Rectangle r) {}
    @Override public void repaint() {}
    @Override public void revalidate() {}
    //@Override public void invalidate() {}
    //@Override public void validate() {}
    //<---- Overridden for performance reasons.
}

class MyLabel extends JLabel {
    private final Border dotBorder;
    private final Border empBorder = BorderFactory.createEmptyBorder(2,2,2,2);
    public MyLabel(Color color) {
        super("dummy");
        setOpaque(true);
        dotBorder = new DotBorder(color, 2);
        setBorder(empBorder);
        //setFocusable(true);
    }
    private boolean focusflag = false;
    private boolean isFocusedBorder() {
        return focusflag;
    }
    public void setFocusedBorder(boolean flag) {
        setBorder((flag)?dotBorder:empBorder);
        focusflag = flag;
    }
    private class DotBorder extends LineBorder {
        public DotBorder(Color color, int thickness) {
            super(color, thickness);
        }
        @Override public boolean isBorderOpaque() {return true;}
        @Override public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D)g;
            g2.translate(x,y);
            if(isFocusedBorder()) {
                g2.setPaint(getLineColor());
                BasicGraphicsUtils.drawDashedRect(g2, 0, 0, w, h);
            }
            g2.translate(-x,-y);
        }
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
        //System.out.println(propertyName);
//      //String literal pool
//      if(propertyName=="text" || propertyName == "labelFor" || propertyName == "displayedMnemonic"
//          || ((propertyName == "font" || propertyName == "foreground") && oldValue != newValue && getClientProperty(javax.swing.plaf.basic.BasicHTML.propertyKey) != null)) {
        if("text".equals(propertyName) || "labelFor".equals(propertyName) || "displayedMnemonic".equals(propertyName)
            || (("font".equals(propertyName) || "foreground".equals(propertyName)) && oldValue != newValue && getClientProperty(javax.swing.plaf.basic.BasicHTML.propertyKey) != null)) {
            super.firePropertyChange(propertyName, oldValue, newValue);
        }
    }
    @Override public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {}
    @Override public void repaint(long tm, int x, int y, int width, int height) {}
    @Override public void repaint(Rectangle r) {}
    @Override public void repaint() {}
    @Override public void invalidate() {}
    @Override public void validate() {}
    @Override public void revalidate() {}
    //<---- Overridden for performance reasons.
}
//
class MyTable extends JTable {
    private final Color rcolor;
    private final Color pcolor;
    private final AlphaComposite alcomp = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.1f);
    private final Path2D polygon = new Path2D.Double();
    private Point srcPoint = null;
    public MyTable(TableModel model) {
        super(model);
        rcolor = SystemColor.activeCaption;
        pcolor = makeColor(rcolor);
        RubberBandingListener rbl = new RubberBandingListener();
        addMouseMotionListener(rbl);
        addMouseListener(rbl);
    }
    @Override public String getToolTipText(MouseEvent e) {
        Point pt = e.getPoint();
        int row  = rowAtPoint(pt);
        int col  = columnAtPoint(pt);
        if(convertColumnIndexToModel(col)!=0 || row<0 || row>getRowCount()) return null;
        Rectangle rect = getCellRect2(this, row, col);
        if(rect.contains(pt)) {
            return getValueAt(row, col).toString();
        }
        return null;
    }
    @Override public void setColumnSelectionInterval(int index0, int index1) {
        int idx = convertColumnIndexToView(0);
        super.setColumnSelectionInterval(idx, idx);
    }
    class RubberBandingListener extends MouseAdapter {
        @Override public void mouseDragged(MouseEvent e) {
            if(srcPoint==null) srcPoint = e.getPoint();
            Point destPoint = e.getPoint();
            polygon.reset();
            polygon.moveTo(srcPoint.x,  srcPoint.y);
            polygon.lineTo(destPoint.x, srcPoint.y);
            polygon.lineTo(destPoint.x, destPoint.y);
            polygon.lineTo(srcPoint.x,  destPoint.y);
            polygon.closePath();
            clearSelection();
            int col = convertColumnIndexToView(0);
            for(int i:getIntersectedIndices(polygon)) {
                addRowSelectionInterval(i,i);
                changeSelection(i, col, true, true);
            }
            repaint();
        }
        @Override public void mouseReleased(MouseEvent e) {
            srcPoint = null;
            repaint();
        }
        @Override public void mousePressed(MouseEvent e) {
            if(rowAtPoint(e.getPoint())<0) {
                clearSelection();
                repaint();
            }else{
                int index = rowAtPoint(e.getPoint());
                Rectangle rect = getCellRect2(MyTable.this, index, convertColumnIndexToView(0));
                if(!rect.contains(e.getPoint())) {
                    clearSelection();
                    repaint();
                }
            }
        }
    }
    //SwingUtilities2.pointOutsidePrefSize(...)
    private static Rectangle getCellRect2(JTable table, int row, int col) {
        TableCellRenderer tcr = table.getCellRenderer(row, col);
        Object value = table.getValueAt(row, col);
        Component cell = tcr.getTableCellRendererComponent(table, value, false, false, row, col);
        Dimension itemSize = cell.getPreferredSize();
        Rectangle cellBounds = table.getCellRect(row, col, false);
        cellBounds.width = itemSize.width;
        return cellBounds;
//         FontMetrics fm = table.getFontMetrics(table.getFont());
//         Object o = table.getValueAt(row, col);
//         int w = fm.stringWidth(o.toString()) + 16 + 2 + 2;
//         Rectangle rect = table.getCellRect(row, col, true);
//         rect.setSize(w, rect.height);
//         return rect;
    }
    @Override public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if(srcPoint==null) return;
        Graphics2D g2d = (Graphics2D) g;
        g2d.setPaint(rcolor);
        g2d.draw(polygon);
        g2d.setComposite(alcomp);
        g2d.setPaint(pcolor);
        g2d.fill(polygon);
    }
    private int[] getIntersectedIndices(Path2D path) {
        TableModel model = getModel();
        Vector<Integer> list = new Vector<Integer>(model.getRowCount());
        int start = -1;
        int end = -1;
        for(int i=0;i<getRowCount();i++) {
            if(path.intersects(getCellRect2(MyTable.this, i, convertColumnIndexToView(0)))) {
                list.add(i);
            }
        }
        int[] il = new int[list.size()];
        for(int i=0;i<list.size();i++) {
            il[i] = list.get(i);
        }
        return il;
    }
    private Color makeColor(Color c) {
        int r = c.getRed();
        int g = c.getGreen();
        int b = c.getBlue();
        return (r>g)
          ?(r>b)?new Color(r,0,0):new Color(0,0,b)
          :(g>b)?new Color(0,g,0):new Color(0,0,b);
    }
}
