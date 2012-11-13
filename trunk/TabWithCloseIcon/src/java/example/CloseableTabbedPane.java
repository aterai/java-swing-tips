package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import javax.swing.plaf.metal.MetalTabbedPaneUI;
import com.sun.java.swing.plaf.windows.WindowsTabbedPaneUI;

/**
 * A JTabbedPane which has a close ('X') icon on each tab.
 *
 * To add a tab, use the method addTab(String, Component)
 *
 * To have an extra icon on each tab (e.g. like in JBuilder, showing the file
 * type) use the method addTab(String, Component, Icon). Only clicking the 'X'
 * closes the tab.
 */
public class CloseableTabbedPane extends JTabbedPane implements MouseListener, MouseMotionListener {
    /**
     * The <code>EventListenerList</code>.
     */
    private EventListenerList eventListenerList = null;

    /**
     * The viewport of the scrolled tabs.
     */
    private JViewport headerViewport = null;

    /**
     * The normal closeicon.
     */
    private Icon normalCloseIcon = null;

    /**
     * The closeicon when the mouse is over.
     */
    private Icon hooverCloseIcon = null;

    /**
     * The closeicon when the mouse is pressed.
     */
    private Icon pressedCloseIcon = null;

    /**
     * Creates a new instance of <code>CloseableTabbedPane</code>
     */
    public CloseableTabbedPane() {
        super();
        init(SwingUtilities.LEFT);
    }

    /**
     * Creates a new instance of <code>CloseableTabbedPane</code>
     * @param horizontalTextPosition the horizontal position of the text (e.g.
     * SwingUtilities.TRAILING or SwingUtilities.LEFT)
     */
    public CloseableTabbedPane(int horizontalTextPosition) {
        super();
        init(horizontalTextPosition);
    }

    /**
     * Initializes the <code>CloseableTabbedPane</code>
     * @param horizontalTextPosition the horizontal position of the text (e.g.
     * SwingUtilities.TRAILING or SwingUtilities.LEFT)
     */
    private void init(int horizontalTextPosition) {
        eventListenerList = new EventListenerList();
        addMouseListener(this);
        addMouseMotionListener(this);

        if(getUI() instanceof MetalTabbedPaneUI) {
            setUI(new CloseableMetalTabbedPaneUI(horizontalTextPosition));
        }else if(getUI() instanceof WindowsTabbedPaneUI) {
            setUI(new CloseableWindowsTabbedPaneUI(horizontalTextPosition));
        }else{
            setUI(new CloseableTabbedPaneUI(horizontalTextPosition));
        }
    }

    /**
     * Allows setting own closeicons.
     * @param normal the normal closeicon
     * @param hoover the closeicon when the mouse is over
     * @param pressed the closeicon when the mouse is pressed
     */
    public void setCloseIcons(Icon normal, Icon hoover, Icon pressed) {
        normalCloseIcon = normal;
        hooverCloseIcon = hoover;
        pressedCloseIcon = pressed;
    }

    /**
     * Adds a <code>Component</code> represented by a title and no icon.
     * @param title the title to be displayed in this tab
     * @param component the component to be displayed when this tab is clicked
     */
    public void addTab(String title, Component component) {
        addTab(title, component, null);
    }

    /**
     * Adds a <code>Component</code> represented by a title and an icon.
     * @param title the title to be displayed in this tab
     * @param component the component to be displayed when this tab is clicked
     * @param extraIcon the icon to be displayed in this tab
     */
    public void addTab(String title, Component component, Icon extraIcon) {
        boolean doPaintCloseIcon = true;
        try{
            Object prop = null;
            if((prop = ((JComponent) component).getClientProperty("isClosable")) != null) {
                doPaintCloseIcon = ((Boolean) prop).booleanValue();
            }
        }catch(Exception ignored) { ignored.printStackTrace(); /*Could probably be a ClassCastException*/}

        super.addTab(title, doPaintCloseIcon ? new CloseTabIcon(extraIcon) : null, component);

        if(headerViewport == null) {
            //             for(Component c : getComponents()) {
            //                 if("TabbedPane.scrollableViewport".equals(c.getName()))
            //                   headerViewport = (JViewport) c;
            //             }

            Component[] list = getComponents();
            for(int i=0;i<list.length;i++) {
                Component c = list[i];
                if("TabbedPane.scrollableViewport".equals(c.getName()))
                  headerViewport = (JViewport) c;
            }
        }
    }

    /**
     * Invoked when the mouse button has been clicked (pressed and released) on
     * a component.
     * @param e the <code>MouseEvent</code>
     */
    @Override public void mouseClicked(MouseEvent e) {
        processMouseEvents(e);
    }

    /**
     * Invoked when the mouse enters a component.
     * @param e the <code>MouseEvent</code>
     */
    @Override public void mouseEntered(MouseEvent e) { }

    /**
     * Invoked when the mouse exits a component.
     * @param e the <code>MouseEvent</code>
     */
    @Override public void mouseExited(MouseEvent e) {
        for(int i=0; i<getTabCount(); i++) {
            CloseTabIcon icon = (CloseTabIcon) getIconAt(i);
            if(icon != null)
              icon.mouseover = false;
        }
        repaint();
    }

    /**
     * Invoked when a mouse button has been pressed on a component.
     * @param e the <code>MouseEvent</code>
     */
    @Override public void mousePressed(MouseEvent e) {
        processMouseEvents(e);
    }

    /**
     * Invoked when a mouse button has been released on a component.
     * @param e the <code>MouseEvent</code>
     */
    @Override public void mouseReleased(MouseEvent e) { }

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
     * @param e the <code>MouseEvent</code>
     */
    @Override public void mouseDragged(MouseEvent e) {
        processMouseEvents(e);
    }

    /**
     * Invoked when the mouse cursor has been moved onto a component but no
     * buttons have been pushed.
     * @param e the <code>MouseEvent</code>
     */
    @Override public void mouseMoved(MouseEvent e) {
        processMouseEvents(e);
    }

    /**
     * Processes all caught <code>MouseEvent</code>s.
     * @param e the <code>MouseEvent</code>
     */
    private void processMouseEvents(MouseEvent e) {
        int tabNumber = getUI().tabForCoordinate(this, e.getX(), e.getY());
        if(tabNumber < 0) return;
        CloseTabIcon icon = (CloseTabIcon) getIconAt(tabNumber);
        if(icon != null) {
            Rectangle rect= icon.getBounds();
            Point pos = headerViewport == null ?
              new Point() : headerViewport.getViewPosition();
            Rectangle drawRect = new Rectangle(
                rect.x - pos.x, rect.y - pos.y, rect.width, rect.height);

            if(e.getID() == e.MOUSE_PRESSED) {
                icon.mousepressed = e.getModifiers() == e.BUTTON1_MASK;
                repaint(drawRect);
            }else if(e.getID() == e.MOUSE_MOVED || e.getID() == e.MOUSE_DRAGGED || e.getID() == e.MOUSE_CLICKED) {
                pos.x += e.getX();
                pos.y += e.getY();
                if(rect.contains(pos)) {
                    if(e.getID() == e.MOUSE_CLICKED) {
                        int selIndex = getSelectedIndex();
                        if(fireCloseTab(selIndex)) {
                            if(selIndex > 0) {
                                // to prevent uncatchable null-pointers
                                Rectangle rec = getUI().getTabBounds(this, selIndex - 1);
                                MouseEvent event = new MouseEvent((Component) e.getSource(),
                                                                  e.getID() + 1,
                                                                  System.currentTimeMillis(),
                                                                  e.getModifiers(),
                                                                  rec.x,
                                                                  rec.y,
                                                                  e.getClickCount(),
                                                                  e.isPopupTrigger(),
                                                                  e.getButton());
                                dispatchEvent(event);
                            }
                            //the tab is being closed
                            //removeTabAt(tabNumber);
                            remove(selIndex);
                        }else{
                            icon.mouseover = false;
                            icon.mousepressed = false;
                            repaint(drawRect);
                        }
                    }else{
                        icon.mouseover = true;
                        icon.mousepressed = e.getModifiers() == e.BUTTON1_MASK;
                    }
                }else{
                    icon.mouseover = false;
                }
                repaint(drawRect);
            }
        }
    }

    /**
     * Adds an <code>CloseableTabbedPaneListener</code> to the tabbedpane.
     * @param l the <code>CloseableTabbedPaneListener</code> to be added
     */
    public void addCloseableTabbedPaneListener(CloseableTabbedPaneListener l) {
        eventListenerList.add(CloseableTabbedPaneListener.class, l);
    }

    /**
     * Removes an <code>CloseableTabbedPaneListener</code> from the tabbedpane.
     * @param l the listener to be removed
     */
    public void removeCloseableTabbedPaneListener(CloseableTabbedPaneListener l) {
        eventListenerList.remove(CloseableTabbedPaneListener.class, l);
    }

    /**
     * Returns an array of all the <code>SearchListener</code>s added to this
     * <code>SearchPane</code> with addSearchListener().
     * @return all of the <code>SearchListener</code>s added or an empty array if
     * no listeners have been added
     */
    public CloseableTabbedPaneListener[] getCloseableTabbedPaneListener() {
        return (CloseableTabbedPaneListener[])eventListenerList.getListeners(CloseableTabbedPaneListener.class);
    }

    /**
     * Notifies all listeners that have registered interest for notification on
     * this event type.
     * @param tabIndexToClose the index of the tab which should be closed
     * @return true if the tab can be closed, false otherwise
     */
    protected boolean fireCloseTab(int tabIndexToClose) {
        boolean closeit = true;
        // Guaranteed to return a non-null array
        Object[] listeners = eventListenerList.getListenerList();
        //for(Object i : listeners) {
        for(int j=0;j<listeners.length;j++) {
            Object i = listeners[j];
            if(i instanceof CloseableTabbedPaneListener &&
               !((CloseableTabbedPaneListener) i).closeTab(tabIndexToClose)) {
                closeit = false;
                break;
            }
        }
        return closeit;
    }

    /**
     * The class which generates the 'X' icon for the tabs. The constructor
     * accepts an icon which is extra to the 'X' icon, so you can have tabs
     * like in JBuilder. This value is null if no extra icon is required.
     */
    class CloseTabIcon implements Icon {
        /**
         * the x position of the icon
         */
        private int x_pos;

        /**
         * the y position of the icon
         */
        private int y_pos;

        /**
         * the width the icon
         */
        private int width;

        /**
         * the height the icon
         */
        private int height;

        /**
         * the additional fileicon
         */
        private Icon fileIcon;

        /**
         * true whether the mouse is over this icon, false otherwise
         */
        private boolean mouseover = false;

        /**
         * true whether the mouse is pressed on this icon, false otherwise
         */
        private boolean mousepressed = false;

        /**
         * Creates a new instance of <code>CloseTabIcon</code>
         * @param fileIcon the additional fileicon, if there is one set
         */
        public CloseTabIcon(Icon fileIcon) {
            this.fileIcon = fileIcon;
            width  = 16;
            height = 16;
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
            boolean doPaintCloseIcon = true;
            try{
                // JComponent.putClientProperty("isClosable", new Boolean(false));
                JTabbedPane tabbedpane = (JTabbedPane) c;
                int tabNumber = tabbedpane.getUI().tabForCoordinate(tabbedpane, x, y);
                JComponent curPanel = (JComponent) tabbedpane.getComponentAt(tabNumber);
                Object prop = null;
                if((prop = curPanel.getClientProperty("isClosable")) != null) {
                    doPaintCloseIcon = ((Boolean) prop).booleanValue();
                }
            }catch(Exception ignored) { ignored.printStackTrace(); /*Could probably be a ClassCastException*/}
            if(doPaintCloseIcon) {
                x_pos = x;
                y_pos = y;
                int y_p = y + 1;

                if(normalCloseIcon != null && !mouseover) {
                    normalCloseIcon.paintIcon(c, g, x, y_p);
                }else if(hooverCloseIcon != null && mouseover && !mousepressed) {
                    hooverCloseIcon.paintIcon(c, g, x, y_p);
                }else if(pressedCloseIcon != null && mousepressed) {
                    pressedCloseIcon.paintIcon(c, g, x, y_p);
                }else{
                    y_p++;

                    Color col = g.getColor();

                    if(mousepressed && mouseover) {
                        g.setColor(Color.WHITE);
                        g.fillRect(x+1, y_p, 12, 13);
                    }

                    g.setColor(Color.BLACK);
                    g.drawLine(x+1, y_p, x+12, y_p);
                    g.drawLine(x+1, y_p+13, x+12, y_p+13);
                    g.drawLine(x, y_p+1, x, y_p+12);
                    g.drawLine(x+13, y_p+1, x+13, y_p+12);
                    g.drawLine(x+3, y_p+3, x+10, y_p+10);
                    if(mouseover)
                      g.setColor(Color.GRAY);
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
            }
        }

        /**
         * Returns the icon's width.
         * @return an int specifying the fixed width of the icon.
         */
        @Override public int getIconWidth() {
            return fileIcon != null ? width + fileIcon.getIconWidth() : width;
        }

        /**
         * Returns the icon's height.
         * @return an int specifying the fixed height of the icon.
         */
        @Override public int getIconHeight() {
            return height;
        }

        /**
         * Gets the bounds of this icon in the form of a <code>Rectangle<code>
         * object. The bounds specify this icon's width, height, and location
         * relative to its parent.
         * @return a rectangle indicating this icon's bounds
         */
        public Rectangle getBounds() {
            return new Rectangle(x_pos, y_pos, width, height);
        }
    }

    static class CloseableWindowsTabbedPaneUI extends WindowsTabbedPaneUI {
        /**
         * the horizontal position of the text
         */
        private int horizontalTextPosition = SwingUtilities.LEFT;

        /**
         * Creates a new instance of <code>CloseableTabbedPaneUI</code>
         */
        public CloseableWindowsTabbedPaneUI() {
        }

        /**
         * Creates a new instance of <code>CloseableTabbedPaneUI</code>
         * @param horizontalTextPosition the horizontal position of the text (e.g.
         * SwingUtilities.TRAILING or SwingUtilities.LEFT)
         */
        public CloseableWindowsTabbedPaneUI(int horizontalTextPosition) {
            this.horizontalTextPosition = horizontalTextPosition;
        }

        /**
         * Layouts the label
         * @param tabPlacement the placement of the tabs
         * @param metrics the font metrics
         * @param tabIndex the index of the tab
         * @param title the title of the tab
         * @param icon the icon of the tab
         * @param tabRect the tab boundaries
         * @param iconRect the icon boundaries
         * @param textRect the text boundaries
         * @param isSelected true whether the tab is selected, false otherwise
         */
        protected void layoutLabel(int tabPlacement, FontMetrics metrics,
                                   int tabIndex, String title, Icon icon,
                                   Rectangle tabRect, Rectangle iconRect,
                                   Rectangle textRect, boolean isSelected) {

            textRect.x = textRect.y = iconRect.x = iconRect.y = 0;

            javax.swing.text.View v = getTextViewForTab(tabIndex);
            if(v != null) {
                putHtmlClientProperty(tabPane, v);
            }

            SwingUtilities.layoutCompoundLabel((JComponent) tabPane,
                                               metrics, title, icon,
                                               SwingUtilities.CENTER,
                                               SwingUtilities.CENTER,
                                               SwingUtilities.CENTER,
                                               //SwingUtilities.TRAILING,
                                               horizontalTextPosition,
                                               tabRect,
                                               iconRect,
                                               textRect,
                                               textIconGap + 2);

            putHtmlClientProperty(tabPane, null);

            int xNudge = getTabLabelShiftX(tabPlacement, tabIndex, isSelected);
            int yNudge = getTabLabelShiftY(tabPlacement, tabIndex, isSelected);
            iconRect.x += xNudge;
            iconRect.y += yNudge;
            textRect.x += xNudge;
            textRect.y += yNudge;
        }
    }
    /**
     * A specific <code>BasicTabbedPaneUI</code>.
     */
    static class CloseableTabbedPaneUI extends BasicTabbedPaneUI {

        /**
         * the horizontal position of the text
         */
        private int horizontalTextPosition = SwingUtilities.LEFT;

        /**
         * Creates a new instance of <code>CloseableTabbedPaneUI</code>
         */
        public CloseableTabbedPaneUI() {
        }

        /**
         * Creates a new instance of <code>CloseableTabbedPaneUI</code>
         * @param horizontalTextPosition the horizontal position of the text (e.g.
         * SwingUtilities.TRAILING or SwingUtilities.LEFT)
         */
        public CloseableTabbedPaneUI(int horizontalTextPosition) {
            this.horizontalTextPosition = horizontalTextPosition;
        }

        /**
         * Layouts the label
         * @param tabPlacement the placement of the tabs
         * @param metrics the font metrics
         * @param tabIndex the index of the tab
         * @param title the title of the tab
         * @param icon the icon of the tab
         * @param tabRect the tab boundaries
         * @param iconRect the icon boundaries
         * @param textRect the text boundaries
         * @param isSelected true whether the tab is selected, false otherwise
         */
        protected void layoutLabel(int tabPlacement, FontMetrics metrics,
                                   int tabIndex, String title, Icon icon,
                                   Rectangle tabRect, Rectangle iconRect,
                                   Rectangle textRect, boolean isSelected) {

            textRect.x = textRect.y = iconRect.x = iconRect.y = 0;

            javax.swing.text.View v = getTextViewForTab(tabIndex);
            if(v != null) {
                putHtmlClientProperty(tabPane, v);
            }

            SwingUtilities.layoutCompoundLabel((JComponent) tabPane,
                                               metrics, title, icon,
                                               SwingUtilities.CENTER,
                                               SwingUtilities.CENTER,
                                               SwingUtilities.CENTER,
                                               //SwingUtilities.TRAILING,
                                               horizontalTextPosition,
                                               tabRect,
                                               iconRect,
                                               textRect,
                                               textIconGap + 2);

            putHtmlClientProperty(tabPane, null);

            int xNudge = getTabLabelShiftX(tabPlacement, tabIndex, isSelected);
            int yNudge = getTabLabelShiftY(tabPlacement, tabIndex, isSelected);
            iconRect.x += xNudge;
            iconRect.y += yNudge;
            textRect.x += xNudge;
            textRect.y += yNudge;
        }
    }

    /**
     * A specific <code>MetalTabbedPaneUI</code>.
     */
    static class CloseableMetalTabbedPaneUI extends MetalTabbedPaneUI {

        /**
         * the horizontal position of the text
         */
        private int horizontalTextPosition = SwingUtilities.LEFT;

        /**
         * Creates a new instance of <code>CloseableMetalTabbedPaneUI</code>
         */
        public CloseableMetalTabbedPaneUI() {
        }

        /**
         * Creates a new instance of <code>CloseableMetalTabbedPaneUI</code>
         * @param horizontalTextPosition the horizontal position of the text (e.g.
         * SwingUtilities.TRAILING or SwingUtilities.LEFT)
         */
        public CloseableMetalTabbedPaneUI(int horizontalTextPosition) {
            this.horizontalTextPosition = horizontalTextPosition;
        }

        /**
         * Layouts the label
         * @param tabPlacement the placement of the tabs
         * @param metrics the font metrics
         * @param tabIndex the index of the tab
         * @param title the title of the tab
         * @param icon the icon of the tab
         * @param tabRect the tab boundaries
         * @param iconRect the icon boundaries
         * @param textRect the text boundaries
         * @param isSelected true whether the tab is selected, false otherwise
         */
        protected void layoutLabel(int tabPlacement, FontMetrics metrics,
                                   int tabIndex, String title, Icon icon,
                                   Rectangle tabRect, Rectangle iconRect,
                                   Rectangle textRect, boolean isSelected) {

            textRect.x = textRect.y = iconRect.x = iconRect.y = 0;

            javax.swing.text.View v = getTextViewForTab(tabIndex);
            if(v != null) {
                putHtmlClientProperty(tabPane, v);
            }

            SwingUtilities.layoutCompoundLabel((JComponent) tabPane,
                                               metrics, title, icon,
                                               SwingUtilities.CENTER,
                                               SwingUtilities.CENTER,
                                               SwingUtilities.CENTER,
                                               //SwingUtilities.TRAILING,
                                               horizontalTextPosition,
                                               tabRect,
                                               iconRect,
                                               textRect,
                                               textIconGap + 2);

            putHtmlClientProperty(tabPane, null);

            int xNudge = getTabLabelShiftX(tabPlacement, tabIndex, isSelected);
            int yNudge = getTabLabelShiftY(tabPlacement, tabIndex, isSelected);
            iconRect.x += xNudge;
            iconRect.y += yNudge;
            textRect.x += xNudge;
            textRect.y += yNudge;
        }
    }
    /**
     * The listener that's notified when an tab should be closed in the
     * <code>CloseableTabbedPane</code>.
     */

    private static void putHtmlClientProperty(JTabbedPane t, javax.swing.text.View v) {
        t.putClientProperty("html", v);
    }
}

interface CloseableTabbedPaneListener extends EventListener {
    /**
     * Informs all <code>CloseableTabbedPaneListener</code>s when a tab should be
     * closed
     * @param tabIndexToClose the index of the tab which should be closed
     * @return true if the tab can be closed, false otherwise
     */
    boolean closeTab(int tabIndexToClose);
}
