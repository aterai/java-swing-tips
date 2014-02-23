package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.swing.*;
import javax.swing.table.*;

public final class MainPanel extends JPanel {
    private final DnDTabbedPane tab = new DnDTabbedPane();
    private final JCheckBox gcheck  = new JCheckBox("Tab Ghost", true);
    private final JCheckBox tcheck  = new JCheckBox("Top", true);
    private final JCheckBox scheck  = new JCheckBox("SCROLL_TAB_LAYOUT", true);
    private final JCheckBox debugp  = new JCheckBox("Debug Paint", true);
    public MainPanel() {
        super(new BorderLayout());
        DnDTabbedPane sub = new DnDTabbedPane();
        sub.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        sub.addTab("Title aa", new JLabel("aaa"));
        sub.addTab("Title bb", new JScrollPane(new JTree()));
        sub.addTab("Title cc", new JScrollPane(new JTextArea("123412341234\n46746745\n245342\n")));

        tab.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        tab.addTab("JTree 00",       new JScrollPane(new JTree()));
        tab.addTab("JLabel 01",      new JLabel("Test"));
        tab.addTab("JTable 02",      new JScrollPane(makeJTable()));
        tab.addTab("JTextArea 03",   new JScrollPane(new JTextArea("asfasdfasfasdfas\nafasfasdfaf\n")));
        tab.addTab("JLabel 04",      new JLabel("<html>asfasfdasdfasdfsa<br>asfdd13412341234123446745fgh"));
        tab.addTab("null 05",        null);
        tab.addTab("JTabbedPane 06", sub);
        tab.addTab("Title 000000000000000006", new JScrollPane(new JTree()));

        add(makeCheckBoxPanel(), BorderLayout.NORTH);
        add(tab);
        setPreferredSize(new Dimension(320, 240));
    }
    private JComponent makeCheckBoxPanel() {
        gcheck.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                tab.hasGhost = gcheck.isSelected();
            }
        });
        tcheck.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                tab.setTabPlacement(tcheck.isSelected() ? JTabbedPane.TOP : JTabbedPane.RIGHT);
            }
        });
        scheck.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                tab.setTabLayoutPolicy(scheck.isSelected()?JTabbedPane.SCROLL_TAB_LAYOUT
                                                          :JTabbedPane.WRAP_TAB_LAYOUT);
            }
        });
        debugp.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                tab.isPaintScrollArea = debugp.isSelected();
            }
        });
        JPanel p1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        p1.add(gcheck); p1.add(tcheck);
        JPanel p2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        p2.add(scheck); p2.add(debugp);
        JPanel p = new JPanel(new BorderLayout());
        p.add(p1, BorderLayout.NORTH); p.add(p2, BorderLayout.SOUTH);
        return p;
    }
    private JTable makeJTable() {
        String[] columnNames = {"String", "Integer", "Boolean"};
        Object[][] data = {
            {"AAA", 1, true}, {"BBB", 2, false},
        };
        TableModel model = new DefaultTableModel(data, columnNames) {
            @Override public Class<?> getColumnClass(int column) {
                return getValueAt(0, column).getClass();
            }
        };
        return new JTable(model);
    }
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                createAndShowGUI();
            }
        });
    }
    public static void createAndShowGUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException |
                 IllegalAccessException | UnsupportedLookAndFeelException ex) {
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

class DnDTabbedPane extends JTabbedPane {
    private static final int LINEWIDTH = 3;
    private static final int RWH = 20;
    private static final int BUTTON_SIZE = 30; //XXX 30 is magic number of scroll button size

    private final GhostGlassPane glassPane = new GhostGlassPane(this);
    protected int dragTabIndex = -1;

    //For Debug: >>>
    protected boolean hasGhost = true;
    protected boolean isPaintScrollArea = true;
    //<<<

    protected Rectangle rBackward = new Rectangle();
    protected Rectangle rForward  = new Rectangle();
    public void autoScrollTest(Point glassPt) {
        Rectangle r = getTabAreaBounds();
        int tabPlacement = getTabPlacement();
        if (tabPlacement == TOP || tabPlacement == BOTTOM) {
            rBackward.setBounds(r.x, r.y, RWH, r.height);
            rForward.setBounds(r.x + r.width - RWH - BUTTON_SIZE, r.y, RWH + BUTTON_SIZE, r.height);
        } else { //if (tabPlacement == LEFT || tabPlacement == RIGHT) {
            rBackward.setBounds(r.x, r.y, r.width, RWH);
            rForward.setBounds(r.x, r.y + r.height - RWH - BUTTON_SIZE, r.width, RWH + BUTTON_SIZE);
        }
        rBackward = SwingUtilities.convertRectangle(getParent(), rBackward, glassPane);
        rForward  = SwingUtilities.convertRectangle(getParent(), rForward,  glassPane);
        if (rBackward.contains(glassPt)) {
            clickArrowButton("scrollTabsBackwardAction");
        } else if (rForward.contains(glassPt)) {
            clickArrowButton("scrollTabsForwardAction");
        }
    }
    private void clickArrowButton(String actionKey) {
        ActionMap map = getActionMap();
        if (map != null) {
            Action action = map.get(actionKey);
            if (action != null && action.isEnabled()) {
                action.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null, 0, 0));
            }
        }
    }

    public DnDTabbedPane() {
        super();
        glassPane.setName("GlassPane");
        new DropTarget(glassPane, DnDConstants.ACTION_COPY_OR_MOVE, new TabDropTargetListener(), true);
        new DragSource().createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_COPY_OR_MOVE, new TabDragGestureListener());
        //DragSource.getDefaultDragSource().createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_COPY_OR_MOVE, new TabDragGestureListener());
    }

    protected int getTargetTabIndex(Point glassPt) {
        Point tabPt = SwingUtilities.convertPoint(glassPane, glassPt, DnDTabbedPane.this);
        boolean isTB = getTabPlacement() == JTabbedPane.TOP || getTabPlacement() == JTabbedPane.BOTTOM;
        for (int i = 0; i < getTabCount(); i++) {
            Rectangle r = getBoundsAt(i);
            if (isTB) {
                r.setRect(r.x - r.width / 2, r.y,  r.width, r.height);
            } else {
                r.setRect(r.x, r.y - r.height / 2, r.width, r.height);
            }
            if (r.contains(tabPt)) {
                return i;
            }
        }
        Rectangle r = getBoundsAt(getTabCount() - 1);
        if (isTB) {
            r.setRect(r.x + r.width / 2, r.y,  r.width, r.height);
        } else {
            r.setRect(r.x, r.y + r.height / 2, r.width, r.height);
        }
        return r.contains(tabPt) ? getTabCount() : -1;
    }

    protected void convertTab(int prev, int next) {
        if (next < 0 || prev == next) {
            return;
        }
        Component cmp = getComponentAt(prev);
        Component tab = getTabComponentAt(prev);
        String str    = getTitleAt(prev);
        Icon icon     = getIconAt(prev);
        String tip    = getToolTipTextAt(prev);
        boolean flg   = isEnabledAt(prev);
        int tgtindex  = prev > next ? next : next - 1;
        remove(prev);
        insertTab(str, icon, cmp, tip, tgtindex);
        setEnabledAt(tgtindex, flg);
        //When you drag'n'drop a disabled tab, it finishes enabled and selected.
        //pointed out by dlorde
        if (flg) {
            setSelectedIndex(tgtindex);
        }
        //I have a component in all tabs (jlabel with an X to close the tab) and when i move a tab the component disappear.
        //pointed out by Daniel Dario Morales Salas
        setTabComponentAt(tgtindex, tab);
    }

    protected void initTargetLeftRightLine(int next) {
        if (next < 0 || dragTabIndex == next || next - dragTabIndex == 1) {
            glassPane.setTargetRect(0, 0, 0, 0);
        } else if (next == 0) {
            Rectangle r = SwingUtilities.convertRectangle(this, getBoundsAt(0), glassPane);
            glassPane.setTargetRect(r.x - LINEWIDTH / 2, r.y, LINEWIDTH, r.height);
        } else {
            Rectangle r = SwingUtilities.convertRectangle(this, getBoundsAt(next - 1), glassPane);
            glassPane.setTargetRect(r.x + r.width - LINEWIDTH / 2, r.y, LINEWIDTH, r.height);
        }
    }

    protected void initTargetTopBottomLine(int next) {
        if (next < 0 || dragTabIndex == next || next - dragTabIndex == 1) {
            glassPane.setTargetRect(0, 0, 0, 0);
        } else if (next == 0) {
            Rectangle r = SwingUtilities.convertRectangle(this, getBoundsAt(0), glassPane);
            glassPane.setTargetRect(r.x, r.y - LINEWIDTH / 2, r.width, LINEWIDTH);
        } else {
            Rectangle r = SwingUtilities.convertRectangle(this, getBoundsAt(next - 1), glassPane);
            glassPane.setTargetRect(r.x, r.y + r.height - LINEWIDTH / 2, r.width, LINEWIDTH);
        }
    }

    protected void initGlassPane(Point tabPt) {
        getRootPane().setGlassPane(glassPane);
        if (hasGhost) {
            Rectangle rect = getBoundsAt(dragTabIndex);
            BufferedImage image = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics g = image.getGraphics();
            paint(g);
            rect.x = rect.x < 0 ? 0 : rect.x;
            rect.y = rect.y < 0 ? 0 : rect.y;
            image = image.getSubimage(rect.x, rect.y, rect.width, rect.height);
            glassPane.setImage(image);
        }
        Point glassPt = SwingUtilities.convertPoint(this, tabPt, glassPane);
        glassPane.setPoint(glassPt);
        glassPane.setVisible(true);
    }

    protected Rectangle getTabAreaBounds() {
        Rectangle tabbedRect = getBounds();
        //pointed out by daryl. NullPointerException: i.e. addTab("Tab", null)
        //Rectangle compRect   = getSelectedComponent().getBounds();
        Component comp = getSelectedComponent();
        int idx = 0;
        while (comp == null && idx<getTabCount()) {
            comp = getComponentAt(idx++);
        }
        Rectangle compRect = (comp == null) ? new Rectangle() : comp.getBounds();
        int tabPlacement = getTabPlacement();
        if (tabPlacement == TOP) {
            tabbedRect.height = tabbedRect.height - compRect.height;
        } else if (tabPlacement == BOTTOM) {
            tabbedRect.y = tabbedRect.y + compRect.y + compRect.height;
            tabbedRect.height = tabbedRect.height - compRect.height;
        } else if (tabPlacement == LEFT) {
            tabbedRect.width = tabbedRect.width - compRect.width;
        } else if (tabPlacement == RIGHT) {
            tabbedRect.x = tabbedRect.x + compRect.x + compRect.width;
            tabbedRect.width = tabbedRect.width - compRect.width;
        }
        tabbedRect.grow(2, 2);
        return tabbedRect;
    }
}

class TabTransferable implements Transferable {
    private static final String NAME = "test";
    private static final DataFlavor FLAVOR = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType, NAME);
    private final Component tabbedPane;
    public TabTransferable(Component tabbedPane) {
        this.tabbedPane = tabbedPane;
    }
    @Override public Object getTransferData(DataFlavor flavor) {
        return tabbedPane;
    }
    @Override public DataFlavor[] getTransferDataFlavors() {
        DataFlavor[] f = new DataFlavor[1];
        f[0] = FLAVOR;
        return f;
    }
    @Override public boolean isDataFlavorSupported(DataFlavor flavor) {
        return flavor.getHumanPresentableName().equals(NAME);
    }
}

class TabDragSourceListener implements DragSourceListener {
    @Override public void dragEnter(DragSourceDragEvent e) {
        e.getDragSourceContext().setCursor(DragSource.DefaultMoveDrop);
    }
    @Override public void dragExit(DragSourceEvent e) {
        e.getDragSourceContext().setCursor(DragSource.DefaultMoveNoDrop);
        //glassPane.setTargetRect(0, 0, 0, 0);
        //glassPane.setPoint(new Point(-1000, -1000));
        //glassPane.repaint();
    }
    @Override public void dragOver(DragSourceDragEvent e) {
        //Point glassPt = e.getLocation();
        //JComponent glassPane = (JComponent) e.getDragSourceContext();
        //SwingUtilities.convertPointFromScreen(glassPt, glassPane);
        //int targetIdx = getTargetTabIndex(glassPt);
        //if (getTabAreaBounds().contains(glassPt) && targetIdx >= 0 && targetIdx!=dragTabIndex && targetIdx!=dragTabIndex + 1) {
        //    e.getDragSourceContext().setCursor(DragSource.DefaultMoveDrop);
        //    glassPane.setCursor(DragSource.DefaultMoveDrop);
        //} else {
        //    e.getDragSourceContext().setCursor(DragSource.DefaultMoveNoDrop);
        //    glassPane.setCursor(DragSource.DefaultMoveNoDrop);
        //}
    }
    @Override public void dragDropEnd(DragSourceDropEvent e) {
        //dragTabIndex = -1;
        //glassPane.setTargetRect(0, 0, 0, 0);
        //glassPane.setVisible(false);
        //glassPane.setImage(null);
    }
    @Override public void dropActionChanged(DragSourceDragEvent e) { /* not needed */ }
}

class TabDragGestureListener implements DragGestureListener {
    @Override public void dragGestureRecognized(DragGestureEvent e) {
        Component c = e.getComponent();
        if (!(c instanceof DnDTabbedPane)) {
            return;
        }
        DnDTabbedPane tabbedPane = (DnDTabbedPane) c;
        if (tabbedPane.getTabCount()<=1) {
            return;
        }
        Point tabPt = e.getDragOrigin();
        tabbedPane.dragTabIndex = tabbedPane.indexAtLocation(tabPt.x, tabPt.y);
        //"disabled tab problem".
        if (tabbedPane.dragTabIndex < 0 || !tabbedPane.isEnabledAt(tabbedPane.dragTabIndex)) {
            return;
        }
        tabbedPane.initGlassPane(e.getDragOrigin());
        try {
            e.startDrag(DragSource.DefaultMoveDrop, new TabTransferable(c), new TabDragSourceListener());
        } catch (InvalidDnDOperationException idoe) {
            idoe.printStackTrace();
        }
    }
}

class TabDropTargetListener implements DropTargetListener {
    private Point prevGlassPt = new Point();
    @Override public void dragEnter(DropTargetDragEvent e) {
        Component c = e.getDropTargetContext().getComponent();
        if (!(c instanceof GhostGlassPane)) {
            return;
        }
        GhostGlassPane glassPane = (GhostGlassPane) c;
        DnDTabbedPane tabbedPane = glassPane.tabbedPane;
        Transferable t = e.getTransferable();
        DataFlavor[] f = e.getCurrentDataFlavors();
        if (t.isDataFlavorSupported(f[0]) && tabbedPane.dragTabIndex >= 0) {
            e.acceptDrag(e.getDropAction());
        } else {
            e.rejectDrag();
        }
    }
    @Override public void dragExit(DropTargetEvent e) {
        Component c = e.getDropTargetContext().getComponent();
        System.out.println("DropTargetListener#dragExit: " + c.getName());
    }
    @Override public void dropActionChanged(DropTargetDragEvent e) { /* not needed */ }
    @Override public void dragOver(final DropTargetDragEvent e) {
        Component c = e.getDropTargetContext().getComponent();
        if (!(c instanceof GhostGlassPane)) {
            return;
        }
        GhostGlassPane glassPane = (GhostGlassPane) c;
        DnDTabbedPane tabbedPane = glassPane.tabbedPane;
        Point glassPt = e.getLocation();
        if (tabbedPane.getTabPlacement() == JTabbedPane.TOP || tabbedPane.getTabPlacement() == JTabbedPane.BOTTOM) {
            tabbedPane.initTargetLeftRightLine(tabbedPane.getTargetTabIndex(glassPt));
        } else {
            tabbedPane.initTargetTopBottomLine(tabbedPane.getTargetTabIndex(glassPt));
        }
        if (tabbedPane.hasGhost) {
            glassPane.setPoint(glassPt);
        }
        if (!prevGlassPt.equals(glassPt)) {
            glassPane.repaint();
        }
        prevGlassPt = glassPt;
        tabbedPane.autoScrollTest(glassPt);
    }
    @Override public void drop(DropTargetDropEvent e) {
        Component c = e.getDropTargetContext().getComponent();
        if (!(c instanceof GhostGlassPane)) {
            return;
        }
        GhostGlassPane glassPane = (GhostGlassPane) c;
        DnDTabbedPane tabbedPane = glassPane.tabbedPane;
        Transferable t = e.getTransferable();
        DataFlavor[] f = t.getTransferDataFlavors();
        if (t.isDataFlavorSupported(f[0]) && tabbedPane.dragTabIndex >= 0) {
            tabbedPane.convertTab(tabbedPane.dragTabIndex, tabbedPane.getTargetTabIndex(e.getLocation()));
            e.dropComplete(true);
        } else {
            e.dropComplete(false);
        }
        tabbedPane.dragTabIndex = -1;
        glassPane.setTargetRect(0, 0, 0, 0);
        glassPane.setVisible(false);
        glassPane.setImage(null);
        tabbedPane.repaint();
    }
}

class GhostGlassPane extends JPanel {
    private static final AlphaComposite ALPHA = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f);
    public final DnDTabbedPane tabbedPane;
    private final Rectangle lineRect  = new Rectangle();
    private final Color     lineColor = new Color(0, 100, 255);
    private Point location = new Point(0, 0);
    private transient BufferedImage draggingGhost;

    public GhostGlassPane(DnDTabbedPane tabbedPane) {
        super();
        this.tabbedPane = tabbedPane;
        setOpaque(false);
        // Bug ID: 6700748 Cursor flickering during D&D when using CellRendererPane with validation
        // http://bugs.sun.com/view_bug.do?bug_id=6700748
        //setCursor(null);
    }
    public void setTargetRect(int x, int y, int width, int height) {
        lineRect.setRect(x, y, width, height);
    }
    public void setImage(BufferedImage draggingGhost) {
        this.draggingGhost = draggingGhost;
    }
    public void setPoint(Point location) {
        this.location = location;
    }
    @Override public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setComposite(ALPHA);
        if (tabbedPane.isPaintScrollArea && tabbedPane.getTabLayoutPolicy() == JTabbedPane.SCROLL_TAB_LAYOUT) {
            g2.setPaint(Color.RED);
            g2.fill(tabbedPane.rBackward);
            g2.fill(tabbedPane.rForward);
        }
        if (draggingGhost != null) {
            double xx = location.getX() - draggingGhost.getWidth(this) /2d;
            double yy = location.getY() - draggingGhost.getHeight(this)/2d;
            g2.drawImage(draggingGhost, (int) xx, (int) yy , null);
        }
        if (tabbedPane.dragTabIndex >= 0) {
            g2.setPaint(lineColor);
            g2.fill(lineRect);
        }
        g2.dispose();
    }
}
