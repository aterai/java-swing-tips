// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.stream.Stream;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    UIManager.put("Caret.width", 2);
    JTextField field1 = new JTextField("Caret.width: 2");

    JTextField field2 = new JTextField("caretWidth: 4");
    field2.putClientProperty("caretWidth", 4);

    JTextField field3 = new JTextField("caretAspectRatio: 0.4");
    field3.putClientProperty("caretAspectRatio", .4f);

    Color color = new Color(0x64_FF_00_00, true);
    Font font = field1.getFont().deriveFont(32f);
    Box box = Box.createVerticalBox();
    Stream.of(field1, field2, field3).forEach(field -> {
      field.setFont(font);
      field.setCaretColor(color);
      box.add(field);
      box.add(Box.createVerticalStrut(10));
    });

    setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    add(box, BorderLayout.NORTH);
    setPreferredSize(new Dimension(320, 240));
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
