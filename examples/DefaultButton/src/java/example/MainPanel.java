// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private final JButton b1 = new JButton("Button1");
  private final JButton b2 = new JButton("Button2");

  private MainPanel() {
    super(new BorderLayout(5, 5));
    JPanel p = new JPanel(new BorderLayout(2, 2));
    p.setBorder(BorderFactory.createTitledBorder("JTextComponent"));
    p.add(new JTextField(), BorderLayout.NORTH);
    p.add(new JScrollPane(new JTextArea()));

    add(makeRadioPane(), BorderLayout.NORTH);
    add(p);
    add(makeButtonPane(), BorderLayout.SOUTH);
    setPreferredSize(new Dimension(320, 240));
  }

  private Box makeButtonPane() {
    Box box = Box.createHorizontalBox();
    box.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));
    box.add(Box.createHorizontalGlue());
    box.add(b1);
    box.add(b2);
    b2.addActionListener(e -> Toolkit.getDefaultToolkit().beep());
    return box;
  }

  private Box makeRadioPane() {
    ButtonGroup bg = new ButtonGroup();
    Box box = Box.createHorizontalBox();
    Map<String, JButton> map = Collections.synchronizedMap(new LinkedHashMap<>());
    map.put("null", null);
    map.put("Button1", b1);
    map.put("Button2", b2);
    ActionListener al = e -> Optional.ofNullable(box.getRootPane())
        .ifPresent(r -> r.setDefaultButton(map.get(e.getActionCommand())));
    map.forEach((key, value) -> {
      JRadioButton r = new JRadioButton(key);
      r.setActionCommand(key);
      r.addActionListener(al);
      bg.add(r);
      box.add(r);
    });
    box.add(Box.createHorizontalGlue());
    box.setBorder(BorderFactory.createTitledBorder("JRootPane#setDefaultButton: "));
    bg.getElements().nextElement().setSelected(true);
    return box;
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
