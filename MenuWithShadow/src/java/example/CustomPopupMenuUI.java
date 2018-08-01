package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@
import java.awt.*;
import java.awt.image.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicPopupMenuUI;

public class CustomPopupMenuUI extends BasicPopupMenuUI {
    public static final int OFF = 4;
    public static final int ARC = 2;
    public static final float ALPHA = .12f;

    public static ComponentUI createUI(JComponent c) {
        return new CustomPopupMenuUI();
    }
    // public static JFrame frame = null;
    // private static boolean isInRootPanel(JComponent popup, Point p) {
    //     if (Objects.isNull(frame)) {
    //         return false;
    //     }
    //     Rectangle r = frame.getBounds();
    //     Dimension d = popup.getPreferredSize();
    //     return r.contains(p.x, p.y, d.width + OFF, d.height + OFF);
    // }

    // // JDK 1.6.0
    // private static boolean isHeavyWeightContainer(Component contents) {
    //     for (Container p = contents.getParent(); p != null; p = p.getParent()) {
    //         if (p instanceof JWindow || p instanceof Panel) {
    //             return true;
    //         }
    //     }
    //     return false;
    // }
    // static class ShadowBorder extends AbstractBorder {
    //     private final transient BufferedImage screenShot;
    //     protected ShadowBorder(JComponent c, Point p) {
    //         super();
    //         BufferedImage bi = null;
    //         try {
    //             Robot robot = new Robot();
    //             Dimension dim = c.getPreferredSize();
    //             Rectangle rect = new Rectangle(p.x, p.y, dim.width + OFF, dim.height + OFF);
    //             bi = robot.createScreenCapture(rect);
    //         } catch (AWTException ex) {
    //             ex.printStackTrace();
    //         }
    //         screenShot = bi;
    //     }
    //     @Override public Insets getBorderInsets(Component c) {
    //         return new Insets(0, 0, OFF, OFF);
    //     }
    //     @Override public void paintBorder(Component comp, Graphics g, int x, int y, int w, int h) {
    //         if (screenShot == null) {
    //             return;
    //         }
    //         Graphics2D g2 = (Graphics2D) g.create();
    //         g2.drawImage(screenShot, x, y, comp);
    //         g2.drawImage(makeShadowImage(x, y, w, h), x, y, comp);
    //         g2.dispose();
    //     }
    // }
    // @Override public Popup getPopup(JPopupMenu popup, int x, int y) {
    //     Popup pp = super.getPopup(popup, x, y);
    //     JPanel panel = (JPanel) SwingUtilities.getUnwrappedParent(popup);
    //     if (isHeavyWeightContainer(panel)) {
    //         System.out.println("outer");
    //         Point p = new Point(x, y);
    //         panel.setBorder(new ShadowBorder(panel, p));
    //     } else {
    //         System.out.println("inner");
    //         panel.setBorder(new ShadowBorderInPanel());
    //     }
    //     panel.setOpaque(false);
    //     return pp;
    // }

    @Override public Popup getPopup(JPopupMenu popup, int x, int y) {
        Popup pp = super.getPopup(popup, x, y);
        if (pp != null) {
            System.out.println(pp);
            EventQueue.invokeLater(() -> {
                Window p = SwingUtilities.getWindowAncestor(popup);
                if (p instanceof JWindow) {
                    p.setBackground(new Color(0x0, true)); // JDK 1.7.0
                }
            });
            Container c = SwingUtilities.getUnwrappedParent(popup);
            if (c instanceof JComponent) {
                JComponent panel = (JComponent) c;
                panel.setBorder(new ShadowBorderInPanel());
                panel.setOpaque(false);
            }
        }
        return pp;
    }

    protected static BufferedImage makeShadowImage(int x, int y, int w, int h) {
        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, ALPHA));
        g2.setPaint(Color.BLACK);
        g2.translate(x, y);
        for (int i = 0; i < OFF; i++) {
            g2.fillRoundRect(OFF, OFF, w - OFF - OFF + i, h - OFF - OFF + i, ARC, ARC);
        }
        g2.dispose();
        return image;
    }

    protected static class ShadowBorderInPanel extends AbstractBorder {
        @Override public Insets getBorderInsets(Component c) {
            return new Insets(0, 0, OFF, OFF);
        }
        @Override public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.drawImage(makeShadowImage(x, y, w, h), x, y, c);
            g2.dispose();
        }
    }
}
