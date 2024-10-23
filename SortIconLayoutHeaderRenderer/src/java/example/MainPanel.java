// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    UIManager.put("TableHeader.rightAlignSortArrow", Boolean.FALSE);
    JTable table = new JTable(makeModel()) {
      @Override public void updateUI() {
        super.updateUI();
        DefaultTableCellRenderer cr = new DefaultTableCellRenderer();
        cr.setHorizontalAlignment(SwingConstants.RIGHT);
        TableCellRenderer hr = new SortIconLayoutHeaderRenderer();
        TableColumnModel cm = getColumnModel();
        for (int i = 1; i < cm.getColumnCount(); i++) {
          TableColumn tc = cm.getColumn(i);
          tc.setHeaderRenderer(hr);
          tc.setCellRenderer(cr);
        }
      }
    };
    table.setAutoCreateRowSorter(true);
    JMenuBar mb = new JMenuBar();
    mb.add(LookAndFeelUtils.createLookAndFeelMenu());
    EventQueue.invokeLater(() -> getRootPane().setJMenuBar(mb));
    add(new JScrollPane(table));
    setPreferredSize(new Dimension(320, 240));
  }

  private static TableModel makeModel() {
    String[] columnNames = {"Name", "CPU", "Memory", "Disk"};
    Object[][] data = {
        {"aaa", "1%", "1.6MB", "0MB/S"}, {"bbb", "1%", "2.4MB", "3MB/S"},
        {"ccc", "2%", "0.3MB", "1MB/S"}, {"ddd", "3%", "0.5MB", "2MB/S"}
    };
    return new DefaultTableModel(data, columnNames) {
      @Override public Class<?> getColumnClass(int column) {
        return String.class;
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

class SortIconLayoutHeaderRenderer implements TableCellRenderer {
  private static final String ASCENDING = "Table.ascendingSortIcon";
  private static final String DESCENDING = "Table.descendingSortIcon";
  private final URI ascendingUri;
  private final URI descendingUri;
  private final URI naturalUri;
  private final Icon ascendingIcon;
  private final Icon descendingIcon;
  private final EmptyIcon emptyIcon;

  protected SortIconLayoutHeaderRenderer() {
    ascendingIcon = UIManager.getLookAndFeelDefaults().getIcon(ASCENDING);
    ascendingUri = getIconUri(ascendingIcon);
    descendingIcon = UIManager.getLookAndFeelDefaults().getIcon(DESCENDING);
    descendingUri = getIconUri(descendingIcon);
    emptyIcon = new EmptyIcon();
    naturalUri = getIconUri(emptyIcon);
  }

  @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    emptyIcon.width = ascendingIcon.getIconWidth();
    emptyIcon.height = ascendingIcon.getIconHeight();
    UIManager.put(ASCENDING, emptyIcon);
    UIManager.put(DESCENDING, emptyIcon);
    TableCellRenderer r = table.getTableHeader().getDefaultRenderer();
    Component c = r.getTableCellRendererComponent(
        table, value, isSelected, hasFocus, row, column);
    UIManager.put(ASCENDING, ascendingIcon);
    UIManager.put(DESCENDING, descendingIcon);
    if (c instanceof JLabel) {
      JLabel l = (JLabel) c;
      // l.setHorizontalAlignment(SwingConstants.RIGHT);
      URI sortUri;
      SortOrder sortOrder = getColumnSortOrder(table, column);
      switch (sortOrder) {
        case ASCENDING:
          sortUri = ascendingUri;
          break;
        case DESCENDING:
          sortUri = descendingUri;
          break;
        default: // case UNSORTED:
          sortUri = naturalUri;
          break;
      }
      int v = 10;
      String img = String.format("<img src='%s'>", sortUri);
      String pct = String.format("<td align='right'>%d%%", v);
      String fmt = "<html><table><tr><td>%s%s<tr><td><td align='right'>%s";
      l.setText(String.format(fmt, img, pct, Objects.toString(value, "")));
    }
    return c;
  }

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

  public static URI getIconUri(Icon icon) {
    int w = icon.getIconWidth();
    int h = icon.getIconHeight();
    BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2 = img.createGraphics();
    icon.paintIcon(null, g2, 0, 0);
    g2.dispose();
    return makeTempFile(img).map(File::toURI).orElse(null);
  }

  private static Optional<File> makeTempFile(BufferedImage img) {
    Optional<File> op;
    try {
      File file = File.createTempFile("icon", ".png");
      file.deleteOnExit();
      ImageIO.write(img, "png", file);
      op = Optional.of(file);
    } catch (IOException ex) {
      op = Optional.empty();
    }
    return op;
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
