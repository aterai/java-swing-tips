// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JPanel p1 = new JPanel(new GridLayout(1, 0, 2, 2));
    p1.setBorder(BorderFactory.createTitledBorder("GridLayout"));
    createComponents().forEach(p1::add);

    JPanel p2 = new JPanel(new GridBagLayout());
    p2.setBorder(BorderFactory.createTitledBorder("GridBagLayout"));
    GridBagConstraints c = new GridBagConstraints();
    c.insets = new Insets(5, 5, 5, 0);
    c.fill = GridBagConstraints.BOTH;
    c.weightx = 1d;
    c.weighty = 1d;
    // Keep gridx and gridy at the default GridBagConstraints.RELATIVE,
    // otherwise changing the z-order does not change the layout position
    createComponents().forEach(child -> p2.add(child, c));

    JButton button = new JButton("rotate");
    button.setFocusable(false);
    button.addActionListener(e -> {
      rotateChildComponents(p1);
      rotateChildComponents(p2);
    });

    JPanel panel = new JPanel(new GridLayout(2, 1));
    panel.add(p1);
    panel.add(p2);

    add(panel);
    add(button, BorderLayout.SOUTH);
    setPreferredSize(new Dimension(320, 240));
  }

  private static List<Component> createComponents() {
    return Arrays.asList(
        new JScrollPane(new JTable(6, 3)),
        new JScrollPane(new JTree()),
        new JScrollPane(new JTextArea("JTextArea")));
  }

  // Moves the last child component to the front (z-order index 0),
  // which shifts the other child components back by one
  private static void rotateChildComponents(Container parent) {
    int count = parent.getComponentCount();
    if (count > 0) {
      parent.setComponentZOrder(parent.getComponent(count - 1), 0);
      parent.revalidate();
      parent.repaint();
    }
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
