// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    // JPanel p1 = new JPanel();
    // Box p1 = Box.createHorizontalBox();
    JPanel p1 = new JPanel(new GridLayout(1, 0, 2, 2));
    p1.setBorder(BorderFactory.createTitledBorder("GridLayout"));
    p1.add(new JScrollPane(new JTable(6, 3)));
    p1.add(new JScrollPane(new JTree()));
    p1.add(new JScrollPane(new JTextArea("JTextArea")));

    JPanel p2 = new JPanel(new GridBagLayout());
    p2.setBorder(BorderFactory.createTitledBorder("GridBagLayout"));
    GridBagConstraints c = new GridBagConstraints();
    c.insets = new Insets(5, 5, 5, 0);
    c.fill = GridBagConstraints.BOTH;
    c.weightx = 1d;
    c.weighty = 1d;
    // c.gridx = GridBagConstraints.RELATIVE;
    // c.gridx = 0;
    p2.add(new JScrollPane(new JTable(6, 3)), c);
    // c.gridx = 1;
    p2.add(new JScrollPane(new JTree()), c);
    // c.gridx = 2;
    p2.add(new JScrollPane(new JTextArea("JTextArea")), c);

    JButton button = new JButton("rotate");
    button.setFocusable(false);
    button.addActionListener(e -> {
      rotateChildComponent(p1);
      rotateChildComponent(p2);
    });

    JPanel panel = new JPanel(new GridLayout(2, 1));
    panel.add(p1);
    panel.add(p2);

    add(panel);
    add(button, BorderLayout.SOUTH);
    setPreferredSize(new Dimension(320, 240));
  }

  public static void rotateChildComponent(Container p) {
    p.setComponentZOrder(p.getComponent(p.getComponentCount() - 1), 0);
    p.revalidate();
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
