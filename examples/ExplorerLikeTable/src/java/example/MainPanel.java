// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageProducer;
import java.awt.image.RGBImageFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.plaf.ColorUIResource;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTable table = new FileListTable(makeModel());
    table.setComponentPopupMenu(new TablePopupMenu());

    KeyStroke tab = KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0);
    KeyStroke stab = KeyStroke.getKeyStroke(KeyEvent.VK_TAB, InputEvent.SHIFT_DOWN_MASK);
    InputMap im = table.getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    im.put(tab, im.get(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0)));
    im.put(stab, im.get(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.SHIFT_DOWN_MASK)));

    Color orgColor = table.getSelectionBackground();
    Color tflColor = this.getBackground();
    table.addFocusListener(new FocusListener() {
      @Override public void focusGained(FocusEvent e) {
        table.setSelectionForeground(Color.WHITE);
        table.setSelectionBackground(orgColor);
      }

      @Override public void focusLost(FocusEvent e) {
        table.setSelectionForeground(Color.BLACK);
        table.setSelectionBackground(tflColor);
      }
    });

    add(new JScrollPane(table));
    setPreferredSize(new Dimension(320, 240));
  }

  private static TableModel makeModel() {
    String[] columnNames = {"Name", "Comment"};
    Object[][] data = {
        {"test1.jpg", "11111"},
        {"test1234.jpg", "  "},
        {"test15354.gif", "22222"},
        {"t.png", "comment"},
        {"33333.jpg", "123"},
        {"4444444444444444.mpg", "test"},
        {"5555555555555", ""},
        {"test1.jpg", ""}
    };
    return new DefaultTableModel(data, columnNames) {
      @Override public Class<?> getColumnClass(int column) {
        return getValueAt(0, column).getClass();
      }

      @Override public boolean isCellEditable(int row, int column) {
        return false;
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

class SelectedImageFilter extends RGBImageFilter {
  // public SelectedImageFilter() {
  //   canFilterIndexColorModel = false;
  // }

  @Override public int filterRGB(int x, int y, int argb) {
    int r = (argb >> 16) & 0xFF;
    int r2 = r >> 1;
    int g = (argb >> 8) & 0xFF;
    int g2 = g >> 1;
    return argb & 0xFF_00_00_FF | r2 << 16 | g2 << 8;
    // return argb & 0xFF_FF_FF_00 | (argb & 0xFF) >> 1;
  }
}

class FileNameRenderer implements TableCellRenderer {
  private final Dimension dim = new Dimension();
  private final JPanel renderer = new JPanel(new BorderLayout());
  private final JLabel textLabel = new JLabel(" ");
  private final JLabel iconLabel;
  private final Border focusBorder = UIManager.getBorder("Table.focusCellHighlightBorder");
  private final Border noFocusBorder;
  private final ImageIcon icon;
  private final ImageIcon selectedIcon;

  protected FileNameRenderer(JTable table) {
    Border b = UIManager.getBorder("Table.noFocusBorder");
    noFocusBorder = Optional.ofNullable(b).orElseGet(() -> {
      Insets i = focusBorder.getBorderInsets(textLabel);
      return BorderFactory.createEmptyBorder(i.top, i.left, i.bottom, i.right);
    });

    JPanel p = new JPanel(new BorderLayout()) {
      @Override public Dimension getPreferredSize() {
        return dim;
      }
    };
    p.setOpaque(false);
    renderer.setOpaque(false);

    // [XP Style Icons - Download](https://xp-style-icons.en.softonic.com/)
    Image image = makeImage("example/wi0063-16.png");
    icon = new ImageIcon(image);

    ImageProducer ip = new FilteredImageSource(image.getSource(), new SelectedImageFilter());
    selectedIcon = new ImageIcon(p.createImage(ip));

    iconLabel = new JLabel(icon);
    iconLabel.setBorder(BorderFactory.createEmptyBorder());

    p.add(iconLabel, BorderLayout.WEST);
    p.add(textLabel);
    renderer.add(p, BorderLayout.WEST);

    Dimension d = iconLabel.getPreferredSize();
    dim.setSize(d);
    table.setRowHeight(d.height);
  }

  @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    textLabel.setFont(table.getFont());
    textLabel.setText(Objects.toString(value, ""));
    textLabel.setBorder(hasFocus ? focusBorder : noFocusBorder);

    FontMetrics fm = table.getFontMetrics(table.getFont());
    Insets i = textLabel.getInsets();
    String text = textLabel.getText();
    int width = iconLabel.getPreferredSize().width + fm.stringWidth(text) + i.left + i.right;
    int colWidth = table.getColumnModel().getColumn(column).getWidth();
    dim.width = Math.min(width, colWidth);

    if (isSelected) {
      textLabel.setOpaque(true);
      textLabel.setForeground(table.getSelectionForeground());
      textLabel.setBackground(table.getSelectionBackground());
      iconLabel.setIcon(selectedIcon);
    } else {
      textLabel.setOpaque(false);
      textLabel.setForeground(table.getForeground());
      textLabel.setBackground(table.getBackground());
      iconLabel.setIcon(icon);
    }
    return renderer;
  }

  public static Image makeImage(String path) {
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    return Optional.ofNullable(cl.getResource(path)).map(url -> {
      try (InputStream s = url.openStream()) {
        return ImageIO.read(s);
      } catch (IOException ex) {
        return makeMissingImage();
      }
    }).orElseGet(FileNameRenderer::makeMissingImage);
  }

  private static BufferedImage makeMissingImage() {
    Icon missingIcon = UIManager.getIcon("html.missingImage");
    int iw = missingIcon.getIconWidth();
    int ih = missingIcon.getIconHeight();
    BufferedImage bi = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2 = bi.createGraphics();
    missingIcon.paintIcon(null, g2, (16 - iw) / 2, (16 - ih) / 2);
    g2.dispose();
    return bi;
  }
}

class FileListTable extends JTable {
  protected FileListTable(TableModel model) {
    super(model);
  }

  @Override public void updateUI() {
    // [JDK-6788475]
    // Changing to Nimbus LAF and back doesn't reset look and feel of JTable completely
    // https://bugs.openjdk.org/browse/JDK-6788475
    // Set a temporary ColorUIResource to avoid this issue
    setSelectionForeground(new ColorUIResource(Color.RED));
    setSelectionBackground(new ColorUIResource(Color.RED));
    setDefaultRenderer(Object.class, null);
    super.updateUI();

    putClientProperty("Table.isFileList", Boolean.TRUE);
    setCellSelectionEnabled(true);
    setIntercellSpacing(new Dimension());
    setShowGrid(false);
    setAutoCreateRowSorter(true);
    setFillsViewportHeight(true);

    TableCellRenderer r = new DefaultTableCellRenderer();
    setDefaultRenderer(Object.class, (table, value, isSelected, hasFocus, row, column) ->
        r.getTableCellRendererComponent(table, value, false, false, row, column));

    TableColumn col = getColumnModel().getColumn(0);
    col.setCellRenderer(new FileNameRenderer(this));
    col.setPreferredWidth(200);
    col = getColumnModel().getColumn(1);
    col.setPreferredWidth(300);
  }
}

final class TablePopupMenu extends JPopupMenu {
  private final JMenuItem delete;

  /* default */ TablePopupMenu() {
    super();
    add("add").addActionListener(e -> {
      JTable table = (JTable) getInvoker();
      DefaultTableModel model = (DefaultTableModel) table.getModel();
      model.addRow(new Object[] {"New row", model.getRowCount(), false});
      Rectangle r = table.getCellRect(model.getRowCount() - 1, 0, true);
      table.scrollRectToVisible(r);
    });
    add("clearSelection").addActionListener(e -> ((JTable) getInvoker()).clearSelection());
    addSeparator();
    delete = add("delete");
    delete.addActionListener(e -> {
      JTable table = (JTable) getInvoker();
      DefaultTableModel model = (DefaultTableModel) table.getModel();
      int[] selection = table.getSelectedRows();
      for (int i = selection.length - 1; i >= 0; i--) {
        model.removeRow(table.convertRowIndexToModel(selection[i]));
      }
    });
  }

  @Override public void show(Component c, int x, int y) {
    if (c instanceof JTable) {
      delete.setEnabled(((JTable) c).getSelectedRowCount() > 0);
      super.show(c, x, y);
    }
  }
}
