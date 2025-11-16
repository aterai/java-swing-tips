// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicSeparatorUI;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    add(makeVerticalBox(), BorderLayout.NORTH);
    add(makeHorizontalBox(), BorderLayout.EAST);
    JMenuBar mb = new JMenuBar();
    mb.add(LookAndFeelUtils.createLookAndFeelMenu());
    EventQueue.invokeLater(() -> getRootPane().setJMenuBar(mb));
    setPreferredSize(new Dimension(320, 240));
  }

  private static Box makeVerticalBox() {
    Box box = Box.createVerticalBox();
    box.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    box.add(Box.createVerticalStrut(10));
    box.add(new JSeparator(SwingConstants.HORIZONTAL));
    box.add(Box.createVerticalStrut(10));
    box.add(new JLabel("↑ Default JSeparator"));
    box.add(Box.createVerticalStrut(20));
    box.add(new GradientFadeOutSeparator(SwingConstants.HORIZONTAL));
    box.add(Box.createVerticalStrut(10));
    box.add(new JLabel("↑ GradientFadeOutSeparator"));
    box.add(Box.createVerticalStrut(10));
    return box;
  }

  private static Box makeHorizontalBox() {
    Box box = Box.createHorizontalBox();
    box.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    box.add(Box.createHorizontalStrut(10));
    box.add(new JSeparator(SwingConstants.VERTICAL));
    box.add(Box.createHorizontalStrut(10));
    box.add(new GradientFadeOutSeparator(SwingConstants.VERTICAL));
    box.add(Box.createHorizontalStrut(10));
    return box;
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

class GradientFadeOutSeparator extends JSeparator {
  // protected GradientFadeOutSeparator() {
  //   super(HORIZONTAL);
  // }

  protected GradientFadeOutSeparator(int orientation) {
    super(orientation);
  }

  @Override public void updateUI() {
    super.updateUI();
    setUI(GradientSeparatorUI.createUI(this));
  }
}

class GradientSeparatorUI extends BasicSeparatorUI {
  private Color backgroundColor;
  private Color shadowColor;
  private Color highlightColor;

  public static ComponentUI createUI(JComponent c) {
    return new GradientSeparatorUI();
  }

  private void updateColors(Component c) {
    Color bgc = c.getBackground();
    Color c1 = UIManager.getColor("Panel.background");
    backgroundColor = c1 instanceof ColorUIResource ? c1 : bgc;
    Color c2 = UIManager.getColor("Separator.shadow");
    shadowColor = c2 instanceof ColorUIResource ? c2 : bgc.darker();
    Color c3 = UIManager.getColor("Separator.highlight");
    highlightColor = c3 instanceof ColorUIResource ? c3 : bgc.brighter();
  }

  @Override public void installUI(JComponent c) {
    super.installUI(c);
    updateColors(c);
  }

  @Override public void paint(Graphics g, JComponent c) {
    if (c instanceof JSeparator) {
      Graphics2D g2 = (Graphics2D) g.create();
      Rectangle r = SwingUtilities.calculateInnerArea(c, null);
      float centerX = (float) r.getCenterX();
      float centerY = (float) r.getCenterY();
      Point2D center = new Point2D.Float(centerX, centerY);
      float radius = Math.max(r.width, r.height);
      float[] dist = {.1f, .6f};
      Color[] colors1 = {shadowColor, backgroundColor};
      // = {shadow, shadow, background, background}, {0f, .1f, .6f, 1f}
      Paint p1 = new RadialGradientPaint(center, radius, dist, colors1);
      Color[] colors2 = {highlightColor, backgroundColor};
      Paint p2 = new RadialGradientPaint(center, radius, dist, colors2);
      if (((JSeparator) c).getOrientation() == SwingConstants.HORIZONTAL) {
        g2.setPaint(p1);
        g2.fillRect(0, 0, r.width, 1);
        g2.setPaint(p2);
        g2.fillRect(0, 1, r.width, 1);
      } else {
        g2.setPaint(p1);
        g2.fillRect(0, 0, 1, r.height);
        g2.setPaint(p2);
        g2.fillRect(1, 0, 1, r.height);
      }
      g2.dispose();
    }
  }
}

final class LookAndFeelUtils {
  private static String lookAndFeel = UIManager.getLookAndFeel().getClass().getName();

  private LookAndFeelUtils() {
    /* Singleton */
  }

  public static JMenu createLookAndFeelMenu() {
    JMenu menu = new JMenu("LookAndFeel");
    ButtonGroup buttonGroup = new ButtonGroup();
    for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
      AbstractButton b = makeButton(info);
      initLookAndFeelAction(info, b);
      menu.add(b);
      buttonGroup.add(b);
    }
    return menu;
  }

  private static AbstractButton makeButton(UIManager.LookAndFeelInfo info) {
    boolean selected = info.getClassName().equals(lookAndFeel);
    return new JRadioButtonMenuItem(info.getName(), selected);
  }

  public static void initLookAndFeelAction(UIManager.LookAndFeelInfo info, AbstractButton b) {
    String cmd = info.getClassName();
    b.setText(info.getName());
    b.setActionCommand(cmd);
    b.setHideActionText(true);
    b.addActionListener(e -> setLookAndFeel(cmd));
  }

  private static void setLookAndFeel(String newLookAndFeel) {
    String oldLookAndFeel = lookAndFeel;
    if (!oldLookAndFeel.equals(newLookAndFeel)) {
      try {
        UIManager.setLookAndFeel(newLookAndFeel);
        lookAndFeel = newLookAndFeel;
      } catch (UnsupportedLookAndFeelException ignored) {
        Toolkit.getDefaultToolkit().beep();
      } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
        Logger.getGlobal().severe(ex::getMessage);
        return;
      }
      updateLookAndFeel();
      // firePropertyChange("lookAndFeel", oldLookAndFeel, newLookAndFeel);
    }
  }

  private static void updateLookAndFeel() {
    for (Window window : Window.getWindows()) {
      SwingUtilities.updateComponentTreeUI(window);
    }
  }
}
