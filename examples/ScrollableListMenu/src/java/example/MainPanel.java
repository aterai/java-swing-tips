// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.stream.Stream;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    Font[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
    DefaultListModel<String> model = new DefaultListModel<>();
    Stream.of(fonts).map(Font::getFontName).sorted().forEach(model::addElement);
    JList<String> list = new PopupList<>(model);
    // The unit increment of the vertical scroll bar is taken from
    // JList#getScrollableUnitIncrement(...), i.e. the row height
    JScrollPane scroll = new JScrollPane(list);
    scroll.setBorder(BorderFactory.createEmptyBorder());
    scroll.setViewportBorder(BorderFactory.createEmptyBorder());

    JMenu subMenu = new JMenu("Font list");
    subMenu.add(scroll);
    JMenu menu = new JMenu("Menu");
    menu.add(subMenu);
    menu.addSeparator();
    menu.add("Item 1");
    menu.add("Item 2");
    JMenuBar menuBar = new JMenuBar();
    menuBar.add(menu);
    menuBar.add(LookAndFeelUtils.createLookAndFeelMenu());
    EventQueue.invokeLater(() -> getRootPane().setJMenuBar(menuBar));
    JTextArea textArea = new JTextArea("The quick brown fox jumps over the lazy dog.");
    textArea.setLineWrap(true);
    list.addListSelectionListener(e -> {
      String name = list.getSelectedValue();
      if (!e.getValueIsAdjusting() && Objects.nonNull(name)) {
        Font font = textArea.getFont();
        textArea.setFont(new Font(name, font.getStyle(), font.getSize()));
      }
    });
    add(new JScrollPane(textArea));
    setPreferredSize(new Dimension(320, 240));
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (UnsupportedLookAndFeelException ignored) {
      Toolkit.getDefaultToolkit().beep();
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
      Logger.getGlobal().severe(ex::getMessage);
      return;
    }
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

class PopupList<E> extends JList<E> {
  private transient PopupListMouseListener listener;
  private transient RadioButtonMenuItemCellRenderer<E> renderer;
  private boolean typeAhead;

  protected PopupList(ListModel<E> model) {
    super(model);
  }

  @Override public void updateUI() {
    removeMouseListener(listener);
    removeMouseMotionListener(listener);
    super.updateUI();
    setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    listener = new PopupListMouseListener();
    addMouseListener(listener);
    addMouseMotionListener(listener);
    // Recreate the renderer so that its JRadioButtonMenuItem uses the new LookAndFeel
    renderer = new RadioButtonMenuItemCellRenderer<>(listener);
    setCellRenderer(renderer);
  }

  public boolean isDoNotCloseOnMouseClick() {
    return renderer.isDoNotCloseOnMouseClick();
  }

  @Override protected void processKeyEvent(KeyEvent e) {
    // BasicListUI selects the item found by the type-ahead search
    // and calls ensureIndexIsVisible(...) in KeyListener#keyTyped(...)
    typeAhead = e.getID() == KeyEvent.KEY_TYPED;
    if (typeAhead) {
      // Clear the rollover so that the focused (lead) item is highlighted
      listener.clearRolloverIndex(this);
    }
    try {
      super.processKeyEvent(e);
    } finally {
      typeAhead = false;
    }
  }

  @Override public void ensureIndexIsVisible(int index) {
    Rectangle cell = getCellBounds(index, index);
    if (typeAhead && cell != null) {
      // Scroll so that the item found by the type-ahead search is centered,
      // JViewport#scrollRectToVisible(...) clamps the position to the view bounds
      Rectangle r = getVisibleRect();
      r.y = cell.y - (r.height - cell.height) / 2;
      scrollRectToVisible(r);
    } else {
      super.ensureIndexIsVisible(index);
    }
  }
}

class RadioButtonMenuItemCellRenderer<E> implements ListCellRenderer<E> {
  public static final String DO_NOT_CLOSE = "RadioButtonMenuItem.doNotCloseOnMouseClick";
  private final JRadioButtonMenuItem item = new JRadioButtonMenuItem();
  private final PopupListMouseListener listener;

  protected RadioButtonMenuItemCellRenderer(PopupListMouseListener listener) {
    this.listener = listener;
    // Java 9+: same client property as JRadioButtonMenuItem (JDK-8165234),
    // but a click on the JList cell does not go through BasicMenuItemUI,
    // so PopupListMouseListener refers to this value by itself
    item.putClientProperty(DO_NOT_CLOSE, true);
  }

  public boolean isDoNotCloseOnMouseClick() {
    // Same lookup order as BasicMenuItemUI: client property, then UIManager
    Object value = item.getClientProperty(DO_NOT_CLOSE);
    return value instanceof Boolean ? (Boolean) value : UIManager.getBoolean(DO_NOT_CLOSE);
  }

  @Override public Component getListCellRendererComponent(
      JList<? extends E> list, E value, int index, boolean isSelected, boolean cellHasFocus) {
    item.setText(Objects.toString(value, ""));
    // The selected list item is shown as a checked radio button
    item.setSelected(isSelected);
    // The rollover list item, or the focused (lead) list item if there is no rollover,
    // is painted with the menu item highlight of the LookAndFeel
    boolean focused = cellHasFocus && !listener.hasRollover();
    item.setArmed(listener.isRolloverIndex(index) || focused);
    return item;
  }
}

class PopupListMouseListener extends MouseAdapter {
  private int rolloverIndex = -1;

  public boolean isRolloverIndex(int i) {
    return rolloverIndex == i;
  }

  public boolean hasRollover() {
    return rolloverIndex >= 0;
  }

  public void clearRolloverIndex(JList<?> list) {
    setRolloverIndex(list, -1);
  }

  private void updateRolloverIndex(MouseEvent e) {
    Component c = e.getComponent();
    if (c instanceof JList) {
      JList<?> list = (JList<?>) c;
      Point pt = e.getPoint();
      int i = list.locationToIndex(pt);
      // locationToIndex(...) returns the nearest cell index,
      // so ignore points outside the cell bounds (e.g. below the last item)
      Rectangle r = list.getCellBounds(i, i);
      setRolloverIndex(list, r != null && r.contains(pt) ? i : -1);
    }
  }

  private void setRolloverIndex(JList<?> list, int i) {
    int prev = rolloverIndex;
    if (prev != i) {
      rolloverIndex = i;
      // Repaint only the previous and new rollover cells
      repaintCell(list, prev);
      repaintCell(list, i);
      if (prev < 0 || i < 0) {
        // The focused (lead) cell is highlighted only when there is no rollover
        repaintCell(list, list.getLeadSelectionIndex());
      }
    }
  }

  private static void repaintCell(JList<?> list, int i) {
    Rectangle r = i >= 0 ? list.getCellBounds(i, i) : null;
    if (r != null) {
      list.repaint(r);
    }
  }

  @Override public void mouseMoved(MouseEvent e) {
    updateRolloverIndex(e);
  }

  @Override public void mouseDragged(MouseEvent e) {
    updateRolloverIndex(e);
  }

  @Override public void mouseClicked(MouseEvent e) {
    Component c = e.getComponent();
    // Only a left click on a list item (the JList selects it on mouse press)
    boolean isItemClicked = SwingUtilities.isLeftMouseButton(e) && rolloverIndex >= 0;
    // Single click: select only, double click: select and close the menus
    boolean doNotClose = c instanceof PopupList && ((PopupList<?>) c).isDoNotCloseOnMouseClick();
    boolean close = !doNotClose || e.getClickCount() >= 2;
    if (isItemClicked && close && c instanceof JList) {
      // Clear the rollover state so that a stale highlight is not shown
      // when the menu is reopened
      setRolloverIndex((JList<?>) c, -1);
      MenuSelectionManager.defaultManager().clearSelectedPath();
    }
  }

  @Override public void mouseExited(MouseEvent e) {
    Component c = e.getComponent();
    if (c instanceof JList) {
      setRolloverIndex((JList<?>) c, -1);
    }
  }
}

final class LookAndFeelUtils {
  private static String lookAndFeel = UIManager.getLookAndFeel().getClass().getName();

  private LookAndFeelUtils() {
    /* Singleton */
  }

  public static JMenu createLookAndFeelMenu() {
    JMenu menu = new JMenu("LookAndFeel");
    ButtonGroup buttonGroup = new ButtonGroup();
    for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
      AbstractButton b = makeButton(info);
      initLookAndFeelAction(info, b);
      menu.add(b);
      buttonGroup.add(b);
    }
    return menu;
  }

  private static AbstractButton makeButton(UIManager.LookAndFeelInfo info) {
    boolean selected = info.getClassName().equals(lookAndFeel);
    return new JRadioButtonMenuItem(info.getName(), selected);
  }

  public static void initLookAndFeelAction(UIManager.LookAndFeelInfo info, AbstractButton b) {
    String cmd = info.getClassName();
    b.setText(info.getName());
    b.setActionCommand(cmd);
    b.setHideActionText(true);
    b.addActionListener(e -> setLookAndFeel(cmd));
  }

  private static void setLookAndFeel(String newLookAndFeel) {
    String oldLookAndFeel = lookAndFeel;
    if (!oldLookAndFeel.equals(newLookAndFeel)) {
      try {
        UIManager.setLookAndFeel(newLookAndFeel);
        lookAndFeel = newLookAndFeel;
      } catch (UnsupportedLookAndFeelException ignored) {
        Toolkit.getDefaultToolkit().beep();
      } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
        Logger.getGlobal().severe(ex::getMessage);
        return;
      }
      updateLookAndFeel();
      // firePropertyChange("lookAndFeel", oldLookAndFeel, newLookAndFeel);
    }
  }

  private static void updateLookAndFeel() {
    for (Window window : Window.getWindows()) {
      SwingUtilities.updateComponentTreeUI(window);
    }
  }
}
