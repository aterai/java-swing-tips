// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.geom.Path2D;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.border.Border;

public final class MainPanel extends JPanel {
  private static final Icon SELECTED_ICON = new ScaledIcon(new CheckBoxIcon(), 12, 12);
  private static final Icon EMPTY_ICON = new EmptyIcon();

  private MainPanel() {
    super();
    List<String> styles = Arrays.asList("Bold", "Italic", "Underline");
    add(createSingleSelection(styles, 1));
    List<String> foods = Arrays.asList("hot dogs", "pizza", "ravioli", "bananas");
    add(createMultipleSelection(foods, 1));
    setPreferredSize(new Dimension(320, 240));
  }

  private static JPanel createPanel(int overlap) {
    // A negative hgap overlaps adjacent buttons so that their LineBorders are shared
    JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, -overlap, 0)) {
      @Override public boolean isOptimizedDrawingEnabled() {
        return false;
      }

      @Override public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        d.width = 1000;
        return d;
      }
    };
    p.setBorder(BorderFactory.createEmptyBorder(5, overlap + 5, 5, 5));
    return p;
  }

  private static JPanel createSingleSelection(List<String> list, int overlap) {
    JPanel p = createPanel(overlap);
    ButtonGroup group = new ToggleButtonGroup();
    list.forEach(title -> {
      AbstractButton b = createCheckToggleButton(title);
      p.add(b);
      group.add(b);
    });
    return p;
  }

  private static JPanel createMultipleSelection(List<String> list, int overlap) {
    JPanel p = createPanel(overlap);
    list.forEach(title -> p.add(createCheckToggleButton(title)));
    return p;
  }

  private static AbstractButton createCheckToggleButton(String title) {
    AbstractButton b = new JToggleButton(title) {
      @Override public void updateUI() {
        super.updateUI();
        setBackground(Color.GRAY);
        setBorder(createBorder(getBackground()));
        setContentAreaFilled(false);
        setFocusPainted(false);
        setOpaque(false);
      }
    };
    // An ItemEvent is also fired on a button deselected by the ButtonGroup,
    // so each button only needs to update itself
    b.addItemListener(e -> updateButton(b));
    return b;
  }

  private static void updateButton(AbstractButton button) {
    if (button.getModel().isSelected()) {
      button.setIcon(SELECTED_ICON);
      button.setForeground(Color.WHITE);
      button.setOpaque(true);
    } else {
      button.setIcon(EMPTY_ICON);
      button.setForeground(Color.BLACK);
      button.setOpaque(false);
    }
  }

  private static Border createBorder(Color color) {
    return BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(color),
        BorderFactory.createEmptyBorder(5, 8, 5, 8));
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
      Logger.getGlobal().severe(ex::getMessage);
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

class ScaledIcon implements Icon {
  private final Icon icon;
  private final int width;
  private final int height;

  protected ScaledIcon(Icon icon, int width, int height) {
    this.icon = icon;
    this.width = width;
    this.height = height;
  }

  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.translate(x, y);
    double sx = width / (double) icon.getIconWidth();
    double sy = height / (double) icon.getIconHeight();
    g2.scale(sx, sy);
    icon.paintIcon(c, g2, 0, 0);
    g2.dispose();
  }

  @Override public int getIconWidth() {
    return width;
  }

  @Override public int getIconHeight() {
    return height;
  }
}

class CheckBoxIcon implements Icon {
  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    if (c instanceof AbstractButton) {
      Graphics2D g2 = (Graphics2D) g.create();
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2.translate(x, y);
      AbstractButton b = (AbstractButton) c;
      if (b.getModel().isSelected()) {
        g2.setPaint(b.getForeground());
        float s = Math.min(getIconWidth(), getIconHeight()) * .05f;
        float w = getIconWidth() - s - s;
        float h = getIconHeight() - s - s;
        float gw = w / 8f;
        float gh = h / 8f;
        g2.setStroke(new BasicStroke(3f * s));
        // The origin is already translated to (x, y), so do not add x and y again
        Path2D p = new Path2D.Float();
        p.moveTo(2f * gw, .5f * h);
        p.lineTo(.4f * w, h - 2f * gh);
        p.lineTo(w - 2f * gw, 2f * gh);
        g2.draw(p);
      }
      g2.dispose();
    }
  }

  @Override public int getIconWidth() {
    return 1000;
  }

  @Override public int getIconHeight() {
    return 1000;
  }
}

class EmptyIcon implements Icon {
  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    /* do nothing */
  }

  @Override public int getIconWidth() {
    return 0;
  }

  @Override public int getIconHeight() {
    return 0;
  }
}

class ToggleButtonGroup extends ButtonGroup {
  private ButtonModel prevModel;

  @Override public void setSelected(ButtonModel m, boolean b) {
    if (m.equals(prevModel)) {
      clearSelection();
    } else {
      super.setSelected(m, b);
    }
    prevModel = getSelection();
  }
}
