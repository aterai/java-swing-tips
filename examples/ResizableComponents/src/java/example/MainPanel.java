// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Optional;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.MouseInputAdapter;
import javax.swing.event.MouseInputListener;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    Point pt = new Point();
    JPopupMenu popup = new JPopupMenu() {
      @Override public void show(Component c, int x, int y) {
        pt.setLocation(x, y);
        super.show(c, x, y);
      }
    };
    popup.add("table").addActionListener(e -> createTable());
    popup.add("tree").addActionListener(e -> createTree());

    JLayeredPane layeredPane = new JLayeredPane() {
      @Override public boolean isOptimizedDrawingEnabled() {
        return false;
      }
    };
    layeredPane.setComponentPopupMenu(popup);
    // Java 1.5.0:
    // layeredPane.addMouseListener(new MouseAdapter() {
    //   /* do nothing listener */
    // });
    add(layeredPane);

    add(createToolBar(pt, layeredPane), BorderLayout.NORTH);
    setPreferredSize(new Dimension(320, 240));
  }

  private static JToolBar createToolBar(Point pt, JLayeredPane layeredPane) {
    JButton addTable = new JButton("add table");
    addTable.addActionListener(e -> {
      pt.setLocation(pt.x + 20, pt.y + 20);
      Component c = createTable();
      Dimension d = c.getPreferredSize();
      c.setBounds(pt.x, pt.y, d.width, d.height);
      layeredPane.add(c);
      layeredPane.moveToFront(c);
    });

    JButton addTree = new JButton("add tree");
    addTree.addActionListener(e -> {
      pt.setLocation(pt.x + 20, pt.y + 20);
      Component c = createTree();
      Dimension d = c.getPreferredSize();
      c.setBounds(pt.x, pt.y, d.width, d.height);
      layeredPane.add(c);
      layeredPane.moveToFront(c);
    });

    JToolBar toolBar = new JToolBar("Resizable Components");
    toolBar.add(addTable);
    toolBar.addSeparator();
    toolBar.add(addTree);
    return toolBar;
  }

  private static Component createTree() {
    JTree tree = new JTree();
    tree.setVisibleRowCount(8);
    Container c = new ResizablePanel(new BorderLayout());
    c.add(new JScrollPane(tree));
    return c;
  }

  private static Component createTable() {
    JTable table = new JTable(12, 3);
    table.setPreferredScrollableViewportSize(new Dimension(160, 160));
    Container c = new ResizablePanel(new BorderLayout());
    c.add(new JScrollPane(table));
    return c;
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

class ResizablePanel extends JPanel { // implements Serializable {
  private transient MouseInputListener resizeListener;

  protected ResizablePanel(LayoutManager layout) {
    super(layout);
  }

  @Override public void updateUI() {
    removeMouseListener(resizeListener);
    removeMouseMotionListener(resizeListener);
    super.updateUI();
    resizeListener = new ResizeMouseListener();
    addMouseListener(resizeListener);
    addMouseMotionListener(resizeListener);
    setBorder(new DefaultResizableBorder());
  }

  // private void writeObject(ObjectOutputStream out) throws IOException {}

  // private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {}

  // private void readObjectNoData() throws ObjectStreamException {}

  // private void readObject() {
  //   this.resizeListener = new ResizeMouseListener();
  // }

  // private Object readResolve() {
  //   this.resizeListener = new ResizeMouseListener();
  //   return this;
  // }

  @Override public void setBorder(Border border) {
    removeMouseListener(resizeListener);
    removeMouseMotionListener(resizeListener);
    if (border instanceof ResizableBorder) {
      addMouseListener(resizeListener);
      addMouseMotionListener(resizeListener);
    }
    super.setBorder(border);
  }
}

// Resizable Components - Santhosh Kumar's Weblog
// https://github.com/santhosh-tekuri/MyBlog/tree/master/ResizableBorder
interface ResizableBorder extends Border {
  Cursor getResizeCursor(MouseEvent e);
}

class DefaultResizableBorder implements ResizableBorder, SwingConstants {
  private static final int SIZE = 6;

  private enum Locations {
    NORTH(Cursor.N_RESIZE_CURSOR) {
      @Override public Point getPoint(Rectangle r) {
        return new Point(r.x + r.width / 2 - SIZE / 2, r.y);
      }
    },
    SOUTH(Cursor.S_RESIZE_CURSOR) {
      @Override public Point getPoint(Rectangle r) {
        return new Point(r.x + r.width / 2 - SIZE / 2, r.y + r.height - SIZE);
      }
    },
    WEST(Cursor.W_RESIZE_CURSOR) {
      @Override public Point getPoint(Rectangle r) {
        return new Point(r.x, r.y + r.height / 2 - SIZE / 2);
      }
    },
    EAST(Cursor.E_RESIZE_CURSOR) {
      @Override public Point getPoint(Rectangle r) {
        return new Point(r.x + r.width - SIZE, r.y + r.height / 2 - SIZE / 2);
      }
    },
    NORTH_WEST(Cursor.NW_RESIZE_CURSOR) {
      @Override public Point getPoint(Rectangle r) {
        return new Point(r.x, r.y);
      }
    },
    NORTH_EAST(Cursor.NE_RESIZE_CURSOR) {
      @Override public Point getPoint(Rectangle r) {
        return new Point(r.x + r.width - SIZE, r.y);
      }
    },
    SOUTH_WEST(Cursor.SW_RESIZE_CURSOR) {
      @Override public Point getPoint(Rectangle r) {
        return new Point(r.x, r.y + r.height - SIZE);
      }
    },
    SOUTH_EAST(Cursor.SE_RESIZE_CURSOR) {
      @Override public Point getPoint(Rectangle r) {
        return new Point(r.x + r.width - SIZE, r.y + r.height - SIZE);
      }
    };

    private final int cursor;

    Locations(int cursor) {
      this.cursor = cursor;
    }

    /* default */ abstract Point getPoint(Rectangle r);

    private Cursor getCursor() {
      return Cursor.getPredefinedCursor(cursor);
    }
  }

  @Override public Insets getBorderInsets(Component component) {
    return new Insets(SIZE, SIZE, SIZE, SIZE);
  }

  @Override public boolean isBorderOpaque() {
    return false;
  }

  @Override public void paintBorder(Component component, Graphics g, int x, int y, int w, int h) {
    g.setColor(Color.black);
    g.drawRect(x + SIZE / 2, y + SIZE / 2, w - SIZE, h - SIZE);
    Rectangle rect = new Rectangle(SIZE, SIZE);
    Rectangle r = new Rectangle(x, y, w, h);
    for (Locations loc : Locations.values()) {
      rect.setLocation(loc.getPoint(r));
      g.setColor(Color.WHITE);
      g.fillRect(rect.x, rect.y, rect.width - 1, rect.height - 1);
      g.setColor(Color.BLACK);
      g.drawRect(rect.x, rect.y, rect.width - 1, rect.height - 1);
    }
  }

  @Override public Cursor getResizeCursor(MouseEvent e) {
    Component c = e.getComponent();
    int w = c.getWidth();
    int h = c.getHeight();
    Point pt = e.getPoint();
    Rectangle bounds = new Rectangle(w, h);
    Rectangle actualBounds = new Rectangle(SIZE, SIZE, w - 2 * SIZE, h - 2 * SIZE);
    Cursor cursor = Cursor.getDefaultCursor();
    if (bounds.contains(pt) && !actualBounds.contains(pt)) {
      Rectangle controlPoint = new Rectangle(SIZE, SIZE);
      cursor = Arrays.stream(Locations.values())
          .filter(loc -> {
            controlPoint.setLocation(loc.getPoint(bounds));
            return controlPoint.contains(pt);
          })
          .findFirst()
          .map(Locations::getCursor)
          .orElse(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
    }
    return cursor;
  }
}

class ResizeMouseListener extends MouseInputAdapter {
  private final Point startPos = new Point();
  private final Rectangle startRect = new Rectangle();
  private Cursor cursor;

  @Override public void mouseMoved(MouseEvent e) {
    JComponent c = (JComponent) e.getComponent();
    ResizableBorder border = (ResizableBorder) c.getBorder();
    c.setCursor(border.getResizeCursor(e));
  }

  @Override public void mouseExited(MouseEvent e) {
    e.getComponent().setCursor(Cursor.getDefaultCursor());
  }

  @Override public void mousePressed(MouseEvent e) {
    JComponent c = (JComponent) e.getComponent();
    ResizableBorder border = (ResizableBorder) c.getBorder();
    cursor = border.getResizeCursor(e);
    startPos.setLocation(SwingUtilities.convertPoint(c, e.getX(), e.getY(), null));
    startRect.setBounds(c.getBounds());
    Container parent = SwingUtilities.getAncestorOfClass(JLayeredPane.class, c);
    if (parent instanceof JLayeredPane) {
      ((JLayeredPane) parent).moveToFront(c);
    }
  }

  @Override public void mouseReleased(MouseEvent e) {
    startRect.setSize(0, 0);
  }

  // @see %JAVA_HOME%/src/javax/swing/plaf/basic/BasicInternalFrameUI.java
  @Override public void mouseDragged(MouseEvent e) {
    if (!startRect.isEmpty()) {
      Component c = e.getComponent();
      Point p = SwingUtilities.convertPoint(c, e.getX(), e.getY(), null);
      int deltaX = startPos.x - p.x;
      int deltaY = startPos.y - p.y;
      Container parent = SwingUtilities.getUnwrappedParent(c);
      int cursorType = Optional.ofNullable(cursor)
          .map(Cursor::getType)
          .orElse(Cursor.DEFAULT_CURSOR);
      Directions.getByCursorType(cursorType).ifPresent(dir -> {
        Point delta = dir.getLimitedDelta(startRect, parent.getBounds(), deltaX, deltaY);
        c.setBounds(dir.getBounds(startRect, delta));
      });
      parent.revalidate();
    }
  }
}

enum Directions {
  NORTH(Cursor.N_RESIZE_CURSOR) {
    @Override public Rectangle getBounds(Rectangle r, Point d) {
      return new Rectangle(r.x, r.y - d.y, r.width, r.height + d.y);
    }

    @Override public Point getLimitedDelta(Rectangle sr, Rectangle pr, int dx, int dy) {
      return new Point(0, DeltaUtils.limitLeft(dy, sr.y, sr.height));
    }
  },
  SOUTH(Cursor.S_RESIZE_CURSOR) {
    @Override public Rectangle getBounds(Rectangle r, Point d) {
      return new Rectangle(r.x, r.y, r.width, r.height - d.y);
    }

    @Override public Point getLimitedDelta(Rectangle sr, Rectangle pr, int dx, int dy) {
      return new Point(0, DeltaUtils.limitRight(dy, sr.y, sr.height, pr.height));
    }
  },
  WEST(Cursor.W_RESIZE_CURSOR) {
    @Override public Rectangle getBounds(Rectangle r, Point d) {
      return new Rectangle(r.x - d.x, r.y, r.width + d.x, r.height);
    }

    @Override public Point getLimitedDelta(Rectangle sr, Rectangle pr, int dx, int dy) {
      return new Point(DeltaUtils.limitLeft(dx, sr.x, sr.width), 0);
    }
  },
  EAST(Cursor.E_RESIZE_CURSOR) {
    @Override public Rectangle getBounds(Rectangle r, Point d) {
      return new Rectangle(r.x, r.y, r.width - d.x, r.height);
    }

    @Override public Point getLimitedDelta(Rectangle sr, Rectangle pr, int dx, int dy) {
      return new Point(DeltaUtils.limitRight(dx, sr.x, sr.width, pr.width), 0);
    }
  },
  NORTH_WEST(Cursor.NW_RESIZE_CURSOR) {
    @Override public Rectangle getBounds(Rectangle r, Point d) {
      return new Rectangle(r.x - d.x, r.y - d.y, r.width + d.x, r.height + d.y);
    }

    @Override public Point getLimitedDelta(Rectangle sr, Rectangle pr, int dx, int dy) {
      return new Point(DeltaUtils.limitLeft(dx, sr.x, sr.width),
          DeltaUtils.limitLeft(dy, sr.y, sr.height));
    }
  },
  NORTH_EAST(Cursor.NE_RESIZE_CURSOR) {
    @Override public Rectangle getBounds(Rectangle r, Point d) {
      return new Rectangle(r.x, r.y - d.y, r.width - d.x, r.height + d.y);
    }

    @Override public Point getLimitedDelta(Rectangle sr, Rectangle pr, int dx, int dy) {
      return new Point(DeltaUtils.limitRight(dx, sr.x, sr.width, pr.width),
          DeltaUtils.limitLeft(dy, sr.y, sr.height));
    }
  },
  SOUTH_WEST(Cursor.SW_RESIZE_CURSOR) {
    @Override public Rectangle getBounds(Rectangle r, Point d) {
      return new Rectangle(r.x - d.x, r.y, r.width + d.x, r.height - d.y);
    }

    @Override public Point getLimitedDelta(Rectangle sr, Rectangle pr, int dx, int dy) {
      return new Point(DeltaUtils.limitLeft(dx, sr.x, sr.width),
          DeltaUtils.limitRight(dy, sr.y, sr.height, pr.height));
    }
  },
  SOUTH_EAST(Cursor.SE_RESIZE_CURSOR) {
    @Override public Rectangle getBounds(Rectangle r, Point d) {
      return new Rectangle(r.x, r.y, r.width - d.x, r.height - d.y);
    }

    @Override public Point getLimitedDelta(Rectangle sr, Rectangle pr, int dx, int dy) {
      return new Point(DeltaUtils.limitRight(dx, sr.x, sr.width, pr.width),
          DeltaUtils.limitRight(dy, sr.y, sr.height, pr.height));
    }
  },
  MOVE(Cursor.MOVE_CURSOR) {
    @Override public Rectangle getBounds(Rectangle r, Point d) {
      return new Rectangle(r.x - d.x, r.y - d.y, r.width, r.height);
    }

    @Override public Point getLimitedDelta(Rectangle sr, Rectangle pr, int dx, int dy) {
      // MOVE has no restrictions
      // parent boundary can be considered separately on the mouseDragged side
      return new Point(dx, dy);
    }
  };

  private final int cursor;

  Directions(int cursor) {
    this.cursor = cursor;
  }

  public abstract Rectangle getBounds(Rectangle rect, Point delta);

  public abstract Point getLimitedDelta(
      Rectangle startRect, Rectangle parentRect, int deltaX, int deltaY);

  public static Optional<Directions> getByCursorType(int cursor) {
    return EnumSet.allOf(Directions.class).stream()
        .filter(d -> d.cursor == cursor).findFirst();
  }
}

final class DeltaUtils {
  private static final Dimension MIN = new Dimension(50, 50);
  private static final Dimension MAX = new Dimension(500, 500);

  private DeltaUtils() {
    // utils
  }

  // Direction to shorten the left side (N/W)
  /* default */ static int limitLeft(int dx, int startVal, int startSize) {
    int top = Math.min(MAX.width - startSize, startVal);
    return Math.max(Math.min(dx, top), MIN.width - startSize);
    // Java 21: return Math.clamp(dx, MIN.width - startSize, top);
  }

  // Direction to shorten the right side (S/E)
  /* default */ static int limitRight(int dx, int startVal, int startSize, int parentSize) {
    int right = Math.max(startSize - MAX.width, startVal + startSize - parentSize);
    return Math.min(Math.max(dx, right), startSize - MIN.width);
    // Java 21: return Math.clamp(dx, right, startSize - MIN.width);
  }
}
