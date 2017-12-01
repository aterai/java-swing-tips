package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.util.*;
// import java.util.List;
import java.util.stream.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.*;
import javax.swing.plaf.basic.*;
import javax.swing.table.*;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());

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
        TableModel model = new DefaultTableModel(data, columnNames) {
            @Override public Class<?> getColumnClass(int column) {
                return getValueAt(0, column).getClass();
            }
            @Override public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new FileListTable(model);
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

class SelectedImageFilter extends RGBImageFilter {
    // public SelectedImageFilter() {
    //     canFilterIndexColorModel = false;
    // }
    @Override public int filterRGB(int x, int y, int argb) {
        int r = (argb >> 16) & 0xFF;
        int g = (argb >>  8) & 0xFF;
        return (argb & 0xFF0000FF) | ((r >> 1) << 16) | ((g >> 1) << 8);
        // return (argb & 0xFFFFFF00) | ((argb & 0xFF) >> 1);
    }
}

class FileNameRenderer implements TableCellRenderer {
    protected final Dimension dim = new Dimension();
    private final JPanel renderer = new JPanel(new BorderLayout());
    private final JLabel textLabel = new JLabel(" ");
    private final JLabel iconLabel;
    private final Border focusBorder = UIManager.getBorder("Table.focusCellHighlightBorder");
    private final Border noFocusBorder;
    private final ImageIcon nicon;
    private final ImageIcon sicon;

    protected FileNameRenderer(JTable table) {
        Border b = UIManager.getBorder("Table.noFocusBorder");
        if (Objects.isNull(b)) { // Nimbus???
            Insets i = focusBorder.getBorderInsets(textLabel);
            b = BorderFactory.createEmptyBorder(i.top, i.left, i.bottom, i.right);
        }
        noFocusBorder = b;

        JPanel p = new JPanel(new BorderLayout()) {
            @Override public Dimension getPreferredSize() {
                return dim;
            }
        };
        p.setOpaque(false);
        renderer.setOpaque(false);

        // http://www.icongalore.com/
        // XP Style Icons - Windows Application Icon, Software XP Icons
        nicon = new ImageIcon(getClass().getResource("wi0063-16.png"));
        sicon = new ImageIcon(p.createImage(new FilteredImageSource(nicon.getImage().getSource(), new SelectedImageFilter())));

        iconLabel = new JLabel(nicon);
        iconLabel.setBorder(BorderFactory.createEmptyBorder());

        p.add(iconLabel, BorderLayout.WEST);
        p.add(textLabel);
        renderer.add(p, BorderLayout.WEST);

        Dimension d = iconLabel.getPreferredSize();
        dim.setSize(d);
        table.setRowHeight(d.height);
    }
    @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        textLabel.setFont(table.getFont());
        textLabel.setText(Objects.toString(value, ""));
        textLabel.setBorder(hasFocus ? focusBorder : noFocusBorder);

        FontMetrics fm = table.getFontMetrics(table.getFont());
        Insets i = textLabel.getInsets();
        int swidth = iconLabel.getPreferredSize().width + fm.stringWidth(textLabel.getText()) + i.left + i.right;
        int cwidth = table.getColumnModel().getColumn(column).getWidth();
        dim.width = Math.min(swidth, cwidth);

        if (isSelected) {
            textLabel.setOpaque(true);
            textLabel.setForeground(table.getSelectionForeground());
            textLabel.setBackground(table.getSelectionBackground());
            iconLabel.setIcon(sicon);
        } else {
            textLabel.setOpaque(false);
            textLabel.setForeground(table.getForeground());
            textLabel.setBackground(table.getBackground());
            iconLabel.setIcon(nicon);
        }
        return renderer;
    }
}

// class FileNameRenderer extends JPanel implements TableCellRenderer {
//     private final MyLabel textLabel;
//     private final JLabel iconLabel;
//     private final Border focusBorder = UIManager.getBorder("Table.focusCellHighlightBorder");
//     private final Border noFocusBorder;
//     private final ImageIcon nicon;
//     private final ImageIcon sicon;
//
//     protected FileNameRenderer(JTable table) {
//         super(new BorderLayout());
//         Border b = UIManager.getBorder("Table.noFocusBorder");
//         if (Objects.isNull(b)) { // Nimbus???
//             Insets i = focusBorder.getBorderInsets(this);
//             b = BorderFactory.createEmptyBorder(i.top, i.left, i.bottom, i.right);
//         }
//         noFocusBorder = b;
//         setOpaque(false);
//         // http://www.icongalore.com/ XP Style Icons - Windows Application Icon, Software XP Icons
//         nicon = new ImageIcon(getClass().getResource("wi0063-16.png"));
//         sicon = new ImageIcon(createImage(new FilteredImageSource(nicon.getImage().getSource(), new SelectedImageFilter())));
//         textLabel = new MyLabel(new Color(~table.getSelectionBackground().getRGB()));
//         iconLabel = new JLabel(nicon);
//         iconLabel.setBorder(BorderFactory.createEmptyBorder());
//
//         add(iconLabel, BorderLayout.WEST);
//         add(textLabel);
//         table.setRowHeight(textLabel.getPreferredSize().height);
//     }
//     @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
//         textLabel.setFont(table.getFont());
//         textLabel.setText(Objects.toString(value, ""));
//         textLabel.setBorder(hasFocus ? focusBorder : noFocusBorder);
//
//         FontMetrics fm = table.getFontMetrics(table.getFont());
//         Insets i = textLabel.getInsets();
//         int swidth = fm.stringWidth(textLabel.getText()) + i.left + i.right;
//         int cwidth = table.getColumnModel().getColumn(column).getWidth() - iconLabel.getPreferredSize().width;
//         textLabel.setPreferredSize(new Dimension(swidth > cwidth ? cwidth : swidth, 10000)); // height: 10000 is dummy
//
//         if (isSelected) {
//             textLabel.setOpaque(true);
//             textLabel.setForeground(table.getSelectionForeground());
//             textLabel.setBackground(table.getSelectionBackground());
//             iconLabel.setIcon(sicon);
//         } else {
//             textLabel.setOpaque(false);
//             textLabel.setForeground(table.getForeground());
//             textLabel.setBackground(table.getBackground());
//             iconLabel.setIcon(nicon);
//         }
//         return this;
//     }
//     // Overridden for performance reasons. ---->
//     @Override public boolean isOpaque() {
//         Color back = getBackground();
//         Component p = getParent();
//         if (Objects.nonNull(p)) {
//             p = p.getParent();
//         } // p should now be the JTable.
//         boolean colorMatch = Objects.nonNull(back) && Objects.nonNull(p) && back.equals(p.getBackground()) && p.isOpaque();
//         return !colorMatch && super.isOpaque();
//     }
//     @Override protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) { /* Overridden for performance reasons. */ }
//     @Override public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue)  { /* Overridden for performance reasons. */ }
//     @Override public void repaint(long tm, int x, int y, int width, int height) { /* Overridden for performance reasons. */ }
//     @Override public void repaint(Rectangle r) { /* Overridden for performance reasons. */ }
//     @Override public void repaint()    { /* Overridden for performance reasons. */ }
//     @Override public void revalidate() { /* Overridden for performance reasons. */ }
//     // @Override public void invalidate() { /* Overridden for performance reasons. */ }
//     // @Override public void validate()   { /* Overridden for performance reasons. */ }
//     // <---- Overridden for performance reasons.
// }
//
// class MyLabel extends JLabel {
//     private final Border dotBorder;
//     private final Border empBorder = BorderFactory.createEmptyBorder(2, 2, 2, 2);
//     private boolean focusflag;
//
//     protected MyLabel(Color color) {
//         super("dummy");
//         setOpaque(true);
//         dotBorder = new DotBorder(color, 2);
//         setBorder(empBorder);
//         // setFocusable(true);
//     }
//     private boolean isFocusedBorder() {
//         return focusflag;
//     }
//     public void setFocusedBorder(boolean flag) {
//         setBorder(flag ? dotBorder : empBorder);
//         focusflag = flag;
//     }
//     private class DotBorder extends LineBorder {
//         protected DotBorder(Color color, int thickness) {
//             super(color, thickness);
//         }
//         @Override public boolean isBorderOpaque() {
//             return true;
//         }
//         @Override public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
//             Graphics2D g2 = (Graphics2D) g.create();
//             g2.translate(x, y);
//             if (isFocusedBorder()) {
//                 g2.setPaint(getLineColor());
//                 BasicGraphicsUtils.drawDashedRect(g2, 0, 0, w, h);
//             }
//             g2.dispose();
//         }
//     }
//     // Overridden for performance reasons. ---->
//     @Override public boolean isOpaque() {
//         Color back = getBackground();
//         Component p = getParent();
//         if (Objects.nonNull(p)) {
//             p = p.getParent();
//         } // p should now be the JTable.
//         boolean colorMatch = Objects.nonNull(back) && Objects.nonNull(p) && back.equals(p.getBackground()) && p.isOpaque();
//         return !colorMatch && super.isOpaque();
//     }
//     @Override protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
//         // System.out.println(propertyName);
// //      // String literal pool
// //      if (propertyName == "text" || propertyName == "labelFor" || propertyName == "displayedMnemonic"
// //          || ((propertyName == "font" || propertyName == "foreground") && oldValue != newValue && Objects.nonNull(getClientProperty(BasicHTML.propertyKey)))) {
//         if ("text".equals(propertyName) || "labelFor".equals(propertyName) || "displayedMnemonic".equals(propertyName)
//               || !Objects.equals(oldValue, newValue) && ("font".equals(propertyName) && Objects.nonNull(getClientProperty(BasicHTML.propertyKey)) || "foreground".equals(propertyName))) {
//             super.firePropertyChange(propertyName, oldValue, newValue);
//         }
//     }
//     @Override public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) { /* Overridden for performance reasons. */ }
//     @Override public void repaint(long tm, int x, int y, int width, int height) { /* Overridden for performance reasons. */ }
//     @Override public void repaint(Rectangle r) { /* Overridden for performance reasons. */ }
//     @Override public void repaint()    { /* Overridden for performance reasons. */ }
//     @Override public void invalidate() { /* Overridden for performance reasons. */ }
//     @Override public void validate()   { /* Overridden for performance reasons. */ }
//     @Override public void revalidate() { /* Overridden for performance reasons. */ }
//     //<---- Overridden for performance reasons.
// }

class FileListTable extends JTable {
    private static final AlphaComposite ALPHA = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .1f);
    private final Color rcolor = SystemColor.activeCaption;
    private final Color pcolor = makeColor(rcolor);
    private final Path2D rubberBand = new Path2D.Double();
    private transient RubberBandingListener rbl;

    protected FileListTable(TableModel model) {
        super(model);
    }
    @Override public void updateUI() {
        // [JDK-6788475] Changing to Nimbus LAF and back doesn't reset look and feel of JTable completely - Java Bug System
        // https://bugs.openjdk.java.net/browse/JDK-6788475
        // XXX: set dummy ColorUIResource
        setSelectionForeground(new ColorUIResource(Color.RED));
        setSelectionBackground(new ColorUIResource(Color.RED));
        removeMouseMotionListener(rbl);
        removeMouseListener(rbl);
        super.updateUI();
        rbl = new RubberBandingListener();
        addMouseMotionListener(rbl);
        addMouseListener(rbl);

        putClientProperty("Table.isFileList", Boolean.TRUE);
        setCellSelectionEnabled(true);
        setIntercellSpacing(new Dimension());
        setShowGrid(false);
        setAutoCreateRowSorter(true);
        setFillsViewportHeight(true);

        setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                return super.getTableCellRendererComponent(table, value, false, false, row, column);
            }
        });

        TableColumn col = getColumnModel().getColumn(0);
        col.setCellRenderer(new FileNameRenderer(this));
        col.setPreferredWidth(200);
        col = getColumnModel().getColumn(1);
        col.setPreferredWidth(300);
    }
    @Override public String getToolTipText(MouseEvent e) {
        Point pt = e.getPoint();
        int row  = rowAtPoint(pt);
        int col  = columnAtPoint(pt);
        if (convertColumnIndexToModel(col) != 0 || row < 0 || row > getRowCount()) {
            return null;
        }
        Rectangle rect = getCellRect2(this, row, col);
        if (rect.contains(pt)) {
            return getValueAt(row, col).toString();
        }
        return null;
    }
    @Override public void setColumnSelectionInterval(int index0, int index1) {
        int idx = convertColumnIndexToView(0);
        super.setColumnSelectionInterval(idx, idx);
    }
    protected Path2D getRubberBand() {
        return rubberBand;
    }
    private class RubberBandingListener extends MouseAdapter {
        private final Point srcPoint = new Point();
        @Override public void mouseDragged(MouseEvent e) {
            Point destPoint = e.getPoint();
            Path2D rubberBand = getRubberBand();
            rubberBand.reset();
            rubberBand.moveTo(srcPoint.x,  srcPoint.y);
            rubberBand.lineTo(destPoint.x, srcPoint.y);
            rubberBand.lineTo(destPoint.x, destPoint.y);
            rubberBand.lineTo(srcPoint.x,  destPoint.y);
            rubberBand.closePath();
            clearSelection();
            int col = convertColumnIndexToView(0);
            int[] indeces = IntStream.range(0, getModel().getRowCount()).filter(i -> rubberBand.intersects(getCellRect2(FileListTable.this, i, col))).toArray();
            for (int i: indeces) {
                addRowSelectionInterval(i, i);
                changeSelection(i, col, true, true);
            }
            repaint();
        }
        @Override public void mouseReleased(MouseEvent e) {
            getRubberBand().reset();
            repaint();
        }
        @Override public void mousePressed(MouseEvent e) {
            srcPoint.setLocation(e.getPoint());
            if (rowAtPoint(e.getPoint()) < 0) {
                clearSelection();
                repaint();
            } else {
                int index = rowAtPoint(e.getPoint());
                Rectangle rect = getCellRect2(FileListTable.this, index, convertColumnIndexToView(0));
                if (!rect.contains(e.getPoint())) {
                    clearSelection();
                    repaint();
                }
            }
        }
    }
    // SwingUtilities2.pointOutsidePrefSize(...)
    protected static Rectangle getCellRect2(JTable table, int row, int col) {
        TableCellRenderer tcr = table.getCellRenderer(row, col);
        Object value = table.getValueAt(row, col);
        Component cell = tcr.getTableCellRendererComponent(table, value, false, false, row, col);
        Dimension itemSize = cell.getPreferredSize();
        Rectangle cellBounds = table.getCellRect(row, col, false);
        cellBounds.width = itemSize.width;
        return cellBounds;
    }
    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setPaint(rcolor);
        g2.draw(rubberBand);
        g2.setComposite(ALPHA);
        g2.setPaint(pcolor);
        g2.fill(rubberBand);
        g2.dispose();
    }
//     private int[] getIntersectedIndices(Path2D path) {
//         TableModel model = getModel();
//         List<Integer> list = new ArrayList<>(model.getRowCount());
//         for (int i = 0; i < getRowCount(); i++) {
//             if (path.intersects(getCellRect2(FileListTable.this, i, convertColumnIndexToView(0)))) {
//                 list.add(i);
//             }
//         }
//         int[] il = new int[list.size()];
//         for (int i = 0; i < list.size(); i++) {
//             il[i] = list.get(i);
//         }
//         return il;
//     }
    private static Color makeColor(Color c) {
        int r = c.getRed();
        int g = c.getGreen();
        int b = c.getBlue();
        return r > g ? r > b ? new Color(r, 0, 0) : new Color(0, 0, b)
                     : g > b ? new Color(0, g, 0) : new Color(0, 0, b);
    }
}
