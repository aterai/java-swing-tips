// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DragSource;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import java.util.TooManyListenersException;
import java.util.logging.Logger;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javax.swing.*;
import javax.swing.plaf.metal.MetalTabbedPaneUI;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    DnDTabbedPane sub = new DnDTabbedPane();
    sub.addTab("Title aa", new JLabel("aaa"));
    sub.addTab("Title bb", new JScrollPane(new JTree()));
    sub.addTab("Title cc", new JScrollPane(new JTextArea("JTextArea cc")));

    DnDTabbedPane tabbedPane = new DnDTabbedPane();
    tabbedPane.addTab("JTree 00", new JScrollPane(new JTree()));
    tabbedPane.addTab("JLabel 01", new JLabel("Test"));
    tabbedPane.addTab("JTable 02", new JScrollPane(new JTable(10, 3)));
    tabbedPane.addTab("JTextArea 03", new JScrollPane(new JTextArea("JTextArea 03")));
    tabbedPane.addTab("JLabel 04", new JLabel("<html>11111111<br>1234567890"));
    tabbedPane.addTab("null 05", null);
    tabbedPane.addTab("JTabbedPane 06", sub);
    tabbedPane.addTab("Title 000000000000000007", new JScrollPane(new JTree()));

    // // ButtonTabComponent
    // for (int i = 0; i < tabbedPane.getTabCount(); i++) {
    //   tabbedPane.setTabComponentAt(i, new ButtonTabComponent(tabbedPane));
    //   tabbedPane.setToolTipTextAt(i, "tooltip: " + i);
    // }

    DnDTabbedPane sub2 = new DnDTabbedPane();
    sub2.addTab("Title aaa", new JLabel("aaa"));
    sub2.addTab("Title bbb", new JScrollPane(new JTree()));
    sub2.addTab("Title ccc", new JScrollPane(new JTextArea("JTextArea ccc")));

    tabbedPane.setName("JTabbedPane#main");
    sub.setName("JTabbedPane#sub1");
    sub2.setName("JTabbedPane#sub2");

    DropTargetListener listener = new TabDropTargetAdapter();
    TransferHandler handler = new TabTransferHandler();
    Stream.of(tabbedPane, sub, sub2).forEach(t -> {
      t.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
      t.setTransferHandler(handler);
      try {
        t.getDropTarget().addDropTargetListener(listener);
      } catch (TooManyListenersException ex) {
        // ex.printStackTrace();
        UIManager.getLookAndFeel().provideErrorFeedback(t);
      }
    });

    JPanel p = new JPanel(new GridLayout(2, 1));
    p.add(tabbedPane);
    p.add(sub2);
    add(p);
    add(createCheckBoxPanel(tabbedPane), BorderLayout.NORTH);
    setPreferredSize(new Dimension(320, 240));
  }

  private static Component createCheckBoxPanel(JTabbedPane tabs) {
    JCheckBox tc = new JCheckBox("Top", true);
    tc.addActionListener(e -> tabs.setTabPlacement(
        tc.isSelected() ? SwingConstants.TOP : SwingConstants.RIGHT));
    JCheckBox sc = new JCheckBox("SCROLL_TAB_LAYOUT", true);
    sc.addActionListener(e -> tabs.setTabLayoutPolicy(
        sc.isSelected() ? JTabbedPane.SCROLL_TAB_LAYOUT : JTabbedPane.WRAP_TAB_LAYOUT));
    JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
    p.add(tc);
    p.add(sc);
    return p;
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

class DnDTabbedPane extends JTabbedPane {
  private static final int SCROLL_SZ = 20; // Test
  private static final int BUTTON_SZ = 30; // XXX 30 is magic number of scroll button size
  // private final DropMode dropMode = DropMode.INSERT;
  private int dragTabIndex = -1;
  private transient DnDTabbedPane.DropLocation dropLocation;
  private transient Handler handler;

  public static final class DropLocation extends TransferHandler.DropLocation {
    private final int index;
    // public boolean canDrop = true; // index >= 0;

    private DropLocation(Point p, int index) {
      super(p);
      this.index = index;
    }

    public int getIndex() {
      return index;
    }

    // @Override public String toString() {
    //   return getClass().getName()
    //     + "[dropPoint=" + getDropPoint() + ","
    //     + "index=" + index + ","
    //     + "insert=" + isInsert + "]";
    // }
  }

  private void clickArrowButton(String actionKey) {
    JButton forwardButton = null;
    JButton backwardButton = null;
    for (Component c : getComponents()) {
      if (c instanceof JButton) {
        if (Objects.isNull(forwardButton)) {
          forwardButton = (JButton) c;
        } else if (Objects.isNull(backwardButton)) {
          backwardButton = (JButton) c;
        }
      }
    }
    JButton button = "scrollTabsForwardAction".equals(actionKey)
        ? forwardButton
        : backwardButton;
    Optional.ofNullable(button).filter(JButton::isEnabled).ifPresent(JButton::doClick);

    // // ArrayIndexOutOfBoundsException
    // Optional.ofNullable(getActionMap())
    //   .map(am -> am.get(actionKey))
    //   .filter(Action::isEnabled)
    //   .ifPresent(a -> a.actionPerformed(
    //       new ActionEvent(this, ACTION_PERFORMED, null, 0, 0)));
    // // ActionMap map = getActionMap();
    // // if (Objects.nonNull(map)) {
    // //   Action action = map.get(actionKey);
    // //   if (Objects.nonNull(action) && action.isEnabled()) {
    // //     action.actionPerformed(new ActionEvent(this, ACTION_PERFORMED, null, 0, 0));
    // //   }
    // // }
  }

  public void autoScrollTest(Point pt) {
    Rectangle r = getTabAreaBounds();
    // int tabPlacement = getTabPlacement();
    // if (tabPlacement == TOP || tabPlacement == BOTTOM) {
    int arrowBoxSize = SCROLL_SZ + BUTTON_SZ;
    Rectangle backward = new Rectangle();
    Rectangle forward = new Rectangle();
    if (isTopBottomTabPlacement(getTabPlacement())) {
      backward.setBounds(r.x, r.y, SCROLL_SZ, r.height);
      forward.setBounds(r.x + r.width - arrowBoxSize, r.y, arrowBoxSize, r.height);
    } else { // if (tabPlacement == LEFT || tabPlacement == RIGHT) {
      backward.setBounds(r.x, r.y, r.width, SCROLL_SZ);
      forward.setBounds(r.x, r.y + r.height - arrowBoxSize, r.width, arrowBoxSize);
    }
    if (backward.contains(pt)) {
      clickArrowButton("scrollTabsBackwardAction");
    } else if (forward.contains(pt)) {
      clickArrowButton("scrollTabsForwardAction");
    }
  }

  @Override public void updateUI() {
    removeMouseListener(handler);
    removeMouseMotionListener(handler);
    removePropertyChangeListener(handler);
    super.updateUI();
    handler = new Handler();
    addMouseListener(handler);
    addMouseMotionListener(handler);
    addPropertyChangeListener(handler);
  }

  public int getDragTabIndex() {
    return dragTabIndex;
  }

  // Test whether the point is in the first half of the tab:
  // the left half for TOP/BOTTOM, the upper half for LEFT/RIGHT.
  private boolean isFirstHalf(Rectangle r, Point pt) {
    return isTopBottomTabPlacement(getTabPlacement())
        ? pt.getX() <= r.getCenterX()
        : pt.getY() <= r.getCenterY();
  }

  // @Override TransferHandler.DropLocation dropLocationForPoint(Point p) {
  public DnDTabbedPane.DropLocation tabDropLocationForPoint(Point p) {
    // assert dropMode == DropMode.INSERT : "Unexpected drop mode";
    int count = getTabCount();
    // findFirst() is short-circuiting, so map(...) is evaluated
    // only for the first tab that contains the point.
    int idx = IntStream.range(0, count)
        .filter(i -> getBoundsAt(i).contains(p))
        .map(i -> isFirstHalf(getBoundsAt(i), p) ? i : i + 1)
        .findFirst()
        .orElse(count == 0 ? -1 : count);
    // int idx = IntStream.range(0, count)
    //     .filter(i -> getBoundsAt(i).contains(p))
    //     .findFirst()
    //     .orElseGet(() -> getTabAreaBounds().contains(p) ? count : -1);
    return new DnDTabbedPane.DropLocation(p, idx);
    // switch (dropMode) {
    //   case INSERT:
    //     for (int i = 0; i < getTabCount(); i++) {
    //       if (getBoundsAt(i).contains(p)) {
    //         return new DnDTabbedPane.DropLocation(p, i);
    //       }
    //     }
    //     if (getTabAreaBounds().contains(p)) {
    //       return new DnDTabbedPane.DropLocation(p, getTabCount());
    //     }
    //     break;
    //   case USE_SELECTION:
    //   case ON:
    //   case ON_OR_INSERT:
    //   default:
    //     assert false : "Unexpected drop mode";
    //     break;
    // }
    // return new DnDTabbedPane.DropLocation(p, -1);
  }

  public final DnDTabbedPane.DropLocation getDropLocation() {
    return dropLocation;
  }

  // // WARNING:
  // // The method DnDTabbedPane.setDropLocation(TransferHandler.DropLocation, Object, boolean)
  // // does not override the inherited method from JComponent since
  // // it is private to a different package
  // @Override Object setDropLocation(DropLocation location, Object state, boolean forDrop) {
  //   DropLocation old = dropLocation;
  //   if (Objects.isNull(location) || !forDrop) {
  //     dropLocation = new DnDTabbedPane.DropLocation(new Point(), -1);
  //   } else if (location instanceof DropLocation) {
  //     dropLocation = (DropLocation) location;
  //   }
  //   firePropertyChange("dropLocation", old, dropLocation);
  //   return null;
  // }

  // Object updateTabDropLocation(DropLocation location, Object state, boolean forDrop) {
  public void updateTabDropLocation(DnDTabbedPane.DropLocation location, boolean forDrop) {
    DnDTabbedPane.DropLocation old = dropLocation;
    if (Objects.isNull(location) || !forDrop) {
      dropLocation = new DnDTabbedPane.DropLocation(new Point(), -1);
    } else {
      dropLocation = location;
    }
    firePropertyChange("dropLocation", old, dropLocation);
    // return state;
  }

  public void exportTab(int dragIndex, JTabbedPane target, int targetIndex) {
    // System.out.println("exportTab");
    final Component cmp = getComponentAt(dragIndex);
    final String title = getTitleAt(dragIndex);
    final Icon icon = getIconAt(dragIndex);
    final String toolTipText = getToolTipTextAt(dragIndex);
    final boolean isEnabled = isEnabledAt(dragIndex);
    final Component tab = getTabComponentAt(dragIndex);
    // // ButtonTabComponent
    // if (tab instanceof ButtonTabComponent) {
    //   tab = new ButtonTabComponent(target);
    // }

    remove(dragIndex);
    target.insertTab(title, icon, cmp, toolTipText, targetIndex);
    target.setEnabledAt(targetIndex, isEnabled);
    target.setTabComponentAt(targetIndex, tab);
    target.setSelectedIndex(targetIndex);
    if (tab instanceof JComponent) {
      ((JComponent) tab).scrollRectToVisible(tab.getBounds());
    }
  }

  public void convertTab(int prev, int next) {
    // System.out.println("convertTab");
    // if (next < 0 || prev == next) {
    //   return;
    // }
    final Component cmp = getComponentAt(prev);
    final Component tab = getTabComponentAt(prev);
    final String title = getTitleAt(prev);
    final Icon icon = getIconAt(prev);
    final String tip = getToolTipTextAt(prev);
    final boolean isEnabled = isEnabledAt(prev);
    int tgtIndex = prev > next ? next : next - 1;
    remove(prev);
    insertTab(title, icon, cmp, tip, tgtIndex);
    setEnabledAt(tgtIndex, isEnabled);
    // When you drag and drop a disabled tab, it finishes enabled and selected.
    // pointed out by dlorde
    if (isEnabled) {
      setSelectedIndex(tgtIndex);
    }
    // I have a component in all tabs (JLabel with an X to close the tab)
    // and when I move a tab the component disappear.
    // pointed out by Daniel Dario Morales Salas
    setTabComponentAt(tgtIndex, tab);
  }

  // public Rectangle getTabAreaBounds() {
  //   Rectangle tabbedRect = getBounds();
  //   Component c = getSelectedComponent();
  //   if (Objects.isNull(c)) {
  //     return tabbedRect;
  //   }
  //   int xx = tabbedRect.x;
  //   int yy = tabbedRect.y;
  //   Rectangle compRect = getSelectedComponent().getBounds();
  //   int tabPlacement = getTabPlacement();
  //   if (tabPlacement == TOP) {
  //     tabbedRect.height -= compRect.height;
  //   } else if (tabPlacement == BOTTOM) {
  //     tabbedRect.y += compRect.y + compRect.height;
  //     tabbedRect.height -= compRect.height;
  //   } else if (tabPlacement == LEFT) {
  //     tabbedRect.width -= compRect.width;
  //   } else { // if (tabPlacement == RIGHT) {
  //     tabbedRect.x += compRect.x + compRect.width;
  //     tabbedRect.width -= compRect.width;
  //   }
  //   tabbedRect.translate(-xx, -yy);
  //   // tabbedRect.grow(2, 2);
  //   return tabbedRect;
  // }

  public Rectangle getTabAreaBounds() {
    // Start from the component coordinate system
    // instead of getBounds() + translate(-x, -y).
    Rectangle tabbedRect = new Rectangle(getSize());
    Rectangle compRect = Optional.ofNullable(getSelectedComponent())
        .map(Component::getBounds)
        .orElseGet(Rectangle::new);
    int tabPlacement = getTabPlacement();
    if (isTopBottomTabPlacement(tabPlacement)) {
      tabbedRect.height -= compRect.height;
      if (tabPlacement == BOTTOM) {
        tabbedRect.y += compRect.y + compRect.height;
      }
    } else {
      tabbedRect.width -= compRect.width;
      if (tabPlacement == RIGHT) {
        tabbedRect.x += compRect.x + compRect.width;
      }
    }
    // tabbedRect.grow(2, 2);
    return tabbedRect;
  }

  public static boolean isTopBottomTabPlacement(int tabPlacement) {
    return tabPlacement == TOP || tabPlacement == BOTTOM;
  }

  private final class Handler extends MouseAdapter implements PropertyChangeListener {
    private final Point startPt = new Point(-1, -1);
    private final int dragThreshold = DragSource.getDragThreshold();
    // Toolkit tk = Toolkit.getDefaultToolkit();
    // Integer dragThreshold = (Integer) tk.getDesktopProperty("DnD.gestureMotionThreshold");

    // PropertyChangeListener
    @Override public void propertyChange(PropertyChangeEvent e) {
      if (Objects.equals("dropLocation", e.getPropertyName())) {
        // repaintDropLocation();
        Optional.ofNullable(getRootPane())
            .map(JRootPane::getGlassPane)
            .filter(GhostGlassPane.class::isInstance)
            .map(GhostGlassPane.class::cast)
            .ifPresent(glassPane -> {
              glassPane.setTargetTabbedPane(DnDTabbedPane.this);
              glassPane.repaint();
            });
      }
    }

    // MouseListener
    @Override public void mousePressed(MouseEvent e) {
      DnDTabbedPane src = (DnDTabbedPane) e.getComponent();
      boolean isOnlyOneTab = src.getTabCount() <= 1;
      if (isOnlyOneTab) {
        startPt.setLocation(-1, -1);
      } else {
        Point tabPt = e.getPoint(); // e.getDragOrigin();
        int i = src.indexAtLocation(tabPt.x, tabPt.y);
        // disabled tab, null component problem.
        // pointed out by daryl. NullPointerException: i.e. addTab("Tab", null)
        boolean b = i < 0 || !src.isEnabledAt(i) || src.getComponentAt(i) == null;
        startPt.setLocation(b ? new Point(-1, -1) : tabPt);
      }
    }

    @Override public void mouseDragged(MouseEvent e) {
      Point tabPt = e.getPoint(); // e.getDragOrigin();
      if (startPt.x >= 0 && startPt.distance(tabPt) > dragThreshold) {
        DnDTabbedPane src = (DnDTabbedPane) e.getComponent();
        TransferHandler th = src.getTransferHandler();
        // When a tab runs rotation occurs, a tab that is not the target is dragged.
        // pointed out by Arjen
        int idx = src.indexAtLocation(startPt.x, startPt.y);
        int selIdx = src.getSelectedIndex();
        boolean isWrap = src.getTabLayoutPolicy() == WRAP_TAB_LAYOUT;
        boolean isRotate = !(src.getUI() instanceof MetalTabbedPaneUI) && idx != selIdx;
        dragTabIndex = isWrap && isRotate ? selIdx : idx;
        th.exportAsDrag(src, e, TransferHandler.MOVE);
        src.getRootPane().getGlassPane().setVisible(true);
        src.updateTabDropLocation(new DnDTabbedPane.DropLocation(tabPt, -1), true);
        startPt.setLocation(-1, -1);
      }
    }
  }
}

class TabDropTargetAdapter extends DropTargetAdapter {
  private void clearDropLocationPaint(Component c) {
    if (c instanceof DnDTabbedPane) {
      DnDTabbedPane t = (DnDTabbedPane) c;
      t.updateTabDropLocation(null, false);
      t.setCursor(Cursor.getDefaultCursor());
    }
  }

  @Override public void drop(DropTargetDropEvent e) {
    Component c = e.getDropTargetContext().getComponent();
    // System.out.println("DropTargetListener#drop: " + c.getName());
    clearDropLocationPaint(c);
  }

  @Override public void dragExit(DropTargetEvent e) {
    Component c = e.getDropTargetContext().getComponent();
    // System.out.println("DropTargetListener#dragExit: " + c.getName());
    clearDropLocationPaint(c);
  }

  // @Override public void dragEnter(DropTargetDragEvent e) {
  //   Component c = e.getDropTargetContext().getComponent();
  //   System.out.println("DropTargetListener#dragEnter: " + c.getName());
  // }

  // @Override public void dragOver(DropTargetDragEvent e) {
  //   // System.out.println("dragOver");
  // }

  // @Override public void dropActionChanged(DropTargetDragEvent e) {
  //   System.out.println("dropActionChanged");
  // }
}

class DnDTabData {
  private final DnDTabbedPane tabbedPane;

  protected DnDTabData(DnDTabbedPane tabbedPane) {
    this.tabbedPane = tabbedPane;
  }

  public DnDTabbedPane getTabbedPane() {
    return tabbedPane;
  }
}

class TabTransferHandler extends TransferHandler {
  private final DataFlavor localObjectFlavor = new DataFlavor(DnDTabData.class, "DnDTabData");
  private DnDTabbedPane source;

  // protected TabTransferHandler() {
  //   super();
  //   System.out.println("TabTransferHandler");
  //   localObjectFlavor = new ActivationDataFlavor(
  //       DnDTabbedPane.class, DataFlavor.javaJVMLocalObjectMimeType, "DnDTabbedPane");
  // }

  @Override protected Transferable createTransferable(JComponent c) {
    // System.out.println("createTransferable");
    if (c instanceof DnDTabbedPane) {
      source = (DnDTabbedPane) c;
    }
    // return new DataHandler(c, localObjectFlavor.getMimeType());
    return new Transferable() {
      @Override public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[] {localObjectFlavor};
      }

      @Override public boolean isDataFlavorSupported(DataFlavor flavor) {
        return Objects.equals(localObjectFlavor, flavor);
      }

      @Override public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
        if (isDataFlavorSupported(flavor)) {
          return new DnDTabData(source);
        } else {
          throw new UnsupportedFlavorException(flavor);
        }
      }
    };
  }

  @Override public boolean canImport(TransferSupport support) {
    // System.out.println("canImport");
    // if (!support.isDrop() || !support.isDataFlavorSupported(localObjectFlavor)) {
    //   // boolean b = support.isDataFlavorSupported(localObjectFlavor);
    //   // System.out.println("canImport:" + support.isDrop() + " " + b);
    //   return false;
    // }
    support.setDropAction(MOVE);
    DropLocation tdl = support.getDropLocation();
    Point pt = tdl.getDropPoint();
    DnDTabbedPane target = (DnDTabbedPane) support.getComponent();
    target.autoScrollTest(pt);
    DnDTabbedPane.DropLocation dl = target.tabDropLocationForPoint(pt);
    int idx = dl.getIndex();

    // if (!isWebStart()) {
    //   // System.out.println("local");
    //   try {
    //     src = (DnDTabbedPane) support.getTransferable().getTransferData(localObjectFlavor);
    //   } catch (Exception ex) {
    //     ex.printStackTrace();
    //   }
    // }

    boolean canDrop = false;
    if (support.isDrop() && support.isDataFlavorSupported(localObjectFlavor)) {
      boolean inArea = target.getTabAreaBounds().contains(pt) && idx >= 0;
      if (target.equals(source)) {
        int dragTabIndex = target.getDragTabIndex();
        canDrop = inArea && idx != dragTabIndex && idx != dragTabIndex + 1;
      } else {
        canDrop = Optional.ofNullable(source)
            .map(c -> !c.isAncestorOf(target))
            .orElse(false) && inArea;
      }
    }

    // [JDK-6700748]
    // Cursor flickering during D&D when using CellRendererPane with validation
    // https://bugs.openjdk.org/browse/JDK-6700748
    Cursor cursor = canDrop ? DragSource.DefaultMoveDrop : DragSource.DefaultMoveNoDrop;
    Component glassPane = target.getRootPane().getGlassPane();
    glassPane.setCursor(cursor);
    target.setCursor(cursor);

    support.setShowDropLocation(canDrop);
    target.updateTabDropLocation(dl, canDrop);
    return canDrop;
  }

  // private static boolean isWebStart() {
  //   try {
  //     ServiceManager.lookup("javax.jnlp.BasicService");
  //     return true;
  //   } catch (UnavailableServiceException ex) {
  //     return false;
  //   }
  // }

  private BufferedImage createDragTabImage(DnDTabbedPane tabs) {
    Rectangle rect = tabs.getBoundsAt(tabs.getDragTabIndex())
        .intersection(new Rectangle(tabs.getSize()));
    int w = Math.max(1, rect.width);
    int h = Math.max(1, rect.height);
    BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2 = img.createGraphics();
    // The clip of this Graphics2D is the tab bounds, so the children
    // outside of it are skipped and no subimage is needed.
    g2.translate(-rect.x, -rect.y);
    tabs.paint(g2);
    g2.dispose();
    return img;
  }

  @Override public int getSourceActions(JComponent c) {
    // System.out.println("getSourceActions");
    int action = NONE;
    if (c instanceof DnDTabbedPane) {
      DnDTabbedPane src = (DnDTabbedPane) c;
      if (src.getDragTabIndex() >= 0) {
        c.getRootPane().setGlassPane(new GhostGlassPane(src));
        setDragImage(createDragTabImage(src));
        c.getRootPane().getGlassPane().setVisible(true);
        action = MOVE;
      }
    }
    return action;
  }

  @Override public boolean importData(TransferSupport support) {
    // System.out.println("importData");
    DnDTabbedPane target = (DnDTabbedPane) support.getComponent();
    DnDTabbedPane.DropLocation dl = target.getDropLocation();
    return getTransferData(support, localObjectFlavor).map(data -> {
      DnDTabbedPane src = data.getTabbedPane();
      int index = dl.getIndex(); // boolean insert = dl.isInsert();
      int dragTabIndex = src.getDragTabIndex();
      if (target.equals(src)) {
        src.convertTab(dragTabIndex, index); // getTargetTabIndex(e.getLocation()));
      } else {
        src.exportTab(dragTabIndex, target, index);
      }
      return true;
    }).orElse(false);
  }

  private static Optional<DnDTabData> getTransferData(
      TransferSupport support, DataFlavor flavor) {
    Optional<DnDTabData> data;
    try {
      Object o = support.getTransferable().getTransferData(flavor);
      data = Optional.of(o).filter(DnDTabData.class::isInstance).map(DnDTabData.class::cast);
    } catch (UnsupportedFlavorException | IOException ex) {
      data = Optional.empty();
    }
    return data;
  }

  @Override protected void exportDone(JComponent c, Transferable data, int action) {
    // System.out.println("exportDone");
    DnDTabbedPane src = (DnDTabbedPane) c;
    src.getRootPane().getGlassPane().setVisible(false);
    src.updateTabDropLocation(null, false);
    src.repaint();
    src.setCursor(Cursor.getDefaultCursor());
  }
}

class GhostGlassPane extends JComponent {
  private static final int LINE_SZ = 3;
  private DnDTabbedPane tabbedPane;

  protected GhostGlassPane(DnDTabbedPane tabbedPane) {
    super();
    this.tabbedPane = tabbedPane;
  }

  @Override public void updateUI() {
    super.updateUI();
    setOpaque(false);
  }

  public void setTargetTabbedPane(DnDTabbedPane tab) {
    tabbedPane = tab;
  }

  private static Optional<Rectangle> getDropLineRect(DnDTabbedPane tabs) {
    return Optional.ofNullable(tabs.getDropLocation())
        // .filter(DnDTabbedPane.DropLocation::canDrop)
        .map(DnDTabbedPane.DropLocation::getIndex)
        .filter(index -> index >= 0)
        .map(index -> {
          int a = Math.min(index, 1); // index == 0 ? 0 : 1;
          Rectangle r = tabs.getBoundsAt(Math.max(index - 1, 0));
          return DnDTabbedPane.isTopBottomTabPlacement(tabs.getTabPlacement())
              ? new Rectangle(r.x - LINE_SZ / 2 + r.width * a, r.y, LINE_SZ, r.height)
              : new Rectangle(r.x, r.y - LINE_SZ / 2 + r.height * a, r.width, LINE_SZ);
        })
        .filter(rect -> !rect.isEmpty());
  }

  @Override protected void paintComponent(Graphics g) {
    getDropLineRect(tabbedPane).ifPresent(rect -> {
      Graphics2D g2 = (Graphics2D) g.create();
      Rectangle r = SwingUtilities.convertRectangle(tabbedPane, rect, this);
      g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .5f));
      g2.setPaint(Color.RED);
      g2.fill(r);
      g2.dispose();
    });
  }
}

/* a closeable tab test
// How to Use Tabbed Panes (The Java™ Tutorials > ... > Using Swing Components)
// https://docs.oracle.com/javase/tutorial/uiswing/components/tabbedpane.html
class ButtonTabComponent extends JPanel {
  private final JTabbedPane tabbedPane;

  protected ButtonTabComponent(JTabbedPane tabbedPane) {
    super(new FlowLayout(FlowLayout.LEFT, 0, 0));
    this.tabbedPane = Objects.requireNonNull(tabbedPane, "TabbedPane cannot be null");
    JLabel label = new JLabel() {
      @Override public String getText() {
        String txt = null;
        int i = tabbedPane.indexOfTabComponent(ButtonTabComponent.this);
        if (i != -1) {
          txt = tabbedPane.getTitleAt(i);
        }
        return txt;
      }

      @Override public Icon getIcon() {
        Icon icn = null;
        int i = tabbedPane.indexOfTabComponent(ButtonTabComponent.this);
        if (i != -1) {
          icn = tabbedPane.getIconAt(i);
        }
        return icn;
      }
    };
    label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));

    JButton button = new TabButton();
    TabButtonHandler handler = new TabButtonHandler();
    button.addActionListener(handler);
    button.addMouseListener(handler);

    add(label);
    add(button);
  }

  @Override public final Component add(Component comp) {
    return super.add(comp);
  }

  @Override public void updateUI() {
    super.updateUI();
    setOpaque(false);
    setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
  }

  private final class TabButtonHandler extends MouseAdapter implements ActionListener {
    @Override public void actionPerformed(ActionEvent e) {
      int i = tabbedPane.indexOfTabComponent(ButtonTabComponent.this);
      if (i != -1) {
        tabbedPane.remove(i);
      }
    }

    @Override public void mouseEntered(MouseEvent e) {
      Component component = e.getComponent();
      if (component instanceof AbstractButton) {
        AbstractButton button = (AbstractButton) component;
        button.setBorderPainted(true);
      }
    }

    @Override public void mouseExited(MouseEvent e) {
      Component component = e.getComponent();
      if (component instanceof AbstractButton) {
        AbstractButton button = (AbstractButton) component;
        button.setBorderPainted(false);
      }
    }
  }
}

final class TabButton extends JButton {
  private static final int SIZE = 17;
  private static final int DELTA = 6;

  @Override public void updateUI() {
    // we don't want to update UI for this button
    // super.updateUI();
    setUI(new BasicButtonUI());
    setToolTipText("close this tab");
    setContentAreaFilled(false);
    setFocusable(false);
    setBorder(BorderFactory.createEtchedBorder());
    setBorderPainted(false);
    setRolloverEnabled(true);
  }

  @Override public Dimension getPreferredSize() {
    return new Dimension(SIZE, SIZE);
  }

  @Override protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setStroke(new BasicStroke(2));
    g2.setPaint(Color.BLACK);
    if (getModel().isRollover()) {
      g2.setPaint(Color.ORANGE);
    }
    if (getModel().isPressed()) {
      g2.setPaint(Color.BLUE);
    }
    g2.drawLine(DELTA, DELTA, getWidth() - DELTA - 1, getHeight() - DELTA - 1);
    g2.drawLine(getWidth() - DELTA - 1, DELTA, DELTA, getHeight() - DELTA - 1);
    g2.dispose();
  }
}
*/
