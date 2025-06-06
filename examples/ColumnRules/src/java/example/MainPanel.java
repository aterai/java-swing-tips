// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.plaf.LayerUI;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTabbedPane tabs = new JTabbedPane();
    tabs.addTab("GridBagLayout", makePanel1());
    tabs.addTab("GridLayout + Border", makePanel2());
    tabs.addTab("GridLayout + JLayer", makePanel3());
    add(tabs);
    setPreferredSize(new Dimension(320, 240));
  }

  private static Component makePanel1() {
    JPanel p = new JPanel(new GridBagLayout());
    p.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    GridBagConstraints c = new GridBagConstraints();
    c.anchor = GridBagConstraints.CENTER;
    c.gridheight = 1;
    c.gridwidth = 1;
    c.gridx = 0;
    c.gridy = 0;
    c.weightx = 1.0;
    c.weighty = 1.0;

    c.fill = GridBagConstraints.BOTH;
    p.add(new JLabel("left", SwingConstants.CENTER), c);

    c.gridx = 1;
    c.weightx = 0.0;
    c.fill = GridBagConstraints.VERTICAL;
    p.add(new JSeparator(SwingConstants.VERTICAL), c);

    c.gridx = 2;
    c.weightx = 1.0;
    c.fill = GridBagConstraints.BOTH;
    p.add(new JLabel("right", SwingConstants.CENTER), c);
    return p;
  }

  private static Component makePanel2() {
    JPanel p = new JPanel(new GridLayout(0, 2, 5, 5)) {
      @Override public void updateUI() {
        super.updateUI();
        Border b = BorderFactory.createEmptyBorder(5, 5, 5, 5);
        setBorder(BorderFactory.createCompoundBorder(b, new ColumnRulesBorder()));
      }
    };
    p.add(new JLabel("left", SwingConstants.CENTER));
    p.add(new JLabel("right", SwingConstants.CENTER));
    return p;
  }

  private static Component makePanel3() {
    JPanel p = new JPanel(new GridLayout(0, 2, 5, 5));
    p.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    p.add(new JLabel("left", SwingConstants.CENTER));
    p.add(new JLabel("right", SwingConstants.CENTER));
    return new JLayer<>(p, new ColumnRulesLayerUI());
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

class ColumnRulesBorder implements Border {
  private final Insets insets = new Insets(0, 0, 0, 0);
  private final JSeparator separator = new JSeparator(SwingConstants.VERTICAL);
  private final Container renderer = new JPanel();

  @Override public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
    if (c instanceof JComponent) {
      Rectangle r = SwingUtilities.calculateInnerArea((JComponent) c, null);
      int sw = separator.getPreferredSize().width;
      int sh = r.height;
      int sx = (int) (r.getCenterX() - sw / 2d);
      int sy = (int) r.getMinY();
      Graphics2D g2 = (Graphics2D) g.create();
      SwingUtilities.paintComponent(g2, separator, renderer, sx, sy, sw, sh);
      g2.dispose();
    }
  }

  @Override public Insets getBorderInsets(Component c) {
    return insets;
  }

  @Override public boolean isBorderOpaque() {
    return true;
  }
}

class ColumnRulesLayerUI extends LayerUI<JComponent> {
  private final JSeparator separator = new JSeparator(SwingConstants.VERTICAL);
  private final Container renderer = new JPanel();

  @Override public void updateUI(JLayer<? extends JComponent> l) {
    super.updateUI(l);
    SwingUtilities.updateComponentTreeUI(separator);
  }

  @Override public void paint(Graphics g, JComponent c) {
    super.paint(g, c);
    if (c instanceof JLayer) {
      JComponent tc = (JComponent) ((JLayer<?>) c).getView();
      Rectangle r = SwingUtilities.calculateInnerArea(tc, null);
      int sw = separator.getPreferredSize().width;
      int sh = r.height;
      int sx = (int) (r.getCenterX() - sw / 2d);
      int sy = (int) r.getMinY();
      Graphics2D g2 = (Graphics2D) g.create();
      SwingUtilities.paintComponent(g2, separator, renderer, sx, sy, sw, sh);
      g2.dispose();
    }
  }
}
