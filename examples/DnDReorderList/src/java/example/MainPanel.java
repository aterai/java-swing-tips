// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JPanel p = new JPanel(new BorderLayout());
    p.add(new JScrollPane(createList(createModel())));
    p.setBorder(BorderFactory.createTitledBorder("Drag & Drop JList"));
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    setPreferredSize(new Dimension(320, 240));
    add(p);
  }

  private static ListModel<Color> createModel() {
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

  private static JList<Color> createList(ListModel<Color> model) {
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
      }
    };
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
    // return list;
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
  protected static final DataFlavor FLAVOR = new DataFlavor(List.class, "List of items");
  private final List<Integer> indices = new ArrayList<>();
  private int addIndex = -1; // Location where items were added
  private int addCount; // Number of items added.

  @Override protected Transferable createTransferable(JComponent c) {
    JList<?> source = (JList<?>) c;
    indices.clear();
    for (int i : source.getSelectedIndices()) {
      indices.add(i);
    }
    List<?> selectedValues = source.getSelectedValuesList();
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

  @Override public boolean canImport(TransferSupport support) {
    return support.isDataFlavorSupported(FLAVOR);
  }

  @Override public int getSourceActions(JComponent c) {
    return COPY_OR_MOVE;
  }

  private static int getInsertionIndex(TransferSupport support) {
    JList<?> target = (JList<?>) support.getComponent();
    int index;
    DropLocation location = support.isDrop() ? support.getDropLocation() : null;
    if (location instanceof JList.DropLocation) { // Mouse Drag & Drop
      index = ((JList.DropLocation) location).getIndex();
    } else { // Keyboard Copy & Paste
      index = target.getSelectedIndex();
    }
    int max = target.getModel().getSize();
    // If it is out of range, it is appended to the end
    return index < 0 ? max : Math.min(index, max);
  }

  private static List<?> getTransferData(TransferSupport support) {
    List<?> values;
    try {
      values = (List<?>) support.getTransferable().getTransferData(FLAVOR);
    } catch (UnsupportedFlavorException | IOException ex) {
      values = Collections.emptyList();
    }
    return values;
  }

  @SuppressWarnings("unchecked")
  @Override public boolean importData(TransferSupport support) {
    JList<?> target = (JList<?>) support.getComponent();
    DefaultListModel<Object> model = (DefaultListModel<Object>) target.getModel();
    int index = getInsertionIndex(support);
    addIndex = index;
    List<?> values = getTransferData(support);
    for (Object o : values) {
      model.add(index, o);
      target.addSelectionInterval(index, index);
      index++;
    }
    // Only a drop within the same list shifts the indices of the items to be removed in
    // exportDone(...); a cut has already removed them before the paste.
    addCount = support.isDrop() ? values.size() : 0;
    return !values.isEmpty();
  }

  @Override protected void exportDone(JComponent c, Transferable data, int action) {
    cleanup(c, action == MOVE);
  }

  private void cleanup(JComponent c, boolean remove) {
    if (remove && !indices.isEmpty()) {
      // If we are moving items around in the same list, we
      // need to adjust the indices accordingly, since those
      // at or after the insertion point have moved.
      if (addCount > 0) {
        indices.replaceAll(i -> i >= addIndex ? i + addCount : i);
      }
      DefaultListModel<?> model = (DefaultListModel<?>) ((JList<?>) c).getModel();
      // Remove from the end so that the remaining indices stay valid
      for (int i = indices.size() - 1; i >= 0; i--) {
        model.remove(indices.get(i));
      }
    }
    indices.clear();
    addCount = 0;
    addIndex = -1;
  }
}
