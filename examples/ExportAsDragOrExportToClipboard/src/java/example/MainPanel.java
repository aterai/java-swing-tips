// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.InputEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JList<Color> list = makeList(makeModel());
    // // Disable JList item Cut, Copy, Paste
    // ActionMap am = list.getActionMap();
    // Action empty = new AbstractAction() {
    //   @Override public void actionPerformed(ActionEvent e) {
    //     /* do nothing */
    //   }
    // };
    // am.put(TransferHandler.getCutAction().getValue(Action.NAME), empty);
    // am.put(TransferHandler.getCopyAction().getValue(Action.NAME), empty);
    // am.put(TransferHandler.getPasteAction().getValue(Action.NAME), empty);
    add(makeControlBox(list), BorderLayout.NORTH);
    add(new JScrollPane(list));
    setPreferredSize(new Dimension(320, 240));
  }

  private static JPanel makeControlBox(JList<Color> list) {
    JCheckBox check1 = new JCheckBox("canExportAsDrag");
    check1.addActionListener(e -> {
      boolean b = check1.isSelected();
      list.putClientProperty(check1.getText(), b);
    });

    JCheckBox check2 = new JCheckBox("canExportToClipboard");
    check2.addActionListener(e -> {
      boolean b = check2.isSelected();
      list.putClientProperty(check2.getText(), b);
    });

    JCheckBox check3 = new JCheckBox("canImportFromClipboard");
    check3.addActionListener(e -> {
      boolean b = check3.isSelected();
      list.putClientProperty(check3.getText(), b);
    });

    Box box1 = Box.createHorizontalBox();
    box1.add(check1);
    Box box2 = Box.createHorizontalBox();
    box2.add(check2);
    box2.add(check3);
    JPanel p = new JPanel(new BorderLayout());
    p.add(box1, BorderLayout.NORTH);
    p.add(box2, BorderLayout.SOUTH);
    return p;
  }

  private static ListModel<Color> makeModel() {
    DefaultListModel<Color> model = new DefaultListModel<>();
    model.addElement(Color.RED);
    model.addElement(Color.BLUE);
    model.addElement(Color.GREEN);
    model.addElement(Color.CYAN);
    model.addElement(Color.ORANGE);
    model.addElement(Color.PINK);
    model.addElement(Color.MAGENTA);
    return model;
  }

  private static JList<Color> makeList(ListModel<Color> model) {
    return new JList<Color>(model) {
      @Override public void updateUI() {
        setSelectionBackground(null); // Nimbus
        setCellRenderer(null);
        super.updateUI();
        ListCellRenderer<? super Color> renderer = getCellRenderer();
        setCellRenderer((list, value, index, isSelected, cellHasFocus) -> {
          Component c = renderer.getListCellRendererComponent(
              list, value, index, isSelected, cellHasFocus);
          c.setForeground(value);
          return c;
        });
        getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        setDropMode(DropMode.INSERT);
        setDragEnabled(true);
        setTransferHandler(new ListItemTransferHandler());
        setComponentPopupMenu(new ListPopupMenu(this));
      }
    };
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

final class ListPopupMenu extends JPopupMenu {
  private final JMenuItem cut;
  private final JMenuItem copy;

  /* default */ ListPopupMenu(JList<?> list) {
    super();
    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    TransferHandler handler = list.getTransferHandler();
    cut = add("cut");
    cut.addActionListener(e -> handler.exportToClipboard(list, clipboard, TransferHandler.MOVE));
    copy = add("copy");
    copy.addActionListener(e -> handler.exportToClipboard(list, clipboard, TransferHandler.COPY));
    add("paste").addActionListener(e -> handler.importData(list, clipboard.getContents(null)));
    addSeparator();
    add("clearSelection").addActionListener(e -> list.clearSelection());
  }

  @Override public void show(Component c, int x, int y) {
    if (c instanceof JList) {
      boolean isSelected = !((JList<?>) c).isSelectionEmpty();
      cut.setEnabled(isSelected);
      copy.setEnabled(isSelected);
      super.show(c, x, y);
    }
  }
}

// Demo - BasicDnD (The Java™ Tutorials > ... > Drag and Drop and Data Transfer)
// https://docs.oracle.com/javase/tutorial/uiswing/dnd/basicdemo.html
class ListItemTransferHandler extends TransferHandler {
  protected static final DataFlavor FLAVOR = new DataFlavor(List.class, "List of items");
  private JList<?> source;
  private final List<Integer> indices = new ArrayList<>();
  private int addIndex = -1; // Location where items were added
  private int addCount; // Number of items added.

  @Override protected Transferable createTransferable(JComponent c) {
    source = (JList<?>) c;
    for (int i : source.getSelectedIndices()) {
      indices.add(i);
    }
    List<?> selectedValues = source.getSelectedValuesList();
    // return new DataHandler(selectedValues.toArray(new Object[0]), mimeType);
    return new Transferable() {
      @Override public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[] {FLAVOR};
      }

      @Override public boolean isDataFlavorSupported(DataFlavor flavor) {
        return Objects.equals(FLAVOR, flavor);
      }

      @Override public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
        if (isDataFlavorSupported(flavor)) {
          return selectedValues;
        } else {
          throw new UnsupportedFlavorException(flavor);
        }
      }
    };
  }

  // @Override public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
  //   System.out.println("canImport(JComponent, DataFlavor[])");
  //   return super.canImport(comp, transferFlavors);
  // }

  @Override public boolean canImport(TransferSupport info) {
    // System.out.println("canImport(TransferSupport)");
    return info.isDataFlavorSupported(FLAVOR);
  }

  @Override public int getSourceActions(JComponent c) {
    return COPY_OR_MOVE;
  }

  private static int getIndex(TransferSupport info) {
    JList<?> target = (JList<?>) info.getComponent();
    int index; // = dl.getIndex();
    if (info.isDrop()) { // Mouse Drag & Drop
      DropLocation tdl = info.getDropLocation();
      if (tdl instanceof JList.DropLocation) {
        index = ((JList.DropLocation) tdl).getIndex();
      } else {
        index = target.getSelectedIndex();
      }
    } else { // Keyboard Copy & Paste
      index = target.getSelectedIndex();
    }
    DefaultListModel<?> model = (DefaultListModel<?>) target.getModel();
    // boolean insert = dl.isInsert();
    int max = model.getSize();
    // int index = dl.getIndex();
    index = index < 0 ? max : index; // If it is out of range, it is appended to the end
    index = Math.min(index, max);
    return index;
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

  // private static boolean canImportFromClipboard(JComponent c) {
  //   Object o = c.getClientProperty("canImportFromClipboard");
  //   return o != null && Objects.equals(o, Boolean.TRUE);
  // }

  @SuppressWarnings("unchecked")
  private boolean importListData(TransferSupport info, JList<?> target) {
    DefaultListModel<Object> model = (DefaultListModel<Object>) target.getModel();
    int index = getIndex(info);
    addIndex = index;
    List<?> values = getTransferData(info);
    for (Object o : values) {
      int i = index++;
      model.add(i, o);
      target.addSelectionInterval(i, i);
    }
    addCount = info.isDrop() && target.equals(source) ? values.size() : 0;
    target.requestFocusInWindow();
    return !values.isEmpty();
  }

  @Override public boolean importData(TransferSupport info) {
    // System.out.println("importData(TransferSupport)");
    JList<?> target = (JList<?>) info.getComponent();
    Object o = target.getClientProperty("canImportFromClipboard");
    boolean b = o != null && Objects.equals(o, Boolean.TRUE);
    // boolean b = canImportFromClipboard(target);
    return (info.isDrop() || b) && importListData(info, target);
  }

  // @Override public boolean importData(JComponent comp, Transferable t) {
  //   // System.out.println("importData(JComponent, Transferable)");
  //   return importData(new TransferSupport(comp, t));
  // }

  @Override public void exportAsDrag(JComponent comp, InputEvent e, int action) {
    // System.out.println("exportAsDrag");
    Object o = comp.getClientProperty("canExportAsDrag");
    if (Objects.equals(o, Boolean.TRUE)) {
      super.exportAsDrag(comp, e, action);
    }
  }

  @Override public void exportToClipboard(JComponent comp, Clipboard clip, int action) {
    // System.out.println("exportToClipboard");
    Object o = comp.getClientProperty("canExportToClipboard");
    if (Objects.equals(o, Boolean.TRUE)) {
      super.exportToClipboard(comp, clip, action);
    }
  }

  @Override protected void exportDone(JComponent c, Transferable data, int action) {
    // cleanup(c, action == MOVE);
    if (action == MOVE && !indices.isEmpty()) {
      // If we are moving items around in the same list, we
      // need to adjust the indices accordingly, since those
      // after the insertion point have moved.
      if (addCount > 0) {
        for (int i = 0; i < indices.size(); i++) {
          if (indices.get(i) >= addIndex) {
            indices.set(i, indices.get(i) + addCount);
          }
        }
      }
      JList<?> src = (JList<?>) c;
      DefaultListModel<?> model = (DefaultListModel<?>) src.getModel();
      for (int i = indices.size() - 1; i >= 0; i--) {
        model.remove(indices.get(i));
      }
    }
    indices.clear();
    addCount = 0;
    addIndex = -1;
  }

  // private void cleanup(JComponent c, boolean remove) {
  //   if (remove && !indices.isEmpty()) {
  //     // If we are moving items around in the same list, we
  //     // need to adjust the indices accordingly, since those
  //     // after the insertion point have moved.
  //     if (addCount > 0) {
  //       for (int i = 0; i < indices.size(); i++) {
  //         if (indices.get(i) >= addIndex) {
  //           indices.set(i, indices.get(i) + addCount);
  //         }
  //       }
  //     }
  //     JList<?> src = (JList<?>) c;
  //     DefaultListModel<?> model = (DefaultListModel<?>) src.getModel();
  //     for (int i = indices.size() - 1; i >= 0; i--) {
  //       model.remove(indices.get(i));
  //     }
  //   }
  //   indices.clear();
  //   addCount = 0;
  //   addIndex = -1;
  // }
}
