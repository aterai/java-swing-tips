// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.geom.Ellipse2D;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    URL url = cl.getResource("example/anime.gif");
    Icon icon = Optional.ofNullable(url).<Icon>map(ImageIcon::new)
        .orElseGet(() -> UIManager.getIcon("html.missingImage"));

    JLabel l1 = new JLabel("Timer Animated ToolTip") {
      @Override public JToolTip createToolTip() {
        JToolTip tip = new AnimatedToolTip(new AnimatedLabel(""));
        tip.setComponent(this);
        return tip;
      }
    };
    l1.setToolTipText("Test1");

    JLabel l2 = new JLabel("Gif Animated ToolTip") {
      // private final Icon icon = new ImageIcon(url);
      @Override public JToolTip createToolTip() {
        JLabel label = new JLabel("", icon, LEFT);
        JToolTip tip = new AnimatedToolTip(label);
        tip.setComponent(this);
        return tip;
      }
    };
    l2.setToolTipText("Test2");

    JLabel l3 = new JLabel("Gif Animated ToolTip(html)");
    l3.setToolTipText(String.format("<html><img src='%s'>Test3</html>", url));

    JPanel p1 = new JPanel(new BorderLayout());
    p1.setBorder(BorderFactory.createTitledBorder("javax.swing.Timer"));
    p1.add(l1);
    JPanel p2 = new JPanel(new BorderLayout());
    p2.setBorder(BorderFactory.createTitledBorder("Animated Gif"));
    p2.add(l2, BorderLayout.NORTH);
    p2.add(l3, BorderLayout.SOUTH);

    Box box = Box.createVerticalBox();
    box.add(p1);
    box.add(Box.createVerticalStrut(20));
    box.add(p2);
    box.add(Box.createVerticalGlue());
    add(box);
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
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
      Logger.getGlobal().severe(ex::getMessage);
      return;
    }
    JFrame frame = new JFrame("@title@");
    // frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

class AnimatedToolTip extends JToolTip {
  private final JLabel iconLabel;

  protected AnimatedToolTip(JLabel label) {
    super();
    this.iconLabel = label;
    LookAndFeel.installColorsAndFont(
        iconLabel, "ToolTip.background", "ToolTip.foreground", "ToolTip.font");
    iconLabel.setOpaque(true);
    setLayout(new BorderLayout());
    add(iconLabel);
  }

  @Override public final void setLayout(LayoutManager mgr) {
    super.setLayout(mgr);
  }

  @Override public final Component add(Component comp) {
    return super.add(comp);
  }

  @Override public Dimension getPreferredSize() {
    return getLayout().preferredLayoutSize(this);
  }

  // @Override public Dimension getPreferredSize() {
  //   Insets i = getInsets();
  //   Dimension d = iconLabel.getPreferredSize();
  //   d.width += i.left + i.right;
  //   d.height += i.top + i.bottom;
  //   return d;
  // }

  @Override public void setTipText(String tipText) {
    String oldValue = iconLabel.getText();
    iconLabel.setText(tipText);
    firePropertyChange("tiptext", oldValue, tipText);
  }

  @Override public String getTipText() {
    // return Objects.nonNull(iconLabel) ? iconLabel.getText() : "";
    return Optional.ofNullable(iconLabel).map(JLabel::getText).orElse("");
  }
}

class AnimatedLabel extends JLabel {
  private transient HierarchyListener listener;
  private final Timer animator = new Timer(100, null);

  protected AnimatedLabel(String title) {
    super(title, new AnimeIcon(), LEADING);
    animator.addActionListener(e -> {
      Icon icon = getIcon();
      if (icon instanceof AnimeIcon) {
        ((AnimeIcon) icon).next();
      }
      repaint();
    });
  }

  @Override public void updateUI() {
    removeHierarchyListener(listener);
    super.updateUI();
    setOpaque(true);
    listener = e -> {
      if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0) {
        if (e.getComponent().isShowing()) {
          startAnimation();
        } else {
          stopAnimation();
        }
      }
    };
    addHierarchyListener(listener);
  }

  private void startAnimation() {
    Icon icon = getIcon();
    if (icon instanceof AnimeIcon) {
      ((AnimeIcon) icon).setRunning(true);
    }
    animator.start();
  }

  private void stopAnimation() {
    Icon icon = getIcon();
    if (icon instanceof AnimeIcon) {
      ((AnimeIcon) icon).setRunning(false);
    }
    animator.stop();
  }
}

class AnimeIcon implements Icon {
  private static final Color ELLIPSE_COLOR = new Color(0x80_80_80);
  private static final double R = 2d;
  private static final double SX = 1d;
  private static final double SY = 1d;
  private static final int WIDTH = (int) (R * 8 + SX * 2);
  private static final int HEIGHT = (int) (R * 8 + SY * 2);
  private final List<Shape> list = new ArrayList<>(Arrays.asList(
      new Ellipse2D.Double(SX + 3 * R, SY + 0 * R, 2 * R, 2 * R),
      new Ellipse2D.Double(SX + 5 * R, SY + 1 * R, 2 * R, 2 * R),
      new Ellipse2D.Double(SX + 6 * R, SY + 3 * R, 2 * R, 2 * R),
      new Ellipse2D.Double(SX + 5 * R, SY + 5 * R, 2 * R, 2 * R),
      new Ellipse2D.Double(SX + 3 * R, SY + 6 * R, 2 * R, 2 * R),
      new Ellipse2D.Double(SX + 1 * R, SY + 5 * R, 2 * R, 2 * R),
      new Ellipse2D.Double(SX + 0 * R, SY + 3 * R, 2 * R, 2 * R),
      new Ellipse2D.Double(SX + 1 * R, SY + 1 * R, 2 * R, 2 * R)));

  private boolean running;

  public void next() {
    if (running) {
      // list.add(list.remove(0));
      Collections.rotate(list, 1);
    }
  }

  public void setRunning(boolean isRunning) {
    running = isRunning;
  }

  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();
    // g2.setPaint(Objects.nonNull(c) ? c.getBackground() : Color.WHITE);
    g2.setPaint(Optional.ofNullable(c).map(Component::getBackground).orElse(Color.WHITE));
    g2.fillRect(x, y, getIconWidth(), getIconHeight());
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setPaint(ELLIPSE_COLOR);
    g2.translate(x, y);

    int size = list.size();
    list.forEach(s -> {
      float alpha = running ? (list.indexOf(s) + 1f) / size : .5f;
      g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
      g2.fill(s);
    });

    // for (int i = 0; i < size; i++) {
    //   float alpha = running ? (i + 1f) / size : .5f;
    //   g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
    //   g2.fill(list.get(i));
    // }
    g2.dispose();
  }

  @Override public int getIconWidth() {
    return WIDTH;
  }

  @Override public int getIconHeight() {
    return HEIGHT;
  }
}
