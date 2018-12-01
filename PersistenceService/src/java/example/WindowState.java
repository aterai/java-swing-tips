package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

import java.awt.*;
import java.io.Serializable;

public class WindowState implements Serializable {
    private static final long serialVersionUID = 1415435143L;
    private Point location = new Point();
    private Dimension size = new Dimension(320, 240);
    // public WindowState() {}
    public final Point getLocation() {
        return location;
    }
    public final void setLocation(Point location) {
        this.location = location;
    }
    public final Dimension getSize() {
        return size;
    }
    public final void setSize(Dimension size) {
        this.size = size;
    }
}
