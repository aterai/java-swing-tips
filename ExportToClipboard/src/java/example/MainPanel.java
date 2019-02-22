// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JPanel p = new JPanel(new GridLayout(1, 2, 10, 0));
    TransferHandler h = new ListItemTransferHandler();
    p.setBorder(BorderFactory.createTitledBorder("Drag & Drop(Copy, Cut, Paste) between JLists"));
    p.add(new JScrollPane(makeList(h)));
    p.add(new JScrollPane(makeList(h)));
    add(p);
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    setPreferredSize(new Dimension(320, 240));
  }

  private static JList<Color> makeList(TransferHandler handler) {
    DefaultListModel<Color> listModel = new DefaultListModel<>();
    listModel.addElement(Color.RED);
    listModel.addElement(Color.BLUE);
    listModel.addElement(Color.GREEN);
    listModel.addElement(Color.CYAN);
    listModel.addElement(Color.ORANGE);
    listModel.addElement(Color.PINK);
    listModel.addElement(Color.MAGENTA);
    JList<Color> list = new JList<>(listModel);
    list.setCellRenderer(new DefaultListCellRenderer() {
      @Override public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        ((JLabel) c).setForeground((Color) value);
        return c;
      }
    });
    list.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    list.setDropMode(DropMode.INSERT);
    list.setDragEnabled(true);
    list.setTransferHandler(handler);

    list.setComponentPopupMenu(new ListPopupMenu(list));

    return list;
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
    }
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

class ListPopupMenu extends JPopupMenu {
  private final JMenuItem cutItem;
  private final JMenuItem copyItem;

  protected ListPopupMenu(JList<?> list) {
    super();
    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    TransferHandler handler = list.getTransferHandler();
    cutItem = add("cut");
    cutItem.addActionListener(e -> handler.exportToClipboard(list, clipboard, TransferHandler.MOVE));
    copyItem = add("copy");
    copyItem.addActionListener(e -> handler.exportToClipboard(list, clipboard, TransferHandler.COPY));
    add("paste").addActionListener(e -> handler.importData(list, clipboard.getContents(null)));
    addSeparator();
    add("clearSelection").addActionListener(e -> list.clearSelection());
  }

  @Override public void show(Component c, int x, int y) {
    if (c instanceof JList) {
      boolean isSelected = !((JList<?>) c).isSelectionEmpty();
      cutItem.setEnabled(isSelected);
      copyItem.setEnabled(isSelected);
      super.show(c, x, y);
    }
  }
}

// Demo - BasicDnD (The Java™ Tutorials > Creating a GUI With JFC/Swing > Drag and Drop and Data Transfer)
// https://docs.oracle.com/javase/tutorial/uiswing/dnd/basicdemo.html
class ListItemTransferHandler extends TransferHandler {
  protected final DataFlavor localObjectFlavor;
  protected JList<?> source;
  protected int[] indices;
  protected int addIndex = -1; // Location where items were added
  protected int addCount; // Number of items added.

  protected ListItemTransferHandler() {
    super();
    // localObjectFlavor = new ActivationDataFlavor(
    //     Object[].class, DataFlavor.javaJVMLocalObjectMimeType, "Array of items");
    // localObjectFlavor = new DataFlavor(Object[].class, "Array of items");
    localObjectFlavor = new DataFlavor(List.class, "List of items");
  }

  @Override protected Transferable createTransferable(JComponent c) {
    source = (JList<?>) c;
    indices = source.getSelectedIndices();
    // return new DataHandler(transferedObjects, localObjectFlavor.getMimeType());
    // Object[] transferedObjects = source.getSelectedValuesList().toArray(new Object[0]);
    List<?> transferedObjects = source.getSelectedValuesList();
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
    return info.isDataFlavorSupported(localObjectFlavor);
  }

  @Override public int getSourceActions(JComponent c) {
    return TransferHandler.COPY_OR_MOVE;
  }

  private static int getIndex(TransferHandler.TransferSupport info) {
    JList<?> target = (JList<?>) info.getComponent();
    int index; // = dl.getIndex();
    if (info.isDrop()) { // Mouse Drag & Drop
      System.out.println("Mouse Drag & Drop");
      TransferHandler.DropLocation tdl = info.getDropLocation();
      if (tdl instanceof JList.DropLocation) {
        index = ((JList.DropLocation) tdl).getIndex();
      } else {
        index = target.getSelectedIndex();
      }
    } else { // Keyboard Copy & Paste
      index = target.getSelectedIndex();
    }
    DefaultListModel<?> listModel = (DefaultListModel<?>) target.getModel();
    // boolean insert = dl.isInsert();
    int max = listModel.getSize();
    // int index = dl.getIndex();
    index = index < 0 ? max : index; // If it is out of range, it is appended to the end
    index = Math.min(index, max);
    return index;
  }

  @SuppressWarnings("unchecked")
  @Override public boolean importData(TransferHandler.TransferSupport info) {
    if (!canImport(info)) {
      return false;
    }
    JList<?> target = (JList<?>) info.getComponent();
    DefaultListModel<Object> listModel = (DefaultListModel<Object>) target.getModel();
    int index = getIndex(info);
    addIndex = index;
    try {
      // Object[] values = (Object[]) info.getTransferable().getTransferData(localObjectFlavor);
      List<?> values = (List<?>) info.getTransferable().getTransferData(localObjectFlavor);
      for (Object o: values) {
        int i = index++;
        listModel.add(i, o);
        target.addSelectionInterval(i, i);
      }
      addCount = target.equals(source) ? values.size() : 0;
      return true;
    } catch (UnsupportedFlavorException | IOException ex) {
      return false;
    }
  }

  @Override public boolean importData(JComponent comp, Transferable t) {
    return importData(new TransferHandler.TransferSupport(comp, t));
  }

  @Override protected void exportDone(JComponent c, Transferable data, int action) {
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
      JList<?> src = (JList<?>) c;
      DefaultListModel<?> model = (DefaultListModel<?>) src.getModel();
      for (int i = indices.length - 1; i >= 0; i--) {
        model.remove(indices[i]);
      }
    }
    indices = null;
    addCount = 0;
    addIndex = -1;
  }
}
