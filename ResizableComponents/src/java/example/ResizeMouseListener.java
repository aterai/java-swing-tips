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
    e.getComponent().setCursor(Cursor.getDefaultCursor());
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
    Directions.getByCursorType(cursor.getType()).ifPresent(dir -> {
      Point delta = getLimitedDelta(parent.getBounds(), deltaX, deltaY);
      c.setBounds(dir.getBounds(startingBounds, delta));
    });
    parent.revalidate();
  }

  private int getDeltaX(int dx) {
    int left = Math.min(MAX.width - startingBounds.width, startingBounds.x);
    return Math.max(Math.min(dx, left), MIN.width - startingBounds.width);
    // int deltaX = dx;
    // if (deltaX < MIN.width - startingBounds.width) {
    //   deltaX = MIN.width - startingBounds.width;
    // } else if (deltaX > MAX.width - startingBounds.width) {
    //   deltaX = MAX.width - startingBounds.width;
    // }
    // if (startingBounds.x < deltaX) {
    //   deltaX = startingBounds.x;
    // }
    // return deltaX;
  }

  private int getDeltaX(int dx, Rectangle pr) {
    int right = Math.max(startingBounds.width - MAX.width, startingBounds.x + startingBounds.width - pr.width);
    return Math.min(Math.max(dx, right), startingBounds.width - MIN.width);
    // int deltaX = dx;
    // if (startingBounds.width - MIN.width < deltaX) {
    //   deltaX = startingBounds.width - MIN.width;
    // } else if (startingBounds.width - MAX.width > deltaX) {
    //   deltaX = startingBounds.width - MAX.width;
    // }
    // if (startingBounds.x + startingBounds.width - pr.width > deltaX) {
    //   deltaX = startingBounds.x + startingBounds.width - pr.width;
    // }
    // return deltaX;
  }

  private int getDeltaY(int dy) {
    int top = Math.min(MAX.height - startingBounds.height, startingBounds.y);
    return Math.max(Math.min(dy, top), MIN.height - startingBounds.height);
    // int deltaY = dy;
    // if (deltaY < MIN.height - startingBounds.height) {
    //   deltaY = MIN.height - startingBounds.height;
    // } else if (deltaY > MAX.height - startingBounds.height) {
    //   deltaY = MAX.height - startingBounds.height;
    // }
    // if (deltaY < startingBounds.y) {
    //   deltaY = startingBounds.y;
    // }
    // return deltaY;
  }

  private int getDeltaY(int dy, Rectangle pr) {
    int bottom = Math.max(startingBounds.height - MAX.height, startingBounds.y + startingBounds.height - pr.height);
    return Math.min(Math.max(dy, bottom), startingBounds.height - MIN.height);
    // int deltaY = dy;
    // if (startingBounds.height - MIN.height < deltaY) {
    //   deltaY = startingBounds.height - MIN.height;
    // } else if (startingBounds.height - MAX.height > deltaY) {
    //   deltaY = startingBounds.height - MAX.height;
    // }
    // if (startingBounds.y + startingBounds.height - deltaY > pr.height) {
    //   deltaY = startingBounds.y + startingBounds.height - pr.height;
    // }
    // return deltaY;
  }

  private Point getLimitedDelta(Rectangle pr, int deltaX, int deltaY) {
    switch (cursor.getType()) {
      case Cursor.N_RESIZE_CURSOR: return new Point(0, getDeltaY(deltaY));
      case Cursor.S_RESIZE_CURSOR: return new Point(0, getDeltaY(deltaY, pr));
      case Cursor.W_RESIZE_CURSOR: return new Point(getDeltaX(deltaX), 0);
      case Cursor.E_RESIZE_CURSOR: return new Point(getDeltaX(deltaX, pr), 0);
      case Cursor.NW_RESIZE_CURSOR: return new Point(getDeltaX(deltaX), getDeltaY(deltaY));
      case Cursor.SW_RESIZE_CURSOR: return new Point(getDeltaX(deltaX), getDeltaY(deltaY, pr));
      case Cursor.NE_RESIZE_CURSOR: return new Point(getDeltaX(deltaX, pr), getDeltaY(deltaY));
      case Cursor.SE_RESIZE_CURSOR: return new Point(getDeltaX(deltaX, pr), getDeltaY(deltaY, pr));
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
  @SuppressWarnings("ImmutableEnumChecker")
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
