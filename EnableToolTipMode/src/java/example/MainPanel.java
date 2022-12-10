// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Objects;
import java.util.stream.Stream;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private static final String TOOLTIP_MODE = "ToolTipManager.enableToolTipMode";

  private MainPanel() {
    super(new BorderLayout());
    String mode = UIManager.getString(TOOLTIP_MODE);
    // System.out.println(mode);

    String allWindows = "allWindows";
    JRadioButton radio1 = new JRadioButton(allWindows, Objects.equals(allWindows, mode));
    radio1.setToolTipText("ToolTip: " + allWindows);
    radio1.addItemListener(e -> UIManager.put(TOOLTIP_MODE, allWindows));

    String activeApp = "activeApplication";
    JRadioButton radio2 = new JRadioButton(activeApp, Objects.equals(activeApp, mode));
    radio2.setToolTipText("ToolTip: " + activeApp);
    radio2.addItemListener(e -> UIManager.put(TOOLTIP_MODE, activeApp));

    JPanel panel = new JPanel();
    panel.setBorder(BorderFactory.createTitledBorder(TOOLTIP_MODE));
    ButtonGroup group = new ButtonGroup();
    Stream.of(radio1, radio2).forEach(r -> {
      group.add(r);
      panel.add(r);
    });

    add(panel, BorderLayout.NORTH);
    add(makeUI());
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    setPreferredSize(new Dimension(320, 240));
  }

  private static Component makeUI() {
    JLabel label = new JLabel("label");
    label.setToolTipText("JLabel");

    JTextField field = new JTextField(20);
    field.setToolTipText("JTextField");

    JButton button = new JButton("button");
    button.setToolTipText("JButton");

    JPanel p = new JPanel();
    p.setBorder(BorderFactory.createTitledBorder("test: " + TOOLTIP_MODE));
    p.add(label);
    p.add(field);
    p.add(button);
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
    frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}
