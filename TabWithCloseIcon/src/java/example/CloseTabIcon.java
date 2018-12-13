package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
import java.awt.*;
import java.util.Objects;
import javax.swing.*;

// Copied from
// JTabbedPane with close Icons | Oracle Forums
// https://community.oracle.com/thread/1356993

/**
 * The class which generates the 'X' icon for the tabs. The constructor
 * accepts an icon which is extra to the 'X' icon, so you can have tabs
 * like in JBuilder. This value is null if no extra icon is required.
 */
public class CloseTabIcon implements Icon {
  /**
   * the x position of the icon.
   */
  private int xpos;

  /**
   * the y position of the icon.
   */
  private int ypos;

  /**
   * the width the icon.
   */
  private final int width;

  /**
   * the height the icon.
   */
  private final int height;

  /**
   * the additional fileicon.
   */
  private final Icon fileIcon;

  /**
   * true whether the mouse is over this icon, false otherwise.
   */
  protected boolean mouseover;

  /**
   * true whether the mouse is pressed on this icon, false otherwise.
   */
  protected boolean mousepressed;

  /**
   * Creates a new instance of <code>CloseTabIcon</code>.
   * @param fileIcon the additional fileicon, if there is one set
   */
  public CloseTabIcon(Icon fileIcon) {
    this.fileIcon = fileIcon;
    this.width = 16;
    this.height = 16;
  }

  /**
   * Draw the icon at the specified location. Icon implementations may use the
   * Component argument to get properties useful for painting, e.g. the
   * foreground or background color.
   * @param c the component which the icon belongs to
   * @param g the graphic object to draw on
   * @param x the upper left point of the icon in the x direction
   * @param y the upper left point of the icon in the y direction
   */
  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    // boolean doPaintCloseIcon = true;
    // // try {
    // //   JComponent.putClientProperty("isClosable", new Boolean(false));
    // if (c instanceof JTabbedPane) {
    //   JTabbedPane tabbedpane = (JTabbedPane) c;
    //   int tabNumber = tabbedpane.getUI().tabForCoordinate(tabbedpane, x, y);
    //   JComponent curPanel = (JComponent) tabbedpane.getComponentAt(tabNumber);
    //   Object prop = curPanel.getClientProperty("isClosable");
    //   if (Objects.nonNull(prop)) {
    //     doPaintCloseIcon = ((Boolean) prop).booleanValue();
    //   }
    // }
    // // } catch (Exception ex) {
    // //   // Could probably be a ClassCastException
    // //   ex.printStackTrace();
    // // }
    // if (doPaintCloseIcon) {
    xpos = x;
    ypos = y;
    int yp = y + 2; // +2: baseline?

    // if (Objects.nonNull(normalCloseIcon) && !mouseover) {
    //   normalCloseIcon.paintIcon(c, g, x, yp);
    // } else if (Objects.nonNull(hooverCloseIcon) && mouseover && !mousepressed) {
    //   hooverCloseIcon.paintIcon(c, g, x, yp);
    // } else if (Objects.nonNull(pressedCloseIcon) && mousepressed) {
    //   pressedCloseIcon.paintIcon(c, g, x, yp);
    // } else {
    // yp++;

    // Color col = g.getColor();

    Graphics2D g2 = (Graphics2D) g.create();
    if (mousepressed && mouseover) {
      g2.setPaint(Color.WHITE);
      g2.fillRect(x + 1, yp + 1, 12, 13);
    }

    g2.setPaint(mouseover ? Color.ORANGE : Color.BLACK);
    // g2.setPaint(Color.BLACK);
    g2.drawLine(x + 1,  yp,    x + 12, yp);
    g2.drawLine(x + 1,  yp + 13, x + 12, yp + 13);
    g2.drawLine(x,    yp + 1,  x,    yp + 12);
    g2.drawLine(x + 13, yp + 1,  x + 13, yp + 12);
    g2.drawLine(x + 3,  yp + 3,  x + 10, yp + 10);

    // if (mouseover) {
    //   g.setColor(Color.GRAY);
    // }
    g2.drawLine(x + 3,  yp + 4, x + 9,  yp + 10);
    g2.drawLine(x + 4,  yp + 3, x + 10, yp + 9);
    g2.drawLine(x + 10, yp + 3, x + 3,  yp + 10);
    g2.drawLine(x + 10, yp + 4, x + 4,  yp + 10);
    g2.drawLine(x + 9,  yp + 3, x + 3,  yp + 9);
    g2.dispose();

    // if (Objects.nonNull(fileIcon)) {
    //   fileIcon.paintIcon(c, g, x + width, yp);
    // }
  }

  /**
   * Returns the icon's width.
   * @return an int specifying the fixed width of the icon.
   */
  @Override public int getIconWidth() {
    return Objects.nonNull(fileIcon) ? width + fileIcon.getIconWidth() : width;
  }

  /**
   * Returns the icon's height.
   * @return an int specifying the fixed height of the icon.
   */
  @Override public int getIconHeight() {
    return height;
  }

  /**
   * Gets the bounds of this icon in the form of a <code>Rectangle</code>
   * object. The bounds specify this icon's width, height, and location
   * relative to its parent.
   * @return a rectangle indicating this icon's bounds
   */
  public Rectangle getBounds() {
    return new Rectangle(xpos, ypos, width, height);
  }
}
