// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.io.Serializable;
import java.util.Objects;

public final class WindowState implements Serializable {
  private static final long serialVersionUID = 1415435143L;
  private static final Dimension MIN = new Dimension(10, 10);
  private final Point location = new Point();
  private final Dimension size = new Dimension(320, 240);
  // public WindowState() {}

  public Point getLocation() {
    return location;
  }

  public void setLocation(Point pt) {
    if (pt.x >= 0 && pt.y >= 0) {
      this.location.setLocation(pt);
    }
  }

  public Dimension getSize() {
    return size;
  }

  public void setSize(Dimension sz) {
    size.setSize(Math.max(MIN.width, sz.width), Math.max(MIN.height, sz.height));
  }

  @Override public int hashCode() {
    return Objects.hash(location, size);
  }

  @Override public boolean equals(Object o) {
    return this == o || o instanceof WindowState && equals2((WindowState) o);
  }

  private boolean equals2(WindowState ws) {
    return Objects.equals(ws.getLocation(), location) && Objects.equals(ws.getSize(), size);
  }

  @Override public String toString() {
    return String.format("location: %s, size: %s", location, size);
  }
}
