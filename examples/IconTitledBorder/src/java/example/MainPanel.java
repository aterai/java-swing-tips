// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.net.URL;
import java.util.Objects;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new GridLayout(4, 1, 5, 5));
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    URL url = cl.getResource("example/16x16.png");
    Icon icon;
    String path;
    if (Objects.nonNull(url)) {
      path = url.toString();
      icon = new ImageIcon(url);
    } else {
      path = "html.missingImage";
      icon = UIManager.getIcon(path);
    }
    JLabel label = new JLabel("test");
    label.setIcon(icon);

    String title1 = String.format("<html><img src='%s' />test", path);
    add(makeComponent(title1, BorderFactory.createTitledBorder(title1)));

    String img = String.format("<img src='%s'/>", path);
    String tbl = "<table cellpadding='0'>";
    String title2 = String.format("<html>%s<tr><td>%s</td><td>test</td></tr>", tbl, img);
    add(makeComponent(title2, BorderFactory.createTitledBorder(title2)));

    add(makeComponent("TitledBorder#paintBorder(...)", new TitledBorder("  test") {
      @Override public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        super.paintBorder(c, g, x, y, width, height);
        icon.paintIcon(c, g, 5, 0);
      }
    }));

    Border border = new ComponentTitledBorder(label, UIManager.getBorder("TitledBorder.border"));
    add(makeComponent("ComponentTitledBorder", border));

    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    setPreferredSize(new Dimension(320, 240));
  }

  private static Component makeComponent(String str, Border border) {
    JLabel l = new JLabel();
    l.setBorder(border);
    l.putClientProperty("html.disable", Boolean.TRUE);
    l.setText(str);
    return l;
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

class ComponentTitledBorder implements Border, SwingConstants {
  private static final int OFFSET = 5;
  private final Component comp;
  private final Border border;

  protected ComponentTitledBorder(Component comp, Border border) {
    this.comp = comp;
    this.border = border;
    if (comp instanceof JComponent) {
      ((JComponent) comp).setOpaque(true);
    }
  }

  @Override public boolean isBorderOpaque() {
    return true;
  }

  @Override public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
    if (c instanceof Container) {
      Insets borderInsets = border.getBorderInsets(c);
      Insets insets = getBorderInsets(c);
      int temp = (insets.top - borderInsets.top) / 2;
      border.paintBorder(c, g, x, y + temp, width, height - temp);
      Dimension size = comp.getPreferredSize();
      Rectangle rect = new Rectangle(OFFSET, 0, size.width, size.height);
      SwingUtilities.paintComponent(g, comp, (Container) c, rect);
      comp.setBounds(rect);
    }
  }

  @Override public Insets getBorderInsets(Component c) {
    Dimension size = comp.getPreferredSize();
    Insets insets = border.getBorderInsets(c);
    insets.top = Math.max(insets.top, size.height);
    return insets;
  }
}
