package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.awt.font.*;
import java.awt.geom.*;
import java.util.Objects;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

public final class MainPanel extends JPanel {
    private static final Color EVEN_COLOR = new Color(245, 245, 255);
    private final String[] columnNames = {"Justified", "Default"};
    private final Object[][] data = {
        {"会社名", ""},
        {"所在地", ""},
        {"電話番号", ""},
        {"設立", ""},
        {"代表取締役", ""},
        {"事業内容", ""}
    };
    private final DefaultTableModel model = new DefaultTableModel(data, columnNames);
    private final JTable table = new JTable(model) {
        @Override public Component prepareRenderer(TableCellRenderer tcr, int row, int column) {
            Component c = super.prepareRenderer(tcr, row, column);
            if (isRowSelected(row)) {
                c.setForeground(getSelectionForeground());
                c.setBackground(getSelectionBackground());
            } else {
                c.setForeground(getForeground());
                c.setBackground((row % 2 == 0) ? EVEN_COLOR : getBackground());
            }
            return c;
        }
    };

    public MainPanel() {
        super(new BorderLayout());

        table.setFocusable(false);
        table.setRowSelectionAllowed(true);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setFillsViewportHeight(true);
        table.setRowHeight(18);
        table.setRowHeight(5, 80);

        TableColumn tc = table.getColumnModel().getColumn(0);
        tc.setMinWidth(100);
        tc.setCellRenderer(new InterIdeographJustifyCellRenderer());

        tc = table.getColumnModel().getColumn(1);
        tc.setPreferredWidth(220);
        add(new JScrollPane(table));
        setPreferredSize(new Dimension(320, 240));
    }

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

class InterIdeographJustifyCellRenderer implements TableCellRenderer {
    private final JustifiedLabel l = new JustifiedLabel();
    @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        l.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
        l.setText(Objects.toString(value, ""));
        return l;
    }
}

class JustifiedLabel extends JLabel {
    private transient TextLayout layout;
    private int prevWidth = -1;
    public JustifiedLabel() {
        this(null);
    }
    public JustifiedLabel(String str) {
        super(str);
    }
    @Override public void setText(String text) {
        super.setText(text);
        prevWidth = -1;
    }
    @Override protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        Font font = getFont();
        Insets ins = getInsets();
        Dimension d = getSize();
        int w = d.width - ins.left - ins.right;
        if (w != prevWidth) {
            prevWidth = w;
            layout = new TextLayout(getText(), font, g2.getFontRenderContext()).getJustifiedLayout((float) w);
        }
        g2.setPaint(getBackground());
        g2.fillRect(0, 0, d.width, d.height);
        g2.setPaint(getForeground());
        //int baseline = getBaseline(d.width, d.height);
        int baseline = ins.top + font.getSize();
        layout.draw(g2, (float) ins.left, (float) baseline);
        g2.dispose();
    }
}

// // http://ateraimemo.com/Swing/JustifiedLabel.html
// class JustifiedLabel extends JLabel {
//     private GlyphVector gvtext;
//     private int prevWidth = -1;
//     public JustifiedLabel() {
//         this(null);
//     }
//     public JustifiedLabel(String str) {
//         super(str);
//     }
//     @Override public void setText(String text) {
//         super.setText(text);
//         prevWidth = -1;
//     }
//     @Override protected void paintComponent(Graphics g) {
//         Graphics2D g2 = (Graphics2D) g.create();
//         Insets ins = getInsets();
//         int w = getSize().width - ins.left - ins.right;
//         if (w != prevWidth) {
//             gvtext = getJustifiedGlyphVector(w, getText(), getFont(), g2.getFontRenderContext());
//             prevWidth = w;
//         }
//         if (Objects.nonNull(gvtext)) {
//             g2.setPaint(getBackground());
//             g2.fillRect(0, 0, getWidth(), getHeight());
//             g2.setPaint(getForeground());
//             g2.drawGlyphVector(gvtext, ins.left, ins.top + getFont().getSize());
//         } else {
//             super.paintComponent(g);
//         }
//         g2.dispose();
//     }
//     private GlyphVector getJustifiedGlyphVector(int width, String str, Font font, FontRenderContext frc) {
//         GlyphVector gv = font.createGlyphVector(frc, str);
//         Rectangle2D r = gv.getVisualBounds();
//         float jwidth = (float) width;
//         float vwidth = (float) r.getWidth();
//         if (jwidth > vwidth) {
//             int num = gv.getNumGlyphs();
//             float xx = (jwidth - vwidth) / (float) (num - 1);
//             float xpos = num == 1 ? (jwidth - vwidth) * .5f : 0f;
//             Point2D gmPos = new Point2D.Double(0d, 0d);
//             for (int i = 0; i < num; i++) {
//                 GlyphMetrics gm = gv.getGlyphMetrics(i);
//                 gmPos.setLocation(xpos, 0);
//                 gv.setGlyphPosition(i, gmPos);
//                 xpos += gm.getAdvance() + xx;
//             }
//             return gv;
//         }
//         return null;
//     }
// }
