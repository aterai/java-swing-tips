// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Objects;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTable table = makeFlatTable();
    JTableHeader header = table.getTableHeader();
    header.setBorder(BorderFactory.createEmptyBorder());
    header.setDefaultRenderer(new DefaultTableCellRenderer() {
      private final CellBorder border = new CellBorder(2, 2, 1, 2);
      @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component c = super.getTableCellRendererComponent(
            table, value, isSelected, hasFocus, row, column);
        c.setBackground(table.getGridColor());
        if (c instanceof JLabel) {
          JLabel l = (JLabel) c;
          border.setStartCell(column == 0);
          l.setBorder(border);
          l.setHorizontalAlignment(CENTER);
        }
        return c;
      }
    });

    // JTextField field = new JTextField();
    // field.setBorder(BorderFactory.createEmptyBorder(2, 2, 1, 2));
    // table.setDefaultEditor(Object.class, new DefaultCellEditor(field));

    JScrollPane scroll = new TranslucentScrollPane(table);
    scroll.setBorder(BorderFactory.createLineBorder(table.getGridColor()));
    // scroll.setBackground(table.getGridColor());
    scroll.getViewport().setBackground(table.getBackground());

    add(scroll);
    setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 20));
    setPreferredSize(new Dimension(320, 240));
  }

  private static JTable makeFlatTable() {
    JTable table = new JTable(10, 3) {
      private final CellBorder border = new CellBorder(2, 2, 1, 2);
      @Override public Component prepareEditor(TableCellEditor editor, int row, int column) {
        Component c = super.prepareEditor(editor, row, column);
        if (c instanceof JTextField) {
          ((JComponent) c).setBorder(border);
          border.setStartCell(column == 0);
        }
        return c;
      }
    };
    table.setAutoCreateRowSorter(true);
    table.setFillsViewportHeight(true);
    table.setShowVerticalLines(false);
    table.setGridColor(Color.ORANGE);
    table.setSelectionForeground(Color.BLACK);
    table.setSelectionBackground(new Color(0x64_AA_EE_FF, true));
    table.setIntercellSpacing(new Dimension(0, 1));
    table.setBorder(BorderFactory.createEmptyBorder());
    table.setDefaultRenderer(Object.class, new FlatTableCellRenderer());
    return table;
  }

  private static final class FlatTableCellRenderer extends DefaultTableCellRenderer {
    private final CellBorder border = new CellBorder(2, 2, 1, 2);

    @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
      Component c = super.getTableCellRendererComponent(
          table, value, isSelected, hasFocus, row, column);
      border.setStartCell(column == 0);
      if (c instanceof JComponent) {
        ((JComponent) c).setBorder(border);
      }
      return c;
    }
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
      Logger.getGlobal().severe(ex::getMessage);
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

class TranslucentScrollPane extends JScrollPane {
  protected TranslucentScrollPane(Component c) {
    super(c);
  }

  @Override public boolean isOptimizedDrawingEnabled() {
    return false; // JScrollBar is overlap
  }

  @Override public void updateUI() {
    super.updateUI();
    EventQueue.invokeLater(() -> {
      getVerticalScrollBar().setUI(new OverlappedScrollBarUI());
      getHorizontalScrollBar().setUI(new OverlappedScrollBarUI());
      setComponentZOrder(getVerticalScrollBar(), 0);
      setComponentZOrder(getHorizontalScrollBar(), 1);
      setComponentZOrder(getViewport(), 2);
      getVerticalScrollBar().setOpaque(false);
      getHorizontalScrollBar().setOpaque(false);
    });
    setVerticalScrollBarPolicy(VERTICAL_SCROLLBAR_ALWAYS);
    setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_ALWAYS);
    setLayout(new OverlapScrollPaneLayout());
  }
}

class CellBorder extends EmptyBorder {
  private boolean startingCell;

  protected CellBorder(int top, int left, int bottom, int right) {
    super(top, left, bottom, right);
  }

  public boolean isStartCell() {
    return startingCell;
  }

  public void setStartCell(boolean b) {
    this.startingCell = b;
  }

  @Override public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
    Component cc = SwingUtilities.getAncestorOfClass(JTable.class, c);
    Color gridColor = Color.RED;
    if (cc instanceof JTable) {
      gridColor = ((JTable) cc).getGridColor();
    } else {
      cc = SwingUtilities.getAncestorOfClass(JTableHeader.class, c);
      if (cc instanceof JTableHeader) {
        gridColor = ((JTableHeader) cc).getTable().getGridColor();
      }
    }

    Graphics2D g2 = (Graphics2D) g.create();
    g2.translate(x, y);
    g2.setPaint(gridColor);
    if (!isStartCell()) {
      g2.drawLine(0, 0, 0, h - 1); // Left line
    }
    g2.dispose();
  }
}

class OverlapScrollPaneLayout extends ScrollPaneLayout {
  private static final int BAR_SIZE = 12;

  @Override public void layoutContainer(Container parent) {
    if (parent instanceof JScrollPane) {
      JScrollPane scrollPane = (JScrollPane) parent;
      Rectangle availR = SwingUtilities.calculateInnerArea(scrollPane, null);
      if (Objects.nonNull(colHead) && colHead.isVisible()) {
        Rectangle colHeadR = new Rectangle(0, availR.y, 0, 0);
        int colHeadHeight = Math.min(availR.height, colHead.getPreferredSize().height);
        colHeadR.height = colHeadHeight;
        availR.y += colHeadHeight;
        availR.height -= colHeadHeight;
        colHeadR.width = availR.width;
        colHeadR.x = availR.x;
        colHead.setBounds(colHeadR);
      }
      if (Objects.nonNull(viewport)) {
        viewport.setBounds(availR);
      }
      if (Objects.nonNull(vsb)) {
        Rectangle vsbR = new Rectangle();
        vsbR.width = BAR_SIZE;
        vsbR.height = availR.height; // - vsbR.width;
        vsbR.x = availR.x + availR.width - vsbR.width;
        vsbR.y = availR.y;
        vsb.setVisible(true);
        vsb.setBounds(vsbR);
      }
      if (Objects.nonNull(hsb)) {
        Rectangle hsbR = new Rectangle();
        hsbR.height = BAR_SIZE;
        hsbR.width = availR.width - hsbR.height;
        hsbR.x = availR.x;
        hsbR.y = availR.y + availR.height - hsbR.height;
        hsb.setVisible(true);
        hsb.setBounds(hsbR);
      }
    }
  }
}

class ZeroSizeButton extends JButton {
  private static final Dimension ZERO_SIZE = new Dimension();

  @Override public Dimension getPreferredSize() {
    return ZERO_SIZE;
  }
}

class OverlappedScrollBarUI extends BasicScrollBarUI {
  private static final Color DEFAULT_COLOR = new Color(100, 180, 255, 100);
  private static final Color DRAGGING_COLOR = new Color(100, 180, 200, 100);
  private static final Color ROLLOVER_COLOR = new Color(100, 180, 220, 100);

  @Override protected JButton createDecreaseButton(int orientation) {
    return new ZeroSizeButton();
  }

  @Override protected JButton createIncreaseButton(int orientation) {
    return new ZeroSizeButton();
  }

  @Override protected void paintTrack(Graphics g, JComponent c, Rectangle r) {
    // Graphics2D g2 = (Graphics2D) g.create();
  }

  @Override protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
    JScrollBar sb = (JScrollBar) c;
    Color color;
    if (r.isEmpty() || !sb.isEnabled()) {
      return;
    } else if (isDragging) {
      color = DRAGGING_COLOR;
    } else if (isThumbRollover()) {
      color = ROLLOVER_COLOR;
    } else {
      color = DEFAULT_COLOR;
    }
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setPaint(color);
    g2.fill(r);
    g2.dispose();
  }
}
