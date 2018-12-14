// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;
import javax.swing.plaf.metal.MetalCheckBoxIcon;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    // System.setProperty("swing.noxp", "true");
    // UIManager.put("CheckBox.interiorBackground", new ColorUIResource(Color.GREEN));
    // UIManager.put("CheckBox.darkShadow", new ColorUIResource(Color.RED));
    // UIManager.put("CheckBox.icon", new IconUIResource(new CheckBoxIcon()));

    JCheckBox cb1 = new JCheckBox("bbbbbbbbbb");
    cb1.setIcon(new CheckBoxIcon());

    JCheckBox cb2 = new JCheckBox("ccccccccccccccc");
    cb2.setIcon(new CheckBoxIcon2());

    JCheckBox cb3 = new JCheckBox("dddddddd");
    cb3.setIcon(new CheckBoxIcon3());

    Box box = Box.createVerticalBox();
    box.add(makeTitledPanel("Default", new JCheckBox("aaaaaaaaaaaaa")));
    box.add(makeTitledPanel("WindowsIconFactory", cb1));
    box.add(makeTitledPanel("CheckBox.icon+RED", cb2));
    box.add(makeTitledPanel("MetalCheckBoxIcon+GRAY", cb3));
    add(box, BorderLayout.NORTH);
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    setPreferredSize(new Dimension(320, 240));
  }

  private static Component makeTitledPanel(String title, Component c) {
    JPanel p = new JPanel(new BorderLayout());
    p.setBorder(BorderFactory.createTitledBorder(title));
    p.add(c);
    return p;
  }

  public static void main(String... args) {
    EventQueue.invokeLater(new Runnable() {
      @Override public void run() {
        createAndShowGui();
      }
    });
  }

  public static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
      ex.printStackTrace();
    }
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

class CheckBoxIcon3 implements Icon {
  private final Icon orgIcon = new MetalCheckBoxIcon();

  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.translate(x, y);
    orgIcon.paintIcon(c, g2, 0, 0);
    g2.setColor(new Color(255, 155, 155, 100));
    g2.fillRect(2, 2, getIconWidth() - 4, getIconHeight() - 4);
    g2.dispose();
  }

  @Override public int getIconWidth() {
    return orgIcon.getIconWidth();
  }

  @Override public int getIconHeight() {
    return orgIcon.getIconHeight();
  }
}

class CheckBoxIcon2 implements Icon {
  private final Icon orgIcon = UIManager.getIcon("CheckBox.icon");

  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.translate(x, y);
    orgIcon.paintIcon(c, g2, 0, 0);
    if (c instanceof AbstractButton) {
      AbstractButton b = (AbstractButton) c;
      ButtonModel model = b.getModel();
      g2.setColor(new Color(255, 155, 155, 100));
      g2.fillRect(2, 2, getIconWidth() - 4, getIconHeight() - 4);
      if (model.isSelected()) {
        g2.setColor(Color.RED);
        g2.drawLine(9, 3, 9, 3);
        g2.drawLine(8, 4, 9, 4);
        g2.drawLine(7, 5, 9, 5);
        g2.drawLine(6, 6, 8, 6);
        g2.drawLine(3, 7, 7, 7);
        g2.drawLine(4, 8, 6, 8);
        g2.drawLine(5, 9, 5, 9);
        g2.drawLine(3, 5, 3, 5);
        g2.drawLine(3, 6, 4, 6);
      }
    }
    g2.dispose();
  }

  @Override public int getIconWidth() {
    return orgIcon.getIconWidth();
  }

  @Override public int getIconHeight() {
    return orgIcon.getIconHeight();
  }
}

class CheckBoxIcon implements Icon {
  // com/sun/java/swing/plaf/windows/WindowsIconFactory.java
  private static final int CSIZE = 13;

  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    if (!(c instanceof JCheckBox)) {
      return;
    }
    JCheckBox cb = (JCheckBox) c;
    ButtonModel model = cb.getModel();
    Graphics2D g2 = (Graphics2D) g.create();
    g2.translate(x, y);

    // outer bevel
    if (cb.isBorderPaintedFlat()) {
      g2.setColor(UIManager.getColor("CheckBox.shadow"));
      g2.drawRect(1, 1, CSIZE - 3, CSIZE - 3);

      if (model.isPressed() && model.isArmed()) {
        g2.setColor(UIManager.getColor("CheckBox.background"));
      } else {
        g2.setColor(UIManager.getColor("CheckBox.interiorBackground"));
      }
      g2.fillRect(2, 2, CSIZE - 4, CSIZE - 4);
    } else {
      // Outer top/left
      g2.setColor(UIManager.getColor("CheckBox.shadow"));
      g2.drawLine(0, 0, 11, 0);
      g2.drawLine(0, 1, 0, 11);

      // Outer bottom/right
      g2.setColor(UIManager.getColor("CheckBox.highlight"));
      g2.drawLine(12, 0, 12, 12);
      g2.drawLine(0, 12, 11, 12);

      // Inner top.left
      g2.setColor(UIManager.getColor("CheckBox.darkShadow"));
      g2.drawLine(1, 1, 10, 1);
      g2.drawLine(1, 2, 1, 10);

      // Inner bottom/right
      g2.setColor(UIManager.getColor("CheckBox.light"));
      g2.drawLine(1, 11, 11, 11);
      g2.drawLine(11, 1, 11, 10);

      // inside box
      Color color = new Color(255, 155, 155);
      if (model.isPressed() && model.isArmed()) {
        // g2.setColor(UIManager.getColor("CheckBox.background"));
        g2.setColor(color.brighter());
      } else {
        // g2.setColor(UIManager.getColor("CheckBox.interiorBackground"));
        g2.setColor(color);
      }
      g2.fillRect(2, 2, CSIZE - 4, CSIZE - 4);
    }

    // if (model.isEnabled()) {
    //   g2.setColor(UIManager.getColor("CheckBox.foreground"));
    // } else {
    //   g2.setColor(UIManager.getColor("CheckBox.shadow"));
    // }

    // paint check
    if (model.isSelected()) {
      g2.setColor(Color.BLUE);
      g2.drawLine(9, 3, 9, 3);
      g2.drawLine(8, 4, 9, 4);
      g2.drawLine(7, 5, 9, 5);
      g2.drawLine(6, 6, 8, 6);
      g2.drawLine(3, 7, 7, 7);
      g2.drawLine(4, 8, 6, 8);
      g2.drawLine(5, 9, 5, 9);
      g2.drawLine(3, 5, 3, 5);
      g2.drawLine(3, 6, 4, 6);
    }

    if (model.isRollover()) {
      g2.setColor(Color.ORANGE);
      g2.drawLine(1, 1, 1 + CSIZE - 3, 1);
      g2.drawLine(1, 1, 1, 1 + CSIZE - 3);
    }
    g2.dispose();
  }

  @Override public int getIconWidth() {
    return CSIZE;
  }

  @Override public int getIconHeight() {
    return CSIZE;
  }
}
