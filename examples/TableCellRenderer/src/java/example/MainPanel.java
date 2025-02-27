// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphMetrics;
import java.awt.font.GlyphVector;
import java.awt.geom.Point2D;
import java.util.Objects;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

public final class MainPanel extends JPanel {
  private static final String STR0 = "Default Default Default Default";
  private static final String STR1 = "GlyphVector GlyphVector GlyphVector GlyphVector";
  private static final String STR2 = "JTextArea JTextArea JTextArea JTextArea";
  private static final String STR3 = "***************************************";

  private MainPanel() {
    super(new BorderLayout());
    JTable table = new JTable(makeModel()) {
      @Override public void updateUI() {
        setSelectionForeground(null); // Nimbus
        setSelectionBackground(null); // Nimbus
        getColumnModel().getColumn(0).setCellRenderer(null);
        getColumnModel().getColumn(1).setCellRenderer(null);
        getColumnModel().getColumn(2).setCellRenderer(null);
        super.updateUI();
        getTableHeader().setReorderingAllowed(false);
        setRowSelectionAllowed(true);
        setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        setRowHeight(50);
        getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer());
        getColumnModel().getColumn(1).setCellRenderer(new WrappedLabelRenderer());
        getColumnModel().getColumn(2).setCellRenderer(new TextAreaCellRenderer());
      }
    };
    // JTableHeader header = table.getTableHeader();
    // header.setReorderingAllowed(false);
    add(new JScrollPane(table));
    setPreferredSize(new Dimension(320, 240));
  }

  private static TableModel makeModel() {
    String[] columnNames = {"Default", "GlyphVector", "JTextArea"};
    Object[][] data = {
        {STR0, STR1, STR2}, {STR0, STR1, STR2}, {STR3, STR3, STR3}, {STR3, STR3, STR3}
    };
    return new DefaultTableModel(data, columnNames) {
      @Override public Class<?> getColumnClass(int column) {
        return getValueAt(0, column).getClass();
      }
    };
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (UnsupportedLookAndFeelException ignored) {
      Toolkit.getDefaultToolkit().beep();
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
      ex.printStackTrace();
      return;
    }
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

class WrappedLabelRenderer implements TableCellRenderer {
  private final JLabel renderer = new WrappedLabel() {
    @Override public void updateUI() {
      super.updateUI();
      setOpaque(true);
      setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
    }
  };

  @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    if (isSelected) {
      renderer.setForeground(table.getSelectionForeground());
      renderer.setBackground(table.getSelectionBackground());
    } else {
      renderer.setForeground(table.getForeground());
      renderer.setBackground(table.getBackground());
    }
    boolean b = value instanceof Number;
    renderer.setHorizontalAlignment(b ? SwingConstants.RIGHT : SwingConstants.LEFT);
    renderer.setFont(table.getFont());
    renderer.setText(Objects.toString(value, ""));
    return renderer;
  }
}

class WrappedLabel extends JLabel {
  private transient GlyphVector gvText;

  protected WrappedLabel() {
    this("");
  }

  protected WrappedLabel(String str) {
    super(str);
  }

  @Override public void doLayout() {
    Rectangle r = SwingUtilities.calculateInnerArea(this, null);
    Font font = getFont();
    FontMetrics fm = getFontMetrics(font);
    FontRenderContext frc = fm.getFontRenderContext();
    GlyphVector gv = font.createGlyphVector(frc, getText());
    gvText = getWrappedGlyphVector(gv, r.getWidth());
    super.doLayout();
  }

  @Override protected void paintComponent(Graphics g) {
    if (Objects.nonNull(gvText)) {
      Insets i = getInsets();
      Graphics2D g2 = (Graphics2D) g.create();
      g2.setPaint(getBackground());
      g2.fillRect(0, 0, getWidth(), getHeight());
      g2.setPaint(getForeground());
      g2.drawGlyphVector(gvText, i.left, getFont().getSize2D() + i.top);
      g2.dispose();
    } else {
      super.paintComponent(g);
    }
  }

  private static GlyphVector getWrappedGlyphVector(GlyphVector gv, double width) {
    Point2D gmPos = new Point2D.Float();
    float lineHeight = (float) gv.getLogicalBounds().getHeight();
    float pos = 0f;
    int lineCount = 0;
    GlyphMetrics gm;

    for (int i = 0; i < gv.getNumGlyphs(); i++) {
      gm = gv.getGlyphMetrics(i);
      float advance = gm.getAdvance();
      if (pos < width && width <= pos + advance) {
        lineCount++;
        pos = 0f;
      }
      gmPos.setLocation(pos, lineHeight * lineCount);
      gv.setGlyphPosition(i, gmPos);
      pos += advance;
    }
    return gv;
  }
}

// delegation pattern
class TextAreaCellRenderer implements TableCellRenderer {
  // public static class UIResource extends TextAreaCellRenderer implements UIResource {}
  private final JTextArea renderer = new JTextArea();

  protected TextAreaCellRenderer() {
    super();
    renderer.setLineWrap(true);
    renderer.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
    // renderer.setName("Table.cellRenderer");
  }

  @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    if (isSelected) {
      renderer.setForeground(table.getSelectionForeground());
      renderer.setBackground(table.getSelectionBackground());
    } else {
      renderer.setForeground(table.getForeground());
      renderer.setBackground(table.getBackground());
    }
    renderer.setFont(table.getFont());
    renderer.setText(Objects.toString(value, ""));
    return renderer;
  }
}

// class TextAreaCellRenderer extends JTextArea implements TableCellRenderer {
//   // public static class UIResource extends TextAreaCellRenderer implements UIResource {}
//
//   @Override public void updateUI() {
//     super.updateUI();
//     setLineWrap(true);
//     setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
//     // setName("Table.cellRenderer");
//   }
//
//   @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
//     if (isSelected) {
//       setForeground(table.getSelectionForeground());
//       setBackground(table.getSelectionBackground());
//     } else {
//       setForeground(table.getForeground());
//       setBackground(table.getBackground());
//     }
//     setFont(table.getFont());
//     setText(Objects.toString(value, ""));
//     return this;
//   }
//
//   // Overridden for performance reasons. ---->
//   @Override public boolean isOpaque() {
//     Color back = getBackground();
//     Object o = SwingUtilities.getAncestorOfClass(JTable.class, this);
//     if (o instanceof JTable) {
//       JTable t = (JTable) o;
//       boolean colorMatch = back != null && back.equals(t.getBackground()) && t.isOpaque();
//       return !colorMatch && super.isOpaque();
//     } else {
//       return super.isOpaque();
//     }
//   }
//
//   @Override protected void firePropertyChange(String property, Object ov, Object nv) {
//     if ("document".equals(property)) {
//       super.firePropertyChange(property, ov, nv);
//     } else if (("font".equals(property) || "foreground".equals(property)) && ov != nv) {
//       super.firePropertyChange(property, ov, nv);
//     }
//   }
//
//   @Override public void firePropertyChange(String property, boolean ov, boolean nv) {
//     /* Overridden for performance reasons. */
//   }
//
//   @Override public void repaint(long tm, int x, int y, int width, int height) {
//     /* Overridden for performance reasons. */
//   }
//
//   @Override public void repaint(Rectangle r) {
//     /* Overridden for performance reasons. */
//   }
//
//   @Override public void repaint() {
//     /* Overridden for performance reasons. */
//   }
//
//   @Override public void invalidate() {
//     /* Overridden for performance reasons. */
//   }
//
//   @Override public void validate() {
//     /* Overridden for performance reasons. */
//   }
//
//   @Override public void revalidate() {
//     /* Overridden for performance reasons. */
//   }
//   // <---- Overridden for performance reasons.
// }
