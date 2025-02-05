// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:

package example;

import com.sun.java.swing.plaf.windows.WindowsTabbedPaneUI;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.EventListener;
import java.util.Objects;
import java.util.stream.Stream;
import javax.swing.*;
import javax.swing.event.EventListenerList;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import javax.swing.text.View;

// Copied from
// JTabbedPane with close Icons | Oracle Forums
// https://community.oracle.com/thread/1356993
/**
 * A JTabbedPane which has a close ('X') icon on each tab.
 *
 * <p>To add a tab, use the method addTab(String, Component)
 *
 * <p>To have an extra icon on each tab (e.g. like in JBuilder, showing the file
 * type) use the method addTab(String, Component, Icon). Only clicking the 'X'
 * closes the tab.
 */
public final class CloseableTabbedPane extends JTabbedPane {
  /**
   * The <code>EventListenerList</code>.
   */
  private EventListenerList eventListenerList;

  /**
   * The viewport of the scrolled tabs.
   */
  // protected JViewport headerViewport;

  private transient CloseableTabIconHandler handler;

  // /**
  //  * The normal closeicon.
  //  */
  // private Icon normalCloseIcon;
  //
  // /**
  //  * The closeicon when the mouse is over.
  //  */
  // private Icon hooverCloseIcon;
  //
  // /**
  //  * The closeicon when the mouse is pressed.
  //  */
  // private Icon pressedCloseIcon;
  //
  // /**
  //  * Creates a new instance of <code>CloseableTabbedPane</code>.
  //  */
  // public CloseableTabbedPane() {
  //   super();
  //   init(SwingConstants.LEFT);
  // }
  //
  // /**
  //  * Creates a new instance of <code>CloseableTabbedPane</code>.
  //  * @param horizTextPosition  the horizontal position of the text (e.g.
  //  * SwingConstants.TRAILING or SwingConstants.LEFT)
  //  */
  // public CloseableTabbedPane(int horizTxtPos) {
  //   super();
  //   init(horizTextPosition);
  // }

  @Override public void updateUI() {
    removeMouseListener(handler);
    removeMouseMotionListener(handler);
    super.updateUI();
    eventListenerList = new EventListenerList();
    handler = new CloseableTabIconHandler();
    addMouseListener(handler);
    addMouseMotionListener(handler);

    if (getUI() instanceof WindowsTabbedPaneUI) {
      setUI(new CloseableWindowsTabbedPaneUI());
    } else {
      setUI(new CloseableTabbedPaneUI());
    }
  }

  // /**
  //  * Allows setting own closeicons.
  //  * @param normal  the normal closeicon
  //  * @param hoover  the closeicon when the mouse is over
  //  * @param pressed  the closeicon when the mouse is pressed
  //  */
  // public void setCloseIcons(Icon normal, Icon hoover, Icon pressed) {
  //   normalCloseIcon = normal;
  //   hooverCloseIcon = hoover;
  //   pressedCloseIcon = pressed;
  // }

  /**
   * Adds a <code>Component</code> represented by a title and no icon.
   *
   * @param title  the title to be displayed in this tab
   * @param component  the component to be displayed when this tab is clicked
   */
  @Override public void addTab(String title, Component component) {
    // addTab(title, component, null);
    super.addTab(title, new CloseTabIcon(null), component);

    // if (Objects.isNull(headerViewport)) {
    //   for (Component c : getComponents()) {
    //     if ("TabbedPane.scrollableViewport".equals(c.getName())) {
    //       headerViewport = (JViewport) c;
    //       break;
    //     }
    //   }
    // }
  }

  // /**
  //  * Adds a <code>Component</code> represented by a title and an icon.
  //  * @param title  the title to be displayed in this tab
  //  * @param component  the component to be displayed when this tab is clicked
  //  * @param extraIcon  the icon to be displayed in this tab
  //  */
  // public void addTab(String title, Component component, Icon extraIcon) {
  //   // boolean doPaintCloseIcon = true;
  //   // if (component instanceof JComponent) {
  //   //   Object prop = ((JComponent) component).getClientProperty("isClosable");
  //   //   if (Objects.nonNull(prop)) {
  //   //     doPaintCloseIcon = ((Boolean) prop).booleanValue();
  //   //   }
  //   // }
  //   //
  //   // super.addTab(title, doPaintCloseIcon ? new CloseTabIcon(extraIcon) : null, component);
  //   super.addTab(title, new CloseTabIcon(extraIcon), component);
  //
  //   if (Objects.isNull(headerViewport)) {
  //     // for (Component c : getComponents()) {
  //     //   if ("TabbedPane.scrollableViewport".equals(c.getName())) {
  //     //     headerViewport = (JViewport) c;
  //     //   }
  //     // }
  //
  //     Component[] list = getComponents();
  //     for (int i = 0; i < list.length; i++) {
  //       Component c = list[i];
  //       if ("TabbedPane.scrollableViewport".equals(c.getName())) {
  //         headerViewport = (JViewport) c;
  //       }
  //     }
  //   }
  // }

  /**
   * Adds an <code>CloseableTabbedPaneListener</code> to the JTabbedPane.
   *
   * @param l  the <code>CloseableTabbedPaneListener</code> to be added
   */
  public void addCloseableTabbedPaneListener(CloseableTabbedPaneListener l) {
    eventListenerList.add(CloseableTabbedPaneListener.class, l);
  }

  /**
   * Removes an <code>CloseableTabbedPaneListener</code> from the JTabbedPane.
   *
   * @param l  the listener to be removed
   */
  public void removeCloseableTabbedPaneListener(CloseableTabbedPaneListener l) {
    eventListenerList.remove(CloseableTabbedPaneListener.class, l);
  }

  /**
   * Returns an array of all the <code>SearchListener</code>s added to this
   * <code>SearchPane</code> with addSearchListener().
   *
   * @return all of the <code>SearchListener</code>s added
   *     or an empty array if no listeners have been added
   */
  public CloseableTabbedPaneListener[] getCloseableTabbedPaneListener() {
    return eventListenerList.getListeners(CloseableTabbedPaneListener.class);
  }

  /**
   * Notifies all listeners that have registered interest for notification on
   * this event type.
   *
   * @param tabIndexToClose  the index of the tab which should be closed
   * @return true if the tab can be closed, false otherwise
   */
  public boolean fireCloseTab(int tabIndexToClose) {
    boolean closeit = true;
    // Guaranteed to return a non-null array
    Object[] listeners = eventListenerList.getListenerList();
    for (Object o : listeners) {
      if (isCloseTabButton(o, tabIndexToClose)) {
        closeit = false;
        break;
      }
    }
    return closeit;
  }

  private static boolean isCloseTabButton(Object o, int i) {
    return o instanceof CloseableTabbedPaneListener
        && !((CloseableTabbedPaneListener) o).closeTab(i);
  }

  public Point getHeaderViewPosition() {
    return Stream.of(getComponents())
        .filter(JViewport.class::isInstance)
        .filter(c -> "TabbedPane.scrollableViewport".equals(c.getName()))
        .findFirst()
        .map(c -> ((JViewport) c).getViewPosition())
        .orElseGet(Point::new);
    // for (Component c : getComponents()) {
    //   if (c instanceof JViewport && "TabbedPane.scrollableViewport".equals(c.getName())) {
    //     return ((JViewport) c).getViewPosition();
    //   }
    // }
    // return new Point();
  }
}

class CloseableTabIconHandler extends MouseAdapter {
  private final Rectangle drawRect = new Rectangle();

  private boolean isCloseTabIconRollover(JTabbedPane tabs, CloseTabIcon icon, MouseEvent e) {
    Rectangle rect = icon.getBounds();
    Point pos = new Point();
    if (tabs instanceof CloseableTabbedPane) {
      pos.setLocation(((CloseableTabbedPane) tabs).getHeaderViewPosition());
    }
    drawRect.setBounds(rect.x - pos.x, rect.y - pos.y, rect.width, rect.height);
    pos.translate(e.getX(), e.getY());
    return rect.contains(pos);
  }

  /**
   * Invoked when the mouse button has been clicked (pressed and released) on
   * a component.
   *
   * @param e  the <code>MouseEvent</code>
   */
  @Override public void mouseClicked(MouseEvent e) {
    CloseableTabbedPane tabbedPane = null;
    CloseTabIcon icon = null;
    Component c = e.getComponent();
    if (c instanceof CloseableTabbedPane) {
      tabbedPane = (CloseableTabbedPane) c;
      icon = getCloseTabIcon((CloseableTabbedPane) c, e.getPoint());
    }
    if (Objects.isNull(tabbedPane) || Objects.isNull(icon)) {
      return;
    }
    if (isCloseTabIconRollover(tabbedPane, icon, e)) {
      int selIndex = tabbedPane.getSelectedIndex();
      if (tabbedPane.fireCloseTab(selIndex)) {
        if (selIndex > 0) {
          // to prevent uncatchable null-pointers
          Rectangle rec = tabbedPane.getBoundsAt(selIndex - 1);
          MouseEvent event = new MouseEvent(
              e.getComponent(), e.getID() + 1,
              System.currentTimeMillis(), e.getModifiersEx(),
              rec.x, rec.y,
              e.getClickCount(), e.isPopupTrigger(), e.getButton());
          tabbedPane.dispatchEvent(event);
        }
        // the tab is being closed
        // removeTabAt(tabNumber);
        tabbedPane.remove(selIndex);
      } else {
        icon.mouseover = false;
        icon.mousepressed = false;
      }
    } else {
      icon.mouseover = false;
    }
    tabbedPane.repaint(drawRect);
  }

  /**
   * Invoked when the mouse exits a component.
   *
   * @param e  the <code>MouseEvent</code>
   */
  @Override public void mouseExited(MouseEvent e) {
    Component c = e.getComponent();
    if (!(c instanceof CloseableTabbedPane)) {
      return;
    }
    CloseableTabbedPane tabbedPane = (CloseableTabbedPane) c;
    for (int i = 0; i < tabbedPane.getTabCount(); i++) {
      CloseTabIcon icon = (CloseTabIcon) tabbedPane.getIconAt(i);
      if (Objects.nonNull(icon)) {
        icon.mouseover = false;
      }
    }
    tabbedPane.repaint();
  }

  /**
   * Invoked when a mouse button has been pressed on a component.
   *
   * @param e  the <code>MouseEvent</code>
   */
  @Override public void mousePressed(MouseEvent e) {
    Component c = e.getComponent();
    if (!(c instanceof CloseableTabbedPane)) {
      return;
    }
    CloseableTabbedPane tabbedPane = (CloseableTabbedPane) c;
    CloseTabIcon icon = getCloseTabIcon(tabbedPane, e.getPoint());
    if (Objects.nonNull(icon)) {
      Rectangle rect = icon.getBounds();
      Point pos = tabbedPane.getHeaderViewPosition();
      drawRect.setBounds(rect.x - pos.x, rect.y - pos.y, rect.width, rect.height);
      icon.mousepressed = (e.getModifiersEx() & InputEvent.BUTTON1_DOWN_MASK) != 0;
      tabbedPane.repaint(drawRect);
    }
  }

  /**
   * Invoked when a mouse button is pressed on a component and then dragged.
   * <code>MOUSE_DRAGGED</code> events will continue to be delivered to the
   * component where the drag originated until the mouse button is released
   * (regardless of whether the mouse position is within the bounds of the
   * component).<br/>
   * <br/>
   * Due to platform-dependent Drag&Drop implementations,
   * <code>MOUSE_DRAGGED</code> events may not be delivered during a native
   * Drag&amp;Drop operation.
   *
   * @param e  the <code>MouseEvent</code>
   */
  @Override public void mouseDragged(MouseEvent e) {
    Component c = e.getComponent();
    if (!(c instanceof CloseableTabbedPane)) {
      return;
    }
    CloseableTabbedPane tabbedPane = (CloseableTabbedPane) c;
    CloseTabIcon icon = getCloseTabIcon(tabbedPane, e.getPoint());
    if (Objects.nonNull(icon)) {
      if (isCloseTabIconRollover(tabbedPane, icon, e)) {
        icon.mouseover = true;
        icon.mousepressed = (e.getModifiersEx() & InputEvent.BUTTON1_DOWN_MASK) != 0;
      } else {
        icon.mouseover = false;
      }
      tabbedPane.repaint(drawRect);
    }
  }

  /**
   * Invoked when the mouse cursor has been moved onto a component but no
   * buttons have been pushed.
   *
   * @param e  the <code>MouseEvent</code>
   */
  @Override public void mouseMoved(MouseEvent e) {
    Component c = e.getComponent();
    if (!(c instanceof CloseableTabbedPane)) {
      return;
    }
    CloseableTabbedPane tabbedPane = (CloseableTabbedPane) c;
    CloseTabIcon icon = getCloseTabIcon(tabbedPane, e.getPoint());
    if (Objects.nonNull(icon)) {
      if (isCloseTabIconRollover(tabbedPane, icon, e)) {
        icon.mouseover = true;
        icon.mousepressed = (e.getModifiersEx() & InputEvent.BUTTON1_DOWN_MASK) != 0;
      } else {
        icon.mouseover = false;
      }
      tabbedPane.repaint(drawRect);
    }
  }

  private CloseTabIcon getCloseTabIcon(CloseableTabbedPane tabbedPane, Point pt) {
    // int i = getUI().tabForCoordinate(this, e.getX(), e.getY());
    int i = tabbedPane.indexAtLocation(pt.x, pt.y);
    return i >= 0 ? (CloseTabIcon) tabbedPane.getIconAt(i) : null;
  }
}

/**
 * The listener that's notified when an tab should be closed in the
 * <code>CloseableTabbedPane</code>.
 */
interface CloseableTabbedPaneListener extends EventListener {
  /**
   * Informs all <code>CloseableTabbedPaneListener</code>s when a tab should be closed.
   *
   * @param tabIndexToClose  the index of the tab which should be closed
   * @return true if the tab can be closed, false otherwise
   */
  boolean closeTab(int tabIndexToClose);
}

/**
 * A specific <code>WindowsTabbedPaneUI</code>.
 */
class CloseableWindowsTabbedPaneUI extends WindowsTabbedPaneUI {
  private static final String HTML = "html";

  /**
   * the horizontal position of the text.
   */
  private int horizTextPosition = LEFT;

  /**
   * Creates a new instance of <code>CloseableTabbedPaneUI</code>.
   */
  protected CloseableWindowsTabbedPaneUI() {
    super();
  }

  /**
   * Creates a new instance of <code>CloseableTabbedPaneUI</code>.
   *
   * @param horizTextPosition  the horizontal position of the text
   *     (e.g. SwingConstants.TRAILING or SwingConstants.LEFT)
   */
  protected CloseableWindowsTabbedPaneUI(int horizTextPosition) {
    super();
    this.horizTextPosition = horizTextPosition;
  }

  /**
   * Layouts the label.
   *
   * @param tabPlacement  the placement of the tabs
   * @param metrics  the font metrics
   * @param tabIndex  the index of the tab
   * @param title  the title of the tab
   * @param icon  the icon of the tab
   * @param tabRect  the tab boundaries
   * @param iconRect  the icon boundaries
   * @param textRect  the text boundaries
   * @param isSelected  true whether the tab is selected, false otherwise
   */
  @Override protected void layoutLabel(int tabPlacement, FontMetrics metrics, int tabIndex, String title, Icon icon, Rectangle tabRect, Rectangle iconRect, Rectangle textRect, boolean isSelected) {
    textRect.setLocation(0, 0);
    iconRect.setLocation(0, 0);

    View v = getTextViewForTab(tabIndex);
    if (Objects.nonNull(v)) {
      tabPane.putClientProperty(HTML, v);
    }

    SwingUtilities.layoutCompoundLabel(
        tabPane,
        metrics, title, icon,
        CENTER,
        CENTER,
        CENTER,
        horizTextPosition, // TRAILING,
        tabRect,
        iconRect,
        textRect,
        textIconGap + 2);

    tabPane.putClientProperty(HTML, null);

    int xnudge = getTabLabelShiftX(tabPlacement, tabIndex, isSelected);
    int ynudge = getTabLabelShiftY(tabPlacement, tabIndex, isSelected);
    iconRect.x += xnudge;
    iconRect.y += ynudge;
    textRect.x += xnudge;
    textRect.y += ynudge;
  }

  public void setHorizTextPosition(int horizTxtPos) {
    horizTextPosition = horizTxtPos;
  }
}

/**
 * A specific <code>BasicTabbedPaneUI</code>.
 */
class CloseableTabbedPaneUI extends BasicTabbedPaneUI {
  private static final String HTML = "html";

  /**
   * the horizontal position of the text.
   */
  private int horizTextPosition = LEFT;

  /**
   * Creates a new instance of <code>CloseableTabbedPaneUI</code>.
   */
  protected CloseableTabbedPaneUI() {
    super();
  }

  /**
   * Creates a new instance of <code>CloseableTabbedPaneUI</code>.
   *
   * @param horizTextPosition  the horizontal position of the text
   *     (e.g. SwingConstants.TRAILING or SwingConstants.LEFT)
   */
  protected CloseableTabbedPaneUI(int horizTextPosition) {
    super();
    this.horizTextPosition = horizTextPosition;
  }

  /**
   * Layouts the label.
   *
   * @param tabPlacement  the placement of the tabs
   * @param metrics  the font metrics
   * @param tabIndex  the index of the tab
   * @param title  the title of the tab
   * @param icon  the icon of the tab
   * @param tabRect  the tab boundaries
   * @param iconRect  the icon boundaries
   * @param textRect  the text boundaries
   * @param isSelected  true whether the tab is selected, false otherwise
   */
  @Override protected void layoutLabel(int tabPlacement, FontMetrics metrics, int tabIndex, String title, Icon icon, Rectangle tabRect, Rectangle iconRect, Rectangle textRect, boolean isSelected) {
    textRect.setLocation(0, 0);
    iconRect.setLocation(0, 0);

    View v = getTextViewForTab(tabIndex);
    if (Objects.nonNull(v)) {
      tabPane.putClientProperty(HTML, v);
    }

    SwingUtilities.layoutCompoundLabel(
        tabPane,
        metrics, title, icon,
        CENTER,
        CENTER,
        CENTER,
        horizTextPosition, // TRAILING,
        tabRect,
        iconRect,
        textRect,
        textIconGap + 2);

    tabPane.putClientProperty(HTML, null);

    int xnudge = getTabLabelShiftX(tabPlacement, tabIndex, isSelected);
    int ynudge = getTabLabelShiftY(tabPlacement, tabIndex, isSelected);
    iconRect.x += xnudge;
    iconRect.y += ynudge;
    textRect.x += xnudge;
    textRect.y += ynudge;
  }

  public void setHorizTextPosition(int horizTxtPos) {
    horizTextPosition = horizTxtPos;
  }
}
