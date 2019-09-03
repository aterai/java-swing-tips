// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionListener;
import java.util.stream.Stream;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new GridLayout(2, 1));
    add(makeUI1());
    add(makeUI2());
    setPreferredSize(new Dimension(320, 240));
  }

  private static Component makeUI1() {
    JPanel p = new JPanel(new GridBagLayout());
    p.setBorder(BorderFactory.createTitledBorder("Override JToggleButton#getPreferredSize(...)"));
    GridBagConstraints c = new GridBagConstraints();
    c.insets = new Insets(5, 5, 5, 5);
    ActionListener al = e -> p.revalidate();
    ButtonGroup bg = new ButtonGroup();
    Stream.of("a1", "a2", "a3").forEach(s -> {
      JToggleButton b = new JToggleButton(s) {
        @Override public Dimension getPreferredSize() {
          int v = isSelected() ? 80 : 50;
          return new Dimension(v, v);
        }
      };
      b.addActionListener(al);
      bg.add(b);
      p.add(b, c);
    });
    return p;
  }

  private static Component makeUI2() {
    JPanel p = new JPanel(new GridBagLayout());
    p.setBorder(BorderFactory.createTitledBorder("Override FlowLayout#layoutContainer(...)"));
    p.setLayout(new FlowLayout() {
      @Override public void layoutContainer(Container target) {
        synchronized (target.getTreeLock()) {
          int nmembers = target.getComponentCount();
          if (nmembers <= 0) {
            return;
          }
          Insets insets = target.getInsets();
          // int vgap = getVgap();
          int hgap = getHgap();
          int rowh = target.getHeight();
          int x = insets.left + hgap;
          for (int i = 0; i < nmembers; i++) {
            Component m = target.getComponent(i);
            if (m.isVisible() && m instanceof AbstractButton) {
              int v = ((AbstractButton) m).isSelected() ? 80 : 50;
              Dimension d = new Dimension(v, v);
              m.setSize(d);
              int y = (rowh - v) / 2;
              m.setLocation(x, y);
              x += d.width + hgap;
            }
          }
        }
      }
    });
    ActionListener al = e -> p.revalidate();
    ButtonGroup bg = new ButtonGroup();
    Stream.of("b1", "b2", "b3").forEach(s -> {
      JToggleButton b = new JToggleButton(s);
      b.addActionListener(al);
      bg.add(b);
      p.add(b);
    });
    return p;
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
      ex.printStackTrace();
      Toolkit.getDefaultToolkit().beep();
    }
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}
