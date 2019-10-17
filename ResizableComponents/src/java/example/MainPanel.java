// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.function.Function;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.MouseInputListener;

public class MainPanel extends JPanel {
  protected final JLayeredPane layeredPane = new JLayeredPane() {
    @Override public boolean isOptimizedDrawingEnabled() {
      return false;
    }
  };
  protected final JToolBar toolbar = new JToolBar("Resizable Components");
  protected final Point pt = new Point();

  public MainPanel() {
    super(new BorderLayout());
    JPopupMenu popup = new JPopupMenu() {
      @Override public void show(Component c, int x, int y) {
        pt.setLocation(x, y);
        super.show(c, x, y);
      }
    };
    popup.add("table").addActionListener(e -> createTable());
    popup.add("tree").addActionListener(e -> createTree());

    layeredPane.setComponentPopupMenu(popup);
    // ??? for 1.5.0
    // layeredPane.addMouseListener(new MouseAdapter() {
    //   /* Dummy listener */
    // });
    add(layeredPane);
    toolbar.add(new AbstractAction("add table") {
      @Override public void actionPerformed(ActionEvent e) {
        pt.setLocation(pt.x + 20, pt.y + 20);
        createTable();
      }
    });
    toolbar.addSeparator();
    toolbar.add(new AbstractAction("add tree") {
      @Override public void actionPerformed(ActionEvent e) {
        pt.setLocation(pt.x + 20, pt.y + 20);
        createTree();
      }
    });
    add(toolbar, BorderLayout.NORTH);
    setPreferredSize(new Dimension(320, 240));
  }

  protected final void createTree() {
    JTree tree = new JTree();
    tree.setVisibleRowCount(8);
    Component c = new JScrollPane(tree);
    Dimension r = c.getPreferredSize();
    JResizer resizer = new JResizer(new BorderLayout());
    resizer.add(c);
    resizer.setBounds(pt.x, pt.y, r.width, r.height);
    layeredPane.add(resizer);
    layeredPane.moveToFront(resizer);
  }

  protected final void createTable() {
    JTable table = new JTable(12, 3);
    table.setPreferredScrollableViewportSize(new Dimension(160, 160));
    Component c = new JScrollPane(table);
    Dimension r = c.getPreferredSize();
    JResizer resizer = new JResizer(new BorderLayout());
    resizer.add(c);
    resizer.setBounds(pt.x, pt.y, r.width, r.height);
    layeredPane.add(resizer);
    layeredPane.moveToFront(resizer);
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

class JResizer extends JPanel { // implements Serializable {
  private transient MouseInputListener resizeListener;

  protected JResizer(LayoutManager layout) {
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
// http://www.jroller.com/santhosh/entry/resizable_components
interface ResizableBorder extends Border {
  Cursor getResizeCursor(MouseEvent e);
}

class DefaultResizableBorder implements ResizableBorder, SwingConstants {
  private static final int SIZE = 6;

  private enum Locations {
    NORTH(Cursor.N_RESIZE_CURSOR, r -> new Point(r.x + r.width / 2 - SIZE / 2, r.y)),
    SOUTH(Cursor.S_RESIZE_CURSOR, r -> new Point(r.x + r.width / 2 - SIZE / 2, r.y + r.height - SIZE)),
    WEST(Cursor.W_RESIZE_CURSOR, r -> new Point(r.x, r.y + r.height / 2 - SIZE / 2)),
    EAST(Cursor.E_RESIZE_CURSOR, r -> new Point(r.x + r.width - SIZE, r.y + r.height / 2 - SIZE / 2)),
    NORTH_WEST(Cursor.NW_RESIZE_CURSOR, r -> new Point(r.x, r.y)),
    NORTH_EAST(Cursor.NE_RESIZE_CURSOR, r -> new Point(r.x + r.width - SIZE, r.y)),
    SOUTH_WEST(Cursor.SW_RESIZE_CURSOR, r -> new Point(r.x, r.y + r.height - SIZE)),
    SOUTH_EAST(Cursor.SE_RESIZE_CURSOR, r -> new Point(r.x + r.width - SIZE, r.y + r.height - SIZE));

    private final Cursor cursor;
    private final Function<Rectangle, Point> location;

    Locations(int cursor, Function<Rectangle, Point> getPoint) {
      this.cursor = Cursor.getPredefinedCursor(cursor);
      this.location = getPoint;
    }

    public Point getPoint(Rectangle r) {
      return location.apply(r);
    }

    public Cursor getCursor() {
      return cursor;
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
    for (Locations loc: Locations.values()) {
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
    if (!bounds.contains(pt)) {
      return Cursor.getDefaultCursor();
    }

    Rectangle actualBounds = new Rectangle(SIZE, SIZE, w - 2 * SIZE, h - 2 * SIZE);
    if (actualBounds.contains(pt)) {
      return Cursor.getDefaultCursor();
    }
    Rectangle rect = new Rectangle(SIZE, SIZE);
    Rectangle r = new Rectangle(0, 0, w, h);
    for (Locations loc: Locations.values()) {
      rect.setLocation(loc.getPoint(r));
      if (rect.contains(pt)) {
        return loc.getCursor();
      }
    }
    return Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR);
  }
}
