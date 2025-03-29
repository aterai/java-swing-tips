// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:

package example;

import java.awt.*;
import java.awt.geom.Path2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.basic.BasicHTML;

@SuppressWarnings("PMD.TooManyMethods")
public class TitledBorder2 extends TitledBorder {
  // Space between the border and the component's edge
  // protected static final int EDGE_SPACING = 2;

  // Space between the border and text
  // protected static final int TEXT_SPACING = 2;
  protected static final int TEXT_SPACING2 = 5;

  // Horizontal inset of text that is left or right justified
  // protected static final int TEXT_INSET_H = 5;
  protected static final int TEXT_INSET_H2 = 11; // TEXT_SPACING2 * 2 + 1;

  @SuppressWarnings("PMD.UseConcurrentHashMap")
  private final Map<BorderPosition, Integer> positionMap = new EnumMap<>(BorderPosition.class);
  private final JLabel label2;

  public enum BorderPosition {
    DEFAULT_POSITION,
    ABOVE_TOP,
    TOP,
    BELOW_TOP,
    ABOVE_BOTTOM,
    BOTTOM,
    BELOW_BOTTOM
  }

  public TitledBorder2(String title) {
    this(null, title, LEADING, DEFAULT_POSITION, null, null);
  }

  // public TitledBorder2(Border border) {
  //   this(border, "", LEADING, DEFAULT_POSITION, null, null);
  // }

  // public TitledBorder2(Border border, String title) {
  //   this(border, title, LEADING, DEFAULT_POSITION, null, null);
  // }

  // public TitledBorder2(Border border, String title, int justification, int position) {
  //   this(border, title, justification, position, null, null);
  // }

  // public TitledBorder2(Border border, String title, int just, int position, Font font) {
  //   this(border, title, just, position, font, null);
  // }

  /**
   * Creates a TitledBorder instance with the specified border,
   * title, title-just, title-position, title-font, and
   * title-color.
   *
   * @param border  the border
   * @param title  the title the border should display
   * @param just  the justification for the title
   * @param pos  the position for the title
   * @param font  the font of the title
   * @param color  the color of the title
   */
  public TitledBorder2(Border border, String title, int just, int pos, Font font, Color color) {
    super(border, title, just, pos, font, color);
    label2 = new JLabel();
    label2.setOpaque(false);
    label2.putClientProperty(BasicHTML.propertyKey, null);
    installPropertyChangeListeners2();
    positionMap.put(BorderPosition.DEFAULT_POSITION, DEFAULT_POSITION);
    positionMap.put(BorderPosition.ABOVE_TOP, ABOVE_TOP);
    positionMap.put(BorderPosition.TOP, TOP);
    positionMap.put(BorderPosition.BELOW_TOP, BELOW_TOP);
    positionMap.put(BorderPosition.ABOVE_BOTTOM, ABOVE_BOTTOM);
    positionMap.put(BorderPosition.BOTTOM, BOTTOM);
    positionMap.put(BorderPosition.BELOW_BOTTOM, BELOW_BOTTOM);
  }

  /**
   * Paints the border for the specified component with the
   * specified position and size.
   *
   * @param c  the component for which this border is being painted
   * @param g  the paint graphics
   * @param x  the x position of the painted border
   * @param y  the y position of the painted border
   * @param width  the width of the painted border
   * @param height  the height of the painted border
   */
  @Override public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
    if (isTitleNotEmpty() && c != null) {
      Border bdr = getBorder();
      int edge = bdr instanceof TitledBorder2 ? 0 : EDGE_SPACING;
      Rectangle br = new Rectangle(x, y, width, height);
      br.grow(-edge, -edge);
      // Rectangle br = new Rectangle();
      // br.x = x + edge;
      // br.y = y + edge;
      // br.width = width - edge - edge;
      // br.height = height - edge - edge;

      Rectangle lr = new Rectangle();
      lr.y = y;
      Dimension size = getLabel2(c).getPreferredSize();
      lr.height = size.height;

      Insets ins = TitledBorderUtils.getBorderInsets(bdr, c);
      initPositionRect(height, edge, ins, br, lr);

      ins.left += edge + TEXT_INSET_H2;
      ins.right += edge + TEXT_INSET_H2;
      int justification = getJustification2(c);
      initJustification(justification, x, width, lr, size, ins);

      paintWrapBorder(c, bdr, g, br, lr);
      g.translate(lr.x, lr.y);
      label2.setSize(lr.width, lr.height);
      label2.paint(g);
      g.translate(-lr.x, -lr.y);
    } else {
      super.paintBorder(c, g, x, y, width, height);
    }
  }

  private void initJustification(int just, int x, int w, Rectangle lr, Dimension sz, Insets ins) {
    lr.x = x;
    lr.width = w - ins.left - ins.right;
    if (lr.width > sz.width) {
      lr.width = sz.width;
    }
    switch (just) {
      case LEFT:
        lr.x += ins.left;
        break;
      case RIGHT:
        lr.x += w - ins.right - lr.width;
        break;
      case CENTER:
        lr.x += (w - lr.width) / 2;
        break;
      default:
        // will NOT execute because of the line preceding the switch.
    }
  }

  private void initPositionRect(int height, int edge, Insets ins, Rectangle br, Rectangle lr) {
    switch (getPosition2()) {
      case ABOVE_TOP:
        ins.left = 0;
        ins.right = 0;
        br.y += lr.height - edge;
        br.height -= lr.height - edge;
        break;
      case TOP:
        ins.top = edge + ins.top / 2 - lr.height / 2;
        if (ins.top < edge) {
          br.y -= ins.top;
          br.height += ins.top;
        } else {
          lr.y += ins.top;
        }
        break;
      case BELOW_TOP:
        lr.y += ins.top + edge;
        break;
      case ABOVE_BOTTOM:
        lr.y += height - lr.height - ins.bottom - edge;
        break;
      case BOTTOM:
        lr.y += height - lr.height;
        ins.bottom = edge + (ins.bottom - lr.height) / 2;
        if (ins.bottom < edge) {
          br.height += ins.bottom;
        } else {
          lr.y -= ins.bottom;
        }
        break;
      case BELOW_BOTTOM:
        ins.left = 0;
        ins.right = 0;
        lr.y += height - lr.height;
        br.height -= lr.height - edge;
        break;
      default:
        // will NOT execute because of the line preceding the switch.
    }
  }

  private void paintWrapBorder(Component c, Border bdr, Graphics g, Rectangle b, Rectangle l) {
    Graphics2D g2 = (Graphics2D) g.create();
    int pos = getPosition2();
    if (pos == TOP || pos == BOTTOM) {
      int tsp = TEXT_SPACING2;
      Path2D p = new Path2D.Float();
      appendRect(p, b.x, b.y, b.width, l.y - b.y);
      appendRect(p, b.x, l.y, l.x - b.x - tsp, l.height);
      appendRect(p, l.x + l.width + tsp, l.y, b.x - l.x + b.width - l.width - tsp, l.height);
      appendRect(p, b.x, l.y + l.height, b.width, b.y - l.y + b.height - l.height);
      g2.clip(p);
    }
    bdr.paintBorder(c, g2, b.x, b.y, b.width, b.height);
    g2.dispose();
  }

  private static void appendRect(Path2D p, int x, int y, int w, int h) {
    p.append(new Rectangle(x, y, w, h), false);
  }

  private boolean isTitleNotEmpty() {
    Border bdr = getBorder();
    String str = getTitle();
    return Objects.nonNull(bdr) && Objects.nonNull(str) && !str.isEmpty();
  }

  /**
   * Reinitialize the insets parameter with this Border's current Insets.
   *
   * @param c  the component for which this border insets value applies
   * @param insets  the object to be reinitialized
   */
  @Override public Insets getBorderInsets(Component c, Insets insets) {
    return isTitleNotEmpty()
        ? getTitleLabelInsets(c, insets)
        : super.getBorderInsets(c, insets);
  }

  private Insets getTitleLabelInsets(Component c, Insets insets) {
    int edge = getBorder() instanceof TitledBorder2 ? 0 : EDGE_SPACING;
    Dimension size = getLabel2(c).getPreferredSize();
    TitledBorderUtils.initInsets(insets, getPosition2(), edge, size);
    insets.top += edge + TEXT_SPACING2;
    insets.left += edge + TEXT_SPACING2;
    insets.right += edge + TEXT_SPACING2;
    insets.bottom += edge + TEXT_SPACING2;
    return insets;
  }

  // private int getPosition2() {
  //   int position = getTitlePosition();
  //   if (position != DEFAULT_POSITION) {
  //     return position;
  //   }
  //   Object value = UIManager.get("TitledBorder.position");
  //   if (value instanceof Integer) {
  //     int i = (Integer) value;
  //     if (0 < i && i <= 6) {
  //       return i;
  //     }
  //   } else if (value instanceof String) {
  //     Integer aboveTop = TitledBorderUtils.getPositionByString(value);
  //     if (aboveTop != null) {
  //       return aboveTop;
  //     }
  //   }
  //   return TOP;
  // }

  private int getPosition2() {
    int position = getTitlePosition();
    if (position == DEFAULT_POSITION) {
      Object value = UIManager.get("TitledBorder.position");
      if (value instanceof Integer && positionMap.containsValue(value)) {
        position = (Integer) value;
      } else if (value instanceof String) {
        position = positionMap.keySet().stream()
            .filter(key -> value.equals(key.name()))
            .findFirst()
            .map(positionMap::get)
            .orElse(ABOVE_TOP);
      } else {
        position = TOP;
      }
    }
    return position;
  }

  private int getJustification2(Component c) {
    int justification = getTitleJustification();
    if (justification == LEADING || justification == DEFAULT_JUSTIFICATION) {
      justification = c.getComponentOrientation().isLeftToRight() ? LEFT : RIGHT;
    } else if (justification == TRAILING) {
      justification = c.getComponentOrientation().isLeftToRight() ? RIGHT : LEFT;
    }
    return justification;
  }

  // private Color getColor2(Component c) {
  //   Color color = getTitleColor();
  //   return Objects.nonNull(color) ? color : c.getForeground();
  // }

  private JLabel getLabel2(Component c) {
    label2.setText(getTitle());
    label2.setFont(getFont(c));
    Color color = getTitleColor();
    label2.setForeground(Objects.nonNull(color) ? color : c.getForeground());
    label2.setComponentOrientation(c.getComponentOrientation());
    label2.setEnabled(c.isEnabled());
    return label2;
  }

  private void installPropertyChangeListeners2() {
    WeakReference<TitledBorder2> weakReference = new WeakReference<>(this);
    PropertyChangeListener listener = new PropertyChangeListener() {
      @Override public void propertyChange(PropertyChangeEvent e) {
        if (weakReference.get() == null) {
          UIManager.removePropertyChangeListener(this);
          UIManager.getDefaults().removePropertyChangeListener(this);
        } else {
          labelUpdate(e.getPropertyName());
        }
      }
    };
    UIManager.addPropertyChangeListener(listener);
    UIManager.getDefaults().addPropertyChangeListener(listener);
  }

  protected final void labelUpdate(String prop) {
    boolean flg = "lookAndFeel".equals(prop) || "LabelUI".equals(prop);
    if (flg) {
      label2.updateUI();
    }
  }
}

final class TitledBorderUtils {
  private TitledBorderUtils() {
    /* Singleton */
  }

  // private static Insets getBorderInsets(Border bdr, Component c, Insets insets) {
  public static Insets getBorderInsets(Border bdr, Component c) {
    Insets insets = new Insets(0, 0, 0, 0);
    if (bdr instanceof AbstractBorder) {
      AbstractBorder ab = (AbstractBorder) bdr;
      insets = ab.getBorderInsets(c, insets);
    } else if (Objects.nonNull(bdr)) {
      Insets i = bdr.getBorderInsets(c);
      insets.set(i.top, i.left, i.bottom, i.right);
    }
    return insets;
  }

  public static void initInsets(Insets insets, int position, int edge, Dimension size) {
    switch (position) {
      case TitledBorder.ABOVE_TOP:
        insets.top += size.height - edge;
        break;
      case TitledBorder.TOP:
        if (insets.top < size.height) {
          insets.top = size.height - edge;
        }
        break;
      case TitledBorder.BELOW_TOP:
        insets.top += size.height;
        break;
      case TitledBorder.ABOVE_BOTTOM:
        insets.bottom += size.height;
        break;
      case TitledBorder.BOTTOM:
        if (insets.bottom < size.height) {
          insets.bottom = size.height - edge;
        }
        break;
      case TitledBorder.BELOW_BOTTOM:
        insets.bottom += size.height - edge;
        break;
      default:
        // will NOT execute because of the line preceding the switch.
    }
  }

  // public static Integer getPositionByString(Object value) {
  //   String s = Objects.toString(value).toUpperCase(Locale.ENGLISH);
  //   Integer ret;
  //   switch (s) {
  //     case "ABOVE_TOP":
  //       ret = TitledBorder.ABOVE_TOP;
  //       break;
  //     case "TOP":
  //       ret = TitledBorder.TOP;
  //       break;
  //     case "BELOW_TOP":
  //       ret = TitledBorder.BELOW_TOP;
  //       break;
  //     case "ABOVE_BOTTOM":
  //       ret = TitledBorder.ABOVE_BOTTOM;
  //       break;
  //     case "BOTTOM":
  //       ret = TitledBorder.BOTTOM;
  //       break;
  //     case "BELOW_BOTTOM":
  //       ret = TitledBorder.BELOW_BOTTOM;
  //       break;
  //     default:
  //       ret = null;
  //       break;
  //   }
  //   return ret;
  // }
}
