package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.geom.*;
import java.awt.font.*;
import javax.swing.*;
import javax.swing.table.*;

public class MainPanel extends JPanel{
    public MainPanel() {
        super(new BorderLayout());

        TestModel model = new TestModel();
        JTable table = new JTable(model);
        table.setRowSelectionAllowed(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        table.setRowHeight(50);

        JTableHeader tableHeader = table.getTableHeader();
        tableHeader.setReorderingAllowed(false);

        TableColumnModel mdl = table.getColumnModel();
        TableColumn col = mdl.getColumn(0);
        col.setMinWidth(50);
        col.setMaxWidth(50);
        col.setResizable(false);

        col = mdl.getColumn(1);
        col.setCellRenderer(new TestRenderer());

        col = mdl.getColumn(2);
        col.setCellRenderer(new TextAreaCellRenderer());

        model.addTest(new Test("GlyphVector GlyphVector GlyphVector GlyphVector",
                               "JTextArea JTextArea JTextArea JTextArea JTextArea"));
        model.addTest(new Test("asdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfas",
                               "asdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfas"));
        model.addTest(new Test("0123456789 0123456789 0123456789 0123456789",
                               "0123456789 0123456789 0123456789 0123456789"));
        model.addTest(new Test("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
                               "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"));

        add(new JScrollPane(table));
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

class TestRenderer extends MyJLabel implements TableCellRenderer {
    public TestRenderer() {
        super();
        setOpaque(true);
        setBorder(BorderFactory.createEmptyBorder(0,5,0,5));
    }
    @Override public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus,
                                                   int row, int column) {
        if(isSelected) {
            setForeground(table.getSelectionForeground());
            setBackground(table.getSelectionBackground());
        }else{
            setForeground(table.getForeground());
            setBackground(table.getBackground());
        }
        setHorizontalAlignment((value instanceof Number)?RIGHT:LEFT);
        setFont(table.getFont());
        setText((value ==null) ? "" : value.toString());
        return this;
    }
}

class MyJLabel extends JLabel {
    private GlyphVector gvtext;
    public MyJLabel() {super();}
    @Override protected void paintComponent(Graphics g) {
        //super.paintComponent(g);
        Graphics2D g2 = (Graphics2D)g;
        int WRAPPING_WIDTH = getWidth()-getInsets().left-getInsets().right;
        FontRenderContext frc = g2.getFontRenderContext();
        gvtext = getWrappedGlyphVector(getText(), WRAPPING_WIDTH, getFont(), frc);
        g2.setPaint(getBackground());
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.setPaint(getForeground());
        g2.drawGlyphVector(gvtext, getInsets().left, getFont().getSize()+getInsets().top);
    }
    private GlyphVector getWrappedGlyphVector(String str, float Wrapping, Font font, FontRenderContext frc) {
        Point2D gmPos    = new Point2D.Double(0.0d, 0.0d);
        GlyphVector gv   = font.createGlyphVector(frc, str);
        float lineheight = (float) (gv.getLogicalBounds().getHeight());
        float xpos       = 0.0f;
        float advance    = 0.0f;
        int   lineCount  = 0;
        GlyphMetrics gm;
        for(int i=0;i<gv.getNumGlyphs();i++) {
            gm = gv.getGlyphMetrics(i);
            advance = gm.getAdvance();
            if(xpos<Wrapping && Wrapping<=xpos+advance) {
                lineCount++;
                xpos = 0.0f;
            }
            gmPos.setLocation(xpos, lineheight*lineCount);
            gv.setGlyphPosition(i, gmPos);
            xpos = xpos + advance;
        }
        return gv;
    }
}

class TextAreaCellRenderer extends JTextArea implements TableCellRenderer {
    //public static class UIResource extends TextAreaCellRenderer implements javax.swing.plaf.UIResource {}
    public TextAreaCellRenderer() {
        super();
        setLineWrap(true);
        setBorder(BorderFactory.createEmptyBorder(0,5,0,5));
        //setName("Table.cellRenderer");
    }
    @Override public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus,
                                                   int row, int column) {
        if(isSelected) {
            setForeground(table.getSelectionForeground());
            setBackground(table.getSelectionBackground());
        }else{
            setForeground(table.getForeground());
            setBackground(table.getBackground());
        }
        setFont(table.getFont());
        setText((value ==null) ? "" : value.toString());
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
        //String literal pool
        //if(propertyName=="document" || ((propertyName == "font" || propertyName == "foreground") && oldValue != newValue)) {
        if("document".equals(propertyName) || (("font".equals(propertyName) || "foreground".equals(propertyName)) && oldValue != newValue)) {
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
