package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.util.Objects;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

public class MainPanel extends JPanel {
    private final TestModel model = new TestModel();
    private final JTable table = new JTable(model);
    public MainPanel() {
        super(new BorderLayout());
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                return super.getTableCellRendererComponent(table, value, false, false, row, column);
            }
        });
        //table.setRowSelectionAllowed(true);
        //table.setCellSelectionEnabled(true);
        table.setIntercellSpacing(new Dimension());
        //table.setRowMargin(0);
        table.setShowGrid(false);
        //table.setShowHorizontalLines(false);
        //table.setShowVerticalLines(false);

        TableColumn col = table.getColumnModel().getColumn(0);
        col.setMinWidth(0);
        col.setMaxWidth(0);
        col.setResizable(false);
        //table.removeColumn(col);
        col = table.getColumnModel().getColumn(1);
        col.setCellRenderer(new TestRenderer(table));
        col.setPreferredWidth(200);
        col = table.getColumnModel().getColumn(2);
        col.setPreferredWidth(300);

        InputMap im = table.getInputMap(JTable.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        KeyStroke tab    = KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0);
        KeyStroke enter  = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
        KeyStroke stab   = KeyStroke.getKeyStroke(KeyEvent.VK_TAB, InputEvent.SHIFT_MASK);
        KeyStroke senter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.SHIFT_DOWN_MASK);
        im.put(tab, im.get(enter));
        im.put(stab, im.get(senter));

        model.addTest(new Test("test1.jpg", "adfasd"));
        model.addTest(new Test("test1234.jpg", "  "));
        model.addTest(new Test("test15354.gif", "fasdf"));
        model.addTest(new Test("t.png", "comment"));
        model.addTest(new Test("tfasdfasd.jpg", "123"));
        model.addTest(new Test("afsdfasdfffffffffffasdfasdf.mpg", "test"));
        model.addTest(new Test("fffffffffffasdfasdf", "456"));
        model.addTest(new Test("test1.jpg", "789"));

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
        table.setComponentPopupMenu(new TablePopupMenu());
        add(new JScrollPane(table));
        setPreferredSize(new Dimension(320, 240));
    }

    class TestCreateAction extends AbstractAction {
        public TestCreateAction(String label, Icon icon) {
            super(label,icon);
        }
        @Override public void actionPerformed(ActionEvent e) {
            model.addTest(new Test("new", ""));
            Rectangle rect = table.getCellRect(model.getRowCount()-1, 0, true);
            table.scrollRectToVisible(rect);
        }
    }

    class DeleteAction extends AbstractAction {
        public DeleteAction(String label, Icon icon) {
            super(label,icon);
        }
        @Override public void actionPerformed(ActionEvent e) {
            int[] selection = table.getSelectedRows();
            if(selection==null || selection.length<=0) { return; }
            for(int i=selection.length-1;i>=0;i--) {
                model.removeRow(table.convertRowIndexToModel(selection[i]));
            }
        }
    }

    class ClearAction extends AbstractAction {
        public ClearAction(String label, Icon icon) {
            super(label,icon);
        }
        @Override public void actionPerformed(ActionEvent evt) {
            table.clearSelection();
        }
    }
    private class TablePopupMenu extends JPopupMenu {
        private final Action deleteAction = new DeleteAction("delete", null);
        public TablePopupMenu() {
            super();
            add(new TestCreateAction("add", null));
            add(new ClearAction("clearSelection", null));
            addSeparator();
            add(deleteAction);
        }
        @Override public void show(Component c, int x, int y) {
            int[] l = table.getSelectedRows();
            deleteAction.setEnabled(l!=null && l.length>0);
            super.show(c, x, y);
        }
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
        }catch(ClassNotFoundException | InstantiationException |
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

class TestRenderer extends Box implements TableCellRenderer {
    private static final Border noFocusBorder = BorderFactory.createEmptyBorder(1,1,1,1);
    private final ImageIcon nicon;
    private final ImageIcon sicon;
    //private final DotLabel textLabel;
    private final JLabel textLabel;
    private final JLabel iconLabel;
    public TestRenderer(JTable table) {
        super(BoxLayout.X_AXIS);
        //textLabel = new DotLabel(new Color(~table.getSelectionBackground().getRGB()));
        textLabel = new JLabel("dummy");
        textLabel.setOpaque(true);
        textLabel.setBorder(noFocusBorder);
        //http://www.icongalore.com/ XP Style Icons - Windows Application Icon, Software XP Icons
        nicon = new ImageIcon(getClass().getResource("wi0063-16.png"));
        ImageProducer ip = new FilteredImageSource(nicon.getImage().getSource(), new SelectedImageFilter());
        sicon = new ImageIcon(createImage(ip));
        iconLabel = new JLabel(nicon);
        iconLabel.setBorder(BorderFactory.createEmptyBorder());
        table.setRowHeight(Math.max(textLabel.getPreferredSize().height,
                                    iconLabel.getPreferredSize().height));
        add(iconLabel);
        add(textLabel);
        add(Box.createHorizontalGlue());
    }
    @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        textLabel.setText(Objects.toString(value, ""));
        FontMetrics fm = table.getFontMetrics(table.getFont());
        int swidth = fm.stringWidth(textLabel.getText()) + textLabel.getInsets().left + textLabel.getInsets().right;
        int cwidth = table.getColumnModel().getColumn(column).getWidth()-iconLabel.getPreferredSize().width;
        textLabel.setPreferredSize(new Dimension(Math.min(swidth, cwidth), 0)); //height:0 is dummy
        if(isSelected) {
            textLabel.setForeground(table.getSelectionForeground());
            textLabel.setBackground(table.getSelectionBackground());
        }else{
            textLabel.setForeground(table.getForeground());
            textLabel.setBackground(table.getBackground());
        }
        if(hasFocus) {
            textLabel.setBorder(UIManager.getBorder("Table.focusCellHighlightBorder"));
        }else{
            textLabel.setBorder(noFocusBorder);
        }
        //textLabel.setFocusedBorder(hasFocus);
        textLabel.setFont(table.getFont());
        iconLabel.setIcon(isSelected?sicon:nicon);
        return this;
    }
    private static class SelectedImageFilter extends RGBImageFilter {
        //public SelectedImageFilter() {
        //    canFilterIndexColorModel = false;
        //}
        @Override public int filterRGB(int x, int y, int argb) {
            //Color color = new Color(argb,true);
            //float[] array = new float[4];
            //color.getComponents(array);
            //return new Color(array[0]*0.5f, array[1]*0.5f, array[2], array[3]).getRGB();
            int r = (argb >> 16) & 0xff;
            int g = (argb >>  8) & 0xff;
            return (argb & 0xff0000ff) | ((r >> 1) << 16) | ((g >> 1) << 8);
        }
    }
}
// class DotLabel extends JLabel {
//     private final Border dotBorder;
//     private final Border empBorder = BorderFactory.createEmptyBorder(2,2,2,2);
//     public DotLabel(Color color) {
//         super("dummy");
//         dotBorder = new DotBorder(color,2);
//         setOpaque(true);
//         setBorder(empBorder);
//         //setFocusable(true);
//     }
//     private boolean focusflag = false;
//     public boolean isFocusedBorder() {
//         return focusflag;
//     }
//     public void setFocusedBorder(boolean flag) {
//         setBorder(flag?dotBorder:empBorder);
//         focusflag = flag;
//     }
//     private class DotBorder extends LineBorder {
//         public DotBorder(Color color, int thickness) {
//             super(color, thickness);
//         }
//         @Override public boolean isBorderOpaque() { return true; }
//         @Override public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
//             Graphics2D g2 = (Graphics2D)g;
//             g2.translate(x,y);
//             if(isFocusedBorder()) {
//                 g2.setPaint(getLineColor());
//                 javax.swing.plaf.basic.BasicGraphicsUtils.drawDashedRect(g2, 0, 0, w, h);
//             }
//             g2.translate(-x,-y);
//         }
//     }
// }
class TestModel extends DefaultTableModel {
    private static final ColumnContext[] columnArray = {
        new ColumnContext("No.",     Integer.class, false),
        new ColumnContext("Name",    String.class,  false),
        new ColumnContext("Comment", String.class,  false)
    };
    private int number = 0;
    public void addTest(Test t) {
        Object[] obj = {number, t.getName(), t.getComment()};
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
    private String name, comment;
    public Test(String name, String comment) {
        this.name = name;
        this.comment = comment;
    }
    public void setName(String str) {
        name = str;
    }
    public void setComment(String str) {
        comment = str;
    }
    public String getName() {
        return name;
    }
    public String getComment() {
        return comment;
    }
}
