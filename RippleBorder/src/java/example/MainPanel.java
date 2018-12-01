package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public final class MainPanel extends JPanel {
    public MainPanel() {
        super(new BorderLayout());
        Box box = Box.createVerticalBox();
        box.add(makeLabel("asdfasdfas"));
        box.add(makeLabel("zxcvzxcvzxcvzxcvzxcvzxcvzxcv"));
        box.add(makeLabel("1235436873434325"));
        box.add(makeLabel("asdfasdfas"));
        box.add(makeLabel("qwerqwerqwerqwerqwerqwerqwewretwertwetrerqwerqw"));
        box.add(makeLabel("1235436873434325"));
        box.add(Box.createVerticalGlue());
        box.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        add(new JScrollPane(box));
        setPreferredSize(new Dimension(320, 240));
    }
    private JLabel makeLabel(String str) {
        JLabel label = new JLabel(str);
        label.setBorder(new RippleBorder(label, 10));
        return label;
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

class RippleBorder extends EmptyBorder {
    protected final Timer animator;
    protected final JComponent comp;
    protected float count = 1f;

    protected RippleBorder(JComponent c, int width) {
        super(width, width, width, width);
        this.comp = c;
        animator = new Timer(80, e -> {
            comp.repaint();
            count += .9f;
        });
        comp.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                comp.setForeground(Color.RED);
                animator.start();
            }
            @Override public void mouseExited(MouseEvent e) {
                comp.setForeground(Color.BLACK);
            }
        });
    }
    @Override public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
        if (!animator.isRunning()) {
            super.paintBorder(c, g, x, y, w, h);
            return;
        }
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setPaint(Color.WHITE);
        float a = 1f / count;
        boolean shouldBeHidden = .12f - a > 1.0e-2;
        if (shouldBeHidden) {
            a = 0f;
        }
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, a));
        Insets i = getBorderInsets();
        int xx = i.left - (int) count;
        int yy = i.top - (int) count;
        int ww = i.left + i.right - (int) (count * 2f);
        int hh = i.top + i.bottom - (int) (count * 2f);
        g2.setStroke(new BasicStroke(count * 1.2f));
        g2.drawRoundRect(xx, yy, w - ww, h - hh, 10, 10);
        if (xx < 0 && animator.isRunning()) {
            count = 1f;
            animator.stop();
        }
        g2.dispose();
    }
}
