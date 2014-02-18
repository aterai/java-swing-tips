package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.image.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.awt.event.*;
import java.net.*;
import java.util.*;
import javax.activation.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.basic.*;

public class MainPanel extends JPanel {
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
        //ListItemTransferHandler handler = new ListItemTransferHandler();
        list.setTransferHandler(new ListItemTransferHandler());
        list.setDropMode(DropMode.INSERT);
        list.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        add(new JScrollPane(list));
        setPreferredSize(new Dimension(320, 240));
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                createAndShowGUI();
            }
        });
    }
    public static void createAndShowGUI() {
        try{
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }catch(ClassNotFoundException | InstantiationException |
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
        //return new Color(array[0], array[1], array[2]*0.5f, array[3]).getRGB();
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
    @Override public boolean isBorderOpaque() { return true; }
    @Override public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
        Graphics2D g2 = (Graphics2D)g.create();
        g2.translate(x,y);
        g2.setPaint(new Color(~SystemColor.activeCaption.getRGB()));
        BasicGraphicsUtils.drawDashedRect(g2, 0, 0, w, h);
        //g2.translate(-x,-y);
        g2.dispose();
    }
}

class ReorderbleList<E extends ListItem> extends JList<E> {
    private static final AlphaComposite ALPHA = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.1f);
    private final JPanel p = new JPanel(new BorderLayout());
    private final JLabel icon  = new JLabel((Icon)null, JLabel.CENTER);
    private final JLabel label = new JLabel("", JLabel.CENTER);
    private final Border dotBorder = new DotBorder(2,2,2,2);
    private final Border empBorder = BorderFactory.createEmptyBorder(2,2,2,2);
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
        label.setOpaque(true);
        label.setForeground(getForeground());
        label.setBackground(getBackground());
        label.setBorder(empBorder);

        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
        p.add(icon);
        p.add(label, BorderLayout.SOUTH);

        setCellRenderer(new ListCellRenderer<ListItem>() {
            @Override public Component getListCellRendererComponent(JList list, ListItem item, int index, boolean isSelected, boolean cellHasFocus) {
                label.setText(item.title);
                if(isSelected) {
                    icon.setIcon(item.sicon);
                    label.setForeground(list.getSelectionForeground());
                    label.setBackground(list.getSelectionBackground());
                }else{
                    icon.setIcon(item.nicon);
                    label.setForeground(list.getForeground());
                    label.setBackground(list.getBackground());
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
            JList list = (JList)e.getComponent();
            if(list.getDragEnabled()) {
                return;
            }
            if(srcPoint==null) {
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
            if(srcPoint==null || !getDragEnabled()) {
                JList list = (JList)e.getComponent();
                Component glassPane = list.getRootPane().getGlassPane();
                //glassPane.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                glassPane.setVisible(false);
            }
            srcPoint = null;
            setDragEnabled(getSelectedIndices().length>0);
            repaint();
        }
        @Override public void mousePressed(MouseEvent e) {
            int index = locationToIndex(e.getPoint());
            Rectangle rect = getCellBounds(index,index);
            if(rect.contains(e.getPoint())) {
                setFocusable(true);
                if(getDragEnabled()) {
                    return;
                }else{
                    //System.out.println("ccc:"+startSelectedIndex);
                    setSelectedIndex(index);
                }
            }else{
                JList list = (JList)e.getComponent();
                Component glassPane = list.getRootPane().getGlassPane();
                //glassPane.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                glassPane.setVisible(false);

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
            ArrayList<Integer> list = new ArrayList<Integer>(model.getSize());
            for(int i=0;i<model.getSize();i++) {
                Rectangle r = getCellBounds(i,i);
                if(p.intersects(r)) {
                    list.add(i);
                }
            }
            int[] il = new int[list.size()];
            for(int i=0;i<list.size();i++) {
                il[i] = list.get(i);
            }
            return il;
        }
    }
    @Override public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if(srcPoint==null || getDragEnabled()) {
            return;
        }
        Graphics2D g2d = (Graphics2D)g.create();
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
        return r>g ? r>b ? new Color(r,0,0) : new Color(0,0,b)
                   : g>b ? new Color(0,g,0) : new Color(0,0,b);
    }
}

class ListItemTransferHandler extends TransferHandler {
    private final DataFlavor localObjectFlavor;
    private int[] indices;
    private int addIndex = -1; //Location where items were added
    private int addCount; //Number of items added.

    public ListItemTransferHandler() {
        super();
        localObjectFlavor = new ActivationDataFlavor(Object[].class, DataFlavor.javaJVMLocalObjectMimeType, "Array of items");
    }
    @SuppressWarnings("deprecation")
    @Override protected Transferable createTransferable(JComponent c) {
        //System.out.println("createTransferable");
        JList list = (JList) c;
        indices = list.getSelectedIndices();
        Object[] transferedObjects = list.getSelectedValues();
        return new DataHandler(transferedObjects, localObjectFlavor.getMimeType());
    }
    @Override public boolean canImport(TransferSupport support) {
        //System.out.println("canImport");
        if(!support.isDrop() || !support.isDataFlavorSupported(localObjectFlavor)) {
            return false;
        }
        support.setShowDropLocation(true);
        support.setDropAction(MOVE);
        return true;
    }
//     private BufferedImage makeIconImage(JList c) {
//         BufferedImage srcimg = new BufferedImage(c.getWidth(), c.getHeight(), BufferedImage.TYPE_INT_ARGB);
//         c.paint(srcimg.getGraphics());
//
//         BufferedImage image = new BufferedImage(c.getWidth(), c.getHeight(), BufferedImage.TYPE_INT_ARGB);
//         Graphics g = image.getGraphics();
//         for(int idx:c.getSelectedIndices()) {
//             Rectangle rect = c.getCellBounds(idx,idx);
//             g.clipRect(rect.x,rect.y,rect.width,rect.height);
//             g.drawImage(srcimg, rect.x, rect.y, null);
//             //image = image.getSubimage(rect.x,rect.y,rect.width,rect.height);
//         }
//         return image;
//     }
    @Override public int getSourceActions(JComponent c) {
        System.out.println("getSourceActions");
        Component glassPane = c.getRootPane().getGlassPane();
        glassPane.setCursor(DragSource.DefaultMoveDrop);
        glassPane.setVisible(true);
        return MOVE;
    }
    @SuppressWarnings("unchecked")
    @Override public boolean importData(TransferSupport support) {
        if(!canImport(support)) {
            return false;
        }
        JList target = (JList)support.getComponent();
        JList.DropLocation dl = (JList.DropLocation)support.getDropLocation();
        DefaultListModel listModel = (DefaultListModel)target.getModel();
        int index = dl.getIndex();
        //boolean insert = dl.isInsert();
        int max = listModel.getSize();
        if(index<0 || index>max) {
            index = max;
        }
        addIndex = index;
        try{
            Object[] values = (Object[])support.getTransferable().getTransferData(localObjectFlavor);
            addCount = values.length;
            for(int i=0;i<values.length;i++) {
                int idx = index++;
                listModel.add(idx, values[i]);
                target.addSelectionInterval(idx, idx);
            }
            return true;
        }catch(UnsupportedFlavorException | IOException ex) {
            ex.printStackTrace();
        }
        return false;
    }
    @Override protected void exportDone(JComponent c, Transferable data, int action) {
        System.out.println("exportDone");
        Component glassPane = c.getRootPane().getGlassPane();
        //glassPane.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        glassPane.setVisible(false);
        cleanup(c, action == MOVE);
    }
    private void cleanup(JComponent c, boolean remove) {
        if(remove && indices != null) {
            JList source = (JList)c;
            DefaultListModel model  = (DefaultListModel)source.getModel();
            if(addCount > 0) {
                for(int i=0;i<indices.length;i++) {
                    if(indices[i]>=addIndex) {
                        indices[i] += addCount;
                    }
                }
            }
            for(int i=indices.length-1;i>=0;i--) {
                model.remove(indices[i]);
            }
        }
        indices  = null;
        addCount = 0;
        addIndex = -1;
    }
}
