// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.EnumSet;
import java.util.Optional;
import javax.swing.*;
import javax.swing.event.MouseInputAdapter;

public final class ResizeMouseListener extends MouseInputAdapter {
  private static final Dimension MIN = new Dimension(50, 50);
  private static final Dimension MAX = new Dimension(500, 500);
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
    if (startRect.isEmpty()) {
      return;
    }
    Component c = e.getComponent();
    Point p = SwingUtilities.convertPoint(c, e.getX(), e.getY(), null);
    int deltaX = startPos.x - p.x;
    int deltaY = startPos.y - p.y;
    Container parent = SwingUtilities.getUnwrappedParent(c);
    int cursorType = Optional.ofNullable(cursor)
        .map(Cursor::getType)
        .orElse(Cursor.DEFAULT_CURSOR);
    Directions.getByCursorType(cursorType).ifPresent(dir -> {
      Point delta = getLimitedDelta(cursorType, parent.getBounds(), deltaX, deltaY);
      c.setBounds(dir.getBounds(startRect, delta));
    });
    parent.revalidate();
  }

  private int getDeltaX(int dx) {
    int left = Math.min(MAX.width - startRect.width, startRect.x);
    return Math.max(Math.min(dx, left), MIN.width - startRect.width);
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
    int right = Math.max(startRect.width - MAX.width, startRect.x + startRect.width - pr.width);
    return Math.min(Math.max(dx, right), startRect.width - MIN.width);
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
    int top = Math.min(MAX.height - startRect.height, startRect.y);
    return Math.max(Math.min(dy, top), MIN.height - startRect.height);
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
    int maxHeight = startRect.height - MAX.height;
    int bottom = Math.max(maxHeight, startRect.y + startRect.height - pr.height);
    return Math.min(Math.max(dy, bottom), startRect.height - MIN.height);
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

  @SuppressWarnings("PMD.OnlyOneReturn")
  private Point getLimitedDelta(int cursorType, Rectangle pr, int deltaX, int deltaY) {
    switch (cursorType) {
      case Cursor.N_RESIZE_CURSOR:
        return new Point(0, getDeltaY(deltaY));
      case Cursor.S_RESIZE_CURSOR:
        return new Point(0, getDeltaY(deltaY, pr));
      case Cursor.W_RESIZE_CURSOR:
        return new Point(getDeltaX(deltaX), 0);
      case Cursor.E_RESIZE_CURSOR:
        return new Point(getDeltaX(deltaX, pr), 0);
      case Cursor.NW_RESIZE_CURSOR:
        return new Point(getDeltaX(deltaX), getDeltaY(deltaY));
      case Cursor.SW_RESIZE_CURSOR:
        return new Point(getDeltaX(deltaX), getDeltaY(deltaY, pr));
      case Cursor.NE_RESIZE_CURSOR:
        return new Point(getDeltaX(deltaX, pr), getDeltaY(deltaY));
      case Cursor.SE_RESIZE_CURSOR:
        return new Point(getDeltaX(deltaX, pr), getDeltaY(deltaY, pr));
      default:
        return new Point(deltaX, deltaY);
    }
  }
}

enum Directions {
  NORTH(Cursor.N_RESIZE_CURSOR) {
    @Override public Rectangle getBounds(Rectangle r, Point d) {
      return new Rectangle(r.x, r.y - d.y, r.width, r.height + d.y);
    }
  },
  SOUTH(Cursor.S_RESIZE_CURSOR) {
    @Override public Rectangle getBounds(Rectangle r, Point d) {
      return new Rectangle(r.x, r.y, r.width, r.height - d.y);
    }
  },
  WEST(Cursor.W_RESIZE_CURSOR) {
    @Override public Rectangle getBounds(Rectangle r, Point d) {
      return new Rectangle(r.x - d.x, r.y, r.width + d.x, r.height);
    }
  },
  EAST(Cursor.E_RESIZE_CURSOR) {
    @Override public Rectangle getBounds(Rectangle r, Point d) {
      return new Rectangle(r.x, r.y, r.width - d.x, r.height);
    }
  },
  NORTH_WEST(Cursor.NW_RESIZE_CURSOR) {
    @Override public Rectangle getBounds(Rectangle r, Point d) {
      return new Rectangle(r.x - d.x, r.y - d.y, r.width + d.x, r.height + d.y);
    }
  },
  NORTH_EAST(Cursor.NE_RESIZE_CURSOR) {
    @Override public Rectangle getBounds(Rectangle r, Point d) {
      return new Rectangle(r.x, r.y - d.y, r.width - d.x, r.height + d.y);
    }
  },
  SOUTH_WEST(Cursor.SW_RESIZE_CURSOR) {
    @Override public Rectangle getBounds(Rectangle r, Point d) {
      return new Rectangle(r.x - d.x, r.y, r.width + d.x, r.height - d.y);
    }
  },
  SOUTH_EAST(Cursor.SE_RESIZE_CURSOR) {
    @Override public Rectangle getBounds(Rectangle r, Point d) {
      return new Rectangle(r.x, r.y, r.width - d.x, r.height - d.y);
    }
  },
  MOVE(Cursor.MOVE_CURSOR) {
    @Override public Rectangle getBounds(Rectangle r, Point d) {
      return new Rectangle(r.x - d.x, r.y - d.y, r.width, r.height);
    }
  };

  private final int cursor;

  Directions(int cursor) {
    this.cursor = cursor;
  }

  public abstract Rectangle getBounds(Rectangle rect, Point delta);

  public static Optional<Directions> getByCursorType(int cursor) {
    return EnumSet.allOf(Directions.class).stream().filter(d -> d.cursor == cursor).findFirst();
  }
}
