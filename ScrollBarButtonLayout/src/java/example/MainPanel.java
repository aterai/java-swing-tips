package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@
import java.awt.*;
import javax.swing.*;
import javax.swing.plaf.metal.MetalScrollBarUI;
import javax.swing.table.*;

import com.sun.java.swing.plaf.windows.WindowsScrollBarUI;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());

        JScrollBar scrollbar = new JScrollBar(Adjustable.VERTICAL);
        scrollbar.setUnitIncrement(10);
        if (scrollbar.getUI() instanceof WindowsScrollBarUI) {
            scrollbar.setUI(new WindowsCustomScrollBarUI());
        } else {
            scrollbar.setUI(new MetalCustomScrollBarUI());
        }
        JScrollPane scroll = new JScrollPane(new JTable(new DefaultTableModel(20, 3)));
        scroll.setVerticalScrollBar(scrollbar);
        add(scroll);
        setPreferredSize(new Dimension(320, 240));
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

// Copied from javax.swing.plaf.basic.BasicScrollBarUI
class WindowsCustomScrollBarUI extends WindowsScrollBarUI {
    @SuppressWarnings({"PMD.ExcessiveMethodLength", "checkstyle:variabledeclarationusagedistance"})
    @Override protected void layoutVScrollbar(JScrollBar sb) {
        Dimension sbSize = sb.getSize();
        Insets sbInsets = sb.getInsets();

        /* Width and left edge of the buttons and thumb. */
        int itemW = sbSize.width - sbInsets.left - sbInsets.right;
        int itemX = sbInsets.left;

        /* Nominal locations of the buttons, assuming their preferred
         * size will fit.
         */
        // boolean squareButtons = DefaultLookup.getBoolean(scrollbar, this, "ScrollBar.squareButtons", false);
        // int decrButtonH = squareButtons ? itemW : decrButton.getPreferredSize().height;
        // int incrButtonH = squareButtons ? itemW : incrButton.getPreferredSize().height;
        int decrButtonH = decrButton.getPreferredSize().height;
        int incrButtonH = incrButton.getPreferredSize().height;

        // int decrButtonY = sbInsets.top;
        int decrButtonY = sbSize.height - sbInsets.bottom - incrButtonH - decrButtonH;
        int incrButtonY = sbSize.height - sbInsets.bottom - incrButtonH;

        /* The thumb must fit within the height left over after we
         * subtract the preferredSize of the buttons and the insets
         * and the gaps
         */
        int sbInsetsH = sbInsets.top + sbInsets.bottom;
        int sbButtonsH = decrButtonH + incrButtonH;

        // // need before 1.7.0 ---->
        // int decrGap = 0;
        // int incrGap = 0;
        // // incrGap = UIManager.getInt("ScrollBar.incrementButtonGap");
        // // decrGap = UIManager.getInt("ScrollBar.decrementButtonGap");
        // // <----

        int gaps = decrGap + incrGap;
        float trackH = sbSize.height - sbInsetsH - sbButtonsH - gaps;

        /* Compute the height and origin of the thumb. The case
         * where the thumb is at the bottom edge is handled specially
         * to avoid numerical problems in computing thumbY. Enforce
         * the thumbs min/max dimensions. If the thumb doesn't
         * fit in the track (trackH) we'll hide it later.
         */
        float min = sb.getMinimum();
        float extent = sb.getVisibleAmount();
        float range = sb.getMaximum() - min;
        // float value = getValue(sb);
        float value = sb.getValue();

        int thumbH = range <= 0 ? getMaximumThumbSize().height : (int) (trackH * (extent / range));
        thumbH = Math.max(thumbH, getMinimumThumbSize().height);
        thumbH = Math.min(thumbH, getMaximumThumbSize().height);

        int thumbY = incrButtonY - incrGap - thumbH;
        if (value < (sb.getMaximum() - sb.getVisibleAmount())) {
            float thumbRange = trackH - thumbH;
            thumbY = (int) (.5f + thumbRange * ((value - min) / (range - extent)));
            // thumbY += decrButtonY + decrButtonH + decrGap;
        }

        /* If the buttons don't fit, allocate half of the available
         * space to each and move the lower one (incrButton) down.
         */
        int sbAvailButtonH = sbSize.height - sbInsetsH;
        if (sbAvailButtonH < sbButtonsH) {
            incrButtonH = sbAvailButtonH / 2;
            decrButtonH = sbAvailButtonH / 2;
            incrButtonY = sbSize.height - sbInsets.bottom - incrButtonH;
        }
        decrButton.setBounds(itemX, decrButtonY, itemW, decrButtonH);
        incrButton.setBounds(itemX, incrButtonY, itemW, incrButtonH);

        /* Update the trackRect field. */
        // int itrackY = decrButtonY + decrButtonH + decrGap;
        // int itrackH = incrButtonY - incrGap - itrackY;
        int itrackY = 0;
        int itrackH = decrButtonY - itrackY;
        trackRect.setBounds(itemX, itrackY, itemW, itrackH);

        /* If the thumb isn't going to fit, zero it's bounds. Otherwise
         * make sure it fits between the buttons. Note that setting the
         * thumbs bounds will cause a repaint.
         */
        if (thumbH >= (int) trackH) {
            setThumbBounds(0, 0, 0, 0);
        } else {
            // if ((thumbY + thumbH) > incrButtonY - incrGap) {
            //     thumbY = incrButtonY - incrGap - thumbH;
            // }
            // if (thumbY < (decrButtonY + decrButtonH + decrGap)) {
            //     thumbY = decrButtonY + decrButtonH + decrGap + 1;
            // }
            if ((thumbY + thumbH) > decrButtonY - decrGap) {
                thumbY = decrButtonY - decrGap - thumbH;
            }
            if (thumbY < 0) { // (decrButtonY + decrButtonH + decrGap)) {
                thumbY = 1; // decrButtonY + decrButtonH + decrGap + 1;
            }
            setThumbBounds(itemX, thumbY, itemW, thumbH);
        }
    }
}

class MetalCustomScrollBarUI extends MetalScrollBarUI {
    @SuppressWarnings({"PMD.ExcessiveMethodLength", "checkstyle:variabledeclarationusagedistance"})
    @Override protected void layoutVScrollbar(JScrollBar sb) {
        Dimension sbSize = sb.getSize();
        Insets sbInsets = sb.getInsets();

        /* Width and left edge of the buttons and thumb. */
        int itemW = sbSize.width - sbInsets.left - sbInsets.right;
        int itemX = sbInsets.left;

        /* Nominal locations of the buttons, assuming their preferred
         * size will fit.
         */
        // boolean squareButtons = DefaultLookup.getBoolean(scrollbar, this, "ScrollBar.squareButtons", false);
        // int decrButtonH = squareButtons ? itemW : decrButton.getPreferredSize().height;
        // int incrButtonH = squareButtons ? itemW : incrButton.getPreferredSize().height;
        int decrButtonH = decrButton.getPreferredSize().height;
        int incrButtonH = incrButton.getPreferredSize().height;

        // int decrButtonY = sbInsets.top;
        int decrButtonY = sbSize.height - sbInsets.bottom - incrButtonH - decrButtonH;
        int incrButtonY = sbSize.height - sbInsets.bottom - incrButtonH;

        /* The thumb must fit within the height left over after we
         * subtract the preferredSize of the buttons and the insets
         * and the gaps
         */
        int sbInsetsH = sbInsets.top + sbInsets.bottom;
        int sbButtonsH = decrButtonH + incrButtonH;

        // // need before 1.7.0 ---->
        // int decrGap = 0;
        // int incrGap = 0;
        // // incrGap = UIManager.getInt("ScrollBar.incrementButtonGap");
        // // decrGap = UIManager.getInt("ScrollBar.decrementButtonGap");
        // // <----

        int gaps = decrGap + incrGap;
        float trackH = sbSize.height - sbInsetsH - sbButtonsH - gaps;

        /* Compute the height and origin of the thumb. The case
         * where the thumb is at the bottom edge is handled specially
         * to avoid numerical problems in computing thumbY. Enforce
         * the thumbs min/max dimensions. If the thumb doesn't
         * fit in the track (trackH) we'll hide it later.
         */
        float min = sb.getMinimum();
        float extent = sb.getVisibleAmount();
        float range = sb.getMaximum() - min;
        // float value = getValue(sb);
        float value = sb.getValue();

        int thumbH = range <= 0 ? getMaximumThumbSize().height : (int) (trackH * (extent / range));
        thumbH = Math.max(thumbH, getMinimumThumbSize().height);
        thumbH = Math.min(thumbH, getMaximumThumbSize().height);

        int thumbY = incrButtonY - incrGap - thumbH;
        if (value < (sb.getMaximum() - sb.getVisibleAmount())) {
            float thumbRange = trackH - thumbH;
            thumbY = (int) (.5f + thumbRange * ((value - min) / (range - extent)));
            // thumbY += decrButtonY + decrButtonH + decrGap;
        }

        /* If the buttons don't fit, allocate half of the available
         * space to each and move the lower one (incrButton) down.
         */
        int sbAvailButtonH = sbSize.height - sbInsetsH;
        if (sbAvailButtonH < sbButtonsH) {
            incrButtonH = sbAvailButtonH / 2;
            decrButtonH = sbAvailButtonH / 2;
            incrButtonY = sbSize.height - sbInsets.bottom - incrButtonH;
        }
        decrButton.setBounds(itemX, decrButtonY, itemW, decrButtonH);
        incrButton.setBounds(itemX, incrButtonY, itemW, incrButtonH);

        /* Update the trackRect field. */
        // int itrackY = decrButtonY + decrButtonH + decrGap;
        // int itrackH = incrButtonY - incrGap - itrackY;
        int itrackY = 0;
        int itrackH = decrButtonY - itrackY;
        trackRect.setBounds(itemX, itrackY, itemW, itrackH);

        /* If the thumb isn't going to fit, zero it's bounds. Otherwise
         * make sure it fits between the buttons. Note that setting the
         * thumbs bounds will cause a repaint.
         */
        if (thumbH >= (int) trackH) {
            setThumbBounds(0, 0, 0, 0);
        } else {
            // if ((thumbY + thumbH) > incrButtonY - incrGap) {
            //     thumbY = incrButtonY - incrGap - thumbH;
            // }
            // if (thumbY < (decrButtonY + decrButtonH + decrGap)) {
            //     thumbY = decrButtonY + decrButtonH + decrGap + 1;
            // }
            if ((thumbY + thumbH) > decrButtonY - decrGap) {
                thumbY = decrButtonY - decrGap - thumbH;
            }
            if (thumbY < 0) { // (decrButtonY + decrButtonH + decrGap)) {
                thumbY = 1; // decrButtonY + decrButtonH + decrGap + 1;
            }
            setThumbBounds(itemX, thumbY, itemW, thumbH);
        }
    }
}
