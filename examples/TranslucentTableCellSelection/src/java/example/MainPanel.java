// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.plaf.LayerUI;
import javax.swing.plaf.UIResource;
import javax.swing.plaf.synth.SynthTableUI;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JMenuBar mb = new JMenuBar();
    mb.add(LookAndFeelUtils.createLookAndFeelMenu());
    EventQueue.invokeLater(() -> getRootPane().setJMenuBar(mb));
    JTable table = new TranslucentCellSelectionTable(createModel());
    JScrollPane scroll = new JScrollPane(table) {
      @Override public void updateUI() {
        super.updateUI();
        setBackground(UIManager.getColor("Table.background"));
        // setBackground(table.getBackground());
        getViewport().setOpaque(false);
        setViewportBorder(BorderFactory.createEmptyBorder(1, 2, 1, 2));
      }
    };
    // add(scroll);
    add(new JLayer<>(scroll, new TranslucentCellSelectionLayerUI()));
    setPreferredSize(new Dimension(320, 240));
  }

  private static TableModel createModel() {
    String[] columnNames = {"String", "Integer", "Boolean"};
    Object[][] data = {
        {"aaa", 12, true}, {"bbb", 5, false}, {"CCC", 92, true}, {"DDD", 0, false},
        {"eee", 32, true}, {"fff", 8, false}, {"ggg", 64, true}, {"hhh", 1, false},
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

final class TranslucentCellSelectionTable extends JTable {
  private static final Color TRANSPARENT = new Color(0x0, true);

  /* default */ TranslucentCellSelectionTable(TableModel model) {
    super(model);
    // The selection outline is painted over the whole JLayer, so repaint the
    // entire table when editing starts, stops, or is canceled to hide/show it.
    addPropertyChangeListener("tableCellEditor", e -> repaint());
  }

  @Override public void updateUI() {
    super.updateUI();
    // If override JTable#paintComponent(...), need to use setOpaque(false)
    // setOpaque(false);
    setCellSelectionEnabled(true);
    setIntercellSpacing(new Dimension(3, 3));
    setAutoCreateRowSorter(true);
    setBackground(TRANSPARENT);
    setRowHeight(20);
    if (getUI() instanceof SynthTableUI) {
      setDefaultRenderer(Boolean.class, new SynthBooleanTableCellRenderer());
    }
  }

  @Override public Component prepareEditor(TableCellEditor editor, int row, int column) {
    Component c = super.prepareEditor(editor, row, column);
    if (c instanceof JComponent) {
      ((JComponent) c).setOpaque(false);
    }
    return c;
  }

  @Override public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
    Component c = super.prepareRenderer(renderer, row, column);
    if (c instanceof JComponent) {
      ((JComponent) c).setOpaque(false);
    }
    c.setForeground(getForeground());
    c.setBackground(TRANSPARENT);
    return c;
  }

  // JTable repaints only the changed rows or columns, which would leave a part
  // of the old selection outline, so repaint the entire table on any selection
  // change (mouse, keyboard, selectAll(), clearSelection(), etc.).
  @Override public void valueChanged(ListSelectionEvent e) {
    super.valueChanged(e);
    repaint();
  }

  @Override public void columnSelectionChanged(ListSelectionEvent e) {
    super.columnSelectionChanged(e);
    repaint();
  }

  // @Override protected void paintComponent(Graphics g) {
  //   super.paintComponent(g);
  //   int cc = getSelectedColumnCount();
  //   int rc = getSelectedRowCount();
  //   if (cc != 0 && rc != 0 && !isEditing()) {
  //     Graphics2D g2 = (Graphics2D) g.create();
  //     g2.setRenderingHint(
  //        RenderingHints.KEY_ANTIALIASING,
  //        RenderingHints.VALUE_ANTIALIAS_ON);
  //     Area area = new Area();
  //     for (int row : getSelectedRows()) {
  //       for (int col : getSelectedColumns()) {
  //         addArea(area, row, col);
  //       }
  //     }
  //     Dimension ics = getIntercellSpacing();
  //     for (Area a : GeomUtils.splitIntoSingleLoopAreas(area)) {
  //       Rectangle r = a.getBounds();
  //       r.width -= ics.width - 1;
  //       r.height -= ics.height - 1;
  //       g2.setPaint(new Color(0x32_00_FE_64, true));
  //       g2.fill(r);
  //       g2.setPaint(getSelectionBackground());
  //       g2.setStroke(new BasicStroke(2f));
  //       g2.draw(r);
  //     }
  //     g2.dispose();
  //   }
  // }
  //
  // private void addArea(Area area, int row, int col) {
  //   if (isCellSelected(row, col)) {
  //     area.add(new Area(getCellRect(row, col, true)));
  //   }
  // }
}

final class GeomUtils {
  private GeomUtils() {
    /* Singleton */
  }

  // Decompose a multi-loop Area into a list of single-loop Areas.
  public static List<Area> splitIntoSingleLoopAreas(Area area) {
    List<Area> subAreas = new ArrayList<>();
    Path2D path = new Path2D.Double();
    PathIterator pi = area.getPathIterator(null);
    double[] cd = new double[6];
    while (!pi.isDone()) {
      switch (pi.currentSegment(cd)) {
        case PathIterator.SEG_MOVETO:
          path.moveTo(cd[0], cd[1]);
          break;
        case PathIterator.SEG_LINETO:
          path.lineTo(cd[0], cd[1]);
          break;
        case PathIterator.SEG_QUADTO:
          path.quadTo(cd[0], cd[1], cd[2], cd[3]);
          break;
        case PathIterator.SEG_CUBICTO:
          path.curveTo(cd[0], cd[1], cd[2], cd[3], cd[4], cd[5]);
          break;
        case PathIterator.SEG_CLOSE:
          path.closePath();
          subAreas.add(new Area(path));
          path.reset();
          break;
        default:
          break;
      }
      pi.next();
    }
    return subAreas;
  }
}

class SynthBooleanTableCellRenderer extends JCheckBox implements TableCellRenderer {
  @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    setHorizontalAlignment(CENTER);
    setName("Table.cellRenderer");
    if (isSelected) {
      setForeground(unwrap(table.getSelectionForeground()));
      setBackground(unwrap(table.getSelectionBackground()));
    } else {
      setForeground(unwrap(table.getForeground()));
      setBackground(unwrap(table.getBackground()));
    }
    setSelected(value != null && (Boolean) value);
    return this;
  }

  private static Color unwrap(Color c) {
    return c instanceof UIResource ? new Color(c.getRGB()) : c;
  }

  // @Override public boolean isOpaque() {
  //   return isRowSelected ? true : super.isOpaque();
  //   return false;
  // }
}

class TranslucentCellSelectionLayerUI extends LayerUI<JScrollPane> {
  // private static final Color SELECTION_BGC = new Color(0x32_00_FE_64, true);
  private static final Stroke BORDER_STROKE = new BasicStroke(2f);

  @Override public void paint(Graphics g, JComponent c) {
    super.paint(g, c);
    JScrollPane scroll = getScrollPane(c);
    JTable table = getTable(scroll);
    if (table != null && hasSelectedCells(table) && !table.isEditing()) {
      Graphics2D g2 = (Graphics2D) g.create();
      g2.setRenderingHint(
          RenderingHints.KEY_ANTIALIASING,
          RenderingHints.VALUE_ANTIALIAS_ON);
      // Clip to the viewport (including its border) so that the selection
      // scrolled out of view is not painted over the header or scrollbars.
      g2.clip(SwingUtilities.convertRectangle(scroll, scroll.getViewportBorderBounds(), c));
      Area area = new Area();
      for (int row : table.getSelectedRows()) {
        for (int col : table.getSelectedColumns()) {
          addArea(c, table, area, row, col);
        }
      }
      Dimension ics = table.getIntercellSpacing();
      Color selectionColor = table.getSelectionBackground();
      // int rgb = selectionColor.getRGB() & 0xFF_FF_FF | (0x32 << 24);
      Color translucentColor = new Color(
          selectionColor.getRed(), selectionColor.getGreen(), selectionColor.getBlue(), 0x32);
      g2.setStroke(BORDER_STROKE);
      for (Area a : GeomUtils.splitIntoSingleLoopAreas(area)) {
        Rectangle r = a.getBounds();
        r.width -= ics.width - 1;
        r.height -= ics.height - 1;
        g2.setPaint(translucentColor);
        g2.fill(r);
        g2.setPaint(selectionColor);
        g2.draw(r);
      }
      g2.dispose();
    }
  }

  private static boolean hasSelectedCells(JTable table) {
    return table.getSelectedRowCount() > 0 && table.getSelectedColumnCount() > 0;
  }

  private static void addArea(Component c, JTable table, Area area, int row, int col) {
    if (table.isCellSelected(row, col)) {
      Rectangle r = table.getCellRect(row, col, true);
      area.add(new Area(SwingUtilities.convertRectangle(table, r, c)));
    }
  }

  private static JScrollPane getScrollPane(Component c) {
    JScrollPane scroll = null;
    if (c instanceof JLayer) {
      Component view = ((JLayer<?>) c).getView();
      if (view instanceof JScrollPane) {
        scroll = (JScrollPane) view;
      }
    }
    return scroll;
  }

  private static JTable getTable(JScrollPane scroll) {
    JTable table = null;
    if (scroll != null) {
      Component view = scroll.getViewport().getView();
      if (view instanceof JTable) {
        table = (JTable) view;
      }
    }
    return table;
  }
}

final class LookAndFeelUtils {
  private static String lookAndFeel = UIManager.getLookAndFeel().getClass().getName();

  private LookAndFeelUtils() {
    /* Singleton */
  }

  public static JMenu createLookAndFeelMenu() {
    JMenu menu = new JMenu("LookAndFeel");
    ButtonGroup buttonGroup = new ButtonGroup();
    for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
      AbstractButton b = createButton(info);
      initLookAndFeelAction(info, b);
      menu.add(b);
      buttonGroup.add(b);
    }
    return menu;
  }

  private static AbstractButton createButton(UIManager.LookAndFeelInfo info) {
    boolean selected = info.getClassName().equals(lookAndFeel);
    return new JRadioButtonMenuItem(info.getName(), selected);
  }

  public static void initLookAndFeelAction(UIManager.LookAndFeelInfo info, AbstractButton b) {
    String cmd = info.getClassName();
    b.setText(info.getName());
    b.setActionCommand(cmd);
    b.setHideActionText(true);
    b.addActionListener(e -> setLookAndFeel(cmd));
  }

  private static void setLookAndFeel(String newLookAndFeel) {
    String oldLookAndFeel = lookAndFeel;
    if (!oldLookAndFeel.equals(newLookAndFeel)) {
      try {
        UIManager.setLookAndFeel(newLookAndFeel);
        lookAndFeel = newLookAndFeel;
      } catch (UnsupportedLookAndFeelException ignored) {
        Toolkit.getDefaultToolkit().beep();
      } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
        Logger.getGlobal().severe(ex::getMessage);
        return;
      }
      updateLookAndFeel();
      // firePropertyChange("lookAndFeel", oldLookAndFeel, newLookAndFeel);
    }
  }

  private static void updateLookAndFeel() {
    for (Window window : Window.getWindows()) {
      SwingUtilities.updateComponentTreeUI(window);
    }
  }
}
