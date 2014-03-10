package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.font.*;
import java.awt.geom.*;
import java.util.Objects;
import javax.swing.*;
import javax.swing.table.*;

public final class MainPanel extends JPanel {
    private static final String STR0 = "Default Default Default Default";
    private static final String STR1 = "GlyphVector GlyphVector GlyphVector GlyphVector";
    private static final String STR2 = "JTextArea JTextArea JTextArea JTextArea";
    private static final String STR3 = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";

    private final String[] columnNames = {"Default", "GlyphVector", "JTextArea"};
    private final Object[][] data = {
        {STR0, STR1, STR2}, {STR0, STR1, STR2},
        {STR3, STR3, STR3}, {STR3, STR3, STR3}
    };
    private final DefaultTableModel model = new DefaultTableModel(data, columnNames) {
        @Override public Class<?> getColumnClass(int column) {
            return getValueAt(0, column).getClass();
        }
    };
    private final JTable table = new JTable(model);

    public MainPanel() {
        super(new BorderLayout());
        table.setRowSelectionAllowed(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        table.setRowHeight(50);

        JTableHeader tableHeader = table.getTableHeader();
        tableHeader.setReorderingAllowed(false);

        TableColumnModel mdl = table.getColumnModel();
        TableColumn col = mdl.getColumn(1);
        col.setCellRenderer(new TestRenderer());

        col = mdl.getColumn(2);
        col.setCellRenderer(new TextAreaCellRenderer());

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
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException |
                 IllegalAccessException | UnsupportedLookAndFeelException ex) {
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

class TestRenderer extends WrappedLabel implements TableCellRenderer {
    public TestRenderer() {
        super();
        setOpaque(true);
        setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
    }
    @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        if (isSelected) {
            setForeground(table.getSelectionForeground());
            setBackground(table.getSelectionBackground());
        } else {
            setForeground(table.getForeground());
            setBackground(table.getBackground());
        }
        setHorizontalAlignment(value instanceof Number ? RIGHT : LEFT);
        setFont(table.getFont());
        setText(Objects.toString(value, ""));
        return this;
    }
}

class WrappedLabel extends JLabel {
    private GlyphVector gvtext;
    public WrappedLabel() {
        this(null);
    }
    public WrappedLabel(String str) {
        super(str);
    }
    //private int prevwidth = -1;
    @Override public void doLayout() {
        Insets i = getInsets();
        int w = getWidth() - i.left - i.right;
        //if (w != prevwidth) {
            Font font = getFont();
            FontMetrics fm = getFontMetrics(font);
            FontRenderContext frc = fm.getFontRenderContext();
            gvtext = getWrappedGlyphVector(getText(), w, font, frc);
        //    prevwidth = w;
        //}
        super.doLayout();
    }
    @Override protected void paintComponent(Graphics g) {
        if (gvtext == null) {
            super.paintComponent(g);
        } else {
            Insets i = getInsets();
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setPaint(getBackground());
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.setPaint(getForeground());
            g2.drawGlyphVector(gvtext, i.left, getFont().getSize() + i.top);
            g2.dispose();
        }
    }
    private GlyphVector getWrappedGlyphVector(String str, float width, Font font, FontRenderContext frc) {
        Point2D gmPos    = new Point2D.Double(0.0d, 0.0d);
        GlyphVector gv   = font.createGlyphVector(frc, str);
        float lineheight = (float) (gv.getLogicalBounds().getHeight());
        float xpos       = 0f;
        float advance    = 0f;
        int   lineCount  = 0;
        GlyphMetrics gm;
        for (int i = 0; i < gv.getNumGlyphs(); i++) {
            gm = gv.getGlyphMetrics(i);
            advance = gm.getAdvance();
            if (xpos < width && width <= xpos + advance) {
                lineCount++;
                xpos = 0f;
            }
            gmPos.setLocation(xpos, lineheight * lineCount);
            gv.setGlyphPosition(i, gmPos);
            xpos = xpos + advance;
        }
        return gv;
    }
}

class TextAreaCellRenderer extends JTextArea implements TableCellRenderer {
    //public static class UIResource extends TextAreaCellRenderer implements UIResource {}
    public TextAreaCellRenderer() {
        super();
        setLineWrap(true);
        setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
        //setName("Table.cellRenderer");
    }
    @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        if (isSelected) {
            setForeground(table.getSelectionForeground());
            setBackground(table.getSelectionBackground());
        } else {
            setForeground(table.getForeground());
            setBackground(table.getBackground());
        }
        setFont(table.getFont());
        setText(Objects.toString(value, ""));
        return this;
    }
    //Overridden for performance reasons. ---->
    @Override public boolean isOpaque() {
        Color back = getBackground();
        Component p = getParent();
        if (p != null) {
            p = p.getParent();
        } // p should now be the JTable.
        boolean colorMatch = back != null && p != null && back.equals(p.getBackground()) && p.isOpaque();
        return !colorMatch && super.isOpaque();
    }
    @Override protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        //String literal pool
        //if (propertyName == "document" || ((propertyName == "font" || propertyName == "foreground") && oldValue != newValue)) {
        if ("document".equals(propertyName) || !Objects.equals(oldValue, newValue) && ("font".equals(propertyName) || "foreground".equals(propertyName))) {
            super.firePropertyChange(propertyName, oldValue, newValue);
        }
    }
    @Override public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) { /* Overridden for performance reasons. */ }
    @Override public void repaint(long tm, int x, int y, int width, int height) { /* Overridden for performance reasons. */ }
    @Override public void repaint(Rectangle r) { /* Overridden for performance reasons. */ }
    @Override public void repaint()    { /* Overridden for performance reasons. */ }
    @Override public void invalidate() { /* Overridden for performance reasons. */ }
    @Override public void validate()   { /* Overridden for performance reasons. */ }
    @Override public void revalidate() { /* Overridden for performance reasons. */ }
    //<---- Overridden for performance reasons.
}
