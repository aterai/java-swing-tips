// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:

package example;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Objects;
import javax.swing.*;

public class JTabbedPaneWithCloseIcons extends JTabbedPane {
  public JTabbedPaneWithCloseIcons() {
    super();
    addMouseListener(new MouseAdapter() {
      @Override public void mouseClicked(MouseEvent e) {
        int index = indexAtLocation(e.getX(), e.getY());
        if (index < 0) {
          return;
        }
        Rectangle rect = ((SimpleCloseTabIcon) getIconAt(index)).getBounds();
        if (rect.contains(e.getX(), e.getY())) {
          removeTabAt(index);
        }
      }
    });
  }

  @Override public void addTab(String title, Component component) {
    super.addTab(title, new SimpleCloseTabIcon(null), component);
  }

  @Override public void addTab(String title, Icon icon, Component component) {
    super.addTab(title, new SimpleCloseTabIcon(icon), component);
  }
}

// Copied from
// JTabbedPane with close Icons | Oracle Forums
// https://community.oracle.com/thread/1356993
/**
 * The class which generates the 'X' icon for the tabs. The constructor
 * accepts an icon which is extra to the 'X' icon, so you can have tabs
 * like in JBuilder. This value is null if no extra icon is required.
 */
class SimpleCloseTabIcon implements Icon {
  private final Icon fileIcon;
  private final Dimension dim = new Dimension(16, 16);
  private final Point pos = new Point();

  protected SimpleCloseTabIcon(Icon fileIcon) {
    this.fileIcon = fileIcon;
  }

  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    pos.setLocation(x, y);
    Graphics2D g2 = (Graphics2D) g.create();
    g2.translate(x, y + 2);
    g2.setPaint(Color.BLACK);

    g2.drawLine(1,  0, 12,  0);
    g2.drawLine(1, 13, 12, 13);
    g2.drawLine(0,  1,  0, 12);
    g2.drawLine(13, 1, 13, 12);
    g2.drawLine(3,  3, 10, 10);
    g2.drawLine(3,  4,  9, 10);
    g2.drawLine(4,  3, 10,  9);
    g2.drawLine(10, 3,  3, 10);
    g2.drawLine(10, 4,  4, 10);
    g2.drawLine(9,  3,  3,  9);

    if (Objects.nonNull(fileIcon)) {
      fileIcon.paintIcon(c, g2, dim.width, 0);
    }
    g2.dispose();
  }

  @Override public int getIconWidth() {
    int a = Objects.nonNull(fileIcon) ? fileIcon.getIconWidth() : 0;
    return dim.width + a;
  }

  @Override public int getIconHeight() {
    return dim.height;
  }

  public Rectangle getBounds() {
    return new Rectangle(pos, dim);
  }
}
