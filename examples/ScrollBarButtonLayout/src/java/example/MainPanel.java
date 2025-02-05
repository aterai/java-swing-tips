// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import com.sun.java.swing.plaf.windows.WindowsScrollBarUI;
import java.awt.*;
import javax.swing.*;
import javax.swing.plaf.metal.MetalScrollBarUI;
import javax.swing.plaf.synth.SynthScrollBarUI;
import javax.swing.table.DefaultTableModel;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JScrollBar scrollbar = new JScrollBar(Adjustable.VERTICAL) {
      @Override public void updateUI() {
        super.updateUI();
        if (getUI() instanceof WindowsScrollBarUI) {
          setUI(new WindowsCustomScrollBarUI());
        } else if (!(getUI() instanceof SynthScrollBarUI)) {
          setUI(new MetalCustomScrollBarUI());
        }
        setUnitIncrement(10);
      }
    };
    JScrollPane scroll = new JScrollPane(new JTable(new DefaultTableModel(20, 3)));
    scroll.setVerticalScrollBar(scrollbar);
    add(scroll);
    setPreferredSize(new Dimension(320, 240));
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (UnsupportedLookAndFeelException ignored) {
      Toolkit.getDefaultToolkit().beep();
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
      ex.printStackTrace();
      return;
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
  @Override protected void layoutVScrollbar(JScrollBar sb) {
    Dimension sbSize = sb.getSize();
    Insets sbInsets = sb.getInsets();

    /* Width and left edge of the buttons and thumb. */
    // int itemW = sbSize.width - sbInsets.left - sbInsets.right;
    // int itemX = sbInsets.left;

    /* Nominal locations of the buttons, assuming their preferred
     * size will fit.
     */
    // boolean square = DefaultLookup.getBoolean(sb, this, "ScrollBar.squareButtons", false);
    // int decrButtonH = square ? itemW : decrButton.getPreferredSize().height;
    // int incrButtonH = square ? itemW : incrButton.getPreferredSize().height;
    int decrButtonH = decrButton.getPreferredSize().height;
    int incrButtonH = incrButton.getPreferredSize().height;

    // int decrButtonY = sbInsets.top;
    // int decrButtonY = sbSize.height - sbInsets.bottom - incrButtonH - decrButtonH;
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

    /* If the buttons don't fit, allocate half of the available
     * space to each and move the lower one (incrButton) down.
     */
    int sbAvailButtonH = sbSize.height - sbInsetsH;
    if (sbAvailButtonH < sbButtonsH) {
      incrButtonH = sbAvailButtonH / 2;
      decrButtonH = sbAvailButtonH / 2;
      incrButtonY = sbSize.height - sbInsets.bottom - incrButtonH;
    }

    /* Width and left edge of the buttons and thumb. */
    int itemW = sbSize.width - sbInsets.left - sbInsets.right;
    int itemX = sbInsets.left;
    int decrButtonY = sbSize.height - sbInsets.bottom - incrButtonH - decrButtonH;
    decrButton.setBounds(itemX, decrButtonY, itemW, decrButtonH);
    incrButton.setBounds(itemX, incrButtonY, itemW, incrButtonH);

    /* Update the trackRect field. */
    // int itrackY = decrButtonY + decrButtonH + decrGap;
    // int itrackH = incrButtonY - incrGap - itrackY;
    // int itrackY = 0;
    // int itrackH = decrButtonY - itrackY;
    // trackRect.setBounds(itemX, itrackY, itemW, itrackH);
    trackRect.setBounds(itemX, 0, itemW, decrButtonY);

    /* If the thumb isn't going to fit, zero its bounds. Otherwise,
     * make sure it fits between the buttons. Note that setting the
     * thumbs bounds will cause a repaint.
     */
    int maxHeight = getMaximumThumbSize().height;
    int minHeight = getMinimumThumbSize().height;
    int gaps = decrGap + incrGap;
    /* Compute the height and origin of the thumb. The case
     * where the thumb is at the bottom edge is handled specially
     * to avoid numerical problems in computing thumbY. Enforce
     * the thumbs min/max dimensions. If the thumb doesn't
     * fit in the track (trackH) we'll hide it later.
     */
    float min = sb.getMinimum();
    int max = sb.getMaximum() - sb.getVisibleAmount();
    float extent = sb.getVisibleAmount();
    float range = sb.getMaximum() - min;
    // float value = getValue(sb);
    float value = sb.getValue();
    int trackH = sbSize.height - sbInsetsH - sbButtonsH - gaps;
    int thumbH = ScrollBarUtils.getThumbHeight(trackH, extent, range, maxHeight, minHeight);
    int y = incrButtonY - incrGap - thumbH;
    float maxThumbY = (trackH - thumbH) * ((value - min) / (range - extent));
    int thumbY = ScrollBarUtils.getThumbY(y, max, value, maxThumbY);
    if (thumbH >= trackH) {
      setThumbBounds(0, 0, 0, 0);
    } else {
      // if ((thumbY + thumbH) > incrButtonY - incrGap) {
      //   thumbY = incrButtonY - incrGap - thumbH;
      // }
      // if (thumbY < (decrButtonY + decrButtonH + decrGap)) {
      //   thumbY = decrButtonY + decrButtonH + decrGap + 1;
      // }
      thumbY = Math.max(0, Math.min(thumbY, decrButtonY - decrGap - thumbH));
      setThumbBounds(itemX, thumbY, itemW, thumbH);
    }
  }
}

class MetalCustomScrollBarUI extends MetalScrollBarUI {
  @Override protected void layoutVScrollbar(JScrollBar sb) {
    Dimension sbSize = sb.getSize();
    Insets sbInsets = sb.getInsets();

    /* Width and left edge of the buttons and thumb. */
    // int itemW = sbSize.width - sbInsets.left - sbInsets.right;
    // int itemX = sbInsets.left;

    /* Nominal locations of the buttons, assuming their preferred
     * size will fit.
     */
    // boolean square = DefaultLookup.getBoolean(sb, this, "ScrollBar.squareButtons", false);
    // int decrButtonH = square ? itemW : decrButton.getPreferredSize().height;
    // int incrButtonH = square ? itemW : incrButton.getPreferredSize().height;
    int decrButtonH = decrButton.getPreferredSize().height;
    int incrButtonH = incrButton.getPreferredSize().height;

    // int decrButtonY = sbInsets.top;
    // int decrButtonY = sbSize.height - sbInsets.bottom - incrButtonH - decrButtonH;
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

    /* If the buttons don't fit, allocate half of the available
     * space to each and move the lower one (incrButton) down.
     */
    int sbAvailButtonH = sbSize.height - sbInsetsH;
    if (sbAvailButtonH < sbButtonsH) {
      incrButtonH = sbAvailButtonH / 2;
      decrButtonH = sbAvailButtonH / 2;
      incrButtonY = sbSize.height - sbInsets.bottom - incrButtonH;
    }

    /* Width and left edge of the buttons and thumb. */
    int itemW = sbSize.width - sbInsets.left - sbInsets.right;
    int itemX = sbInsets.left;
    int decrButtonY = sbSize.height - sbInsets.bottom - incrButtonH - decrButtonH;
    decrButton.setBounds(itemX, decrButtonY, itemW, decrButtonH);
    incrButton.setBounds(itemX, incrButtonY, itemW, incrButtonH);

    /* Update the trackRect field. */
    // int itrackY = decrButtonY + decrButtonH + decrGap;
    // int itrackH = incrButtonY - incrGap - itrackY;
    // int itrackY = 0;
    // int itrackH = decrButtonY - itrackY;
    // trackRect.setBounds(itemX, itrackY, itemW, itrackH);
    trackRect.setBounds(itemX, 0, itemW, decrButtonY);

    /* If the thumb isn't going to fit, zero its bounds. Otherwise,
     * make sure it fits between the buttons. Note that setting the
     * thumbs bounds will cause a repaint.
     */
    int maxHeight = getMaximumThumbSize().height;
    int minHeight = getMinimumThumbSize().height;
    int gaps = decrGap + incrGap;
    /* Compute the height and origin of the thumb. The case
     * where the thumb is at the bottom edge is handled specially
     * to avoid numerical problems in computing thumbY. Enforce
     * the thumbs min/max dimensions. If the thumb doesn't
     * fit in the track (trackH) we'll hide it later.
     */
    float min = sb.getMinimum();
    int max = sb.getMaximum() - sb.getVisibleAmount();
    float extent = sb.getVisibleAmount();
    float range = sb.getMaximum() - min;
    // float value = getValue(sb);
    float value = sb.getValue();
    int trackH = sbSize.height - sbInsetsH - sbButtonsH - gaps;
    int thumbH = ScrollBarUtils.getThumbHeight(trackH, extent, range, maxHeight, minHeight);
    int y = incrButtonY - incrGap - thumbH;
    float maxThumbY = (trackH - thumbH) * ((value - min) / (range - extent));
    int thumbY = ScrollBarUtils.getThumbY(y, max, value, maxThumbY);
    if (thumbH >= trackH) {
      setThumbBounds(0, 0, 0, 0);
    } else {
      // if ((thumbY + thumbH) > incrButtonY - incrGap) {
      //   thumbY = incrButtonY - incrGap - thumbH;
      // }
      // if (thumbY < (decrButtonY + decrButtonH + decrGap)) {
      //   thumbY = decrButtonY + decrButtonH + decrGap + 1;
      // }
      thumbY = Math.max(0, Math.min(thumbY, decrButtonY - decrGap - thumbH));
      setThumbBounds(itemX, thumbY, itemW, thumbH);
    }
  }
}

final class ScrollBarUtils {
  private ScrollBarUtils() {
    /* Singleton */
  }

  public static int getThumbHeight(
      int trackH, float extent, float range, int maxThumbH, int minThumbH) {
    int thumbH = range <= 0 ? maxThumbH : (int) (trackH * (extent / range));
    thumbH = Math.min(Math.max(thumbH, minThumbH), maxThumbH);
    return thumbH;
  }

  public static int getThumbY(int y, float max, float value, float maxThumbY) {
    int thumbY = y;
    if (value < max) {
      thumbY = (int) (.5f + maxThumbY);
    }
    return thumbY;
  }
}
