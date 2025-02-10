// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.List;
import javax.swing.*;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.DimensionUIResource;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTable table = new JTable(makeModel()) {
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
        TableCellRenderer hr = new VerticalTableHeaderRenderer();
        TableColumnModel cm = getColumnModel();
        for (int i = 0; i < cm.getColumnCount(); i++) {
          TableColumn tc = cm.getColumn(i);
          tc.setHeaderRenderer(hr);
          tc.setPreferredWidth(32);
        }
        setAutoCreateRowSorter(true);
        setAutoResizeMode(AUTO_RESIZE_OFF);
      }
    };
    // table.setRowSorter(new TableRowSorter<>(table.getModel()));
    // table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

    JButton button = new JButton("clear SortKeys");
    button.addActionListener(e -> table.getRowSorter().setSortKeys(null));

    JMenuBar mb = new JMenuBar();
    mb.add(LookAndFeelUtils.createLookAndFeelMenu());
    EventQueue.invokeLater(() -> getRootPane().setJMenuBar(mb));

    add(button, BorderLayout.SOUTH);
    add(new JScrollPane(table));
    setPreferredSize(new Dimension(320, 240));
  }

  private static TableModel makeModel() {
    String[] columnNames = {"String", "Integer", "Boolean"};
    Object[][] data = {
        {"a", 12, true}, {"b", 5, false}, {"C", 92, true}, {"D", 0, false}
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

class VerticalTableHeaderRenderer implements TableCellRenderer {
  private static final String ASCENDING = "Table.ascendingSortIcon";
  private static final String DESCENDING = "Table.descendingSortIcon";
  private final Icon ascendingIcon;
  private final Icon descendingIcon;
  private final EmptyIcon emptyIcon;
  private final JPanel intermediate = new JPanel();
  private final JLabel label = new JLabel("", null, SwingConstants.LEADING);

  protected VerticalTableHeaderRenderer() {
    ascendingIcon = UIManager.getLookAndFeelDefaults().getIcon(ASCENDING);
    descendingIcon = UIManager.getLookAndFeelDefaults().getIcon(DESCENDING);
    emptyIcon = new EmptyIcon(ascendingIcon.getIconWidth(), ascendingIcon.getIconHeight());
  }

  @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    UIManager.put(ASCENDING, emptyIcon);
    UIManager.put(DESCENDING, emptyIcon);
    TableCellRenderer r = table.getTableHeader().getDefaultRenderer();
    Component c = r.getTableCellRendererComponent(
        table, value, isSelected, hasFocus, row, column);
    UIManager.put(ASCENDING, ascendingIcon);
    UIManager.put(DESCENDING, descendingIcon);
    if (c instanceof JLabel) {
      JLabel l = (JLabel) c;
      l.setHorizontalAlignment(SwingConstants.CENTER);
      SortOrder sortOrder = getColumnSortOrder(table, column);
      Icon sortIcon;
      switch (sortOrder) {
        case ASCENDING:
          sortIcon = ascendingIcon;
          break;
        case DESCENDING:
          sortIcon = descendingIcon;
          break;
        default: // case UNSORTED:
          sortIcon = emptyIcon;
          break;
      }
      label.setText(l.getText());
      label.setIcon(new RotateIcon(sortIcon, 90));
      label.setHorizontalTextPosition(SwingConstants.LEFT);
      label.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));

      // l.setIcon(new RotateIcon(new ComponentIcon(label), -90));
      l.setIcon(makeVerticalHeaderIcon(label));
      l.setText(null);
    }
    return c;
  }

  // https://github.com/aterai/java-swing-tips/blob/master/SortIconLayoutHeaderRenderer/src/java/example/MainPanel.java
  public static SortOrder getColumnSortOrder(JTable table, int column) {
    SortOrder rv = SortOrder.UNSORTED;
    if (table != null && table.getRowSorter() != null) {
      List<? extends RowSorter.SortKey> sortKeys = table.getRowSorter().getSortKeys();
      int mi = table.convertColumnIndexToModel(column);
      if (!sortKeys.isEmpty() && sortKeys.get(0).getColumn() == mi) {
        rv = sortKeys.get(0).getSortOrder();
      }
    }
    return rv;
  }

  // https://github.com/aterai/java-swing-tips/blob/master/RotatedVerticalTextTabs/src/java/example/MainPanel.java
  private Icon makeVerticalHeaderIcon(Component c) {
    Dimension d = c.getPreferredSize();
    int w = d.height;
    int h = d.width;
    BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2 = (Graphics2D) bi.getGraphics();
    AffineTransform at = AffineTransform.getTranslateInstance(0, h);
    at.quadrantRotate(-1);
    g2.setTransform(at);
    SwingUtilities.paintComponent(g2, c, intermediate, 0, 0, h, w);
    g2.dispose();
    return new ImageIcon(bi);
  }

  // // https://github.com/aterai/java-swing-tips/blob/master/TableHeaderCheckBox/src/java/example/MainPanel.java
  // class ComponentIcon implements Icon {
  //   private final Component cmp;
  //
  //   protected ComponentIcon(Component cmp) {
  //     this.cmp = cmp;
  //   }
  //
  //   @Override public void paintIcon(Component c, Graphics g, int x, int y) {
  //     SwingUtilities.paintComponent(g, cmp, p, x, y, getIconWidth(), getIconHeight());
  //   }
  //
  //   @Override public int getIconWidth() {
  //     return cmp.getPreferredSize().width;
  //   }
  //
  //   @Override public int getIconHeight() {
  //     return cmp.getPreferredSize().height;
  //   }
  // }
}

class EmptyIcon implements Icon {
  private final int width;
  private final int height;

  protected EmptyIcon(int width, int height) {
    this.width = width;
    this.height = height;
  }

  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    /* do nothing */
  }

  @Override public int getIconWidth() {
    return width;
  }

  @Override public int getIconHeight() {
    return height;
  }
}

// https://github.com/aterai/java-swing-tips/blob/master/RotatedIcon/src/java/example/MainPanel.java
class RotateIcon implements Icon {
  private final Dimension dim = new Dimension();
  private final Image image;
  private final AffineTransform trans;

  protected RotateIcon(Icon icon, int rotate) {
    if (rotate % 90 != 0) {
      throw new IllegalArgumentException(rotate + ": Rotate must be (rotate % 90 == 0)");
    }
    dim.setSize(icon.getIconWidth(), icon.getIconHeight());
    image = new BufferedImage(dim.width, dim.height, BufferedImage.TYPE_INT_ARGB);
    Graphics g = image.getGraphics();
    icon.paintIcon(null, g, 0, 0);
    g.dispose();

    int numquadrants = rotate / 90 % 4;
    switch (numquadrants) {
      case 3:
      case -1:
        trans = AffineTransform.getTranslateInstance(0, dim.width);
        dim.setSize(icon.getIconHeight(), icon.getIconWidth());
        break;
      case 1:
      case -3:
        trans = AffineTransform.getTranslateInstance(dim.height, 0);
        dim.setSize(icon.getIconHeight(), icon.getIconWidth());
        break;
      case 2:
        trans = AffineTransform.getTranslateInstance(dim.width, dim.height);
        break;
      default:
        trans = AffineTransform.getTranslateInstance(0, 0);
        break;
    }
    trans.quadrantRotate(numquadrants);
    // or: trans.concatenate(AffineTransform.getQuadrantRotateInstance(numquadrants));
    // or: trans.rotate(Math.PI / 2.0 * numquadrants);
    // or: trans.rotate(Math.toRadians(90d * numquadrants));
  }

  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.translate(x, y);
    g2.drawImage(image, trans, c);
    g2.dispose();
  }

  @Override public int getIconWidth() {
    return dim.width;
  }

  @Override public int getIconHeight() {
    return dim.height;
  }
}

// @see SwingSet3/src/com/sun/swingset3/SwingSet3.java
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
