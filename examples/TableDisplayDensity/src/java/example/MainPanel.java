// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.DimensionUIResource;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTable table = new ScaledTable(makeModel());
    table.setAutoCreateRowSorter(true);
    add(makeToolBar(table), BorderLayout.NORTH);
    add(new JScrollPane(table));
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

  private static JToolBar makeToolBar(JTable table) {
    Font font = table.getFont();
    float fontSize = font.getSize2D();
    int rowHeight = table.getRowHeight();
    JToggleButton b1 = new JToggleButton("*1", true);
    b1.addActionListener(e -> scaling(table, font, fontSize, rowHeight, 1f));
    JToggleButton b2 = new JToggleButton("*1.5");
    b2.addActionListener(e -> scaling(table, font, fontSize, rowHeight, 1.5f));
    JToggleButton b3 = new JToggleButton("*2");
    b3.addActionListener(e -> scaling(table, font, fontSize, rowHeight, 2f));
    JToolBar toolBar = new JToolBar();
    ButtonGroup group = new ButtonGroup();
    for (JToggleButton b : Arrays.asList(b1, b2, b3)) {
      b.setFocusable(false);
      group.add(b);
      toolBar.add(b);
      toolBar.add(Box.createHorizontalStrut(5));
    }
    return toolBar;
  }

  private static void scaling(JTable table, Font font, float fontSize, int rowHeight, float x) {
    table.removeEditor();
    Font f = font.deriveFont(fontSize * x);
    table.setFont(f);
    table.getTableHeader().setFont(f);
    table.setRowHeight((int) (.5f + rowHeight * x));
    // // IntercellSpacing
    // Dimension d = new Dimension(1, 1);
    // d.width = (int) (.5f + d.width * x);
    // d.height = (int) (.5f + d.height * x);
    // table.setIntercellSpacing(d);
    // // SortIcon:
    // UIDefaults def = UIManager.getLookAndFeelDefaults();
    // Icon ascending = def.getIcon("Table.ascendingSortIcon");
    // Icon descending = def.getIcon("Table.descendingSortIcon");
    // int w = (int) (.5f + ascending.getIconWidth() * x);
    // int h = (int) (.5f + ascending.getIconHeight() * x);
    // UIManager.put("Table.ascendingSortIcon", new ScaledIcon(ascending, w, h));
    // UIManager.put("Table.descendingSortIcon", new ScaledIcon(descending, w, h));
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

class ScaledTable extends JTable {
  private final Insets iconIns = new Insets(2, 2, 2, 2);
  private final transient Icon checkIcon = new CheckBoxIcon();

  protected ScaledTable(TableModel model) {
    super(model);
  }

  @Override public void updateUI() {
    ColorUIResource reset = new ColorUIResource(Color.RED);
    setSelectionForeground(reset);
    setSelectionBackground(reset);
    super.updateUI();
    UIDefaults def = UIManager.getLookAndFeelDefaults();
    Object showGrid = def.get("Table.showGrid");
    Color gridColor = def.getColor("Table.gridColor");
    if (showGrid == null && gridColor != null) {
      setShowGrid(true);
      setIntercellSpacing(new DimensionUIResource(1, 1));
      createDefaultRenderers();
    }
  }

  @Override public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
    Component c = super.prepareRenderer(renderer, row, column);
    // c.setFont(getFont());
    if (c instanceof JCheckBox) {
      JCheckBox cb = (JCheckBox) c;
      cb.setBorderPainted(false);
      updateCheckIcon(cb);
    }
    return c;
  }

  @Override public Component prepareEditor(TableCellEditor editor, int row, int column) {
    Component c = super.prepareEditor(editor, row, column);
    c.setFont(getFont());
    if (c instanceof JCheckBox) {
      JCheckBox cb = (JCheckBox) c;
      cb.setForeground(getSelectionForeground());
      cb.setBackground(getSelectionBackground());
      cb.setBorderPainted(false);
      updateCheckIcon(cb);
    }
    return c;
  }

  private void updateCheckIcon(JCheckBox checkBox) {
    int s = getRowHeight() - iconIns.top - iconIns.bottom;
    checkBox.setIcon(new ScaledIcon(checkIcon, s, s));
  }
}

class ScaledIcon implements Icon {
  private final Icon icon;
  private final int width;
  private final int height;

  protected ScaledIcon(Icon icon, int width, int height) {
    this.icon = icon;
    this.width = width;
    this.height = height;
  }

  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.translate(x, y);
    double sx = width / (double) icon.getIconWidth();
    double sy = height / (double) icon.getIconHeight();
    g2.scale(sx, sy);
    icon.paintIcon(c, g2, 0, 0);
    g2.dispose();
  }

  @Override public int getIconWidth() {
    return width;
  }

  @Override public int getIconHeight() {
    return height;
  }
}

class CheckBoxIcon implements Icon {
  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    if (c instanceof AbstractButton) {
      Graphics2D g2 = (Graphics2D) g.create();
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2.translate(x, y);
      AbstractButton b = (AbstractButton) c;
      g2.setPaint(b.getForeground()); // g2.setPaint(Color.DARK_GRAY);
      float s = Math.min(getIconWidth(), getIconHeight()) * .05f;
      g2.setStroke(new BasicStroke(s));
      float w = getIconWidth() - s - s;
      float h = getIconHeight() - s - s;
      g2.draw(new Rectangle2D.Float(s, s, w, h));
      float gw = w / 8f;
      float gh = h / 8f;
      if (b.getModel().isSelected()) {
        g2.setStroke(new BasicStroke(3f * s));
        Path2D p = new Path2D.Float();
        p.moveTo(x + 2f * gw, y + .5f * h);
        p.lineTo(x + .4f * w, y + h - 2f * gh);
        p.lineTo(x + w - 2f * gw, y + 2f * gh);
        g2.draw(p);
      }
      g2.dispose();
    }
  }

  @Override public int getIconWidth() {
    return 1000;
  }

  @Override public int getIconHeight() {
    return 1000;
  }
}
