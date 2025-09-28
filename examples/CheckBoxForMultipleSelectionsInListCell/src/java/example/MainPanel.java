// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.border.Border;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new GridLayout(1, 2));
    JList<ListItem> list = new JList<ListItem>(makeModel()) {
      @Override public void updateUI() {
        super.updateUI();
        setCellRenderer(new SimpleListItemCellRenderer());
      }
    };
    add(new JScrollPane(list));
    add(new JScrollPane(new MultipleSelectionList<>(makeModel())));
    setPreferredSize(new Dimension(320, 240));
  }

  private static ListModel<ListItem> makeModel() {
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
  private int rollOverIndex = -1;
  private int checkedIndex = -1;

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

  @Override public void setSelectionInterval(int anchor, int lead) {
    if (checkedIndex < 0) {
      super.setSelectionInterval(anchor, lead);
    } else {
      EventQueue.invokeLater(() -> {
        if (checkedIndex >= 0 && lead == anchor && checkedIndex == anchor) {
          super.addSelectionInterval(checkedIndex, checkedIndex);
        } else {
          super.setSelectionInterval(anchor, lead);
        }
      });
    }
  }

  @Override public void removeSelectionInterval(int index0, int index1) {
    if (checkedIndex < 0) {
      super.removeSelectionInterval(index0, index1);
    } else {
      EventQueue.invokeLater(() -> super.removeSelectionInterval(index0, index1));
    }
  }

  private final class ItemCheckBoxesListener extends MouseAdapter {
    private final Point srcPoint = new Point();

    @Override public void mouseExited(MouseEvent e) {
      rollOverIndex = -1;
      e.getComponent().repaint();
    }

    @Override public void mouseMoved(MouseEvent e) {
      Point pt = e.getPoint();
      int idx = locationToIndex(pt);
      if (!getCellBounds(idx, idx).contains(pt)) {
        idx = -1;
      }
      Rectangle rect = new Rectangle();
      if (idx >= 0) {
        rect.add(getCellBounds(idx, idx));
        if (rollOverIndex >= 0 && idx != rollOverIndex) {
          rect.add(getCellBounds(rollOverIndex, rollOverIndex));
        }
        rollOverIndex = idx;
      } else {
        if (rollOverIndex >= 0) {
          rect.add(getCellBounds(rollOverIndex, rollOverIndex));
        }
        rollOverIndex = -1;
      }
      ((JComponent) e.getComponent()).repaint(rect);
    }

    @Override public void mousePressed(MouseEvent e) {
      JList<?> l = (JList<?>) e.getComponent();
      Point pt = e.getPoint();
      int index = l.locationToIndex(pt);
      if (l.getCellBounds(index, index).contains(pt)) {
        cellPressed(e, index);
      } else {
        EventQueue.invokeLater(() -> {
          l.getSelectionModel().setAnchorSelectionIndex(-1);
          l.getSelectionModel().setLeadSelectionIndex(-1);
          rollOverIndex = -1;
          checkedIndex = -1;
          l.clearSelection();
        });
      }
      srcPoint.setLocation(pt);
      l.repaint();
    }

    private void cellPressed(MouseEvent e, int index) {
      if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() > 1) {
        ListItem item = getModel().getElementAt(index);
        JOptionPane.showMessageDialog(getRootPane(), item.getTitle());
      } else {
        checkedIndex = -1;
        getCheckBoxAt(e, index).ifPresent(button -> {
          checkedIndex = index;
          if (isSelectedIndex(index)) {
            removeSelectionInterval(index, index);
          } else {
            setSelectionInterval(index, index);
          }
        });
      }
    }

    private Optional<Component> getCheckBoxAt(MouseEvent e, int index) {
      JList<E> list = MultipleSelectionList.this;
      boolean b = e.isShiftDown() || e.isControlDown() || e.isAltDown();
      return b ? Optional.empty() : getDeepestBoxAt(list, index, e.getPoint());
    }

    private Optional<Component> getDeepestBoxAt(JList<E> list, int index, Point pt) {
      E proto = list.getPrototypeCellValue();
      ListCellRenderer<? super E> cr = list.getCellRenderer();
      Component c = cr.getListCellRendererComponent(list, proto, index, false, false);
      Rectangle r = list.getCellBounds(index, index);
      c.setBounds(r);
      pt.translate(-r.x, -r.y);
      return Optional.ofNullable(SwingUtilities.getDeepestComponentAt(c, pt.x, pt.y))
          .filter(b -> b instanceof JCheckBox || b instanceof Box.Filler);
    }
  }

  protected class ListItemCellRenderer implements ListCellRenderer<E> {
    private final JPanel renderer = new JPanel(new BorderLayout(0, 0));
    private final AbstractButton check = new JCheckBox();
    private final Component filler = Box.createRigidArea(check.getPreferredSize());
    private final JLabel label = new JLabel("");
    private final JPanel itemPanel = new JPanel(new BorderLayout(2, 2)) {
      @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
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
      label.setForeground(itemPanel.getForeground());
      label.setBackground(itemPanel.getBackground());
      label.setOpaque(false);
      check.setOpaque(false);
      check.setVisible(false);
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
      itemPanel.setBorder(cellHasFocus ? focusBorder : noFocusBorder);
      check.setSelected(isSelected);
      check.getModel().setRollover(index == rollOverIndex);
      if (isSelected) {
        label.setForeground(list.getSelectionForeground());
        label.setBackground(SELECTED_COLOR);
        itemPanel.setBackground(SELECTED_COLOR);
        itemPanel.add(check, BorderLayout.WEST);
        check.setVisible(true);
      } else if (index == rollOverIndex) {
        itemPanel.setBackground(ROLLOVER_COLOR);
        itemPanel.add(check, BorderLayout.WEST);
        check.setVisible(true);
      } else {
        label.setForeground(list.getForeground());
        label.setBackground(list.getBackground());
        itemPanel.setBackground(list.getBackground());
        itemPanel.add(filler, BorderLayout.WEST);
        check.setVisible(false);
      }
      return renderer;
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
