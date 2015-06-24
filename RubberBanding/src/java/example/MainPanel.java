package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.*;
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
        this.nicon = new ImageIcon(getClass().getResource(iconfile));
        ImageProducer ip = new FilteredImageSource(nicon.getImage().getSource(), new SelectedImageFilter());
        this.sicon = new ImageIcon(Toolkit.getDefaultToolkit().createImage(ip));
        this.title = title;
    }
}

class RubberBandSelectionList<E extends ListItem> extends JList<E> {
    private static final AlphaComposite ALPHA = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .1f);
    private transient RubberBandingListener rbl;
    private Color rubberBandColor;
    public RubberBandSelectionList(ListModel<E> model) {
        super(model);
    }
    @Override public void updateUI() {
        setSelectionForeground(null); //Nimbus
        setSelectionBackground(null); //Nimbus
        setCellRenderer(null);
        removeMouseListener(rbl);
        removeMouseMotionListener(rbl);
        super.updateUI();

        rubberBandColor = makeRubberBandColor(getSelectionBackground());
        setLayoutOrientation(JList.HORIZONTAL_WRAP);
        setVisibleRowCount(0);
        setFixedCellWidth(62);
        setFixedCellHeight(62);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        setCellRenderer(new ListItemListCellRenderer<E>());
        rbl = new RubberBandingListener();
        addMouseMotionListener(rbl);
        addMouseListener(rbl);
    }
    @Override public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (Objects.isNull(rbl)) {
            return;
        }
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setPaint(getSelectionBackground());
        g2.draw(rbl.rubberBand);
        g2.setComposite(ALPHA);
        g2.setPaint(rubberBandColor);
        g2.fill(rbl.rubberBand);
        g2.dispose();
    }
    private static Color makeRubberBandColor(Color c) {
        int r = c.getRed();
        int g = c.getGreen();
        int b = c.getBlue();
        return r > g ? r > b ? new Color(r, 0, 0) : new Color(0, 0, b)
                     : g > b ? new Color(0, g, 0) : new Color(0, 0, b);
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

// class DotBorder extends EmptyBorder {
//     public DotBorder(Insets borderInsets) {
//         super(borderInsets);
//     }
//     public DotBorder(int top, int left, int bottom, int right) {
//         super(top, left, bottom, right);
//     }
//     @Override public boolean isBorderOpaque() {
//         return true;
//     }
//     @Override public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
//         Graphics2D g2 = (Graphics2D) g.create();
//         g2.translate(x, y);
//         g2.setPaint(new Color(~SystemColor.activeCaption.getRGB()));
//         //new Color(200, 150, 150));
//         //g2.setStroke(dashed);
//         //g2.drawRect(0, 0, w - 1, h - 1);
//         BasicGraphicsUtils.drawDashedRect(g2, 0, 0, w, h);
//         //g2.translate(-x, -y);
//         g2.dispose();
//     }
//     //@Override public Insets getBorderInsets(Component c)
//     //@Override public Insets getBorderInsets(Component c, Insets insets)
// }

class ListItemListCellRenderer<E extends ListItem> implements ListCellRenderer<E> {
    private final JPanel p = new JPanel(new BorderLayout());
    private final JLabel icon  = new JLabel((Icon) null, SwingConstants.CENTER);
    private final JLabel label = new JLabel("", SwingConstants.CENTER);
    //private final Border dotBorder = new DotBorder(2, 2, 2, 2);
    //private final Border empBorder = BorderFactory.createEmptyBorder(2, 2, 2, 2);
    private final Border focusCellHighlightBorder = UIManager.getBorder("List.focusCellHighlightBorder");
    private final Border noFocusBorder; // = UIManager.getBorder("List.noFocusBorder");

    public ListItemListCellRenderer() {
        Border b = UIManager.getBorder("List.noFocusBorder");
        if (Objects.isNull(b)) { //Nimbus???
            Insets i = focusCellHighlightBorder.getBorderInsets(label);
            b = BorderFactory.createEmptyBorder(i.top, i.left, i.bottom, i.right);
        }
        noFocusBorder = b;
        icon.setOpaque(false);
        label.setForeground(p.getForeground());
        label.setBackground(p.getBackground());
        label.setBorder(noFocusBorder);
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        p.add(icon);
        p.add(label, BorderLayout.SOUTH);
    }
    @Override public Component getListCellRendererComponent(JList<? extends E> list, E item, int index, boolean isSelected, boolean cellHasFocus) {
        label.setText(item.title);
        //label.setBorder(cellHasFocus ? dotBorder : empBorder);
        label.setBorder(cellHasFocus ? focusCellHighlightBorder : noFocusBorder);
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
        return p;
    }
}

class RubberBandingListener extends MouseAdapter {
    public final Path2D rubberBand = new Path2D.Double();
    private final Point srcPoint = new Point();
    @Override public void mouseDragged(MouseEvent e) {
        JList l = (JList) e.getComponent();
        l.setFocusable(true);
        Point destPoint = e.getPoint();
        rubberBand.reset();
        rubberBand.moveTo(srcPoint.x,  srcPoint.y);
        rubberBand.lineTo(destPoint.x, srcPoint.y);
        rubberBand.lineTo(destPoint.x, destPoint.y);
        rubberBand.lineTo(srcPoint.x,  destPoint.y);
        rubberBand.closePath();
        l.setSelectedIndices(getIntersectsIcons(l, rubberBand));
        //JDK 1.8.0: l.setSelectedIndices(IntStream.range(0, l.getModel().getSize()).filter(i -> rubberBand.intersects(l.getCellBounds(i, i))).toArray());
        l.repaint();
    }
    @Override public void mouseReleased(MouseEvent e) {
        rubberBand.reset();
        Component c = e.getComponent();
        c.setFocusable(true);
        c.repaint();
    }
    @Override public void mousePressed(MouseEvent e) {
        JList l = (JList) e.getComponent();
        int index = l.locationToIndex(e.getPoint());
        Rectangle rect = l.getCellBounds(index, index);
        if (rect.contains(e.getPoint())) {
            l.setFocusable(true);
        } else {
            l.clearSelection();
            l.getSelectionModel().setAnchorSelectionIndex(-1);
            l.getSelectionModel().setLeadSelectionIndex(-1);
            l.setFocusable(false);
        }
        srcPoint.setLocation(e.getPoint());
        l.repaint();
    }
    //JDK 1.7.0
    private static int[] getIntersectsIcons(JList<?> l, Shape rect) {
        ListModel model = l.getModel();
        List<Integer> ll = new ArrayList<>(model.getSize());
        for (int i = 0; i < model.getSize(); i++) {
            if (rect.intersects(l.getCellBounds(i, i))) {
                ll.add(i);
            }
        }
        //JDK 1.8.0: return ll.stream().mapToInt(i -> i).toArray();
        int[] il = new int[ll.size()];
        for (int i = 0; i < ll.size(); i++) {
            il[i] = ll.get(i);
        }
        return il;
    }
}
