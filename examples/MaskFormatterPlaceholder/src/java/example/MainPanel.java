// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.text.ParseException;
import java.util.Optional;
import java.util.stream.Stream;
import javax.swing.*;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.MaskFormatter;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    Box box = Box.createVerticalBox();
    box.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

    String mask = "###-####";
    JFormattedTextField field0 = new JFormattedTextField();
    createFormatter(mask)
        .ifPresent(f -> field0.setFormatterFactory(new DefaultFormatterFactory(f)));
    box.add(makeTitledPanel("new MaskFormatter(\"###-####\")", field0));
    box.add(Box.createVerticalStrut(15));

    JFormattedTextField field1 = new JFormattedTextField();
    createFormatter(mask).ifPresent(formatter -> {
      formatter.setPlaceholderCharacter('_');
      field1.setFormatterFactory(new DefaultFormatterFactory(formatter));
    });

    JFormattedTextField field2 = new JFormattedTextField();
    createFormatter(mask).ifPresent(formatter -> {
      formatter.setPlaceholderCharacter('_');
      formatter.setPlaceholder("000-0000");
      field2.setFormatterFactory(new DefaultFormatterFactory(formatter));
    });
    box.add(makeTitledPanel("MaskFormatter#setPlaceholderCharacter('_')", field1));
    box.add(Box.createVerticalStrut(15));

    Font font = new Font(Font.MONOSPACED, Font.PLAIN, 18);
    Insets insets = new Insets(1, 1 + 18 / 2, 1, 1);
    Stream.of(field0, field1, field2).forEach(tf -> {
      tf.setFont(font);
      tf.setColumns(mask.length() + 1);
      tf.setMargin(insets);
    });
    box.add(makeTitledPanel("MaskFormatter#setPlaceholder(\"000-0000\")", field2));
    box.add(Box.createVerticalGlue());

    add(box, BorderLayout.NORTH);
    setPreferredSize(new Dimension(320, 240));
  }

  private static Optional<MaskFormatter> createFormatter(String s) {
    Optional<MaskFormatter> op;
    try {
      op = Optional.of(new MaskFormatter(s));
    } catch (ParseException ex) {
      op = Optional.empty();
    }
    return op;
  }

  private static Component makeTitledPanel(String title, Component c) {
    JPanel p = new JPanel();
    p.setBorder(BorderFactory.createTitledBorder(title));
    p.add(c);
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
