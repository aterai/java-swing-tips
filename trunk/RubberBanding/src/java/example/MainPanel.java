package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.net.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.basic.*;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());

        DefaultListModel<ListItem> model = new DefaultListModel<>();
        //http://www.icongalore.com/ XP Style Icons - Windows Application Icon, Software XP Icons
        model.addElement(new ListItem("ADFFDF asd", "wi0054-32.png"));
        model.addElement(new ListItem("test",       "wi0062-32.png"));
        model.addElement(new ListItem("adfasdf",    "wi0063-32.png"));
        model.addElement(new ListItem("Test",       "wi0064-32.png"));
        model.addElement(new ListItem("12345",      "wi0096-32.png"));
        model.addElement(new ListItem("111111",     "wi0054-32.png"));
        model.addElement(new ListItem("22222",      "wi0062-32.png"));
        model.addElement(new ListItem("3333",       "wi0063-32.png"));

        add(new JScrollPane(new RubberBandSelectionList<ListItem>(model)));
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

class ListItem {
    public final ImageIcon nicon;
    public final ImageIcon sicon;
    public final String title;
    public ListItem(String title, String iconfile) {
        URL url = getClass().getResource(iconfile);
        this.nicon = new ImageIcon(url);
        ImageProducer ip = new FilteredImageSource(nicon.getImage().getSource(), new SelectedImageFilter());
        this.sicon = new ImageIcon(Toolkit.getDefaultToolkit().createImage(ip));
        this.title = title;
    }
}

class RubberBandSelectionList<E extends ListItem> extends JList<E> {
    private static final AlphaComposite ALPHA = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .1f);
    private RubberBandListCellRenderer<E> renderer;
    private Color polygonColor;
    public RubberBandSelectionList(ListModel<E> model) {
        super(model);
    }
    @Override public void updateUI() {
        setSelectionForeground(null);
        setSelectionBackground(null);
        setCellRenderer(null);
        if (renderer == null) {
            renderer = new RubberBandListCellRenderer<E>();
        } else {
            removeMouseMotionListener(renderer);
            removeMouseListener(renderer);
        }
        super.updateUI();
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                setCellRenderer(renderer);
                addMouseMotionListener(renderer);
                addMouseListener(renderer);
                setLayoutOrientation(JList.HORIZONTAL_WRAP);
                setVisibleRowCount(0);
                setFixedCellWidth(62);
                setFixedCellHeight(62);
                setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            }
        });
        Color c = getSelectionBackground();
        int r = c.getRed();
        int g = c.getGreen();
        int b = c.getBlue();
        polygonColor = r > g ? r > b ? new Color(r, 0, 0) : new Color(0, 0, b)
                             : g > b ? new Color(0, g, 0) : new Color(0, 0, b);
    }
    @Override public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (renderer != null && renderer.polygon != null) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setPaint(getSelectionBackground());
            g2.draw(renderer.polygon);
            g2.setComposite(ALPHA);
            g2.setPaint(polygonColor);
            g2.fill(renderer.polygon);
            g2.dispose();
        }
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
    @Override public boolean isBorderOpaque() { return true; }
    @Override public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.translate(x, y);
        g2.setPaint(new Color(~SystemColor.activeCaption.getRGB()));
        //new Color(200, 150, 150));
        //g2.setStroke(dashed);
        //g2.drawRect(0, 0, w - 1, h - 1);
        BasicGraphicsUtils.drawDashedRect(g2, 0, 0, w, h);
        //g2.translate(-x, -y);
        g2.dispose();
    }
    //@Override public Insets getBorderInsets(Component c)
    //@Override public Insets getBorderInsets(Component c, Insets insets)
}

class RubberBandListCellRenderer<E extends ListItem> extends JPanel implements ListCellRenderer<E>, MouseListener, MouseMotionListener {
    private final JLabel icon  = new JLabel((Icon) null, JLabel.CENTER);
    private final JLabel label = new JLabel("", JLabel.CENTER);
    private final Border dotBorder = new DotBorder(2, 2, 2, 2);
    private final Border empBorder = BorderFactory.createEmptyBorder(2, 2, 2, 2);
    private final Point srcPoint = new Point();
    public Path2D polygon;
    public RubberBandListCellRenderer() {
        super(new BorderLayout());
        icon.setOpaque(false);
        label.setOpaque(true);
        label.setForeground(getForeground());
        label.setBackground(getBackground());
        label.setBorder(empBorder);
        this.setOpaque(false);
        this.setBorder(empBorder);
        this.add(icon);
        this.add(label, BorderLayout.SOUTH);
    }
    @Override public Component getListCellRendererComponent(JList<? extends E> list, E item, int index, boolean isSelected, boolean cellHasFocus) {
        icon.setIcon(isSelected ? item.sicon : item.nicon);
        label.setText(item.title);
        label.setBorder(cellHasFocus ? dotBorder : empBorder);
        if (isSelected) {
            label.setForeground(list.getSelectionForeground());
            label.setBackground(list.getSelectionBackground());
        } else {
            label.setForeground(list.getForeground());
            label.setBackground(list.getBackground());
        }
        return this;
    }
    @Override public void mouseDragged(MouseEvent e) {
        JList list = (JList) e.getComponent();
        list.setFocusable(true);
        if (polygon == null) {
            srcPoint.setLocation(e.getPoint());
        }
        Point destPoint = e.getPoint();
        polygon = new Path2D.Double();
        polygon.moveTo(srcPoint.x,  srcPoint.y);
        polygon.lineTo(destPoint.x, srcPoint.y);
        polygon.lineTo(destPoint.x, destPoint.y);
        polygon.lineTo(srcPoint.x,  destPoint.y);
        polygon.closePath();
        list.setSelectedIndices(getIntersectsIcons(list, polygon));
        list.repaint();
    }
    @Override public void mouseMoved(MouseEvent e)   { /* not needed */ }
    @Override public void mouseClicked(MouseEvent e) { /* not needed */ }
    @Override public void mouseEntered(MouseEvent e) { /* not needed */ }
    @Override public void mouseExited(MouseEvent e)  { /* not needed */ }
    @Override public void mouseReleased(MouseEvent e) {
        Component c = e.getComponent();
        c.setFocusable(true);
        polygon = null;
        c.repaint();
    }
    @Override public void mousePressed(MouseEvent e) {
        JList list = (JList) e.getComponent();
        int index = list.locationToIndex(e.getPoint());
        Rectangle rect = list.getCellBounds(index, index);
        if (rect.contains(e.getPoint())) {
            list.setFocusable(true);
        } else {
            list.clearSelection();
            list.getSelectionModel().setAnchorSelectionIndex(-1);
            list.getSelectionModel().setLeadSelectionIndex(-1);
            //list.getSelectionModel().setLeadSelectionIndex(list.getModel().getSize());
            list.setFocusable(false);
        }
    }
    private int[] getIntersectsIcons(JList l, Shape p) {
        ListModel model = l.getModel();
        List<Integer> list = new ArrayList<>(model.getSize());
        for (int i = 0; i < model.getSize(); i++) {
            Rectangle r = l.getCellBounds(i, i);
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

// class MyList extends JList {
//     private final JPanel p = new JPanel(new BorderLayout());
//     private final JLabel icon  = new JLabel((Icon) null, JLabel.CENTER);
//     private final JLabel label = new JLabel("", JLabel.CENTER);
//     private final Border dotBorder = new DotBorder(2, 2, 2, 2);
//     private final Border empBorder = BorderFactory.createEmptyBorder(2, 2, 2, 2);
//     private final Color rcolor;
//     private final Color pcolor;
//     private final AlphaComposite alcomp = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .1f);
//     private final Polygon polygon = new Polygon();
//     private final Line2D line = new Line2D.Double();
//     private Point srcPoint = null;
//     public MyList(ListModel model) {
//         super(model);
//         icon.setOpaque(false);
//         label.setOpaque(true);
//         label.setForeground(getForeground());
//         label.setBackground(getBackground());
//         label.setBorder(empBorder);
//         p.setOpaque(false);
//         p.setBorder(empBorder);
//         p.add(icon);
//         p.add(label, BorderLayout.SOUTH);
//         rcolor = SystemColor.activeCaption;
//         pcolor = makeColor(rcolor);
//         setLayoutOrientation(JList.HORIZONTAL_WRAP);
//         setVisibleRowCount(0);
//         setFixedCellWidth(62);
//         setFixedCellHeight(62);
//         setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
//         setCellRenderer(new ListCellRenderer() {
//             @Override public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
//                 ListItem item = (ListItem) getModel().getElementAt(index);
//                 icon.setIcon(isSelected ? item.sicon : item.nicon);
//                 label.setText(item.title);
//                 label.setBorder(cellHasFocus ? dotBorder : empBorder);
//                 if (isSelected) {
//                     label.setForeground(list.getSelectionForeground());
//                     label.setBackground(list.getSelectionBackground());
//                 } else {
//                     label.setForeground(list.getForeground());
//                     label.setBackground(list.getBackground());
//                 }
//                 return p;
//             }
//         });
//         RubberBandingListener rbl = new RubberBandingListener();
//         addMouseMotionListener(rbl);
//         addMouseListener(rbl);
//     }
//     class RubberBandingListener extends MouseInputAdapter {
//         @Override public void mouseDragged(MouseEvent e) {
//             setFocusable(true);
//             if (srcPoint == null) { srcPoint = e.getPoint(); }
//             Point destPoint = e.getPoint();
//             polygon.reset();
//             polygon.addPoint(srcPoint.x,  srcPoint.y);
//             polygon.addPoint(destPoint.x, srcPoint.y);
//             polygon.addPoint(destPoint.x, destPoint.y);
//             polygon.addPoint(srcPoint.x,  destPoint.y);
//             //setSelectedIndices(getIntersectsIcons(polygon));
//             if (srcPoint.getX() == destPoint.getX() || srcPoint.getY() == destPoint.getY()) {
//                 line.setLine(srcPoint.getX(), srcPoint.getY(), destPoint.getX(), destPoint.getY());
//                 setSelectedIndices(getIntersectsIcons(line));
//             } else {
//                 setSelectedIndices(getIntersectsIcons(polygon));
//             }
//             repaint();
//         }
//         @Override public void mouseReleased(MouseEvent e) {
//             setFocusable(true);
//             srcPoint = null;
//             repaint();
//         }
//         @Override public void mousePressed(MouseEvent e) {
//             int index = locationToIndex(e.getPoint());
//             Rectangle rect = getCellBounds(index, index);
//             if (!rect.contains(e.getPoint())) {
//                 clearSelection();
//                 getSelectionModel().setAnchorSelectionIndex(-1);
//                 getSelectionModel().setLeadSelectionIndex(-1);
//                 setFocusable(false);
//             } else {
//                 setFocusable(true);
//             }
//         }
//         private int[] getIntersectsIcons(Shape p) {
//             ListModel model = getModel();
//             Vector<Integer> list = new Vector<>(model.getSize());
//             for (int i = 0; i < model.getSize(); i++) {
//                 Rectangle r = getCellBounds(i, i);
//                 if (p.intersects(r)) {
//                     list.add(i);
//                 }
//             }
//             int[] il = new int[list.size()];
//             for (int i = 0; i < list.size(); i++) {
//                 il[i] = list.get(i);
//             }
//             return il;
//         }
//     }
//     @Override public void paintComponent(Graphics g) {
//         super.paintComponent(g);
//         if (srcPoint == null) { return; }
//         Graphics2D g2d = (Graphics2D) g.create();
//         g2d.setPaint(rcolor);
//         g2d.drawPolygon(polygon);
//         g2d.setComposite(alcomp);
//         g2d.setPaint(pcolor);
//         g2d.fillPolygon(polygon);
//         g2d.dispose();
//     }
//     private Color makeColor(Color c) {
//         int r = c.getRed();
//         int g = c.getGreen();
//         int b = c.getBlue();
//         return r > g ? r > b ? new Color(r, 0, 0) : new Color(0, 0, b)
//                      : g > b ? new Color(0, g, 0) : new Color(0, 0, b);
//     }
// }
// class MyList2 extends JList {
//     private final JPanel p = new JPanel(new BorderLayout());
//     private final JLabel icon  = new JLabel((Icon) null, JLabel.CENTER);
//     private final JLabel label = new JLabel("", JLabel.CENTER);
//     private final Border dotBorder = new DotBorder(2, 2, 2, 2);
//     private final Border empBorder = BorderFactory.createEmptyBorder(2, 2, 2, 2);
//     private final Color rcolor;
//     private final Color pcolor;
//     private final AlphaComposite alcomp = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .1f);
//     private final Path2D polygon = new Path2D.Double();
//     private Point srcPoint = null;
//     public MyList2(ListModel model) {
//         super(model);
//         icon.setOpaque(false);
//         label.setOpaque(true);
//         label.setForeground(getForeground());
//         label.setBackground(getBackground());
//         label.setBorder(empBorder);
//         p.setOpaque(false);
//         p.setBorder(empBorder);
//         p.add(icon);
//         p.add(label, BorderLayout.SOUTH);
//         rcolor = SystemColor.activeCaption;
//         pcolor = makeColor(rcolor);
//         setLayoutOrientation(JList.HORIZONTAL_WRAP);
//         setVisibleRowCount(0);
//         setFixedCellWidth(62);
//         setFixedCellHeight(62);
//         setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
//         setCellRenderer(new ListCellRenderer() {
//             @Override public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
//                 ListItem item = (ListItem) getModel().getElementAt(index);
//                 icon.setIcon(isSelected ? item.sicon : item.nicon);
//                 label.setText(item.title);
//                 label.setBorder(cellHasFocus ? dotBorder : empBorder);
//                 if (isSelected) {
//                     label.setForeground(list.getSelectionForeground());
//                     label.setBackground(list.getSelectionBackground());
//                 } else {
//                     label.setForeground(list.getForeground());
//                     label.setBackground(list.getBackground());
//                 }
//                 return p;
//             }
//         });
//         RubberBandingListener rbl = new RubberBandingListener();
//         addMouseMotionListener(rbl);
//         addMouseListener(rbl);
//     }
//     class RubberBandingListener extends MouseAdapter {
//         @Override public void mouseDragged(MouseEvent e) {
//             setFocusable(true);
//             if (srcPoint == null) { srcPoint = e.getPoint(); }
//             Point destPoint = e.getPoint();
//             polygon.reset();
//             polygon.moveTo(srcPoint.x,  srcPoint.y);
//             polygon.lineTo(destPoint.x, srcPoint.y);
//             polygon.lineTo(destPoint.x, destPoint.y);
//             polygon.lineTo(srcPoint.x,  destPoint.y);
//             polygon.closePath();
//             setSelectedIndices(getIntersectsIcons(polygon));
//             repaint();
//         }
//         @Override public void mouseReleased(MouseEvent e) {
//             setFocusable(true);
//             srcPoint = null;
//             repaint();
//         }
//         @Override public void mousePressed(MouseEvent e) {
//             int index = locationToIndex(e.getPoint());
//             Rectangle rect = getCellBounds(index, index);
//             if (!rect.contains(e.getPoint())) {
//                 clearSelection();
//                 getSelectionModel().setAnchorSelectionIndex(-1);
//                 getSelectionModel().setLeadSelectionIndex(-1);
//                 setFocusable(false);
//             } else {
//                 setFocusable(true);
//             }
//         }
//         private int[] getIntersectsIcons(Shape p) {
//             ListModel model = getModel();
//             Vector<Integer> list = new Vector<>(model.getSize());
//             for (int i = 0; i < model.getSize(); i++) {
//                 Rectangle r = getCellBounds(i, i);
//                 if (p.intersects(r)) {
//                     list.add(i);
//                 }
//             }
//             int[] il = new int[list.size()];
//             for (int i = 0; i < list.size(); i++) {
//                 il[i] = list.get(i);
//             }
//             return il;
//         }
//     }
//     @Override public void paintComponent(Graphics g) {
//         super.paintComponent(g);
//         if (srcPoint == null) { return; }
//         Graphics2D g2d = (Graphics2D) g.create();
//         g2d.setPaint(rcolor);
//         g2d.draw(polygon);
//         g2d.setComposite(alcomp);
//         g2d.setPaint(pcolor);
//         g2d.fill(polygon);
//         g2d.dispose();
//     }
//     private Color makeColor(Color c) {
//         int r = c.getRed();
//         int g = c.getGreen();
//         int b = c.getBlue();
//         return r > g ? r > b ? new Color(r, 0, 0) : new Color(0, 0, b)
//                      : g > b ? new Color(0, g, 0) : new Color(0, 0, b);
//     }
// }
