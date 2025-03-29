// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.HierarchyEvent;
import java.util.stream.Stream;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super();
    UIManager.put("OptionPane.okButtonText", "back");
    // UIManager.put("Button.focus", new Color(0x0, true));

    JButton button1 = new JButton("Default");
    button1.addActionListener(e -> {
      Component p = ((JComponent) e.getSource()).getRootPane();
      JOptionPane.showMessageDialog(p, "Default", "title0", JOptionPane.PLAIN_MESSAGE);
    });

    JLabel label2 = new JLabel("JButton#setFocusPainted(false)");
    label2.addHierarchyListener(e -> {
      Component c = e.getComponent();
      if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && c.isShowing()) {
        descendants(((JComponent) c).getRootPane())
            .filter(JButton.class::isInstance)
            .map(JButton.class::cast)
            .findFirst()
            .ifPresent(b -> {
              b.setFocusPainted(false);
              b.setText("back2");
            });
      }
    });
    JButton button2 = new JButton("showMessageDialog + HierarchyListener");
    button2.addActionListener(e -> {
      Component p = ((JComponent) e.getSource()).getRootPane();
      JOptionPane.showMessageDialog(p, label2, "title2", JOptionPane.PLAIN_MESSAGE);
    });

    // Customizing Button Text - How to Make Dialogs (The Java™ Tutorials > ...)
    // https://docs.oracle.com/javase/tutorial/uiswing/components/dialog.html#button
    JButton button3 = new JButton("showOptionDialog");
    button3.addActionListener(e -> {
      Object[] options = {"Yes, please"}; // {"Yes, please", "No way!"};
      JOptionPane.showOptionDialog(
          ((JComponent) e.getSource()).getRootPane(),
          "Would you like green eggs and ham?",
          "A Silly Question",
          JOptionPane.DEFAULT_OPTION,
          JOptionPane.PLAIN_MESSAGE,
          null, options, options[0]);
    });

    Stream.of(button1, button2, button3).forEach(this::add);
    setPreferredSize(new Dimension(320, 240));
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
