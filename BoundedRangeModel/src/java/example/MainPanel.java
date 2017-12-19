package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;
import java.util.stream.IntStream;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.table.*;

public class MainPanel extends JPanel {
    protected static final Color THUMB_COLOR  = new Color(0, 0, 255, 50);
    protected static final String PATTERN     = "Swing";
    protected final List<Integer> highlighter = new ArrayList<>();

    protected final DefaultTableModel model = new DefaultTableModel(0, 2);
    protected final JTable table         = new JTable(model);
    protected final JScrollPane scroll   = new JScrollPane(table);
    protected final JLabel label         = new JLabel();
    protected final JScrollBar scrollbar = new JScrollBar(Adjustable.VERTICAL);

    public MainPanel() {
        super(new BorderLayout());
        IntStream.range(0, 100).forEach(i -> model.addRow(new Object[] {i % 19 == 0 || i % 17 == 0 ? PATTERN : "aaaaa", ""}));
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (highlighter.contains(row)) {
                    setBackground(Color.YELLOW);
                } else {
                    setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
                }
                return this;
            }
        });
        table.setFillsViewportHeight(true);
        scroll.setVerticalScrollBar(scrollbar);
        scrollbar.getModel().addChangeListener(e -> label.repaint());

        label.setIcon(new HighlightIcon());
        Border in  = BorderFactory.createLineBorder(Color.BLACK);
        Border out = BorderFactory.createEmptyBorder(5, 5, 5, 5);
        label.setBorder(BorderFactory.createCompoundBorder(out, in));
        MouseInputListener handler = new HighlightBarHandler();
        label.addMouseListener(handler);
        label.addMouseMotionListener(handler);

        JToggleButton button = new JToggleButton("highlight");
        button.addActionListener(e -> {
            highlighter.clear();
            if (((JToggleButton) e.getSource()).isSelected()) {
                updateHighlighter();
            }
            label.getRootPane().repaint();
        });

        Box box = Box.createHorizontalBox();
        box.add(Box.createHorizontalGlue());
        box.add(button);
        add(scroll);
        add(label, BorderLayout.EAST);
        add(box, BorderLayout.SOUTH);
        setPreferredSize(new Dimension(320, 240));
    }
    private class HighlightBarHandler extends MouseInputAdapter {
        @Override public void mousePressed(MouseEvent e) {
            processHighlightBarMouseEvent(e);
        }
        @Override public void mouseDragged(MouseEvent e) {
            processHighlightBarMouseEvent(e);
        }
    }
    protected void processHighlightBarMouseEvent(MouseEvent e) {
        Point pt = e.getPoint();
        Component c = (Component) e.getComponent();
        BoundedRangeModel m = scrollbar.getModel();
        int iv = (int) (.5 - m.getExtent() * .5 + pt.y * (m.getMaximum() - m.getMinimum()) / (double) c.getHeight());
        m.setValue(iv);
    }
    protected final void updateHighlighter() {
        for (int i = 0; i < table.getRowCount(); i++) {
            if (Objects.equals(PATTERN, table.getValueAt(i, 0))) {
                highlighter.add(i);
            }
        }
    }
    private class HighlightIcon implements Icon {
        @Override public void paintIcon(Component c, Graphics g, int x, int y) {
            JViewport vport = Objects.requireNonNull((JViewport) SwingUtilities.getAncestorOfClass(JViewport.class, table));
            Rectangle vrect = vport.getBounds();
            Rectangle trect = table.getBounds();
            Rectangle crect = SwingUtilities.calculateInnerArea(label, label.getBounds());
            //Insets insets   = ((JComponent) c).getInsets();
            //Insets insets   = label.getInsets();

            //paint Background
            g.setColor(Color.WHITE);
            g.fillRect(crect.x, crect.y, crect.width, crect.height);

            //double sy = (crect.height - insets.top - insets.bottom) / trect.getHeight();
            double sy = crect.getHeight() / trect.getHeight();
            AffineTransform at = AffineTransform.getScaleInstance(1d, sy);
            //paint Highlight
            g.setColor(Color.YELLOW);
            highlighter.forEach(viewIndex -> {
                Rectangle r = table.getCellRect(viewIndex, 0, true);
                Rectangle s = at.createTransformedShape(r).getBounds();
                g.fillRect(x, crect.y + s.y, getIconWidth(), Math.max(2, s.height - 2));
            });
            //paint Thumb
            if (scrollbar.isVisible()) {
                Rectangle thumbRect = new Rectangle(vrect);
                thumbRect.y = vport.getViewPosition().y;
                g.setColor(THUMB_COLOR);
                Rectangle r = at.createTransformedShape(thumbRect).getBounds();
                g.fillRect(x, crect.y + r.y, getIconWidth(), r.height);
                g.setColor(THUMB_COLOR.darker());
                g.drawRect(x, crect.y + r.y, getIconWidth() - 1, r.height - 1);
            }
        }
        @Override public int getIconWidth() {
            return 14;
        }
        @Override public int getIconHeight() {
            return scroll.getHeight();
        }
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
