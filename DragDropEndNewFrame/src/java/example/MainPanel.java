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
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.IntStream;
import javax.swing.*;
import javax.swing.plaf.metal.MetalTabbedPaneUI;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    DnDTabbedPane sub = new DnDTabbedPane();
    sub.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
    sub.addTab("Title aa", new JLabel("aaa"));
    sub.addTab("Title bb", new JScrollPane(new JTree()));
    sub.addTab("Title cc", new JScrollPane(new JTextArea("123412341234\n46746745\n245342\n")));

    DnDTabbedPane tab = new DnDTabbedPane();
    tab.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
    tab.addTab("JTree 00", new JScrollPane(new JTree()));
    tab.addTab("JLabel 01", new JLabel("Test"));
    tab.addTab("JTable 02", new JScrollPane(new JTable(20, 3)));
    tab.addTab("JTextArea 03", new JScrollPane(new JTextArea("111111111\n2222222222\n")));
    tab.addTab("JLabel 04", new JLabel("<html>33333333333<br>13412341234123446745"));
    tab.addTab("null 05", null);
    tab.addTab("JTabbedPane 06", sub);
    tab.addTab("Title 000000000000000007", new JScrollPane(new JTree()));

    add(makeCheckBoxPanel(tab), BorderLayout.NORTH);
    add(tab);
    setPreferredSize(new Dimension(320, 240));
  }

  private static Component makeCheckBoxPanel(DnDTabbedPane tabs) {
    JCheckBox check1 = new JCheckBox("Tab Ghost", true);
    check1.addActionListener(e -> tabs.hasGhost = check1.isSelected());

    JCheckBox check2 = new JCheckBox("Top", true);
    check2.addActionListener(e -> {
      boolean f = ((JCheckBox) e.getSource()).isSelected();
      tabs.setTabPlacement(f ? SwingConstants.TOP : SwingConstants.RIGHT);
    });

    JCheckBox check3 = new JCheckBox("SCROLL_TAB_LAYOUT", true);
    check3.addActionListener(e -> {
      boolean f = ((JCheckBox) e.getSource()).isSelected();
      tabs.setTabLayoutPolicy(f ? JTabbedPane.SCROLL_TAB_LAYOUT : JTabbedPane.WRAP_TAB_LAYOUT);
    });

    JCheckBox check4 = new JCheckBox("Debug Paint", true);
    check4.addActionListener(e -> tabs.isPaintScrollArea = check4.isSelected());

    JPanel p1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
    p1.add(check1);
    p1.add(check2);

    JPanel p2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
    p2.add(check3);
    p2.add(check4);

    JPanel p = new JPanel(new BorderLayout());
    p.add(p1, BorderLayout.NORTH);
    p.add(p2, BorderLayout.SOUTH);
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

class DnDTabbedPane extends JTabbedPane {
  private static final int LINE_SIZE = 3;
  private static final int RWH = 20;
  private static final int BUTTON_SIZE = 30; // XXX 30 is magic number of scroll button size
  protected int dragTabIndex = -1;
  // For Debug: >>>
  protected boolean hasGhost = true;
  protected boolean isPaintScrollArea = true;
  // <<<
  protected Rectangle rectBackward = new Rectangle();
  protected Rectangle rectForward = new Rectangle();
  private final GhostGlassPane glassPane = new GhostGlassPane(this);

  protected DnDTabbedPane() {
    super();
    glassPane.setName("GlassPane");
    new DropTarget(
        glassPane, DnDConstants.ACTION_COPY_OR_MOVE, new TabDropTargetListener(), true);
    DragSource.getDefaultDragSource().createDefaultDragGestureRecognizer(
        this, DnDConstants.ACTION_COPY_OR_MOVE, new TabDragGestureListener());
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
    JButton b = "scrollTabsForwardAction".equals(actionKey) ? forwardButton : backwardButton;
    Optional.ofNullable(b)
        .filter(JButton::isEnabled)
        .ifPresent(JButton::doClick);
  }

  public void autoScrollTest(Point glassPt) {
    Rectangle r = getTabAreaBounds();
    if (isTopBottomTabPlacement(getTabPlacement())) {
      rectBackward.setBounds(r.x, r.y, RWH, r.height);
      rectForward.setBounds(r.x + r.width - RWH - BUTTON_SIZE, r.y, RWH + BUTTON_SIZE, r.height);
    } else {
      rectBackward.setBounds(r.x, r.y, r.width, RWH);
      rectForward.setBounds(r.x, r.y + r.height - RWH - BUTTON_SIZE, r.width, RWH + BUTTON_SIZE);
    }
    rectBackward = SwingUtilities.convertRectangle(getParent(), rectBackward, glassPane);
    rectForward = SwingUtilities.convertRectangle(getParent(), rectForward, glassPane);
    if (rectBackward.contains(glassPt)) {
      clickArrowButton("scrollTabsBackwardAction");
    } else if (rectForward.contains(glassPt)) {
      clickArrowButton("scrollTabsForwardAction");
    }
  }

  protected int getTargetTabIndex(Point glassPt) {
    Point tabPt = SwingUtilities.convertPoint(glassPane, glassPt, this);
    Point d = isTopBottomTabPlacement(getTabPlacement()) ? new Point(1, 0) : new Point(0, 1);
    return IntStream.range(0, getTabCount()).filter(i -> {
      Rectangle r = getBoundsAt(i);
      r.translate(-r.width * d.x / 2, -r.height * d.y / 2);
      return r.contains(tabPt);
    }).findFirst().orElseGet(() -> {
      int count = getTabCount();
      Rectangle r = getBoundsAt(count - 1);
      r.translate(r.width * d.x / 2, r.height * d.y / 2);
      return r.contains(tabPt) ? count : -1;
    });
  }

  protected void convertTab(int prev, int next) {
    if (next < 0 || prev == next) {
      return;
    }
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
    if (isEnabled) {
      setSelectedIndex(tgtIndex);
    }
    setTabComponentAt(tgtIndex, tab);
  }

  protected void initTargetLine(int next) {
    boolean isSideNeighbor = next < 0 || dragTabIndex == next || next - dragTabIndex == 1;
    if (isSideNeighbor) {
      glassPane.setTargetRect(0, 0, 0, 0);
      return;
    }
    Optional.ofNullable(getBoundsAt(Math.max(0, next - 1))).ifPresent(boundsRect -> {
      final Rectangle r = SwingUtilities.convertRectangle(this, boundsRect, glassPane);
      int a = Math.min(next, 1);
      if (isTopBottomTabPlacement(getTabPlacement())) {
        glassPane.setTargetRect(r.x + r.width * a - LINE_SIZE / 2, r.y, LINE_SIZE, r.height);
      } else {
        glassPane.setTargetRect(r.x, r.y + r.height * a - LINE_SIZE / 2, r.width, LINE_SIZE);
      }
    });
  }

  protected void initGlassPane(Point tabPt) {
    getRootPane().setGlassPane(glassPane);
    if (hasGhost) {
      Component c = Optional.ofNullable(getTabComponentAt(dragTabIndex))
          .orElseGet(() -> new JLabel(getTitleAt(dragTabIndex)));
      Dimension d = c.getPreferredSize();
      BufferedImage image = new BufferedImage(d.width, d.height, BufferedImage.TYPE_INT_ARGB);
      Graphics2D g2 = image.createGraphics();
      SwingUtilities.paintComponent(g2, c, glassPane, 0, 0, d.width, d.height);
      g2.dispose();
      glassPane.setImage(image);
    }
    Point glassPt = SwingUtilities.convertPoint(this, tabPt, glassPane);
    glassPane.setPoint(glassPt);
    glassPane.setVisible(true);
  }

  protected Rectangle getTabAreaBounds() {
    Rectangle tabbedRect = getBounds();
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
    tabbedRect.grow(2, 2);
    return tabbedRect;
  }

  public static boolean isTopBottomTabPlacement(int tabPlacement) {
    return tabPlacement == TOP || tabPlacement == BOTTOM;
  }
}

class TabTransferable implements Transferable {
  private static final String NAME = "test";
  private final Component tabbedPane;

  protected TabTransferable(Component tabbedPane) {
    this.tabbedPane = tabbedPane;
  }

  @Override public Object getTransferData(DataFlavor flavor) {
    DataFlavor[] flavors = getTransferDataFlavors();
    return flavor.equals(flavors[0]) ? tabbedPane : Collections.<File>emptyList();
  }

  @Override public DataFlavor[] getTransferDataFlavors() {
    return new DataFlavor[] {
        new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType, NAME),
        DataFlavor.javaFileListFlavor
    };
  }

  @Override public boolean isDataFlavorSupported(DataFlavor flavor) {
    return Arrays.asList(getTransferDataFlavors()).contains(flavor);
  }
}

class TabDragSourceListener implements DragSourceListener {
  @Override public void dragEnter(DragSourceDragEvent e) {
    e.getDragSourceContext().setCursor(DragSource.DefaultMoveDrop);
  }

  @Override public void dragExit(DragSourceEvent e) {
    e.getDragSourceContext().setCursor(DragSource.DefaultMoveNoDrop);
  }

  @Override public void dragOver(DragSourceDragEvent e) {
    /* not needed */
  }

  @Override public void dragDropEnd(DragSourceDropEvent e) {
    Component c = e.getDragSourceContext().getComponent();
    JRootPane root = ((JComponent) c).getRootPane();
    Class<GhostGlassPane> clz = GhostGlassPane.class;
    Optional.ofNullable(root.getGlassPane())
        .filter(clz::isInstance).map(clz::cast)
        .ifPresent(p -> p.setVisible(false));
    boolean dropSuccess = e.getDropSuccess();
    Window w = SwingUtilities.getWindowAncestor(c);
    boolean outOfFrame = !w.getBounds().contains(e.getLocation());
    if (dropSuccess && outOfFrame && c instanceof DnDTabbedPane) {
      DnDTabbedPane tabs = makeDnDTabbedPane((DnDTabbedPane) c);
      JFrame frame = new JFrame();
      frame.getContentPane().add(tabs);
      frame.setSize(320, 240);
      frame.setLocation(e.getLocation());
      frame.setVisible(true);
    }
  }

  private static DnDTabbedPane makeDnDTabbedPane(DnDTabbedPane src) {
    int index = src.dragTabIndex;
    final Component cmp = src.getComponentAt(index);
    final Component tab = src.getTabComponentAt(index);
    final String title = src.getTitleAt(index);
    final Icon icon = src.getIconAt(index);
    final String tip = src.getToolTipTextAt(index);
    src.remove(index);
    DnDTabbedPane tabs = new DnDTabbedPane();
    tabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
    tabs.addTab(title, icon, cmp, tip);
    tabs.setTabComponentAt(0, tab);
    return tabs;
  }

  @Override public void dropActionChanged(DragSourceDragEvent e) {
    /* not needed */
  }
}

class TabDragGestureListener implements DragGestureListener {
  private final DragSourceListener handler = new TabDragSourceListener();

  @Override public void dragGestureRecognized(DragGestureEvent e) {
    Optional.ofNullable(e.getComponent())
        .filter(DnDTabbedPane.class::isInstance)
        .map(DnDTabbedPane.class::cast)
        .filter(t -> t.getTabCount() > 1)
        .ifPresent(t -> startDrag(e, t));
  }

  private void startDrag(DragGestureEvent e, DnDTabbedPane tabs) {
    Point tabPt = e.getDragOrigin();
    int idx = tabs.indexAtLocation(tabPt.x, tabPt.y);
    int selIdx = tabs.getSelectedIndex();
    boolean isTabRunsRotated = !(tabs.getUI() instanceof MetalTabbedPaneUI)
        && tabs.getTabLayoutPolicy() == JTabbedPane.WRAP_TAB_LAYOUT
        && idx != selIdx;
    tabs.dragTabIndex = isTabRunsRotated ? selIdx : idx;
    if (tabs.dragTabIndex >= 0 && tabs.isEnabledAt(tabs.dragTabIndex)) {
      tabs.initGlassPane(tabPt);
      try {
        e.startDrag(DragSource.DefaultMoveDrop, new TabTransferable(tabs), handler);
      } catch (InvalidDnDOperationException ex) {
        throw new IllegalStateException(ex);
      }
    }
  }
}

class TabDropTargetListener implements DropTargetListener {
  private static final Point HIDDEN_POINT = new Point(0, -1000);

  private static Optional<GhostGlassPane> getGhostGlassPane(Component c) {
    Class<GhostGlassPane> clz = GhostGlassPane.class;
    return Optional.ofNullable(c).filter(clz::isInstance).map(clz::cast);
  }

  @Override public void dragEnter(DropTargetDragEvent e) {
    getGhostGlassPane(e.getDropTargetContext().getComponent()).ifPresent(glassPane -> {
      Transferable t = e.getTransferable();
      for (DataFlavor flavor : e.getCurrentDataFlavors()) {
        if (t.isDataFlavorSupported(flavor)) {
          e.acceptDrag(e.getDropAction());
          return;
        }
      }
      e.rejectDrag();
    });
  }

  @Override public void dragExit(DropTargetEvent e) {
    getGhostGlassPane(e.getDropTargetContext().getComponent()).ifPresent(glassPane -> {
      glassPane.setPoint(HIDDEN_POINT);
      glassPane.setTargetRect(0, 0, 0, 0);
      glassPane.repaint();
    });
  }

  @Override public void dropActionChanged(DropTargetDragEvent e) {
    /* not needed */
  }

  @Override public void dragOver(DropTargetDragEvent e) {
    Component c = e.getDropTargetContext().getComponent();
    getGhostGlassPane(c).ifPresent(glassPane -> {
      Point glassPt = e.getLocation();

      DnDTabbedPane tabbedPane = glassPane.tabbedPane;
      tabbedPane.initTargetLine(tabbedPane.getTargetTabIndex(glassPt));
      tabbedPane.autoScrollTest(glassPt);

      glassPane.setPoint(glassPt);
      glassPane.repaint();
    });
  }

  @Override public void drop(DropTargetDropEvent e) {
    Component c = e.getDropTargetContext().getComponent();
    getGhostGlassPane(c).ifPresent(glassPane -> {
      DnDTabbedPane tabbedPane = glassPane.tabbedPane;
      Transferable t = e.getTransferable();
      DataFlavor[] f = t.getTransferDataFlavors();
      int prev = tabbedPane.dragTabIndex;
      int next = tabbedPane.getTargetTabIndex(e.getLocation());
      if (t.isDataFlavorSupported(f[1])) {
        e.dropComplete(true);
      } else if (t.isDataFlavorSupported(f[0]) && prev != next) {
        tabbedPane.convertTab(prev, next);
        e.dropComplete(true);
      } else {
        e.dropComplete(false);
      }
      glassPane.setVisible(false);
    });
  }
}

class GhostGlassPane extends JComponent {
  public final DnDTabbedPane tabbedPane;
  private final Rectangle lineRect = new Rectangle();
  private final Color lineColor = new Color(0, 100, 255);
  private final Point location = new Point();
  private transient BufferedImage draggingGhost;

  protected GhostGlassPane(DnDTabbedPane tabbedPane) {
    super();
    this.tabbedPane = tabbedPane;
  }

  public void setTargetRect(int x, int y, int width, int height) {
    lineRect.setBounds(x, y, width, height);
  }

  public void setImage(BufferedImage draggingImage) {
    this.draggingGhost = draggingImage;
  }

  public void setPoint(Point pt) {
    this.location.setLocation(pt);
  }

  @Override public boolean isOpaque() {
    return false;
  }

  @Override public void setVisible(boolean v) {
    super.setVisible(v);
    if (!v) {
      setTargetRect(0, 0, 0, 0);
      setImage(null);
    }
  }

  @Override protected void paintComponent(Graphics g) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .5f));
    boolean b = tabbedPane.getTabLayoutPolicy() == JTabbedPane.SCROLL_TAB_LAYOUT;
    if (b && tabbedPane.isPaintScrollArea) {
      g2.setPaint(Color.RED);
      g2.fill(tabbedPane.rectBackward);
      g2.fill(tabbedPane.rectForward);
    }
    if (draggingGhost != null) {
      double xx = location.getX() - draggingGhost.getWidth(this) / 2d;
      double yy = location.getY() - draggingGhost.getHeight(this) / 2d;
      g2.drawImage(draggingGhost, (int) xx, (int) yy, this);
    }
    g2.setPaint(lineColor);
    g2.fill(lineRect);
    g2.dispose();
  }
}
