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
import javax.swing.*;
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

    JScrollPane scroll = new JScrollPane(new RoundedCellSelectionTable(makeModel())) {
      @Override public void updateUI() {
        super.updateUI();
        setBackground(Color.WHITE);
        getViewport().setOpaque(false);
        setViewportBorder(BorderFactory.createEmptyBorder(1, 2, 1, 2));
      }
    };
    add(scroll);
    setPreferredSize(new Dimension(320, 240));
  }

  private static TableModel makeModel() {
    String[] columnNames = {"String", "Integer", "Boolean"};
    Object[][] data = {
        {"aaa", 12, true}, {"bbb", 5, false}, {"CCC", 92, true}, {"DDD", 0, false},
        {"eee", 32, true}, {"fff", 8, false}, {"ggg", 64, true}, {"hhh", 1, false}
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

class RoundedCellSelectionTable extends JTable {
  protected RoundedCellSelectionTable(TableModel model) {
    super(model);
  }

  @Override public void updateUI() {
    super.updateUI();
    setOpaque(false);
    setFocusable(false);
    setCellSelectionEnabled(true);
    setShowGrid(false);
    setIntercellSpacing(new Dimension());
    setAutoCreateRowSorter(true);
    setBackground(new Color(0x0, true));
    setRowHeight(20);
    if (getUI() instanceof SynthTableUI) {
      setDefaultRenderer(Boolean.class, new SynthBooleanTableCellRenderer2());
      // UIDefaults d = new UIDefaults();
      // d.put("Table:\"Table.cellRenderer\".opaque", Boolean.FALSE);
      // TableCellRenderer r = getDefaultRenderer(Boolean.class);
      // if (r instanceof JComponent) {
      //   JComponent c = (JComponent) r;
      //   c.putClientProperty("Nimbus.Overrides", d);
      //   c.putClientProperty("Nimbus.Overrides.InheritDefaults", Boolean.FALSE);
      // }
    }
  }

  @Override public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
    Component c = super.prepareRenderer(renderer, row, column);
    if (c instanceof JComponent) {
      ((JComponent) c).setOpaque(false);
    }
    return c;
  }

  @Override public Component prepareEditor(TableCellEditor editor, int row, int column) {
    Component c = super.prepareEditor(editor, row, column);
    if (c instanceof JComponent) {
      ((JComponent) c).setOpaque(false);
    }
    return c;
  }

  @Override public void changeSelection(int rowIndex, int columnIndex, boolean toggle, boolean extend) {
    super.changeSelection(rowIndex, columnIndex, toggle, extend);
    repaint();
  }

  @Override protected void paintComponent(Graphics g) {
    if (getSelectedColumnCount() != 0 && getSelectedRowCount() != 0) {
      Graphics2D g2 = (Graphics2D) g.create();
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2.setPaint(getSelectionBackground());
      Area area = new Area();
      for (int row : getSelectedRows()) {
        for (int col : getSelectedColumns()) {
          addArea(area, row, col);
        }
      }
      // Arrays.stream(getSelectedRows())
      //     .boxed()
      //     .flatMap(row -> Arrays.stream(getSelectedColumns())
      //         .filter(col -> isCellSelected(row, col))
      //         .mapToObj(col -> getCellRect(row, col, true))
      //         .map(Area::new))
      //     .forEach(area::add);

      int arc = 8;
      // if (!area.isEmpty()) {
      for (Area a : GeomUtils.singularization(area)) {
        // List<Point2D> lst = GeomUtils.convertAreaToPoint2DList(a);
        // g2.fill(GeomUtils.convertRoundedPath(lst, arc / 2d));
        Rectangle r = a.getBounds();
        g2.fillRoundRect(r.x, r.y, r.width - 1, r.height - 1, arc, arc);
      }
      g2.dispose();
    }
    super.paintComponent(g);
  }

  private void addArea(Area area, int row, int col) {
    if (isCellSelected(row, col)) {
      area.add(new Area(getCellRect(row, col, true)));
    }
  }
}

final class GeomUtils {
  private GeomUtils() {
    /* Singleton */
  }

  // public static List<Point2D> convertAreaToPoint2DList(Area area) {
  //   List<Point2D> list = new ArrayList<>();
  //   PathIterator pi = area.getPathIterator(null);
  //   double[] coords = new double[6];
  //   while (!pi.isDone()) {
  //     int pathSegmentType = pi.currentSegment(coords);
  //     switch (pathSegmentType) {
  //       case PathIterator.SEG_MOVETO:
  //       case PathIterator.SEG_LINETO:
  //         list.add(new Point2D.Double(coords[0], coords[1]));
  //         break;
  //       default:
  //         break;
  //     }
  //     pi.next();
  //   }
  //   return list;
  // }

  /**
   * Rounding the corners of a Rectilinear Polygon.
   */
  // public static Path2D convertRoundedPath(List<Point2D> list, double arc) {
  //   double kappa = 4d * (Math.sqrt(2d) - 1d) / 3d; // = 0.55228...;
  //   double akv = arc - arc * kappa;
  //   int sz = list.size();
  //   Point2D pt0 = list.get(0);
  //   Path2D path = new Path2D.Double();
  //   path.moveTo(pt0.getX() + arc, pt0.getY());
  //   for (int i = 0; i < sz; i++) {
  //     Point2D prv = list.get((i - 1 + sz) % sz);
  //     Point2D cur = list.get(i);
  //     Point2D nxt = list.get((i + 1) % sz);
  //     double dx0 = Math.signum(cur.getX() - prv.getX());
  //     double dy0 = Math.signum(cur.getY() - prv.getY());
  //     double dx1 = Math.signum(nxt.getX() - cur.getX());
  //     double dy1 = Math.signum(nxt.getY() - cur.getY());
  //     path.curveTo(
  //         cur.getX() - dx0 * akv, cur.getY() - dy0 * akv,
  //         cur.getX() + dx1 * akv, cur.getY() + dy1 * akv,
  //         cur.getX() + dx1 * arc, cur.getY() + dy1 * arc);
  //     path.lineTo(nxt.getX() - dx1 * arc, nxt.getY() - dy1 * arc);
  //   }
  //   path.closePath();
  //   return path;
  // }

  public static List<Area> singularization(Area rect) {
    List<Area> list = new ArrayList<>();
    Path2D path = new Path2D.Double();
    PathIterator pi = rect.getPathIterator(null);
    double[] coords = new double[6];
    while (!pi.isDone()) {
      switch (pi.currentSegment(coords)) {
        case PathIterator.SEG_MOVETO:
          path.moveTo(coords[0], coords[1]);
          break;
        case PathIterator.SEG_LINETO:
          path.lineTo(coords[0], coords[1]);
          break;
        case PathIterator.SEG_QUADTO:
          path.quadTo(coords[0], coords[1], coords[2], coords[3]);
          break;
        case PathIterator.SEG_CUBICTO:
          path.curveTo(coords[0], coords[1], coords[2], coords[3], coords[4], coords[5]);
          break;
        case PathIterator.SEG_CLOSE:
          path.closePath();
          list.add(new Area(path));
          path.reset();
          break;
        default:
          break;
      }
      pi.next();
    }
    return list;
  }
}

class SynthBooleanTableCellRenderer2 extends JCheckBox implements TableCellRenderer {
  @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    // isRowSelected = isSelected;
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

final class LookAndFeelUtils {
  private static String lookAndFeel = UIManager.getLookAndFeel().getClass().getName();

  private LookAndFeelUtils() {
    /* Singleton */
  }

  public static JMenu createLookAndFeelMenu() {
    JMenu menu = new JMenu("LookAndFeel");
    ButtonGroup buttonGroup = new ButtonGroup();
    for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
      AbstractButton b = makeButton(info);
      initLookAndFeelAction(info, b);
      menu.add(b);
      buttonGroup.add(b);
    }
    return menu;
  }

  private static AbstractButton makeButton(UIManager.LookAndFeelInfo info) {
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
        ex.printStackTrace();
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
