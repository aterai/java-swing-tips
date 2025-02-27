// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Optional;
import javax.swing.*;
import javax.swing.border.Border;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new GridLayout(0, 1, 0, 10));
    JPanel p1 = new JPanel(new GridBagLayout());
    Border b1 = BorderFactory.createCompoundBorder(
        BorderFactory.createTitledBorder("GridBagLayout"),
        BorderFactory.createMatteBorder(5, 10, 15, 20, Color.RED)
    );
    p1.setBorder(b1);
    p1.add(new JButton("GridBagLayout"));
    // p1.add(new JButton("append"));
    add(p1);

    JPanel p2 = new JPanel(new CenterLayout());
    Border b2 = BorderFactory.createCompoundBorder(
        BorderFactory.createTitledBorder("CenterLayout"),
        BorderFactory.createMatteBorder(5, 10, 15, 20, Color.GREEN)
    );
    p2.setBorder(b2);
    p2.add(new JButton("CenterLayout"));
    // p2.add(new JButton("ignore"));
    add(p2);
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

// @see javax.swing.colorchooser.CenterLayout
class CenterLayout implements LayoutManager {
  @Override public void addLayoutComponent(String name, Component comp) {
    /* not needed */
  }

  @Override public void removeLayoutComponent(Component comp) {
    /* not needed */
  }

  @Override public Dimension preferredLayoutSize(Container container) {
    return Optional.ofNullable(container.getComponent(0)).map(c -> {
      Dimension size = c.getPreferredSize();
      Insets insets = container.getInsets();
      size.width += insets.left + insets.right;
      size.height += insets.top + insets.bottom;
      return size;
    }).orElseGet(Dimension::new);
  }

  @Override public Dimension minimumLayoutSize(Container c) {
    return preferredLayoutSize(c);
  }

  @Override public void layoutContainer(Container container) {
    Component c = container.getComponent(0);
    c.setSize(c.getPreferredSize());
    if (container instanceof JComponent) {
      Dimension size = c.getSize();
      Rectangle r = SwingUtilities.calculateInnerArea((JComponent) container, null);
      int x = r.x + (r.width - size.width) / 2;
      int y = r.y + (r.height - size.height) / 2;
      c.setBounds(x, y, size.width, size.height);
    }
  }
}
