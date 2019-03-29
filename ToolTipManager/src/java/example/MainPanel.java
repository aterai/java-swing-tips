// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ItemEvent;
import java.util.stream.Stream;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JLabel label = new JLabel("label");
    label.setToolTipText("JLabel - ToolTip");

    JTextField field = new JTextField(20);
    field.setToolTipText("JTextField");
    // TEST: ToolTipManager.sharedInstance().unregisterComponent(field);

    JButton button = new JButton("button");
    button.setToolTipText("JButton - ToolTip");

    JPanel p = new JPanel();
    p.add(label);
    p.add(field);
    p.add(button);
    p.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

    JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(BorderFactory.createTitledBorder("ToolTip Test"));
    panel.add(p, BorderLayout.NORTH);
    panel.add(new JScrollPane(new JTextArea("dummy")));
    add(makeToolPanel(), BorderLayout.NORTH);
    add(panel);
    setPreferredSize(new Dimension(320, 240));
  }

  private static Component makeToolPanel() {
    JRadioButton radio = new JRadioButton("on", true);
    radio.addItemListener(e -> ToolTipManager.sharedInstance().setEnabled(e.getStateChange() == ItemEvent.SELECTED));

    JPanel panel = new JPanel();
    panel.setBorder(BorderFactory.createTitledBorder("ToolTipManager"));
    panel.add(new JLabel("ToolTip enabled:"));
    ButtonGroup group = new ButtonGroup();
    Stream.of(radio, new JRadioButton("off")).forEach(r -> {
      group.add(r);
      panel.add(r);
    });
    return panel;
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
      Toolkit.getDefaultToolkit().beep();
    }
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}
