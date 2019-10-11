// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.font.TextLayout;
import java.util.Objects;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    String[] columnNames = {"Justified", "Default"};
    Object[][] data = {
      {"会社名", ""},
      {"所在地", ""},
      {"電話番号", ""},
      {"設立", ""},
      {"代表取締役", ""},
      {"事業内容", ""}
    };
    TableModel model = new DefaultTableModel(data, columnNames);
    JTable table = new JTable(model) {
      private final Color evenColor = new Color(0xF5_F5_FF);
      @Override public Component prepareRenderer(TableCellRenderer tcr, int row, int column) {
        Component c = super.prepareRenderer(tcr, row, column);
        if (isRowSelected(row)) {
          c.setForeground(getSelectionForeground());
          c.setBackground(getSelectionBackground());
        } else {
          c.setForeground(getForeground());
          c.setBackground(row % 2 == 0 ? evenColor : getBackground());
        }
        return c;
      }
    };
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

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
      ex.printStackTrace();
      Toolkit.getDefaultToolkit().beep();
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
  private final JustifiedLabel renderer = new JustifiedLabel();

  @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    renderer.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
    renderer.setText(Objects.toString(value, ""));
    return renderer;
  }
}

class JustifiedLabel extends JLabel {
  private transient TextLayout layout;
  private int prevWidth = -1;

  protected JustifiedLabel() {
    this(null);
  }

  protected JustifiedLabel(String str) {
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
    // int baseline = getBaseline(d.width, d.height);
    float baseline = ins.top + font.getSize2D();
    layout.draw(g2, (float) ins.left, baseline);
    g2.dispose();
  }
}

// // https://ateraimemo.com/Swing/JustifiedLabel.html
// class JustifiedLabel extends JLabel {
//   private GlyphVector gvText;
//   private int prevWidth = -1;
//   protected JustifiedLabel() {
//     this(null);
//   }
//
//   protected JustifiedLabel(String str) {
//     super(str);
//   }
//
//   @Override public void setText(String text) {
//     super.setText(text);
//     prevWidth = -1;
//   }
//
//   @Override protected void paintComponent(Graphics g) {
//     Graphics2D g2 = (Graphics2D) g.create();
//     Insets ins = getInsets();
//     int w = getSize().width - ins.left - ins.right;
//     if (w != prevWidth) {
//       gvText = getJustifiedGlyphVector(w, getText(), getFont(), g2.getFontRenderContext());
//       prevWidth = w;
//     }
//     if (Objects.nonNull(gvText)) {
//       g2.setPaint(getBackground());
//       g2.fillRect(0, 0, getWidth(), getHeight());
//       g2.setPaint(getForeground());
//       g2.drawGlyphVector(gvText, ins.left, ins.top + getFont().getSize());
//     } else {
//       super.paintComponent(g);
//     }
//     g2.dispose();
//   }
//
//   private GlyphVector getJustifiedGlyphVector(int width, String str, Font font, FontRenderContext frc) {
//     GlyphVector gv = font.createGlyphVector(frc, str);
//     Rectangle2D r = gv.getVisualBounds();
//     float jw = (float) width;
//     float vw = (float) r.getWidth();
//     if (jw > vw) {
//       int num = gv.getNumGlyphs();
//       float xx = (jw - vw) / (float) (num - 1);
//       float xpos = num == 1 ? (jw - vw) * .5f : 0f;
//       Point2D gmPos = new Point2D.Float();
//       for (int i = 0; i < num; i++) {
//         GlyphMetrics gm = gv.getGlyphMetrics(i);
//         gmPos.setLocation(xpos, 0);
//         gv.setGlyphPosition(i, gmPos);
//         xpos += gm.getAdvance() + xx;
//       }
//       return gv;
//     }
//     return null;
//   }
// }
