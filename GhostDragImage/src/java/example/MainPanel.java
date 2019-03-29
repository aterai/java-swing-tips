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
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());

    DefaultListModel<ListItem> model = new DefaultListModel<>();
    // [XP Style Icons - Download](https://xp-style-icons.en.softonic.com/)
    model.addElement(new ListItem("asdasdfsd", "wi0009-32.png"));
    model.addElement(new ListItem("12345", "wi0054-32.png"));
    model.addElement(new ListItem("ADFFDF.asd", "wi0062-32.png"));
    model.addElement(new ListItem("test", "wi0063-32.png"));
    model.addElement(new ListItem("32.png", "wi0064-32.png"));
    model.addElement(new ListItem("asdfsd.jpg", "wi0096-32.png"));
    model.addElement(new ListItem("6896", "wi0111-32.png"));
    model.addElement(new ListItem("t467467est", "wi0122-32.png"));
    model.addElement(new ListItem("test123", "wi0124-32.png"));
    model.addElement(new ListItem("test(1)", "wi0126-32.png"));

    ReorderbleList<ListItem> list = new ReorderbleList<>(model);

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

  public static void main(String... args) {
    EventQueue.invokeLater(new Runnable() {
      @Override public void run() {
        createAndShowGui();
      }
    });
  }

  public static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
      ex.printStackTrace();
      Toolkit.getDefaultToolkit().beep();
    }
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

// Demo - BasicDnD (The Java™ Tutorials > Creating a GUI With JFC/Swing > Drag and Drop and Data Transfer)
// https://docs.oracle.com/javase/tutorial/uiswing/dnd/basicdemo.html
class ListItemTransferHandler extends TransferHandler {
  protected final DataFlavor localObjectFlavor;
  protected int[] indices;
  protected int addIndex = -1; // Location where items were added
  protected int addCount; // Number of items added.
  protected static final JLabel LABEL = new JLabel() {
    @Override public Dimension getPreferredSize() {
      Dimension d = super.getPreferredSize();
      d.width = 32;
      return d;
    }
  };

  protected ListItemTransferHandler() {
    super();
    // localObjectFlavor = new ActivationDataFlavor(
    //     Object[].class, DataFlavor.javaJVMLocalObjectMimeType, "Array of items");
    localObjectFlavor = new DataFlavor(List.class, "List of items");

    LABEL.setOpaque(true);
    LABEL.setBorder(BorderFactory.createLineBorder(Color.GRAY));
    LABEL.setHorizontalAlignment(SwingConstants.CENTER);
    LABEL.setForeground(Color.WHITE);
    LABEL.setBackground(new Color(0, 0, 255, 200));
  }

  @Override protected Transferable createTransferable(JComponent c) {
    JList<?> source = (JList<?>) c;
    c.getRootPane().getGlassPane().setVisible(true);
    indices = source.getSelectedIndices();
    List<?> transferedObjects = source.getSelectedValuesList();
    // return new DataHandler(transferedObjects, localObjectFlavor.getMimeType());
    return new Transferable() {
      @Override public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[] {localObjectFlavor};
      }

      @Override public boolean isDataFlavorSupported(DataFlavor flavor) {
        return Objects.equals(localObjectFlavor, flavor);
      }

      @Override public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        if (isDataFlavorSupported(flavor)) {
          return transferedObjects;
        } else {
          throw new UnsupportedFlavorException(flavor);
        }
      }
    };
  }

  @Override public boolean canImport(TransferHandler.TransferSupport info) {
    return info.isDrop() && info.isDataFlavorSupported(localObjectFlavor);
  }

  @Override public int getSourceActions(JComponent c) {
    System.out.println("getSourceActions");
    c.getRootPane().getGlassPane().setCursor(DragSource.DefaultMoveDrop);
    if (c instanceof JList) {
      JList<?> source = (JList<?>) c;
      setDragImage(createDragImage(source));
      // Point pt = c.getMousePosition();
      // if (Objects.nonNull(pt)) {
      //   setDragImageOffset(pt);
      // }
      Optional.ofNullable(c.getMousePosition()).ifPresent(this::setDragImageOffset);
      return TransferHandler.MOVE; // TransferHandler.COPY_OR_MOVE;
    }
    return TransferHandler.NONE;
  }

  private static <E> BufferedImage createDragImage(JList<E> source) {
    int w = source.getWidth();
    int h = source.getHeight();
    BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2 = bi.createGraphics();
    ListCellRenderer<? super E> renderer = source.getCellRenderer();
    for (int i: source.getSelectedIndices()) {
      Component c = renderer.getListCellRendererComponent(source, source.getModel().getElementAt(i), i, false, false);
      Rectangle rect = source.getCellBounds(i, i);
      SwingUtilities.paintComponent(g2, c, source, rect);
    }
    g2.dispose();
    return bi;
  }

  @SuppressWarnings("unchecked")
  @Override public boolean importData(TransferHandler.TransferSupport info) {
    TransferHandler.DropLocation tdl = info.getDropLocation();
    if (!canImport(info) || !(tdl instanceof JList.DropLocation)) {
      return false;
    }
    JList.DropLocation dl = (JList.DropLocation) tdl;
    JList<?> target = (JList<?>) info.getComponent();
    DefaultListModel<Object> listModel = (DefaultListModel<Object>) target.getModel();
    // boolean insert = dl.isInsert();
    int max = listModel.getSize();
    int index = dl.getIndex();
    index = index < 0 ? max : index; // If it is out of range, it is appended to the end
    index = Math.min(index, max);
    addIndex = index;
    try {
      List<?> values = (List<?>) info.getTransferable().getTransferData(localObjectFlavor);
      for (Object o: values) {
        int i = index++;
        listModel.add(i, o);
        target.addSelectionInterval(i, i);
      }
      addCount = values.size();
      return true;
    } catch (UnsupportedFlavorException | IOException ex) {
      return false;
    }
  }

  @Override protected void exportDone(JComponent c, Transferable data, int action) {
    System.out.println("exportDone");
    Component glassPane = c.getRootPane().getGlassPane();
    // glassPane.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    glassPane.setVisible(false);
    cleanup(c, action == TransferHandler.MOVE);
  }

  private void cleanup(JComponent c, boolean remove) {
    if (remove && Objects.nonNull(indices)) {
      // If we are moving items around in the same list, we
      // need to adjust the indices accordingly, since those
      // after the insertion point have moved.
      if (addCount > 0) {
        for (int i = 0; i < indices.length; i++) {
          if (indices[i] >= addIndex) {
            indices[i] += addCount;
          }
        }
      }
      JList<?> source = (JList<?>) c;
      DefaultListModel<?> model = (DefaultListModel<?>) source.getModel();
      for (int i = indices.length - 1; i >= 0; i--) {
        model.remove(indices[i]);
      }
    }
    indices = null;
    addCount = 0;
    addIndex = -1;
  }
}

class CompactListItemTransferHandler extends ListItemTransferHandler {
  @Override public int getSourceActions(JComponent c) {
    System.out.println("getSourceActions");
    Component glassPane = c.getRootPane().getGlassPane();
    glassPane.setCursor(DragSource.DefaultMoveDrop);
    if (!(c instanceof JList)) {
      return TransferHandler.NONE;
    }
    JList<?> source = (JList<?>) c;
    int w = source.getFixedCellWidth();
    int h = source.getFixedCellHeight() - 20; // TODO
    setDragImage(createCompactDragImage(source, w, h));
    setDragImageOffset(new Point(w / 2, h));
    return TransferHandler.MOVE; // TransferHandler.COPY_OR_MOVE;
  }

  private static <E> BufferedImage createCompactDragImage(JList<E> source, int w, int h) {
    if (w <= 0 || h <= 0) {
      throw new IllegalArgumentException("width and height must be > 0");
    }
    int[] selectedIndices = source.getSelectedIndices();
    BufferedImage br = source.getGraphicsConfiguration().createCompatibleImage(w, h, Transparency.TRANSLUCENT);
    Graphics2D g2 = br.createGraphics();
    ListCellRenderer<? super E> renderer = source.getCellRenderer();
    int idx = selectedIndices[0];
    E valueAt = source.getModel().getElementAt(idx);
    Component c = renderer.getListCellRendererComponent(source, valueAt, idx, false, false);
    Rectangle rect = source.getCellBounds(idx, idx);
    SwingUtilities.paintComponent(g2, c, source, 0, 0, rect.width, rect.height);
    int selectedCount = selectedIndices.length;
    boolean isMoreThanOneItemSelected = selectedCount > 1;
    if (isMoreThanOneItemSelected) {
      LABEL.setText(Objects.toString(selectedCount));
      Dimension d = LABEL.getPreferredSize();
      SwingUtilities.paintComponent(g2, LABEL, source, (w - d.width) / 2, (h - d.height) / 2, d.width, d.height);
    }
    g2.dispose();
    br.coerceData(true);
    return br;
  }
}
