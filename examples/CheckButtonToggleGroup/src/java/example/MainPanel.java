// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.geom.Path2D;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Stream;
import javax.swing.*;
import javax.swing.border.Border;

public final class MainPanel extends JPanel {
  private static final Icon SELECTED_ICON = new ScaledIcon(new CheckBoxIcon(), 12, 12);
  private static final Icon EMPTY_ICON = new EmptyIcon();

  private MainPanel() {
    super();
    List<String> style = Arrays.asList("Bold", "Italic", "Underline");
    add(makeButtonToggles(style, 1));
    List<String> food = Arrays.asList("hot dogs", "pizza", "ravioli", "bananas");
    add(makeMultipleSelection(food, 1));
    setPreferredSize(new Dimension(320, 240));
  }

  private static JPanel makePanel(int overlap) {
    return new JPanel(new FlowLayout(FlowLayout.CENTER, -overlap, 0)) {
      @Override public boolean isOptimizedDrawingEnabled() {
        return false;
      }

      @Override public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        d.width = 1000;
        return d;
      }
    };
  }

  public static JPanel makeButtonToggles(List<String> list, int overlap) {
    JPanel p = makePanel(overlap);
    p.setBorder(BorderFactory.createEmptyBorder(5, overlap + 5, 5, 5));
    ButtonGroup bg = new ToggleButtonGroup();
    list.forEach(title -> {
      AbstractButton b = makeCheckToggleButton(title);
      p.add(b);
      bg.add(b);
    });
    return p;
  }

  public static JPanel makeMultipleSelection(List<String> list, int overlap) {
    JPanel p = makePanel(overlap);
    p.setBorder(BorderFactory.createEmptyBorder(5, overlap + 5, 5, 5));
    list.forEach(title -> {
      AbstractButton b = makeCheckToggleButton(title);
      p.add(b);
    });
    return p;
  }

  private static AbstractButton makeCheckToggleButton(String title) {
    AbstractButton b = new JToggleButton(title) {
      @Override public void updateUI() {
        super.updateUI();
        setBackground(Color.GRAY);
        setBorder(makeBorder(getBackground()));
        setContentAreaFilled(false);
        setFocusPainted(false);
        setOpaque(false);
      }
    };
    b.addActionListener(e -> {
      Container parent = ((AbstractButton) e.getSource()).getParent();
      SwingUtils.descendants(parent)
          .filter(AbstractButton.class::isInstance)
          .map(AbstractButton.class::cast)
          .forEach(MainPanel::updateButton);
    });
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

  private static Border makeBorder(Color bgc) {
    return BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(bgc),
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
      g2.setPaint(b.getForeground());
      float s = Math.min(getIconWidth(), getIconHeight()) * .05f;
      g2.setStroke(new BasicStroke(s));
      float w = getIconWidth() - s - s;
      float h = getIconHeight() - s - s;
      float gw = w / 8f;
      float gh = h / 8f;
      if (b.getModel().isSelected()) {
        g2.setStroke(new BasicStroke(3f * s));
        Path2D p = new Path2D.Float();
        p.moveTo(x + 2f * gw, y + .5f * h);
        p.lineTo(x + .4f * w, y + h - 2f * gh);
        p.lineTo(x + w - 2f * gw, y + 2f * gh);
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

final class SwingUtils {
  private SwingUtils() {
    /* Singleton */
  }

  public static Stream<Component> descendants(Container parent) {
    return Stream.of(parent.getComponents())
        .filter(Container.class::isInstance).map(Container.class::cast)
        .flatMap(c -> Stream.concat(Stream.of(c), descendants(c)));
  }
}
