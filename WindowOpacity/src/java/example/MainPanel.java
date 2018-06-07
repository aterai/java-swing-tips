package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.IOException;
import java.util.*;
import javax.imageio.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private final transient TexturePaint imageTexture = makeImageTexture();
    private final transient TexturePaint checkerTexture = makeCheckerTexture();
    private transient TexturePaint texture;

    public MainPanel() {
        super();
        setBackground(new Color(.5f, .8f, .5f, .5f));
        add(new JLabel("aaa: "));
        add(new JTextField(10));
        add(new JButton("bbb"));

        JComboBox<String> combo = new JComboBox<>(new String[] {"Color(.5f, .8f, .5f, .5f)", "ImageTexturePaint", "CheckerTexturePaint"});

        // if (System.getProperty("java.version").startsWith("1.7.0")) {
        //     // XXX: JDK 1.7.0 Translucency JFrame + JComboBox bug???
        //     // http://www.oracle.com/technetwork/java/javase/2col/7u6-bugfixes-1733378.html
        //     // [JDK-7156657] Version 7 doesn't support translucent popup menus against a translucent window - Java Bug System
        //     // https://bugs.openjdk.java.net/browse/JDK-7156657
        //     combo.addPopupMenuListener(new TranslucencyFrameComboBoxPopupMenuListener());
        // }
        combo.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                // JComboBox cbox = (JComboBox) e.getSource();
                // Object o = cbox.getSelectedItem();
                Object o = e.getItem();
                if ("ImageTexturePaint".equals(o)) {
                    texture = imageTexture;
                    setOpaque(false);
                } else if ("CheckerTexturePaint".equals(o)) {
                    texture = checkerTexture;
                    setOpaque(false);
                } else {
                    texture = null;
                    setOpaque(true);
                }
                getRootPane().getContentPane().repaint();
                // Window w = SwingUtilities.getWindowAncestor(getRootPane());
                // if (w instanceof JFrame) { // XXX: JDK 1.7.0 ???
                //     // ((JFrame) w).getRootPane().repaint();
                //     ((JFrame) w).getContentPane().repaint();
                // } else {
                //     revalidate();
                //     repaint();
                // }
            }
        });
        add(combo);
        setPreferredSize(new Dimension(320, 240));
    }
    @Override protected void paintComponent(Graphics g) {
        Optional.ofNullable(texture).ifPresent(tx -> {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setPaint(tx);
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.dispose();
        });
        super.paintComponent(g);
    }
    private TexturePaint makeImageTexture() {
        BufferedImage bi = null;
        try {
            bi = ImageIO.read(getClass().getResource("unkaku_w.png"));
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new IllegalArgumentException(ex);
        }
        return new TexturePaint(bi, new Rectangle(bi.getWidth(), bi.getHeight()));
    }
    private TexturePaint makeCheckerTexture() {
        int cs = 6;
        int sz = cs * cs;
        BufferedImage bi = new BufferedImage(sz, sz, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = bi.createGraphics();
        g2.setPaint(new Color(200, 150, 100, 50));
        g2.fillRect(0, 0, sz, sz);
        for (int i = 0; i * cs < sz; i++) {
            for (int j = 0; j * cs < sz; j++) {
                if ((i + j) % 2 == 0) {
                    g2.fillRect(i * cs, j * cs, cs, cs);
                }
            }
        }
        g2.dispose();
        return new TexturePaint(bi, new Rectangle(sz, sz));
    }
    public static void main(String... args) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                createAndShowGUI();
            }
        });
    }
    public static void createAndShowGUI() {
        // try {
        //     UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        // } catch (ClassNotFoundException | InstantiationException
        //        | IllegalAccessException | UnsupportedLookAndFeelException ex) {
        //     ex.printStackTrace();
        // }
        JFrame.setDefaultLookAndFeelDecorated(true);
        JFrame frame = new JFrame("@title@");
        // frame.setUndecorated(true);

        // if (System.getProperty("java.version").startsWith("1.6.0")) {
        //     AWTUtilities.setWindowOpaque(frame, false);
        // }
        frame.setBackground(new Color(0x0, true)); // 1.7.0
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
// // http://www.oracle.com/technetwork/java/javase/2col/7u6-bugfixes-1733378.html
// // [JDK-7156657] Version 7 doesn't support translucent popup menus against a translucent window - Java Bug System
// // https://bugs.openjdk.java.net/browse/JDK-7156657
// class TranslucencyFrameComboBoxPopupMenuListener implements PopupMenuListener {
//     @Override public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
//         EventQueue.invokeLater(() -> {
//             JComboBox combo = (JComboBox) e.getSource();
//             Object o = combo.getAccessibleContext().getAccessibleChild(0);
//             if (o instanceof JComponent) { // BasicComboPopup
//                 ((JComponent) o).repaint();
//             }
//         });
//     }
//     @Override public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {}
//     @Override public void popupMenuCanceled(PopupMenuEvent e) {}
// }
