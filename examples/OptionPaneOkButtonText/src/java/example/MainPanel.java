// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.HierarchyEvent;
import java.util.logging.Logger;
import java.util.stream.Stream;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super();
    UIManager.put("OptionPane.okButtonText", "back");
    // UIManager.put("Button.focus", new Color(0x0, true));
    JButton button1 = makeButton1();
    JButton button2 = makeButton2();
    JButton button3 = makeButton3();
    Stream.of(button1, button2, button3).forEach(this::add);
    setPreferredSize(new Dimension(320, 240));
  }

  private static JButton makeButton1() {
    JButton button = new JButton("Default");
    button.addActionListener(e -> {
      Component p = ((JComponent) e.getSource()).getRootPane();
      JOptionPane.showMessageDialog(p, "Default", "title0", JOptionPane.PLAIN_MESSAGE);
    });
    return button;
  }

  private static JButton makeButton2() {
    JLabel label = new JLabel("JButton#setFocusPainted(false)");
    label.addHierarchyListener(e -> {
      Component c = e.getComponent();
      if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && c.isShowing()) {
        setButtonText((JComponent) c, "back2");
      }
    });
    JButton button = new JButton("showMessageDialog + HierarchyListener");
    button.addActionListener(e -> {
      Component p = ((JComponent) e.getSource()).getRootPane();
      JOptionPane.showMessageDialog(p, label, "title2", JOptionPane.PLAIN_MESSAGE);
    });
    return button;
  }

  public static void setButtonText(JComponent parent, String title) {
    descendants(parent.getRootPane())
        .filter(JButton.class::isInstance)
        .map(JButton.class::cast)
        .findFirst()
        .ifPresent(b -> {
          b.setFocusPainted(false);
          b.setText(title);
        });
  }

  private static JButton makeButton3() {
    // Customizing Button Text - How to Make Dialogs (The Java? Tutorials > ...)
    // https://docs.oracle.com/javase/tutorial/uiswing/components/dialog.html#button
    JButton button = new JButton("showOptionDialog");
    button.addActionListener(e -> {
      Object[] options = {"Yes, please"}; // {"Yes, please", "No way!"};
      JOptionPane.showOptionDialog(
          ((JComponent) e.getSource()).getRootPane(),
          "Would you like green eggs and ham?",
          "A Silly Question",
          JOptionPane.DEFAULT_OPTION,
          JOptionPane.PLAIN_MESSAGE,
          null, options, options[0]);
    });
    return button;
  }

  public static Stream<Component> descendants(Container parent) {
    return Stream.of(parent.getComponents())
        .filter(Container.class::isInstance).map(Container.class::cast)
        .flatMap(c -> Stream.concat(Stream.of(c), descendants(c)));
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
