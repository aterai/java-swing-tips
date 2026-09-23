// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DragSource;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    DefaultListModel<ListItem> model = new DefaultListModel<>();
    // [XP Style Icons - Download](https://xp-style-icons.en.softonic.com/)
    model.addElement(new ListItem("wi0009-32", "example/wi0009-32.png"));
    model.addElement(new ListItem("12345", "example/wi0054-32.png"));
    model.addElement(new ListItem("wi0062-32.png", "example/wi0062-32.png"));
    model.addElement(new ListItem("test", "example/wi0063-32.png"));
    model.addElement(new ListItem("32.png", "example/wi0064-32.png"));
    model.addElement(new ListItem("wi0096-32.png", "example/wi0096-32.png"));
    model.addElement(new ListItem("6896", "example/wi0111-32.png"));
    model.addElement(new ListItem("t467467est", "example/wi0122-32.png"));
    model.addElement(new ListItem("test123", "example/wi0124-32.png"));
    model.addElement(new ListItem("test(1)", "example/wi0126-32.png"));
    ReorderableList<ListItem> list = new ReorderableList<>(model);

    JCheckBox check = new JCheckBox("Compact drag image mode") {
      @Override public void updateUI() {
        super.updateUI();
        setSelected(false);
      }
    };
    check.addActionListener(e -> {
      if (((JCheckBox) e.getSource()).isSelected()) {
        list.setTransferHandler(new CompactListItemTransferHandler());
      } else {
        list.setTransferHandler(new ListItemTransferHandler());
      }
    });

    add(check, BorderLayout.NORTH);
    add(new JScrollPane(list));
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

// Demo - BasicDnD (The Java™ Tutorials > ... > Drag and Drop and Data Transfer)
// https://docs.oracle.com/javase/tutorial/uiswing/dnd/basicdemo.html
class ListItemTransferHandler extends TransferHandler {
  private static final DataFlavor FLAVOR = new DataFlavor(List.class, "List of items");
  private final List<Integer> indices = new ArrayList<>();
  private int addIndex = -1; // Location where items were added
  private int addCount; // Number of items added.

  @Override protected Transferable createTransferable(JComponent c) {
    JList<?> source = (JList<?>) c;
    c.getRootPane().getGlassPane().setVisible(true);
    for (int i : source.getSelectedIndices()) {
      indices.add(i);
    }
    return new Transferable() {
      @Override public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[] {FLAVOR};
      }

      @Override public boolean isDataFlavorSupported(DataFlavor flavor) {
        return Objects.equals(FLAVOR, flavor);
      }

      @Override public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
        if (isDataFlavorSupported(flavor)) {
          return source.getSelectedValuesList();
        } else {
          throw new UnsupportedFlavorException(flavor);
        }
      }
    };
  }

  @Override public boolean canImport(TransferSupport info) {
    return info.isDrop() && info.isDataFlavorSupported(FLAVOR);
  }

  @Override public int getSourceActions(JComponent c) {
    int action = NONE;
    if (c instanceof JList && !((JList<?>) c).isSelectionEmpty()) {
      c.getRootPane().getGlassPane().setCursor(DragSource.DefaultMoveDrop);
      updateDragImage((JList<?>) c);
      action = MOVE;
    }
    return action;
  }

  protected void updateDragImage(JList<?> src) {
    Rectangle bounds = getSelectedCellsBounds(src);
    setDragImage(createDragImage(src, bounds));
    Point pt = Optional.ofNullable(src.getMousePosition()).orElseGet(bounds::getLocation);
    pt.translate(-bounds.x, -bounds.y);
    setDragImageOffset(pt);
  }

  private static Rectangle getSelectedCellsBounds(JList<?> list) {
    return Arrays.stream(list.getSelectedIndices())
        .mapToObj(i -> list.getCellBounds(i, i))
        .reduce(Rectangle::union)
        .orElseGet(Rectangle::new);
  }

  private static <E> BufferedImage createDragImage(JList<E> list, Rectangle bounds) {
    BufferedImage bi = new BufferedImage(bounds.width, bounds.height, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2 = bi.createGraphics();
    g2.translate(-bounds.x, -bounds.y);
    ListCellRenderer<? super E> renderer = list.getCellRenderer();
    for (int i : list.getSelectedIndices()) {
      E value = list.getModel().getElementAt(i);
      Component c = renderer.getListCellRendererComponent(list, value, i, false, false);
      SwingUtilities.paintComponent(g2, c, list, list.getCellBounds(i, i));
    }
    g2.dispose();
    return bi;
  }

  private static int getIndex(TransferSupport info) {
    JList<?> target = (JList<?>) info.getComponent();
    int index;
    DropLocation dl = info.isDrop() ? info.getDropLocation() : null;
    if (dl instanceof JList.DropLocation) { // Mouse Drag & Drop
      index = ((JList.DropLocation) dl).getIndex();
    } else { // Keyboard Copy & Paste
      index = target.getSelectedIndex();
    }
    int max = target.getModel().getSize();
    // If it is out of range, it is appended to the end
    return index < 0 ? max : Math.min(index, max);
  }

  private static List<?> getTransferData(TransferSupport info) {
    List<?> values;
    try {
      values = (List<?>) info.getTransferable().getTransferData(FLAVOR);
    } catch (UnsupportedFlavorException | IOException ex) {
      values = Collections.emptyList();
    }
    return values;
  }

  @SuppressWarnings("unchecked")
  @Override public boolean importData(TransferSupport info) {
    JList<?> target = (JList<?>) info.getComponent();
    DefaultListModel<Object> model = (DefaultListModel<Object>) target.getModel();
    int index = getIndex(info);
    addIndex = index;
    List<?> values = getTransferData(info);
    for (Object o : values) {
      model.add(index, o);
      target.addSelectionInterval(index, index);
      index += 1;
    }
    addCount = info.isDrop() ? values.size() : 0;
    return !values.isEmpty();
  }

  @Override protected void exportDone(JComponent c, Transferable data, int action) {
    c.getRootPane().getGlassPane().setVisible(false);
    cleanup(c, action == MOVE);
  }

  private void cleanup(JComponent c, boolean remove) {
    if (remove && !indices.isEmpty()) {
      // If we are moving items around in the same list, we
      // need to adjust the indices accordingly, since those
      // after the insertion point have moved.
      if (addCount > 0) {
        indices.replaceAll(i -> i >= addIndex ? i + addCount : i);
      }
      DefaultListModel<?> model = (DefaultListModel<?>) ((JList<?>) c).getModel();
      for (int i = indices.size() - 1; i >= 0; i--) {
        model.remove(indices.get(i));
      }
    }
    indices.clear();
    addCount = 0;
    addIndex = -1;
  }
}

class CompactListItemTransferHandler extends ListItemTransferHandler {
  private static final JLabel COUNT_LABEL = createCountLabel();

  @Override protected void updateDragImage(JList<?> src) {
    // Cut off the title at the bottom of the cell and use only the icon area:
    // title text height + title border(top: 2, bottom: 2) + cell border(bottom: 2)
    int titleHeight = src.getFontMetrics(src.getFont()).getHeight() + 6;
    int w = src.getFixedCellWidth();
    int h = src.getFixedCellHeight() - titleHeight;
    setDragImage(createCompactDragImage(src, w, h));
    setDragImageOffset(new Point(w / 2, h));
  }

  private static JLabel createCountLabel() {
    JLabel label = new JLabel() {
      @Override public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        d.width = 32;
        return d;
      }
    };
    label.setOpaque(true);
    label.setBorder(BorderFactory.createLineBorder(Color.GRAY));
    label.setHorizontalAlignment(SwingConstants.CENTER);
    label.setForeground(Color.WHITE);
    label.setBackground(new Color(0xC8_00_00_FF, true));
    return label;
  }

  private static <E> BufferedImage createCompactDragImage(JList<E> list, int w, int h) {
    if (w <= 0 || h <= 0) {
      throw new IllegalArgumentException("width and height must be > 0");
    }
    GraphicsConfiguration gc = list.getGraphicsConfiguration();
    BufferedImage image = gc.createCompatibleImage(w, h, Transparency.TRANSLUCENT);
    Graphics2D g2 = image.createGraphics();
    int idx = list.getMinSelectionIndex();
    E value = list.getModel().getElementAt(idx);
    ListCellRenderer<? super E> renderer = list.getCellRenderer();
    Component c = renderer.getListCellRendererComponent(list, value, idx, false, false);
    Rectangle rect = list.getCellBounds(idx, idx);
    SwingUtilities.paintComponent(g2, c, list, 0, 0, rect.width, rect.height);
    if (idx != list.getMaxSelectionIndex()) { // two or more items are selected
      COUNT_LABEL.setText(Integer.toString(list.getSelectedIndices().length));
      Dimension d = COUNT_LABEL.getPreferredSize();
      int x = (w - d.width) / 2;
      int y = (h - d.height) / 2;
      SwingUtilities.paintComponent(g2, COUNT_LABEL, list, x, y, d.width, d.height);
    }
    g2.dispose();
    image.coerceData(true);
    return image;
  }
}
