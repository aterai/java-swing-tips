// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicSeparatorUI;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JPanel p = new JPanel(new GridLayout(2, 1));
    p.add(makeIndentPanel("JSeparator", new JSeparator(), 10));
    p.add(makeIndentPanel("GradientSeparator", new GradientSeparator(), 10));
    add(p);
    add(makeBox(), BorderLayout.EAST);
    setPreferredSize(new Dimension(320, 240));
  }

  private static Box makeBox() {
    Box box = Box.createHorizontalBox();
    box.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    box.add(Box.createHorizontalStrut(10));
    box.add(new JSeparator(SwingConstants.VERTICAL));
    box.add(Box.createHorizontalStrut(10));
    box.add(new GradientSeparator(SwingConstants.VERTICAL));
    box.add(Box.createHorizontalStrut(10));
    return box;
  }

  public static Component makeIndentPanel(String title, JSeparator sp, int indent) {
    JPanel p = new JPanel(new GridBagLayout());
    p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    GridBagConstraints c = new GridBagConstraints();
    c.insets = new Insets(2, 2, 2, 2);
    c.gridwidth = 2;
    c.weightx = 1d;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.anchor = GridBagConstraints.LINE_START;
    p.add(makeTitledPanel(title, sp), c);

    c.insets = new Insets(2, 2 + indent, 2, 2);
    c.gridwidth = 1;
    c.gridy = 1;
    p.add(new JTextField(), c);

    c.insets = new Insets(2, 0, 2, 2);
    c.weightx = 0d;
    c.fill = GridBagConstraints.NONE;
    p.add(new JButton("JButton"), c);

    return p;
  }

  private static Component makeTitledPanel(String title, JSeparator separator) {
    JPanel p = new JPanel(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();
    c.insets = new Insets(2, 2, 2, 2);
    p.add(new JLabel(title), c);

    c.weightx = 1d;
    c.fill = GridBagConstraints.HORIZONTAL;
    p.add(separator, c);

    return p;
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

class GradientSeparator extends JSeparator {
  protected GradientSeparator() {
    super();
  }

  protected GradientSeparator(int orientation) {
    super(orientation);
  }

  @Override public void updateUI() {
    super.updateUI();
    setUI(GradientSeparatorUI.createUI(this));
  }
}

class GradientSeparatorUI extends BasicSeparatorUI {
  private Color bgc;
  private Color ssc;
  private Color shc;

  public static ComponentUI createUI(JComponent c) {
    return new GradientSeparatorUI();
  }

  private void updateColors(Component j) {
    Color c = UIManager.getColor("Panel.background");
    bgc = c instanceof ColorUIResource ? c : j.getBackground();
    c = UIManager.getColor("Separator.shadow");
    ssc = c instanceof ColorUIResource ? c : j.getBackground().brighter();
    c = UIManager.getColor("Separator.highlight");
    shc = c instanceof ColorUIResource ? c : j.getBackground().darker();
  }

  @Override public void installUI(JComponent c) {
    super.installUI(c);
    updateColors(c);
  }

  @Override public void paint(Graphics g, JComponent c) {
    if (c instanceof JSeparator) {
      Graphics2D g2 = (Graphics2D) g.create();
      Dimension s = c.getSize();
      JSeparator js = (JSeparator) c;
      if (js.getOrientation() == SwingConstants.VERTICAL) {
        g2.setPaint(new GradientPaint(0f, 0f, ssc, 0f, s.height, bgc, true));
        g2.fillRect(0, 0, 1, s.height);
        g2.setPaint(new GradientPaint(0f, 0f, shc, 0f, s.height, bgc, true));
        g2.fillRect(1, 0, 1, s.height);
      } else {
        g2.setPaint(new GradientPaint(0f, 0f, ssc, s.width, 0f, bgc, true));
        g2.fillRect(0, 0, s.width, 1);
        g2.setPaint(new GradientPaint(0f, 0f, shc, s.width, 0f, bgc, true));
        g2.fillRect(0, 1, s.width, 1);
      }
      g2.dispose();
    }
  }
}
