// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetContext;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.dnd.InvalidDnDOperationException;
import java.io.IOException;
import java.util.stream.IntStream;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTabbedPane tabs = new JTabbedPane();
    tabs.addTab("Tab:000", new JScrollPane(makeList(0)));
    tabs.addTab("Tab:001", new JScrollPane(makeList(1)));
    tabs.addTab("Tab:002", new JScrollPane(makeList(2)));
    add(tabs);
    new DropTarget(tabs, DnDConstants.ACTION_MOVE, new TabTitleDropTargetListener(), true);
    setPreferredSize(new Dimension(320, 240));
  }

  private static JList<String> makeList(int index) {
    DefaultListModel<String> model = new DefaultListModel<>();
    model.addElement(index + " - 1111");
    model.addElement(index + " - 22222222");
    model.addElement(index + " - 333333333333");
    model.addElement(index + " - ----------");
    model.addElement(index + " - +++++++++++++++");
    model.addElement(index + " - ****");
    return new DnDList<>(model);
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

class DnDList<E> extends JList<E> implements DragGestureListener, Transferable {
  private static final String NAME = "test";

  protected DnDList() {
    this(null);
  }

  protected DnDList(ListModel<E> model) {
    super(model);
    initDragGestureRecognizer();
  }

  private void initDragGestureRecognizer() {
    DragSource.getDefaultDragSource().createDefaultDragGestureRecognizer(
        this, DnDConstants.ACTION_MOVE, this);
  }

  // Interface: DragGestureListener
  @Override public void dragGestureRecognized(DragGestureEvent e) {
    try {
      e.startDrag(DragSource.DefaultMoveDrop, this, new ListDragSourceListener());
    } catch (InvalidDnDOperationException ex) {
      throw new IllegalStateException(ex);
    }
  }

  // Interface: Transferable
  // DataFlavor FLAVOR = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType, NAME);
  // DataFlavor FLAVOR = new DataFlavor(Object.class, DataFlavor.javaJVMLocalObjectMimeType);
  @Override public Object getTransferData(DataFlavor flavor) {
    return this;
  }

  @Override public DataFlavor[] getTransferDataFlavors() {
    return new DataFlavor[] {new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType, NAME)};
  }

  @Override public boolean isDataFlavorSupported(DataFlavor flavor) {
    return NAME.equals(flavor.getHumanPresentableName());
  }
}

class TabTitleDropTargetListener implements DropTargetListener {
  private int targetTabIndex = -1;

  @Override public void dropActionChanged(DropTargetDragEvent e) {
    /* not needed */
  }

  @Override public void dragExit(DropTargetEvent e) {
    /* not needed */
  }

  @Override public void dragEnter(DropTargetDragEvent e) {
    /* not needed */
  }

  @Override public void dragOver(DropTargetDragEvent e) {
    targetTabIndex = -1;
    Transferable t = e.getTransferable();
    boolean supported = t.isDataFlavorSupported(t.getTransferDataFlavors()[0]);
    if (supported && notOwnTab(e)) {
      e.acceptDrag(e.getDropAction());
    } else {
      e.rejectDrag();
    }
    e.getDropTargetContext().getComponent().repaint();
  }

  @SuppressWarnings("unchecked")
  @Override public void drop(DropTargetDropEvent e) {
    try {
      DropTargetContext c = e.getDropTargetContext();
      Component o = c.getComponent();
      Transferable t = e.getTransferable();
      DataFlavor[] f = t.getTransferDataFlavors();

      if (o instanceof JTabbedPane) {
        JTabbedPane tabbedPane = (JTabbedPane) o;
        JScrollPane sp = (JScrollPane) tabbedPane.getComponentAt(targetTabIndex);
        JViewport vp = sp.getViewport();
        JList<String> targetList = (JList<String>) SwingUtilities.getUnwrappedView(vp);
        JList<String> sourceList = (JList<String>) t.getTransferData(f[0]);

        DefaultListModel<String> tm = (DefaultListModel<String>) targetList.getModel();
        DefaultListModel<String> sm = (DefaultListModel<String>) sourceList.getModel();

        int[] indices = sourceList.getSelectedIndices();
        for (int j = indices.length - 1; j >= 0; j--) {
          tm.addElement(sm.remove(indices[j]));
        }
        e.dropComplete(true);
      } else {
        e.dropComplete(false);
      }
    } catch (UnsupportedFlavorException | IOException ex) {
      e.dropComplete(false);
    }
  }

  private boolean notOwnTab(DropTargetDragEvent e) {
    Point pt = e.getLocation();
    Component c = e.getDropTargetContext().getComponent();
    return c instanceof JTabbedPane && notOwnTab((JTabbedPane) c, pt);
  }

  private boolean notOwnTab(JTabbedPane tabbedPane, Point pt) {
    targetTabIndex = IntStream.range(0, tabbedPane.getTabCount())
      .filter(i -> tabbedPane.getBoundsAt(i).contains(pt))
      .findFirst()
      .orElse(-1);
    return targetTabIndex >= 0 && targetTabIndex != tabbedPane.getSelectedIndex();
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
