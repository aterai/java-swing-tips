package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

public final class MainPanel extends JPanel {
    private final JLayeredPane layeredPane = new JLayeredPane();
    private final JToolBar toolbar = new JToolBar("Resizable Components");
    private final Point pt = new Point();

    public MainPanel() {
        super(new BorderLayout());
        JPopupMenu popup = new JPopupMenu() {
            @Override public void show(Component c, int x, int y) {
                pt.setLocation(x, y);
                super.show(c, x, y);
            }
        };
        popup.add(new AbstractAction("table") {
            @Override public void actionPerformed(ActionEvent e) {
                createTable();
            }
        });
        popup.add(new AbstractAction("tree") {
            @Override public void actionPerformed(ActionEvent e) {
                createTree();
            }
        });
        layeredPane.setComponentPopupMenu(popup);
        //layeredPane.addMouseListener(new MouseAdapter() { /* Dummy listener */ }); //??? for 1.5.0
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
    private void createTree() {
        JTree tree = new JTree();
        tree.setVisibleRowCount(8);
        Component c = new JScrollPane(tree);
        Dimension r = c.getPreferredSize();
        JResizer resizer = new JResizer(c);
        resizer.setBounds(pt.x, pt.y, r.width, r.height);
        layeredPane.add(resizer);
        layeredPane.moveToFront(resizer);
    }
    private void createTable() {
        JTable table = new JTable(12, 3);
        table.setPreferredScrollableViewportSize(new Dimension(160, 160));
        Component c = new JScrollPane(table);
        Dimension r = c.getPreferredSize();
        JResizer resizer = new JResizer(c);
        resizer.setBounds(pt.x, pt.y, r.width, r.height);
        layeredPane.add(resizer);
        layeredPane.moveToFront(resizer);
    }
    public static void main(String... args) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                createAndShowGUI();
            }
        });
    }
    public static void createAndShowGUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException |
                 IllegalAccessException | UnsupportedLookAndFeelException ex) {
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

class JResizer extends JPanel { // implements Serializable {
    private transient MouseAdapter resizeListener;

    public JResizer(Component comp) {
        this(comp, new DefaultResizableBorder(6));
    }
    public JResizer(Component comp, ResizableBorder border) {
        super(new BorderLayout());
        resizeListener = new ResizeMouseListener();
        setBorder(border);
        add(comp);
    }
//     private void writeObject(java.io.ObjectOutputStream out) throws IOException {}
//     private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {}
//     private void readObjectNoData() throws ObjectStreamException {}
//     private void readObject() {
//         this.resizeListener = new ResizeMouseListener();
//     }
//     private Object readResolve() {
//         this.resizeListener = new ResizeMouseListener();
//         return this;
//     }
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

class ResizeMouseListener extends MouseAdapter {
    private static final Dimension MIN = new Dimension(50, 50);
    private static final Dimension MAX = new Dimension(500, 500);
    private int cursor;
    private Point startPos;
    private Rectangle startingBounds;
    @Override public void mouseMoved(MouseEvent e) {
        JComponent c = (JComponent) e.getComponent();
        ResizableBorder border = (ResizableBorder) c.getBorder();
        c.setCursor(Cursor.getPredefinedCursor(border.getResizeCursor(e)));
    }
    @Override public void mouseExited(MouseEvent e) {
        Component c = e.getComponent();
        c.setCursor(Cursor.getDefaultCursor());
    }
    @Override public void mousePressed(MouseEvent e) {
        JComponent c = (JComponent) e.getComponent();
        ResizableBorder border = (ResizableBorder) c.getBorder();
        cursor = border.getResizeCursor(e);
        startPos = SwingUtilities.convertPoint(c, e.getX(), e.getY(), null);
        startingBounds = c.getBounds();
        Container parent = SwingUtilities.getAncestorOfClass(JLayeredPane.class, c);
        if (parent instanceof JLayeredPane) {
            ((JLayeredPane) parent).moveToFront(c);
        }
    }
    private int getDeltaX(int dx) {
        int deltaX = dx;
        if (startingBounds.width + deltaX < MIN.width) {
            deltaX = -(startingBounds.width - MIN.width);
        } else if (startingBounds.width + deltaX > MAX.width) {
            deltaX = MAX.width - startingBounds.width;
        }
        if (startingBounds.x - deltaX < 0) {
            deltaX = startingBounds.x;
        }
        return deltaX;
    }
    private int getDeltaX(int dx, Rectangle parentBounds) {
        int deltaX = dx;
        if (startingBounds.width - deltaX < MIN.width) {
            deltaX = startingBounds.width - MIN.width;
        } else if (startingBounds.width - deltaX > MAX.width) {
            deltaX = -(MAX.width - startingBounds.width);
        }
        if (startingBounds.x + startingBounds.width - deltaX > parentBounds.width) {
            deltaX = startingBounds.x + startingBounds.width - parentBounds.width;
        }
        return deltaX;
    }
    private int getDeltaY(int dy) {
        int deltaY = dy;
        if (startingBounds.height + deltaY < MIN.height) {
            deltaY = -(startingBounds.height - MIN.height);
        } else if (startingBounds.height + deltaY > MAX.height) {
            deltaY = MAX.height - startingBounds.height;
        }
        if (startingBounds.y - deltaY < 0) {
            deltaY = startingBounds.y;
        }
        return deltaY;
    }
    private int getDeltaY(int dy, Rectangle parentBounds) {
        int deltaY = dy;
        if (startingBounds.height - deltaY < MIN.height) {
            deltaY = startingBounds.height - MIN.height;
        } else if (startingBounds.height - deltaY > MAX.height) {
            deltaY = -(MAX.height - startingBounds.height);
        }
        if (startingBounds.y + startingBounds.height - deltaY > parentBounds.height) {
            deltaY = startingBounds.y + startingBounds.height - parentBounds.height;
        }
        return deltaY;
    }
    //@see %JAVA_HOME%/src/javax/swing/plaf/basic/BasicInternalFrameUI.java
    @Override public void mouseDragged(MouseEvent e) {
        if (startPos == null || startingBounds == null) {
            return;
        }
        Component c = e.getComponent();
        Point p = SwingUtilities.convertPoint(c, e.getX(), e.getY(), null);
        int deltaX = startPos.x - p.x;
        int deltaY = startPos.y - p.y;

        Container parent = SwingUtilities.getUnwrappedParent(c);
        Rectangle parentBounds = parent.getBounds();

        switch(cursor) {
          case Cursor.NW_RESIZE_CURSOR: {
              int dx = getDeltaX(deltaX);
              int dy = getDeltaY(deltaY);
              c.setBounds(
                  startingBounds.x - dx,
                  startingBounds.y - dy,
                  startingBounds.width  + dx,
                  startingBounds.height + dy);
              break;
          }
          case Cursor.N_RESIZE_CURSOR: {
              int dy = getDeltaY(deltaY);
              c.setBounds(
                  startingBounds.x,
                  startingBounds.y - dy,
                  startingBounds.width,
                  startingBounds.height + dy);
              break;
          }
          case Cursor.NE_RESIZE_CURSOR: {
              int dx = getDeltaX(deltaX, parentBounds);
              int dy = getDeltaY(deltaY);
              c.setBounds(
                  startingBounds.x,
                  startingBounds.y - dy,
                  startingBounds.width  - dx,
                  startingBounds.height + dy);
              break;
          }
          case Cursor.E_RESIZE_CURSOR: {
              int dx = getDeltaX(deltaX, parentBounds);
              c.setSize(
                  startingBounds.width - dx,
                  startingBounds.height);
              break;
          }
          case Cursor.SE_RESIZE_CURSOR: {
              int dx = getDeltaX(deltaX, parentBounds);
              int dy = getDeltaY(deltaY, parentBounds);
              c.setSize(
                  startingBounds.width  - dx,
                  startingBounds.height - dy);
              break;
          }
          case Cursor.S_RESIZE_CURSOR: {
              int dy = getDeltaY(deltaY, parentBounds);
              c.setSize(
                  startingBounds.width,
                  startingBounds.height - dy);
              break;
          }
          case Cursor.SW_RESIZE_CURSOR: {
              int dx = getDeltaX(deltaX);
              int dy = getDeltaY(deltaY, parentBounds);
              c.setBounds(
                  startingBounds.x - dx,
                  startingBounds.y,
                  startingBounds.width  + dx,
                  startingBounds.height - dy);
              break;
          }
          case Cursor.W_RESIZE_CURSOR: {
              int dx = getDeltaX(deltaX);
              c.setBounds(
                  startingBounds.x - dx,
                  startingBounds.y,
                  startingBounds.width + dx,
                  startingBounds.height);
              break;
          }
          case Cursor.MOVE_CURSOR: {
              c.setLocation(
                  startingBounds.x - deltaX,
                  startingBounds.y - deltaY);
              break;
          }
          default:
            return;
        }
        //setBounds(newX, newY, newW, newH);
        parent.revalidate();
        //parent.repaint();
    }
    @Override public void mouseReleased(MouseEvent e) {
        startPos = null;
        startingBounds = null;
    }
}

//Resizable Components - Santhosh Kumar's Weblog
//http://www.jroller.com/santhosh/entry/resizable_components
interface ResizableBorder extends Border {
    int getResizeCursor(MouseEvent e);
}

class DefaultResizableBorder implements ResizableBorder, SwingConstants {
    private final int dist;

    private static int[] locations = {
        NORTH,
        SOUTH,
        WEST,
        EAST,
        NORTH_WEST,
        NORTH_EAST,
        SOUTH_WEST,
        SOUTH_EAST,
        0, // move
        -1, // no location
    };

    private static int[] cursors = {
        Cursor.N_RESIZE_CURSOR,
        Cursor.S_RESIZE_CURSOR,
        Cursor.W_RESIZE_CURSOR,
        Cursor.E_RESIZE_CURSOR,
        Cursor.NW_RESIZE_CURSOR,
        Cursor.NE_RESIZE_CURSOR,
        Cursor.SW_RESIZE_CURSOR,
        Cursor.SE_RESIZE_CURSOR,
        Cursor.MOVE_CURSOR,
        Cursor.DEFAULT_CURSOR,
    };

    public DefaultResizableBorder(int dist) {
        super();
        this.dist = dist;
    }

    @Override public Insets getBorderInsets(Component component) {
        return new Insets(dist, dist, dist, dist);
    }

    @Override public boolean isBorderOpaque() {
        return false;
    }

    @Override public void paintBorder(Component component, Graphics g, int x, int y, int w, int h) {
        g.setColor(Color.black);
        g.drawRect(x + dist / 2, y + dist / 2, w - dist, h - dist);
        for (int i = 0; i < locations.length - 2; i++) {
            Rectangle rect = getRectangle(x, y, w, h, locations[i]);
            g.setColor(Color.WHITE);
            g.fillRect(rect.x, rect.y, rect.width - 1, rect.height - 1);
            g.setColor(Color.BLACK);
            g.drawRect(rect.x, rect.y, rect.width - 1, rect.height - 1);
        }
    }

    private Rectangle getRectangle(int x, int y, int w, int h, int location) {
        switch(location) {
          case NORTH:      return new Rectangle(x + w / 2 - dist / 2, y, dist, dist);
          case SOUTH:      return new Rectangle(x + w / 2 - dist / 2, y + h - dist, dist, dist);
          case WEST:       return new Rectangle(x, y + h / 2 - dist / 2, dist, dist);
          case EAST:       return new Rectangle(x + w - dist, y + h / 2 - dist / 2, dist, dist);
          case NORTH_WEST: return new Rectangle(x, y, dist, dist);
          case NORTH_EAST: return new Rectangle(x + w - dist, y, dist, dist);
          case SOUTH_WEST: return new Rectangle(x, y + h - dist, dist, dist);
          case SOUTH_EAST: return new Rectangle(x + w - dist, y + h - dist, dist, dist);
          default:         return null; //throw  new AssertionError("Unknown location");
        }
        //return null;
    }

    public int getResizeCursor(MouseEvent e) {
        Component c = e.getComponent();
        int w = c.getWidth();
        int h = c.getHeight();
        Point pt = e.getPoint();

        Rectangle bounds = new Rectangle(0, 0, w, h);
        if (!bounds.contains(pt)) {
            return Cursor.DEFAULT_CURSOR;
        }

        Rectangle actualBounds = new Rectangle(dist, dist, w - 2 * dist, h - 2 * dist);
        if (actualBounds.contains(pt)) {
            return Cursor.DEFAULT_CURSOR;
        }
        for (int i = 0; i < locations.length - 2; i++) {
            Rectangle r = getRectangle(0, 0, w, h, locations[i]);
            if (r.contains(pt)) {
                return cursors[i];
            }
        }
        return Cursor.MOVE_CURSOR;
    }
}
