// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTextField field1 = new JTextField("aaaaaaaaaa");
    field1.addFocusListener(new BackgroundFocusListener(new Color(0xE6_E6_FF)));

    JTextField field2 = new JTextField();
    field2.addFocusListener(new BackgroundFocusListener(new Color(0xFF_FF_E6)));

    JTextField field3 = new JTextField("123465789735");
    field3.addFocusListener(new BackgroundFocusListener(new Color(0xFF_E6_E6)));

    Box box = Box.createVerticalBox();
    box.add(makeTitledPanel("Color(230, 230, 255)", field1));
    box.add(Box.createVerticalStrut(5));
    box.add(makeTitledPanel("Color(255, 255, 230)", field2));
    box.add(Box.createVerticalStrut(5));
    box.add(makeTitledPanel("Color(255, 230, 230)", field3));
    box.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    add(box, BorderLayout.NORTH);
    setPreferredSize(new Dimension(320, 240));
  }

  private static Component makeTitledPanel(String title, Component c) {
    JPanel p = new JPanel(new BorderLayout());
    p.setBorder(BorderFactory.createTitledBorder(title));
    p.add(c);
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
    frame.setResizable(false);
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

class BackgroundFocusListener implements FocusListener {
  private final Color color;

  protected BackgroundFocusListener(Color color) {
    this.color = color;
  }

  @Override public void focusGained(FocusEvent e) {
    e.getComponent().setBackground(color);
  }

  @Override public void focusLost(FocusEvent e) {
    e.getComponent().setBackground(UIManager.getColor("TextField.background"));
  }
}
