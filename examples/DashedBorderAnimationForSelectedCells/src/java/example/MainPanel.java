// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
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
    JTable table = new TranslucentCellSelectionTable(makeModel());
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

  private static TableModel makeModel() {
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

class TranslucentCellSelectionTable extends JTable {
  protected TranslucentCellSelectionTable(TableModel model) {
    super(model);
  }

  @Override public void updateUI() {
    super.updateUI();
    // If override JTable#paintComponent(...), need to use setOpaque(false)
    // setOpaque(false);
    setCellSelectionEnabled(true);
    setIntercellSpacing(new Dimension(3, 3));
    setAutoCreateRowSorter(true);
    setBackground(new Color(0x0, true));
    setRowHeight(20);
    if (getUI() instanceof SynthTableUI) {
      setDefaultRenderer(Boolean.class, new SynthBooleanTableCellRenderer2());
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
    c.setBackground(new Color(0x0, true));
    return c;
  }

  @Override public void editingStopped(ChangeEvent e) {
    super.editingStopped(e);
    repaint();
  }

  @Override public void changeSelection(int rowIndex, int columnIndex, boolean toggle, boolean extend) {
    super.changeSelection(rowIndex, columnIndex, toggle, extend);
    repaint();
  }
}

final class GeomUtils {
  private GeomUtils() {
    /* Singleton */
  }

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
}

class TranslucentCellSelectionLayerUI extends LayerUI<JScrollPane> {
  private static final float WIDTH = 2f;
  private static final float MITER = 5f;
  private static final float[] DASH = {4f, 2f};
  private static final Stroke BORDER_STROKE1 = new BasicStroke(
      WIDTH, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, MITER, DASH, 0f);
  private static final Stroke BORDER_STROKE2 = new BasicStroke(
      WIDTH, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, MITER, DASH, 2f);
  private static Stroke borderStroke = BORDER_STROKE1;
  private JTable table;
  private boolean flg;
  private final Timer animator = new Timer(480, e -> {
    if (table != null && !table.isEditing()) {
      borderStroke = flg ? BORDER_STROKE1 : BORDER_STROKE2;
      repaintSelectedArea(table);
      flg = !flg;
    }
  });

  @Override public void installUI(JComponent c) {
    super.installUI(c);
    if (c instanceof JLayer) {
      ((JLayer<?>) c).setLayerEventMask(AWTEvent.FOCUS_EVENT_MASK);
    }
  }

  @Override public void uninstallUI(JComponent c) {
    if (c instanceof JLayer) {
      ((JLayer<?>) c).setLayerEventMask(0);
    }
    super.uninstallUI(c);
  }

  @Override protected void processFocusEvent(FocusEvent e, JLayer<? extends JScrollPane> l) {
    table = getTable(l);
    if (e.getID() == FocusEvent.FOCUS_GAINED) {
      animator.start();
    } else {
      animator.stop();
    }
    super.processFocusEvent(e, l);
  }

  @Override public void paint(Graphics g, JComponent c) {
    super.paint(g, c);
    JTable table = getTable(c);
    int cc = table.getSelectedColumnCount();
    int rc = table.getSelectedRowCount();
    if (cc != 0 && rc != 0 && !table.isEditing()) {
      Graphics2D g2 = (Graphics2D) g.create();
      g2.setRenderingHint(
          RenderingHints.KEY_ANTIALIASING,
          RenderingHints.VALUE_ANTIALIAS_ON);
      Area area = new Area();
      getSelectedArea(table).forEach(r -> {
        Rectangle rect = SwingUtilities.convertRectangle(table, r, c);
        area.add(new Area(rect));
      });
      Dimension ics = table.getIntercellSpacing();
      Color v = table.getSelectionBackground();
      Color sbc = new Color(v.getRed(), v.getGreen(), v.getBlue(), 0x32);
      for (Area a : GeomUtils.singularization(area)) {
        Rectangle r = a.getBounds();
        r.width -= ics.width - 1;
        r.height -= ics.height - 1;
        g2.setPaint(sbc);
        g2.fill(r);
        g2.setPaint(v);
        g2.setStroke(borderStroke);
        g2.draw(r);
      }
      g2.dispose();
    }
  }

  private static List<Rectangle> getSelectedArea(JTable tbl) {
    List<Rectangle> list = new ArrayList<>();
    for (int row : tbl.getSelectedRows()) {
      for (int col : tbl.getSelectedColumns()) {
        if (tbl.isCellSelected(row, col)) {
          list.add(tbl.getCellRect(row, col, true));
        }
      }
    }
    return list;
  }

  private static JTable getTable(Component c) {
    JTable table = null;
    if (c instanceof JLayer) {
      Component c1 = ((JLayer<?>) c).getView();
      if (c1 instanceof JScrollPane) {
        table = (JTable) ((JScrollPane) c1).getViewport().getView();
      }
    }
    return table;
  }

  private static void repaintSelectedArea(JTable tbl) {
    int cc = tbl.getSelectedColumnCount();
    int rc = tbl.getSelectedRowCount();
    if (cc != 0 && rc != 0) {
      Area area = new Area();
      getSelectedArea(tbl).forEach(r -> area.add(new Area(r)));
      tbl.repaint(area.getBounds());
    }
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