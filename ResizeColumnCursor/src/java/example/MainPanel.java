// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import com.sun.java.swing.plaf.windows.WindowsTableHeaderUI;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Optional;
import javax.swing.*;
import javax.swing.event.MouseInputListener;
import javax.swing.plaf.TableHeaderUI;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;
import javax.swing.plaf.basic.BasicTableHeaderUI;
import javax.swing.plaf.synth.SynthTableHeaderUI;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new GridLayout(0, 1));
    JSplitPane sp = makeSplitPane(false);
    BasicSplitPaneDivider divider = ((BasicSplitPaneUI) sp.getUI()).getDivider();
    divider.setCursor(Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR)); // Ubuntu

    add(makeSplitPane(false));
    add(sp);
    add(makeSplitPane(true));
    setPreferredSize(new Dimension(320, 240));
  }

  private static JSplitPane makeSplitPane(boolean flag) {
    JSplitPane sp;
    if (flag) {
      sp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT) {
        @Override public void updateUI() {
          super.updateUI();
          EventQueue.invokeLater(() -> {
            Component c = ((BasicSplitPaneUI) getUI()).getDivider();
            c.setCursor(ResizeUtils.createCursor("⇹", 32, 32, c));
          });
        }
      };
    } else {
      sp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    }
    sp.setContinuousLayout(true);
    sp.setTopComponent(new JScrollPane(new JTree()));
    sp.setBottomComponent(new JScrollPane(flag ? makeTable() : new JTable(2, 3)));
    EventQueue.invokeLater(() -> sp.setDividerLocation(.3));
    return sp;
  }

  private static JTable makeTable() {
    return new JTable(2, 3) {
      @Override protected JTableHeader createDefaultTableHeader() {
        return new JTableHeader(columnModel) {
          @Override public void updateUI() {
            super.updateUI();
            TableHeaderUI ui = getUI();
            if (ui instanceof WindowsTableHeaderUI) {
              setUI(new MyWindowsTableHeaderUI());
            } else if (ui instanceof SynthTableHeaderUI) {
              setUI(new MySynthTableHeaderUI());
            } else {
              setUI(new MyBasicTableHeaderUI());
            }
          }
        };
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

final class ResizeUtils {
  private ResizeUtils() {
    /* HideUtilityClassConstructor */
  }

  public static Cursor createCursor(String s, int width, int height, Component c) {
    float size = height - 2f;
    BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2 = bi.createGraphics();
    g2.setFont(c.getFont().deriveFont(size));
    Shape shape = new TextLayout(s, g2.getFont(), g2.getFontRenderContext()).getOutline(null);
    Icon icon = new ShapeIcon(shape, width, height);
    icon.paintIcon(c, g2, 0, 0);
    g2.dispose();
    Point hotSpot = new Point(width / 2, height / 2);
    return c.getToolkit().createCustomCursor(bi, hotSpot, s);
  }

  public static boolean canResize(TableColumn column, JTableHeader header) {
    return column != null && header.getResizingAllowed() && column.getResizable();
  }

  public static TableColumn getResizeColumn(JTableHeader header, Point p) {
    return Optional.ofNullable(header)
        .map(h -> {
          int column = h.columnAtPoint(p);
          return column == -1 ? null : getRect(h, p, column);
        })
        .map(r -> {
          int column = header.columnAtPoint(p);
          int midPoint = r.x + r.width / 2;
          int colIdx;
          if (header.getComponentOrientation().isLeftToRight()) {
            colIdx = p.x < midPoint ? column - 1 : column;
          } else {
            colIdx = p.x < midPoint ? column : column - 1;
          }
          return colIdx == -1 ? null : header.getColumnModel().getColumn(colIdx);
        })
        .orElse(null);
  }

  private static Rectangle getRect(JTableHeader header, Point p, int column) {
    Rectangle r = header.getHeaderRect(column);
    r.grow(-3, 0);
    return r.contains(p) ? null : r;
  }
}

class MyWindowsTableHeaderUI extends WindowsTableHeaderUI {
  @Override protected MouseInputListener createMouseInputListener() {
    return new MouseInputHandler() {
      private final Cursor resizeCursor = ResizeUtils.createCursor("⇼", 32, 32, header);
      private final Cursor defaultCursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);

      @Override public void mouseMoved(MouseEvent e) {
        super.mouseMoved(e);
        if (!header.isEnabled()) {
          return;
        }
        if (ResizeUtils.canResize(ResizeUtils.getResizeColumn(header, e.getPoint()), header)) {
          header.setCursor(resizeCursor);
        } else {
          header.setCursor(defaultCursor);
        }
      }
    };
  }
}

class MySynthTableHeaderUI extends SynthTableHeaderUI {
  @Override protected MouseInputListener createMouseInputListener() {
    return new MouseInputHandler() {
      private final Cursor resizeCursor = ResizeUtils.createCursor("⇼", 32, 32, header);
      private final Cursor defaultCursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);

      @Override public void mouseMoved(MouseEvent e) {
        super.mouseMoved(e);
        if (!header.isEnabled()) {
          return;
        }
        if (ResizeUtils.canResize(ResizeUtils.getResizeColumn(header, e.getPoint()), header)) {
          header.setCursor(resizeCursor);
        } else {
          header.setCursor(defaultCursor);
        }
      }
    };
  }
}

class MyBasicTableHeaderUI extends BasicTableHeaderUI {
  @Override protected MouseInputListener createMouseInputListener() {
    return new MouseInputHandler() {
      private final Cursor resizeCursor = ResizeUtils.createCursor("⇼", 32, 32, header);
      private final Cursor defaultCursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);

      @Override public void mouseMoved(MouseEvent e) {
        super.mouseMoved(e);
        if (!header.isEnabled()) {
          return;
        }
        if (ResizeUtils.canResize(ResizeUtils.getResizeColumn(header, e.getPoint()), header)) {
          header.setCursor(resizeCursor);
        } else {
          header.setCursor(defaultCursor);
        }
      }
    };
  }
}

class ShapeIcon implements Icon {
  private final Shape shape;
  private final int width;
  private final int height;

  protected ShapeIcon(Shape shape, int width, int height) {
    this.shape = shape;
    this.width = width;
    this.height = height;
  }

  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();
    // g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.translate(x, y);
    Rectangle2D b = shape.getBounds2D();
    double tx = width / 2d - b.getCenterX();
    double ty = height / 2d - b.getCenterY();
    AffineTransform toCenterAt = AffineTransform.getTranslateInstance(tx, ty);
    g2.setPaint(c.getForeground());
    g2.fill(toCenterAt.createTransformedShape(shape));
    g2.dispose();
  }

  @Override public int getIconWidth() {
    return width;
  }

  @Override public int getIconHeight() {
    return height;
  }
}
