package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.Date;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.plaf.basic.BasicScrollBarUI;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new GridLayout(1, 2));
        add(new JScrollPane(makeList()));
        add(makeTranslucentScrollBar(makeList()));
        setPreferredSize(new Dimension(320, 240));
    }
    private static JComponent makeList() {
        DefaultListModel<String> model = new DefaultListModel<>();
        for (int i = 0; i < 50; i++) {
            Date d = new Date();
            model.addElement(String.format("%d: %s", i, d.toString()));
        }
        return new JList<String>(model);
    }
    private static JScrollPane makeTranslucentScrollBar(JComponent c) {
        return new JScrollPane(c) {
            @Override public boolean isOptimizedDrawingEnabled() {
                return false; // JScrollBar is overlap
            }
            @Override public void updateUI() {
                super.updateUI();
                EventQueue.invokeLater(new Runnable() {
                    @Override public void run() {
                        setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
                        setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

                        getVerticalScrollBar().setUI(new TranslucentScrollBarUI());

                        setComponentZOrder(getVerticalScrollBar(), 0);
                        setComponentZOrder(getViewport(), 1);
                        getVerticalScrollBar().setOpaque(false);
                    }
                });
                setLayout(new ScrollPaneLayout() {
                    @Override public void layoutContainer(Container parent) {
                        if (parent instanceof JScrollPane) {
                            JScrollPane scrollPane = (JScrollPane) parent;

                            Rectangle availR = scrollPane.getBounds();
                            availR.setLocation(0, 0); //availR.x = availR.y = 0;

                            Insets insets = parent.getInsets();
                            availR.x = insets.left;
                            availR.y = insets.top;
                            availR.width  -= insets.left + insets.right;
                            availR.height -= insets.top  + insets.bottom;

                            Rectangle vsbR = new Rectangle();
                            vsbR.width  = 12;
                            vsbR.height = availR.height;
                            vsbR.x = availR.x + availR.width - vsbR.width;
                            vsbR.y = availR.y;

                            if (viewport != null) {
                                viewport.setBounds(availR);
                            }
                            if (vsb != null) {
                                vsb.setVisible(true);
                                vsb.setBounds(vsbR);
                            }
                        }
                    }
                });
            }
        };
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

class ZeroSizeButton extends JButton {
    private static final Dimension ZERO_SIZE = new Dimension();
    @Override public Dimension getPreferredSize() {
        return ZERO_SIZE;
    }
}

class TranslucentScrollBarUI extends BasicScrollBarUI {
    private static final Color DEFAULT_COLOR  = new Color(220, 100, 100, 100);
    private static final Color DRAGGING_COLOR = new Color(200, 100, 100, 100);
    private static final Color ROLLOVER_COLOR = new Color(255, 120, 100, 100);

    @Override protected JButton createDecreaseButton(int orientation) {
        return new ZeroSizeButton();
    }
    @Override protected JButton createIncreaseButton(int orientation) {
        return new ZeroSizeButton();
    }
    @Override protected void paintTrack(Graphics g, JComponent c, Rectangle r) {
        //Graphics2D g2 = (Graphics2D) g.create();
        //g2.setPaint(new Color(100, 100, 100, 100));
        //g2.fillRect(r.x, r.y, r.width - 1, r.height - 1);
        //g2.dispose();
    }
    @Override protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
        JScrollBar sb = (JScrollBar) c;
        Color color;
        if (!sb.isEnabled() || r.width > r.height) {
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
        g2.fillRect(r.x, r.y, r.width - 1, r.height - 1);
        g2.setPaint(Color.WHITE);
        g2.drawRect(r.x, r.y, r.width - 1, r.height - 1);
        g2.dispose();
    }
}
