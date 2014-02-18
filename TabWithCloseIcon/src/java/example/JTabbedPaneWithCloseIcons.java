package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class JTabbedPaneWithCloseIcons extends JTabbedPane {
    public JTabbedPaneWithCloseIcons() {
        super();
        addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                tabClicked(e);
            }
        });
    }
    @Override public void addTab(String title, Component component) {
        this.addTab(title, component, null);
    }
    public void addTab(String title, Component component, Icon extraIcon) {
        super.addTab(title, new SimpleCloseTabIcon(extraIcon), component);
    }
    private void tabClicked(MouseEvent e) {
        int index = getUI().tabForCoordinate(this, e.getX(), e.getY());
        if(index<0) {
            return;
        }
        Rectangle rect = ((SimpleCloseTabIcon)getIconAt(index)).getBounds();
        if(rect.contains(e.getX(), e.getY())) {
            removeTabAt(index);
        }
    }
}

//Copid from
//JTabbedPane with close Icons | Oracle Forums
//https://community.oracle.com/thread/1356993
/**
 * The class which generates the 'X' icon for the tabs. The constructor
 * accepts an icon which is extra to the 'X' icon, so you can have tabs
 * like in JBuilder. This value is null if no extra icon is required.
 */
class SimpleCloseTabIcon implements Icon {
    private final Icon fileIcon;
    private final Dimension dim = new Dimension(16, 16);
    private final Point pos = new Point();

    public SimpleCloseTabIcon(Icon fileIcon) {
        this.fileIcon = fileIcon;
    }
    @Override public void paintIcon(Component c, Graphics g, int x, int y) {
        pos.setLocation(x, y);
        Color col = g.getColor();
        g.setColor(Color.BLACK);

        int yp = y + 2;
        g.drawLine(x+1,  yp,    x+12, yp);
        g.drawLine(x+1,  yp+13, x+12, yp+13);
        g.drawLine(x,    yp+1,  x,    yp+12);
        g.drawLine(x+13, yp+1,  x+13, yp+12);
        g.drawLine(x+3,  yp+3,  x+10, yp+10);
        g.drawLine(x+3,  yp+4,  x+9,  yp+10);
        g.drawLine(x+4,  yp+3,  x+10, yp+9);
        g.drawLine(x+10, yp+3,  x+3,  yp+10);
        g.drawLine(x+10, yp+4,  x+4,  yp+10);
        g.drawLine(x+9,  yp+3,  x+3,  yp+9);

        g.setColor(col);
        if(fileIcon != null) {
            fileIcon.paintIcon(c, g, x+dim.width, yp);
        }
    }
    @Override public int getIconWidth() {
        return fileIcon == null ? dim.width : dim.width + fileIcon.getIconWidth();
    }
    @Override public int getIconHeight() {
        return dim.height;
    }
    public Rectangle getBounds() {
        return new Rectangle(pos, dim);
    }
}
