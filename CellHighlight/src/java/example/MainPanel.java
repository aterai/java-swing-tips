// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Optional;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public final class MainPanel extends JPanel {
  private final JTable table = new JTable(new DefaultTableModel(10, 10)) {
    protected transient HighlightListener highlighter;
    @Override public void updateUI() {
      removeMouseListener(highlighter);
      removeMouseMotionListener(highlighter);
      super.updateUI();
      // setAutoCreateRowSorter(true);
      setRowSelectionAllowed(false);

      highlighter = new HighlightListener();
      addMouseListener(highlighter);
      addMouseMotionListener(highlighter);

      setDefaultRenderer(Object.class, new HighlightRenderer(highlighter));
      setDefaultRenderer(Number.class, new HighlightRenderer(highlighter));
    }
  };

  private MainPanel() {
    super(new BorderLayout());
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

class HighlightListener extends MouseAdapter {
  private static final Color HIGHLIGHT1 = new Color(200, 200, 255);
  private static final Color HIGHLIGHT2 = new Color(240, 240, 255);
  private int vrow = -1; // viewRowIndex
  private int vcol = -1; // viewColumnIndex

  public Optional<? extends Color> getHighlightableCellColor(int row, int column) {
    if (this.vrow == row || this.vcol == column) {
      if (this.vrow == row && this.vcol == column) {
        return Optional.of(HIGHLIGHT1);
      } else {
        return Optional.of(HIGHLIGHT2);
      }
    }
    return Optional.empty();
  }

  private void setHighlighTableCell(MouseEvent e) {
    Point pt = e.getPoint();
    Component c = e.getComponent();
    if (c instanceof JTable) {
      JTable table = (JTable) c;
      vrow = table.rowAtPoint(pt);
      vcol = table.columnAtPoint(pt);
      if (vrow < 0 || vcol < 0) {
        vrow = -1;
        vcol = -1;
      }
      table.repaint();
    }
  }

  @Override public void mouseMoved(MouseEvent e) {
    setHighlighTableCell(e);
  }

  @Override public void mouseDragged(MouseEvent e) {
    setHighlighTableCell(e);
  }

  @Override public void mouseExited(MouseEvent e) {
    vrow = -1;
    vcol = -1;
    e.getComponent().repaint();
  }
}

class HighlightRenderer extends DefaultTableCellRenderer {
  private final transient HighlightListener highlighter;

  protected HighlightRenderer(HighlightListener highlighter) {
    super();
    this.highlighter = highlighter;
  }

  @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    setHorizontalAlignment(value instanceof Number ? RIGHT : LEFT);
    setBackground(table.getBackground());
    highlighter.getHighlightableCellColor(row, column).ifPresent(this::setBackground);
    return this;
  }
}

// class HighlightableTable extends JTable {
//   private final HighlightListener highlighter;
//   protected HighlightableTable(TableModel model) {
//     super(model);
//     highlighter = new HighlightListener(this);
//     addMouseListener(highlighter);
//     addMouseMotionListener(highlighter);
//   }
//   @Override public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
//     Component c = super.prepareRenderer(renderer, row, column);
//     if (highlighter.isHighlightableCell(row, column)) {
//       c.setBackground(Color.RED);
//     } else if (isRowSelected(row)) {
//       c.setBackground(getSelectionBackground());
//     } else {
//       c.setBackground(Color.WHITE);
//     }
//     return c;
//   }
// }
