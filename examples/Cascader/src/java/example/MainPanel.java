// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.accessibility.Accessible;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTextArea log = new JTextArea();
    log.setEditable(false);

    TreeModel address = createModel(
        "Japan/Tokyo/Chiyoda", "Japan/Tokyo/Chuo", "Japan/Tokyo/Minato",
        "Japan/Tokyo/Shinjuku", "Japan/Tokyo/Shibuya",
        "Japan/Osaka/Kita", "Japan/Osaka/Chuo", "Japan/Osaka/Naniwa",
        "Japan/Kyoto/Kamigyo", "Japan/Kyoto/Nakagyo", "Japan/Kyoto/Shimogyo",
        "Japan/Hokkaido/Sapporo", "Japan/Hokkaido/Hakodate",
        "USA/California/Los Angeles", "USA/California/San Francisco",
        "USA/California/San Diego", "USA/New York/New York City",
        "USA/New York/Buffalo", "USA/Texas/Houston", "USA/Texas/Austin",
        "UK/England/London", "UK/England/Manchester",
        "UK/Scotland/Edinburgh", "UK/Scotland/Glasgow");
    CascaderComboBox combo1 = new CascaderComboBox(address, false);
    combo1.addItemListener(e -> appendLog(log, e));

    TreeModel category = createModel(
        "Electronics/Computers/Laptops", "Electronics/Computers/Desktops",
        "Electronics/Computers/Tablets",
        "Electronics/Computers/Accessories/Keyboards",
        "Electronics/Computers/Accessories/Mice",
        "Electronics/Computers/Accessories/Monitors",
        "Electronics/Phones/Smartphones", "Electronics/Phones/Feature Phones",
        "Electronics/Audio/Headphones", "Electronics/Audio/Speakers",
        "Books/Fiction/Mystery", "Books/Fiction/Science Fiction",
        "Books/Fiction/Fantasy", "Books/Non-Fiction/History",
        "Books/Non-Fiction/Science", "Books/Comics",
        "Home/Kitchen/Cookware", "Home/Kitchen/Tableware",
        "Home/Furniture/Chairs", "Home/Furniture/Tables",
        "Home/Furniture/Shelves");
    CascaderComboBox combo2 = new CascaderComboBox(category, true);
    combo2.addItemListener(e -> appendLog(log, e));

    Box box = Box.createVerticalBox();
    box.add(createTitledPanel("Expand on click", combo1));
    box.add(Box.createVerticalStrut(5));
    box.add(createTitledPanel("Expand on hover", combo2));
    add(box, BorderLayout.NORTH);
    add(new JScrollPane(log));
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    setPreferredSize(new Dimension(320, 240));
  }

  private static void appendLog(JTextArea log, ItemEvent e) {
    if (e.getStateChange() == ItemEvent.SELECTED) {
      log.append(CascaderComboBox.toText((TreePath) e.getItem()) + "\n");
    }
  }

  // Build a tree from slash separated paths: "Japan/Tokyo/Chiyoda"
  private static TreeModel createModel(String... paths) {
    DefaultMutableTreeNode root = new DefaultMutableTreeNode("root");
    for (String path : paths) {
      DefaultMutableTreeNode parent = root;
      for (String name : path.split("/")) {
        parent = getOrCreateChild(parent, name);
      }
    }
    return new DefaultTreeModel(root);
  }

  private static DefaultMutableTreeNode getOrCreateChild(
      DefaultMutableTreeNode parent, String name) {
    DefaultMutableTreeNode found = null;
    for (int i = 0; i < parent.getChildCount() && found == null; i++) {
      DefaultMutableTreeNode child = (DefaultMutableTreeNode) parent.getChildAt(i);
      if (Objects.equals(child.getUserObject(), name)) {
        found = child;
      }
    }
    if (found == null) {
      found = new DefaultMutableTreeNode(name);
      parent.add(found);
    }
    return found;
  }

  private static Component createTitledPanel(String title, Component c) {
    JPanel p = new JPanel(new BorderLayout());
    p.setBorder(BorderFactory.createTitledBorder(title));
    p.add(c);
    return p;
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

// A JComboBox whose dropdown list is replaced with cascading JList columns.
// The ComboBoxModel has no elements and holds only the selected TreePath,
// so the TreeModel children are read lazily when each column is opened.
@SuppressWarnings("PMD.TooManyMethods")
final class CascaderComboBox extends JComboBox<TreePath> {
  private static final String PLACEHOLDER = "Please select";
  private final TreeModel treeModel;
  private final boolean expandOnHover;
  private final JPanel columnPanel = new JPanel();
  private final List<JList<Object>> columns = new ArrayList<>();
  private final transient CascadeMouseHandler mouseHandler = new CascadeMouseHandler();
  private int activeColumn;

  /* default */ CascaderComboBox(TreeModel treeModel, boolean expandOnHover) {
    super(new DefaultComboBoxModel<>());
    this.treeModel = treeModel;
    this.expandOnHover = expandOnHover;
    columnPanel.setLayout(new BoxLayout(columnPanel, BoxLayout.X_AXIS));
    setRenderer(new DefaultListCellRenderer() {
      @Override public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        String text = value instanceof TreePath ? toText((TreePath) value) : PLACEHOLDER;
        return super.getListCellRendererComponent(list, text, index, isSelected, cellHasFocus);
      }
    });
    addPopupMenuListener(new PopupMenuListener() {
      @Override public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
        installColumnPanel();
      }

      @Override public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
        /* not needed */
      }

      @Override public void popupMenuCanceled(PopupMenuEvent e) {
        /* not needed */
      }
    });
    installCascadeActions();
  }

  public static String toText(TreePath path) {
    // Skip the root node
    return Arrays.stream(path.getPath()).skip(1)
        .map(Objects::toString)
        .collect(Collectors.joining(" / "));
  }

  public void setSelectedPath(TreePath path) {
    // JComboBox#setSelectedItem(...) rejects an item that is not
    // in the model of a non-editable JComboBox, so set it to the model
    getModel().setSelectedItem(path);
  }

  private JPopupMenu getPopup() {
    // BasicComboBoxUI returns its ComboPopup (a JPopupMenu) as child 0
    Accessible a = getUI().getAccessibleChild(this, 0);
    return a instanceof JPopupMenu ? (JPopupMenu) a : null;
  }

  // Called by BasicComboPopup#show() before the popup location is computed
  private void installColumnPanel() {
    JPopupMenu popup = getPopup();
    // The popup is recreated by updateUI(), so check it every time
    if (popup != null && !popup.isAncestorOf(columnPanel)) {
      popup.removeAll(); // remove the JScrollPane of the default JList
      popup.add(columnPanel);
    }
    columnPanel.removeAll();
    columns.clear();
    setActiveColumn(0);
    addColumn(treeModel.getRoot());
    // Restore the columns from the selected path
    Object selected = getModel().getSelectedItem();
    if (selected instanceof TreePath) {
      Object[] nodes = ((TreePath) selected).getPath();
      for (int i = 1; i < nodes.length && i - 1 < columns.size(); i++) {
        JList<Object> list = columns.get(i - 1);
        int idx = treeModel.getIndexOfChild(nodes[i - 1], nodes[i]);
        if (idx < 0) {
          break; // the rest of the path no longer exists in the TreeModel
        }
        // The ListSelectionListener appends the next column
        list.setSelectedIndex(idx);
        list.ensureIndexIsVisible(idx);
        setActiveColumn(i - 1);
      }
    }
  }

  private void addColumn(Object parent) {
    JList<Object> list = new JList<>(new ChildListModel(treeModel, parent));
    list.setFocusable(false); // keep the focus on the JComboBox
    list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    list.setVisibleRowCount(getMaximumRowCount());
    list.setFixedCellHeight(getHeight());
    list.setCellRenderer(new CascadeCellRenderer(
        treeModel, l -> columns.indexOf(l) == activeColumn));
    list.addListSelectionListener(this::selectionChanged);
    list.addMouseListener(mouseHandler);
    list.addMouseMotionListener(mouseHandler);
    JScrollPane scroll = new JScrollPane(list);
    scroll.setBorder(BorderFactory.createMatteBorder(
        0, columns.isEmpty() ? 0 : 1, 0, 0, Color.LIGHT_GRAY));
    scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    columns.add(list);
    columnPanel.add(scroll);
  }

  private void selectionChanged(ListSelectionEvent e) {
    int idx = columns.indexOf(e.getSource());
    if (!e.getValueIsAdjusting() && idx >= 0) {
      updateColumns(idx);
    }
  }

  private void updateColumns(int idx) {
    // Remove the columns to the right of the changed column
    for (int i = columns.size() - 1; i > idx; i--) {
      columns.remove(i);
      columnPanel.remove(i);
    }
    Object value = columns.get(idx).getSelectedValue();
    if (value != null && !treeModel.isLeaf(value)) {
      addColumn(value);
    }
    columnPanel.revalidate();
    columnPanel.repaint();
    JPopupMenu popup = getPopup();
    if (popup != null && popup.isVisible()) {
      popup.pack(); // resize the popup to the new number of columns
    }
  }

  private void commit(int columnIndex) {
    List<Object> nodes = new ArrayList<>();
    nodes.add(treeModel.getRoot());
    for (int i = 0; i <= columnIndex; i++) {
      nodes.add(columns.get(i).getSelectedValue());
    }
    setPopupVisible(false);
    setSelectedPath(new TreePath(nodes.toArray()));
  }

  // Replace the key actions of the JComboBox while the popup is visible
  private void installCascadeActions() {
    ActionMap am = getActionMap();
    // Open the popup with the arrow keys as the default actions do
    Consumer<ActionEvent> show = e -> setPopupVisible(true);
    Consumer<ActionEvent> none = e -> { /* do nothing */ };
    Action next = createAction(() -> moveSelection(1), show);
    Action prev = createAction(() -> moveSelection(-1), show);
    am.put("selectNext", next);
    am.put("selectNext2", next);
    am.put("selectPrevious", prev);
    am.put("selectPrevious2", prev);
    am.put("enterPressed", createAction(this::commitOrMoveRight, this::fireDefaultEnterAction));
    am.put("cascadeRight", createAction(this::moveRight, none));
    am.put("cascadeLeft", createAction(this::moveLeft, none));
    InputMap im = getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    im.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "cascadeRight");
    im.put(KeyStroke.getKeyStroke(KeyEvent.VK_KP_RIGHT, 0), "cascadeRight");
    im.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "cascadeLeft");
    im.put(KeyStroke.getKeyStroke(KeyEvent.VK_KP_LEFT, 0), "cascadeLeft");
  }

  private Action createAction(Runnable cascade, Consumer<ActionEvent> fallback) {
    return new AbstractAction() {
      @Override public void actionPerformed(ActionEvent e) {
        if (isPopupVisible() && !columns.isEmpty()) {
          cascade.run();
        } else {
          fallback.accept(e);
        }
      }
    };
  }

  // Delegate to the LookAndFeel action (e.g. press the default button)
  private void fireDefaultEnterAction(ActionEvent e) {
    ActionMap parent = getActionMap().getParent();
    Action a = parent == null ? null : parent.get("enterPressed");
    if (a != null) {
      a.actionPerformed(e);
    }
  }

  private void setActiveColumn(int index) {
    if (activeColumn != index) {
      activeColumn = index;
      // Repaint the selection colors of the active and inactive columns
      columnPanel.repaint();
    }
  }

  private void moveSelection(int step) {
    JList<Object> list = columns.get(activeColumn);
    int size = list.getModel().getSize();
    if (size > 0) {
      int idx = list.getSelectedIndex();
      if (idx < 0) {
        idx = step > 0 ? 0 : size - 1;
      } else {
        idx = Math.max(0, Math.min(size - 1, idx + step));
      }
      list.setSelectedIndex(idx);
      list.ensureIndexIsVisible(idx);
    }
  }

  private void moveRight() {
    if (activeColumn + 1 < columns.size()) {
      setActiveColumn(activeColumn + 1);
      JList<Object> list = columns.get(activeColumn);
      if (list.getSelectedIndex() < 0 && list.getModel().getSize() > 0) {
        list.setSelectedIndex(0);
      }
    }
  }

  private void moveLeft() {
    if (activeColumn > 0) {
      // Clearing the selection also removes the columns to the right
      columns.get(activeColumn).clearSelection();
      setActiveColumn(activeColumn - 1);
    }
  }

  private void commitOrMoveRight() {
    Object value = columns.get(activeColumn).getSelectedValue();
    if (value != null) {
      if (treeModel.isLeaf(value)) {
        commit(activeColumn);
      } else {
        moveRight();
      }
    }
  }

  private final class CascadeMouseHandler extends MouseAdapter {
    @Override public void mousePressed(MouseEvent e) {
      setActiveColumn(Math.max(0, columns.indexOf(e.getComponent())));
    }

    @Override public void mouseReleased(MouseEvent e) {
      JList<?> list = (JList<?>) e.getComponent();
      int idx = getIndexAt(list, e.getPoint());
      // Ctrl+click deselects the cell, so commit only the selected leaf
      if (SwingUtilities.isLeftMouseButton(e) && idx >= 0
          && idx == list.getSelectedIndex()
          && treeModel.isLeaf(list.getModel().getElementAt(idx))) {
        commit(columns.indexOf(list));
      }
    }

    @Override public void mouseMoved(MouseEvent e) {
      JList<?> list = (JList<?>) e.getComponent();
      int idx = getIndexAt(list, e.getPoint());
      if (expandOnHover && idx >= 0) {
        // Activate the hovered column even if its cell is already selected
        setActiveColumn(columns.indexOf(list));
        list.setSelectedIndex(idx);
      }
    }

    private int getIndexAt(JList<?> list, Point pt) {
      // JList#locationToIndex(...) returns the last index below the last cell
      int idx = list.locationToIndex(pt);
      Rectangle r = idx < 0 ? null : list.getCellBounds(idx, idx);
      return r != null && r.contains(pt) ? idx : -1;
    }
  }
}

// A ListModel that reads the children of a TreeModel node on demand
class ChildListModel extends AbstractListModel<Object> {
  private final transient TreeModel model;
  private final transient Object parent;

  protected ChildListModel(TreeModel model, Object parent) {
    super();
    this.model = model;
    this.parent = parent;
  }

  @Override public int getSize() {
    return model.getChildCount(parent);
  }

  @Override public Object getElementAt(int index) {
    return model.getChild(parent, index);
  }
}

class CascadeCellRenderer implements ListCellRenderer<Object> {
  private final DefaultListCellRenderer renderer = new DefaultListCellRenderer();
  private final JPanel panel = new JPanel(new BorderLayout());
  private final JLabel arrow = new JLabel();
  private final Icon arrowIcon = new ArrowIcon();
  private final Icon emptyIcon = new EmptyIcon(arrowIcon);
  private final TreeModel model;
  private final Predicate<JList<?>> isActive;

  protected CascadeCellRenderer(TreeModel model, Predicate<JList<?>> isActive) {
    this.model = model;
    this.isActive = isActive;
    // Paint the background with the JPanel only, since Nimbus ignores
    // the ColorUIResource background of a JLabel or JPanel
    renderer.setOpaque(false);
    arrow.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 4));
    panel.add(renderer);
    panel.add(arrow, BorderLayout.EAST);
  }

  @Override public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
    Component c = renderer.getListCellRendererComponent(
        list, value, index, isSelected, false);
    Color bgc = c.getBackground();
    if (isSelected && !isActive.test(list)) {
      // Show the selected path of the parent columns in a lighter color
      bgc = blend(list.getSelectionBackground(), list.getBackground());
      c.setForeground(list.getForeground());
    }
    // A plain Color (not a ColorUIResource) is required for Nimbus
    panel.setBackground(new Color(bgc.getRGB()));
    // Keep the width of the arrow on the leaf rows to align the text
    arrow.setIcon(model.isLeaf(value) ? emptyIcon : arrowIcon);
    arrow.setForeground(c.getForeground());
    return panel;
  }

  private static Color blend(Color c1, Color c2) {
    return new Color(
        (c1.getRed() + c2.getRed() * 3) / 4,
        (c1.getGreen() + c2.getGreen() * 3) / 4,
        (c1.getBlue() + c2.getBlue() * 3) / 4);
  }
}

class ArrowIcon implements Icon {
  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.translate(x, y);
    g2.setPaint(c.getForeground());
    // A thin stroke similar to the arrow button of the JComboBox
    g2.setStroke(new BasicStroke(1f));
    Path2D p = new Path2D.Double();
    p.moveTo(2d, 1d);
    p.lineTo(6d, 5d);
    p.lineTo(2d, 9d);
    g2.draw(p);
    g2.dispose();
  }

  @Override public int getIconWidth() {
    return 8;
  }

  @Override public int getIconHeight() {
    return 10;
  }
}

class EmptyIcon implements Icon {
  private final Icon icon;

  protected EmptyIcon(Icon icon) {
    this.icon = icon;
  }

  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    /* Empty icon */
  }

  @Override public int getIconWidth() {
    return icon.getIconWidth();
  }

  @Override public int getIconHeight() {
    return icon.getIconHeight();
  }
}
