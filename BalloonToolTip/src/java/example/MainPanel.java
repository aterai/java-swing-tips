package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.Objects;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new GridLayout(1, 2));

        DefaultListModel<String> model = new DefaultListModel<>();
        model.addElement("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        model.addElement("aaaa");
        model.addElement("aaaabbb");
        model.addElement("aaaabbbcc");
        model.addElement("1234567890abcdefghijklmnopqrstuvwxyz");
        model.addElement("bbb1");
        model.addElement("bbb12");
        model.addElement("1234567890-+*/=ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        model.addElement("bbb123");

        JList<String> list1 = new JList<String>(model) {
            @Override public JToolTip createToolTip() {
                JToolTip tip = new BalloonToolTip();
                tip.setComponent(this);
                return tip;
            }
            @Override public void updateUI() {
                super.updateUI();
                setCellRenderer(new TooltipListCellRenderer<>());
            }
        };

        JList<String> list2 = new JList<String>(model) {
            @Override public void updateUI() {
                super.updateUI();
                setCellRenderer(new TooltipListCellRenderer<>());
            }
        };

        add(makeTitledPanel("BalloonToolTip", list1));
        add(makeTitledPanel("Default JToolTip", list2));
        setPreferredSize(new Dimension(320, 240));
    }
    private static Component makeTitledPanel(String title, Component c) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder(title));
        p.add(new JScrollPane(c, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER));
        return p;
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

class TooltipListCellRenderer<E> implements ListCellRenderer<E> {
    private final ListCellRenderer<? super E> renderer = new DefaultListCellRenderer();
    @Override public Component getListCellRendererComponent(JList<? extends E> list, E value, int index, boolean isSelected, boolean cellHasFocus) {
        JLabel l = (JLabel) renderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        Insets i = l.getInsets();
        Container c = SwingUtilities.getAncestorOfClass(JViewport.class, list);
        Rectangle rect = c.getBounds();
        rect.width -= i.left + i.right;
        FontMetrics fm = l.getFontMetrics(l.getFont());
        String str = Objects.toString(value, "");
        l.setToolTipText(fm.stringWidth(str) > rect.width ? str : null);
        return l;
    }
}

class BalloonToolTip extends JToolTip {
    private HierarchyListener listener;
    @Override public void updateUI() {
        removeHierarchyListener(listener);
        super.updateUI();
        listener = e -> {
            Component c = e.getComponent();
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && c.isShowing()) {
                Component w = SwingUtilities.getRoot(c);
                if (w instanceof JWindow) {
                    System.out.println("Popup$HeavyWeightWindow");
                    ((JWindow) w).setBackground(new Color(0x0, true));
                }
            }
        };
        addHierarchyListener(listener);
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(8, 5, 0, 5));
    }
    @Override public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        d.height = 28;
        return d;
    }
    @Override public void paintComponent(Graphics g) {
        Shape s = makeBalloonShape();
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(getBackground());
        g2.fill(s);
        g2.setColor(getForeground());
        g2.draw(s);
        g2.dispose();
        super.paintComponent(g);
    }
    private Shape makeBalloonShape() {
        Insets i = getInsets();
        int w = getWidth() - 1;
        int h = getHeight() - 1;
        int v = i.top / 2;
        Polygon triangle = new Polygon();
        triangle.addPoint(i.left + v + v, 0);
        triangle.addPoint(i.left + v, v);
        triangle.addPoint(i.left + v + v + v, v);
        Area area = new Area(new RoundRectangle2D.Float(0, v, w, h - i.bottom - v, i.top, i.top));
        area.add(new Area(triangle));
        return area;
    }
}
