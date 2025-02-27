// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Objects;
import java.util.Optional;
import javax.swing.*;
import javax.swing.plaf.UIResource;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTable table = new JTable(makeModel()) {
      private transient HighlightListener highlighter;
      @Override public void updateUI() {
        addMouseListener(highlighter);
        addMouseMotionListener(highlighter);
        setDefaultRenderer(Object.class, null);
        setDefaultRenderer(Number.class, null);
        setDefaultRenderer(Boolean.class, null);
        super.updateUI();
        highlighter = new HighlightListener();
        addMouseListener(highlighter);
        addMouseMotionListener(highlighter);
        setDefaultRenderer(Object.class, new RolloverDefaultTableCellRenderer(highlighter));
        setDefaultRenderer(Number.class, new RolloverNumberRenderer(highlighter));
        setDefaultRenderer(Boolean.class, new RolloverBooleanRenderer(highlighter));
      }

      @Override public Component prepareEditor(TableCellEditor editor, int row, int column) {
        Component c = super.prepareEditor(editor, row, column);
        if (c instanceof JCheckBox) {
          c.setBackground(getSelectionBackground());
        }
        return c;
      }
    };
    table.setAutoCreateRowSorter(true);

    JSplitPane sp = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    sp.setTopComponent(new JScrollPane(new JTable(table.getModel())));
    sp.setBottomComponent(new JScrollPane(table));
    sp.setResizeWeight(.5);

    add(sp);
    setPreferredSize(new Dimension(320, 240));
  }

  private static TableModel makeModel() {
    String[] columnNames = {"String", "Integer", "Boolean"};
    Object[][] data = {
        {"aaa", 12, true}, {"bbb", 5, false}, {"CCC", 92, true}, {"DDD", 0, false}
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

class HighlightListener extends MouseAdapter {
  private int viewRowIndex = -1;
  private int viewColumnIndex = -1;

  public boolean isHighlightedCell(int row, int column) {
    return viewRowIndex == row && viewColumnIndex == column;
  }

  private static Optional<JTable> getTable(Component c) {
    JTable table = null;
    if (c instanceof JTable) {
      table = (JTable) c;
    }
    return Optional.ofNullable(table);
  }

  @Override public void mouseMoved(MouseEvent e) {
    getTable(e.getComponent()).ifPresent(table -> {
      Point pt = e.getPoint();
      final int prevRow = viewRowIndex;
      final int prevCol = viewColumnIndex;
      viewRowIndex = table.rowAtPoint(pt);
      viewColumnIndex = table.columnAtPoint(pt);
      if (viewRowIndex < 0 || viewColumnIndex < 0) {
        viewRowIndex = -1;
        viewColumnIndex = -1;
      }
      // >>>> HyperlinkCellRenderer.java
      // @see https://github.com/sjas/swingset3/blob/master/trunk/SwingSet3/src/com/sun/swingset3/demos/table/HyperlinkCellRenderer.java
      if (viewRowIndex == prevRow && viewColumnIndex == prevCol) {
        return;
      }
      Rectangle repaintRect;
      if (viewRowIndex >= 0) {
        Rectangle r = table.getCellRect(viewRowIndex, viewColumnIndex, false);
        if (prevRow >= 0 && prevCol >= 0) {
          repaintRect = r.union(table.getCellRect(prevRow, prevCol, false));
        } else {
          repaintRect = r;
        }
      } else {
        repaintRect = table.getCellRect(prevRow, prevCol, false);
      }
      table.repaint(repaintRect);
      // <<<<
      // table.repaint();
    });
  }

  @Override public void mouseExited(MouseEvent e) {
    getTable(e.getComponent()).ifPresent(table -> {
      if (viewRowIndex >= 0 && viewColumnIndex >= 0) {
        table.repaint(table.getCellRect(viewRowIndex, viewColumnIndex, false));
      }
      viewRowIndex = -1;
      viewColumnIndex = -1;
    });
  }
}

class RolloverDefaultTableCellRenderer extends DefaultTableCellRenderer {
  private static final Color HIGHLIGHT = new Color(0xFF_96_32);
  private final transient HighlightListener highlighter;

  protected RolloverDefaultTableCellRenderer(HighlightListener highlighter) {
    super();
    this.highlighter = highlighter;
  }

  @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    Component c = super.getTableCellRendererComponent(
        table, value, isSelected, hasFocus, row, column);
    boolean highlightedCell = highlighter.isHighlightedCell(row, column);
    String fmt = highlightedCell ? "<html><u>%s" : "%s";
    if (c instanceof JLabel) {
      ((JLabel) c).setText(String.format(fmt, value));
    }
    Color selectionFgc = table.getSelectionForeground();
    Color selectionBgc = table.getSelectionBackground();
    if (highlightedCell) {
      c.setForeground(isSelected ? selectionFgc : HIGHLIGHT);
      c.setBackground(isSelected ? selectionBgc.darker() : table.getBackground());
    } else {
      c.setForeground(isSelected ? selectionFgc : table.getForeground());
      c.setBackground(isSelected ? selectionBgc : table.getBackground());
    }
    return c;
  }
}

class RolloverNumberRenderer extends RolloverDefaultTableCellRenderer {
  protected RolloverNumberRenderer(HighlightListener highlighter) {
    super(highlighter);
  }

  @Override public void updateUI() {
    super.updateUI();
    setHorizontalAlignment(RIGHT);
  }
}

class RolloverBooleanRenderer implements TableCellRenderer, UIResource {
  private final HighlightListener highlighter;
  private final JCheckBox check = new JCheckBox() {
    @Override public void updateUI() {
      super.updateUI();
      // setHorizontalAlignment(SwingConstants.CENTER);
      setBorderPainted(true);
      setRolloverEnabled(true);
      setOpaque(true);
      setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
    }

    // Overridden for performance reasons. ---->
    @Override public boolean isOpaque() {
      Object o = SwingUtilities.getAncestorOfClass(JTable.class, this);
      return o instanceof JTable ? colorNotMatch((JTable) o) : super.isOpaque();
    }

    private boolean colorNotMatch(JTable t) {
      Color bgc = getBackground();
      boolean colorMatch = bgc != null && bgc.equals(t.getBackground()) && t.isOpaque();
      return !colorMatch && super.isOpaque();
    }

    @Override protected void firePropertyChange(String propertyName, Object ov, Object nv) {
      // System.out.println(propertyName);
      // if (propertyName == "border" ||
      //     ((propertyName == "font" || propertyName == "foreground") && ov != nv)) {
      //   super.firePropertyChange(propertyName, ov, nv);
      // }
    }

    @Override public void firePropertyChange(String propertyName, boolean ov, boolean nv) {
      /* Overridden for performance reasons. */
    }

    @Override public void repaint(long tm, int x, int y, int width, int height) {
      /* Overridden for performance reasons. */
    }

    @Override public void repaint(Rectangle r) {
      /* Overridden for performance reasons. */
    }

    @Override public void repaint() {
      /* Overridden for performance reasons. */
    }

    @Override public void invalidate() {
      /* Overridden for performance reasons. */
    }

    @Override public void validate() {
      /* Overridden for performance reasons. */
    }

    @Override public void revalidate() {
      /* Overridden for performance reasons. */
    }
    // <---- Overridden for performance reasons.
  };

  protected RolloverBooleanRenderer(HighlightListener highlighter) {
    this.highlighter = highlighter;
  }

  @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    check.getModel().setRollover(highlighter.isHighlightedCell(row, column));
    check.setHorizontalAlignment(SwingConstants.CENTER);
    if (isSelected) {
      check.setForeground(table.getSelectionForeground());
      check.setBackground(table.getSelectionBackground());
    } else {
      check.setForeground(table.getForeground());
      check.setBackground(table.getBackground());
      // setBackground(row % 2 == 0 ? table.getBackground() : Color.WHITE); // Nimbus
    }
    check.setSelected(Objects.equals(value, Boolean.TRUE));
    return check;
  }
}
