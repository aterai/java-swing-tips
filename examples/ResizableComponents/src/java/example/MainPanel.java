// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import javax.swing.*;
import javax.swing.border.Border;
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
    // ??? for 1.5.0
    // layeredPane.addMouseListener(new MouseAdapter() {
    //   /* do nothing listener */
    // });
    add(layeredPane);

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
    add(toolBar, BorderLayout.NORTH);
    setPreferredSize(new Dimension(320, 240));
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

    public abstract Point getPoint(Rectangle r);

    public Cursor getCursor() {
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
    // for (Locations loc : Locations.values()) {
    //   rect.setLocation(loc.getPoint(r));
    //   if (rect.contains(pt)) {
    //     return loc.getCursor();
    //   }
    // }
    // return Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR);
  }
}
