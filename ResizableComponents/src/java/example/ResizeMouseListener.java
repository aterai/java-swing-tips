// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.EnumSet;
import java.util.Optional;
import java.util.function.BiFunction;
import javax.swing.*;
import javax.swing.event.MouseInputAdapter;

public final class ResizeMouseListener extends MouseInputAdapter {
  private static final Dimension MIN = new Dimension(50, 50);
  private static final Dimension MAX = new Dimension(500, 500);
  private final Point startPos = new Point();
  private final Rectangle startingBounds = new Rectangle();
  private Cursor cursor;

  @Override public void mouseMoved(MouseEvent e) {
    JComponent c = (JComponent) e.getComponent();
    ResizableBorder border = (ResizableBorder) c.getBorder();
    c.setCursor(border.getResizeCursor(e));
  }

  @Override public void mouseExited(MouseEvent e) {
    Component c = e.getComponent();
    c.setCursor(Cursor.getDefaultCursor());
  }

  @Override public void mousePressed(MouseEvent e) {
    JComponent c = (JComponent) e.getComponent();
    ResizableBorder border = (ResizableBorder) c.getBorder();
    cursor = border.getResizeCursor(e);
    startPos.setLocation(SwingUtilities.convertPoint(c, e.getX(), e.getY(), null));
    startingBounds.setBounds(c.getBounds());
    Container parent = SwingUtilities.getAncestorOfClass(JLayeredPane.class, c);
    if (parent instanceof JLayeredPane) {
      ((JLayeredPane) parent).moveToFront(c);
    }
  }

  @Override public void mouseReleased(MouseEvent e) {
    startingBounds.setSize(0, 0);
  }

  // @see %JAVA_HOME%/src/javax/swing/plaf/basic/BasicInternalFrameUI.java
  @Override public void mouseDragged(MouseEvent e) {
    if (startingBounds.isEmpty()) {
      return;
    }
    Component c = e.getComponent();
    Point p = SwingUtilities.convertPoint(c, e.getX(), e.getY(), null);
    int deltaX = startPos.x - p.x;
    int deltaY = startPos.y - p.y;
    Container parent = SwingUtilities.getUnwrappedParent(c);
    Rectangle parentBounds = parent.getBounds();
    Directions.getByCursorType(cursor.getType()).ifPresent(dir -> {
      // Point delta = dir.getBoundedDelta(startingBounds, parentBounds, new Point(deltaX, deltaY));
      Point delta = getLimitedDelta(parentBounds, deltaX, deltaY);
      c.setBounds(dir.getBounds(startingBounds, delta));
    });
    parent.revalidate();
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

  private Point getLimitedDelta(Rectangle parentBounds, int deltaX, int deltaY) {
    switch (cursor.getType()) {
      case Cursor.NW_RESIZE_CURSOR: return new Point(getDeltaX(deltaX), getDeltaY(deltaY));
      case Cursor.N_RESIZE_CURSOR: return new Point(deltaX, getDeltaY(deltaY));
      case Cursor.NE_RESIZE_CURSOR: return new Point(getDeltaX(deltaX, parentBounds), getDeltaY(deltaY));
      case Cursor.E_RESIZE_CURSOR: return new Point(getDeltaX(deltaX, parentBounds), deltaY);
      case Cursor.SE_RESIZE_CURSOR: return new Point(getDeltaX(deltaX, parentBounds), getDeltaY(deltaY, parentBounds));
      case Cursor.S_RESIZE_CURSOR: return new Point(deltaX, getDeltaY(deltaY, parentBounds));
      case Cursor.SW_RESIZE_CURSOR: return new Point(getDeltaX(deltaX), getDeltaY(deltaY, parentBounds));
      case Cursor.W_RESIZE_CURSOR: return new Point(getDeltaX(deltaX), deltaY);
      default: return new Point(deltaX, deltaY);
    }
  }
}

enum Directions {
  NORTH(Cursor.N_RESIZE_CURSOR, (r, d) -> new Rectangle(r.x, r.y - d.y, r.width, r.height + d.y)),
  SOUTH(Cursor.S_RESIZE_CURSOR, (r, d) -> new Rectangle(r.x, r.y, r.width, r.height - d.y)),
  WEST(Cursor.W_RESIZE_CURSOR, (r, d) -> new Rectangle(r.x - d.x, r.y, r.width + d.x, r.height)),
  EAST(Cursor.E_RESIZE_CURSOR, (r, d) -> new Rectangle(r.x, r.y, r.width - d.x, r.height)),
  NORTH_WEST(Cursor.NW_RESIZE_CURSOR, (r, d) -> new Rectangle(r.x - d.x, r.y - d.y, r.width + d.x, r.height + d.y)),
  NORTH_EAST(Cursor.NE_RESIZE_CURSOR, (r, d) -> new Rectangle(r.x, r.y - d.y, r.width - d.x, r.height + d.y)),
  SOUTH_WEST(Cursor.SW_RESIZE_CURSOR, (r, d) -> new Rectangle(r.x, r.y, r.width, r.height)),
  SOUTH_EAST(Cursor.SE_RESIZE_CURSOR, (r, d) -> new Rectangle(r.x, r.y, r.width - d.x, r.height - d.y)),
  MOVE(Cursor.MOVE_CURSOR, (r, d) -> new Rectangle(r.x - d.x, r.y - d.y, r.width, r.height));

  private final int cursor;
  private final BiFunction<Rectangle, Point, Rectangle> getBounds;

  Directions(int cursor, BiFunction<Rectangle, Point, Rectangle> getBounds) {
    this.cursor = cursor;
    this.getBounds = getBounds;
  }

  public Rectangle getBounds(Rectangle rect, Point delta) {
    return getBounds.apply(rect, delta);
  }

  public static Optional<Directions> getByCursorType(int cursor) {
    return EnumSet.allOf(Directions.class).stream().filter(d -> d.cursor == cursor).findFirst();
  }
}
