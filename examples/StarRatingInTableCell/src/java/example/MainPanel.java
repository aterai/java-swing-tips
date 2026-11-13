// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.RGBImageFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EventObject;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

public final class MainPanel extends JPanel {
  public static final int AVERAGE_COLUMN = 1;
  public static final int RATING_COLUMN = 2;

  private MainPanel() {
    super(new BorderLayout());
    Image image = loadStarImage();
    Icon defaultIcon = new ImageIcon(image);
    List<Icon> icons = new ArrayList<>(RatingBar.MAX);
    for (int i = 0; i < RatingBar.MAX; i++) {
      float f = .6f + .1f * i;
      icons.add(makeStarImageIcon(image, new SelectedImageFilter(f, f, 0f)));
    }

    JTable table = new JTable(createModel()) {
      private transient RolloverHandler handler;

      @Override public void updateUI() {
        removeMouseListener(handler);
        removeMouseMotionListener(handler);
        super.updateUI();
        setAutoCreateRowSorter(true);
        setRowHeight(26);
        handler = new RolloverHandler();
        addMouseListener(handler);
        addMouseMotionListener(handler);

        TableColumn c1 = getColumnModel().getColumn(AVERAGE_COLUMN);
        c1.setCellRenderer(new FiveStarRatingRenderer());
        c1.setPreferredWidth(160);
        TableColumn c2 = getColumnModel().getColumn(RATING_COLUMN);
        c2.setCellRenderer(new RatingRenderer(defaultIcon, icons, 1));
        c2.setCellEditor(new RatingEditor(defaultIcon, icons, 1));
        c2.setPreferredWidth(120);
      }
    };
    installKeyBindings(table);
    add(new JScrollPane(table));
    JLabel help = new JLabel(" Click: rate (click same star again: clear) / Keys 0-5: rate");
    add(help, BorderLayout.SOUTH);
    setPreferredSize(new Dimension(420, 240));
  }

  private static TableModel createModel() {
    String[] columnNames = {"Title", "Average", "Your rating"};
    Object[][] data = {
        {"Apple", 3.5, 0}, {"Banana", 4.3, 3}, {"Cherry", 5d, 5},
        {"Durian", 1.8, 1}, {"Elderberry", 2.75, 0}, {"Fig", 0.4, 2},
    };
    return new DefaultTableModel(data, columnNames) {
      @Override public Class<?> getColumnClass(int column) {
        return getValueAt(0, column).getClass();
      }

      @Override public boolean isCellEditable(int row, int column) {
        return column == RATING_COLUMN;
      }
    };
  }

  // Keyboard: 0-5 set the rating of the lead selection row directly
  private static void installKeyBindings(JTable table) {
    InputMap im = table.getInputMap(WHEN_FOCUSED);
    ActionMap am = table.getActionMap();
    for (int i = 0; i <= RatingBar.MAX; i++) {
      String key = "rate" + i;
      im.put(KeyStroke.getKeyStroke(KeyEvent.VK_0 + i, 0), key);
      im.put(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD0 + i, 0), key);
      int level = i;
      am.put(key, new AbstractAction() {
        @Override public void actionPerformed(ActionEvent e) {
          int row = table.getSelectionModel().getLeadSelectionIndex();
          int col = table.convertColumnIndexToView(RATING_COLUMN);
          if (row >= 0 && col >= 0 && !table.isEditing()) {
            table.setValueAt(level, row, col);
          }
        }
      });
    }
  }

  private static Image loadStarImage() {
    String path = "example/31g.png";
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    return Optional.ofNullable(cl.getResource(path)).map(url -> {
      Image img;
      try (InputStream s = url.openStream()) {
        img = ImageIO.read(s);
      } catch (IOException ex) {
        img = createMissingImage();
      }
      return img;
    }).orElseGet(MainPanel::createMissingImage);
  }

  private static ImageIcon makeStarImageIcon(Image image, ImageFilter filter) {
    FilteredImageSource producer = new FilteredImageSource(image.getSource(), filter);
    return new ImageIcon(Toolkit.getDefaultToolkit().createImage(producer));
  }

  private static Image createMissingImage() {
    Icon missingIcon = UIManager.getIcon("html.missingImage");
    int iw = missingIcon.getIconWidth();
    int ih = missingIcon.getIconHeight();
    BufferedImage bi = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2 = bi.createGraphics();
    missingIcon.paintIcon(null, g2, (16 - iw) / 2, (16 - ih) / 2);
    g2.dispose();
    return bi;
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

/**
 * Read-only renderer: draws a fractional 5-star value (e.g. 3.5, 4.3)
 * using the glyph outline of "★★★★★" (based on FiveStarRatingLabel).
 */
class FiveStarRatingRenderer extends JComponent implements TableCellRenderer {
  private static final String STAR = "★★★★★";
  private static final Color STAR_COLOR = new Color(0x00_AA_00);
  private double rating;

  @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    rating = value instanceof Number ? ((Number) value).doubleValue() : 0d;
    rating = Math.max(0d, Math.min(RatingBar.MAX, rating));
    setFont(new Font(Font.SERIF, Font.PLAIN, Math.max(12, table.getRowHeight() - 6)));
    setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
    setForeground(isSelected ? table.getSelectionForeground() : table.getForeground());
    setToolTipText(String.format("%.2f", rating));
    return this;
  }

  @Override protected void paintComponent(Graphics g) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setPaint(getBackground());
    g2.fillRect(0, 0, getWidth(), getHeight());
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    FontRenderContext frc = g2.getFontRenderContext();
    GlyphVector gv = getFont().createGlyphVector(frc, STAR);
    Rectangle2D r = gv.getVisualBounds();
    double tx = 4d - r.getX();
    double ty = getHeight() / 2d - r.getCenterY();
    AffineTransform at = AffineTransform.getTranslateInstance(tx, ty);

    // width to be filled: whole glyphs for the integer part,
    // and a fraction of the glyph bounds for the decimal part
    int ip = (int) rating;
    double fp = rating - ip;
    double fill = 0d;
    for (int i = 0; i < gv.getNumGlyphs(); i++) {
      if (i < ip) {
        fill += gv.getGlyphMetrics(i).getAdvance();
      } else if (i == ip) {
        fill += gv.getGlyphMetrics(i).getBounds2D().getWidth() * fp;
      }
    }
    Shape s = at.createTransformedShape(gv.getOutline());
    g2.setPaint(STAR_COLOR);
    g2.draw(s);
    Rectangle2D clip = new Rectangle2D.Double(r.getX(), r.getY(), fill, r.getHeight());
    Shape oldClip = g2.getClip();
    g2.clip(at.createTransformedShape(clip));
    g2.fill(s);
    g2.setClip(oldClip);

    // numeric value on the right side of the stars
    g2.setPaint(getForeground());
    g2.setFont(getFont().deriveFont(getFont().getSize2D() * .7f));
    FontMetrics fm = g2.getFontMetrics();
    float x = (float) (tx + r.getMaxX() + 6d);
    float y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2f;
    g2.drawString(String.format("%.1f", rating), x, y);
    g2.dispose();
  }
}

/**
 * Single painted component that shows 5 star icons (based on RatingLabel#LevelBar).
 * It has no child JLabels, so that JTable can repost mouse events to it directly
 * when it is used as a cell editor.
 */
class RatingBar extends JComponent {
  public static final int MAX = 5;
  private final Icon defaultIcon;
  private final List<Icon> iconList;
  private final int gap;
  private int level;
  private int rolloverLevel = -1;

  protected RatingBar(Icon defaultIcon, List<Icon> iconList, int gap) {
    super();
    this.defaultIcon = defaultIcon;
    this.iconList = Collections.unmodifiableList(new ArrayList<>(iconList));
    this.gap = gap;
    setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
  }

  public int getLevel() {
    return level;
  }

  public void setLevel(int level) {
    this.level = Math.max(0, Math.min(MAX, level));
    repaint();
  }

  public void setRolloverLevel(int rolloverLevel) {
    this.rolloverLevel = rolloverLevel;
    repaint();
  }

  @Override public Dimension getPreferredSize() {
    Insets i = getInsets();
    int w = defaultIcon.getIconWidth() * MAX + gap * 2 * (MAX - 1);
    return new Dimension(w + i.left + i.right, defaultIcon.getIconHeight() + i.top + i.bottom);
  }

  private int iconX(int index) {
    return getInsets().left + index * (defaultIcon.getIconWidth() + gap * 2);
  }

  /**
   * Returns the level (1-5) under the specified point, or -1.
   */
  public int getLevelAt(Point p) {
    int iw = defaultIcon.getIconWidth();
    for (int i = 0; i < MAX; i++) {
      int x = iconX(i);
      if (x - gap <= p.x && p.x < x + iw + gap) {
        return i + 1;
      }
    }
    return -1;
  }

  @Override protected void paintComponent(Graphics g) {
    if (isOpaque()) {
      g.setColor(getBackground());
      g.fillRect(0, 0, getWidth(), getHeight());
    }
    int current = rolloverLevel >= 0 ? rolloverLevel : level;
    int y = (getHeight() - defaultIcon.getIconHeight()) / 2;
    for (int i = 0; i < MAX; i++) {
      Icon icon = i < current ? iconList.get(i) : defaultIcon;
      icon.paintIcon(this, g, iconX(i), y);
    }
  }
}

/**
 * Tracks the mouse location over the JTable and stores it as a client property,
 * so that RatingRenderer can paint the hover preview without starting edit.
 */
class RolloverHandler extends MouseAdapter {
  public static final String KEY = "RatingRolloverPoint";
  private int prevRow = -1;
  private int prevCol = -1;

  @Override public void mouseMoved(MouseEvent e) {
    JTable table = (JTable) e.getComponent();
    Point pt = e.getPoint();
    int row = table.rowAtPoint(pt);
    int col = table.columnAtPoint(pt);
    boolean isRating = col >= 0 && table.convertColumnIndexToModel(col) == MainPanel.RATING_COLUMN;
    table.putClientProperty(KEY, isRating ? pt : null);
    table.setCursor(isRating ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : null);
    repaintCell(table, prevRow, prevCol);
    if (isRating) {
      repaintCell(table, row, col);
      prevRow = row;
      prevCol = col;
    } else {
      prevRow = -1;
      prevCol = -1;
    }
  }

  @Override public void mouseDragged(MouseEvent e) {
    mouseMoved(e);
  }

  @Override public void mouseExited(MouseEvent e) {
    JTable table = (JTable) e.getComponent();
    table.putClientProperty(KEY, null);
    table.setCursor(null);
    repaintCell(table, prevRow, prevCol);
    prevRow = -1;
    prevCol = -1;
  }

  private static void repaintCell(JTable table, int row, int col) {
    if (row >= 0 && col >= 0) {
      table.repaint(table.getCellRect(row, col, false));
    }
  }
}

class RatingRenderer implements TableCellRenderer {
  private final RatingBar renderer;

  protected RatingRenderer(Icon defaultIcon, List<Icon> iconList, int gap) {
    renderer = new RatingBar(defaultIcon, iconList, gap);
    renderer.setOpaque(true);
  }

  @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    renderer.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
    renderer.setLevel(value instanceof Integer ? (Integer) value : 0);
    int rollover = -1;
    Object o = table.getClientProperty(RolloverHandler.KEY);
    if (o instanceof Point) {
      Point pt = (Point) o;
      if (table.rowAtPoint(pt) == row && table.columnAtPoint(pt) == column) {
        Rectangle r = table.getCellRect(row, column, false);
        pt = new Point(pt.x - r.x, pt.y - r.y);
        rollover = renderer.getLevelAt(pt);
      }
    }
    renderer.setRolloverLevel(rollover);
    return renderer;
  }
}

/**
 * One-click cell editor: JTable reposts the mousePressed/mouseReleased events
 * to the editor component, and the level is committed on mouseReleased.
 * Clicking the current level again clears the rating (level 0).
 */
class RatingEditor extends AbstractCellEditor implements TableCellEditor {
  private final RatingBar editor;
  private int prev;

  protected RatingEditor(Icon defaultIcon, List<Icon> iconList, int gap) {
    super();
    editor = new RatingBar(defaultIcon, iconList, gap);
    editor.setOpaque(true);
    editor.addMouseListener(new MouseAdapter() {
      @Override public void mouseReleased(MouseEvent e) {
        int lv = editor.getLevelAt(e.getPoint());
        boolean inside = editor.contains(e.getPoint());
        if (SwingUtilities.isLeftMouseButton(e) && inside && lv > 0) {
          editor.setLevel(lv == prev ? 0 : lv);
          stopCellEditing();
        } else {
          cancelCellEditing();
        }
      }
    });
  }

  @Override public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
    prev = value instanceof Integer ? (Integer) value : 0;
    editor.setLevel(prev);
    editor.setRolloverLevel(-1);
    editor.setBackground(table.getSelectionBackground());
    return editor;
  }

  @Override public Object getCellEditorValue() {
    return editor.getLevel();
  }

  // start editing only by left mouse click (F2 and typing keys are ignored)
  @Override public boolean isCellEditable(EventObject e) {
    return e instanceof MouseEvent && SwingUtilities.isLeftMouseButton((MouseEvent) e)
        && ((MouseEvent) e).getID() == MouseEvent.MOUSE_PRESSED;
  }
}

class SelectedImageFilter extends RGBImageFilter {
  private final float rf;
  private final float gf;
  private final float bf;

  protected SelectedImageFilter(float rf, float gf, float bf) {
    super();
    this.rf = Math.min(1f, rf);
    this.gf = Math.min(1f, gf);
    this.bf = Math.min(1f, bf);
    canFilterIndexColorModel = false;
  }

  @Override public int filterRGB(int x, int y, int argb) {
    int a = argb & 0xFF_00_00_00;
    int r = Math.round(((argb >> 16) & 0xFF) * rf);
    int g = Math.round(((argb >> 8) & 0xFF) * gf);
    int b = Math.round((argb & 0xFF) * bf);
    return a | r << 16 | g << 8 | b;
  }
}
