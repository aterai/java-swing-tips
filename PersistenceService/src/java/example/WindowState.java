package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.io.*;

public class WindowState implements Serializable{
    private static final long serialVersionUID = 1415435143L;
    private Point location = new Point(0, 0);
    private Dimension size = new Dimension(320, 240);
    public WindowState() {}
    public Point getLocation() {
        return this.location;
    }
    public void setLocation(Point location) {
        this.location = location;
    }
    public Dimension getSize() {
        return size;
    }
    public void setSize(Dimension size) {
        this.size = size;
    }
}
