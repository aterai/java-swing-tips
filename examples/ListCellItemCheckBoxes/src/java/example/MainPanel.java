// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.swing.*;
import javax.swing.border.Border;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
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

    JList<ListItem> list = new RubberBandSelectionList<>(model);
    list.setPrototypeCellValue(new ListItem("red", new ColorIcon(Color.RED)));
    list.setOpaque(false);
    list.setBackground(new Color(0x0, true));

    JPopupMenu popup = new JPopupMenu("JList JPopupMenu");
    popup.add("info").addActionListener(e -> {
      String msg = list.getSelectedValuesList().stream()
          .map(ListItem::getTitle)
          .collect(Collectors.joining(", "));
      JOptionPane.showMessageDialog(list.getRootPane(), msg);
    });
    popup.addSeparator();
    popup.add("JMenuItem 1");
    popup.add("JMenuItem 2");
    list.setComponentPopupMenu(popup);
    // list.addListSelectionListener(e -> SwingUtilities.getUnwrappedParent(list).repaint());

    JScrollPane scroll = new JScrollPane(list);
    scroll.setBackground(new Color(0x0, true));
    scroll.setOpaque(false);
    scroll.setBorder(BorderFactory.createEmptyBorder());
    scroll.setViewportBorder(BorderFactory.createEmptyBorder());
    scroll.getViewport().setOpaque(false);

    JPanel panel = new JPanel(new BorderLayout());
    panel.setOpaque(false);
    panel.add(scroll);

    add(panel);
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
      ex.printStackTrace();
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

class RubberBandSelectionList<E extends ListItem> extends JList<E> {
  protected static final Color SELECTED_COLOR = new Color(0x40_32_64_FF, true);
  protected static final Color ROLLOVER_COLOR = new Color(0x40_32_64_AA, true);
  private transient ItemCheckBoxesListener rbl;
  private Color rubberBandColor;
  private final Path2D rubberBand = new Path2D.Double();
  private int rollOverIndex = -1;
  private int checkedIndex = -1;

  protected RubberBandSelectionList(ListModel<E> model) {
    super(model);
  }

  @Override public void updateUI() {
    setSelectionForeground(null); // Nimbus
    setSelectionBackground(null); // Nimbus
    setCellRenderer(null);
    removeMouseListener(rbl);
    removeMouseMotionListener(rbl);
    super.updateUI();

    rubberBandColor = makeRubberBandColor(getSelectionBackground());
    setLayoutOrientation(HORIZONTAL_WRAP);
    setVisibleRowCount(0);
    setFixedCellWidth(80);
    setFixedCellHeight(60);
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

    setCellRenderer(new ListItemCellRenderer());
    rbl = new ItemCheckBoxesListener();
    addMouseMotionListener(rbl);
    addMouseListener(rbl);
  }

  @Override protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setPaint(getSelectionBackground());
    g2.draw(rubberBand);
    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .1f));
    g2.setPaint(rubberBandColor);
    g2.fill(rubberBand);
    g2.dispose();
  }

  @Override public void setSelectionInterval(int anchor, int lead) {
    if (checkedIndex < 0 && isDragging()) {
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

  private static Color makeRubberBandColor(Color c) {
    int r = c.getRed();
    int g = c.getGreen();
    int b = c.getBlue();
    int max = Math.max(Math.max(r, g), b);
    if (max == r) {
      max <<= 8;
    } else if (max == g) {
      max <<= 4;
    }
    return new Color(max);
  }

  protected Path2D getRubberBand() {
    return rubberBand;
  }

  protected boolean isDragging() {
    Rectangle r = getRubberBand().getBounds();
    return r.width != 0 || r.height != 0;
  }

  private final class ItemCheckBoxesListener extends MouseAdapter {
    private final Point srcPoint = new Point();

    @Override public void mouseDragged(MouseEvent e) {
      checkedIndex = -1;
      JList<?> l = (JList<?>) e.getComponent();
      // l.setFocusable(true);
      Point dstPoint = e.getPoint();
      Path2D rb = getRubberBand();
      rb.reset();
      rb.moveTo(srcPoint.x, srcPoint.y);
      rb.lineTo(dstPoint.x, srcPoint.y);
      rb.lineTo(dstPoint.x, dstPoint.y);
      rb.lineTo(srcPoint.x, dstPoint.y);
      rb.closePath();

      int[] indices = IntStream.range(0, l.getModel().getSize())
          .filter(i -> rb.intersects(l.getCellBounds(i, i))).toArray();
      l.setSelectedIndices(indices);
      l.repaint();
    }

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
      if (idx > 0) {
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

    @Override public void mouseReleased(MouseEvent e) {
      getRubberBand().reset();
      JList<?> l = (JList<?>) e.getComponent();
      // l.getSelectionModel().setValueIsAdjusting(false);
      // l.setFocusable(true);
      l.repaint();
    }

    @Override public void mousePressed(MouseEvent e) {
      JList<?> l = (JList<?>) e.getComponent();
      // l.getSelectionModel().setValueIsAdjusting(true);
      Point pt = e.getPoint();
      int index = l.locationToIndex(pt);
      if (l.getCellBounds(index, index).contains(pt)) {
        // l.setFocusable(false);
        cellPressed(e, index);
      } else {
        EventQueue.invokeLater(() -> {
          l.getSelectionModel().setAnchorSelectionIndex(-1);
          l.getSelectionModel().setLeadSelectionIndex(-1);
          rollOverIndex = -1;
          checkedIndex = -1;
          l.clearSelection();
        });
        // l.setFocusable(false);
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
            // setFocusable(false);
            removeSelectionInterval(index, index);
          } else {
            setSelectionInterval(index, index);
          }
        });
      }
    }

    private Optional<AbstractButton> getCheckBoxAt(MouseEvent e, int index) {
      JList<E> list = RubberBandSelectionList.this;
      boolean b = e.isShiftDown() || e.isControlDown() || e.isAltDown();
      return b ? Optional.empty() : getDeepestButtonAt(list, index, e.getPoint());
    }

    private Optional<AbstractButton> getDeepestButtonAt(JList<E> list, int index, Point pt) {
      E proto = list.getPrototypeCellValue();
      ListCellRenderer<? super E> cr = list.getCellRenderer();
      Component c = cr.getListCellRendererComponent(list, proto, index, false, false);
      Rectangle r = list.getCellBounds(index, index);
      c.setBounds(r);
      // c.doLayout(); // may be needed for other layout managers (e.g. FlowLayout)
      pt.translate(-r.x, -r.y);
      return Optional.ofNullable(SwingUtilities.getDeepestComponentAt(c, pt.x, pt.y))
          .filter(AbstractButton.class::isInstance)
          .map(AbstractButton.class::cast);
    }
  }

  protected class ListItemCellRenderer implements ListCellRenderer<E> {
    private final JPanel renderer = new JPanel(new BorderLayout(0, 0));
    private final AbstractButton check = new JCheckBox();
    private final JLabel icon = new JLabel("", null, SwingConstants.CENTER);
    private final JLabel label = new JLabel("", SwingConstants.CENTER);
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

      label.setVerticalTextPosition(SwingConstants.TOP);
      label.setHorizontalTextPosition(SwingConstants.CENTER);
      label.setForeground(itemPanel.getForeground());
      label.setBackground(itemPanel.getBackground());
      label.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
      label.setOpaque(false);

      icon.setHorizontalTextPosition(SwingConstants.CENTER);
      icon.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
      icon.setOpaque(false);

      check.setOpaque(false);
      check.setVisible(false);

      Dimension d = check.getPreferredSize();
      JPanel p = new JPanel(new BorderLayout(0, 0));
      p.setOpaque(false);
      p.add(check, BorderLayout.NORTH);
      p.add(Box.createHorizontalStrut(d.width), BorderLayout.SOUTH);

      itemPanel.add(p, BorderLayout.EAST);
      itemPanel.add(Box.createHorizontalStrut(d.width), BorderLayout.WEST);
      itemPanel.add(icon);
      itemPanel.add(label, BorderLayout.SOUTH);
      itemPanel.setOpaque(true);

      renderer.add(itemPanel);
      renderer.setOpaque(false);
      renderer.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
    }

    @Override public Component getListCellRendererComponent(JList<? extends E> list, E value, int index, boolean isSelected, boolean cellHasFocus) {
      label.setText(value.getTitle());
      itemPanel.setBorder(cellHasFocus ? focusBorder : noFocusBorder);
      icon.setIcon(value.getIcon());
      check.setSelected(isSelected);
      check.getModel().setRollover(index == rollOverIndex);
      if (isSelected) {
        label.setForeground(list.getSelectionForeground());
        label.setBackground(SELECTED_COLOR);
        itemPanel.setBackground(SELECTED_COLOR);
        check.setVisible(true);
      } else if (index == rollOverIndex) {
        itemPanel.setBackground(ROLLOVER_COLOR);
        check.setVisible(true);
      } else {
        label.setForeground(list.getForeground());
        label.setBackground(list.getBackground());
        itemPanel.setBackground(list.getBackground());
        check.setVisible(false);
      }
      return renderer;
    }
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
    return 32;
  }

  @Override public int getIconHeight() {
    return 32;
  }
}
