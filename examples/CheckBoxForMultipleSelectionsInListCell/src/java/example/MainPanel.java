// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Objects;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.border.Border;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new GridLayout(1, 2));
    JList<ListItem> list = new JList<ListItem>(createModel()) {
      @Override public void updateUI() {
        super.updateUI();
        setCellRenderer(new SimpleListItemCellRenderer());
      }
    };
    add(new JScrollPane(list));
    add(new JScrollPane(new MultipleSelectionList<>(createModel())));
    setPreferredSize(new Dimension(320, 240));
  }

  private static ListModel<ListItem> createModel() {
    DefaultListModel<ListItem> model = new DefaultListModel<>();
    model.addElement(new ListItem("red", new ColorIcon(Color.RED)));
    model.addElement(new ListItem("green", new ColorIcon(Color.GREEN)));
    model.addElement(new ListItem("blue", new ColorIcon(Color.BLUE)));
    model.addElement(new ListItem("cyan", new ColorIcon(Color.CYAN)));
    model.addElement(new ListItem("darkGray", new ColorIcon(Color.DARK_GRAY)));
    model.addElement(new ListItem("gray", new ColorIcon(Color.GRAY)));
    model.addElement(new ListItem("lightGray", new ColorIcon(Color.LIGHT_GRAY)));
    model.addElement(new ListItem("magenta", new ColorIcon(Color.MAGENTA)));
    model.addElement(new ListItem("orange", new ColorIcon(Color.ORANGE)));
    model.addElement(new ListItem("pink", new ColorIcon(Color.PINK)));
    model.addElement(new ListItem("yellow", new ColorIcon(Color.YELLOW)));
    model.addElement(new ListItem("black", new ColorIcon(Color.BLACK)));
    model.addElement(new ListItem("white", new ColorIcon(Color.WHITE)));
    return model;
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

class MultipleSelectionList<E extends ListItem> extends JList<E> {
  protected static final Color SELECTED_COLOR = new Color(0x40_32_64_FF, true);
  protected static final Color ROLLOVER_COLOR = new Color(0x40_32_64_AA, true);
  private transient ItemCheckBoxesListener checkListener;
  private int rolloverIndex = -1;
  private int checkIndex = -1; // index of the cell whose check box is pressed

  protected MultipleSelectionList(ListModel<E> model) {
    super(model);
  }

  @Override public void updateUI() {
    setSelectionForeground(null); // Nimbus
    setSelectionBackground(null); // Nimbus
    setCellRenderer(null);
    removeMouseListener(checkListener);
    removeMouseMotionListener(checkListener);
    super.updateUI();
    setCellRenderer(new ListItemCellRenderer());
    checkListener = new ItemCheckBoxesListener();
    addMouseMotionListener(checkListener);
    addMouseListener(checkListener);
  }

  @Override protected void processMouseEvent(MouseEvent e) {
    // The MouseListener of the ListUI is notified before ItemCheckBoxesListener
    // and calls setSelectionInterval(...), so the index of the pressed check box
    // must be updated before any MouseListener is notified.
    if (e.getID() == MouseEvent.MOUSE_PRESSED) {
      checkIndex = getCheckBoxIndexAt(e);
    }
    super.processMouseEvent(e);
  }

  @Override public void setSelectionInterval(int anchor, int lead) {
    if (checkIndex < 0) {
      super.setSelectionInterval(anchor, lead);
    } else {
      // Defer the selection change requested by the ListUI so that it is
      // applied after the check box toggles the selection of the cell
      EventQueue.invokeLater(() -> {
        int idx = checkIndex;
        if (idx >= 0 && lead == anchor && idx == anchor) {
          super.addSelectionInterval(idx, idx);
        } else {
          super.setSelectionInterval(anchor, lead);
        }
      });
    }
  }

  @Override public void removeSelectionInterval(int index0, int index1) {
    if (checkIndex < 0) {
      super.removeSelectionInterval(index0, index1);
    } else {
      EventQueue.invokeLater(() -> super.removeSelectionInterval(index0, index1));
    }
  }

  protected int getCellIndexAt(Point pt) {
    int index = locationToIndex(pt);
    Rectangle r = getCellBounds(index, index);
    return r != null && r.contains(pt) ? index : -1;
  }

  protected void repaintCell(int index) {
    Rectangle r = getCellBounds(index, index);
    if (r != null) {
      repaint(r);
    }
  }

  private int getCheckBoxIndexAt(MouseEvent e) {
    boolean modifierDown = e.isShiftDown() || e.isControlDown() || e.isAltDown();
    Point pt = e.getPoint();
    int index = getCellIndexAt(pt);
    boolean hit = !modifierDown && index >= 0 && isCheckBoxAt(index, pt);
    return hit ? index : -1;
  }

  private boolean isCheckBoxAt(int index, Point pt) {
    E value = getModel().getElementAt(index);
    ListCellRenderer<? super E> cr = getCellRenderer();
    Component c = cr.getListCellRendererComponent(this, value, index, false, false);
    Rectangle r = getCellBounds(index, index);
    c.setBounds(r);
    c.validate(); // lay out the JCheckBox or Box.Filler swapped by the cell renderer
    Component deepest = SwingUtilities.getDeepestComponentAt(c, pt.x - r.x, pt.y - r.y);
    // The cell renderer uses a Box.Filler instead of a hidden JCheckBox
    return deepest instanceof JCheckBox || deepest instanceof Box.Filler;
  }

  private final class ItemCheckBoxesListener extends MouseAdapter {
    @Override public void mouseExited(MouseEvent e) {
      repaintCell(rolloverIndex);
      rolloverIndex = -1;
    }

    @Override public void mouseMoved(MouseEvent e) {
      int index = getCellIndexAt(e.getPoint());
      if (index != rolloverIndex) {
        repaintCell(rolloverIndex);
        rolloverIndex = index;
        repaintCell(index);
      }
    }

    @Override public void mousePressed(MouseEvent e) {
      int index = getCellIndexAt(e.getPoint());
      if (index >= 0) {
        cellPressed(e, index);
      } else {
        EventQueue.invokeLater(() -> {
          getSelectionModel().setAnchorSelectionIndex(-1);
          getSelectionModel().setLeadSelectionIndex(-1);
          rolloverIndex = -1;
          checkIndex = -1;
          clearSelection();
        });
      }
      repaint();
    }

    private void cellPressed(MouseEvent e, int index) {
      if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() > 1) {
        ListItem item = getModel().getElementAt(index);
        JOptionPane.showMessageDialog(getRootPane(), item.getTitle());
      } else if (checkIndex == index) {
        if (isSelectedIndex(index)) {
          removeSelectionInterval(index, index);
        } else {
          setSelectionInterval(index, index);
        }
      }
    }
  }

  protected class ListItemCellRenderer implements ListCellRenderer<E> {
    private final JPanel renderer = new JPanel(new BorderLayout(0, 0));
    private final BorderLayout itemLayout = new BorderLayout(2, 2);
    private final AbstractButton check = new JCheckBox();
    private final Component filler = Box.createRigidArea(check.getPreferredSize());
    private final JLabel label = new JLabel("");
    private final JPanel itemPanel = new JPanel(itemLayout) {
      @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Paint the translucent selection color once more to make
        // the selected cell darker than the rollover cell
        if (SELECTED_COLOR.equals(getBackground())) {
          Graphics2D g2 = (Graphics2D) g.create();
          g2.setPaint(SELECTED_COLOR);
          g2.fillRect(0, 0, getWidth(), getHeight());
          g2.dispose();
        }
      }
    };
    private final Border focusBorder = UIManager.getBorder("List.focusCellHighlightBorder");
    private final Border noFocusBorder; // = UIManager.getBorder("List.noFocusBorder");

    protected ListItemCellRenderer() {
      Border b = UIManager.getBorder("List.noFocusBorder");
      if (Objects.isNull(b)) { // Nimbus???
        Insets i = focusBorder.getBorderInsets(itemPanel);
        b = BorderFactory.createEmptyBorder(i.top, i.left, i.bottom, i.right);
      }
      noFocusBorder = b;
      itemPanel.setBorder(noFocusBorder);
      label.setOpaque(false);
      check.setOpaque(false);
      itemPanel.add(filler, BorderLayout.WEST);
      itemPanel.add(label);
      itemPanel.setOpaque(true);
      renderer.add(itemPanel);
      renderer.setOpaque(false);
    }

    @Override public Component getListCellRendererComponent(JList<? extends E> list, E value, int index, boolean isSelected, boolean cellHasFocus) {
      if (value != null) {
        label.setText(value.getTitle());
        label.setIcon(value.getIcon());
      }
      boolean rollover = index == rolloverIndex;
      itemPanel.setBorder(cellHasFocus ? focusBorder : noFocusBorder);
      check.setSelected(isSelected);
      check.getModel().setRollover(rollover);
      // Show the JCheckBox only in the selected or rollover cell, otherwise
      // use a Box.Filler of the same size to keep the position of the label
      setLeadingComponent(isSelected || rollover ? check : filler);
      label.setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());
      if (isSelected) {
        itemPanel.setBackground(SELECTED_COLOR);
      } else if (rollover) {
        itemPanel.setBackground(ROLLOVER_COLOR);
      } else {
        itemPanel.setBackground(list.getBackground());
      }
      return renderer;
    }

    private void setLeadingComponent(Component c) {
      Component old = itemLayout.getLayoutComponent(BorderLayout.WEST);
      if (!Objects.equals(old, c)) {
        if (old != null) {
          itemPanel.remove(old);
        }
        itemPanel.add(c, BorderLayout.WEST);
      }
    }
  }
}

class SimpleListItemCellRenderer implements ListCellRenderer<ListItem> {
  private final JLabel renderer = new JLabel("");
  private final Border focusBorder = UIManager.getBorder("List.focusCellHighlightBorder");
  private final Border noFocusBorder; // = UIManager.getBorder("List.noFocusBorder");

  protected SimpleListItemCellRenderer() {
    Border b = UIManager.getBorder("List.noFocusBorder");
    if (Objects.isNull(b)) { // Nimbus???
      Insets i = focusBorder.getBorderInsets(renderer);
      b = BorderFactory.createEmptyBorder(i.top, i.left, i.bottom, i.right);
    }
    noFocusBorder = b;
    renderer.setBorder(noFocusBorder);
    renderer.setOpaque(true);
  }

  @Override public Component getListCellRendererComponent(JList<? extends ListItem> list, ListItem value, int index, boolean isSelected, boolean cellHasFocus) {
    renderer.setText(value.getTitle());
    renderer.setIcon(value.getIcon());
    renderer.setBorder(cellHasFocus ? focusBorder : noFocusBorder);
    if (isSelected) {
      renderer.setForeground(list.getSelectionForeground());
      renderer.setBackground(list.getSelectionBackground());
    } else {
      renderer.setForeground(list.getForeground());
      renderer.setBackground(list.getBackground());
    }
    return renderer;
  }
}

class ListItem {
  private final Icon icon;
  private final String title;

  protected ListItem(String title, Icon icon) {
    this.title = title;
    this.icon = icon;
  }

  public String getTitle() {
    return title;
  }

  public Icon getIcon() {
    return icon;
  }
}

class ColorIcon implements Icon {
  private final Color color;

  protected ColorIcon(Color color) {
    this.color = color;
  }

  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.translate(x, y);
    g2.setPaint(color);
    g2.fillRect(0, 0, getIconWidth(), getIconHeight());
    g2.dispose();
  }

  @Override public int getIconWidth() {
    return 16;
  }

  @Override public int getIconHeight() {
    return 16;
  }
}
