// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.stream.IntStream;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private static final String LF = "\n";

  private MainPanel() {
    super(new BorderLayout(5, 5));
    StringBuilder buf = new StringBuilder();
    IntStream.range(0, 100).forEach(i -> buf.append(i).append(LF));
    String str = buf.toString();
    JScrollPane scroll = new JScrollPane(new JTextArea(str));

    String key = "ScrollPane.useChildTextComponentFocus";
    JCheckBox check = new JCheckBox(key, UIManager.getBoolean(key));
    check.addActionListener(e -> {
      UIManager.put(key, ((JCheckBox) e.getSource()).isSelected());
      SwingUtilities.updateComponentTreeUI(scroll);
    });

    JPanel p = new JPanel(new GridLayout(1, 2));
    p.add(new JScrollPane(new JTextArea(str)));
    p.add(scroll);

    add(check, BorderLayout.NORTH);
    add(p);
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    setPreferredSize(new Dimension(320, 240));
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
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
