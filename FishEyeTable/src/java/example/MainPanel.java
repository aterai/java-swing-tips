// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    String[] columnNames = {"String", "Integer", "Boolean"};
    Object[][] data = {
      {"aaa", -1, true}
    };
    DefaultTableModel model = new DefaultTableModel(data, columnNames) {
      @Override public Class<?> getColumnClass(int column) {
        return getValueAt(0, column).getClass();
      }
    };
    IntStream.range(0, 20)
        .mapToObj(i -> new Object[] {"Name: " + i, i, i % 2 == 0})
        .forEach(model::addRow);
    JTable table = new FishEyeTable(model);
    table.setRowSelectionInterval(0, 0);
    JScrollPane scroll = new JScrollPane(table);
    scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
    scroll.setPreferredSize(new Dimension(320, 240));
    add(scroll, BorderLayout.NORTH);
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

class FishEyeRowContext implements Serializable {
  private static final long serialVersionUID = 1L;
  public final int height;
  public final Font font;
  public final Color color;

  protected FishEyeRowContext(int height, Font font, Color color) {
    this.height = height;
    this.font = font;
    this.color = color;
  }
}

class FishEyeTable extends JTable {
  private final List<FishEyeRowContext> fishEyeRowList;
  private final Font minFont;
  private transient FishEyeTableHandler handler;

  protected FishEyeTable(TableModel m) {
    super(m);
    Font font = getFont();
    minFont = font.deriveFont(8f);
    Font font12 = font.deriveFont(10f);
    Font font18 = font.deriveFont(16f);
    Font font24 = font.deriveFont(22f);
    Font font32 = font.deriveFont(30f);
    Color color12 = new Color(0xFA_FA_FA);
    Color color18 = new Color(0xF5_F5_F5);
    Color color24 = new Color(0xF0_F0_F0);
    Color color32 = new Color(0xE6_E6_FA);

    fishEyeRowList = Arrays.asList(
        new FishEyeRowContext(12, font12, color12),
        new FishEyeRowContext(18, font18, color18),
        new FishEyeRowContext(24, font24, color24),
        new FishEyeRowContext(32, font32, color32),
        new FishEyeRowContext(24, font24, color24),
        new FishEyeRowContext(18, font18, color18),
        new FishEyeRowContext(12, font12, color12)
    );
  }

  @Override public void updateUI() {
    removeMouseListener(handler);
    removeMouseMotionListener(handler);
    getSelectionModel().removeListSelectionListener(handler);
    super.updateUI();
    setColumnSelectionAllowed(false);
    setRowSelectionAllowed(true);
    setFillsViewportHeight(true);

    handler = new FishEyeTableHandler();
    addMouseListener(handler);
    addMouseMotionListener(handler);
    getSelectionModel().addListSelectionListener(handler);
  }

  private final class FishEyeTableHandler extends MouseAdapter implements ListSelectionListener {
    protected int prevRow = -1;
    protected int prevHeight;

    @Override public void mouseMoved(MouseEvent e) {
      update(rowAtPoint(e.getPoint()));
    }

    @Override public void mouseDragged(MouseEvent e) {
      update(rowAtPoint(e.getPoint()));
    }

    @Override public void mousePressed(MouseEvent e) {
      e.getComponent().repaint();
    }

    @Override public void valueChanged(ListSelectionEvent e) {
      if (e.getValueIsAdjusting()) {
        return;
      }
      update(getSelectedRow());
    }

    private void update(int row) {
      if (prevRow == row) {
        return;
      }
      initRowHeight(prevHeight, row);
      prevRow = row;
    }
  }

  @Override public void doLayout() {
    super.doLayout();
    Container p = SwingUtilities.getAncestorOfClass(JViewport.class, this);
    if (!(p instanceof JViewport)) {
      return;
    }
    int h = ((JViewport) p).getExtentSize().height;
    if (h == handler.prevHeight) {
      return;
    }
    initRowHeight(h, getSelectedRow());
    handler.prevHeight = h;
  }

  @Override public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
    Component c = super.prepareRenderer(renderer, row, column);
    int rowCount = getModel().getRowCount();
    Color color = Color.WHITE;
    Font font = minFont;
    int ccRow = handler.prevRow;
    int index = 0;
    int rd2 = (fishEyeRowList.size() - 1) / 2;
    for (int i = -rd2; i < rowCount; i++) {
      if (ccRow - rd2 <= i && i <= ccRow + rd2) {
        if (i == row) {
          color = fishEyeRowList.get(index).color;
          font = fishEyeRowList.get(index).font;
          break;
        }
        index++;
      }
    }
    c.setBackground(color);
    c.setFont(font);
    if (isRowSelected(row)) {
      c.setBackground(getSelectionBackground());
    }
    return c;
  }

  private int getViewableColoredRowCount(int idx) {
    int rd2 = (fishEyeRowList.size() - 1) / 2;
    int rc = getModel().getRowCount();
    if (rd2 - idx > 0) {
      return rd2 + 1 + idx;
    } else if (idx > rc - 1 - rd2 && idx < rc - 1 + rd2) {
      return rc - idx + rd2;
    }
    return fishEyeRowList.size();
  }

  protected void initRowHeight(int height, int ccRow) {
    int rd2 = (fishEyeRowList.size() - 1) / 2;
    int rowCount = getModel().getRowCount();
    int viewRc = getViewableColoredRowCount(ccRow);
    int viewH = getViewHeight(viewRc);
    int restRc = rowCount - viewRc;
    int restH = height - viewH;
    int restRh = Math.max(1, restH / restRc); // restRh = restRh > 0 ? restRh : 1;
    int restGap = restH - restRh * restRc;
    // System.out.format("%d-%d=%dx%d+%d=%d", height, viewH, restRc, restRh, restGap, restH);
    int index = -1;
    for (int i = -rd2; i < rowCount; i++) {
      int crh;
      if (ccRow - rd2 <= i && i <= ccRow + rd2) {
        index++;
        if (i < 0) {
          continue;
        }
        crh = fishEyeRowList.get(index).height;
      } else {
        if (i < 0) {
          continue;
        }
        crh = restRh + (restGap > 0 ? 1 : 0);
        restGap--;
      }
      setRowHeight(i, crh);
    }
  }

  private int getViewHeight(int count) {
    int h = 0;
    for (int i = 0; i < count; i++) {
      h += fishEyeRowList.get(i).height;
    }
    return h;
  }
}
