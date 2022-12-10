// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new GridLayout(1, 2, 5, 5));
    JPanel p1 = new JPanel();
    p1.setBorder(BorderFactory.createTitledBorder("left"));
    p1.add(new JTextField(16));
    p1.add(new JTextField(16));
    p1.add(new JTextField(16));
    p1.add(new JTextField(16));
    p1.add(new JTextField(16));
    p1.add(new JTextField(16));
    // p1.setFocusTraversalPolicyProvider(true);
    p1.setFocusCycleRoot(true);

    JPanel p2 = new JPanel();
    p2.setBorder(BorderFactory.createTitledBorder("right"));
    p2.add(new JTextField(16));
    p2.add(new JTextField(16));
    p2.add(new JTextField(16));
    p2.add(new JTextField(16));
    p2.add(new JTextField(16));
    p2.add(new JTextField(16));
    p2.setFocusTraversalPolicyProvider(true);
    p2.setFocusCycleRoot(true);

    p2.setFocusTraversalKeys(
        KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS,
        Collections.emptySet());
    p2.setFocusTraversalKeys(
        KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS,
        makeKeyStrokeSet(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, InputEvent.SHIFT_DOWN_MASK)));
    p2.setFocusTraversalKeys(
        KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS,
        makeKeyStrokeSet(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0)));

    add(p1);
    // p1.add(p2);
    add(p2);
    setPreferredSize(new Dimension(320, 240));
  }

  private static Set<AWTKeyStroke> makeKeyStrokeSet(KeyStroke keyStroke) {
    return new HashSet<>(Collections.singletonList(keyStroke));
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
