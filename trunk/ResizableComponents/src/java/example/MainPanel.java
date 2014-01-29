package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

public class MainPanel extends JPanel {
    //private final JPanel panel = new JPanel((LayoutManager)null);
    private final JLayeredPane panel = new JLayeredPane();
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
        panel.setComponentPopupMenu(popup);
        panel.addMouseListener(new MouseAdapter() { /* Dummy listener */ });
        add(panel);
        toolbar.add(new AbstractAction("add table") {
            @Override public void actionPerformed(ActionEvent e) {
                pt.setLocation(pt.x+20, pt.y+20);
                createTable();
            }
        });
        toolbar.addSeparator();
        toolbar.add(new AbstractAction("add tree") {
            @Override public void actionPerformed(ActionEvent e) {
                pt.setLocation(pt.x+20, pt.y+20);
                createTree();
            }
        });
        add(toolbar, BorderLayout.NORTH);
        setPreferredSize(new Dimension(320, 240));
    }
    private void createTree() {
        JTree tree = new JTree();
        Component comp = new JScrollPane(tree);
        comp.setPreferredSize(new Dimension(160, 160));
        Dimension bounds = comp.getPreferredSize();
        JResizer resizer = new JResizer(comp);
        resizer.setBounds(pt.x, pt.y, bounds.width, bounds.height);
        panel.add(resizer);
        //if(panel instanceof JLayeredPane) {
            panel.moveToFront(resizer);
        //}else{
        //    panel.revalidate();
        //    panel.repaint();
        //}
    }
    private void createTable() {
        JTable table = new JTable(new Object[][] {
            {"[xxxxxxxxx]", "[yyyyyyyyyyy]", "[zzzzzzzzz]"},
            {"[xxxxxxx]", "[yyyyyyy]", "[zzzzzzzzz]"},
            {"[xxxxxxxxxx]", "[yyyyyyyyyy]", "[zzzzzzzzzz]"}
        }, new String[] {"col1", "col2", "col3"});
        table.setPreferredScrollableViewportSize(new Dimension(160, 160));
        Component comp = new JScrollPane(table);
        Dimension bounds = comp.getPreferredSize();
        JResizer resizer = new JResizer(comp);
        resizer.setBounds(pt.x, pt.y, bounds.width, bounds.height);
        panel.add(resizer);
        //if(panel instanceof JLayeredPane) {
            panel.moveToFront(resizer);
        //}else{
        //    panel.revalidate();
        //    panel.repaint();
        //}
    }
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                createAndShowGUI();
            }
        });
    }
    public static void createAndShowGUI() {
        try{
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }catch(ClassNotFoundException | InstantiationException |
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

class JResizer extends JComponent { // implements Serializable {
    private transient MouseInputListener resizeListener;

    public JResizer(Component comp) {
        this(comp, new DefaultResizableBorder(6));
    }
    public JResizer(Component comp, ResizableBorder border) {
        super();
        resizeListener = new ResizeMouseListener();
        setLayout(new BorderLayout());
        add(comp);
        setBorder(border);
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
        if(border instanceof ResizableBorder) {
            addMouseListener(resizeListener);
            addMouseMotionListener(resizeListener);
        }
        super.setBorder(border);
    }

    private class ResizeMouseListener extends MouseInputAdapter {
        private int cursor;
        private Point startPos = null;
        private Rectangle startingBounds = null;
        @Override public void mouseMoved(MouseEvent e) {
            ResizableBorder border = (ResizableBorder)getBorder();
            setCursor(Cursor.getPredefinedCursor(border.getResizeCursor(e)));
        }
        @Override public void mouseExited(MouseEvent e) {
            setCursor(Cursor.getDefaultCursor());
        }
        @Override public void mousePressed(MouseEvent e) {
            ResizableBorder border = (ResizableBorder)getBorder();
            cursor = border.getResizeCursor(e);
            startPos = SwingUtilities.convertPoint((Component)e.getSource(), e.getX(), e.getY(), null);
            startingBounds = getBounds();
            if(getParent() instanceof JLayeredPane) {
                ((JLayeredPane)getParent()).moveToFront((Component)e.getSource());
            }
        }
        // %JAVA_HOME%/src/javax/swing/plaf/basic/BasicInternalFrameUI.java
        @Override public void mouseDragged(MouseEvent e) {
            if(startPos==null || startingBounds==null) {
                return;
            }
            Point p = SwingUtilities.convertPoint((Component)e.getSource(), e.getX(), e.getY(), null);
            int deltaX = startPos.x - p.x;
            int deltaY = startPos.y - p.y;
            int newX = getX();
            int newY = getY();
            int newW = getWidth();
            int newH = getHeight();

            JComponent parent = (JComponent)getParent();
            Rectangle parentBounds = parent.getBounds();
            Dimension min = new Dimension(50,50);
            Dimension max = new Dimension(500,500);

            switch(cursor) {
              case Cursor.N_RESIZE_CURSOR: {
                  if(startingBounds.height + deltaY < min.height) {
                      deltaY = -(startingBounds.height - min.height);
                  }else if(startingBounds.height + deltaY > max.height) {
                      deltaY = max.height - startingBounds.height;
                  }
                  if(startingBounds.y - deltaY < 0) {
                      deltaY = startingBounds.y;
                  }
                  newX = startingBounds.x;
                  newY = startingBounds.y - deltaY;
                  newW = startingBounds.width;
                  newH = startingBounds.height + deltaY;
                  break;
              }
              case Cursor.NE_RESIZE_CURSOR: {
                  if(startingBounds.height + deltaY < min.height) {
                      deltaY = -(startingBounds.height - min.height);
                  }else if(startingBounds.height + deltaY > max.height) {
                      deltaY = max.height - startingBounds.height;
                  }
                  if(startingBounds.y - deltaY < 0) { deltaY = startingBounds.y; }
                  if(startingBounds.width - deltaX < min.width) {
                      deltaX = startingBounds.width - min.width;
                  }else if(startingBounds.width - deltaX > max.width) {
                      deltaX = -(max.width - startingBounds.width);
                  }
                  if(startingBounds.x + startingBounds.width - deltaX > parentBounds.width) {
                      deltaX = startingBounds.x + startingBounds.width - parentBounds.width;
                  }
                  newX = startingBounds.x;
                  newY = startingBounds.y - deltaY;
                  newW = startingBounds.width - deltaX;
                  newH = startingBounds.height + deltaY;
                  break;
              }
              case Cursor.E_RESIZE_CURSOR: {
                  if(startingBounds.width - deltaX < min.width) {
                      deltaX = startingBounds.width - min.width;
                  }else if(startingBounds.width - deltaX > max.width) {
                      deltaX = -(max.width - startingBounds.width);
                  }
                  if(startingBounds.x + startingBounds.width - deltaX > parentBounds.width) {
                      deltaX = startingBounds.x + startingBounds.width - parentBounds.width;
                  }
                  newW = startingBounds.width - deltaX;
                  newH = startingBounds.height;
                  break;
              }
              case Cursor.SE_RESIZE_CURSOR: {
                  if(startingBounds.width - deltaX < min.width) {
                      deltaX = startingBounds.width - min.width;
                  }else if(startingBounds.width - deltaX > max.width) {
                      deltaX = -(max.width - startingBounds.width);
                  }
                  if(startingBounds.x + startingBounds.width - deltaX > parentBounds.width) {
                      deltaX = startingBounds.x + startingBounds.width - parentBounds.width;
                  }
                  if(startingBounds.height - deltaY < min.height) {
                      deltaY = startingBounds.height - min.height;
                  }else if(startingBounds.height - deltaY > max.height) {
                      deltaY = -(max.height - startingBounds.height);
                  }
                  if(startingBounds.y + startingBounds.height - deltaY > parentBounds.height) {
                      deltaY = startingBounds.y + startingBounds.height - parentBounds.height;
                  }
                  newW = startingBounds.width - deltaX;
                  newH = startingBounds.height - deltaY;
                  break;
              }
              case Cursor.S_RESIZE_CURSOR: {
                  if(startingBounds.height - deltaY < min.height) {
                      deltaY = startingBounds.height - min.height;
                  }else if(startingBounds.height - deltaY > max.height) {
                      deltaY = -(max.height - startingBounds.height);
                  }
                  if(startingBounds.y + startingBounds.height - deltaY > parentBounds.height) {
                      deltaY = startingBounds.y + startingBounds.height - parentBounds.height;
                  }
                  newW = startingBounds.width;
                  newH = startingBounds.height - deltaY;
                  break;
              }
              case Cursor.SW_RESIZE_CURSOR: {
                  if(startingBounds.height - deltaY < min.height) {
                      deltaY = startingBounds.height - min.height;
                  }else if(startingBounds.height - deltaY > max.height) {
                      deltaY = -(max.height - startingBounds.height);
                  }
                  if(startingBounds.y + startingBounds.height - deltaY > parentBounds.height) {
                      deltaY = startingBounds.y + startingBounds.height - parentBounds.height;
                  }
                  if(startingBounds.width + deltaX < min.width) {
                      deltaX = -(startingBounds.width - min.width);
                  }else if(startingBounds.width + deltaX > max.width) {
                      deltaX = max.width - startingBounds.width;
                  }
                  if(startingBounds.x - deltaX < 0) {
                      deltaX = startingBounds.x;
                  }
                  newX = startingBounds.x - deltaX;
                  newY = startingBounds.y;
                  newW = startingBounds.width + deltaX;
                  newH = startingBounds.height - deltaY;
                  break;
              }
              case Cursor.W_RESIZE_CURSOR: {
                  if(startingBounds.width + deltaX < min.width) {
                      deltaX = -(startingBounds.width - min.width);
                  }else if(startingBounds.width + deltaX > max.width) {
                      deltaX = max.width - startingBounds.width;
                  }
                  if(startingBounds.x - deltaX < 0) {
                      deltaX = startingBounds.x;
                  }
                  newX = startingBounds.x - deltaX;
                  newY = startingBounds.y;
                  newW = startingBounds.width + deltaX;
                  newH = startingBounds.height;
                  break;
              }
              case Cursor.NW_RESIZE_CURSOR: {
                  if(startingBounds.width + deltaX < min.width) {
                      deltaX = -(startingBounds.width - min.width);
                  }else if(startingBounds.width + deltaX > max.width) {
                      deltaX = max.width - startingBounds.width;
                  }
                  if(startingBounds.x - deltaX < 0) {
                      deltaX = startingBounds.x;
                  }
                  if(startingBounds.height + deltaY < min.height) {
                      deltaY = -(startingBounds.height - min.height);
                  }else if(startingBounds.height + deltaY > max.height) {
                      deltaY = max.height - startingBounds.height;
                  }
                  if(startingBounds.y - deltaY < 0) {
                      deltaY = startingBounds.y;
                  }
                  newX = startingBounds.x - deltaX;
                  newY = startingBounds.y - deltaY;
                  newW = startingBounds.width + deltaX;
                  newH = startingBounds.height + deltaY;
                  break;
              }
              case Cursor.MOVE_CURSOR: {
                  newX = startingBounds.x - deltaX;
                  newY = startingBounds.y - deltaY;
                  break;
              }
              default:
                return;
            }
            setBounds(newX, newY, newW, newH);
            parent.revalidate();
            //parent.repaint();
        }
        @Override public void mouseReleased(MouseEvent e) {
            startPos = null;
            startingBounds = null;
        }
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
        g.drawRect(x+dist/2, y+dist/2, w-dist, h-dist);
        for(int i=0; i<locations.length-2; i++) {
            Rectangle rect = getRectangle(x, y, w, h, locations[i]);
            g.setColor(Color.WHITE);
            g.fillRect(rect.x, rect.y, rect.width-1, rect.height-1);
            g.setColor(Color.BLACK);
            g.drawRect(rect.x, rect.y, rect.width-1, rect.height-1);
        }
    }

    private Rectangle getRectangle(int x, int y, int w, int h, int location) {
        switch(location) {
          case NORTH:      return new Rectangle(x+w/2-dist/2, y, dist, dist);
          case SOUTH:      return new Rectangle(x+w/2-dist/2, y+h-dist, dist, dist);
          case WEST:       return new Rectangle(x, y+h/2-dist/2, dist, dist);
          case EAST:       return new Rectangle(x+w-dist, y+h/2-dist/2, dist, dist);
          case NORTH_WEST: return new Rectangle(x, y, dist, dist);
          case NORTH_EAST: return new Rectangle(x+w-dist, y, dist, dist);
          case SOUTH_WEST: return new Rectangle(x, y+h-dist, dist, dist);
          case SOUTH_EAST: return new Rectangle(x+w-dist, y+h-dist, dist, dist);
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
        if(!bounds.contains(pt)) {
            return Cursor.DEFAULT_CURSOR;
        }

        Rectangle actualBounds = new Rectangle(dist, dist, w-2*dist, h-2*dist);
        if(actualBounds.contains(pt)) {
            return Cursor.DEFAULT_CURSOR;
        }
        for(int i=0; i<locations.length-2; i++) {
            Rectangle r = getRectangle(0, 0, w, h, locations[i]);
            if(r.contains(pt)) {
                return cursors[i];
            }
        }
        return Cursor.MOVE_CURSOR;
    }
}
