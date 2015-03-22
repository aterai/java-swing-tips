package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.plaf.LayerUI;
import javax.swing.text.DefaultEditorKit;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());
        add(new JScrollPane(new JTextArea()));
        setPreferredSize(new Dimension(320, 240));
    }
    public static JMenuBar makeManuBar() {
        JMenuBar mb = new JMenuBar();
        JMenu menu = new JMenu("File");

        JComponent edit = makeEditButtonBar(Arrays.asList(
            makeButton("Cut",   new DefaultEditorKit.CutAction()),
            makeButton("Copy",  new DefaultEditorKit.CopyAction()),
            makeButton("Paste", new DefaultEditorKit.PasteAction())));

        menu.add("aaaaaaaaaa");
        menu.addSeparator();
        menu.add(makeEditMenuItem(edit));
        menu.addSeparator();
        menu.add("bbbb");
        menu.add("cccccc");
        menu.add("ddddd");

        mb.add(menu);
        return mb;
    }
    private static JMenuItem makeEditMenuItem(final JComponent edit) {
        JMenuItem item = new JMenuItem("Edit") {
            @Override public Dimension getPreferredSize() {
                Dimension d = super.getPreferredSize();
                d.width += edit.getPreferredSize().width;
                d.height = Math.max(edit.getPreferredSize().height, d.height);
                return d;
            }
            @Override protected void fireStateChanged() {
                setForeground(Color.BLACK);
                super.fireStateChanged();
            }
        };
        item.setEnabled(false);

        GridBagConstraints c = new GridBagConstraints();
        item.setLayout(new GridBagLayout());
        c.gridheight = 1;
        c.gridwidth  = 1;
        c.gridy = 0;
        c.gridx = 0;

        c.weightx = 1d;
        c.fill = GridBagConstraints.HORIZONTAL;
        item.add(Box.createHorizontalGlue(), c);
        c.gridx = 1;
        c.fill = GridBagConstraints.NONE;
        c.weightx = 0d;
        c.anchor = GridBagConstraints.EAST;
        item.add(edit, c);

        return item;
    }
    private static JComponent makeEditButtonBar(List<AbstractButton> list) {
        int size = list.size();
        JPanel p = new JPanel(new GridLayout(1, size, 0, 0)) {
            @Override public Dimension getMaximumSize() {
                return super.getPreferredSize();
            }
        };
        for (AbstractButton b: list) {
            b.setIcon(new ToggleButtonBarCellIcon());
            p.add(b);
        }
        p.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        p.setOpaque(false);

        return new JLayer<JPanel>(p, new EditMenuLayerUI(list.get(size - 1)));
    }
    private static AbstractButton makeButton(String title, Action action) {
        JButton b = new JButton(action);
        b.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                JButton b = (JButton) e.getSource();
                Container c = SwingUtilities.getAncestorOfClass(JPopupMenu.class, b);
                if (c instanceof JPopupMenu) {
                    ((JPopupMenu) c).setVisible(false);
                }
            }
        });
        b.setText(title);
        b.setVerticalAlignment(SwingConstants.CENTER);
        b.setVerticalTextPosition(SwingConstants.CENTER);
        b.setHorizontalAlignment(SwingConstants.CENTER);
        b.setHorizontalTextPosition(SwingConstants.CENTER);
        b.setBorder(BorderFactory.createEmptyBorder());
        b.setContentAreaFilled(false);
        b.setFocusPainted(false);
        b.setOpaque(false);
        b.setBorder(BorderFactory.createEmptyBorder());
        return b;
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
        frame.setJMenuBar(makeManuBar());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

//http://http://ateraimemo.com/Swing/ToggleButtonBar.html
class ToggleButtonBarCellIcon implements Icon {
    @Override public void paintIcon(Component c, Graphics g, int x, int y) {
        Container parent = c.getParent();
        if (parent == null) {
            return;
        }

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int r = 4;
        int w = c.getWidth();
        int h = c.getHeight() - 1;
        Path2D.Float p = new Path2D.Float();

        if (c == parent.getComponent(0)) {
            //:first-child
            p.moveTo(x, y + r);
            p.quadTo(x, y, x + r, y);
            p.lineTo(x + w, y);
            p.lineTo(x + w, y + h);
            p.lineTo(x + r, y + h);
            p.quadTo(x, y + h, x, y + h - r);
        } else if (c == parent.getComponent(parent.getComponentCount() - 1)) {
            //:last-child
            w--;
            p.moveTo(x, y);
            p.lineTo(x + w - r, y);
            p.quadTo(x + w, y, x + w, y + r);
            p.lineTo(x + w, y + h - r);
            p.quadTo(x + w, y + h, x + w - r, y + h);
            p.lineTo(x, y + h);
        } else {
            p.moveTo(x, y);
            p.lineTo(x + w, y);
            p.lineTo(x + w, y + h);
            p.lineTo(x, y + h);
        }
        p.closePath();
        Area area = new Area(p);
        Color color = new Color(0, true);
        Color borderColor = Color.GRAY.brighter();
        if (c instanceof AbstractButton) {
            ButtonModel m = ((AbstractButton) c).getModel();
            if (m.isPressed()) {
                color = new Color(200, 200, 255);
            } else if (m.isSelected() || m.isRollover()) {
                borderColor = Color.GRAY;
            }
        }
        g2.setPaint(color);
        g2.fill(area);
        g2.setPaint(borderColor);
        g2.draw(area);
        g2.dispose();
    }
    @Override public int getIconWidth() {
        return 40;
    }
    @Override public int getIconHeight() {
        return 20;
    }
}

class EditMenuLayerUI extends LayerUI<JPanel> {
    private final AbstractButton lastButton;
    private Shape shape;
    public EditMenuLayerUI(AbstractButton button) {
        super();
        this.lastButton = button;
    }
    @Override public void paint(Graphics g, JComponent c) {
        super.paint(g, c);
        if (shape != null) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setPaint(Color.GRAY);
            g2.draw(shape);
            g2.dispose();
        }
    }
    @Override public void installUI(JComponent c) {
        super.installUI(c);
        if (c instanceof JLayer) {
            ((JLayer) c).setLayerEventMask(AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK);
        }
    }
    @Override public void uninstallUI(JComponent c) {
        if (c instanceof JLayer) {
            ((JLayer) c).setLayerEventMask(0);
        }
        super.uninstallUI(c);
    }
    private void update(MouseEvent e, JLayer<? extends JPanel> l) {
        int id = e.getID();
        Shape s = null;
        if (id == MouseEvent.MOUSE_ENTERED || id == MouseEvent.MOUSE_MOVED) {
            Component c = e.getComponent();
            if (!Objects.equals(c, lastButton)) {
                Rectangle r = c.getBounds();
                s = new Line2D.Double(r.x + r.width, r.y, r.x + r.width, r.y + r.height - 1);
            }
        }
        if (!Objects.equals(s, shape)) {
            shape = s;
            l.getView().repaint();
        }
    }
    @Override protected void processMouseEvent(MouseEvent e, JLayer<? extends JPanel> l) {
        update(e, l);
    }
    @Override protected void processMouseMotionEvent(MouseEvent e, JLayer<? extends JPanel> l) {
        update(e, l);
    }
}
