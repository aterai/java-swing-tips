// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private static final String KEY = "Button.defaultButtonFollowsFocus";
  private final JButton b1 = new JButton("Button1");
  private final JButton b2 = new JButton("Button2");

  private MainPanel() {
    super(new BorderLayout(5, 5));
    Box box = Box.createHorizontalBox();
    box.setBorder(BorderFactory.createTitledBorder(KEY));
    JRadioButton r1 = new JRadioButton("TRUE");
    JRadioButton r2 = new JRadioButton("FALSE");
    if (UIManager.getBoolean(KEY)) {
      r1.setSelected(true);
    } else {
      r2.setSelected(true);
    }
    ButtonGroup bg = new ButtonGroup();
    ActionListener al = e -> UIManager.put(KEY, r1.equals(e.getSource()));
    Arrays.asList(r1, r2).forEach(r -> {
      r.addActionListener(al);
      bg.add(r);
      box.add(r);
    });
    box.add(Box.createHorizontalGlue());

    JPanel p = new JPanel(new GridLayout(2, 1));
    p.add(makeRadioPane());
    p.add(box);

    add(p, BorderLayout.NORTH);
    add(makeSampleTextComponent());
    add(makeDefaultButtonPanel(), BorderLayout.SOUTH);
    setPreferredSize(new Dimension(320, 240));
  }

  private Box makeDefaultButtonPanel() {
    Box box = Box.createHorizontalBox();
    box.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));
    box.add(Box.createHorizontalGlue());
    box.add(b1);
    box.add(b2);
    b2.addActionListener(e -> Toolkit.getDefaultToolkit().beep());
    return box;
  }

  private Box makeRadioPane() {
    Box box = Box.createHorizontalBox();
    box.setBorder(BorderFactory.createTitledBorder("JRootPane#setDefaultButton(...)"));
    Map<String, JButton> map = Collections.synchronizedMap(new LinkedHashMap<>());
    map.put("null", null);
    map.put("Button1", b1);
    map.put("Button2", b2);
    ButtonGroup bg = new ButtonGroup();
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
    bg.getElements().nextElement().setSelected(true);
    return box;
  }

  private static Component makeSampleTextComponent() {
    JPanel p = new JPanel(new BorderLayout(2, 2));
    p.setBorder(BorderFactory.createTitledBorder("JTextComponent"));
    p.add(new JTextField(), BorderLayout.NORTH);
    p.add(new JScrollPane(new JTextArea()));
    return p;
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
