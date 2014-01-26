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
    public void addTab(String title, Component component) {
        this.addTab(title, component, null);
    }
    public void addTab(String title, Component component, Icon extraIcon) {
        super.addTab(title, new CloseTabIcon(extraIcon), component);
    }
    private void tabClicked(MouseEvent e) {
        int index = getUI().tabForCoordinate(this, e.getX(), e.getY());
        if(index<0) {
            return;
        }
        Rectangle rect = ((CloseTabIcon)getIconAt(index)).getBounds();
        if(rect.contains(e.getX(), e.getY())) {
            removeTabAt(index);
        }
    }
}

/**
 * The class which generates the 'X' icon for the tabs. The constructor
 * accepts an icon which is extra to the 'X' icon, so you can have tabs
 * like in JBuilder. This value is null if no extra icon is required.
 */
class CloseTabIcon implements Icon {
    private int x_pos;
    private int y_pos;
    private final int width;
    private final int height;
    private Icon fileIcon;
    public CloseTabIcon(Icon fileIcon) {
        this.fileIcon=fileIcon;
        width  = 16;
        height = 16;
    }
    @Override public void paintIcon(Component c, Graphics g, int x, int y) {
        this.x_pos=x;
        this.y_pos=y;
        Color col=g.getColor();
        g.setColor(Color.BLACK);
        int y_p=y+2;
        g.drawLine(x+1, y_p, x+12, y_p);
        g.drawLine(x+1, y_p+13, x+12, y_p+13);
        g.drawLine(x, y_p+1, x, y_p+12);
        g.drawLine(x+13, y_p+1, x+13, y_p+12);
        g.drawLine(x+3, y_p+3, x+10, y_p+10);
        g.drawLine(x+3, y_p+4, x+9, y_p+10);
        g.drawLine(x+4, y_p+3, x+10, y_p+9);
        g.drawLine(x+10, y_p+3, x+3, y_p+10);
        g.drawLine(x+10, y_p+4, x+4, y_p+10);
        g.drawLine(x+9, y_p+3, x+3, y_p+9);
        g.setColor(col);
        if(fileIcon != null) {
            fileIcon.paintIcon(c, g, x+width, y_p);
        }
    }
    @Override public int getIconWidth() {
        return fileIcon != null ? width + fileIcon.getIconWidth() : width;
    }
    @Override public int getIconHeight() {
        return height;
    }
    public Rectangle getBounds() {
        return new Rectangle(x_pos, y_pos, width, height);
    }
}
