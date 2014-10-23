package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.Objects;
import javax.swing.*;
import javax.swing.event.*;

public final class MainPanel extends JPanel {
    private final DefaultListModel<String> model = new DefaultListModel<>();
    public MainPanel() {
        super(new GridLayout(1, 3));

        model.addElement("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        model.addElement("aaaa");
        model.addElement("aaaabbb");
        model.addElement("aaaabbbcc");
        model.addElement("1234567890abcdefghijklmnopqrstuvwxyz");
        model.addElement("bbb1");
        model.addElement("bbb12");
        model.addElement("1234567890-+*/=ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        model.addElement("bbb123");

        add(makeTitledPanel("CellBounds", new TooltipList<>(model)));
        add(makeTitledPanel("ListCellRenderer", new CellRendererTooltipList<>(model)));
        add(makeTitledPanel("Default location", new JList<>(model)));
        setPreferredSize(new Dimension(320, 240));
    }
    private static JComponent makeTitledPanel(String title, JList<String> list) {
        list.setCellRenderer(new TooltipListCellRenderer());
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder(title));
        p.add(new JScrollPane(list, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER));
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

class TooltipList<E> extends JList<E> {
    public TooltipList(ListModel<E> m) {
        super(m);
    }
    @Override public Point getToolTipLocation(MouseEvent event) {
        Point pt = null;
        if (event != null) {
            Point p = event.getPoint();
            ListCellRenderer<? super E> r = getCellRenderer();
            int i = locationToIndex(p);
            Rectangle cellBounds = getCellBounds(i, i);
            if (i != -1 && r != null && cellBounds != null && cellBounds.contains(p.x, p.y)) {
                ListSelectionModel lsm = getSelectionModel();
                Component rComponent = r.getListCellRendererComponent(this, getModel().getElementAt(i), i, lsm.isSelectedIndex(i), hasFocus() && lsm.getLeadSelectionIndex() == i);
                if (rComponent instanceof JComponent && ((JComponent) rComponent).getToolTipText() != null) {
                    pt = cellBounds.getLocation();
                }
            }
        }
        return pt;
    }
}

class CellRendererTooltipList<E> extends JList<E> {
    private final JLabel label = new JLabel();
    public CellRendererTooltipList(ListModel<E> m) {
        super(m);
    }
    @Override public Point getToolTipLocation(MouseEvent event) {
        Point pt = null;
        Point p = event.getPoint();
        int i = locationToIndex(p);
        ListCellRenderer<? super E> r = getCellRenderer();
        final Rectangle cellBounds = getCellBounds(i, i);
        if (i != -1 && r != null && cellBounds != null && cellBounds.contains(p.x, p.y)) {
            ListSelectionModel lsm = getSelectionModel();
            E str = ((ListModel<E>) getModel()).getElementAt(i);
            final Component rComponent = r.getListCellRendererComponent(this, str, i, lsm.isSelectedIndex(i), hasFocus() && lsm.getLeadSelectionIndex() == i);
            if (rComponent instanceof JComponent && ((JComponent) rComponent).getToolTipText() != null) {
                pt = cellBounds.getLocation();
                label.setIcon(new RendererIcon(rComponent, cellBounds));
            }
        }
        return pt;
    }
    @Override public JToolTip createToolTip() {
        JToolTip tip = new JToolTip() {
            @Override public Dimension getPreferredSize() {
                Insets i = getInsets();
                Dimension d = label.getPreferredSize();
                return new Dimension(d.width + i.left + i.right, d.height + i.top + i.bottom);
            }
        };
        tip.setBorder(BorderFactory.createEmptyBorder());
        tip.setLayout(new BorderLayout());
        tip.setComponent(this);
        tip.add(label);
        return tip;
    }
}

class TooltipListCellRenderer extends DefaultListCellRenderer {
    @Override public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean hasFocus) {
        JLabel l = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, hasFocus);
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

class RendererIcon implements Icon {
    private final Component renderer;
    private final Rectangle rect;
    public RendererIcon(Component renderer, Rectangle rect) {
        this.renderer = renderer;
        this.rect = rect;
    }
    @Override public void paintIcon(Component c, Graphics g, int x, int y) {
        if (c instanceof Container) {
            g.translate(-x, -y);
            rect.setLocation(0, 0);

            SwingUtilities.paintComponent(g, renderer, (Container) c, rect);
            g.translate(x, y);
        }
    }
    @Override public int getIconWidth() {
        return rect.width;
    }
    @Override public int getIconHeight() {
        return rect.height;
    }
}
