// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    String[] columnNames = {"String", "Integer", "Boolean"};
    Object[][] data = {
        {"a", 12, true}, {"b", 5, false}, {"C", 92, true}, {"D", 0, false}
    };
    TableModel model = new DefaultTableModel(data, columnNames) {
      @Override public Class<?> getColumnClass(int column) {
        return getValueAt(0, column).getClass();
      }
    };
    JTable table = new JTable(model) {
      @Override public void updateUI() {
        super.updateUI();
        TableCellRenderer hr = new VerticalTableHeaderRenderer();
        TableColumnModel cm = getColumnModel();
        for (int i = 0; i < cm.getColumnCount(); i++) {
          TableColumn tc = cm.getColumn(i);
          tc.setHeaderRenderer(hr);
          tc.setPreferredWidth(32);
        }
      }
    };
    TableRowSorter<TableModel> sorter = new TableRowSorter<>(model);
    table.setRowSorter(sorter);
    table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

    JButton button = new JButton("clear SortKeys");
    button.addActionListener(e -> sorter.setSortKeys(null));

    JMenuBar mb = new JMenuBar();
    mb.add(LookAndFeelUtil.createLookAndFeelMenu());
    EventQueue.invokeLater(() -> getRootPane().setJMenuBar(mb));

    add(button, BorderLayout.SOUTH);
    add(new JScrollPane(table));
    setPreferredSize(new Dimension(320, 240));
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
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

class VerticalTableHeaderRenderer implements TableCellRenderer {
  private static final String ASCENDING = "Table.ascendingSortIcon";
  private static final String DESCENDING = "Table.descendingSortIcon";
  private final Icon ascendingIcon;
  private final Icon descendingIcon;
  private final EmptyIcon emptyIcon = new EmptyIcon();
  private final JPanel intermediate = new JPanel();
  private final JLabel label = new JLabel("", null, SwingConstants.LEADING);

  protected VerticalTableHeaderRenderer() {
    ascendingIcon = UIManager.getLookAndFeelDefaults().getIcon(ASCENDING);
    descendingIcon = UIManager.getLookAndFeelDefaults().getIcon(DESCENDING);
    emptyIcon.width = ascendingIcon.getIconWidth();
    emptyIcon.height = ascendingIcon.getIconHeight();
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
  private Icon makeVerticalHeaderIcon(Component label) {
    Dimension d = label.getPreferredSize();
    int w = d.height;
    int h = d.width;
    BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2 = (Graphics2D) bi.getGraphics();
    AffineTransform at = AffineTransform.getTranslateInstance(0, h);
    at.quadrantRotate(-1);
    g2.setTransform(at);
    SwingUtilities.paintComponent(g2, label, intermediate, 0, 0, h, w);
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
  protected int width = 5;
  protected int height = 5;

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

    int numquadrants = (rotate / 90) % 4;
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
    trans.rotate(Math.toRadians(90d * numquadrants));
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

final class LookAndFeelUtil {
  private static String lookAndFeel = UIManager.getLookAndFeel().getClass().getName();

  private LookAndFeelUtil() {
    /* Singleton */
  }

  public static JMenu createLookAndFeelMenu() {
    JMenu menu = new JMenu("LookAndFeel");
    ButtonGroup lafGroup = new ButtonGroup();
    for (UIManager.LookAndFeelInfo lafInfo : UIManager.getInstalledLookAndFeels()) {
      menu.add(createLookAndFeelItem(lafInfo.getName(), lafInfo.getClassName(), lafGroup));
    }
    return menu;
  }

  private static JMenuItem createLookAndFeelItem(String laf, String lafClass, ButtonGroup bg) {
    JMenuItem lafItem = new JRadioButtonMenuItem(laf, lafClass.equals(lookAndFeel));
    lafItem.setActionCommand(lafClass);
    lafItem.setHideActionText(true);
    lafItem.addActionListener(e -> {
      ButtonModel m = bg.getSelection();
      try {
        setLookAndFeel(m.getActionCommand());
      } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
        UIManager.getLookAndFeel().provideErrorFeedback((Component) e.getSource());
      }
    });
    bg.add(lafItem);
    return lafItem;
  }

  private static void setLookAndFeel(String lookAndFeel) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
    String oldLookAndFeel = LookAndFeelUtil.lookAndFeel;
    if (!oldLookAndFeel.equals(lookAndFeel)) {
      UIManager.setLookAndFeel(lookAndFeel);
      LookAndFeelUtil.lookAndFeel = lookAndFeel;
      updateLookAndFeel();
      // firePropertyChange("lookAndFeel", oldLookAndFeel, lookAndFeel);
    }
  }

  private static void updateLookAndFeel() {
    for (Window window : Window.getWindows()) {
      SwingUtilities.updateComponentTreeUI(window);
    }
  }
}
