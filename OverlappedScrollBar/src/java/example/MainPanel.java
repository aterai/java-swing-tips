package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@
import java.awt.*;
import java.util.Objects;
import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.*;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new GridLayout(1, 2));
        add(new JScrollPane(makeList()));
        add(makeTranslucentScrollBar(makeList()));
        setPreferredSize(new Dimension(320, 240));
    }
    private static JTable makeList() {
        DefaultTableModel model = new DefaultTableModel(30, 5);
        JTable table = new JTable(model);
        table.setAutoCreateRowSorter(true);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        return table;
    }
    private static JScrollPane makeTranslucentScrollBar(JTable c) {
        return new JScrollPane(c) {
            @Override public boolean isOptimizedDrawingEnabled() {
                return false; // JScrollBar is overlap
            }
            @Override public void updateUI() {
                super.updateUI();
                EventQueue.invokeLater(() -> {
                    getVerticalScrollBar().setUI(new OverlappedScrollBarUI());
                    getHorizontalScrollBar().setUI(new OverlappedScrollBarUI());
                    setComponentZOrder(getVerticalScrollBar(), 0);
                    setComponentZOrder(getHorizontalScrollBar(), 1);
                    setComponentZOrder(getViewport(), 2);
                    getVerticalScrollBar().setOpaque(false);
                    getHorizontalScrollBar().setOpaque(false);
                });
                setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
                setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
                setLayout(new OverlapScrollPaneLayout());
            }
        };
    }
    public static void main(String... args) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                createAndShowGui();
            }
        });
    }
    public static void createAndShowGui() {
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

class OverlapScrollPaneLayout extends ScrollPaneLayout {
    private static final int BAR_SIZE = 12;
    @Override public void layoutContainer(Container parent) {
        if (parent instanceof JScrollPane) {
            JScrollPane scrollPane = (JScrollPane) parent;

            Rectangle availR = scrollPane.getBounds();
            availR.setLocation(0, 0); // availR.x = availR.y = 0;

            Insets insets = parent.getInsets();
            availR.x = insets.left;
            availR.y = insets.top;
            availR.width -= insets.left + insets.right;
            availR.height -= insets.top + insets.bottom;

            Rectangle colHeadR = new Rectangle(0, availR.y, 0, 0);
            if (Objects.nonNull(colHead) && colHead.isVisible()) {
                int colHeadHeight = Math.min(availR.height, colHead.getPreferredSize().height);
                colHeadR.height = colHeadHeight;
                availR.y += colHeadHeight;
                availR.height -= colHeadHeight;
            }

            colHeadR.width = availR.width;
            colHeadR.x = availR.x;
            if (Objects.nonNull(colHead)) {
                colHead.setBounds(colHeadR);
            }

            Rectangle hsbR = new Rectangle();
            hsbR.height = BAR_SIZE;
            hsbR.width = availR.width - hsbR.height;
            hsbR.x = availR.x;
            hsbR.y = availR.y + availR.height - hsbR.height;

            Rectangle vsbR = new Rectangle();
            vsbR.width = BAR_SIZE;
            vsbR.height = availR.height - vsbR.width;
            vsbR.x = availR.x + availR.width - vsbR.width;
            vsbR.y = availR.y;

            if (Objects.nonNull(viewport)) {
                viewport.setBounds(availR);
            }
            if (Objects.nonNull(vsb)) {
                vsb.setVisible(true);
                vsb.setBounds(vsbR);
            }
            if (Objects.nonNull(hsb)) {
                hsb.setVisible(true);
                hsb.setBounds(hsbR);
            }
        }
    }
}

class ZeroSizeButton extends JButton {
    private static final Dimension ZERO_SIZE = new Dimension();
    @Override public Dimension getPreferredSize() {
        return ZERO_SIZE;
    }
}

class OverlappedScrollBarUI extends BasicScrollBarUI {
    private static final Color DEFAULT_COLOR = new Color(100, 180, 255, 100);
    private static final Color DRAGGING_COLOR = new Color(100, 180, 200, 100);
    private static final Color ROLLOVER_COLOR = new Color(100, 180, 220, 100);

    @Override protected JButton createDecreaseButton(int orientation) {
        return new ZeroSizeButton();
    }
    @Override protected JButton createIncreaseButton(int orientation) {
        return new ZeroSizeButton();
    }
    @Override protected void paintTrack(Graphics g, JComponent c, Rectangle r) {
        // Graphics2D g2 = (Graphics2D) g.create();
        // g2.setPaint(new Color(100, 100, 100, 100));
        // g2.fillRect(r.x, r.y, r.width - 1, r.height - 1);
        // g2.dispose();
    }
    @Override protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
        JScrollBar sb = (JScrollBar) c;
        Color color;
        if (r.isEmpty() || !sb.isEnabled()) {
            return;
        } else if (isDragging) {
            color = DRAGGING_COLOR;
        } else if (isThumbRollover()) {
            color = ROLLOVER_COLOR;
        } else {
            color = DEFAULT_COLOR;
        }
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setPaint(color);
        g2.fillRoundRect(r.x, r.y, r.width - 1, r.height - 1, 8, 8);
        g2.setPaint(Color.WHITE);
        g2.drawRoundRect(r.x, r.y, r.width - 1, r.height - 1, 8, 8);
        g2.dispose();
    }
}
