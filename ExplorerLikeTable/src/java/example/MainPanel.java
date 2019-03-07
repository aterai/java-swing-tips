// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageProducer;
import java.awt.image.RGBImageFilter;
import java.util.Objects;
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

    String[] columnNames = {"Name", "Comment"};
    Object[][] data = {
      {"test1.jpg", "adfasd"},
      {"test1234.jpg", "  "},
      {"test15354.gif", "fasdf"},
      {"t.png", "comment"},
      {"tfasdfasd.jpg", "123"},
      {"afsdfasdfffffffffffasdfasdf.mpg", "test"},
      {"fffffffffffasdfasdf", ""},
      {"test1.jpg", ""}
    };
    TableModel model = new DefaultTableModel(data, columnNames) {
      @Override public Class<?> getColumnClass(int column) {
        return getValueAt(0, column).getClass();
      }

      @Override public boolean isCellEditable(int row, int column) {
        return false;
      }
    };
    JTable table = new FileListTable(model);

    InputMap im = table.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    KeyStroke tab = KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0);
    KeyStroke enter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
    KeyStroke stab = KeyStroke.getKeyStroke(KeyEvent.VK_TAB, InputEvent.SHIFT_DOWN_MASK);
    KeyStroke senter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.SHIFT_DOWN_MASK);
    im.put(tab, im.get(enter));
    im.put(stab, im.get(senter));

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

    table.setComponentPopupMenu(new TablePopupMenu());
    add(new JScrollPane(table));
    setPreferredSize(new Dimension(320, 240));
  }

  public static void main(String... args) {
    EventQueue.invokeLater(new Runnable() {
      @Override public void run() {
        createAndShowGui();
      }
    });
  }

  public static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
      ex.printStackTrace();
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
    int g = (argb >> 8) & 0xFF;
    return (argb & 0xFF_00_00_FF) | ((r >> 1) << 16) | ((g >> 1) << 8);
    // return (argb & 0xFF_FF_FF_00) | ((argb & 0xFF) >> 1);
  }
}

class FileNameRenderer implements TableCellRenderer {
  protected final Dimension dim = new Dimension();
  private final JPanel renderer = new JPanel(new BorderLayout());
  private final JLabel textLabel = new JLabel(" ");
  private final JLabel iconLabel;
  private final Border focusBorder = UIManager.getBorder("Table.focusCellHighlightBorder");
  private final Border noFocusBorder;
  private final ImageIcon nicon;
  private final ImageIcon sicon;

  protected FileNameRenderer(JTable table) {
    Border b = UIManager.getBorder("Table.noFocusBorder");
    if (Objects.isNull(b)) { // Nimbus???
      Insets i = focusBorder.getBorderInsets(textLabel);
      b = BorderFactory.createEmptyBorder(i.top, i.left, i.bottom, i.right);
    }
    noFocusBorder = b;

    JPanel p = new JPanel(new BorderLayout()) {
      @Override public Dimension getPreferredSize() {
        return dim;
      }
    };
    p.setOpaque(false);
    renderer.setOpaque(false);

    // [XP Style Icons - Download](https://xp-style-icons.en.softonic.com/)
    nicon = new ImageIcon(getClass().getResource("wi0063-16.png"));

    ImageProducer ip = new FilteredImageSource(nicon.getImage().getSource(), new SelectedImageFilter());
    sicon = new ImageIcon(p.createImage(ip));

    iconLabel = new JLabel(nicon);
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
    int swidth = iconLabel.getPreferredSize().width + fm.stringWidth(textLabel.getText()) + i.left + i.right;
    int cwidth = table.getColumnModel().getColumn(column).getWidth();
    dim.width = Math.min(swidth, cwidth);

    if (isSelected) {
      textLabel.setOpaque(true);
      textLabel.setForeground(table.getSelectionForeground());
      textLabel.setBackground(table.getSelectionBackground());
      iconLabel.setIcon(sicon);
    } else {
      textLabel.setOpaque(false);
      textLabel.setForeground(table.getForeground());
      textLabel.setBackground(table.getBackground());
      iconLabel.setIcon(nicon);
    }
    return renderer;
  }
}

class FileListTable extends JTable {
  protected FileListTable(TableModel model) {
    super(model);
  }

  @Override public void updateUI() {
    // [JDK-6788475] Changing to Nimbus LAF and back doesn't reset look and feel of JTable completely - Java Bug System
    // https://bugs.openjdk.java.net/browse/JDK-6788475
    // XXX: set dummy ColorUIResource
    setSelectionForeground(new ColorUIResource(Color.RED));
    setSelectionBackground(new ColorUIResource(Color.RED));
    super.updateUI();

    putClientProperty("Table.isFileList", Boolean.TRUE);
    setCellSelectionEnabled(true);
    setIntercellSpacing(new Dimension());
    setShowGrid(false);
    setAutoCreateRowSorter(true);
    setFillsViewportHeight(true);

    setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
      @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        return super.getTableCellRendererComponent(table, value, false, false, row, column);
      }
    });

    TableColumn col = getColumnModel().getColumn(0);
    col.setCellRenderer(new FileNameRenderer(this));
    col.setPreferredWidth(200);
    col = getColumnModel().getColumn(1);
    col.setPreferredWidth(300);
  }
}

class TablePopupMenu extends JPopupMenu {
  private final JMenuItem delete;

  protected TablePopupMenu() {
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
