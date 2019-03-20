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
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

public final class MainPanel extends JPanel {
  private static final String STR0 = "Default Default Default Default";
  private static final String STR1 = "GlyphVector GlyphVector GlyphVector GlyphVector";
  private static final String STR2 = "JTextArea JTextArea JTextArea JTextArea";
  private static final String STR3 = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";

  private MainPanel() {
    super(new BorderLayout());

    String[] columnNames = {"Default", "GlyphVector", "JTextArea"};
    Object[][] data = {
      {STR0, STR1, STR2}, {STR0, STR1, STR2},
      {STR3, STR3, STR3}, {STR3, STR3, STR3}
    };
    TableModel model = new DefaultTableModel(data, columnNames) {
      @Override public Class<?> getColumnClass(int column) {
        return getValueAt(0, column).getClass();
      }
    };
    JTable table = new JTable(model) {
      @Override public void updateUI() {
        getColumnModel().getColumn(1).setCellRenderer(null);
        getColumnModel().getColumn(2).setCellRenderer(null);
        super.updateUI();
        setRowSelectionAllowed(true);
        setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        setRowHeight(50);
        getColumnModel().getColumn(1).setCellRenderer(new TestRenderer());
        getColumnModel().getColumn(2).setCellRenderer(new TextAreaCellRenderer());
      }
    };

    JTableHeader tableHeader = table.getTableHeader();
    tableHeader.setReorderingAllowed(false);

    add(new JScrollPane(table));
    setPreferredSize(new Dimension(320, 240));
  }

  public static void main(String... args) {
    EventQueue.invokeLater(new Runnable() {
      @Override public void run() {
        createAndShowGui();
      }
    });
  }

  public static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
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
  @Override public void updateUI() {
    super.updateUI();
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

  protected WrappedLabel() {
    super();
  }

  protected WrappedLabel(String str) {
    super(str);
  }

  @Override public void doLayout() {
    Insets i = getInsets();
    int w = getWidth() - i.left - i.right;
    Font font = getFont();
    FontMetrics fm = getFontMetrics(font);
    FontRenderContext frc = fm.getFontRenderContext();
    gvtext = getWrappedGlyphVector(getText(), w, font, frc);
    super.doLayout();
  }

  @Override protected void paintComponent(Graphics g) {
    if (Objects.nonNull(gvtext)) {
      Insets i = getInsets();
      Graphics2D g2 = (Graphics2D) g.create();
      g2.setPaint(getBackground());
      g2.fillRect(0, 0, getWidth(), getHeight());
      g2.setPaint(getForeground());
      g2.drawGlyphVector(gvtext, i.left, getFont().getSize() + i.top);
      g2.dispose();
    } else {
      super.paintComponent(g);
    }
  }

  private static GlyphVector getWrappedGlyphVector(String str, double width, Font font, FontRenderContext frc) {
    Point2D gmPos = new Point2D.Float();
    GlyphVector gv = font.createGlyphVector(frc, str);
    float lineheight = (float) gv.getLogicalBounds().getHeight();
    float xpos = 0f;
    float advance = 0f;
    int lineCount = 0;
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
//       JTable table = (JTable) o;
//       boolean colorMatch = Objects.nonNull(back) && back.equals(table.getBackground()) && table.isOpaque();
//       return !colorMatch && super.isOpaque();
//     } else {
//       return super.isOpaque();
//     }
//   }
//
//   @Override protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
//     if ("document".equals(propertyName)) {
//       super.firePropertyChange(propertyName, oldValue, newValue);
//     } else if (("font".equals(propertyName) || "foreground".equals(propertyName)) && oldValue != newValue) {
//       super.firePropertyChange(propertyName, oldValue, newValue);
//     }
//   }
//
//   @Override public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) { /* Overridden for performance reasons. */ }
//
//   @Override public void repaint(long tm, int x, int y, int width, int height) { /* Overridden for performance reasons. */ }
//
//   @Override public void repaint(Rectangle r) { /* Overridden for performance reasons. */ }
//
//   @Override public void repaint() { /* Overridden for performance reasons. */ }
//
//   @Override public void invalidate() { /* Overridden for performance reasons. */ }
//
//   @Override public void validate() { /* Overridden for performance reasons. */ }
//
//   @Override public void revalidate() { /* Overridden for performance reasons. */ }
//   // <---- Overridden for performance reasons.
// }
