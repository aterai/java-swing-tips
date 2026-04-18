// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.MouseInputAdapter;

public final class MainPanel extends JPanel {
  private final transient ListItem[] defaultItems = {
      new ListItem("red", Color.RED),
      new ListItem("green", Color.GREEN),
      new ListItem("blue", Color.BLUE),
      new ListItem("cyan", Color.CYAN),
      new ListItem("darkGray", Color.DARK_GRAY),
      new ListItem("gray", Color.GRAY),
      new ListItem("lightGray", Color.LIGHT_GRAY),
      new ListItem("magenta", Color.MAGENTA),
      new ListItem("orange", Color.ORANGE),
      new ListItem("pink", Color.PINK),
      new ListItem("yellow", Color.YELLOW),
      new ListItem("black", Color.BLACK),
      new ListItem("white", Color.WHITE),
  };
  private final DefaultListModel<ListItem> model = new DefaultListModel<>();
  private final JList<ListItem> list = new JList<ListItem>(model) {
    private transient MouseInputAdapter handler;
    @Override public void updateUI() {
      removeMouseListener(handler);
      setSelectionForeground(null);
      setSelectionBackground(null);
      setCellRenderer(null);
      super.updateUI();
      setLayoutOrientation(HORIZONTAL_WRAP);
      getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
      setVisibleRowCount(0);
      setFixedCellWidth(64);
      setFixedCellHeight(64);
      setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
      setCellRenderer(new ListItemRenderer<>());
      handler = new ClearSelectionListener();
      addMouseListener(handler);
    }
  };
  private transient Comparator<ListItem> comparator;
  private final JRadioButton ascending = new JRadioButton("ascending", true);
  private final JRadioButton descending = new JRadioButton("descending");
  private final List<JRadioButton> directions = Arrays.asList(ascending, descending);

  private MainPanel() {
    super(new BorderLayout());
    Stream.of(defaultItems).forEach(model::addElement);
    list.setModel(model);

    JRadioButton noneButton = new JRadioButton("None", true);
    noneButton.addItemListener(e -> {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        setComparator(null);
        sortList();
      }
    });

    JRadioButton nameButton = new JRadioButton("Name");
    nameButton.addItemListener(e -> {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        setComparator(Comparator.comparing(ListItem::getTitle));
        reverseComparatorIfNeeded();
        sortList();
      }
    });

    JRadioButton colorButton = new JRadioButton("Color");
    colorButton.addItemListener(e -> {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        setComparator(Comparator.comparing(item -> item.getColor().getRGB()));
        reverseComparatorIfNeeded();
        sortList();
      }
    });

    Box sortBox = Box.createHorizontalBox();
    sortBox.add(new JLabel("Sort: "));
    ButtonGroup sortButtonGroup = new ButtonGroup();
    Stream.of(noneButton, nameButton, colorButton).forEach(r -> {
      sortButtonGroup.add(r);
      sortBox.add(r);
    });
    sortBox.add(Box.createHorizontalGlue());

    Box directionBox = Box.createHorizontalBox();
    directionBox.add(new JLabel("Direction: "));
    ButtonGroup dirButtonGroup = new ButtonGroup();
    ItemListener directionListener = e -> {
      if (e.getStateChange() == ItemEvent.SELECTED && comparator != null) {
        setComparator(comparator.reversed());
        sortList();
      }
    };
    directions.forEach(r -> {
      dirButtonGroup.add(r);
      directionBox.add(r);
      r.addItemListener(directionListener);
      r.setEnabled(false);
    });
    directionBox.add(Box.createHorizontalGlue());

    JPanel p = new JPanel(new GridLayout(2, 1));
    p.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 2));
    p.add(sortBox);
    p.add(directionBox);

    add(p, BorderLayout.NORTH);
    add(new JScrollPane(list));
    setPreferredSize(new Dimension(320, 240));
  }

  private void setComparator(Comparator<ListItem> comp) {
    this.comparator = comp;
  }

  private void reverseComparatorIfNeeded() {
    if (descending.isSelected()) {
      comparator = comparator.reversed();
    }
  }

  private void sortList() {
    List<ListItem> selectedItems = list.getSelectedValuesList();
    model.clear();
    if (comparator == null) {
      Stream.of(defaultItems).forEach(model::addElement);
      directions.forEach(r -> r.setEnabled(false));
    } else {
      Stream.of(defaultItems).sorted(comparator).forEach(model::addElement);
      directions.forEach(r -> r.setEnabled(true));
    }
    for (ListItem item : selectedItems) {
      int i = model.indexOf(item);
      list.addSelectionInterval(i, i);
    }
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

class ListItemRenderer<E extends ListItem> implements ListCellRenderer<E> {
  protected static final Color SELECTED_COLOR = new Color(0x40_32_64_FF, true);
  private final JLabel label = new JLabel("", null, SwingConstants.CENTER) {
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
  private final JPanel renderer = new JPanel(new BorderLayout());
  private final Border focusBorder = UIManager.getBorder("List.focusCellHighlightBorder");
  private final Border noFocusBorder; // = UIManager.getBorder("List.noFocusBorder");

  protected ListItemRenderer() {
    Border b = UIManager.getBorder("List.noFocusBorder");
    if (Objects.isNull(b)) { // Nimbus???
      Insets i = focusBorder.getBorderInsets(renderer);
      b = BorderFactory.createEmptyBorder(i.top, i.left, i.bottom, i.right);
    }
    noFocusBorder = b;
    label.setVerticalTextPosition(SwingConstants.BOTTOM);
    label.setHorizontalTextPosition(SwingConstants.CENTER);
    label.setForeground(renderer.getForeground());
    label.setBackground(renderer.getBackground());
    label.setBorder(noFocusBorder);
    label.setOpaque(false);
    renderer.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
    renderer.add(label);
    renderer.setOpaque(true);
  }

  @Override public Component getListCellRendererComponent(JList<? extends E> list, E value, int index, boolean isSelected, boolean cellHasFocus) {
    label.setText(value.getTitle());
    label.setBorder(cellHasFocus ? focusBorder : noFocusBorder);
    label.setIcon(value.getIcon());
    if (isSelected) {
      label.setForeground(list.getSelectionForeground());
      renderer.setBackground(list.getSelectionBackground());
    } else {
      label.setForeground(list.getForeground());
      renderer.setBackground(list.getBackground());
    }
    return renderer;
  }
}

class ListItem {
  private final String title;
  private final Color color;
  private final Icon icon;

  protected ListItem(String title, Color color) {
    this.title = title;
    this.color = color;
    this.icon = new ColorIcon(color);
  }

  public String getTitle() {
    return title;
  }

  public Color getColor() {
    return color;
  }

  // public int getColorCode() {
  //   return color.getRGB();
  // }

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
    g2.setPaint(Color.BLACK);
    g2.drawRect(0, 0, getIconWidth(), getIconHeight());
    g2.dispose();
  }

  @Override public int getIconWidth() {
    return 30;
  }

  @Override public int getIconHeight() {
    return 30;
  }
}

// https://github.com/aterai/java-swing-tips/blob/master/ClearSelection/src/java/example/MainPanel.java
class ClearSelectionListener extends MouseInputAdapter {
  private boolean startOutside;

  private static <E> void clearSelectionAndResetFocus(JList<E> list) {
    list.clearSelection();
    list.getSelectionModel().setAnchorSelectionIndex(-1);
    list.getSelectionModel().setLeadSelectionIndex(-1);
  }

  private static <E> boolean containsPoint(JList<E> list, Point pt) {
    return IntStream.range(0, list.getModel().getSize())
        .mapToObj(i -> list.getCellBounds(i, i))
        .anyMatch(r -> r != null && r.contains(pt));
  }

  @Override public void mousePressed(MouseEvent e) {
    JList<?> list = (JList<?>) e.getComponent();
    startOutside = !containsPoint(list, e.getPoint());
    if (startOutside) {
      clearSelectionAndResetFocus(list);
    }
  }

  @Override public void mouseReleased(MouseEvent e) {
    startOutside = false;
  }

  @Override public void mouseDragged(MouseEvent e) {
    JList<?> list = (JList<?>) e.getComponent();
    if (containsPoint(list, e.getPoint())) {
      startOutside = false;
    } else if (startOutside) {
      clearSelectionAndResetFocus(list);
    }
  }
}
