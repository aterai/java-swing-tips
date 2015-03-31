package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.List;
import javax.activation.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.basic.*;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());

        DefaultListModel<ListItem> model = new DefaultListModel<>();
        //http://www.icongalore.com/ XP Style Icons - Windows Application Icon, Software XP Icons
        model.addElement(new ListItem("asdasdfsd",  "wi0009-32.png"));
        model.addElement(new ListItem("12345",      "wi0054-32.png"));
        model.addElement(new ListItem("ADFFDF.asd", "wi0062-32.png"));
        model.addElement(new ListItem("test",       "wi0063-32.png"));
        model.addElement(new ListItem("32.png",     "wi0064-32.png"));
        model.addElement(new ListItem("asdfsd.jpg", "wi0096-32.png"));
        model.addElement(new ListItem("6896",       "wi0111-32.png"));
        model.addElement(new ListItem("t467467est", "wi0122-32.png"));
        model.addElement(new ListItem("test123",    "wi0124-32.png"));
        model.addElement(new ListItem("test(1)",    "wi0126-32.png"));

        ReorderbleList<ListItem> list = new ReorderbleList<>();
        list.setModel(model);
        //list.putClientProperty("List.isFileList", Boolean.TRUE);
        list.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        final ListItemTransferHandler handler = new ListItemTransferHandler();
        list.setTransferHandler(handler);
        list.setDropMode(DropMode.INSERT);
        list.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        add(new JCheckBox(new AbstractAction("Compact drag image mode") {
            @Override public void actionPerformed(ActionEvent e) {
                handler.setCompactDragImageMode(((JCheckBox) e.getSource()).isSelected());
            }
        }), BorderLayout.NORTH);
        add(new JScrollPane(list));
        setPreferredSize(new Dimension(320, 240));
    }

    public static void main(String... args) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                createAndShowGUI();
            }
        });
    }
    public static void createAndShowGUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException
               | IllegalAccessException | UnsupportedLookAndFeelException ex) {
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

class ListItem {
    public final ImageIcon nicon;
    public final ImageIcon sicon;
    public final String title;
    public ListItem(String title, String iconfile) {
        URL url = getClass().getResource(iconfile);
        this.nicon = new ImageIcon(url);
        ImageFilter filter = new SelectedImageFilter();
        ImageProducer ip = new FilteredImageSource(nicon.getImage().getSource(), filter);
        this.sicon = new ImageIcon(Toolkit.getDefaultToolkit().createImage(ip));
        this.title = title;
    }
}

class SelectedImageFilter extends RGBImageFilter {
    //public SelectedImageFilter() {
    //    canFilterIndexColorModel = false;
    //}
    @Override public int filterRGB(int x, int y, int argb) {
        //Color color = new Color(argb, true);
        //float[] array = new float[4];
        //color.getComponents(array);
        //return new Color(array[0], array[1], array[2] * .5f, array[3]).getRGB();
        return (argb & 0xffffff00) | ((argb & 0xff) >> 1);
    }
}

class DotBorder extends EmptyBorder {
    public DotBorder(Insets borderInsets) {
        super(borderInsets);
    }
    public DotBorder(int top, int left, int bottom, int right) {
        super(top, left, bottom, right);
    }
    @Override public boolean isBorderOpaque() {
        return true;
    }
    @Override public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.translate(x, y);
        g2.setPaint(new Color(~SystemColor.activeCaption.getRGB()));
        BasicGraphicsUtils.drawDashedRect(g2, 0, 0, w, h);
        //g2.translate(-x, -y);
        g2.dispose();
    }
}

class ReorderbleList<E extends ListItem> extends JList<E> {
    private static final AlphaComposite ALPHA = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .1f);
    private final JPanel p = new JPanel(new BorderLayout());
    private final JLabel icon  = new JLabel((Icon) null, SwingConstants.CENTER);
    private final JLabel label = new JLabel("", SwingConstants.CENTER);
    private final Border dotBorder = new DotBorder(2, 2, 2, 2);
    private final Border empBorder = BorderFactory.createEmptyBorder(2, 2, 2, 2);
    private final Color rcolor;
    private final Color pcolor;
    private final Polygon polygon = new Polygon();
    private Point srcPoint;

    public ReorderbleList() {
        super();
        rcolor = SystemColor.activeCaption;
        pcolor = makeColor(rcolor);
        setLayoutOrientation(JList.HORIZONTAL_WRAP);
        setVisibleRowCount(0);
        setFixedCellWidth(62);
        setFixedCellHeight(62);
        icon.setOpaque(false);
        label.setForeground(getForeground());
        label.setBackground(getBackground());
        label.setBorder(empBorder);

        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        p.add(icon);
        p.add(label, BorderLayout.SOUTH);

        setCellRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                ListItem item = (ListItem) value;
                label.setText(item.title);
                if (isSelected) {
                    icon.setIcon(item.sicon);
                    label.setForeground(list.getSelectionForeground());
                    label.setBackground(list.getSelectionBackground());
                    label.setOpaque(true);
                } else {
                    icon.setIcon(item.nicon);
                    label.setForeground(list.getForeground());
                    label.setBackground(list.getBackground());
                    label.setOpaque(false);
                }
                label.setBorder(cellHasFocus ? dotBorder : empBorder);
                return p;
            }
        });
        RubberBandingListener rbl = new RubberBandingListener();
        addMouseMotionListener(rbl);
        addMouseListener(rbl);
    }
    @Override public void updateUI() {
        setSelectionForeground(null); //Nimbus
        setSelectionBackground(null); //Nimbus
        super.updateUI();
    }
    class RubberBandingListener extends MouseAdapter {
        @Override public void mouseDragged(MouseEvent e) {
            if (getDragEnabled()) {
                return;
            }
            if (Objects.isNull(srcPoint)) {
                srcPoint = e.getPoint();
            }
            Point destPoint = e.getPoint();
            polygon.reset();
            polygon.addPoint(srcPoint.x,  srcPoint.y);
            polygon.addPoint(destPoint.x, srcPoint.y);
            polygon.addPoint(destPoint.x, destPoint.y);
            polygon.addPoint(srcPoint.x,  destPoint.y);
            setSelectedIndices(getIntersectsIcons(polygon));
            repaint();
        }
        @Override public void mouseReleased(MouseEvent e) {
            setFocusable(true);
            srcPoint = null;
            setDragEnabled(getSelectedIndices().length > 0);
            repaint();
        }
        @Override public void mousePressed(MouseEvent e) {
            int index = locationToIndex(e.getPoint());
            Rectangle rect = getCellBounds(index, index);
            if (rect.contains(e.getPoint())) {
                setFocusable(true);
                if (getDragEnabled()) {
                    return;
                } else {
                    //System.out.println("ccc:" + startSelectedIndex);
                    setSelectedIndex(index);
                }
            } else {
                clearSelection();
                getSelectionModel().setAnchorSelectionIndex(-1);
                getSelectionModel().setLeadSelectionIndex(-1);
                setFocusable(false);
                setDragEnabled(false);
            }
            repaint();
        }
        private int[] getIntersectsIcons(Shape p) {
            ListModel model = getModel();
            List<Integer> list = new ArrayList<>(model.getSize());
            for (int i = 0; i < model.getSize(); i++) {
                Rectangle r = getCellBounds(i, i);
                if (p.intersects(r)) {
                    list.add(i);
                }
            }
            int[] il = new int[list.size()];
            for (int i = 0; i < list.size(); i++) {
                il[i] = list.get(i);
            }
            return il;
        }
    }
    @Override public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (Objects.isNull(srcPoint) || getDragEnabled()) {
            return;
        }
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setPaint(rcolor);
        g2d.drawPolygon(polygon);
        g2d.setComposite(ALPHA);
        g2d.setPaint(pcolor);
        g2d.fillPolygon(polygon);
        g2d.dispose();
    }
    private Color makeColor(Color c) {
        int r = c.getRed();
        int g = c.getGreen();
        int b = c.getBlue();
        return r > g ? r > b ? new Color(r, 0, 0) : new Color(0, 0, b)
                     : g > b ? new Color(0, g, 0) : new Color(0, 0, b);
    }
}

class ListItemTransferHandler extends TransferHandler {
    private final DataFlavor localObjectFlavor;
    private int[] indices;
    private int addIndex = -1; //Location where items were added
    private int addCount; //Number of items added.
    private boolean compact;
    private static final JLabel LABEL = new JLabel() {
        @Override public Dimension getPreferredSize() {
            Dimension d = super.getPreferredSize();
            d.width = 32;
            return d;
        }
    };

    public ListItemTransferHandler() {
        super();
        localObjectFlavor = new ActivationDataFlavor(Object[].class, DataFlavor.javaJVMLocalObjectMimeType, "Array of items");
        LABEL.setOpaque(true);
        LABEL.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        LABEL.setHorizontalAlignment(SwingConstants.CENTER);
        LABEL.setForeground(Color.WHITE);
        LABEL.setBackground(new Color(0, 0, 255, 200));
    }
    public void setCompactDragImageMode(boolean compact) {
        this.compact = compact;
    }
    @Override protected Transferable createTransferable(JComponent c) {
        JList source = (JList) c;
        indices = source.getSelectedIndices();
        @SuppressWarnings("deprecation") Object[] transferedObjects = source.getSelectedValues();
        return new DataHandler(transferedObjects, localObjectFlavor.getMimeType());
    }
    @Override public boolean canImport(TransferSupport info) {
        if (info.isDrop() && info.isDataFlavorSupported(localObjectFlavor)) {
            info.setShowDropLocation(true);
            info.setDropAction(MOVE);
            return true;
        } else {
            return false;
        }
    }
    @Override public int getSourceActions(JComponent c) {
        System.out.println("getSourceActions");
        if (!(c instanceof JList)) {
            return NONE;
        }
        JList source = (JList) c;
        Point pt;
        if (compact) {
            int w = source.getFixedCellWidth();
            int h = source.getFixedCellHeight() - 20; //TODO
            setDragImage(createCompactDragImage(source, w, h));
            pt = new Point(w / 2, h);
        } else {
            setDragImage(createDragImage(source));
            pt = c.getMousePosition();
        }
        if (Objects.nonNull(pt)) {
            setDragImageOffset(pt);
        }
        return MOVE; //TransferHandler.COPY_OR_MOVE;
    }
    private static BufferedImage createDragImage(JList source) {
        int w = source.getWidth();
        int h = source.getHeight();
        BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = bi.createGraphics();
        DefaultListCellRenderer renderer = (DefaultListCellRenderer) source.getCellRenderer();
        for (int i: source.getSelectedIndices()) {
            Component c = renderer.getListCellRendererComponent(source, source.getModel().getElementAt(i), i, false, false);
            Rectangle rect = source.getCellBounds(i, i);
            SwingUtilities.paintComponent(g2, c, source, rect);
        }
        g2.dispose();
        return bi;
    }
    private static BufferedImage createCompactDragImage(JList source, int w, int h) {
        BufferedImage br = null;
        if (w > 0 && h > 0) {
            br = source.getGraphicsConfiguration().createCompatibleImage(w, h, Transparency.TRANSLUCENT);
        } else {
            return null;
        }
        int[] selectedIndices = source.getSelectedIndices();
        int length = selectedIndices.length;
        Graphics2D g2 = br.createGraphics();
        DefaultListCellRenderer renderer = (DefaultListCellRenderer) source.getCellRenderer();
        int idx = selectedIndices[0];
        Object valueAt = source.getModel().getElementAt(idx);
        Component c = renderer.getListCellRendererComponent(source, valueAt, idx, false, false);
        Rectangle rect = source.getCellBounds(idx, idx);
        SwingUtilities.paintComponent(g2, c, source, 0, 0, rect.width, rect.height);
        if (length > 1) {
            LABEL.setText(String.valueOf(length));
            Dimension d = LABEL.getPreferredSize();
            SwingUtilities.paintComponent(g2, LABEL, source, (w - d.width) / 2, (h - d.height) / 2, d.width, d.height);
        }
        g2.dispose();
        br.coerceData(true);
        return br;
    }
    @SuppressWarnings("unchecked")
    @Override public boolean importData(TransferSupport info) {
        if (!canImport(info)) {
            return false;
        }
        TransferHandler.DropLocation tdl = info.getDropLocation();
        if (!(tdl instanceof JList.DropLocation)) {
            return false;
        }
        JList.DropLocation dl = (JList.DropLocation) tdl;
        JList target = (JList) info.getComponent();
        DefaultListModel listModel = (DefaultListModel) target.getModel();
        int index = dl.getIndex();
        //boolean insert = dl.isInsert();
        int max = listModel.getSize();
        if (index < 0 || index > max) {
            index = max;
        }
        addIndex = index;
        try {
            Object[] values = (Object[]) info.getTransferable().getTransferData(localObjectFlavor);
            for (int i = 0; i < values.length; i++) {
                int idx = index++;
                listModel.add(idx, values[i]);
                target.addSelectionInterval(idx, idx);
            }
            addCount = values.length;
            return true;
        } catch (UnsupportedFlavorException | IOException ex) {
            ex.printStackTrace();
        }
        return false;
    }
    @Override protected void exportDone(JComponent c, Transferable data, int action) {
        cleanup(c, action == MOVE);
    }
    private void cleanup(JComponent c, boolean remove) {
        if (remove && Objects.nonNull(indices)) {
            //If we are moving items around in the same list, we
            //need to adjust the indices accordingly, since those
            //after the insertion point have moved.
            if (addCount > 0) {
                for (int i = 0; i < indices.length; i++) {
                    if (indices[i] >= addIndex) {
                        indices[i] += addCount;
                    }
                }
            }
            JList source = (JList) c;
            DefaultListModel model = (DefaultListModel) source.getModel();
            for (int i = indices.length - 1; i >= 0; i--) {
                model.remove(indices[i]);
            }
        }
        indices  = null;
        addCount = 0;
        addIndex = -1;
    }
}
