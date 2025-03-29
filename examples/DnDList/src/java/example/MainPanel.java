// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.dnd.InvalidDnDOperationException;
import java.util.logging.Logger;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    add(new JScrollPane(makeList()));
    setPreferredSize(new Dimension(320, 240));
  }

  private static JList<String> makeList() {
    DefaultListModel<String> model = new DefaultListModel<>();
    model.addElement("1111");
    model.addElement("22222222");
    model.addElement("333333333333");
    model.addElement("<<<<<<---->>>>>>");
    model.addElement("============");
    model.addElement("****");

    JList<String> list = new DnDList<>();
    list.setModel(model);
    return list;
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

class DnDList<E> extends JList<E> implements DragGestureListener {
  private static final Color LINE_COLOR = new Color(0x64_64_FF);
  private static final String NAME = "test";
  private static final String MIME_TYPE = DataFlavor.javaJVMLocalObjectMimeType;
  private static final DataFlavor FLAVOR = new DataFlavor(MIME_TYPE, NAME);
  private static final Color EVEN_BGC = new Color(0xF0_F0_F0);
  protected int draggedIndex = -1;
  protected int targetIndex = -1;
  protected final transient Transferable transferable = new Transferable() {
    @Override public Object getTransferData(DataFlavor flavor) {
      return this;
    }

    @Override public DataFlavor[] getTransferDataFlavors() {
      return new DataFlavor[] {FLAVOR};
    }

    @Override public boolean isDataFlavorSupported(DataFlavor flavor) {
      return NAME.equals(flavor.getHumanPresentableName());
    }
  };
  private final Rectangle targetLine = new Rectangle();

  protected DnDList() {
    super();
    new DropTarget(this, DnDConstants.ACTION_COPY_OR_MOVE, new ItemDropTargetListener(), true);
    DragSource.getDefaultDragSource().createDefaultDragGestureRecognizer(
        this, DnDConstants.ACTION_COPY_OR_MOVE, this);
  }

  @Override public void updateUI() {
    setCellRenderer(null);
    super.updateUI();
    ListCellRenderer<? super E> renderer = getCellRenderer();
    setCellRenderer((list, value, index, isSelected, cellHasFocus) -> {
      Component c = renderer.getListCellRendererComponent(
          list, value, index, isSelected, cellHasFocus);
      updateColor(list, c, index, isSelected);
      return c;
    });
  }

  private static void updateColor(JList<?> list, Component c, int i, boolean selected) {
    if (selected) {
      c.setForeground(list.getSelectionForeground());
      c.setBackground(list.getSelectionBackground());
    } else {
      c.setForeground(list.getForeground());
      c.setBackground(i % 2 == 0 ? EVEN_BGC : list.getBackground());
    }
  }

  @Override protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    if (targetIndex >= 0) {
      Graphics2D g2 = (Graphics2D) g.create();
      g2.setPaint(LINE_COLOR);
      g2.fill(targetLine);
      g2.dispose();
    }
  }

  protected void initTargetLine(Point p) {
    Rectangle rect = getCellBounds(0, 0);
    int cellHeight = rect.height;
    int lineHeight = 2;
    int modelSize = getModel().getSize();
    targetIndex = -1;
    targetLine.setSize(rect.width, lineHeight);
    for (int i = 0; i < modelSize; i++) {
      rect.setLocation(0, cellHeight * i - cellHeight / 2);
      if (rect.contains(p)) {
        targetIndex = i;
        targetLine.setLocation(0, i * cellHeight);
        break;
      }
    }
    if (targetIndex < 0) {
      targetIndex = modelSize;
      targetLine.setLocation(0, targetIndex * cellHeight - lineHeight);
    }
  }

  // Interface: DragGestureListener
  @Override public void dragGestureRecognized(DragGestureEvent e) {
    boolean oneOrMore = getSelectedIndices().length > 1;
    draggedIndex = locationToIndex(e.getDragOrigin());
    if (oneOrMore || draggedIndex < 0) {
      return;
    }
    try {
      e.startDrag(DragSource.DefaultMoveDrop, transferable, new ListDragSourceListener());
    } catch (InvalidDnDOperationException ex) {
      throw new IllegalStateException(ex);
    }
  }

  private final class ItemDropTargetListener implements DropTargetListener {
    // DropTargetListener interface
    @Override public void dragExit(DropTargetEvent e) {
      targetIndex = -1;
      repaint();
    }

    @Override public void dragEnter(DropTargetDragEvent e) {
      if (isDragAcceptable(e)) {
        e.acceptDrag(e.getDropAction());
      } else {
        e.rejectDrag();
      }
    }

    @Override public void dragOver(DropTargetDragEvent e) {
      if (isDragAcceptable(e)) {
        e.acceptDrag(e.getDropAction());
      } else {
        e.rejectDrag();
        return;
      }
      initTargetLine(e.getLocation());
      repaint();
    }

    @Override public void dropActionChanged(DropTargetDragEvent e) {
      // if (isDragAcceptable(e)) {
      //   e.acceptDrag(e.getDropAction());
      // } else {
      //   e.rejectDrag();
      // }
    }

    @Override public void drop(DropTargetDropEvent e) {
      DefaultListModel<E> model = (DefaultListModel<E>) getModel();
      if (isDropAcceptable(e) && targetIndex >= 0) {
        E str = model.get(draggedIndex);
        if (targetIndex == draggedIndex) {
          setSelectedIndex(targetIndex);
        } else if (targetIndex < draggedIndex) {
          model.remove(draggedIndex);
          model.add(targetIndex, str);
          setSelectedIndex(targetIndex);
        } else {
          model.add(targetIndex, str);
          model.remove(draggedIndex);
          setSelectedIndex(targetIndex - 1);
        }
        e.dropComplete(true);
      } else {
        e.dropComplete(false);
      }
      e.dropComplete(false);
      targetIndex = -1;
      repaint();
    }

    private boolean isDragAcceptable(DropTargetDragEvent e) {
      DataFlavor[] flavors = e.getCurrentDataFlavors();
      return transferable.isDataFlavorSupported(flavors[0]);
    }

    private boolean isDropAcceptable(DropTargetDropEvent e) {
      DataFlavor[] flavors = e.getTransferable().getTransferDataFlavors();
      return transferable.isDataFlavorSupported(flavors[0]);
    }
  }
}

class ListDragSourceListener implements DragSourceListener {
  @Override public void dragEnter(DragSourceDragEvent e) {
    e.getDragSourceContext().setCursor(DragSource.DefaultMoveDrop);
  }

  @Override public void dragExit(DragSourceEvent e) {
    e.getDragSourceContext().setCursor(DragSource.DefaultMoveNoDrop);
  }

  @Override public void dragOver(DragSourceDragEvent e) {
    /* not needed */
  }

  @Override public void dropActionChanged(DragSourceDragEvent e) {
    /* not needed */
  }

  @Override public void dragDropEnd(DragSourceDropEvent e) {
    /* not needed */
  }
}
