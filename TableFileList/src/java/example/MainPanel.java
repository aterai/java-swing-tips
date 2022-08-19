// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageProducer;
import java.awt.image.RGBImageFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.IntStream;
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
    TableModel model = new DefaultTableModel(data, columnNames) {
      @Override public Class<?> getColumnClass(int column) {
        return getValueAt(0, column).getClass();
      }

      @Override public boolean isCellEditable(int row, int column) {
        return false;
      }
    };
    JTable table = new FileListTable(model);
    add(new JScrollPane(table));
    setPreferredSize(new Dimension(320, 240));
  }

  // private static int getStringWidth(JTable table, int row, int column) {
  //   FontMetrics fm = table.getFontMetrics(table.getFont());
  //   Object o = table.getValueAt(row, column);
  //   return fm.stringWidth(o.toString()) + ICON_SIZE + 2 + 2;
  // }

  // private static boolean isOnLabel(JTable table, Point pt, int row, int col) {
  //   Rectangle rect = table.getCellRect(row, col, true);
  //   rect.setSize(getStringWidth(table, row, col), rect.height);
  //   return(rect.contains(pt));
  // }

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
  private final ImageIcon icon;
  private final ImageIcon selectedIcon;

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

  private static Image makeMissingImage() {
    Icon missingIcon = UIManager.getIcon("html.missingImage");
    int iw = missingIcon.getIconWidth();
    int ih = missingIcon.getIconHeight();
    BufferedImage bi = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2 = bi.createGraphics();
    missingIcon.paintIcon(null, g2, (16 - iw) / 2, (16 - ih) / 2);
    g2.dispose();
    return bi;
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
}

class FileListTable extends JTable {
  private static final Color BAND_COLOR = makeRubberBandColor(SystemColor.activeCaption);
  private static final Path2D RUBBER_BAND = new Path2D.Double();
  private transient RubberBandingListener rbl;

  protected FileListTable(TableModel model) {
    super(model);
  }

  @Override public void updateUI() {
    // [JDK-6788475]
    // Changing to Nimbus LAF and back doesn't reset look and feel of JTable completely
    // https://bugs.openjdk.org/browse/JDK-6788475
    // XXX: set dummy ColorUIResource
    setSelectionForeground(new ColorUIResource(Color.RED));
    setSelectionBackground(new ColorUIResource(Color.RED));
    removeMouseMotionListener(rbl);
    removeMouseListener(rbl);
    setDefaultRenderer(Object.class, null);
    super.updateUI();
    rbl = new RubberBandingListener();
    addMouseMotionListener(rbl);
    addMouseListener(rbl);

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

  @Override public String getToolTipText(MouseEvent e) {
    Point pt = e.getPoint();
    int row = rowAtPoint(pt);
    int col = columnAtPoint(pt);
    if (convertColumnIndexToModel(col) != 0 || row < 0 || row > getRowCount()) {
      return null;
    }
    Rectangle rect = getCellRect2(this, row, col);
    if (rect.contains(pt)) {
      return getValueAt(row, col).toString();
    }
    return null;
  }

  @Override public void setColumnSelectionInterval(int index0, int index1) {
    int idx = convertColumnIndexToView(0);
    super.setColumnSelectionInterval(idx, idx);
  }

  protected Path2D getRubberBand() {
    return RUBBER_BAND;
  }

  private class RubberBandingListener extends MouseAdapter {
    private final Point srcPoint = new Point();

    @Override public void mouseDragged(MouseEvent e) {
      Point destPoint = e.getPoint();
      Path2D rb = getRubberBand();
      rb.reset();
      rb.moveTo(srcPoint.x, srcPoint.y);
      rb.lineTo(destPoint.x, srcPoint.y);
      rb.lineTo(destPoint.x, destPoint.y);
      rb.lineTo(srcPoint.x, destPoint.y);
      rb.closePath();
      clearSelection();
      int col = convertColumnIndexToView(0);
      int[] indices = IntStream.range(0, getModel().getRowCount())
          .filter(i -> rb.intersects(getCellRect2(FileListTable.this, i, col)))
          .toArray();
      for (int i : indices) {
        addRowSelectionInterval(i, i);
        changeSelection(i, col, true, true);
      }
      e.getComponent().repaint();
    }

    @Override public void mouseReleased(MouseEvent e) {
      getRubberBand().reset();
      e.getComponent().repaint();
    }

    @Override public void mousePressed(MouseEvent e) {
      srcPoint.setLocation(e.getPoint());
      if (rowAtPoint(e.getPoint()) < 0) {
        clearSelection();
        e.getComponent().repaint();
      } else {
        int index = rowAtPoint(e.getPoint());
        Rectangle rect = getCellRect2(FileListTable.this, index, convertColumnIndexToView(0));
        if (!rect.contains(e.getPoint())) {
          clearSelection();
          e.getComponent().repaint();
        }
      }
    }
  }

  // SwingUtilities2.pointOutsidePrefSize(...)
  protected static Rectangle getCellRect2(JTable table, int row, int col) {
    TableCellRenderer tcr = table.getCellRenderer(row, col);
    Object value = table.getValueAt(row, col);
    Component cell = tcr.getTableCellRendererComponent(table, value, false, false, row, col);
    Dimension itemSize = cell.getPreferredSize();
    Rectangle cellBounds = table.getCellRect(row, col, false);
    cellBounds.width = itemSize.width;
    return cellBounds;
  }

  @Override protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setPaint(SystemColor.activeCaption);
    g2.draw(RUBBER_BAND);
    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .1f));
    g2.setPaint(BAND_COLOR);
    g2.fill(RUBBER_BAND);
    g2.dispose();
  }

  // private int[] getIntersectedIndices(Path2D path) {
  //   TableModel model = getModel();
  //   List<Integer> list = new ArrayList<>(model.getRowCount());
  //   for (int i = 0; i < getRowCount(); i++) {
  //     if (path.intersects(getCellRect2(FileListTable.this, i, convertColumnIndexToView(0)))) {
  //       list.add(i);
  //     }
  //   }
  //   int[] il = new int[list.size()];
  //   for (int i = 0; i < list.size(); i++) {
  //     il[i] = list.get(i);
  //   }
  //   return il;
  // }

  public static Color makeRubberBandColor(Color c) {
    int r = c.getRed();
    int g = c.getGreen();
    int b = c.getBlue();
    int max = Math.max(Math.max(r, g), b);
    if (max == r) {
      max <<= 8;
    } else if (max == g) {
      max <<= 4;
    }
    return new Color(max);
  }
}
